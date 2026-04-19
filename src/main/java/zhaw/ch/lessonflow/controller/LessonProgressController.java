package zhaw.ch.lessonflow.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import zhaw.ch.lessonflow.model.LessonProgress;
import zhaw.ch.lessonflow.repository.LessonProgressRepository;
import zhaw.ch.lessonflow.services.LessonProgressService;

@RestController
@RequestMapping("/api")
public class LessonProgressController {

    @Autowired
    LessonProgressRepository lessonProgressRepository;

    @Autowired
    LessonProgressService lessonProgressService;

    @PostMapping("/progress")
    public ResponseEntity<LessonProgress> createLessonProgress(@RequestBody LessonProgress lessonProgress) {
        LessonProgress savedProgress = lessonProgressRepository.save(lessonProgress);
        return new ResponseEntity<>(savedProgress, HttpStatus.CREATED);
    }

    @GetMapping("/progress")
    public List<LessonProgress> getAllLessonProgress() {
        return lessonProgressRepository.findAll();
    }

    @GetMapping("/progress/{id}")
    public ResponseEntity<LessonProgress> getLessonProgressById(@PathVariable String id) {
        Optional<LessonProgress> lessonProgress = lessonProgressRepository.findById(id);

        if (lessonProgress.isPresent()) {
            return new ResponseEntity<>(lessonProgress.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/progress/enrollment/{enrollmentId}")
    public List<LessonProgress> getLessonProgressByEnrollmentId(@PathVariable String enrollmentId) {
        return lessonProgressRepository.findByEnrollmentId(enrollmentId);
    }

    @GetMapping("/progress/lesson/{lessonId}")
    public List<LessonProgress> getLessonProgressByLessonId(@PathVariable String lessonId) {
        return lessonProgressRepository.findByLessonId(lessonId);
    }

    @PostMapping("/progress/{id}/material-done")
    public ResponseEntity<LessonProgress> markMaterialDone(@PathVariable String id) {
        Optional<LessonProgress> progress = lessonProgressService.markMaterialDone(id);

        if (progress.isPresent()) {
            return new ResponseEntity<>(progress.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/progress/{id}/meeting-done")
    public ResponseEntity<LessonProgress> confirmMeeting(@PathVariable String id) {
        Optional<LessonProgress> progress = lessonProgressService.confirmMeeting(id);

        if (progress.isPresent()) {
            return new ResponseEntity<>(progress.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /*  abgelöst von attempt/submit
    @PostMapping("/progress/{id}/passed")
    public ResponseEntity<LessonProgress> markPassed(@PathVariable String id) {
        Optional<LessonProgress> progress = lessonProgressService.markPassed(id);

        if (progress.isPresent()) {
            return new ResponseEntity<>(progress.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
        */

}