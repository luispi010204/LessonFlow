package zhaw.ch.lessonflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("lessons")
public class Lesson {

    @Id
    private String id;
    private String courseId;
    private int lessonNumber;
    private String title;
    private String material;
    private String meetingLink;
}