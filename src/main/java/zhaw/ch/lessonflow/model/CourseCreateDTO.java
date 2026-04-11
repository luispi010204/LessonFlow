package zhaw.ch.lessonflow.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CourseCreateDTO {

    private String tutorUserId;
    private String title;
    private String description;
    private CourseStatus status;
}