package zhaw.ch.lessonflow.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class LessonCreateDTO {

    private String courseId;
    private int lessonNumber;
    private String title;
    private String material;
    private String meetingLink;
}