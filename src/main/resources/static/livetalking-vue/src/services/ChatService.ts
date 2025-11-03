// 定义消息类型
export interface Message {
  id: string;
  content: string;
  sender: 'user' | 'ai';
  timestamp: Date;
  thinking?: string; // 添加思考过程字段
}

// 聊天服务类
export class ChatService {
  private apiBaseUrl = '/ark/bot/chat';
  
  // 发送消息并获取AI回复，增加历史对话参数
  async sendMessage(content: string, history: Message[] = []): Promise<Message> {
    try {
      // 准备请求数据，包含当前消息和历史对话
      const requestData = {
        prompt: content,
        history: history.map(msg => ({
          role: msg.sender, // 'user' 或 'ai'
          content: msg.content
        }))
      };
      
      // 发送POST请求到后端API
      const response = await fetch(this.apiBaseUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestData)
      });
      
      // 检查响应状态
      if (!response.ok) {
        throw new Error(`API请求失败: ${response.status}`);
      }
      
      // 解析响应数据
      const result = await response.json();
      
      // 假设后端返回的消息内容在data.content字段中
      // 根据实际后端返回格式调整
      const aiResponse = result?.data || JSON.stringify(result);
      
      return {
        id: Date.now().toString(),
        content: aiResponse.content,
        sender: 'ai',
        timestamp: new Date(),
        thinking: aiResponse.reasoningContent,  // 思考内容赋值
      };
    } catch (error) {
      console.error('发送消息失败:', error);
      // 返回错误消息给前端显示
      return {
        id: Date.now().toString(),
        content: `抱歉，无法连接到AI服务: ${error instanceof Error ? error.message : '未知错误'}`,
        sender: 'ai',
        timestamp: new Date(),
      };
    }
  }
}

// 导出单例
export const chatService = new ChatService();