package com.sgs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sgs.common.Result;
import com.sgs.entity.Student;
import com.sgs.service.StudentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Api(tags = "学生管理")
@RestController
@RequestMapping(value = "/api/student", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @ApiOperation("分页查询学生")
    @GetMapping("/page")
    public Result<Page<Student>> page(@RequestParam(defaultValue = "1") long current,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) String keyword) {
        return Result.success(studentService.pageQuery(current, size, keyword));
    }

    @ApiOperation("根据ID查询学生")
    @GetMapping("/{id}")
    public Result<Student> getById(@PathVariable Long id) {
        return Result.success(studentService.getById(id));
    }

    @ApiOperation("新增学生")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public Result<?> save(@RequestBody Student student) {
        studentService.save(student);
        return Result.success();
    }

    @ApiOperation("修改学生")
    @PutMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public Result<?> update(@RequestBody Student student) {
        studentService.updateById(student);
        return Result.success();
    }

    @ApiOperation("删除学生")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        studentService.removeById(id);
        return Result.success();
    }

    @ApiOperation("学生总数")
    @GetMapping("/count")
    public Result<Long> count() {
        return Result.success(studentService.count());
    }

    @ApiOperation("查询所有学生")
    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(studentService.list());
    }
}
