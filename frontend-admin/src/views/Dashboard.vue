<template>
  <div>
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6" v-for="item in stats" :key="item.title">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center; gap: 16px">
            <el-icon :size="40" :color="item.color"><component :is="item.icon" /></el-icon>
            <div>
              <div style="font-size: 28px; font-weight: bold">{{ item.value }}</div>
              <div style="color: #999; font-size: 14px">{{ item.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header>
        <span>欢迎使用学生成绩管理系统</span>
      </template>
      <p>当前用户：{{ userStore.realName || userStore.username }}</p>
      <p>角色：{{ userStore.roles.join(', ') }}</p>
      <p style="margin-top: 16px; color: #666">
        本系统支持学生、教师、课程、成绩的全面管理，以及成绩数据可视化分析。
      </p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import request from '@/api/request'

const store = useStore()
const userStore = computed(() => store.state.user)

const stats = ref([
  { title: '学生总数', value: 0, icon: 'User', color: '#409EFF' },
  { title: '教师总数', value: 0, icon: 'Avatar', color: '#67C23A' },
  { title: '课程总数', value: 0, icon: 'Reading', color: '#E6A23C' },
  { title: '成绩记录', value: 0, icon: 'Document', color: '#F56C6C' }
])

const loadStats = async () => {
  try {
    const [s, t, c, sc] = await Promise.all([
      request.get('/student/count'),
      request.get('/teacher/count'),
      request.get('/course/count'),
      request.get('/score/count')
    ])
    stats.value[0].value = s.data ?? 0
    stats.value[1].value = t.data ?? 0
    stats.value[2].value = c.data ?? 0
    stats.value[3].value = sc.data ?? 0
  } catch (e) {
    // 静默处理
  }
}

onMounted(loadStats)
</script>
