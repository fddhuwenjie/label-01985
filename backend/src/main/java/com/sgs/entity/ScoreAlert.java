package com.sgs.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩预警记录
 */
@Data
@TableName("score_alert")
public class ScoreAlert {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;

    /** 上一次成绩 */
    private BigDecimal previousScore;
    /** 本次成绩 */
    private BigDecimal currentScore;
    /** 下降幅度（正数，单位：分） */
    private BigDecimal dropValue;
    /** 预警等级 LOW / MEDIUM / HIGH */
    private String alertLevel;

    private Long previousScoreId;
    private Long currentScoreId;

    /** 处理状态 0-未处理 1-已处理 */
    private Integer status;
    private LocalDateTime handleTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
