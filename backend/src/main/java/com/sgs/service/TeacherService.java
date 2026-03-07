package com.sgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sgs.entity.Teacher;
import com.sgs.mapper.TeacherMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TeacherService extends ServiceImpl<TeacherMapper, Teacher> {

    public Page<Teacher> pageQuery(long current, long size, String keyword) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Teacher::getName, keyword)
                    .or().like(Teacher::getTeacherNo, keyword);
        }
        wrapper.orderByDesc(Teacher::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }
}
