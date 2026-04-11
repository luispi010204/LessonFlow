package zhaw.ch.lessonflow.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class QuizCreateDTO {

    private String lessonId;
    private int passPercent;
    private List<String> questions;
}