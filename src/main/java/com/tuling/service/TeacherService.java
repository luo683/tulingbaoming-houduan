package com.tuling.service;

import com.tuling.entity.Teacher;
import com.tuling.repository.TeacherRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TeacherService {

    private static final Logger log = LoggerFactory.getLogger(TeacherService.class);

    @Autowired
    private TeacherRepository teacherRepository;

    /**
     * Get all teachers, ordered by sort order ascending.
     */
    public List<Teacher> getAllTeachers() {
        log.debug("Fetching all teachers");
        return teacherRepository.findAllByOrderBySortOrderAsc();
    }

    /**
     * Create a new teacher.
     */
    @Transactional
    public Teacher createTeacher(Teacher teacher) {
        log.info("Creating new teacher: {}", teacher.getName());
        Teacher saved = teacherRepository.save(teacher);
        log.info("Teacher created with id: {}", saved.getId());
        return saved;
    }

    /**
     * Update an existing teacher. Only non-null fields from the update are applied.
     */
    @Transactional
    public Teacher updateTeacher(Long id, Teacher update) {
        log.info("Updating teacher id: {}", id);
        Teacher existing = teacherRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Teacher not found with id: " + id));

        if (update.getName() != null) {
            existing.setName(update.getName());
        }
        if (update.getTitle() != null) {
            existing.setTitle(update.getTitle());
        }
        if (update.getDept() != null) {
            existing.setDept(update.getDept());
        }
        if (update.getQuote() != null) {
            existing.setQuote(update.getQuote());
        }
        if (update.getResearch() != null) {
            existing.setResearch(update.getResearch());
        }
        if (update.getTags() != null) {
            existing.setTags(update.getTags());
        }
        if (update.getAvatarGradient() != null) {
            existing.setAvatarGradient(update.getAvatarGradient());
        }
        existing.setSortOrder(update.getSortOrder());

        Teacher saved = teacherRepository.save(existing);
        log.info("Teacher updated: {}", saved.getId());
        return saved;
    }

    /**
     * Delete a teacher by id.
     */
    @Transactional
    public void deleteTeacher(Long id) {
        log.info("Deleting teacher id: {}", id);
        if (!teacherRepository.existsById(id)) {
            throw new NoSuchElementException("Teacher not found with id: " + id);
        }
        teacherRepository.deleteById(id);
        log.info("Teacher deleted: {}", id);
    }
}
