package zhaw.ch.lessonflow.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import zhaw.ch.lessonflow.model.Enrollment;
import zhaw.ch.lessonflow.model.Lesson;
import zhaw.ch.lessonflow.model.LessonProgress;
import zhaw.ch.lessonflow.repository.LessonProgressRepository;
import zhaw.ch.lessonflow.services.CourseService;
import zhaw.ch.lessonflow.services.EnrollmentService;
import zhaw.ch.lessonflow.services.LessonProgressService;
import zhaw.ch.lessonflow.services.LessonService;
import zhaw.ch.lessonflow.services.TutorNotificationService;
import zhaw.ch.lessonflow.services.UserService;
import zhaw.ch.lessonflow.model.LessonProgressState;

@ExtendWith(MockitoExtension.class)
public class LessonProgressControllerTest {

    @Mock
    LessonProgressRepository lessonProgressRepository;

    @Mock
    LessonProgressService lessonProgressService;

    @Mock
    EnrollmentService enrollmentService;

    @Mock
    CourseService courseService;

    @Mock
    LessonService lessonService;

    @Mock
    UserService userService;

    @Mock
    TutorNotificationService tutorNotificationService;

    @InjectMocks
    LessonProgressController lessonProgressController;

    Enrollment enrollment;
    Lesson lesson;
    LessonProgress lessonProgress;

    @BeforeEach
    void setUp() {
        enrollment = new Enrollment(
                "course-1",
                "auth0|learner-1",
                "ENROLLED");
        ReflectionTestUtils.setField(enrollment, "id", "enrollment-1");

        lesson = new Lesson(
                "course-1",
                1,
                "Understanding Rhythm",
                "Learn about beats, tempo and note values.",
                "https://meeting.example.com");
        ReflectionTestUtils.setField(lesson, "id", "lesson-1");

        lessonProgress = new LessonProgress(
                "enrollment-1",
                "lesson-1",
                LessonProgressState.UNLOCKED,
                false,
                0);
        ReflectionTestUtils.setField(lessonProgress, "id", "progress-1");
    }

