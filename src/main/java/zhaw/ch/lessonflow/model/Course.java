package zhaw.ch.lessonflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("courses")
public class Course {

    @Id
    private String id;
    private String tutorUserId;
    private String title;
    private String description;
    private CourseStatus status;
}
