<script setup lang="ts">
import ChatRoom from './components/ChatRoom.vue'
import CubeWall from './components/CubeWall.vue'
</script>


<template>
  <div id="app">
    <CubeWall />
    <!-- <ChatRoom /> -->
  </div>
</template>

<style scoped>
#app {
  width: 100%;
  height: 100vh;
  margin: 0;
  padding: 0;
}
</style>