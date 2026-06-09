package com.sgs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sgs.common.Result;
import com.sgs.entity.ScoreAlert;
import com.sgs.service.ScoreAnalysisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 成绩趋势分析与预警接口
 */
@Api(tags = "成绩趋势分析与预警")
@RestController
@RequestMapping(value = "/api/score-analysis", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class ScoreAnalysisController {

    private final ScoreAnalysisService scoreAnalysisService;

    @ApiOperation("学生成绩趋势数据（支持按科目筛选）")
    @GetMapping("/trend")
    public Result<Map<String, Object>> trend(@RequestParam Long studentId,
                                             @RequestParam(required = false) Long courseId) {
        return Result.success(scoreAnalysisService.getScoreTrend(studentId, courseId));
    }

    @ApiOperation("预警列表（分页）")
    @GetMapping("/alert/page")
    public Result<Page<ScoreAlert>> pageAlerts(@RequestParam(defaultValue = "1") long current,
                                               @RequestParam(defaultValue = "10") long size,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(required = false) String alertLevel) {
        return Result.success(scoreAnalysisService.pageAlerts(current, size, status, alertLevel));
    }

    @ApiOperation("扫描成绩并生成预警记录")
    @PostMapping("/alert/generate")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public Result<Integer> generateAlerts() {
        return Result.success(scoreAnalysisService.generateAlerts());
    }

    @ApiOperation("标记预警为已处理")
    @PutMapping("/alert/{id}/handle")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> markHandled(@PathVariable Long id) {
        scoreAnalysisService.markHandled(id);
        return Result.success();
    }

    @ApiOperation("两个班级在某科目上的成绩对比")
    @GetMapping("/class-compare")
    public Result<Map<String, Object>> compareClasses(@RequestParam Long classAId,
                                                      @RequestParam Long classBId,
                                                      @RequestParam Long courseId) {
        return Result.success(scoreAnalysisService.compareClasses(classAId, classBId, courseId));
    }
}
