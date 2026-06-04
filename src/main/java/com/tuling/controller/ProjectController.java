package com.tuling.controller;

import com.tuling.entity.Project;
import com.tuling.service.ProjectService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Project REST Controller
 * Public endpoints under /api/projects
 * Admin endpoints under /api/projects/admin
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;

    // ── Public Endpoints ──

    /**
     * Get all projects, ordered by creation date (newest first).
     */
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        log.info("GET /api/projects");
        List<Project> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    // ── Admin Endpoints ──

    /**
     * Admin: Create a new project.
     */
    @PostMapping("/admin")
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        log.info("POST /api/projects/admin, name={}", project.getName());
        Project created = projectService.createProject(project);
        return ResponseEntity.ok(created);
    }

    /**
     * Admin: Update an existing project.
     */
    @PutMapping("/admin/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project update) {
        log.info("PUT /api/projects/admin/{}", id);
        try {
            Project updated = projectService.updateProject(id, update);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            log.warn("Project not found for update: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Admin: Delete a project.
     */
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        log.info("DELETE /api/projects/admin/{}", id);
        try {
            projectService.deleteProject(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.warn("Project not found for deletion: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
