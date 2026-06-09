<template>
  <div>
    <el-card style="margin-bottom: 20px">
      <div style="display: flex; align-items: center; gap: 16px; flex-wrap: wrap">
        <el-select v-model="studentId" placeholder="选择学生" clearable filterable style="width: 220px" @change="loadTrend">
          <el-option v-for="s in studentList" :key="s.id" :label="`${s.name}（${s.studentNo}）`" :value="s.id" />
        </el-select>
        <el-select v-model="courseId" placeholder="全部科目" clearable filterable style="width: 220px" @change="loadTrend">
          <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
        </el-select>
        <el-button type="primary" :loading="loading" @click="loadTrend">查询趋势</el-button>
      </div>
    </el-card>

    <el-card v-if="hasData">
      <template #header>成绩趋势折线图</template>
      <div ref="trendChartRef" style="height: 450px"></div>
    </el-card>
    <el-empty v-else-if="searched" description="暂无趋势数据，请选择学生后查询" />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getScoreTrend } from '@/api/score'
import { getStudentList } from '@/api/student'
import { getCourseList } from '@/api/course'
import { ElMessage } from 'element-plus'

const studentId = ref(null)
const courseId = ref(null)
const studentList = ref([])
const courseList = ref([])
const loading = ref(false)
const hasData = ref(false)
const searched = ref(false)
const trendChartRef = ref()
let trendChart = null

const handleResize = () => { trendChart?.resize() }

const loadOptions = async () => {
  try {
    const [sRes, cRes] = await Promise.all([getStudentList(), getCourseList()])
    studentList.value = sRes.data || []
    courseList.value = cRes.data || []
  } catch (e) { /* ignore */ }
}

const loadTrend = async () => {
  if (!studentId.value) {
    ElMessage.warning('请先选择学生')
    return
  }
  loading.value = true
  searched.value = true
  try {
    const res = await getScoreTrend({ studentId: studentId.value, courseId: courseId.value || undefined })
    const data = res.data || []
    if (data.length === 0) {
      hasData.value = false
      return
    }
    hasData.value = true
    await nextTick()
    if (trendChartRef.value && !trendChart) {
      trendChart = echarts.init(trendChartRef.value)
    }

    const courseMap = {}
    const semesterSet = new Set()
    data.forEach(item => {
      const name = item.courseName
      if (!courseMap[name]) courseMap[name] = {}
      courseMap[name][item.semester] = Number(item.score)
      semesterSet.add(item.semester)
    })
    const semesters = [...semesterSet].sort()
    const series = Object.keys(courseMap).map(name => ({
      name,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      data: semesters.map(s => courseMap[name][s] ?? null),
      emphasis: { focus: 'series' }
    }))

    trendChart.setOption({
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(50, 50, 50, 0.9)',
        textStyle: { color: '#fff' }
      },
      legend: {
        data: Object.keys(courseMap),
        bottom: 0,
        type: 'scroll'
      },
      grid: { left: 60, right: 30, top: 40, bottom: 60 },
      xAxis: {
        type: 'category',
        data: semesters,
        axisLabel: { fontSize: 12, color: '#606266' },
        axisLine: { lineStyle: { color: '#dcdfe6' } }
      },
      yAxis: {
        type: 'value',
        name: '分数',
        nameTextStyle: { fontSize: 12, color: '#909399', padding: [0, 0, 5, 0] },
        min: 0,
        max: 100,
        interval: 20,
        axisLabel: { fontSize: 12, color: '#606266' },
        axisLine: { show: true, lineStyle: { color: '#dcdfe6' } },
        splitLine: { lineStyle: { color: '#ebeef5', type: 'dashed' } }
      },
      series
    }, true)
  } catch (e) { /* ignore */ } finally { loading.value = false }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  loadOptions()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
})
</script>

<style scoped>
</style>
