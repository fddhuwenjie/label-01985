<template>
  <div>
    <el-card style="margin-bottom: 20px">
      <div style="display: flex; align-items: center; gap: 16px; flex-wrap: wrap">
        <el-select v-model="selectedStudent" placeholder="请选择学生" style="width: 200px" filterable @change="loadTrend">
          <el-option v-for="s in studentList" :key="s.id" :label="`${s.name} (${s.studentNo})`" :value="s.id" />
        </el-select>
        <el-select v-model="selectedCourse" placeholder="全部科目" style="width: 180px" clearable @change="loadTrend">
          <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
        </el-select>
        <el-button type="primary" :loading="loading" @click="loadTrend">查询趋势</el-button>
      </div>
    </el-card>

    <el-card v-if="hasData">
      <template #header>
        <span>成绩趋势折线图</span>
      </template>
      <div ref="chartRef" style="height: 500px"></div>
    </el-card>

    <el-empty v-else-if="searched" description="暂无成绩数据" />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getStudentScoreTrend, getStudentList, getCourseList } from '@/api/score'
import { ElMessage } from 'element-plus'

const selectedStudent = ref(null)
const selectedCourse = ref(null)
const studentList = ref([])
const courseList = ref([])
const trendData = ref([])
const loading = ref(false)
const hasData = ref(false)
const searched = ref(false)
const chartRef = ref()
let chart = null

const handleResize = () => {
  chart?.resize()
}

const initChart = async () => {
  await nextTick()
  if (chartRef.value && !chart) {
    chart = echarts.init(chartRef.value)
  }
}

const loadStudents = async () => {
  try {
    const res = await getStudentList()
    studentList.value = res.data?.records || []
    if (studentList.value.length > 0) {
      selectedStudent.value = studentList.value[0].id
    }
  } catch (e) {
    console.error('加载学生列表失败', e)
  }
}

const loadCourses = async () => {
  try {
    const res = await getCourseList()
    courseList.value = res.data?.records || []
  } catch (e) {
    console.error('加载课程列表失败', e)
  }
}

const loadTrend = async () => {
  if (!selectedStudent.value) {
    ElMessage.warning('请先选择学生')
    return
  }

  loading.value = true
  searched.value = true
  try {
    const params = { studentId: selectedStudent.value }
    if (selectedCourse.value) {
      params.courseId = selectedCourse.value
    }
    const res = await getStudentScoreTrend(params)
    trendData.value = res.data || []

    if (trendData.value.length === 0) {
      hasData.value = false
      return
    }

    hasData.value = true
    await nextTick()
    await initChart()
    renderChart()
  } catch (e) {
    console.error('加载趋势数据失败', e)
  } finally {
    loading.value = false
  }
}

const renderChart = () => {
  const data = trendData.value

  const courseMap = {}
  data.forEach(item => {
    if (!courseMap[item.courseName]) {
      courseMap[item.courseName] = []
    }
    courseMap[item.courseName].push({
      examTime: item.examTime || item.semester,
      score: item.score,
      semester: item.semester
    })
  })

  const allTimes = [...new Set(data.map(item => item.examTime || item.semester))].sort()

  const series = Object.keys(courseMap).map(courseName => {
    const scores = allTimes.map(time => {
      const found = courseMap[courseName].find(item => (item.examTime || item.semester) === time)
      return found ? found.score : null
    })
    return {
      name: courseName,
      type: 'line',
      data: scores,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { width: 2 },
      emphasis: { focus: 'series' }
    }
  })

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      textStyle: { color: '#fff' },
      formatter: (params) => {
        let html = `${params[0].axisValue}<br/>`
        params.forEach(p => {
          if (p.value !== null && p.value !== undefined) {
            html += `${p.marker} ${p.seriesName}: <b>${p.value}</b> 分<br/>`
          }
        })
        return html
      }
    },
    legend: {
      data: Object.keys(courseMap),
      top: 0,
      type: 'scroll'
    },
    grid: {
      left: 50,
      right: 30,
      top: 60,
      bottom: 60
    },
    xAxis: {
      type: 'category',
      data: allTimes,
      axisLabel: { rotate: 30, fontSize: 12, color: '#606266' },
      axisLine: { lineStyle: { color: '#dcdfe6' } }
    },
    yAxis: {
      type: 'value',
      name: '分数',
      nameTextStyle: { fontSize: 12, color: '#909399' },
      min: 0,
      max: 100,
      interval: 10,
      axisLabel: { fontSize: 12, color: '#606266' },
      splitLine: { lineStyle: { color: '#ebeef5', type: 'dashed' } }
    },
    series
  }, true)
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)
  await Promise.all([loadStudents(), loadCourses()])
  if (selectedStudent.value) {
    loadTrend()
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<style scoped>
</style>
