package com.sgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sgs.entity.ScoreAlert;
import com.sgs.mapper.ScoreAlertMapper;
import com.sgs.mapper.ScoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScoreAlertService extends ServiceImpl<ScoreAlertMapper, ScoreAlert> {

    private final ScoreMapper scoreMapper;

    public List<Map<String, Object>> getAlertList(Integer status, String alertLevel, String studentName) {
        return baseMapper.selectAlertList(status, alertLevel, studentName);
    }

    @Transactional(rollbackFor = Exception.class)
    public int generateAlerts(Double threshold) {
        if (threshold == null) {
            threshold = 15.0;
        }
        List<Map<String, Object>> drops = scoreMapper.selectConsecutiveScoreDrops(threshold);
        int count = 0;
        for (Map<String, Object> drop : drops) {
            ScoreAlert alert = new ScoreAlert();
            alert.setStudentId(((Number) drop.get("studentId")).longValue());
            alert.setStudentName((String) drop.get("studentName"));
            alert.setCourseId(((Number) drop.get("courseId")).longValue());
            alert.setCourseName((String) drop.get("courseName"));
            alert.setPrevScore(BigDecimal.valueOf(((Number) drop.get("prevScore")).doubleValue()));
            alert.setCurrScore(BigDecimal.valueOf(((Number) drop.get("currScore")).doubleValue()));
            alert.setDropScore(BigDecimal.valueOf(((Number) drop.get("dropScore")).doubleValue()));
            alert.setAlertLevel((String) drop.get("alertLevel"));
            alert.setSemester((String) drop.get("semester"));
            alert.setStatus(0);
            save(alert);
            count++;
        }
        return count;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean handleAlert(Long id, String handleRemark) {
        ScoreAlert alert = getById(id);
        if (alert == null) {
            return false;
        }
        alert.setStatus(1);
        alert.setHandleRemark(handleRemark);
        return updateById(alert);
    }

    public long getUnhandledCount() {
        LambdaQueryWrapper<ScoreAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreAlert::getStatus, 0);
        return count(wrapper);
    }
}
