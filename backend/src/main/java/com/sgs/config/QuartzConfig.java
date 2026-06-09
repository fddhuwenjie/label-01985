package com.sgs.config;

import com.sgs.job.ScoreAlertJob;
import com.sgs.job.ScoreStatisticsJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz 定时任务配置
 */
@Configuration
public class QuartzConfig {

    /**
     * 成绩统计任务详情
     */
    @Bean
    public JobDetail scoreStatisticsJobDetail() {
        return JobBuilder.newJob(ScoreStatisticsJob.class)
                .withIdentity("scoreStatisticsJob", "statisticsGroup")
                .withDescription("每日成绩统计分析任务")
                .storeDurably()
                .build();
    }

    /**
     * 成绩统计触发器 - 每天凌晨2点执行
     */
    @Bean
    public Trigger scoreStatisticsTrigger(JobDetail scoreStatisticsJobDetail) {
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
                .cronSchedule("0 0 2 * * ?")
                .withMisfireHandlingInstructionDoNothing();

        return TriggerBuilder.newTrigger()
                .forJob(scoreStatisticsJobDetail)
                .withIdentity("scoreStatisticsTrigger", "statisticsGroup")
                .withDescription("每日凌晨2点执行成绩统计")
                .withSchedule(scheduleBuilder)
                .build();
    }

    /**
     * 演示用触发器 - 每5分钟执行一次（便于测试）
     */
    @Bean
    public Trigger scoreStatisticsDemoTrigger(JobDetail scoreStatisticsJobDetail) {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInMinutes(5)
                .repeatForever();

        return TriggerBuilder.newTrigger()
                .forJob(scoreStatisticsJobDetail)
                .withIdentity("scoreStatisticsDemoTrigger", "statisticsGroup")
                .withDescription("每5分钟执行一次成绩统计（演示用）")
                .withSchedule(scheduleBuilder)
                .startNow()
                .build();
    }

    /**
     * 成绩预警任务详情
     */
    @Bean
    public JobDetail scoreAlertJobDetail() {
        return JobBuilder.newJob(ScoreAlertJob.class)
                .withIdentity("scoreAlertJob", "alertGroup")
                .withDescription("成绩预警检测任务")
                .storeDurably()
                .build();
    }

    /**
     * 成绩预警触发器 - 每天凌晨3点执行
     */
    @Bean
    public Trigger scoreAlertTrigger(JobDetail scoreAlertJobDetail) {
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
                .cronSchedule("0 0 3 * * ?")
                .withMisfireHandlingInstructionDoNothing();

        return TriggerBuilder.newTrigger()
                .forJob(scoreAlertJobDetail)
                .withIdentity("scoreAlertTrigger", "alertGroup")
                .withDescription("每日凌晨3点执行成绩预警检测")
                .withSchedule(scheduleBuilder)
                .build();
    }

    /**
     * 成绩预警演示用触发器 - 每10分钟执行一次（便于测试）
     */
    @Bean
    public Trigger scoreAlertDemoTrigger(JobDetail scoreAlertJobDetail) {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInMinutes(10)
                .repeatForever();

        return TriggerBuilder.newTrigger()
                .forJob(scoreAlertJobDetail)
                .withIdentity("scoreAlertDemoTrigger", "alertGroup")
                .withDescription("每10分钟执行一次成绩预警检测（演示用）")
                .withSchedule(scheduleBuilder)
                .startNow()
                .build();
    }
}
