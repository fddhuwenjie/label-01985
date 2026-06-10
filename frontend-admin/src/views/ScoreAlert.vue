<template>
  <div>
    <el-card style="margin-bottom: 20px">
      <div style="display: flex; align-items: center; gap: 16px; flex-wrap: wrap">
        <el-input v-model="searchForm.studentName" placeholder="搜索学生姓名" style="width: 200px" clearable />
        <el-select v-model="searchForm.status" placeholder="预警状态" style="width: 150px" clearable>
          <el-option label="未处理" :value="0" />
          <el-option label="已处理" :value="1" />
        </el-select>
        <el-select v-model="searchForm.alertLevel" placeholder="预警等级" style="width: 150px" clearable>
          <el-option label="一般" value="NORMAL" />
          <el-option label="警告" value="WARNING" />
          <el-option label="危险" value="DANGER" />
        </el-select>
        <el-button type="primary" :loading="loading" @click="loadData">查询</el-button>
        <el-button type="success" :loading="generating" @click="handleGenerate">生成预警</el-button>
        <el-tag type="danger" style="margin-left: auto">
          未处理: {{ unhandledCount }} 条
        </el-tag>
      </div>
    </el-card>

    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="studentName" label="学生姓名" min-width="100" />
        <el-table-column prop="studentNo" label="学号" min-width="120" />
        <el-table-column prop="courseName" label="科目" min-width="120" />
        <el-table-column prop="prevScore" label="上次成绩" min-width="100" align="right">
          <template #default="{ row }">{{ Number(row.prevScore).toFixed(1) }}</template>
        </el-table-column>
        <el-table-column prop="currScore" label="本次成绩" min-width="100" align="right">
          <template #default="{ row }">{{ Number(row.currScore).toFixed(1) }}</template>
        </el-table-column>
        <el-table-column prop="dropScore" label="下降幅度" min-width="100" align="right">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">
              -{{ Number(row.dropScore).toFixed(1) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="alertLevel" label="预警等级" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getAlertTagType(row.alertLevel)">
              {{ getAlertLevelText(row.alertLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="semester" label="学期" min-width="120" />
        <el-table-column prop="status" label="状态" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'">
              {{ row.status === 0 ? '未处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="预警时间" min-width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" size="small" type="primary" @click="openHandleDialog(row)">
              处理
            </el-button>
            <el-button v-else size="small" disabled>
              已处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="handleDialogVisible" title="处理预警" width="500px" :close-on-click-modal="false">
      <el-form :model="handleForm" label-width="80px">
        <el-form-item label="学生">
          <span>{{ handleForm.studentName }}</span>
        </el-form-item>
        <el-form-item label="科目">
          <span>{{ handleForm.courseName }}</span>
        </el-form-item>
        <el-form-item label="下降">
          <span style="color: #f56c6c; font-weight: bold">
            -{{ Number(handleForm.dropScore).toFixed(1) }} 分
          </span>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="handleForm.handleRemark"
            type="textarea"
            :rows="4"
            placeholder="请输入处理备注（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAlertList, generateAlerts, handleAlert, getUnhandledAlertCount } from '@/api/score'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const loading = ref(false)
const generating = ref(false)
const submitting = ref(false)
const unhandledCount = ref(0)
const handleDialogVisible = ref(false)

const searchForm = reactive({
  studentName: '',
  status: null,
  alertLevel: ''
})

const handleForm = reactive({
  id: null,
  studentName: '',
  courseName: '',
  dropScore: 0,
  handleRemark: ''
})

const getAlertLevelText = (level) => {
  const map = { NORMAL: '一般', WARNING: '警告', DANGER: '危险' }
  return map[level] || level
}

const getAlertTagType = (level) => {
  const map = { NORMAL: 'info', WARNING: 'warning', DANGER: 'danger' }
  return map[level] || 'info'
}

const loadUnhandledCount = async () => {
  try {
    const res = await getUnhandledAlertCount()
    unhandledCount.value = res.data || 0
  } catch (e) {
    // 已由拦截器统一处理
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchForm.studentName) params.studentName = searchForm.studentName
    if (searchForm.status !== null && searchForm.status !== '') params.status = searchForm.status
    if (searchForm.alertLevel) params.alertLevel = searchForm.alertLevel
    const res = await getAlertList(params)
    tableData.value = res.data || []
  } catch (e) {
    // 已由拦截器统一处理
  } finally {
    loading.value = false
  }
}

const handleGenerate = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要生成成绩预警吗？系统将自动检测连续两次考试成绩下降超过15分的学生。',
      '生成预警',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    generating.value = true
    const res = await generateAlerts(15.0)
    ElMessage.success(`预警生成完成，共生成 ${res.data} 条预警记录`)
    loadData()
    loadUnhandledCount()
  } catch (e) {
    if (e !== 'cancel') {
      // 已由拦截器统一处理
    }
  } finally {
    generating.value = false
  }
}

const openHandleDialog = (row) => {
  handleForm.id = row.id
  handleForm.studentName = row.studentName
  handleForm.courseName = row.courseName
  handleForm.dropScore = row.dropScore
  handleForm.handleRemark = ''
  handleDialogVisible.value = true
}

const submitHandle = async () => {
  submitting.value = true
  try {
    await handleAlert(handleForm.id, { handleRemark: handleForm.handleRemark })
    ElMessage.success('处理成功')
    handleDialogVisible.value = false
    loadData()
    loadUnhandledCount()
  } catch (e) {
    // 已由拦截器统一处理
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
  loadUnhandledCount()
})
</script>

<style scoped>
</style>
