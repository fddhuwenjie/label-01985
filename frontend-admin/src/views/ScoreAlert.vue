<template>
  <div>
    <el-card style="margin-bottom: 20px">
      <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 16px">
        <div style="display: flex; align-items: center; gap: 16px">
          <el-select v-model="statusFilter" placeholder="全部状态" style="width: 150px" clearable @change="loadAlerts">
            <el-option label="未处理" :value="0" />
            <el-option label="已处理" :value="1" />
          </el-select>
          <el-button type="primary" :loading="loading" @click="loadAlerts">刷新</el-button>
        </div>
        <el-button type="warning" @click="handleGenerateAlerts">
          <el-icon><Warning /></el-icon>
          <span>手动检测预警</span>
        </el-button>
      </div>
    </el-card>

    <el-card>
      <el-table :data="alertList" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="studentName" label="学生姓名" min-width="120" />
        <el-table-column prop="courseName" label="科目" min-width="150" />
        <el-table-column label="上次成绩" width="110" align="center">
          <template #default="{ row }">{{ row.prevScore?.toFixed(1) }}</template>
        </el-table-column>
        <el-table-column label="本次成绩" width="110" align="center">
          <template #default="{ row }">{{ row.currentScore?.toFixed(1) }}</template>
        </el-table-column>
        <el-table-column label="下降幅度" width="110" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">↓ {{ row.dropAmount?.toFixed(1) }}分</span>
          </template>
        </el-table-column>
        <el-table-column label="预警等级" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.alertLevel === 'WARNING'" type="warning" size="small">预警</el-tag>
            <el-tag v-else-if="row.alertLevel === 'MEDIUM'" type="danger" size="small">中级</el-tag>
            <el-tag v-else-if="row.alertLevel === 'SEVERE'" type="danger" size="small" effect="dark">严重</el-tag>
            <el-tag v-else size="small">{{ row.alertLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning" size="small">未处理</el-tag>
            <el-tag v-else type="success" size="small">已处理</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="预警时间" width="170" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="primary" link size="small" @click="handleMarkProcessed(row)">
              标记已处理
            </el-button>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; text-align: right">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadAlerts"
          @current-change="loadAlerts"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Warning } from '@element-plus/icons-vue'
import { getScoreAlertPage, markAlertProcessed, generateScoreAlerts } from '@/api/score'
import { ElMessage, ElMessageBox } from 'element-plus'

const alertList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref(null)

const loadAlerts = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    if (statusFilter.value !== null && statusFilter.value !== undefined) {
      params.status = statusFilter.value
    }
    const res = await getScoreAlertPage(params)
    alertList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载预警列表失败', e)
  } finally {
    loading.value = false
  }
}

const handleMarkProcessed = async (row) => {
  try {
    await ElMessageBox.confirm(`确定将 ${row.studentName} 的 ${row.courseName} 预警标记为已处理吗？`, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await markAlertProcessed(row.id)
    ElMessage.success('标记成功')
    loadAlerts()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('标记失败', e)
    }
  }
}

const handleGenerateAlerts = async () => {
  try {
    await ElMessageBox.confirm('确定要手动触发预警检测吗？系统将检测所有成绩下降超过15分的记录。', '确认检测', {
      confirmButtonText: '开始检测',
      cancelButtonText: '取消',
      type: 'info'
    })
    const res = await generateScoreAlerts(15)
    const count = res.data || 0
    if (count > 0) {
      ElMessage.success(`检测完成，新增 ${count} 条预警记录`)
    } else {
      ElMessage.info('检测完成，暂无新增预警记录')
    }
    loadAlerts()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('预警检测失败', e)
    }
  }
}

onMounted(() => {
  loadAlerts()
})
</script>

<style scoped>
</style>
