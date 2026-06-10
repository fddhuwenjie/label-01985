<template>
  <div>
    <el-card style="margin-bottom: 20px">
      <div style="display: flex; align-items: center; gap: 16px; flex-wrap: wrap">
        <el-radio-group v-model="activeTab" @change="handleTabChange">
          <el-radio-button label="stats">成绩统计</el-radio-button>
          <el-radio-button label="trend">成绩趋势</el-radio-button>
          <el-radio-button label="compare">班级对比</el-radio-button>
        </el-radio-group>
      </div>
    </el-card>

    <div v-if="activeTab === 'stats'">
      <el-card style="margin-bottom: 20px">
        <div style="display: flex; align-items: center; gap: 16px">
          <el-input v-model="semester" placeholder="如: 2024-2025-1" style="width: 250px" clearable />
          <el-button type="primary" :loading="statsLoading" @click="loadStats">查询统计</el-button>
        </div>
      </el-card>

      <div v-if="hasData">
        <el-row :gutter="20" class="analysis-row">
          <el-col :span="12">
            <el-card class="analysis-card">
              <template #header>各科平均分</template>
              <div ref="avgChartRef" class="chart-container"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="analysis-card">
              <template #header>学生排名 TOP10</template>
              <el-table :data="rankingData" border stripe class="ranking-table">
                <el-table-column type="index" label="排名" width="60" align="center" />
                <el-table-column prop="studentNo" label="学号" min-width="120" />
                <el-table-column prop="studentName" label="姓名" min-width="100" />
                <el-table-column prop="totalScore" label="总分" min-width="100" align="right">
                  <template #default="{ row }">{{ Number(row.totalScore).toFixed(1) }}</template>
                </el-table-column>
                <el-table-column prop="avgScore" label="平均分" min-width="100" align="right">
                  <template #default="{ row }">{{ Number(row.avgScore).toFixed(2) }}</template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px">
          <el-col :span="24">
            <el-card>
              <template #header>各科成绩对比（雷达图）</template>
              <div ref="compareChartRef" style="height: 400px"></div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <el-empty v-else-if="searched" description="该学期暂无成绩数据" />
    </div>

    <div v-if="activeTab === 'trend'">
      <el-card style="margin-bottom: 20px">
        <div style="display: flex; align-items: center; gap: 16px; flex-wrap: wrap">
          <el-select v-model="trendForm.studentId" placeholder="请选择学生" style="width: 250px" filterable>
            <el-option
              v-for="stu in studentList"
              :key="stu.id"
              :label="`${stu.name} (${stu.studentNo})`"
              :value="stu.id"
            />
          </el-select>
          <el-select v-model="trendForm.courseId" placeholder="全部科目" style="width: 200px" clearable>
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
            />
          </el-select>
          <el-button type="primary" :loading="trendLoading" @click="loadTrend">查看趋势</el-button>
        </div>
      </el-card>

      <el-card>
        <template #header>成绩趋势折线图</template>
        <div ref="trendChartRef" style="height: 450px"></div>
      </el-card>
    </div>

    <div v-if="activeTab === 'compare'">
      <el-card style="margin-bottom: 20px">
        <div style="display: flex; align-items: center; gap: 16px; flex-wrap: wrap">
          <el-select v-model="compareForm.class1Id" placeholder="选择班级1" style="width: 220px">
            <el-option v-for="cls in classList" :key="cls.id" :label="cls.className" :value="cls.id" />
          </el-select>
          <span style="color: #909399">VS</span>
          <el-select v-model="compareForm.class2Id" placeholder="选择班级2" style="width: 220px">
            <el-option v-for="cls in classList" :key="cls.id" :label="cls.className" :value="cls.id" />
          </el-select>
          <el-select v-model="compareForm.courseId" placeholder="选择科目" style="width: 200px">
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
            />
          </el-select>
          <el-button type="primary" :loading="compareLoading" @click="loadCompare">开始对比</el-button>
        </div>
      </el-card>

      <div v-if="compareData.length > 0">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>
                <span style="font-weight: bold; color: #409EFF">{{ compareData[0]?.className }}</span>
              </template>
              <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px">
                <div class="stat-item">
                  <div class="stat-label">平均分</div>
                  <div class="stat-value">{{ formatNum(compareData[0]?.avgScore) }}</div>
                </div>
                <div class="stat-item">
                  <div class="stat-label">中位数</div>
                  <div class="stat-value">{{ formatNum(compareData[0]?.medianScore) }}</div>
                </div>
                <div class="stat-item">
                  <div class="stat-label">标准差</div>
                  <div class="stat-value">{{ formatNum(compareData[0]?.stdDevScore) }}</div>
                </div>
                <div class="stat-item">
                  <div class="stat-label">及格率</div>
                  <div class="stat-value">{{ formatNum(compareData[0]?.passRate) }}%</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>
                <span style="font-weight: bold; color: #67C23A">{{ compareData[1]?.className }}</span>
              </template>
              <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px">
                <div class="stat-item">
                  <div class="stat-label">平均分</div>
                  <div class="stat-value">{{ formatNum(compareData[1]?.avgScore) }}</div>
                </div>
                <div class="stat-item">
                  <div class="stat-label">中位数</div>
                  <div class="stat-value">{{ formatNum(compareData[1]?.medianScore) }}</div>
                </div>
                <div class="stat-item">
                  <div class="stat-label">标准差</div>
                  <div class="stat-value">{{ formatNum(compareData[1]?.stdDevScore) }}</div>
                </div>
                <div class="stat-item">
                  <div class="stat-label">及格率</div>
                  <div class="stat-value">{{ formatNum(compareData[1]?.passRate) }}%</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-card style="margin-top: 20px">
          <template #header>班级成绩对比图</template>
          <div ref="compareBarChartRef" style="height: 400px"></div>
        </el-card>
      </div>

      <el-empty v-else-if="compareSearched" description="暂无对比数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getScoreStats, getStudentRanking, getStudentTrend, getClassComparison } from '@/api/score'
