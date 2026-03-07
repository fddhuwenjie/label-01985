package com.sgs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sgs.common.Result;
import com.sgs.entity.Score;
import com.sgs.service.ScoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Api(tags = "成绩管理")
@RestController
@RequestMapping(value = "/api/score", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    @ApiOperation("分页查询成绩")
    @GetMapping("/page")
    public Result<Page<Score>> page(@RequestParam(defaultValue = "1") long current,
                                    @RequestParam(defaultValue = "10") long size,
                                    @RequestParam(required = false) Long studentId,
                                    @RequestParam(required = false) Long courseId,
                                    @RequestParam(required = false) String semester) {
        return Result.success(scoreService.pageQuery(current, size, studentId, courseId, semester));
    }

    @ApiOperation("新增成绩")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public Result<?> save(@RequestBody Score score) {
        scoreService.save(score);
        scoreService.notifyScoreChange("SCORE_CREATED", score);
        return Result.success();
    }

    @ApiOperation("修改成绩")
    @PutMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public Result<?> update(@RequestBody Score score) {
        scoreService.updateById(score);
        scoreService.notifyScoreChange("SCORE_UPDATE", score);
        return Result.success();
    }

    @ApiOperation("删除成绩")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        scoreService.removeById(id);
        return Result.success();
    }

    @ApiOperation("成绩统计")
    @GetMapping("/stats")
    public Result<List<Map<String, Object>>> stats(@RequestParam String semester) {
        return Result.success(scoreService.getScoreStats(semester));
    }

    @ApiOperation("学生排名")
    @GetMapping("/ranking")
    public Result<List<Map<String, Object>>> ranking(@RequestParam String semester) {
        return Result.success(scoreService.getStudentRanking(semester));
    }

    @ApiOperation("导出成绩Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public void exportExcel(HttpServletResponse response,
                            @RequestParam(required = false) String semester) throws IOException {
        scoreService.exportExcel(response, semester);
    }

    @ApiOperation("导入成绩Excel")
    @PostMapping("/import")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public Result<?> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        scoreService.importExcel(file);
        return Result.success("导入成功");
    }

    @ApiOperation("成绩总数")
    @GetMapping("/count")
    public Result<Long> count() {
        return Result.success(scoreService.count());
    }
}
