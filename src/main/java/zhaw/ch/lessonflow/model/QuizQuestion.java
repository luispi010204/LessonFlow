package zhaw.ch.lessonflow.model;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class QuizQuestion {

    @NonNull
    private String questionText;

    @NonNull
    private List<String> options;

    @NonNull
    private int correctOptionIndex;
}