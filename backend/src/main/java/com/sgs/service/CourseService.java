package com.sgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sgs.entity.Course;
import com.sgs.mapper.CourseMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CourseService extends ServiceImpl<CourseMapper, Course> {

    public Page<Course> pageQuery(long current, long size, String keyword) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Course::getCourseName, keyword)
                    .or().like(Course::getCourseNo, keyword);
        }
        wrapper.orderByDesc(Course::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }
}
