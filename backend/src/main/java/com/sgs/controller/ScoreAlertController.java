package com.sgs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sgs.common.Result;
import com.sgs.service.ScoreAlertService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "成绩预警")
@RestController
@RequestMapping(value = "/api/alert", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class ScoreAlertController {

    private final ScoreAlertService scoreAlertService;

    @ApiOperation("预警列表")
    @GetMapping("/page")
    public Result<Page<Map<String, Object>>> page(@RequestParam(defaultValue = "1") long current,
                                                    @RequestParam(defaultValue = "10") long size,
                                                    @RequestParam(required = false) Integer status,
                                                    @RequestParam(required = false) String alertLevel,
                                                    @RequestParam(required = false) Long studentId) {
        return Result.success(scoreAlertService.alertPage(current, size, status, alertLevel, studentId));
    }

    @ApiOperation("检测预警")
    @PostMapping("/detect")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public Result<?> detect() {
        int count = scoreAlertService.detectAndCreateAlerts();
        return Result.success("检测完成，新增预警记录 " + count + " 条");
    }

    @ApiOperation("标记已处理")
    @PutMapping("/{id}/process")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> process(@PathVariable Long id) {
        scoreAlertService.markAsProcessed(id);
        return Result.success();
    }
}
