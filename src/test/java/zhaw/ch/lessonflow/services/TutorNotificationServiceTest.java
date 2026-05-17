package zhaw.ch.lessonflow.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import zhaw.ch.lessonflow.model.Course;
import zhaw.ch.lessonflow.model.CourseStatus;
import zhaw.ch.lessonflow.model.Enrollment;
import zhaw.ch.lessonflow.model.Lesson;
import zhaw.ch.lessonflow.model.LessonProgress;
import zhaw.ch.lessonflow.model.LessonProgressState;
import zhaw.ch.lessonflow.model.Mail;
import zhaw.ch.lessonflow.repository.CourseRepository;

@ExtendWith(MockitoExtension.class)
public class TutorNotificationServiceTest {

    @Mock
    CourseRepository courseRepository;

    @Mock
    EnrollmentService enrollmentService;

    @Mock
    LessonService lessonService;

    @Mock
    MailService mailService;

    @InjectMocks
    TutorNotificationService tutorNotificationService;

    LessonProgress progress;
    Lesson lesson;
    Enrollment enrollment;
    Course course;

    @BeforeEach
    void setUp() {
        progress = new LessonProgress(
                "enrollment-1",
                "lesson-1",
                LessonProgressState.MATERIAL_DONE,
                false,
                0);
        ReflectionTestUtils.setField(progress, "id", "progress-1");

        lesson = new Lesson(
                "course-1",
                1,
                "JavaScript Functions",
                "Functions allow reusable code blocks.",
                "https://meeting.example.com");
        ReflectionTestUtils.setField(lesson, "id", "lesson-1");

        enrollment = new Enrollment(
                "course-1",
                "auth0|learner-1",
                "ENROLLED");
        ReflectionTestUtils.setField(enrollment, "id", "enrollment-1");

        course = new Course(
                "auth0|tutor-1",
                "JavaScript Basics",
                "Learn the basics of JavaScript.",
                CourseStatus.PUBLISHED);
        ReflectionTestUtils.setField(course, "id", "course-1");
        course.setTutorEmail("tutor@lessonflow.com");
    }

    @Test
    void shouldNotifyTutorWhenMaterialIsDone() {
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(mailService.sendMail(any(Mail.class))).thenReturn(true);

        boolean result = tutorNotificationService.notifyTutorMaterialDone(
                progress,
                "learner@lessonflow.com");

        assertTrue(result);

        verify(mailService).sendMail(argThat(mail ->
                mail.getTo().equals("tutor@lessonflow.com")
                        && mail.getSubject().contains("LessonFlow")
                        && mail.getMessage().contains("JavaScript Basics")
                        && mail.getMessage().contains("JavaScript Functions")
                        && mail.getMessage().contains("learner@lessonflow.com")
                        && mail.getMessage().contains("enrollment-1")
        ));
    }

    @Test
    void shouldReturnFalseWhenProgressIsNull() {
        boolean result = tutorNotificationService.notifyTutorMaterialDone(
                null,
                "learner@lessonflow.com");

        assertFalse(result);

        verify(mailService, never()).sendMail(any(Mail.class));
    }

    @Test
    void shouldReturnFalseWhenLessonDoesNotExist() {
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.empty());

        boolean result = tutorNotificationService.notifyTutorMaterialDone(
                progress,
                "learner@lessonflow.com");

        assertFalse(result);

        verify(mailService, never()).sendMail(any(Mail.class));
    }

    @Test
    void shouldReturnFalseWhenEnrollmentDoesNotExist() {
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.empty());

        boolean result = tutorNotificationService.notifyTutorMaterialDone(
                progress,
                "learner@lessonflow.com");

        assertFalse(result);

        verify(mailService, never()).sendMail(any(Mail.class));
    }

    @Test
    void shouldReturnFalseWhenCourseDoesNotExist() {
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(courseRepository.findById("course-1")).thenReturn(Optional.empty());

        boolean result = tutorNotificationService.notifyTutorMaterialDone(
                progress,
                "learner@lessonflow.com");

        assertFalse(result);

        verify(mailService, never()).sendMail(any(Mail.class));
    }

    @Test
    void shouldReturnFalseWhenTutorEmailIsMissing() {
        course.setTutorEmail("");

        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));

        boolean result = tutorNotificationService.notifyTutorMaterialDone(
                progress,
                "learner@lessonflow.com");

        assertFalse(result);

        verify(mailService, never()).sendMail(any(Mail.class));
    }
}