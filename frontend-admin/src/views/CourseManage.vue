<template>
  <div>
    <el-card>
      <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
        <el-input v-model="keyword" placeholder="搜索课程名称/编号" style="width: 300px" clearable @clear="loadData">
          <template #append>
            <el-button @click="loadData"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
        <el-button type="primary" @click="openDialog()">新增课程</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="courseNo" label="课程编号" min-width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column label="授课教师" min-width="120">
          <template #default="{ row }">{{ getTeacherName(row.teacherId) }}</template>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑课程' : '新增课程'" width="500px"
               :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="课程编号" prop="courseNo">
          <el-input v-model="form.courseNo" placeholder="请输入课程编号" maxlength="30" />
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="form.courseName" placeholder="请输入课程名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="form.credit" :min="0" :max="10" controls-position="right" />
        </el-form-item>
        <el-form-item label="授课教师" prop="teacherId">
          <el-select v-model="form.teacherId" placeholder="请选择授课教师" clearable filterable style="width: 100%">
            <el-option v-for="t in teacherList" :key="t.id" :label="`${t.name}（${t.teacherNo}）`" :value="t.id" />
          </el-select>
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
import { getCoursePage, addCourse, updateCourse, deleteCourse } from '@/api/course'
import { getTeacherList } from '@/api/teacher'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatBeijingTime } from '@/utils/format'

const tableData = ref([])
const teacherList = ref([])
const loading = ref(false)
const submitLoading = ref(false)
const current = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const dialogVisible = ref(false)
const formRef = ref()

const form = reactive({ id: null, courseNo: '', courseName: '', credit: 0, teacherId: null, semester: '' })
const rules = {
  courseNo: [
    { required: true, message: '请输入课程编号', trigger: 'blur' },
    { min: 2, max: 30, message: '课程编号长度为2-30个字符', trigger: 'blur' }
  ],
  courseName: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, max: 100, message: '课程名称长度为2-100个字符', trigger: 'blur' }
  ],
  credit: [{ required: true, message: '请输入学分', trigger: 'change' }],
  semester: [
    { required: true, message: '请输入学期', trigger: 'blur' },
    { pattern: /^\d{4}-\d{4}-[12]$/, message: '学期格式如: 2024-2025-1', trigger: 'blur' }
  ]
}

const getTeacherName = (id) => {
  const t = teacherList.value.find(item => item.id === id)
  return t ? t.name : (id || '--')
}

const loadTeachers = async () => {
  try {
    const res = await getTeacherList()
    teacherList.value = res.data || []
  } catch (e) { /* ignore */ }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCoursePage({ current: current.value, size: size.value, keyword: keyword.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) { /* ignore */ } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  if (row) {
    Object.assign(form, { id: row.id, courseNo: row.courseNo, courseName: row.courseName, credit: row.credit, teacherId: row.teacherId, semester: row.semester })
  } else {
    Object.assign(form, { id: null, courseNo: '', courseName: '', credit: 0, teacherId: null, semester: '' })
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
    if (form.id) {
      await updateCourse(form)
      ElMessage.success('课程信息修改成功')
    } else {
      await addCourse(form)
      ElMessage.success('课程新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) { /* ignore */ } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该课程吗？删除后不可恢复。', '删除确认', {
      confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning'
    })
    await deleteCourse(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { /* cancel or error */ }
}

onMounted(() => { loadData(); loadTeachers() })
</script>
