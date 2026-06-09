package com.sgs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sgs.entity.ScoreAlert;
import com.sgs.mapper.ScoreAlertMapper;
import com.sgs.mapper.ScoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScoreAlertService extends ServiceImpl<ScoreAlertMapper, ScoreAlert> {

    private final ScoreMapper scoreMapper;

    public int detectAndCreateAlerts() {
        List<Map<String, Object>> drops = scoreMapper.selectScoreDropAlerts();
        int count = 0;
        for (Map<String, Object> drop : drops) {
            ScoreAlert alert = new ScoreAlert();
            alert.setStudentId(((Number) drop.get("student_id")).longValue());
            alert.setStudentName((String) drop.get("studentName"));
            alert.setCourseId(((Number) drop.get("courseId")).longValue());
            alert.setCourseName((String) drop.get("courseName"));
            alert.setPreviousScore(new BigDecimal(drop.get("previousScore").toString()));
            alert.setCurrentScore(new BigDecimal(drop.get("currentScore").toString()));
            alert.setDropAmount(new BigDecimal(drop.get("dropAmount").toString()));
            alert.setSemester((String) drop.get("semester"));
            alert.setAlertLevel(determineAlertLevel(alert.getDropAmount()));
            alert.setStatus(0);
            save(alert);
            count++;
        }
        return count;
    }

    private String determineAlertLevel(BigDecimal dropAmount) {
        double drop = dropAmount.doubleValue();
        if (drop > 35) {
            return "URGENT";
        } else if (drop > 25) {
            return "SERIOUS";
        } else {
            return "WARNING";
        }
    }

    public Page<Map<String, Object>> alertPage(long current, long size, Integer status,
                                                 String alertLevel, Long studentId) {
        Page<Map<String, Object>> page = new Page<>(current, size);
        return baseMapper.selectAlertPage(page, status, alertLevel, studentId);
    }

    public boolean markAsProcessed(Long id) {
        return baseMapper.markAsProcessed(id) > 0;
    }
}
