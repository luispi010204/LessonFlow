package zhaw.ch.lessonflow.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import zhaw.ch.lessonflow.model.Course;
import zhaw.ch.lessonflow.model.Enrollment;
import zhaw.ch.lessonflow.model.Lesson;
import zhaw.ch.lessonflow.model.LessonCreateDTO;
import zhaw.ch.lessonflow.repository.CourseRepository;
import zhaw.ch.lessonflow.repository.EnrollmentRepository;
import zhaw.ch.lessonflow.repository.LessonRepository;
import zhaw.ch.lessonflow.services.CourseService;
import zhaw.ch.lessonflow.services.EnrollmentService;
import zhaw.ch.lessonflow.services.UserService;

@RestController
@RequestMapping("/api")
public class LessonController {

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    CourseService courseService;

    @Autowired
    EnrollmentService enrollmentService;

    @Autowired
    UserService userService;

    @PostMapping("/lesson")
    public ResponseEntity<Lesson> createLesson(@RequestBody LessonCreateDTO fDTO) {

        if (!userService.userHasRole("tutor")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (hasInvalidLessonCreateDetails(fDTO)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!courseService.courseExists(fDTO.getCourseId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!courseService.courseBelongsToTutor(fDTO.getCourseId(), userService.getCurrentUserId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (lessonRepository.findByCourseIdAndLessonNumber(
                fDTO.getCourseId(), fDTO.getLessonNumber()).isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Lesson fDAO = new Lesson(
                fDTO.getCourseId(),
                fDTO.getLessonNumber(),
                fDTO.getTitle(),
                fDTO.getMaterial(),
                fDTO.getMeetingLink()
        );

        Lesson savedLesson = lessonRepository.save(fDAO);

        enrollmentService.createProgressForNewLessonForExistingEnrollments(savedLesson);

        return new ResponseEntity<>(savedLesson, HttpStatus.CREATED);
    }

    @PutMapping("/lesson/{id}")
    public ResponseEntity<Lesson> updateLesson(
            @PathVariable String id,
            @RequestBody LessonCreateDTO fDTO) {

        if (!userService.userHasRole("tutor")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<Lesson> lessonData = lessonRepository.findById(id);

        if (lessonData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (hasInvalidLessonContent(fDTO)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Lesson lesson = lessonData.get();

        if (!courseService.courseBelongsToTutor(lesson.getCourseId(), userService.getCurrentUserId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        lesson.updateDetails(
                fDTO.getTitle(),
                fDTO.getMaterial(),
                fDTO.getMeetingLink()
        );

        Lesson savedLesson = lessonRepository.save(lesson);
        return new ResponseEntity<>(savedLesson, HttpStatus.OK);
    }

    @GetMapping("/lesson")
    public ResponseEntity<List<Lesson>> getAllLessons() {

        if (!userService.userHasRole("tutor")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        String currentUserId = userService.getCurrentUserId();

        List<Course> tutorCourses = courseRepository.findByTutorUserId(currentUserId);
        List<Lesson> lessons = new ArrayList<>();

        for (Course course : tutorCourses) {
            lessons.addAll(lessonRepository.findByCourseIdOrderByLessonNumberAsc(course.getId()));
        }

        return new ResponseEntity<>(lessons, HttpStatus.OK);
    }

    @GetMapping("/lesson/{id}")
    public ResponseEntity<Lesson> getLessonById(@PathVariable String id) {
        Optional<Lesson> lessonData = lessonRepository.findById(id);

        if (lessonData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Lesson lesson = lessonData.get();

        if (canAccessLesson(lesson)) {
            return new ResponseEntity<>(lesson, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/lesson/course/{courseId}")
    public ResponseEntity<List<Lesson>> getLessonsByCourseId(@PathVariable String courseId) {

        if (!courseService.courseExists(courseId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String currentUserId = userService.getCurrentUserId();

        if (userService.userHasRole("tutor")
                && courseService.courseBelongsToTutor(courseId, currentUserId)) {
            List<Lesson> lessons = lessonRepository.findByCourseIdOrderByLessonNumberAsc(courseId);
            return new ResponseEntity<>(lessons, HttpStatus.OK);
        }

        if (userService.userHasRole("learner")
                && learnerIsEnrolledInCourse(courseId, currentUserId)) {
            List<Lesson> lessons = lessonRepository.findByCourseIdOrderByLessonNumberAsc(courseId);
            return new ResponseEntity<>(lessons, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    private boolean canAccessLesson(Lesson lesson) {
        String currentUserId = userService.getCurrentUserId();

        if (userService.userHasRole("tutor")
                && courseService.courseBelongsToTutor(lesson.getCourseId(), currentUserId)) {
            return true;
        }

        return userService.userHasRole("learner")
                && learnerIsEnrolledInCourse(lesson.getCourseId(), currentUserId);
    }

    private boolean learnerIsEnrolledInCourse(String courseId, String learnerUserId) {
        List<Enrollment> enrollments = enrollmentRepository.findByLearnerUserId(learnerUserId);

        for (Enrollment enrollment : enrollments) {
            if (enrollment.getCourseId().equals(courseId)
                    && "ENROLLED".equals(enrollment.getStatus())) {
                return true;
            }
        }

        return false;
    }

    private boolean hasInvalidLessonCreateDetails(LessonCreateDTO dto) {
        return dto.getCourseId() == null || dto.getCourseId().isBlank()
                || dto.getLessonNumber() < 1
                || hasInvalidLessonContent(dto);
    }

    private boolean hasInvalidLessonContent(LessonCreateDTO dto) {
        return dto.getTitle() == null || dto.getTitle().isBlank()
                || dto.getMaterial() == null || dto.getMaterial().isBlank()
                || dto.getMeetingLink() == null || dto.getMeetingLink().isBlank();
    }
}