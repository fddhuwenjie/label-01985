package com.sgs.controller;

import com.sgs.common.Result;
import com.sgs.entity.ClassInfo;
import com.sgs.mapper.ClassInfoMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "班级管理")
@RestController
@RequestMapping(value = "/api/class", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class ClassController {

    private final ClassInfoMapper classInfoMapper;

    @ApiOperation("查询所有班级")
    @GetMapping("/list")
    public Result<List<ClassInfo>> list() {
        return Result.success(classInfoMapper.selectList(null));
    }

    @ApiOperation("根据ID查询班级")
    @GetMapping("/{id}")
    public Result<ClassInfo> getById(@PathVariable Long id) {
        return Result.success(classInfoMapper.selectById(id));
    }
}
