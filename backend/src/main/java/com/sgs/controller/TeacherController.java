package com.sgs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sgs.common.Result;
import com.sgs.entity.Teacher;
import com.sgs.service.TeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Api(tags = "教师管理")
@RestController
@RequestMapping(value = "/api/teacher", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @ApiOperation("分页查询教师")
    @GetMapping("/page")
    public Result<Page<Teacher>> page(@RequestParam(defaultValue = "1") long current,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) String keyword) {
        return Result.success(teacherService.pageQuery(current, size, keyword));
    }

    @ApiOperation("根据ID查询教师")
    @GetMapping("/{id}")
    public Result<Teacher> getById(@PathVariable Long id) {
        return Result.success(teacherService.getById(id));
    }

    @ApiOperation("新增教师")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> save(@RequestBody Teacher teacher) {
        teacherService.save(teacher);
        return Result.success();
    }

    @ApiOperation("修改教师")
    @PutMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> update(@RequestBody Teacher teacher) {
        teacherService.updateById(teacher);
        return Result.success();
    }

    @ApiOperation("删除教师")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        teacherService.removeById(id);
        return Result.success();
    }

    @ApiOperation("教师总数")
    @GetMapping("/count")
    public Result<Long> count() {
        return Result.success(teacherService.count());
    }

    @ApiOperation("查询所有教师")
    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(teacherService.list());
    }
}
