package zhaw.ch.lessonflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("enrollments")
public class Enrollment {

    @Id
    private String id;
    private String courseId;
    private String learnerUserId;
    private String status;
}