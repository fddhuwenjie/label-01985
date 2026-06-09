package com.sgs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    public IPage<ScoreAlert> pageQuery(long current, long size, String studentName, String courseName, Integer status) {
        Page<ScoreAlert> page = new Page<>(current, size);
        return baseMapper.selectAlertPage(page, studentName, courseName, status);
    }

    @Transactional
    public int generateAlerts() {
        List<Map<String, Object>> drops = scoreMapper.detectScoreDrops();
        int count = 0;
        for (Map<String, Object> drop : drops) {
            Long studentId = ((Number) drop.get("studentId")).longValue();
            Long courseId = ((Number) drop.get("courseId")).longValue();
            String studentName = (String) drop.get("studentName");
            String courseName = (String) drop.get("courseName");
            BigDecimal previousScore = new BigDecimal(drop.get("previousScore").toString());
            BigDecimal currentScore = new BigDecimal(drop.get("currentScore").toString());
            BigDecimal dropAmount = new BigDecimal(drop.get("dropAmount").toString());
            String alertLevel = (String) drop.get("alertLevel");

            LambdaQueryWrapper<ScoreAlert> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ScoreAlert::getStudentId, studentId)
                   .eq(ScoreAlert::getCourseId, courseId)
                   .eq(ScoreAlert::getStatus, 0);
            if (count(wrapper) > 0) {
                continue;
            }

            ScoreAlert alert = new ScoreAlert();
            alert.setStudentId(studentId);
            alert.setStudentName(studentName);
            alert.setCourseId(courseId);
            alert.setCourseName(courseName);
            alert.setPreviousScore(previousScore);
            alert.setCurrentScore(currentScore);
            alert.setDropAmount(dropAmount);
            alert.setAlertLevel(alertLevel);
            alert.setStatus(0);
            alert.setRemark("连续两次考试下降超过15分");
            save(alert);
            count++;
        }
        return count;
    }

    public boolean markAsHandled(Long id, String remark) {
        ScoreAlert alert = getById(id);
        if (alert == null) {
            return false;
        }
        alert.setStatus(1);
        if (remark != null && !remark.isEmpty()) {
            alert.setRemark(remark);
        }
        return updateById(alert);
    }

    public long countUnhandled() {
        LambdaQueryWrapper<ScoreAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreAlert::getStatus, 0);
        return count(wrapper);
    }
}
