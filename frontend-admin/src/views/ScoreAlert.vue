<template>
  <div>
    <el-card>
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 12px">
        <div style="display: flex; gap: 12px; flex-wrap: wrap">
          <el-select v-model="status" placeholder="处理状态" clearable style="width: 140px" @change="loadData">
            <el-option label="未处理" :value="0" />
            <el-option label="已处理" :value="1" />
          </el-select>
          <el-select v-model="alertLevel" placeholder="预警等级" clearable style="width: 140px" @change="loadData">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
          </el-select>
          <el-button type="primary" @click="loadData">查询</el-button>
        </div>
        <el-button type="success" :loading="generating" @click="handleGenerate">
          扫描生成预警
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="studentName" label="学生姓名" min-width="100" />
        <el-table-column prop="courseName" label="科目" min-width="140" />
        <el-table-column label="上一次成绩" min-width="110" align="right">
          <template #default="{ row }">{{ Number(row.previousScore).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="本次成绩" min-width="100" align="right">
          <template #default="{ row }">{{ Number(row.currentScore).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="下降幅度" min-width="100" align="right">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">
              -{{ Number(row.dropValue).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="预警等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.alertLevel)" effect="dark">
              {{ levelLabel(row.alertLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="生成时间" min-width="170">
          <template #default="{ row }">{{ formatBeijingTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="处理时间" min-width="170">
          <template #default="{ row }">{{ row.handleTime ? formatBeijingTime(row.handleTime) : '--' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              :disabled="row.status === 1"
              @click="handleMark(row)"
            >
              标记已处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 16px; justify-content: flex-end"
        v-model:current-page="current"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAlertPage, generateAlerts, markAlertHandled } from '@/api/scoreAnalysis'
import { formatBeijingTime } from '@/utils/format'

const tableData = ref([])
const total = ref(0)
const current = ref(1)
const size = ref(10)
const status = ref(null)
const alertLevel = ref('')
const loading = ref(false)
const generating = ref(false)

const levelLabel = (lv) => ({ LOW: '低', MEDIUM: '中', HIGH: '高' }[lv] || lv)
const levelTagType = (lv) => ({ LOW: 'info', MEDIUM: 'warning', HIGH: 'danger' }[lv] || '')

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAlertPage({
      current: current.value,
      size: size.value,
      status: status.value,
      alertLevel: alertLevel.value || undefined
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { /* 拦截器统一处理 */ } finally {
    loading.value = false
  }
}

const handleGenerate = async () => {
  try {
    await ElMessageBox.confirm(
      '将扫描全部成绩数据，对连续两次考试下降超过 15 分的科目生成预警，是否继续？',
      '扫描预警',
      { type: 'warning' }
    )
  } catch { return }

  generating.value = true
  try {
    const res = await generateAlerts()
    ElMessage.success(`扫描完成，新增预警 ${res.data || 0} 条`)
    current.value = 1
    loadData()
  } catch (e) { /* 拦截器统一处理 */ } finally {
    generating.value = false
  }
}

const handleMark = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认将「${row.studentName} - ${row.courseName}」的预警标记为已处理？`,
      '操作确认',
      { type: 'warning' }
    )
  } catch { return }

  try {
    await markAlertHandled(row.id)
    ElMessage.success('已标记为已处理')
    loadData()
  } catch (e) { /* 拦截器统一处理 */ }
}

onMounted(loadData)
</script>
