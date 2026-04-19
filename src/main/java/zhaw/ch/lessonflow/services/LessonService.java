package zhaw.ch.lessonflow.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zhaw.ch.lessonflow.model.Lesson;
import zhaw.ch.lessonflow.repository.LessonRepository;

@Service
public class LessonService {

    @Autowired
    LessonRepository lessonRepository;

    public boolean lessonExists(String lessonId) {
        return lessonRepository.existsById(lessonId);
    }

    public Optional<Lesson> getLessonById(String lessonId) {
        return lessonRepository.findById(lessonId);
    }

    public Optional<Lesson> getNextLesson(String courseId, int currentLessonNumber) {
        return lessonRepository.findByCourseIdAndLessonNumber(courseId, currentLessonNumber + 1);
    }
}