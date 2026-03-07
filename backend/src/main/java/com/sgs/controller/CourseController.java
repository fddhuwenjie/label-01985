package com.sgs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sgs.common.Result;
import com.sgs.entity.Course;
import com.sgs.service.CourseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Api(tags = "课程管理")
@RestController
@RequestMapping(value = "/api/course", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @ApiOperation("分页查询课程")
    @GetMapping("/page")
    public Result<Page<Course>> page(@RequestParam(defaultValue = "1") long current,
                                     @RequestParam(defaultValue = "10") long size,
                                     @RequestParam(required = false) String keyword) {
        return Result.success(courseService.pageQuery(current, size, keyword));
    }

    @ApiOperation("根据ID查询课程")
    @GetMapping("/{id}")
    public Result<Course> getById(@PathVariable Long id) {
        return Result.success(courseService.getById(id));
    }

    @ApiOperation("新增课程")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> save(@RequestBody Course course) {
        courseService.save(course);
        return Result.success();
    }

    @ApiOperation("修改课程")
    @PutMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> update(@RequestBody Course course) {
        courseService.updateById(course);
        return Result.success();
    }

    @ApiOperation("删除课程")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return Result.success();
    }

    @ApiOperation("查询所有课程")
    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(courseService.list());
    }

    @ApiOperation("课程总数")
    @GetMapping("/count")
    public Result<Long> count() {
        return Result.success(courseService.count());
    }
}
