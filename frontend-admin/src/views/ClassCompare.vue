<template>
  <div>
    <el-card style="margin-bottom: 20px">
      <div style="display: flex; gap: 16px; align-items: center; flex-wrap: wrap">
        <el-select v-model="classAId" placeholder="班级 A" filterable style="width: 220px">
          <el-option
            v-for="c in classList"
            :key="c.id"
            :label="c.className"
            :value="c.id"
            :disabled="c.id === classBId"
          />
        </el-select>

        <span style="color: #909399">VS</span>

        <el-select v-model="classBId" placeholder="班级 B" filterable style="width: 220px">
          <el-option
            v-for="c in classList"
            :key="c.id"
            :label="c.className"
            :value="c.id"
            :disabled="c.id === classAId"
          />
        </el-select>

        <el-select v-model="courseId" placeholder="科目" filterable style="width: 220px">
          <el-option
            v-for="c in courseList"
            :key="c.id"
            :label="c.courseName"
            :value="c.id"
          />
        </el-select>

        <el-button type="primary" :loading="loading" @click="loadCompare">对比</el-button>
      </div>
    </el-card>

    <div v-if="hasResult">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span style="font-weight: bold">{{ classAName }}</span>
            </template>
            <stat-list :data="result.classA" />
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span style="font-weight: bold">{{ classBName }}</span>
            </template>
            <stat-list :data="result.classB" />
          </el-card>
        </el-col>
      </el-row>

      <el-card style="margin-top: 20px">
        <template #header>对比柱状图</template>
        <div ref="chartRef" class="compare-chart"></div>
      </el-card>
    </div>

    <el-empty v-else-if="searched" description="暂无对比数据" />
  </div>
</template>

<script setup>
import { ref, computed, h, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getClassList } from '@/api/student'
import { getCourseList } from '@/api/course'
import { compareClasses } from '@/api/scoreAnalysis'

const classList = ref([])
const courseList = ref([])
const classAId = ref(null)
const classBId = ref(null)
const courseId = ref(null)
const result = ref({ classA: {}, classB: {} })
const hasResult = ref(false)
const searched = ref(false)
const loading = ref(false)

const chartRef = ref()
let chart = null

const classAName = computed(() => classList.value.find(c => c.id === classAId.value)?.className || '班级A')
const classBName = computed(() => classList.value.find(c => c.id === classBId.value)?.className || '班级B')

const handleResize = () => chart?.resize()

// 局部小组件：统计指标列表
const StatList = {
  props: ['data'],
  setup(props) {
    const items = computed(() => [
      { label: '参与人数', value: props.data.studentCount ?? 0, suffix: '人' },
      { label: '平均分', value: fmt(props.data.avgScore), suffix: '分' },
      { label: '中位数', value: fmt(props.data.medianScore), suffix: '分' },
      { label: '标准差', value: fmt(props.data.stdDev), suffix: '' },
      { label: '及格率', value: fmt(props.data.passRate), suffix: '%' }
    ])
    return () => h('div', { class: 'stat-list' }, items.value.map(it =>
      h('div', { class: 'stat-row' }, [
        h('span', { class: 'stat-label' }, it.label),
        h('span', { class: 'stat-value' }, `${it.value}${it.suffix}`)
      ])
    ))
  }
}

function fmt(v) {
  if (v === null || v === undefined || v === '') return '--'
  return Number(v).toFixed(2)
}

const loadBasicData = async () => {
  try {
    const [clsRes, courseRes] = await Promise.all([
      getClassList(),
      getCourseList()
    ])
    classList.value = clsRes.data || []
    courseList.value = courseRes.data || []
  } catch (e) { /* 拦截器统一处理 */ }
}

const loadCompare = async () => {
  if (!classAId.value || !classBId.value || !courseId.value) {
    ElMessage.warning('请选择两个班级和一个科目')
    return
  }
  if (classAId.value === classBId.value) {
    ElMessage.warning('请选择两个不同的班级')
    return
  }

  loading.value = true
  searched.value = true
  try {
    const res = await compareClasses(classAId.value, classBId.value, courseId.value)
    result.value = res.data || { classA: {}, classB: {} }
    hasResult.value = true
    await nextTick()
    renderChart()
  } catch (e) { /* 拦截器统一处理 */ } finally {
    loading.value = false
  }
}

const renderChart = () => {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)

  const metrics = ['平均分', '中位数', '标准差', '及格率(%)']
  const a = result.value.classA || {}
  const b = result.value.classB || {}
  const valuesA = [a.avgScore, a.medianScore, a.stdDev, a.passRate].map(toNum)
  const valuesB = [b.avgScore, b.medianScore, b.stdDev, b.passRate].map(toNum)

  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: [classAName.value, classBName.value], top: 0 },
    grid: { left: 50, right: 30, top: 50, bottom: 40 },
    xAxis: { type: 'category', data: metrics },
    yAxis: { type: 'value' },
    series: [
      {
        name: classAName.value,
        type: 'bar',
        data: valuesA,
        itemStyle: { color: '#409EFF', borderRadius: [4, 4, 0, 0] },
        label: { show: true, position: 'top' },
        barWidth: '30%'
      },
      {
        name: classBName.value,
        type: 'bar',
        data: valuesB,
        itemStyle: { color: '#67C23A', borderRadius: [4, 4, 0, 0] },
        label: { show: true, position: 'top' },
        barWidth: '30%'
      }
    ]
  }, true)
}

function toNum(v) {
  if (v === null || v === undefined || v === '') return 0
  return Number(Number(v).toFixed(2))
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  loadBasicData()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<style scoped>
.compare-chart {
  width: 100%;
  height: 380px;
}
.stat-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.stat-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 14px;
  background-color: #f5f7fa;
  border-radius: 6px;
}
.stat-label {
  color: #606266;
}
.stat-value {
  color: #303133;
  font-weight: bold;
  font-size: 16px;
}
</style>
