package zhaw.ch.lessonflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("quizzes")
public class Quiz {

    @Id
    private String id;
    private String lessonId;
    private int passPercent;
    private List<String> questions;
}