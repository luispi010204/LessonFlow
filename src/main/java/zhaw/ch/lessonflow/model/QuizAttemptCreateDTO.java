package zhaw.ch.lessonflow.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class QuizAttemptCreateDTO {

    private String quizId;
    private String enrollmentId;
    private String lessonId;
    private double scorePercent;
}