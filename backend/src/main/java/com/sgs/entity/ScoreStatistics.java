package com.sgs.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩统计分析结果实体
 */
@Data
@TableName("score_statistics")
public class ScoreStatistics {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程名称（冗余存储便于查询）
     */
    private String courseName;

    /**
     * 统计类型：DAILY-每日统计, WEEKLY-每周统计
     */
    private String statType;

    /**
     * 参与人数
     */
    private Integer studentCount;

    /**
     * 平均分
     */
    private BigDecimal avgScore;

    /**
     * 最高分
     */
    private BigDecimal maxScore;

    /**
     * 最低分
     */
    private BigDecimal minScore;

    /**
     * 及格率（百分比）
     */
    private BigDecimal passRate;

    /**
     * 优秀率（>=90分，百分比）
     */
    private BigDecimal excellentRate;

    /**
     * 统计时间
     */
    private LocalDateTime statTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
