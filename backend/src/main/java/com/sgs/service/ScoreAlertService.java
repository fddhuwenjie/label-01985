package com.sgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sgs.entity.ScoreAlert;
import com.sgs.mapper.ScoreAlertMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScoreAlertService extends ServiceImpl<ScoreAlertMapper, ScoreAlert> {

    public Page<ScoreAlert> pageQuery(long current, long size, Integer status, Long studentId) {
        LambdaQueryWrapper<ScoreAlert> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ScoreAlert::getStatus, status);
        }
        if (studentId != null) {
            wrapper.eq(ScoreAlert::getStudentId, studentId);
        }
        wrapper.orderByDesc(ScoreAlert::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }

    public boolean markAsProcessed(Long id) {
        return baseMapper.markAsProcessed(id) > 0;
    }
}
