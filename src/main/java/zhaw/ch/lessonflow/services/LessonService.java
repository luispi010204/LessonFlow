package zhaw.ch.lessonflow.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zhaw.ch.lessonflow.repository.LessonRepository;

@Service
public class LessonService {

    @Autowired
    LessonRepository lessonRepository;

    public boolean lessonExists(String lessonId) {
        return lessonRepository.existsById(lessonId);
    }
}