<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue';
import { Message, chatService } from '../services/ChatService';
// 导入marked库用于Markdown渲染
import { marked } from 'marked';

// 聊天消息列表
const messages = ref<Message[]>([]);
// 用户输入的消息
const userInput = ref('');
// 是否正在发送消息
const isSending = ref(false);
// 记录展开状态的消息ID
const expandedMessages = ref<Set<string>>(new Set());

// 本地存储的键名
const STORAGE_KEY = 'chat_messages';

// 滚动到最新消息
const scrollToBottom = async () => {
  await nextTick();
  const chatContainer = document.getElementById('chat-container');
  if (chatContainer) {
    chatContainer.scrollTop = chatContainer.scrollHeight;
  }
};

// 从本地存储加载消息
const loadMessages = () => {
  try {
    const storedMessages = localStorage.getItem(STORAGE_KEY);
    if (storedMessages) {
      // 解析并转换时间戳字符串为Date对象
      const parsedMessages = JSON.parse(storedMessages, (key, value) => {
        if (key === 'timestamp') {
          return new Date(value);
        }
        return value;
      });
      messages.value = parsedMessages;
    }
  } catch (error) {
    console.error('加载历史消息失败:', error);
  }
};

// 保存消息到本地存储
const saveMessages = () => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(messages.value));
  } catch (error) {
    console.error('保存消息失败:', error);
  }
};

// 发送消息
const sendMessage = async () => {
  const content = userInput.value.trim();
  if (!content || isSending.value) return;
  
  // 添加用户消息
  const userMessage: Message = {
    id: Date.now().toString(),
    content,
    sender: 'user',
    timestamp: new Date(),
  };
  messages.value.push(userMessage);
  userInput.value = '';
  
  // 保存消息到本地存储
  saveMessages();
  
  // 滚动到底部
  await scrollToBottom();
  
  // 获取AI回复
  isSending.value = true;
  try {
    // 传递历史对话
    const aiMessage = await chatService.sendMessage(content, messages.value);
    messages.value.push(aiMessage);
    
    // 再次保存消息到本地存储
    saveMessages();
    
    await scrollToBottom();
  } catch (error) {
    console.error('发送消息失败:', error);
  } finally {
    isSending.value = false;
  }
};

// 处理键盘事件
const handleKeyPress = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault();
    sendMessage();
  }
};

// 切换消息展开/折叠状态
const toggleExpand = (messageId: string) => {
  if (expandedMessages.value.has(messageId)) {
    expandedMessages.value.delete(messageId);
  } else {
    expandedMessages.value.add(messageId);
  }
};

// 检查消息是否展开
const isExpanded = (messageId: string): boolean => {
  return expandedMessages.value.has(messageId);
};

// 清空对话历史
const clearHistory = () => {
  if (confirm('确定要清空所有对话历史吗？')) {
    // 保留欢迎消息（如果存在）
    const welcomeMessage = messages.value.find(msg => msg.id === 'welcome');
    if (welcomeMessage) {
      messages.value = [welcomeMessage];
    } else {
      messages.value = [];
    }
    saveMessages();
  }
};

// Markdown渲染函数
const renderMarkdown = (text: string): string => {
  try {
    return marked.parse(text).toString();
  } catch (error) {
    console.error('Markdown渲染失败:', error);
    return text;
  }
};

// 组件挂载时
onMounted(() => {
  // 先尝试加载历史消息
  loadMessages();
  
  // 如果没有历史消息，添加欢迎消息
  if (messages.value.length === 0) {
    messages.value.push({
      id: 'welcome',
      content: '你好呀！看来今天心情不错呢～ 😊 是想随便聊聊，还是有什么事情需要我帮忙呀？',
      sender: 'ai',
      timestamp: new Date(),
      thinking: '用户看起来心情很好，使用了笑脸表情，可以采用轻松愉快的语气进行交流。用户可能希望获得帮助或只是想聊天，需要判断用户的意图。'
    });
    saveMessages();
  }
  
  scrollToBottom();
});
</script>

