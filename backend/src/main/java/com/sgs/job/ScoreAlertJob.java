package com.sgs.job;

import com.sgs.service.ScoreService;
import com.sgs.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScoreAlertJob implements Job {

    private final ScoreService scoreService;
    private final NotificationWebSocketHandler webSocketHandler;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("========== 开始执行成绩预警检测任务 ==========");

        try {
            int alertCount = scoreService.generateScoreAlerts(15.0);

            if (alertCount > 0) {
                String message = String.format("成绩预警检测完成，新生成 %d 条预警记录", alertCount);
                log.info(message);
                webSocketHandler.broadcastMessage("SCORE_ALERT_DETECTED", message);
            } else {
                log.info("成绩预警检测完成，无新增预警记录");
            }

            log.info("========== 成绩预警检测任务完成 ==========");

        } catch (Exception e) {
            log.error("成绩预警检测任务执行失败", e);
            throw new JobExecutionException("成绩预警检测任务执行失败: " + e.getMessage(), e);
        }
    }
}
