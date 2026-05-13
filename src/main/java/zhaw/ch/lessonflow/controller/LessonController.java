package zhaw.ch.lessonflow.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import zhaw.ch.lessonflow.model.Lesson;
import zhaw.ch.lessonflow.model.LessonCreateDTO;
import zhaw.ch.lessonflow.repository.LessonRepository;
import zhaw.ch.lessonflow.services.CourseService;
import zhaw.ch.lessonflow.services.UserService;

@RestController
@RequestMapping("/api")
public class LessonController {

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    CourseService courseService;

    @Autowired
    UserService userService;

    @PostMapping("/lesson")
    public ResponseEntity<Lesson> createLesson(@RequestBody LessonCreateDTO fDTO) {

        if (!userService.userHasRole("tutor")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (hasInvalidLessonDetails(fDTO)) {
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

        if (hasInvalidLessonDetails(fDTO)) {
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
    public List<Lesson> getAllLessons() {
        return lessonRepository.findAll();
    }

    @GetMapping("/lesson/{id}")
    public ResponseEntity<Lesson> getLessonById(@PathVariable String id) {
        Optional<Lesson> lesson = lessonRepository.findById(id);

        if (lesson.isPresent()) {
            return new ResponseEntity<>(lesson.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/lesson/course/{courseId}")
    public List<Lesson> getLessonsByCourseId(@PathVariable String courseId) {
        return lessonRepository.findByCourseIdOrderByLessonNumberAsc(courseId);
    }

    private boolean hasInvalidLessonDetails(LessonCreateDTO dto) {
        return dto.getTitle() == null || dto.getTitle().isBlank()
                || dto.getMaterial() == null || dto.getMaterial().isBlank()
                || dto.getMeetingLink() == null || dto.getMeetingLink().isBlank();
    }
}