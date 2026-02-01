package com.lms.kanjiorigin.service;

import java.util.List;

public interface KanjiLessonProgressService {
    void markLessonAsLearned(String lessonId, String userId);
    void unmarkLessonAsLearned(String lessonId, String userId);
    List<String> getLearnedLessonIds(String userId);
}
