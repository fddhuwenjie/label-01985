<template>
  <div>
    <el-card style="margin-bottom: 20px">
      <div style="display: flex; align-items: center; gap: 16px; flex-wrap: wrap">
        <el-select
          v-model="studentId"
          filterable
          placeholder="请选择学生"
          style="width: 260px"
          @change="loadTrend"
        >
          <el-option
            v-for="s in studentList"
            :key="s.id"
            :label="`${s.name}（${s.studentNo}）`"
            :value="s.id"
          />
        </el-select>

        <el-select
          v-model="courseId"
          clearable
          placeholder="按科目筛选（默认全部）"
          style="width: 240px"
          @change="loadTrend"
        >
          <el-option
            v-for="c in courseList"
            :key="c.id"
            :label="c.courseName"
            :value="c.id"
          />
        </el-select>

        <el-button type="primary" :loading="loading" @click="loadTrend">查询趋势</el-button>
      </div>
    </el-card>

    <el-card>
      <template #header>
        <span>成绩趋势折线图</span>
        <span style="margin-left: 12px; color: #909399; font-size: 12px">
          横轴：考试时间；纵轴：分数；按科目分组
        </span>
      </template>
      <div v-show="hasData" ref="trendChartRef" class="trend-chart"></div>
      <el-empty v-if="!hasData && searched" description="暂无成绩趋势数据" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getStudentList } from '@/api/student'
import { getCourseList } from '@/api/course'
import { getScoreTrend } from '@/api/scoreAnalysis'
import { formatBeijingTime } from '@/utils/format'

const studentList = ref([])
const courseList = ref([])
const studentId = ref(null)
const courseId = ref(null)
const loading = ref(false)
const hasData = ref(false)
const searched = ref(false)

const trendChartRef = ref()
let trendChart = null

const handleResize = () => trendChart?.resize()

const initChart = async () => {
  await nextTick()
  if (trendChartRef.value && !trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
}

const loadBasicData = async () => {
  try {
    const [stuRes, courseRes] = await Promise.all([
      getStudentList(),
      getCourseList()
    ])
    studentList.value = stuRes.data || []
    courseList.value = courseRes.data || []
    if (studentList.value.length && !studentId.value) {
      studentId.value = studentList.value[0].id
      loadTrend()
    }
  } catch (e) { /* 拦截器统一处理 */ }
}

const loadTrend = async () => {
  if (!studentId.value) {
    ElMessage.warning('请先选择学生')
    return
  }
  loading.value = true
  searched.value = true
  try {
    const res = await getScoreTrend(studentId.value, courseId.value || undefined)
    const data = res.data || {}
    const grouped = data.groupedBySubject || {}
    const subjects = Object.keys(grouped)

    if (subjects.length === 0) {
      hasData.value = false
      return
    }

    // 收集所有时间点（横轴去重并排序）
    const timeSet = new Set()
    subjects.forEach(name => {
      grouped[name].forEach(r => timeSet.add(r.examTime))
    })
    const xAxisData = Array.from(timeSet).sort()

    const series = subjects.map(name => {
      const map = new Map()
      grouped[name].forEach(r => map.set(r.examTime, Number(r.score)))
      return {
        name,
        type: 'line',
        smooth: true,
        connectNulls: true,
        symbol: 'circle',
        symbolSize: 8,
        data: xAxisData.map(t => (map.has(t) ? map.get(t) : null)),
        label: { show: true, fontSize: 11 }
      }
    })

    hasData.value = true
    await initChart()

    trendChart.setOption({
      tooltip: {
        trigger: 'axis',
        formatter: (params) => {
          const time = formatBeijingTime(params[0].axisValue)
          const lines = params
            .filter(p => p.value != null)
            .map(p => `${p.marker} ${p.seriesName}: <b>${p.value}</b> 分`)
            .join('<br/>')
          return `${time}<br/>${lines}`
        }
      },
      legend: { top: 0, type: 'scroll' },
      grid: { left: 50, right: 30, top: 50, bottom: 70 },
      xAxis: {
        type: 'category',
        data: xAxisData,
        axisLabel: {
          formatter: (v) => formatBeijingTime(v),
          rotate: 30,
          fontSize: 12
        },
        boundaryGap: false
      },
      yAxis: {
        type: 'value',
        name: '分数',
        min: 0,
        max: 100,
        splitLine: { lineStyle: { type: 'dashed', color: '#ebeef5' } }
      },
      dataZoom: [{ type: 'inside' }, { type: 'slider', height: 20 }],
      series
    }, true)
  } catch (e) { /* 拦截器统一处理 */ } finally {
    loading.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  loadBasicData()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
})
</script>

<style scoped>
.trend-chart {
  width: 100%;
  height: 460px;
}
</style>
