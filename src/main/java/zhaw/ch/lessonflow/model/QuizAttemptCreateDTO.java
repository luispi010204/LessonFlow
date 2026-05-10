package zhaw.ch.lessonflow.model;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class QuizAttemptCreateDTO {

    private String quizId;
    private String enrollmentId;
    private String lessonId;
    private List<Integer> selectedOptionIndexes;
}