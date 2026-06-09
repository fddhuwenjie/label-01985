package com.sgs.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sgs.entity.Score;
import com.sgs.entity.ScoreAlert;
import com.sgs.mapper.ScoreMapper;
import com.sgs.mapper.ScoreAlertMapper;
import com.sgs.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreService extends ServiceImpl<ScoreMapper, Score> {

    private final NotificationWebSocketHandler webSocketHandler;
    private final ScoreAlertMapper scoreAlertMapper;

    public Page<Score> pageQuery(long current, long size, Long studentId, Long courseId, String semester) {
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(Score::getStudentId, studentId);
        }
        if (courseId != null) {
            wrapper.eq(Score::getCourseId, courseId);
        }
        if (semester != null && !semester.isEmpty()) {
            wrapper.eq(Score::getSemester, semester);
        }
        wrapper.orderByDesc(Score::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }

    public List<Map<String, Object>> getScoreStats(String semester) {
        return baseMapper.selectScoreStatsBySemester(semester);
    }

    public List<Map<String, Object>> getStudentRanking(String semester) {
        return baseMapper.selectStudentRanking(semester);
    }

    public void exportExcel(HttpServletResponse response, String semester) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("成绩表", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        if (semester != null && !semester.isEmpty()) {
            wrapper.eq(Score::getSemester, semester);
        }
        List<Score> list = list(wrapper);
        EasyExcel.write(response.getOutputStream(), Score.class).sheet("成绩").doWrite(list);
    }

    public void importExcel(MultipartFile file) throws IOException {
        List<Score> dataList = new ArrayList<>();
        EasyExcel.read(file.getInputStream(), Score.class, new AnalysisEventListener<Score>() {
            @Override
            public void invoke(Score data, AnalysisContext context) {
                dataList.add(data);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                saveBatch(dataList);
            }
        }).sheet().doRead();

        if (!dataList.isEmpty()) {
            webSocketHandler.broadcastMessage("SCORE_IMPORT", 
                String.format("批量导入完成，共导入 %d 条成绩记录", dataList.size()));
        }
    }

    /**
     * 发送成绩变更通知
     */
    public void notifyScoreChange(String type, Score score) {
        String message = String.format("成绩变更: 学生ID=%d, 课程ID=%d, 分数=%.1f",
                score.getStudentId(), score.getCourseId(), 
                score.getScore() != null ? score.getScore().doubleValue() : 0);
        webSocketHandler.broadcastMessage(type, message);
    }

    public List<Map<String, Object>> getStudentScoreTrend(Long studentId, Long courseId, String courseName) {
        return baseMapper.selectStudentScoreTrend(studentId, courseId, courseName);
    }

    public List<Map<String, Object>> getClassScoreComparison(Long courseId, Long class1Id, Long class2Id) {
        return baseMapper.selectClassScoreComparison(courseId, class1Id, class2Id);
    }

    @Transactional
    public int generateScoreAlerts(Double threshold) {
        if (threshold == null) {
            threshold = 15.0;
        }
        List<Map<String, Object>> dropList = baseMapper.selectScoreDropsForAlert(threshold);
        if (dropList.isEmpty()) {
            log.info("没有需要生成预警的成绩下降记录");
            return 0;
        }

        int count = 0;
        for (Map<String, Object> drop : dropList) {
            ScoreAlert alert = new ScoreAlert();
            alert.setStudentId(((Number) drop.get("studentId")).longValue());
            alert.setStudentName((String) drop.get("studentName"));
            alert.setCourseId(((Number) drop.get("courseId")).longValue());
            alert.setCourseName((String) drop.get("courseName"));
            alert.setPrevScore(new BigDecimal(drop.get("prevScore").toString()));
            alert.setCurrentScore(new BigDecimal(drop.get("currentScore").toString()));
            alert.setDropAmount(new BigDecimal(drop.get("dropAmount").toString()));

            BigDecimal dropAmount = new BigDecimal(drop.get("dropAmount").toString());
            if (dropAmount.compareTo(new BigDecimal("30")) >= 0) {
                alert.setAlertLevel("SEVERE");
            } else if (dropAmount.compareTo(new BigDecimal("20")) >= 0) {
                alert.setAlertLevel("MEDIUM");
            } else {
                alert.setAlertLevel("WARNING");
            }

            alert.setStatus(0);
            scoreAlertMapper.insert(alert);
            count++;

            log.info("生成成绩预警: 学生={}, 课程={}, 下降={}分, 等级={}",
                    alert.getStudentName(), alert.getCourseName(), alert.getDropAmount(), alert.getAlertLevel());
        }

        if (count > 0) {
            webSocketHandler.broadcastMessage("SCORE_ALERT",
                    String.format("新生成 %d 条成绩预警记录，请及时处理", count));
        }

        return count;
    }
}
