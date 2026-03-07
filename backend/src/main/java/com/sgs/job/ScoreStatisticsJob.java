package com.sgs.job;

import com.sgs.entity.ScoreStatistics;
import com.sgs.mapper.ScoreStatisticsMapper;
import com.sgs.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 成绩统计定时任务
 * 定期分析各课程成绩数据，生成统计报告
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScoreStatisticsJob implements Job {

    private final ScoreStatisticsMapper scoreStatisticsMapper;
    private final NotificationWebSocketHandler webSocketHandler;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("========== 开始执行成绩统计分析任务 ==========");
        LocalDateTime statTime = LocalDateTime.now();

        try {
            List<Map<String, Object>> statsData = scoreStatisticsMapper.calculateStatisticsByCourse();

            if (statsData.isEmpty()) {
                log.info("暂无成绩数据需要统计");
                return;
            }

            int savedCount = 0;
            for (Map<String, Object> data : statsData) {
                ScoreStatistics stat = new ScoreStatistics();
                stat.setCourseId(((Number) data.get("courseId")).longValue());
                stat.setCourseName((String) data.get("courseName"));
                stat.setStatType("DAILY");
                stat.setStudentCount(((Number) data.get("studentCount")).intValue());
                stat.setAvgScore(new BigDecimal(data.get("avgScore").toString()));
                stat.setMaxScore(new BigDecimal(data.get("maxScore").toString()));
                stat.setMinScore(new BigDecimal(data.get("minScore").toString()));
                stat.setPassRate(new BigDecimal(data.get("passRate").toString()));
                stat.setExcellentRate(new BigDecimal(data.get("excellentRate").toString()));
                stat.setStatTime(statTime);
                stat.setCreateTime(LocalDateTime.now());

                scoreStatisticsMapper.insert(stat);
                savedCount++;

                log.info("课程[{}]统计完成: 人数={}, 平均分={}, 及格率={}%, 优秀率={}%",
                        stat.getCourseName(),
                        stat.getStudentCount(),
                        stat.getAvgScore(),
                        stat.getPassRate(),
                        stat.getExcellentRate());
            }

            String message = String.format("成绩统计分析完成，共统计 %d 门课程数据", savedCount);
            webSocketHandler.broadcastMessage("STATISTICS_COMPLETE", message);

            log.info("========== 成绩统计分析任务完成，共处理 {} 门课程 ==========", savedCount);

        } catch (Exception e) {
            log.error("成绩统计任务执行失败", e);
            throw new JobExecutionException("成绩统计任务执行失败: " + e.getMessage(), e);
        }
    }
}
