package com.sgs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sgs.common.Result;
import com.sgs.entity.ClassInfo;
import com.sgs.entity.Course;
import com.sgs.entity.ScoreAlert;
import com.sgs.entity.Student;
import com.sgs.mapper.ClassInfoMapper;
import com.sgs.mapper.CourseMapper;
import com.sgs.mapper.StudentMapper;
import com.sgs.service.ScoreAlertService;
import com.sgs.service.ScoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "成绩趋势分析与预警")
@RestController
@RequestMapping(value = "/api/analysis", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class AnalysisController {

    private final ScoreService scoreService;
    private final ScoreAlertService scoreAlertService;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final ClassInfoMapper classInfoMapper;

    @ApiOperation("获取学生成绩趋势数据（折线图）")
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> getTrend(@RequestParam Long studentId,
                                                      @RequestParam(required = false) Long courseId) {
        return Result.success(scoreService.getStudentScoreTrend(studentId, courseId));
    }

    @ApiOperation("班级成绩对比")
    @GetMapping("/class-compare")
    public Result<Map<String, Object>> classCompare(@RequestParam Long classId1,
                                                    @RequestParam Long classId2,
                                                    @RequestParam Long courseId) {
        return Result.success(scoreService.compareClasses(classId1, classId2, courseId));
    }

    @ApiOperation("手动触发预警生成")
    @PostMapping("/alerts/generate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Integer> generateAlerts() {
        int count = scoreAlertService.generateAlerts();
        return Result.success(count);
    }

    @ApiOperation("预警记录分页查询")
    @GetMapping("/alerts/page")
    public Result<IPage<ScoreAlert>> alertPage(@RequestParam(defaultValue = "1") long current,
                                              @RequestParam(defaultValue = "10") long size,
                                              @RequestParam(required = false) String studentName,
                                              @RequestParam(required = false) String courseName,
                                              @RequestParam(required = false) Integer status) {
        return Result.success(scoreAlertService.pageQuery(current, size, studentName, courseName, status));
    }

    @ApiOperation("标记预警为已处理")
    @PutMapping("/alerts/{id}/handle")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public Result<?> handleAlert(@PathVariable Long id,
                                 @RequestParam(required = false) String remark) {
        boolean ok = scoreAlertService.markAsHandled(id, remark);
        return ok ? Result.success() : Result.error("预警记录不存在");
    }

    @ApiOperation("未处理预警数量")
    @GetMapping("/alerts/unhandled-count")
    public Result<Long> unhandledCount() {
        return Result.success(scoreAlertService.countUnhandled());
    }

    @ApiOperation("获取所有学生（用于选择）")
    @GetMapping("/students")
    public Result<List<Student>> listStudents() {
        return Result.success(studentMapper.selectList(null));
    }

    @ApiOperation("获取所有课程（用于选择）")
    @GetMapping("/courses")
    public Result<List<Course>> listCourses() {
        return Result.success(courseMapper.selectList(null));
    }

    @ApiOperation("获取所有班级（用于选择）")
    @GetMapping("/classes")
    public Result<List<ClassInfo>> listClasses() {
        return Result.success(classInfoMapper.selectList(null));
    }
}