    @Test
    void shouldReturnLessonProgressByIdWhenLearnerOwnsEnrollment() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(lessonProgress));
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-1");
        when(userService.userHasRole("learner")).thenReturn(true);

        ResponseEntity<LessonProgress> response = lessonProgressController.getLessonProgressById("progress-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("progress-1", response.getBody().getId());
        assertEquals("enrollment-1", response.getBody().getEnrollmentId());
        assertEquals("lesson-1", response.getBody().getLessonId());
        assertEquals(LessonProgressState.UNLOCKED, response.getBody().getState());
    }

    @Test
    void shouldReturnLessonProgressByIdWhenTutorOwnsCourse() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(lessonProgress));
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(userService.userHasRole("learner")).thenReturn(false);
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(courseService.courseBelongsToTutor("course-1", "auth0|tutor-1")).thenReturn(true);

        ResponseEntity<LessonProgress> response = lessonProgressController.getLessonProgressById("progress-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("progress-1", response.getBody().getId());
    }

    @Test
    void shouldReturnNotFoundWhenLessonProgressDoesNotExist() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.empty());

        ResponseEntity<LessonProgress> response = lessonProgressController.getLessonProgressById("progress-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(enrollmentService, never()).getEnrollmentById(anyString());
    }

    @Test
    void shouldReturnForbiddenWhenUserCannotAccessLessonProgressEnrollment() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(lessonProgress));
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|other-user");
        when(userService.userHasRole("learner")).thenReturn(false);
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<LessonProgress> response = lessonProgressController.getLessonProgressById("progress-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void shouldReturnLessonProgressByEnrollmentIdWhenLearnerOwnsEnrollment() {
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-1");
        when(userService.userHasRole("learner")).thenReturn(true);
        when(lessonProgressRepository.findByEnrollmentId("enrollment-1"))
                .thenReturn(List.of(lessonProgress));

        ResponseEntity<List<LessonProgress>> response = lessonProgressController
                .getLessonProgressByEnrollmentId("enrollment-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(1, response.getBody().size());
        assertEquals("progress-1", response.getBody().get(0).getId());
        assertEquals("enrollment-1", response.getBody().get(0).getEnrollmentId());
    }

    @Test
    void shouldReturnLessonProgressByEnrollmentIdWhenTutorOwnsCourse() {
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(userService.userHasRole("learner")).thenReturn(false);
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(courseService.courseBelongsToTutor("course-1", "auth0|tutor-1")).thenReturn(true);
        when(lessonProgressRepository.findByEnrollmentId("enrollment-1"))
                .thenReturn(List.of(lessonProgress));

        ResponseEntity<List<LessonProgress>> response = lessonProgressController
                .getLessonProgressByEnrollmentId("enrollment-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(1, response.getBody().size());
        assertEquals("progress-1", response.getBody().get(0).getId());
    }

    @Test
    void shouldReturnNotFoundWhenEnrollmentDoesNotExistForProgressByEnrollmentId() {
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.empty());

        ResponseEntity<List<LessonProgress>> response = lessonProgressController
                .getLessonProgressByEnrollmentId("enrollment-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(lessonProgressRepository, never()).findByEnrollmentId(anyString());
    }

    @Test
    void shouldReturnForbiddenWhenUserCannotAccessProgressByEnrollmentId() {
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|other-user");
        when(userService.userHasRole("learner")).thenReturn(false);
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<List<LessonProgress>> response = lessonProgressController
                .getLessonProgressByEnrollmentId("enrollment-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(lessonProgressRepository, never()).findByEnrollmentId(anyString());
    }

    @Test
    void shouldReturnLessonProgressByLessonIdWhenTutorOwnsCourse() {
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseService.courseBelongsToTutor("course-1", "auth0|tutor-1")).thenReturn(true);
        when(lessonProgressRepository.findByLessonId("lesson-1"))
                .thenReturn(List.of(lessonProgress));

        ResponseEntity<List<LessonProgress>> response = lessonProgressController
                .getLessonProgressByLessonId("lesson-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(1, response.getBody().size());
        assertEquals("lesson-1", response.getBody().get(0).getLessonId());
    }

    @Test
    void shouldReturnNotFoundWhenLessonDoesNotExistForProgressByLessonId() {
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.empty());

        ResponseEntity<List<LessonProgress>> response = lessonProgressController
                .getLessonProgressByLessonId("lesson-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(lessonProgressRepository, never()).findByLessonId(anyString());
    }

    @Test
    void shouldReturnForbiddenWhenUserIsNotTutorForProgressByLessonId() {
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<List<LessonProgress>> response = lessonProgressController
                .getLessonProgressByLessonId("lesson-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(lessonProgressRepository, never()).findByLessonId(anyString());
    }

    @Test
    void shouldReturnForbiddenWhenTutorDoesNotOwnCourseForProgressByLessonId() {
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|other-tutor");
        when(courseService.courseBelongsToTutor("course-1", "auth0|other-tutor")).thenReturn(false);

        ResponseEntity<List<LessonProgress>> response = lessonProgressController
                .getLessonProgressByLessonId("lesson-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(lessonProgressRepository, never()).findByLessonId(anyString());
    }

    @Test
    void shouldMarkMaterialDoneWhenLearnerOwnsEnrollmentAndNotifyTutor() {
        LessonProgress materialDoneProgress = new LessonProgress(
                "enrollment-1",
                "lesson-1",
                LessonProgressState.MATERIAL_DONE,
                false,
                0);
        ReflectionTestUtils.setField(materialDoneProgress, "id", "progress-1");

        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(lessonProgress));
        when(userService.userHasRole("learner")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-1");
        when(userService.getEmail()).thenReturn("learner@lessonflow.com");
        when(enrollmentService.enrollmentBelongsToLearner("enrollment-1", "auth0|learner-1"))
                .thenReturn(true);
        when(lessonProgressService.markMaterialDone("progress-1"))
                .thenReturn(Optional.of(materialDoneProgress));

        ResponseEntity<LessonProgress> response = lessonProgressController.markMaterialDone("progress-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("progress-1", response.getBody().getId());
        assertEquals(LessonProgressState.MATERIAL_DONE, response.getBody().getState());

        verify(tutorNotificationService).notifyTutorMaterialDone(
                materialDoneProgress,
                "learner@lessonflow.com");
    }

    @Test
    void shouldReturnNotFoundWhenMarkingMaterialDoneForMissingProgress() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.empty());

        ResponseEntity<LessonProgress> response = lessonProgressController.markMaterialDone("progress-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(lessonProgressService, never()).markMaterialDone(anyString());
    }

    @Test
    void shouldReturnForbiddenWhenNonLearnerMarksMaterialDone() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(lessonProgress));
        when(userService.userHasRole("learner")).thenReturn(false);

        ResponseEntity<LessonProgress> response = lessonProgressController.markMaterialDone("progress-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(lessonProgressService, never()).markMaterialDone(anyString());
    }

    @Test
    void shouldReturnForbiddenWhenLearnerDoesNotOwnProgressEnrollmentForMaterialDone() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(lessonProgress));
        when(userService.userHasRole("learner")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|other-learner");
        when(enrollmentService.enrollmentBelongsToLearner("enrollment-1", "auth0|other-learner"))
                .thenReturn(false);

        ResponseEntity<LessonProgress> response = lessonProgressController.markMaterialDone("progress-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(lessonProgressService, never()).markMaterialDone(anyString());
    }

    @Test
    void shouldReturnBadRequestWhenMaterialDoneServiceReturnsEmpty() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(lessonProgress));
        when(userService.userHasRole("learner")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-1");
        when(enrollmentService.enrollmentBelongsToLearner("enrollment-1", "auth0|learner-1"))
                .thenReturn(true);
        when(lessonProgressService.markMaterialDone("progress-1"))
                .thenReturn(Optional.empty());

        ResponseEntity<LessonProgress> response = lessonProgressController.markMaterialDone("progress-1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldConfirmMeetingWhenTutorOwnsLessonCourse() {
        LessonProgress meetingDoneProgress = new LessonProgress(
                "enrollment-1",
                "lesson-1",
                LessonProgressState.MEETING_DONE,
                true,
                0);
        ReflectionTestUtils.setField(meetingDoneProgress, "id", "progress-1");

        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(lessonProgress));
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseService.courseBelongsToTutor("course-1", "auth0|tutor-1")).thenReturn(true);
        when(lessonProgressService.confirmMeeting("progress-1"))
                .thenReturn(Optional.of(meetingDoneProgress));

        ResponseEntity<LessonProgress> response = lessonProgressController.confirmMeeting("progress-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("progress-1", response.getBody().getId());
        assertEquals(LessonProgressState.MEETING_DONE, response.getBody().getState());
    }

    @Test
    void shouldReturnNotFoundWhenConfirmingMeetingForMissingProgress() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.empty());

        ResponseEntity<LessonProgress> response = lessonProgressController.confirmMeeting("progress-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(lessonProgressService, never()).confirmMeeting(anyString());
    }

    @Test
    void shouldReturnForbiddenWhenNonTutorConfirmsMeeting() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(lessonProgress));
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<LessonProgress> response = lessonProgressController.confirmMeeting("progress-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(lessonProgressService, never()).confirmMeeting(anyString());
    }

    @Test
    void shouldReturnForbiddenWhenTutorDoesNotOwnLessonCourseForConfirmMeeting() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(lessonProgress));
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUserId()).thenReturn("auth0|other-tutor");
        when(courseService.courseBelongsToTutor("course-1", "auth0|other-tutor")).thenReturn(false);

        ResponseEntity<LessonProgress> response = lessonProgressController.confirmMeeting("progress-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(lessonProgressService, never()).confirmMeeting(anyString());
    }

    @Test
    void shouldReturnBadRequestWhenConfirmMeetingServiceReturnsEmpty() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(lessonProgress));
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseService.courseBelongsToTutor("course-1", "auth0|tutor-1")).thenReturn(true);
        when(lessonProgressService.confirmMeeting("progress-1"))
                .thenReturn(Optional.empty());

        ResponseEntity<LessonProgress> response = lessonProgressController.confirmMeeting("progress-1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldReturnCurrentLessonWhenLearnerOwnsEnrollment() {
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-1");
        when(userService.userHasRole("learner")).thenReturn(true);
        when(lessonProgressService.getCurrentLesson("enrollment-1"))
                .thenReturn(Optional.of(lesson));

        ResponseEntity<Lesson> response = lessonProgressController.getCurrentLesson("enrollment-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("lesson-1", response.getBody().getId());
        assertEquals("Understanding Rhythm", response.getBody().getTitle());
    }

    @Test
    void shouldReturnForbiddenWhenUserCannotAccessCurrentLessonEnrollment() {
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|other-user");
        when(userService.userHasRole("learner")).thenReturn(false);
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<Lesson> response = lessonProgressController.getCurrentLesson("enrollment-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(lessonProgressService, never()).getCurrentLesson(anyString());
    }

    @Test
    void shouldReturnNotFoundWhenCurrentLessonEnrollmentDoesNotExist() {
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.empty());

        ResponseEntity<Lesson> response = lessonProgressController.getCurrentLesson("enrollment-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(lessonProgressService, never()).getCurrentLesson(anyString());
    }

    @Test
    void shouldReturnNotFoundWhenCurrentLessonServiceReturnsEmpty() {
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-1");
        when(userService.userHasRole("learner")).thenReturn(true);
        when(lessonProgressService.getCurrentLesson("enrollment-1"))
                .thenReturn(Optional.empty());

        ResponseEntity<Lesson> response = lessonProgressController.getCurrentLesson("enrollment-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}