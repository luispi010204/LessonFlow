package zhaw.ch.lessonflow.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zhaw.ch.lessonflow.model.Course;
import zhaw.ch.lessonflow.model.Enrollment;
import zhaw.ch.lessonflow.model.Lesson;
import zhaw.ch.lessonflow.model.LessonProgress;
import zhaw.ch.lessonflow.model.Mail;
import zhaw.ch.lessonflow.repository.CourseRepository;

@Service
public class TutorNotificationService {

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    EnrollmentService enrollmentService;

    @Autowired
    LessonService lessonService;

    @Autowired
    MailService mailService;

    public boolean notifyTutorMaterialDone(LessonProgress progress, String learnerEmail) {
        if (progress == null) {
            return false;
        }

        Optional<Lesson> lessonData = lessonService.getLessonById(progress.getLessonId());

        if (lessonData.isEmpty()) {
            return false;
        }

        Lesson lesson = lessonData.get();

        Optional<Enrollment> enrollmentData = enrollmentService.getEnrollmentById(progress.getEnrollmentId());

        if (enrollmentData.isEmpty()) {
            return false;
        }

        Enrollment enrollment = enrollmentData.get();

        Optional<Course> courseData = courseRepository.findById(lesson.getCourseId());

        if (courseData.isEmpty()) {
            return false;
        }

        Course course = courseData.get();

        if (course.getTutorEmail() == null || course.getTutorEmail().isBlank()) {
            return false;
        }

        Mail mail = new Mail();
        mail.setTo(course.getTutorEmail());
        mail.setSubject("LessonFlow: Learner completed lesson material");

        mail.setMessage("""
                Hello

                A learner has completed the lesson material and is ready for the meeting.

                Course:
                %s

                Lesson:
                Lesson %d: %s

                Learner:
                %s

                Enrollment ID:
                %s

                Please arrange the meeting with the learner. After the meeting has taken place, open your LessonFlow Tutor Dashboard and confirm the meeting for this lesson.

                Best regards
                LessonFlow
                """.formatted(
                course.getTitle(),
                lesson.getLessonNumber(),
                lesson.getTitle(),
                getLearnerDisplayValue(learnerEmail, enrollment),
                enrollment.getId()));

        return mailService.sendMail(mail);
    }

    private String getLearnerDisplayValue(String learnerEmail, Enrollment enrollment) {
        if (learnerEmail != null && !learnerEmail.isBlank()) {
            return learnerEmail;
        }

        return enrollment.getLearnerUserId();
    }
}