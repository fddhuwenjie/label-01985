package com.sgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sgs.common.BusinessException;
import com.sgs.entity.ScoreAlert;
import com.sgs.mapper.ScoreAlertMapper;
import com.sgs.mapper.ScoreMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 成绩趋势分析与预警服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreAnalysisService extends ServiceImpl<ScoreAlertMapper, ScoreAlert> {

    /** 成绩下降预警阈值（分），超过该值触发预警 */
    public static final BigDecimal ALERT_THRESHOLD = new BigDecimal("15");

    private final ScoreMapper scoreMapper;

    /**
     * 学生成绩趋势数据（横轴=考试时间，纵轴=分数，可按 courseId 过滤）。
     * 返回 ECharts 友好的结构：xAxis(时间)/series(分数) + 原始记录。
     */
    public Map<String, Object> getScoreTrend(Long studentId, Long courseId) {
        List<Map<String, Object>> records = scoreMapper.selectScoreTrend(studentId, courseId);

        // 仅做数据装配（无统计循环计算）
        Map<String, List<Map<String, Object>>> bySubject = new LinkedHashMap<>();
        for (Map<String, Object> row : records) {
            String name = (String) row.get("courseName");
            bySubject.computeIfAbsent(name, k -> new java.util.ArrayList<>()).add(row);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("studentId", studentId);
        result.put("courseId", courseId);
        result.put("records", records);
        result.put("groupedBySubject", bySubject);
        return result;
    }

    /**
     * 扫描成绩数据并生成预警记录。
     * 成绩下降幅度的判定在 SQL 中完成（窗口函数 LAG），Java 层只做幂等保存。
     *
     * @return 新增的预警记录数
     */
    @Transactional(rollbackFor = Exception.class)
    public int generateAlerts() {
        List<Map<String, Object>> drops = scoreMapper.detectScoreDrops(ALERT_THRESHOLD);
        int created = 0;
        for (Map<String, Object> row : drops) {
            Long previousScoreId = ((Number) row.get("previousScoreId")).longValue();
            Long currentScoreId = ((Number) row.get("currentScoreId")).longValue();
            Long studentId = ((Number) row.get("studentId")).longValue();
            Long courseId = ((Number) row.get("courseId")).longValue();

            // 幂等：同一对成绩只生成一条预警
            Long exists = baseMapper.selectCount(new LambdaQueryWrapper<ScoreAlert>()
                    .eq(ScoreAlert::getStudentId, studentId)
                    .eq(ScoreAlert::getCourseId, courseId)
                    .eq(ScoreAlert::getPreviousScoreId, previousScoreId)
                    .eq(ScoreAlert::getCurrentScoreId, currentScoreId));
            if (exists != null && exists > 0) {
                continue;
            }

            ScoreAlert alert = new ScoreAlert();
            alert.setStudentId(studentId);
            alert.setStudentName((String) row.get("studentName"));
            alert.setCourseId(courseId);
            alert.setCourseName((String) row.get("courseName"));
            alert.setPreviousScore(toBigDecimal(row.get("previousScore")));
            alert.setCurrentScore(toBigDecimal(row.get("currentScore")));
            alert.setDropValue(toBigDecimal(row.get("dropValue")));
            alert.setAlertLevel((String) row.get("alertLevel"));
            alert.setPreviousScoreId(previousScoreId);
            alert.setCurrentScoreId(currentScoreId);
            alert.setStatus(0);
            save(alert);
            created++;
        }
        log.info("成绩预警扫描完成，新增预警 {} 条", created);
        return created;
    }

    /**
     * 分页查询预警列表
     */
    public Page<ScoreAlert> pageAlerts(long current, long size, Integer status, String alertLevel) {
        LambdaQueryWrapper<ScoreAlert> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ScoreAlert::getStatus, status);
        }
        if (alertLevel != null && !alertLevel.isEmpty()) {
            wrapper.eq(ScoreAlert::getAlertLevel, alertLevel);
        }
        wrapper.orderByDesc(ScoreAlert::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }

    /**
     * 标记预警为已处理
     */
    public void markHandled(Long id) {
        ScoreAlert alert = getById(id);
        if (alert == null) {
            throw new BusinessException("预警记录不存在");
        }
        update(new LambdaUpdateWrapper<ScoreAlert>()
                .eq(ScoreAlert::getId, id)
                .set(ScoreAlert::getStatus, 1)
                .set(ScoreAlert::getHandleTime, LocalDateTime.now()));
    }

    /**
     * 班级成绩对比：两个班级 + 一个科目，返回平均分、中位数、标准差、及格率。
     * 全部统计在 SQL 层完成（XML Mapper），Java 仅做装配。
     */
    public Map<String, Object> compareClasses(Long classAId, Long classBId, Long courseId) {
        Map<String, Object> statsA = scoreMapper.selectClassStats(classAId, courseId);
        Map<String, Object> statsB = scoreMapper.selectClassStats(classBId, courseId);

        Map<String, Object> result = new HashMap<>();
        result.put("courseId", courseId);
        result.put("classA", buildClassResult(classAId, statsA));
        result.put("classB", buildClassResult(classBId, statsB));
        return result;
    }

    private Map<String, Object> buildClassResult(Long classId, Map<String, Object> stats) {
        Map<String, Object> map = new HashMap<>();
        map.put("classId", classId);
        if (stats == null) {
            map.put("studentCount", 0);
            map.put("avgScore", null);
            map.put("medianScore", null);
            map.put("stdDev", null);
            map.put("passRate", null);
        } else {
            map.put("studentCount", stats.get("studentCount"));
            map.put("avgScore", stats.get("avgScore"));
            map.put("medianScore", stats.get("medianScore"));
            map.put("stdDev", stats.get("stdDev"));
            map.put("passRate", stats.get("passRate"));
        }
        return map;
    }

    private BigDecimal toBigDecimal(Object o) {
        if (o == null) return null;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        return new BigDecimal(o.toString());
    }
}
