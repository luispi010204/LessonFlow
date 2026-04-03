package zhaw.ch.lessonflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("quiz_attempts")
public class QuizAttempt {

    @Id
    private String id;
    private String quizId;
    private String enrollmentId;
    private String lessonId;
    private double scorePercent;
    private boolean passed;
}