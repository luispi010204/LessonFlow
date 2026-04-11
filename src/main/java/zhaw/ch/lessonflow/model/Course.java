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
@Document("courses")
public class Course {

    @Id
    private String id;

    @NonNull
    private String tutorUserId;

    @NonNull
    private String title;

    @NonNull
    private String description;

    @NonNull
    private CourseStatus status;
}