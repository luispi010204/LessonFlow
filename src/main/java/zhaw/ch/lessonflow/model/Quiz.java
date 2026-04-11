package zhaw.ch.lessonflow.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Document("quizzes")
public class Quiz {

    @Id
    private String id;

    @NonNull
    private String lessonId;

    @NonNull
    private int passPercent;

    @NonNull
    private List<String> questions;
}