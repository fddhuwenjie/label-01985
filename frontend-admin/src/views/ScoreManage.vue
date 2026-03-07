<template>
  <div>
    <el-card>
      <div style="display: flex; justify-content: space-between; margin-bottom: 16px; flex-wrap: wrap; gap: 8px">
        <div style="display: flex; gap: 8px; flex-wrap: wrap">
          <el-select v-model="query.studentId" placeholder="选择学生" clearable filterable style="width: 180px">
            <el-option v-for="s in studentList" :key="s.id" :label="`${s.name}（${s.studentNo}）`" :value="s.id" />
          </el-select>
          <el-select v-model="query.courseId" placeholder="选择课程" clearable filterable style="width: 180px">
            <el-option v-for="c in courseList" :key="c.id" :label="`${c.courseName}（${c.courseNo}）`" :value="c.id" />
          </el-select>
          <el-input v-model="query.semester" placeholder="学期" style="width: 160px" clearable />
          <el-button type="primary" @click="loadData"><el-icon><Search /></el-icon>查询</el-button>
        </div>
        <div style="display: flex; gap: 8px">
          <el-button type="success" :loading="exportLoading" @click="handleExport">导出Excel</el-button>
          <el-upload action="" :before-upload="handleImport" :show-file-list="false" accept=".xlsx,.xls">
            <el-button type="warning">导入Excel</el-button>
          </el-upload>
          <el-button type="primary" @click="openDialog()">新增成绩</el-button>
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column label="学生" min-width="150">
          <template #default="{ row }">{{ getStudentName(row.studentId) }}</template>
        </el-table-column>
        <el-table-column label="课程" min-width="150">
          <template #default="{ row }">{{ getCourseName(row.courseId) }}</template>
        </el-table-column>
        <el-table-column prop="score" label="成绩" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.score >= 60 ? 'success' : 'danger'" effect="plain">{{ row.score }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="semester" label="学期" min-width="140" />
        <el-table-column label="创建时间" min-width="180">
          <template #default="{ row }">{{ formatBeijingTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination style="margin-top: 16px; justify-content: flex-end"
        v-model:current-page="current" v-model:page-size="size"
        :total="total" :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next" @change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑成绩' : '新增成绩'" width="500px"
               :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="学生" prop="studentId">
          <el-select v-model="form.studentId" placeholder="请选择学生" clearable filterable style="width: 100%">
            <el-option v-for="s in studentList" :key="s.id" :label="`${s.name}（${s.studentNo}）`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" clearable filterable style="width: 100%">
            <el-option v-for="c in courseList" :key="c.id" :label="`${c.courseName}（${c.courseNo}）`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="成绩" prop="score">
          <el-input-number v-model="form.score" :min="0" :max="100" :precision="2" placeholder="请输入成绩" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-input v-model="form.semester" placeholder="如: 2024-2025-1" maxlength="30" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { getScorePage, addScore, updateScore, deleteScore, exportScore } from '@/api/score'
import { getStudentList } from '@/api/student'
import { getCourseList } from '@/api/course'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatBeijingTime } from '@/utils/format'
import request from '@/api/request'

const tableData = ref([])
const studentList = ref([])
const courseList = ref([])
const loading = ref(false)
const submitLoading = ref(false)
const exportLoading = ref(false)
const current = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()

const query = reactive({ studentId: '', courseId: '', semester: '' })
const form = reactive({ id: null, studentId: null, courseId: null, score: 0, semester: '' })
const rules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  score: [{ required: true, message: '请输入成绩', trigger: 'change' }],
  semester: [
    { required: true, message: '请输入学期', trigger: 'blur' },
    { pattern: /^\d{4}-\d{4}-[12]$/, message: '学期格式如: 2024-2025-1', trigger: 'blur' }
  ]
}

const getStudentName = (id) => {
  const s = studentList.value.find(item => item.id === id)
  return s ? `${s.name}（${s.studentNo}）` : (id || '--')
}
const getCourseName = (id) => {
  const c = courseList.value.find(item => item.id === id)
  return c ? `${c.courseName}（${c.courseNo}）` : (id || '--')
}

const loadOptions = async () => {
  try {
    const [sRes, cRes] = await Promise.all([getStudentList(), getCourseList()])
    studentList.value = sRes.data || []
    courseList.value = cRes.data || []
  } catch (e) { /* ignore */ }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getScorePage({
      current: current.value, size: size.value,
      studentId: query.studentId || undefined,
      courseId: query.courseId || undefined,
      semester: query.semester || undefined
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) { /* ignore */ } finally { loading.value = false }
}

const openDialog = (row) => {
  if (row) {
    Object.assign(form, { id: row.id, studentId: row.studentId, courseId: row.courseId, score: row.score, semester: row.semester })
  } else {
    Object.assign(form, { id: null, studentId: null, courseId: null, score: 0, semester: '' })
  }
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

const handleSubmit = async () => {
  try { await formRef.value.validate() } catch (e) {
    ElMessage.warning('请完善表单信息后再提交')
    return
  }
  submitLoading.value = true
  try {
    if (form.id) { await updateScore(form); ElMessage.success('成绩修改成功') }
    else { await addScore(form); ElMessage.success('成绩录入成功') }
    dialogVisible.value = false
    loadData()
  } catch (e) { /* ignore */ } finally { submitLoading.value = false }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该成绩记录吗？删除后不可恢复。', '删除确认', {
      confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning'
    })
    await deleteScore(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { /* cancel or error */ }
}

const handleExport = async () => {
  exportLoading.value = true
  try {
    const res = await exportScore(query.semester)
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', '成绩表.xlsx')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) { /* ignore */ } finally { exportLoading.value = false }
}

const handleImport = async (file) => {
  const formData = new FormData()
  formData.append('file', file)
  try {
    await request.post('/score/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    ElMessage.success('成绩导入成功')
    loadData()
  } catch (e) { /* ignore */ }
  return false
}

onMounted(() => { loadData(); loadOptions() })
</script>

<style scoped>
:deep(.el-input-number .el-input__inner) { text-align: left; }
</style>
