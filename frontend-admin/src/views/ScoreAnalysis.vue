<template>
  <div>
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
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getScoreStats, getStudentRanking } from '@/api/score'
import { ElMessage } from 'element-plus'

const semester = ref('2024-2025-1')
const rankingData = ref([])
const statsLoading = ref(false)
const hasData = ref(false)
const searched = ref(false)
const avgChartRef = ref()
const compareChartRef = ref()
let avgChart = null
let compareChart = null

const handleResize = () => {
  avgChart?.resize()
  compareChart?.resize()
}

const initCharts = async () => {
  await nextTick()
  if (avgChartRef.value && !avgChart) {
    avgChart = echarts.init(avgChartRef.value)
  }
  if (compareChartRef.value && !compareChart) {
    compareChart = echarts.init(compareChartRef.value)
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
    await initCharts()

    // 平均分柱状图
    avgChart.setOption({
      tooltip: { 
        trigger: 'axis', 
        formatter: '{b}<br/>平均分: {c} 分',
        backgroundColor: 'rgba(50, 50, 50, 0.9)',
        textStyle: { color: '#fff' }
      },
      grid: { 
        left: 50, 
        right: 20, 
        top: 35, 
        bottom: 70
      },
      xAxis: { 
        type: 'category', 
        data: statsData.map(i => i.courseName), 
        axisLabel: { 
          rotate: 35, 
          interval: 0, 
          fontSize: 12,
          color: '#606266'
        },
        axisLine: { lineStyle: { color: '#dcdfe6' } },
        axisTick: { alignWithLabel: true }
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
        label: { 
          show: true, 
          position: 'top', 
          formatter: '{c}', 
          fontSize: 12,
          fontWeight: 'bold',
          color: '#409EFF'
        }
      }]
    }, true)

    // 雷达图
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
  } catch (e) {
    // 已由拦截器统一处理
  } finally {
    statsLoading.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  loadStats()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  avgChart?.dispose()
  compareChart?.dispose()
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
</style>
