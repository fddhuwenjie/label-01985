package com.sgs.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sgs.entity.Score;
import com.sgs.mapper.ScoreMapper;
import com.sgs.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScoreService extends ServiceImpl<ScoreMapper, Score> {

    private final NotificationWebSocketHandler webSocketHandler;

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

    public List<Map<String, Object>> getStudentScoreTrend(Long studentId, Long courseId) {
        return baseMapper.selectStudentScoreTrend(studentId, courseId);
    }

    public Map<String, Object> getClassComparison(Long classId1, Long classId2, Long courseId) {
        Map<String, Object> class1Stats = baseMapper.selectClassStats(classId1, courseId);
        Map<String, Object> class2Stats = baseMapper.selectClassStats(classId2, courseId);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("class1", class1Stats);
        result.put("class2", class2Stats);
        return result;
    }
}
