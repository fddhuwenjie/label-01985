package com.sgs.controller;

import com.sgs.common.Result;
import com.sgs.entity.ClassInfo;
import com.sgs.service.ClassInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "班级管理")
@RestController
@RequestMapping(value = "/api/class", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class ClassInfoController {

    private final ClassInfoService classInfoService;

    @ApiOperation("查询所有班级")
    @GetMapping("/list")
    public Result<List<ClassInfo>> list() {
        return Result.success(classInfoService.list());
    }
}
