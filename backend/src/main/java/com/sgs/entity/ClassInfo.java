package com.sgs.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("class_info")
public class ClassInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String className;
    private String grade;
    private String major;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
