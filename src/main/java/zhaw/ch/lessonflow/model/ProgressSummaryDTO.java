package zhaw.ch.lessonflow.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProgressSummaryDTO {
    private int totalLessons;
    private int passedLessons;
    private String currentLessonId;
}