import { getStudentList } from '@/api/student'
import { getCourseList } from '@/api/course'
import { getClassList } from '@/api/class'
import { ElMessage } from 'element-plus'

const activeTab = ref('stats')
const semester = ref('2024-2025-1')
const rankingData = ref([])
const statsLoading = ref(false)
const hasData = ref(false)
const searched = ref(false)
const avgChartRef = ref()
const compareChartRef = ref()
const trendChartRef = ref()
const compareBarChartRef = ref()
let avgChart = null
let compareChart = null
let trendChart = null
let compareBarChart = null

const trendLoading = ref(false)
const trendForm = reactive({ studentId: null, courseId: null })
const studentList = ref([])
const courseList = ref([])
const classList = ref([])

const compareLoading = ref(false)
const compareSearched = ref(false)
const compareData = ref([])
const compareForm = reactive({ class1Id: null, class2Id: null, courseId: null })

const handleResize = () => {
  avgChart?.resize()
  compareChart?.resize()
  trendChart?.resize()
  compareBarChart?.resize()
}

const formatNum = (val) => {
  if (val === null || val === undefined) return '-'
  return Number(val).toFixed(2)
}

const loadStudentList = async () => {
  try {
    const res = await getStudentList()
    studentList.value = res.data || []
  } catch (e) {}
}

const loadCourseList = async () => {
  try {
    const res = await getCourseList()
    courseList.value = res.data || []
  } catch (e) {}
}

const loadClassList = async () => {
  try {
    const res = await getClassList()
    classList.value = res.data || []
  } catch (e) {}
}

const handleTabChange = async () => {
  await nextTick()
  if (activeTab.value === 'trend' && trendForm.studentId) {
    initTrendChart()
  }
  if (activeTab.value === 'compare' && compareData.value.length > 0) {
    initCompareBarChart()
  }
}

const loadStats = async () => {
  if (!semester.value || !semester.value.trim()) {
    ElMessage.warning('请先输入要查询的学期')
    return
  }

  statsLoading.value = true
  searched.value = true
  try {
    const [statsRes, rankRes] = await Promise.all([
      getScoreStats(semester.value),
      getStudentRanking(semester.value)
    ])

    rankingData.value = (rankRes.data || []).slice(0, 10)
    const statsData = statsRes.data || []

    if (statsData.length === 0) {
      hasData.value = false
      return
    }

    hasData.value = true
    await nextTick()
    initAvgChart(statsData)
    initCompareChart(statsData)
  } catch (e) {
  } finally {
    statsLoading.value = false
  }
}

