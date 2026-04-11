package zhaw.ch.lessonflow.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class EnrollmentCreateDTO {

    private String courseId;
    private String learnerUserId;
    private String status;
}