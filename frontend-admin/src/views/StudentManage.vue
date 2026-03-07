<template>
  <div>
    <el-card>
      <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
        <el-input v-model="keyword" placeholder="搜索学生姓名/学号" style="width: 300px" clearable @clear="loadData">
          <template #append>
            <el-button @click="loadData"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
        <el-button type="primary" @click="openDialog()">新增学生</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="studentNo" label="学号" min-width="120" />
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="classId" label="班级ID" min-width="100" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑学生' : '新增学生'" width="500px"
               :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="请输入学号" maxlength="30" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" maxlength="50" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="班级ID">
          <el-input-number v-model="form.classId" :min="1" placeholder="请选择班级" controls-position="right" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
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
import { getStudentPage, addStudent, updateStudent, deleteStudent } from '@/api/student'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatBeijingTime, phoneValidator } from '@/utils/format'

const tableData = ref([])
const loading = ref(false)
const submitLoading = ref(false)
const current = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const dialogVisible = ref(false)
const formRef = ref()

const form = reactive({ id: null, studentNo: '', name: '', gender: 1, classId: null, phone: '' })
const rules = {
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 2, max: 30, message: '学号长度为2-30个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度为2-50个字符', trigger: 'blur' }
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ validator: phoneValidator, trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getStudentPage({ current: current.value, size: size.value, keyword: keyword.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    // 已由拦截器统一处理
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  if (row) {
    Object.assign(form, { id: row.id, studentNo: row.studentNo, name: row.name, gender: row.gender, classId: row.classId, phone: row.phone })
  } else {
    Object.assign(form, { id: null, studentNo: '', name: '', gender: 1, classId: null, phone: '' })
  }
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    ElMessage.warning('请完善表单信息后再提交')
    return
  }
  submitLoading.value = true
  try {
    if (form.id) {
      await updateStudent(form)
      ElMessage.success('学生信息修改成功')
    } else {
      await addStudent(form)
      ElMessage.success('学生新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    // 已由拦截器统一处理
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该学生吗？删除后不可恢复。', '删除确认', {
      confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning'
    })
    await deleteStudent(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { /* cancel or error */ }
}

onMounted(loadData)
</script>
