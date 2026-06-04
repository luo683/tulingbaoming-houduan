package com.tuling.controller;

import com.tuling.entity.Teacher;
import com.tuling.service.TeacherService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Teacher REST Controller
 * Public endpoints under /api/teachers
 * Admin endpoints under /api/teachers/admin
 */
@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private static final Logger log = LoggerFactory.getLogger(TeacherController.class);

    @Autowired
    private TeacherService teacherService;

    // ── Public Endpoints ──

    /**
     * Get all teachers, ordered by sort order.
     */
    @GetMapping
    public ResponseEntity<List<Teacher>> getAllTeachers() {
        log.info("GET /api/teachers");
        List<Teacher> teachers = teacherService.getAllTeachers();
        return ResponseEntity.ok(teachers);
    }

    // ── Admin Endpoints ──

    /**
     * Admin: Create a new teacher.
     */
    @PostMapping("/admin")
    public ResponseEntity<Teacher> createTeacher(@RequestBody Teacher teacher) {
        log.info("POST /api/teachers/admin, name={}", teacher.getName());
        Teacher created = teacherService.createTeacher(teacher);
        return ResponseEntity.ok(created);
    }

    /**
     * Admin: Update an existing teacher.
     */
    @PutMapping("/admin/{id}")
    public ResponseEntity<Teacher> updateTeacher(@PathVariable Long id, @RequestBody Teacher update) {
        log.info("PUT /api/teachers/admin/{}", id);
        try {
            Teacher updated = teacherService.updateTeacher(id, update);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            log.warn("Teacher not found for update: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Admin: Delete a teacher.
     */
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        log.info("DELETE /api/teachers/admin/{}", id);
        try {
            teacherService.deleteTeacher(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.warn("Teacher not found for deletion: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
