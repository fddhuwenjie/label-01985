package com.sgs.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("score_alert")
public class ScoreAlert {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private BigDecimal previousScore;
    private BigDecimal currentScore;
    private BigDecimal dropAmount;
    private String alertLevel;
    private String semester;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
