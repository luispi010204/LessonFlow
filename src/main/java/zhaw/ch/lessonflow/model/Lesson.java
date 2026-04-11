package zhaw.ch.lessonflow.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Document("lessons")
public class Lesson {

    @Id
    private String id;

    @NonNull
    private String courseId;

    @NonNull
    private int lessonNumber;

    @NonNull
    private String title;

    @NonNull
    private String material;

    @NonNull
    private String meetingLink;
}