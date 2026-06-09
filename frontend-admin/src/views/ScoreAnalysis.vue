<template>
  <div>
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="成绩统计" name="stats">
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
      </el-tab-pane>

      <el-tab-pane label="成绩趋势分析" name="trend">
        <el-card style="margin-bottom: 20px">
          <div style="display: flex; align-items: center; gap: 16px; flex-wrap: wrap">
            <span>选择学生：</span>
            <el-select v-model="selectedStudent" placeholder="请选择学生" style="width: 220px" filterable>
              <el-option v-for="s in students" :key="s.id" :label="`${s.name} (${s.studentNo})`" :value="s.id" />
            </el-select>
            <span>科目筛选：</span>
            <el-select v-model="selectedCourse" placeholder="全部科目" clearable style="width: 200px">
              <el-option v-for="c in courses" :key="c.id" :label="c.courseName" :value="c.id" />
            </el-select>
            <el-button type="primary" :loading="trendLoading" @click="loadTrend">查看趋势</el-button>
          </div>
        </el-card>
        <el-card v-if="trendData.length > 0">
          <template #header>历次考试成绩趋势</template>
          <div ref="trendChartRef" style="height: 450px"></div>
        </el-card>
        <el-empty v-else-if="trendSearched" description="暂无该学生的成绩数据" />
      </el-tab-pane>

      <el-tab-pane label="成绩预警" name="alert">
        <el-card style="margin-bottom: 20px">
          <div style="display: flex; align-items: center; gap: 16px; flex-wrap: wrap">
            <el-button type="danger" :loading="generating" @click="doGenerateAlerts">
              <el-icon><Warning /></el-icon>&nbsp;一键生成预警
            </el-button>
            <el-tag v-if="unhandledCount > 0" type="danger">未处理预警：{{ unhandledCount }} 条</el-tag>
            <div style="flex: 1"></div>
            <el-input v-model="alertQuery.studentName" placeholder="学生姓名" style="width: 150px" clearable />
            <el-input v-model="alertQuery.courseName" placeholder="课程名称" style="width: 150px" clearable />
            <el-select v-model="alertQuery.status" placeholder="状态" clearable style="width: 120px">
              <el-option label="未处理" :value="0" />
              <el-option label="已处理" :value="1" />
            </el-select>
            <el-button type="primary" @click="loadAlerts">查询</el-button>
          </div>
        </el-card>
        <el-card>
          <el-table :data="alertList" border stripe v-loading="alertLoading">
            <el-table-column type="index" label="#" width="55" align="center" />
            <el-table-column prop="studentName" label="学生姓名" min-width="100" />
            <el-table-column prop="courseName" label="科目" min-width="140" />
            <el-table-column label="上次成绩" width="100" align="right">
              <template #default="{ row }">{{ Number(row.previousScore).toFixed(1) }}</template>
            </el-table-column>
            <el-table-column label="本次成绩" width="100" align="right">
              <template #default="{ row }">{{ Number(row.currentScore).toFixed(1) }}</template>
            </el-table-column>
            <el-table-column label="下降幅度" width="110" align="right">
              <template #default="{ row }">
                <span :class="['drop-amount', 'level-' + row.alertLevel.toLowerCase()]">
                  -{{ Number(row.dropAmount).toFixed(1) }} 分
                </span>
              </template>
            </el-table-column>
            <el-table-column label="预警等级" width="110" align="center">
              <template #default="{ row }">
                <el-tag :type="alertTagType(row.alertLevel)" size="small">{{ alertLevelLabel(row.alertLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'warning' : 'success'" size="small">
                  {{ row.status === 0 ? '未处理' : '已处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="生成时间" min-width="170" />
            <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
            <el-table-column label="操作" width="120" align="center" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 0" type="primary" link size="small" @click="openHandleDialog(row)">
                  标记处理
                </el-button>
                <span v-else style="color: #909399">-</span>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            style="margin-top: 16px; justify-content: flex-end; display: flex"
            v-model:current-page="alertPage.current"
            v-model:page-size="alertPage.size"
            :total="alertPage.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadAlerts"
            @current-change="loadAlerts"
          />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="班级对比" name="compare">
        <el-card style="margin-bottom: 20px">
          <div style="display: flex; align-items: center; gap: 16px; flex-wrap: wrap">
            <span>班级1：</span>
            <el-select v-model="compareForm.classId1" placeholder="选择班级A" style="width: 200px">
              <el-option v-for="c in classes" :key="c.id" :label="c.className" :value="c.id" />
            </el-select>
            <span>班级2：</span>
            <el-select v-model="compareForm.classId2" placeholder="选择班级B" style="width: 200px">
              <el-option v-for="c in classes" :key="c.id" :label="c.className" :value="c.id" />
            </el-select>
            <span>科目：</span>
            <el-select v-model="compareForm.courseId" placeholder="选择科目" style="width: 200px">
              <el-option v-for="c in courses" :key="c.id" :label="c.courseName" :value="c.id" />
            </el-select>
            <el-button type="primary" :loading="compareLoading" @click="doCompare">开始对比</el-button>
          </div>
        </el-card>
        <el-row v-if="compareResult" :gutter="20">
          <el-col :span="12">
            <el-card class="compare-card">
              <template #header>
                <span style="font-weight: bold">{{ className(compareForm.classId1) }}</span>
              </template>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="参考人数">{{ compareResult.class1.studentCount }}</el-descriptions-item>
                <el-descriptions-item label="平均分">
                  <span class="stat-num avg">{{ Number(compareResult.class1.avgScore).toFixed(2) }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="中位数">
                  <span class="stat-num">{{ Number(compareResult.class1.medianScore).toFixed(2) }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="标准差">
                  <span class="stat-num std">{{ Number(compareResult.class1.stdDev).toFixed(2) }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="及格率">
                  <span class="stat-num pass">{{ Number(compareResult.class1.passRate).toFixed(2) }}%</span>
                </el-descriptions-item>
                <el-descriptions-item label="最高分">{{ compareResult.class1.maxScore }}</el-descriptions-item>
                <el-descriptions-item label="最低分">{{ compareResult.class1.minScore }}</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="compare-card">
              <template #header>
                <span style="font-weight: bold">{{ className(compareForm.classId2) }}</span>
              </template>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="参考人数">{{ compareResult.class2.studentCount }}</el-descriptions-item>
                <el-descriptions-item label="平均分">
                  <span class="stat-num avg">{{ Number(compareResult.class2.avgScore).toFixed(2) }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="中位数">
                  <span class="stat-num">{{ Number(compareResult.class2.medianScore).toFixed(2) }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="标准差">
                  <span class="stat-num std">{{ Number(compareResult.class2.stdDev).toFixed(2) }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="及格率">
                  <span class="stat-num pass">{{ Number(compareResult.class2.passRate).toFixed(2) }}%</span>
                </el-descriptions-item>
                <el-descriptions-item label="最高分">{{ compareResult.class2.maxScore }}</el-descriptions-item>
                <el-descriptions-item label="最低分">{{ compareResult.class2.minScore }}</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
        </el-row>
        <el-card v-if="compareResult" style="margin-top: 20px">
          <template #header>对比可视化</template>
          <div ref="compareBarRef" style="height: 400px"></div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="handleDialogVisible" title="标记预警为已处理" width="460px">
      <el-form label-width="80px">
        <el-form-item label="学生">{{ currentAlert?.studentName }}</el-form-item>
        <el-form-item label="科目">{{ currentAlert?.courseName }}</el-form-item>
        <el-form-item label="下降幅度">{{ currentAlert ? Number(currentAlert.dropAmount).toFixed(1) : 0 }} 分</el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="handleRemark" type="textarea" :rows="3" placeholder="请输入处理意见（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="handling" @click="confirmHandle">确认标记</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { getScoreStats, getStudentRanking } from '@/api/score'
import {
  getStudentTrend, classCompare, generateAlerts, getAlertPage, handleAlert,
  getUnhandledCount, listStudents, listCourses, listClasses
} from '@/api/analysis'

const activeTab = ref('stats')

// ---------- 原有统计 ----------
const semester = ref('2024-2025-1')
const rankingData = ref([])
const statsLoading = ref(false)
const hasData = ref(false)
const searched = ref(false)
const avgChartRef = ref()
const compareChartRef = ref()
let avgChart = null
let compareChart = null

// ---------- 趋势分析 ----------
const students = ref([])
const courses = ref([])
const classes = ref([])
const selectedStudent = ref(null)
const selectedCourse = ref(null)
const trendLoading = ref(false)
const trendData = ref([])
const trendSearched = ref(false)
const trendChartRef = ref()
let trendChart = null

// ---------- 预警 ----------
const generating = ref(false)
const alertLoading = ref(false)
const alertList = ref([])
const unhandledCount = ref(0)
const alertQuery = ref({ studentName: '', courseName: '', status: null })
const alertPage = ref({ current: 1, size: 10, total: 0 })
const handleDialogVisible = ref(false)
const currentAlert = ref(null)
const handleRemark = ref('')
const handling = ref(false)

// ---------- 班级对比 ----------
const compareLoading = ref(false)
const compareForm = ref({ classId1: null, classId2: null, courseId: null })
const compareResult = ref(null)
const compareBarRef = ref()
let compareBarChart = null

const handleResize = () => {
  avgChart?.resize()
  compareChart?.resize()
  trendChart?.resize()
  compareBarChart?.resize()
}

const className = (id) => classes.value.find(c => c.id === id)?.className || '-'

const alertLevelLabel = (level) => ({ YELLOW: '黄色预警', ORANGE: '橙色预警', RED: '红色预警' }[level] || level)
const alertTagType = (level) => ({ YELLOW: 'warning', ORANGE: 'danger', RED: 'danger' }[level] || 'info')

// ---------- 原有统计 ----------
const initStatsCharts = async () => {
  await nextTick()
  if (avgChartRef.value && !avgChart) avgChart = echarts.init(avgChartRef.value)
  if (compareChartRef.value && !compareChart) compareChart = echarts.init(compareChartRef.value)
}

const loadStats = async () => {
  if (!semester.value?.trim()) { ElMessage.warning('请先输入学期'); return }
  statsLoading.value = true; searched.value = true
  try {
    const [statsRes, rankRes] = await Promise.all([getScoreStats(semester.value), getStudentRanking(semester.value)])
    rankingData.value = (rankRes.data || []).slice(0, 10)
    const statsData = statsRes.data || []
    hasData.value = statsData.length > 0
    if (!hasData.value) return
    await nextTick(); await initStatsCharts()
    avgChart.setOption({
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(50,50,50,0.9)', textStyle: { color: '#fff' } },
      grid: { left: 50, right: 20, top: 35, bottom: 70 },
      xAxis: { type: 'category', data: statsData.map(i => i.courseName),
        axisLabel: { rotate: 35, interval: 0, fontSize: 12, color: '#606266' },
        axisLine: { lineStyle: { color: '#dcdfe6' } }, axisTick: { alignWithLabel: true } },
      yAxis: { type: 'value', name: '分数', min: 0, max: 100, interval: 20,
        axisLabel: { fontSize: 12, color: '#606266' }, splitLine: { lineStyle: { color: '#ebeef5', type: 'dashed' } } },
      series: [{ type: 'bar', data: statsData.map(i => Number(i.avgScore).toFixed(2)),
        itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409EFF' }, { offset: 1, color: '#a0cfff' }]), borderRadius: [4,4,0,0] },
        barWidth: '50%', label: { show: true, position: 'top', formatter: '{c}', fontSize: 12, fontWeight: 'bold', color: '#409EFF' } }]
    }, true)
    compareChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { data: ['平均分', '最高分', '最低分'], bottom: 0 },
      radar: { indicator: statsData.map(i => ({ name: i.courseName, max: 100 })), shape: 'polygon' },
      series: [{ type: 'radar', data: [
        { value: statsData.map(i => Number(i.avgScore).toFixed(2)), name: '平均分', areaStyle: { opacity: 0.1 } },
        { value: statsData.map(i => Number(i.maxScore).toFixed(2)), name: '最高分', areaStyle: { opacity: 0.1 } },
        { value: statsData.map(i => Number(i.minScore).toFixed(2)), name: '最低分', areaStyle: { opacity: 0.1 } }
      ]}]
    }, true)
  } finally { statsLoading.value = false }
}

// ---------- 趋势 ----------
const loadTrend = async () => {
  if (!selectedStudent.value) { ElMessage.warning('请选择学生'); return }
  trendLoading.value = true; trendSearched.value = true
  try {
    const res = await getStudentTrend(selectedStudent.value, selectedCourse.value)
    trendData.value = res.data || []
    if (!trendData.value.length) {
      if (trendChart) { trendChart.dispose(); trendChart = null }
      return
    }
    await nextTick()
    if (!trendChart) trendChart = echarts.init(trendChartRef.value)
    // 按课程分组
    const groupMap = {}
    trendData.value.forEach(item => {
      const key = item.courseName
      if (!groupMap[key]) groupMap[key] = []
      groupMap[key].push([item.examName || item.semester, Number(item.score)])
    })
    const series = Object.keys(groupMap).map(name => ({
      name, type: 'line', smooth: true, symbolSize: 8,
      data: groupMap[name].map(d => d[1]),
      label: { show: true, formatter: '{c}' },
      emphasis: { focus: 'series' }
    }))
    const xData = groupMap[Object.keys(groupMap)[0]].map(d => d[0])
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { top: 0 },
      grid: { left: 50, right: 30, top: 50, bottom: 40 },
      xAxis: { type: 'category', data: xData, axisLabel: { color: '#606266' } },
      yAxis: { type: 'value', name: '分数', min: 0, max: 100,
        axisLabel: { color: '#606266' }, splitLine: { lineStyle: { type: 'dashed', color: '#ebeef5' } } },
      series
    }, true)
  } finally { trendLoading.value = false }
}

// ---------- 预警 ----------
const loadAlerts = async () => {
  alertLoading.value = true
  try {
    const res = await getAlertPage(alertPage.value.current, alertPage.value.size, alertQuery.value)
    alertList.value = res.data?.records || []
    alertPage.value.total = res.data?.total || 0
  } finally { alertLoading.value = false }
}

const loadUnhandledCount = async () => {
  try {
    const res = await getUnhandledCount()
    unhandledCount.value = res.data || 0
  } catch {}
}

const doGenerateAlerts = async () => {
  try {
    await ElMessageBox.confirm('将扫描所有成绩数据，自动检测连续两次下降超过15分的学生并生成预警记录，是否继续？', '确认', { type: 'warning' })
  } catch { return }
  generating.value = true
  try {
    const res = await generateAlerts()
    ElMessage.success(`预警生成完成，新增 ${res.data} 条预警记录`)
    await loadAlerts()
    await loadUnhandledCount()
  } finally { generating.value = false }
}

const openHandleDialog = (row) => {
  currentAlert.value = row
  handleRemark.value = ''
  handleDialogVisible.value = true
}

const confirmHandle = async () => {
  handling.value = true
  try {
    await handleAlert(currentAlert.value.id, handleRemark.value)
    ElMessage.success('已标记为已处理')
    handleDialogVisible.value = false
    await loadAlerts()
    await loadUnhandledCount()
  } finally { handling.value = false }
}

// ---------- 班级对比 ----------
const doCompare = async () => {
  const f = compareForm.value
  if (!f.classId1 || !f.classId2 || !f.courseId) { ElMessage.warning('请完整选择两个班级和科目'); return }
  if (f.classId1 === f.classId2) { ElMessage.warning('请选择两个不同的班级'); return }
  compareLoading.value = true
  try {
    const res = await classCompare(f.classId1, f.classId2, f.courseId)
    compareResult.value = res.data
    await nextTick()
    if (!compareBarChart) compareBarChart = echarts.init(compareBarRef.value)
    const c1 = compareResult.value.class1
    const c2 = compareResult.value.class2
    const n1 = className(f.classId1), n2 = className(f.classId2)
    compareBarChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      legend: { data: [n1, n2], top: 0 },
      grid: { left: 60, right: 30, top: 50, bottom: 40 },
      xAxis: { type: 'category', data: ['平均分', '中位数', '标准差', '及格率(%)'] },
      yAxis: { type: 'value', axisLabel: { color: '#606266' }, splitLine: { lineStyle: { type: 'dashed' } } },
      series: [
        { name: n1, type: 'bar', data: [Number(c1.avgScore), Number(c1.medianScore), Number(c1.stdDev), Number(c1.passRate)],
          itemStyle: { color: '#409EFF', borderRadius: [4,4,0,0] }, label: { show: true, position: 'top' } },
        { name: n2, type: 'bar', data: [Number(c2.avgScore), Number(c2.medianScore), Number(c2.stdDev), Number(c2.passRate)],
          itemStyle: { color: '#67C23A', borderRadius: [4,4,0,0] }, label: { show: true, position: 'top' } }
      ]
    }, true)
  } finally { compareLoading.value = false }
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)
  loadStats()
  const [sRes, cRes, clRes] = await Promise.all([listStudents(), listCourses(), listClasses()])
  students.value = sRes.data || []
  courses.value = cRes.data || []
  classes.value = clRes.data || []
  loadAlerts()
  loadUnhandledCount()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  avgChart?.dispose(); compareChart?.dispose(); trendChart?.dispose(); compareBarChart?.dispose()
})
</script>

<style scoped>
.analysis-row { display: flex; align-items: stretch; }
.analysis-row > .el-col { display: flex; }
.analysis-card { width: 100%; }
.analysis-card :deep(.el-card__body) { padding: 16px; height: 420px; box-sizing: border-box; }
.chart-container { width: 100%; height: 100%; }
.ranking-table { width: 100%; }
.ranking-table :deep(.el-table__body-wrapper) { max-height: 380px; overflow-y: auto; }
.compare-card :deep(.el-descriptions__label) { width: 110px; }
.stat-num { font-weight: bold; font-size: 16px; color: #303133; }
.stat-num.avg { color: #409EFF; }
.stat-num.std { color: #E6A23C; }
.stat-num.pass { color: #67C23A; }
.drop-amount { font-weight: bold; }
.drop-amount.level-yellow { color: #E6A23C; }
.drop-amount.level-orange { color: #FF9800; }
.drop-amount.level-red { color: #F56C6C; }
</style>
