<template>
  <div>
    <el-card style="margin-bottom: 20px">
      <div style="display: flex; align-items: center; gap: 16px; flex-wrap: wrap">
        <el-select v-model="classId1" placeholder="选择班级1" clearable filterable style="width: 200px">
          <el-option v-for="c in classList" :key="c.id" :label="c.className" :value="c.id" />
        </el-select>
        <span style="font-size: 16px; color: #909399">VS</span>
        <el-select v-model="classId2" placeholder="选择班级2" clearable filterable style="width: 200px">
          <el-option v-for="c in classList" :key="c.id" :label="c.className" :value="c.id" />
        </el-select>
        <el-select v-model="courseId" placeholder="选择科目" clearable filterable style="width: 200px">
          <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
        </el-select>
        <el-button type="primary" :loading="loading" @click="loadComparison">对比分析</el-button>
      </div>
    </el-card>

    <template v-if="hasData">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>{{ class1Name }}</template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="平均分">{{ fmt(class1.avgScore) }}</el-descriptions-item>
              <el-descriptions-item label="中位数">{{ fmt(class1.medianScore) }}</el-descriptions-item>
              <el-descriptions-item label="标准差">{{ fmt(class1.stdDev) }}</el-descriptions-item>
              <el-descriptions-item label="及格率">{{ fmt(class1.passRate) }}%</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>{{ class2Name }}</template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="平均分">{{ fmt(class2.avgScore) }}</el-descriptions-item>
              <el-descriptions-item label="中位数">{{ fmt(class2.medianScore) }}</el-descriptions-item>
              <el-descriptions-item label="标准差">{{ fmt(class2.stdDev) }}</el-descriptions-item>
              <el-descriptions-item label="及格率">{{ fmt(class2.passRate) }}%</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <el-card style="margin-top: 20px">
        <template #header>对比柱状图</template>
        <div ref="compareChartRef" style="height: 400px"></div>
      </el-card>
    </template>
    <el-empty v-else-if="searched" description="请选择两个班级和科目后进行对比" />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getClassComparison } from '@/api/score'
import { getClassList } from '@/api/classInfo'
import { getCourseList } from '@/api/course'
import { ElMessage } from 'element-plus'

const classId1 = ref(null)
const classId2 = ref(null)
const courseId = ref(null)
const classList = ref([])
const courseList = ref([])
const loading = ref(false)
const hasData = ref(false)
const searched = ref(false)
const compareChartRef = ref()
let compareChart = null

const class1 = ref({})
const class2 = ref({})
const class1Name = ref('')
const class2Name = ref('')

const handleResize = () => { compareChart?.resize() }

const fmt = (val) => {
  if (val === null || val === undefined) return '--'
  return Number(val).toFixed(2)
}

const loadOptions = async () => {
  try {
    const [clRes, coRes] = await Promise.all([getClassList(), getCourseList()])
    classList.value = clRes.data || []
    courseList.value = coRes.data || []
  } catch (e) { /* ignore */ }
}

const loadComparison = async () => {
  if (!classId1.value || !classId2.value || !courseId.value) {
    ElMessage.warning('请选择两个班级和一个科目')
    return
  }
  if (classId1.value === classId2.value) {
    ElMessage.warning('请选择不同的班级进行对比')
    return
  }
  loading.value = true
  searched.value = true
  try {
    const res = await getClassComparison({
      classId1: classId1.value,
      classId2: classId2.value,
      courseId: courseId.value
    })
    const data = res.data || {}
    class1.value = data.class1 || {}
    class2.value = data.class2 || {}

    if (!class1.value.className && !class2.value.className) {
      hasData.value = false
      return
    }

    class1Name.value = class1.value.className || '班级1'
    class2Name.value = class2.value.className || '班级2'
    hasData.value = true

    await nextTick()
    if (compareChartRef.value && !compareChart) {
      compareChart = echarts.init(compareChartRef.value)
    }

    const indicators = ['平均分', '中位数', '标准差', '及格率(%)']
    const class1Values = [
      Number(class1.value.avgScore) || 0,
      Number(class1.value.medianScore) || 0,
      Number(class1.value.stdDev) || 0,
      Number(class1.value.passRate) || 0
    ]
    const class2Values = [
      Number(class2.value.avgScore) || 0,
      Number(class2.value.medianScore) || 0,
      Number(class2.value.stdDev) || 0,
      Number(class2.value.passRate) || 0
    ]

    compareChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        backgroundColor: 'rgba(50, 50, 50, 0.9)',
        textStyle: { color: '#fff' }
      },
      legend: { data: [class1Name.value, class2Name.value], bottom: 0 },
      grid: { left: 60, right: 30, top: 40, bottom: 50 },
      xAxis: {
        type: 'category',
        data: indicators,
        axisLabel: { fontSize: 12, color: '#606266' },
        axisLine: { lineStyle: { color: '#dcdfe6' } }
      },
      yAxis: {
        type: 'value',
        axisLabel: { fontSize: 12, color: '#606266' },
        axisLine: { show: true, lineStyle: { color: '#dcdfe6' } },
        splitLine: { lineStyle: { color: '#ebeef5', type: 'dashed' } }
      },
      series: [
        {
          name: class1Name.value,
          type: 'bar',
          barWidth: '30%',
          data: class1Values.map(v => v.toFixed(2)),
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#409EFF' },
              { offset: 1, color: '#a0cfff' }
            ]),
            borderRadius: [4, 4, 0, 0]
          }
        },
        {
          name: class2Name.value,
          type: 'bar',
          barWidth: '30%',
          data: class2Values.map(v => v.toFixed(2)),
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#67C23A' },
              { offset: 1, color: '#b3e19d' }
            ]),
            borderRadius: [4, 4, 0, 0]
          }
        }
      ]
    }, true)
  } catch (e) { /* ignore */ } finally { loading.value = false }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  loadOptions()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  compareChart?.dispose()
})
</script>

<style scoped>
</style>
