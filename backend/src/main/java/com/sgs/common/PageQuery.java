package com.sgs.common;

import lombok.Data;

/**
 * 分页查询参数
 */
@Data
public class PageQuery {
    private long current = 1;
    private long size = 10;
    private String keyword;
}
