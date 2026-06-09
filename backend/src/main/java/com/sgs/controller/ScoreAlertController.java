package com.sgs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sgs.common.Result;
import com.sgs.entity.ScoreAlert;
import com.sgs.service.ScoreAlertService;
import com.sgs.service.ScoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "成绩预警管理")
@RestController
@RequestMapping(value = "/api/score-alert", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class ScoreAlertController {

    private final ScoreAlertService scoreAlertService;
    private final ScoreService scoreService;

    @ApiOperation("分页查询预警列表")
    @GetMapping("/page")
    public Result<Page<ScoreAlert>> page(@RequestParam(defaultValue = "1") long current,
                                         @RequestParam(defaultValue = "10") long size,
                                         @RequestParam(required = false) Integer status,
                                         @RequestParam(required = false) Long studentId) {
        return Result.success(scoreAlertService.pageQuery(current, size, status, studentId));
    }

    @ApiOperation("标记预警为已处理")
    @PutMapping("/{id}/process")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public Result<?> markAsProcessed(@PathVariable Long id) {
        boolean success = scoreAlertService.markAsProcessed(id);
        return success ? Result.success("标记成功") : Result.error("标记失败");
    }

    @ApiOperation("手动触发预警检测")
    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Integer> generateAlerts(@RequestParam(required = false) Double threshold) {
        int count = scoreService.generateScoreAlerts(threshold);
        return Result.success(count);
    }

    @ApiOperation("学生成绩趋势数据")
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> getStudentTrend(@RequestParam Long studentId,
                                                              @RequestParam(required = false) Long courseId,
                                                              @RequestParam(required = false) String courseName) {
        return Result.success(scoreService.getStudentScoreTrend(studentId, courseId, courseName));
    }

    @ApiOperation("班级成绩对比")
    @GetMapping("/class-comparison")
    public Result<List<Map<String, Object>>> getClassComparison(@RequestParam Long courseId,
                                                                @RequestParam Long class1Id,
                                                                @RequestParam Long class2Id) {
        return Result.success(scoreService.getClassScoreComparison(courseId, class1Id, class2Id));
    }
}
