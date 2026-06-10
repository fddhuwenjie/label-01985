package com.sgs.controller;

import com.sgs.common.Result;
import com.sgs.service.ScoreAlertService;
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

    @ApiOperation("预警列表查询")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String alertLevel,
            @RequestParam(required = false) String studentName) {
        return Result.success(scoreAlertService.getAlertList(status, alertLevel, studentName));
    }

    @ApiOperation("生成预警记录")
    @PostMapping("/generate")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public Result<Integer> generate(@RequestParam(defaultValue = "15.0") Double threshold) {
        int count = scoreAlertService.generateAlerts(threshold);
        return Result.success(count);
    }

    @ApiOperation("标记预警为已处理")
    @PutMapping("/handle/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public Result<?> handle(@PathVariable Long id,
                            @RequestBody(required = false) Map<String, String> body) {
        String remark = body != null ? body.get("handleRemark") : null;
        boolean success = scoreAlertService.handleAlert(id, remark);
        if (success) {
            return Result.success("处理成功");
        } else {
            return Result.error("预警记录不存在");
        }
    }

    @ApiOperation("未处理预警数量")
    @GetMapping("/unhandled-count")
    public Result<Long> unhandledCount() {
        return Result.success(scoreAlertService.getUnhandledCount());
    }
}