const initAvgChart = (statsData) => {
  if (!avgChartRef.value) return
  if (!avgChart) {
    avgChart = echarts.init(avgChartRef.value)
  }
  avgChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>平均分: {c} 分',
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      textStyle: { color: '#fff' }
    },
    grid: { left: 50, right: 20, top: 35, bottom: 70 },
    xAxis: {
      type: 'category',
      data: statsData.map(i => i.courseName),
      axisLabel: { rotate: 35, interval: 0, fontSize: 12, color: '#606266' },
      axisLine: { lineStyle: { color: '#dcdfe6' } },
      axisTick: { alignWithLabel: true }
    },
    yAxis: {
      type: 'value',
      name: '分数',
      nameTextStyle: { fontSize: 12, color: '#909399', padding: [0, 0, 5, 0] },
      min: 0, max: 100, interval: 20,
      axisLabel: { fontSize: 12, color: '#606266' },
      axisLine: { show: true, lineStyle: { color: '#dcdfe6' } },
      splitLine: { lineStyle: { color: '#ebeef5', type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      data: statsData.map(i => Number(i.avgScore).toFixed(2)),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409EFF' },
          { offset: 1, color: '#a0cfff' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      barWidth: '50%',
      label: { show: true, position: 'top', formatter: '{c}', fontSize: 12, fontWeight: 'bold', color: '#409EFF' }
    }]
  }, true)
}

const initCompareChart = (statsData) => {
  if (!compareChartRef.value) return
  if (!compareChart) {
    compareChart = echarts.init(compareChartRef.value)
  }
  compareChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { data: ['平均分', '最高分', '最低分'], bottom: 0 },
    radar: {
      indicator: statsData.map(i => ({ name: i.courseName, max: 100 })),
      shape: 'polygon'
    },
    series: [{
      type: 'radar',
      data: [
        { value: statsData.map(i => Number(i.avgScore).toFixed(2)), name: '平均分', areaStyle: { opacity: 0.1 } },
        { value: statsData.map(i => Number(i.maxScore).toFixed(2)), name: '最高分', areaStyle: { opacity: 0.1 } },
        { value: statsData.map(i => Number(i.minScore).toFixed(2)), name: '最低分', areaStyle: { opacity: 0.1 } }
      ]
    }]
  }, true)
}

const loadTrend = async () => {
  if (!trendForm.studentId) {
    ElMessage.warning('请先选择学生')
    return
  }

  trendLoading.value = true
  try {
    const params = {}
    if (trendForm.courseId) params.courseId = trendForm.courseId
    const res = await getStudentTrend(trendForm.studentId, params)
    const data = res.data || []
    await nextTick()
    initTrendChart(data)
  } catch (e) {
  } finally {
    trendLoading.value = false
  }
}

const initTrendChart = (data = []) => {
  if (!trendChartRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const courseMap = new Map()
  data.forEach(item => {
    const courseName = item.courseName
    if (!courseMap.has(courseName)) {
      courseMap.set(courseName, [])
    }
    courseMap.get(courseName).push({
      time: item.semester || item.examTime,
      score: Number(item.score)
    })
  })

  const allTimes = [...new Set(data.map(item => item.semester || item.examTime))].sort()
  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#8e44ad', '#16a085', '#d35400']

  const series = []
  let colorIndex = 0
  courseMap.forEach((scores, courseName) => {
    const scoreMap = new Map(scores.map(s => [s.time, s.score]))
    const dataArr = allTimes.map(time => scoreMap.has(time) ? scoreMap.get(time) : null)
    series.push({
      name: courseName,
      type: 'line',
      data: dataArr,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { width: 2, color: colors[colorIndex % colors.length] },
      itemStyle: { color: colors[colorIndex % colors.length] },
      connectNulls: false
    })
    colorIndex++
  })

  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      textStyle: { color: '#fff' }
    },
    legend: {
      data: Array.from(courseMap.keys()),
      top: 0,
      type: 'scroll'
    },
    grid: { left: 50, right: 30, top: 60, bottom: 40 },
    xAxis: {
      type: 'category',
      data: allTimes,
      axisLabel: { fontSize: 12, color: '#606266' },
      axisLine: { lineStyle: { color: '#dcdfe6' } }
    },
    yAxis: {
      type: 'value',
      name: '分数',
      min: 0,
      max: 100,
      nameTextStyle: { fontSize: 12, color: '#909399' },
      axisLabel: { fontSize: 12, color: '#606266' },
      splitLine: { lineStyle: { color: '#ebeef5', type: 'dashed' } }
    },
    series
  }, true)
}

