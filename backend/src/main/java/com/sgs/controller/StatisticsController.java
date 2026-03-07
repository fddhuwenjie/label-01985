package com.sgs.controller;

import com.sgs.common.Result;
import com.sgs.entity.ScoreStatistics;
import com.sgs.job.ScoreStatisticsJob;
import com.sgs.mapper.ScoreStatisticsMapper;
import com.sgs.websocket.NotificationWebSocketHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 成绩统计分析接口
 */
@Api(tags = "成绩统计分析")
@RestController
@RequestMapping(value = "/api/statistics", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class StatisticsController {

    private final ScoreStatisticsMapper scoreStatisticsMapper;
    private final ScoreStatisticsJob scoreStatisticsJob;
    private final NotificationWebSocketHandler webSocketHandler;

    @ApiOperation("获取最新统计结果")
    @GetMapping("/latest")
    public Result<List<ScoreStatistics>> getLatestStatistics(
            @RequestParam(defaultValue = "DAILY") String statType) {
        return Result.success(scoreStatisticsMapper.getLatestStatistics(statType));
    }

    @ApiOperation("实时计算统计数据")
    @GetMapping("/realtime")
    public Result<List<Map<String, Object>>> getRealtimeStatistics() {
        return Result.success(scoreStatisticsMapper.calculateStatisticsByCourse());
    }

    @ApiOperation("手动触发统计任务")
    @PostMapping("/trigger")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> triggerStatistics() throws JobExecutionException {
        scoreStatisticsJob.execute(null);
        return Result.success("统计任务已执行完成");
    }

    @ApiOperation("获取WebSocket在线人数")
    @GetMapping("/online-count")
    public Result<Integer> getOnlineCount() {
        return Result.success(webSocketHandler.getOnlineCount());
    }
}
