package zhaw.ch.lessonflow.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@Document("quiz_attempts")
public class QuizAttempt {

    @Id
    private String id;

    @NonNull
    private String quizId;

    @NonNull
    private String enrollmentId;

    @NonNull
    private String lessonId;

    @NonNull
    private double scorePercent;

    @NonNull
    private boolean passed;
}