const loadCompare = async () => {
  if (!compareForm.class1Id || !compareForm.class2Id || !compareForm.courseId) {
    ElMessage.warning('请选择两个班级和一个科目')
    return
  }

  compareLoading.value = true
  compareSearched.value = true
  try {
    const res = await getClassComparison({
      class1Id: compareForm.class1Id,
      class2Id: compareForm.class2Id,
      courseId: compareForm.courseId
    })
    compareData.value = res.data || []
    await nextTick()
    initCompareBarChart()
  } catch (e) {
  } finally {
    compareLoading.value = false
  }
}

const initCompareBarChart = () => {
  if (!compareBarChartRef.value || compareData.value.length < 2) return
  if (!compareBarChart) {
    compareBarChart = echarts.init(compareBarChartRef.value)
  }

  const data1 = compareData.value[0]
  const data2 = compareData.value[1]

  compareBarChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      textStyle: { color: '#fff' }
    },
    legend: { data: [data1.className, data2.className], bottom: 0 },
    grid: { left: 60, right: 30, top: 30, bottom: 50 },
    xAxis: {
      type: 'category',
      data: ['平均分', '中位数', '标准差', '及格率(%)'],
      axisLabel: { fontSize: 13, color: '#606266' },
      axisLine: { lineStyle: { color: '#dcdfe6' } }
    },
    yAxis: {
      type: 'value',
      name: '数值',
      nameTextStyle: { fontSize: 12, color: '#909399' },
      axisLabel: { fontSize: 12, color: '#606266' },
      splitLine: { lineStyle: { color: '#ebeef5', type: 'dashed' } }
    },
    series: [
      {
        name: data1.className,
        type: 'bar',
        data: [
          Number(data1.avgScore || 0).toFixed(2),
          Number(data1.medianScore || 0).toFixed(2),
          Number(data1.stdDevScore || 0).toFixed(2),
          Number(data1.passRate || 0).toFixed(2)
        ],
        itemStyle: { color: '#409EFF', borderRadius: [4, 4, 0, 0] },
        barWidth: '30%',
        label: { show: true, position: 'top', fontSize: 12, color: '#409EFF', fontWeight: 'bold' }
      },
      {
        name: data2.className,
        type: 'bar',
        data: [
          Number(data2.avgScore || 0).toFixed(2),
          Number(data2.medianScore || 0).toFixed(2),
          Number(data2.stdDevScore || 0).toFixed(2),
          Number(data2.passRate || 0).toFixed(2)
        ],
        itemStyle: { color: '#67C23A', borderRadius: [4, 4, 0, 0] },
        barWidth: '30%',
        label: { show: true, position: 'top', fontSize: 12, color: '#67C23A', fontWeight: 'bold' }
      }
    ]
  }, true)
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  loadStats()
  loadStudentList()
  loadCourseList()
  loadClassList()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  avgChart?.dispose()
  compareChart?.dispose()
  trendChart?.dispose()
  compareBarChart?.dispose()
})
</script>

<style scoped>
.analysis-row {
  display: flex;
  align-items: stretch;
}
.analysis-row > .el-col {
  display: flex;
}
.analysis-card {
  width: 100%;
}
.analysis-card :deep(.el-card__body) {
  padding: 16px;
  height: 420px;
  box-sizing: border-box;
}
.chart-container {
  width: 100%;
  height: 100%;
}
.ranking-table {
  width: 100%;
}
.ranking-table :deep(.el-table__body-wrapper) {
  max-height: 380px;
  overflow-y: auto;
}
.stat-item {
  text-align: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
</style>