<template>
  <div class="chat-room">
    <div class="chat-header">
      <h2>AI对话助手</h2>
      <button class="clear-history-btn" @click="clearHistory" title="清空历史">
        +
      </button>
    </div>
    
    <div id="chat-container" class="chat-container">
      <div v-for="message in messages" :key="message.id" :class="['message', message.sender]">
        
        <div v-if="message.sender === 'ai' && message.thinking" class="message-thinking-section">
            <button 
                class="thinking-toggle"
                @click="toggleExpand(message.id)"
            >
            {{ isExpanded(message.id) ? '▲ 收起思考过程' : '▼ 查看思考过程' }}
            </button>
            <transition name="slide">
                <div v-if="isExpanded(message.id)" class="thinking-content">
                    <pre>{{ message.thinking }}</pre>
                </div>
            </transition>
        </div>

        <!-- 使用v-html和Markdown渲染函数 -->
        <div class="message-content" v-html="renderMarkdown(message.content)"></div>
        <div class="message-time">{{ new Date(message.timestamp).toLocaleTimeString() }}</div>
      </div>

      <div v-if="isSending" class="message ai typing">
        <div class="message-content">
          <span class="typing-indicator">
            <span></span>
            <span></span>
            <span></span>
          </span>
        </div>
      </div>

    </div>
    <div class="chat-input-area">
      <textarea
        v-model="userInput"
        @keypress="handleKeyPress"
        placeholder="请输入您的问题..."
        :disabled="isSending"
        rows="1"
      ></textarea>
      <button @click="sendMessage" :disabled="isSending">
        {{ isSending ? '发送中...' : '发送' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.chat-header {
  background-color: #42b883;
  color: white;
  padding: 1rem;
  text-align: center;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.clear-history-btn {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  border: none; /* 移除默认边框 */
  outline: none; /* 移除点击后的轮廓 */
  border-radius: 50%;
  width: 32px;
  height: 32px;
  font-size: 1.2rem;
  cursor: pointer;
  transition: background-color 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  transform: rotate(45deg);
}

.clear-history-btn:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

.thinking-content {
  width: 100%;
  max-width: 100%;
  min-width: 0;
  overflow-wrap: break-word;
  box-sizing: border-box;
  background-color: #f8f8f8;
  border-left: 3px solid #ccc;
  padding: 0.5rem;
  font-size: 0.85rem;
  color: #666;
  line-height: 1.4;
  border-radius: 4px;
  text-align: left;
}

.message.ai .thinking-content {
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

/* 确保pre标签不会超出容器 */
.thinking-content pre {
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 0.9em;
  line-height: 1.6;
  max-width: 100%;
  overflow: auto;
  margin: 0;
  box-sizing: border-box;
}

.slide-enter-active {
  transition: all 0.3s ease-out;
}

.slide-leave-active {
  transition: all 0.2s cubic-bezier(1, 0.5, 0.8, 1);
}

/* 修复过渡动画，避免影响布局 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease-out;
  max-height: 1000px;
  overflow: hidden;
}

.slide-enter-from,
.slide-leave-to {
  max-height: 0;
  opacity: 0;
}

.chat-room {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 800px;
  margin: 0 auto;
  background-color: #f5f5f5;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.chat-header {
  background-color: #42b883;
  color: white;
  padding: 1rem;
  text-align: center;
}

.chat-header h2 {
  margin: 0;
  font-size: 1.5rem;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  background-color: #fff;
}

/* 添加自定义滚动条样式 */
.chat-container::-webkit-scrollbar {
  width: 6px;
}

/* 隐藏滚动条轨道背景 */
.chat-container::-webkit-scrollbar-track {
  background: transparent;
}

/* 只保留滚动条滑块 */
.chat-container::-webkit-scrollbar-thumb {
  background: rgba(120, 120, 120, 0.3); /* 使用半透明颜色，更隐蔽 */
  border-radius: 3px;
  transition: background-color 0.2s ease;
}

/* 鼠标悬停时滑块颜色变深 */
.chat-container::-webkit-scrollbar-thumb:hover {
  background: rgba(80, 80, 80, 0.5);
}


.message {
  max-width: 70%;
  padding: 0.75rem 1rem;
  border-radius: 18px;
  position: relative;
  word-wrap: break-word;
  box-sizing: border-box;
  width: fit-content; /* 确保消息宽度根据内容自适应 */
}

.message.user {
  align-self: flex-end;
  background-color: #42b883;
  color: white;
  border-bottom-right-radius: 4px;
  /* width: fit-content; /* 确保用户消息也是适应内容宽度 */ 
}

.message.ai {
  align-self: flex-start;
  background-color: #f0f0f0;
  color: #333;
  border-bottom-left-radius: 4px;
  box-sizing: border-box;
  /* 移除了固定width，改为min-width和max-width控制 */
  min-width: 0;
  max-width: 70%;
  width: fit-content;
}

.message.typing {
  opacity: 0.7;
}

.message-content {
  line-height: 1.6;
  text-align: left;
}

.message-time {
  font-size: 0.7rem;
  opacity: 0.7;
  margin-top: 0.25rem;
  text-align: right;
}

.ai .message-time {
  text-align: left;
}

/* 思考内容样式 */
.message-thinking-section {
  margin-bottom: 0.5rem;
  width: 100%;
  box-sizing: border-box;
}

.thinking-toggle {
  display: inline-block;
  margin: 0 0 0.5rem 0;
  border: none; /* 取消默认外边框 */
  outline: none; /* 取消点击/聚焦时的外边框 */
  background-color: rgba(66, 184, 131, 0.1);
  color: #2c6c53;
  border-radius: 15px;
  padding: 6px 12px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
}

.thinking-toggle:hover {
  background-color: #42b883;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.thinking-content {
  background-color: #f8f8f8;
  border-left: 3px solid #ccc;
  padding: 0.5rem;
  font-size: 0.85rem;
  color: #666;
  line-height: 1.4;
  border-radius: 4px;
}

.chat-input-area {
  padding: 1rem;
  background-color: white;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 0.75rem;
  align-items: stretch; /* 改为stretch使子元素高度一致 */
}

.chat-input-area textarea {
  flex: 1;
  padding: 0.75rem 1rem;
  border: 1px solid #ddd;
  border-radius: 20px;
  resize: none;
  min-height: 40px;
  max-height: 120px;
  font-family: inherit;
  font-size: 1rem;
  line-height: 1.4;
  box-sizing: border-box;
}

.chat-input-area textarea:focus {
  outline: none;
  border-color: #42b883;
}

.chat-input-area button {
  padding: 0.75rem 1.5rem;
  background-color: #42b883;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 500;
  font-size: 1rem;
  transition: all 0.2s;
  min-width: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  /* 确保按钮高度与输入框一致 */
  height: 40px;
  margin: auto 0; /* 垂直居中 */
}

.chat-input-area button:hover:not(:disabled) {
  background-color: #35495e;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.chat-input-area button:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: none;
}

.chat-input-area button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

/* 打字动画效果 */
.typing-indicator {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  background-color: #666;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

/* Markdown样式 */
.message-content :deep(h1),
.message-content :deep(h2),
.message-content :deep(h3),
.message-content :deep(h4),
.message-content :deep(h5),
.message-content :deep(h6) {
  margin-top: 0.5em;
  margin-bottom: 0.5em;
  font-weight: 600;
}

.message-content :deep(h1) { font-size: 1.5em; }
.message-content :deep(h2) { font-size: 1.3em; }
.message-content :deep(h3) { font-size: 1.1em; }

.message-content :deep(p) {
  margin-top: 0.5em;
  margin-bottom: 0.5em;
}

.message-content :deep(ul),
.message-content :deep(ol) {
  margin-top: 0.5em;
  margin-bottom: 0.5em;
  padding-left: 1.5em;
}

.message-content :deep(li) {
  margin-bottom: 0.2em;
}

.message-content :deep(pre) {
  background-color: #f5f5f5;
  padding: 0.5em;
  border-radius: 4px;
  overflow-x: auto;
  font-family: monospace;
  margin: 0.5em 0;
}

.message-content :deep(code) {
  background-color: #f5f5f5;
  padding: 0.1em 0.3em;
  border-radius: 3px;
  font-family: monospace;
  font-size: 0.9em;
}

.message-content :deep(pre code) {
  background-color: transparent;
  padding: 0;
}

.message-content :deep(blockquote) {
  border-left: 3px solid #42b883;
  padding-left: 0.8em;
  margin-left: 0;
  color: #666;
}

.message-content :deep(a) {
  color: #42b883;
  text-decoration: none;
}

.message-content :deep(a:hover) {
  text-decoration: underline;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* 响应式设计 */
@media (max-width: 600px) {
  .chat-room {
    height: 100vh;
    max-width: none;
  }
  
  .message {
    max-width: 85%;
  }
  
  .chat-input-area {
    padding: 0.75rem;
  }
}
</style>