<template>
  <div>
    <el-card style="margin-bottom: 20px">
      <div style="display: flex; align-items: center; gap: 16px; flex-wrap: wrap">
        <el-select v-model="class1Id" placeholder="选择班级1" style="width: 200px" @change="loadComparison">
          <el-option v-for="c in classList" :key="c.id" :label="c.className" :value="c.id" />
        </el-select>
        <span style="color: #909399">VS</span>
        <el-select v-model="class2Id" placeholder="选择班级2" style="width: 200px" @change="loadComparison">
          <el-option v-for="c in classList" :key="c.id" :label="c.className" :value="c.id" />
        </el-select>
        <el-select v-model="courseId" placeholder="选择科目" style="width: 200px" @change="loadComparison">
          <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
        </el-select>
        <el-button type="primary" :loading="loading" @click="loadComparison">开始对比</el-button>
      </div>
    </el-card>

    <div v-if="hasData">
      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span style="font-weight: bold">{{ class1Data?.className }}</span>
                <el-tag type="primary" size="small">班级1</el-tag>
              </div>
            </template>
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="参与人数">{{ class1Data?.studentCount }}</el-descriptions-item>
              <el-descriptions-item label="平均分">
                <span style="color: #409EFF; font-weight: bold">{{ class1Data?.avgScore?.toFixed(2) }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="中位数">{{ class1Data?.medianScore?.toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item label="标准差">{{ class1Data?.stdDevScore?.toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item label="及格率" :span="2">
                <el-progress :percentage="Number(class1Data?.passRate?.toFixed(2) || 0)" :color="getProgressColor(class1Data?.passRate)" />
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span style="font-weight: bold">{{ class2Data?.className }}</span>
                <el-tag type="success" size="small">班级2</el-tag>
              </div>
            </template>
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="参与人数">{{ class2Data?.studentCount }}</el-descriptions-item>
              <el-descriptions-item label="平均分">
                <span style="color: #67c23a; font-weight: bold">{{ class2Data?.avgScore?.toFixed(2) }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="中位数">{{ class2Data?.medianScore?.toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item label="标准差">{{ class2Data?.stdDevScore?.toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item label="及格率" :span="2">
                <el-progress :percentage="Number(class2Data?.passRate?.toFixed(2) || 0)" :color="getProgressColor(class2Data?.passRate)" />
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <el-card>
        <template #header>
          <span>数据对比图</span>
        </template>
        <div ref="chartRef" style="height: 450px"></div>
      </el-card>
    </div>

    <el-empty v-else-if="searched" description="暂无对比数据" />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getClassScoreComparison, getCourseList } from '@/api/score'
import { ElMessage } from 'element-plus'

const class1Id = ref(null)
const class2Id = ref(null)
const courseId = ref(null)
const classList = ref([
  { id: 1, className: '计算机2201班' },
  { id: 2, className: '计算机2202班' },
  { id: 3, className: '软件2201班' },
  { id: 4, className: '软件2202班' },
  { id: 5, className: '信安2201班' },
  { id: 6, className: '计算机2301班' },
  { id: 7, className: '计算机2302班' },
  { id: 8, className: '软件2301班' },
  { id: 9, className: '数据2301班' },
  { id: 10, className: '人工智能2301班' }
])
const courseList = ref([])
const comparisonData = ref([])
const class1Data = ref(null)
const class2Data = ref(null)
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

const loadCourses = async () => {
  try {
    const res = await getCourseList()
    courseList.value = res.data?.records || []
    if (courseList.value.length > 0) {
      courseId.value = courseList.value[0].id
    }
  } catch (e) {
    console.error('加载课程列表失败', e)
  }
}

const getProgressColor = (rate) => {
  if (rate >= 90) return '#67c23a'
  if (rate >= 70) return '#409EFF'
  if (rate >= 60) return '#e6a23c'
  return '#f56c6c'
}

const loadComparison = async () => {
  if (!class1Id.value || !class2Id.value || !courseId.value) {
    ElMessage.warning('请选择两个班级和一个科目')
    return
  }
  if (class1Id.value === class2Id.value) {
    ElMessage.warning('请选择不同的两个班级')
    return
  }

  loading.value = true
  searched.value = true
  try {
    const res = await getClassScoreComparison({
      courseId: courseId.value,
      class1Id: class1Id.value,
      class2Id: class2Id.value
    })
    comparisonData.value = res.data || []

    if (comparisonData.value.length === 0) {
      hasData.value = false
      return
    }

    class1Data.value = comparisonData.value.find(d => d.classTag === 'class1') || null
    class2Data.value = comparisonData.value.find(d => d.classTag === 'class2') || null

    hasData.value = !!(class1Data.value && class2Data.value)
    if (hasData.value) {
      await nextTick()
      await initChart()
      renderChart()
    }
  } catch (e) {
    console.error('加载对比数据失败', e)
  } finally {
    loading.value = false
  }
}

const renderChart = () => {
  const indicators = ['平均分', '中位数', '及格率', '100-标准差']
  const c1 = class1Data.value
  const c2 = class2Data.value

  const normalize = (val, max) => Math.min(100, (val / max) * 100)

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      textStyle: { color: '#fff' }
    },
    legend: {
      data: [c1.className, c2.className],
      bottom: 0
    },
    grid: {
      left: 60,
      right: 30,
      top: 40,
      bottom: 60
    },
    xAxis: {
      type: 'category',
      data: indicators,
      axisLabel: { fontSize: 13, color: '#606266' },
      axisLine: { lineStyle: { color: '#dcdfe6' } }
    },
    yAxis: {
      type: 'value',
      name: '数值',
      nameTextStyle: { fontSize: 12, color: '#909399' },
      min: 0,
      max: 100,
      axisLabel: { fontSize: 12, color: '#606266' },
      splitLine: { lineStyle: { color: '#ebeef5', type: 'dashed' } }
    },
    series: [
      {
        name: c1.className,
        type: 'bar',
        data: [
          Number(c1.avgScore?.toFixed(2) || 0),
          Number(c1.medianScore?.toFixed(2) || 0),
          Number(c1.passRate?.toFixed(2) || 0),
          Math.max(0, 100 - Number(c1.stdDevScore?.toFixed(2) || 0))
        ],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409EFF' },
            { offset: 1, color: '#a0cfff' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        barWidth: '30%',
        label: {
          show: true,
          position: 'top',
          formatter: '{c}',
          fontSize: 12,
          color: '#409EFF',
          fontWeight: 'bold'
        }
      },
      {
        name: c2.className,
        type: 'bar',
        data: [
          Number(c2.avgScore?.toFixed(2) || 0),
          Number(c2.medianScore?.toFixed(2) || 0),
          Number(c2.passRate?.toFixed(2) || 0),
          Math.max(0, 100 - Number(c2.stdDevScore?.toFixed(2) || 0))
        ],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#67c23a' },
            { offset: 1, color: '#b3e19d' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        barWidth: '30%',
        label: {
          show: true,
          position: 'top',
          formatter: '{c}',
          fontSize: 12,
          color: '#67c23a',
          fontWeight: 'bold'
        }
      }
    ]
  }, true)
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)
  await loadCourses()
  if (classList.value.length >= 2) {
    class1Id.value = classList.value[0].id
    class2Id.value = classList.value[1].id
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<style scoped>
</style>
