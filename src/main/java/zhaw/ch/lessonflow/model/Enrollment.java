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
@Document("enrollments")
public class Enrollment {

    @Id
    private String id;

    @NonNull
    private String courseId;

    @NonNull
    private String learnerUserId;

    @NonNull
    private String status;
}