package com.tuling.service;

import com.tuling.entity.Project;
import com.tuling.repository.ProjectRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Get all active projects, newest first.
     */
    public List<Project> getActiveProjects() {
        log.debug("Fetching active projects");
        return projectRepository.findByStatusOrderByCreatedAtDesc("active");
    }

    /**
     * Get all projects regardless of status.
     */
    public List<Project> getAllProjects() {
        log.debug("Fetching all projects");
        return projectRepository.findAll();
    }

    /**
     * Create a new project.
     */
    @Transactional
    public Project createProject(Project project) {
        log.info("Creating new project: {}", project.getName());
        Project saved = projectRepository.save(project);
        log.info("Project created with id: {}", saved.getId());
        return saved;
    }

    /**
     * Update an existing project. Only non-null fields from the update are applied.
     */
    @Transactional
    public Project updateProject(Long id, Project update) {
        log.info("Updating project id: {}", id);
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Project not found with id: " + id));

        if (update.getName() != null) {
            existing.setName(update.getName());
        }
        if (update.getDescription() != null) {
            existing.setDescription(update.getDescription());
        }
        if (update.getTechTags() != null) {
            existing.setTechTags(update.getTechTags());
        }
        if (update.getGithubLink() != null) {
            existing.setGithubLink(update.getGithubLink());
        }
        if (update.getDemoLink() != null) {
            existing.setDemoLink(update.getDemoLink());
        }
        if (update.getCoverEmoji() != null) {
            existing.setCoverEmoji(update.getCoverEmoji());
        }
        if (update.getStatus() != null) {
            existing.setStatus(update.getStatus());
        }
        // memberCount defaults to 1, so only apply if positive
        if (update.getMemberCount() > 0) {
            existing.setMemberCount(update.getMemberCount());
        }
        if (update.getStarCount() >= 0) {
            existing.setStarCount(update.getStarCount());
        }

        Project saved = projectRepository.save(existing);
        log.info("Project updated: {}", saved.getId());
        return saved;
    }

    /**
     * Delete a project by id.
     */
    @Transactional
    public void deleteProject(Long id) {
        log.info("Deleting project id: {}", id);
        if (!projectRepository.existsById(id)) {
            throw new NoSuchElementException("Project not found with id: " + id);
        }
        projectRepository.deleteById(id);
        log.info("Project deleted: {}", id);
    }
}
