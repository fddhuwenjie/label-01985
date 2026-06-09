<template>
  <el-container style="height: 100vh">
    <el-aside :width="isCollapse ? '64px' : '220px'" style="transition: width 0.3s">
      <div class="logo">
        <span v-if="!isCollapse">成绩管理系统</span>
        <span v-else>SGS</span>
      </div>
      <el-menu :default-active="route.path" router :collapse="isCollapse"
               background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF">
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>首页概览</span>
        </el-menu-item>
        <el-menu-item index="/student">
          <el-icon><User /></el-icon>
          <span>学生管理</span>
        </el-menu-item>
        <el-menu-item index="/teacher">
          <el-icon><Avatar /></el-icon>
          <span>教师管理</span>
        </el-menu-item>
        <el-menu-item index="/course">
          <el-icon><Reading /></el-icon>
          <span>课程管理</span>
        </el-menu-item>
        <el-menu-item index="/score">
          <el-icon><Document /></el-icon>
          <span>成绩管理</span>
        </el-menu-item>
        <el-menu-item index="/analysis">
          <el-icon><TrendCharts /></el-icon>
          <span>成绩分析</span>
        </el-menu-item>
        <el-menu-item index="/score-trend">
          <el-icon><DataLine /></el-icon>
          <span>成绩趋势</span>
        </el-menu-item>
        <el-menu-item index="/score-alert">
          <el-icon><Warning /></el-icon>
          <span>成绩预警</span>
        </el-menu-item>
        <el-menu-item index="/class-compare">
          <el-icon><Histogram /></el-icon>
          <span>班级对比</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #eee">
        <el-icon style="cursor: pointer; font-size: 20px" @click="isCollapse = !isCollapse">
          <Fold v-if="!isCollapse" /><Expand v-else />
        </el-icon>
        <div style="display: flex; align-items: center; gap: 16px">
          <el-badge :value="onlineCount" :max="99" type="success" class="online-badge">
            <el-tag :type="wsConnected ? 'success' : 'danger'" size="small">
              {{ wsConnected ? '实时连接' : '离线' }}
            </el-tag>
          </el-badge>
          <span>{{ userStore.realName || userStore.username }}</span>
          <el-button type="danger" size="small" plain @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'

const route = useRoute()
const router = useRouter()
const store = useStore()
const userStore = computed(() => store.state.user)
const isCollapse = ref(false)

const wsConnected = ref(false)
const onlineCount = ref(0)
let ws = null
let reconnectTimer = null

const getWsUrl = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.hostname
  const port = '8080'
  return `${protocol}//${host}:${port}/ws/notification`
}

const connectWebSocket = () => {
  if (ws && ws.readyState === WebSocket.OPEN) return

  ws = new WebSocket(getWsUrl())

  ws.onopen = () => {
    wsConnected.value = true
    console.log('WebSocket 连接成功')
  }

  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      onlineCount.value = data.onlineCount || 0

      if (data.type === 'CONNECTED') {
        ElNotification.success({ title: '实时通信', message: data.content, duration: 3000 })
      } else if (data.type === 'SCORE_UPDATE' || data.type === 'SCORE_CREATED') {
        ElNotification.info({ title: '成绩变更通知', message: data.content, duration: 5000 })
      } else if (data.type === 'STATISTICS_COMPLETE') {
        ElNotification.success({ title: '统计完成', message: data.content, duration: 5000 })
      } else if (data.type === 'SCORE_IMPORT') {
        ElNotification.success({ title: '批量导入', message: data.content, duration: 5000 })
      }
    } catch (e) {
      console.error('消息解析失败', e)
    }
  }

  ws.onclose = () => {
    wsConnected.value = false
    console.log('WebSocket 连接关闭，5秒后重连...')
    reconnectTimer = setTimeout(connectWebSocket, 5000)
  }

  ws.onerror = (error) => {
    console.error('WebSocket 错误', error)
    wsConnected.value = false
  }
}

onMounted(() => {
  connectWebSocket()
})

onUnmounted(() => {
  if (ws) {
    ws.close()
  }
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
  }
})

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
      confirmButtonText: '确定退出', cancelButtonText: '取消', type: 'warning'
    })
    if (ws) ws.close()
    await store.dispatch('user/logout')
    ElMessage.success('已安全退出')
    router.push('/login')
  } catch (e) { /* cancel */ }
}
</script>

<style scoped>
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #263445;
}
.el-aside {
  background-color: #304156;
  overflow: hidden;
}
.online-badge {
  margin-right: 8px;
}
</style>
