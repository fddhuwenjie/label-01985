<template>
  <div>
    <el-card>
      <div style="display: flex; justify-content: space-between; margin-bottom: 16px; flex-wrap: wrap; gap: 8px">
        <div style="display: flex; gap: 8px; flex-wrap: wrap">
          <el-select v-model="query.status" placeholder="处理状态" clearable style="width: 140px">
            <el-option label="未处理" :value="0" />
            <el-option label="已处理" :value="1" />
          </el-select>
          <el-select v-model="query.alertLevel" placeholder="预警等级" clearable style="width: 140px">
            <el-option label="一般预警" value="WARNING" />
            <el-option label="严重预警" value="SERIOUS" />
            <el-option label="紧急预警" value="URGENT" />
          </el-select>
          <el-button type="primary" @click="loadData"><el-icon><Search /></el-icon>查询</el-button>
        </div>
        <el-button type="warning" :loading="detectLoading" @click="handleDetect">
          <el-icon><Warning /></el-icon>检测预警
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="studentName" label="学生姓名" min-width="100" />
        <el-table-column prop="courseName" label="科目" min-width="120" />
        <el-table-column prop="previousScore" label="前次成绩" min-width="100" align="center" />
        <el-table-column prop="currentScore" label="本次成绩" min-width="100" align="center" />
        <el-table-column label="下降幅度" min-width="100" align="center">
          <template #default="{ row }">
            <span style="color: #F56C6C; font-weight: bold">-{{ row.dropAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="预警等级" min-width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.alertLevel)" effect="dark">{{ levelLabel(row.alertLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="semester" label="学期" min-width="130" />
        <el-table-column label="状态" min-width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="plain">
              {{ row.status === 1 ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" size="small" type="success" @click="handleProcess(row.id)">
              标记已处理
            </el-button>
            <span v-else style="color: #909399; font-size: 12px">--</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination style="margin-top: 16px; justify-content: flex-end"
        v-model:current-page="current" v-model:page-size="size"
        :total="total" :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next" @change="loadData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAlertPage, detectAlerts, processAlert } from '@/api/alert'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const loading = ref(false)
const detectLoading = ref(false)
const current = ref(1)
const size = ref(10)
const total = ref(0)

const query = reactive({ status: null, alertLevel: '' })

const levelTagType = (level) => {
  const map = { WARNING: 'warning', SERIOUS: 'danger', URGENT: 'danger' }
  return map[level] || 'info'
}
const levelLabel = (level) => {
  const map = { WARNING: '一般预警', SERIOUS: '严重预警', URGENT: '紧急预警' }
  return map[level] || level
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAlertPage({
      current: current.value, size: size.value,
      status: query.status ?? undefined,
      alertLevel: query.alertLevel || undefined
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) { /* ignore */ } finally { loading.value = false }
}

const handleDetect = async () => {
  detectLoading.value = true
  try {
    const res = await detectAlerts()
    ElMessage.success(res.data || '检测完成')
    loadData()
  } catch (e) { /* ignore */ } finally { detectLoading.value = false }
}

const handleProcess = async (id) => {
  try {
    await ElMessageBox.confirm('确定将该预警标记为已处理？', '确认', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    await processAlert(id)
    ElMessage.success('已标记为已处理')
    loadData()
  } catch (e) { /* cancel or error */ }
}

onMounted(() => { loadData() })
</script>

<style scoped>
</style>
