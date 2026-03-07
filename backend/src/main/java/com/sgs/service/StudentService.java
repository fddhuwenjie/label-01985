package com.sgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sgs.entity.Student;
import com.sgs.mapper.StudentMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StudentService extends ServiceImpl<StudentMapper, Student> {

    public Page<Student> pageQuery(long current, long size, String keyword) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Student::getName, keyword)
                    .or().like(Student::getStudentNo, keyword);
        }
        wrapper.orderByDesc(Student::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }
}
