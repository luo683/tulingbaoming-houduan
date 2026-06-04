package com.tuling.controller;

import com.tuling.entity.Event;
import com.tuling.service.EventService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Event REST Controller
 * Public endpoints under /api/events
 * Admin endpoints under /api/events/admin
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    @Autowired
    private EventService eventService;

    // ── Public Endpoints ──

    /**
     * Get all events, newest first.
     */
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        log.info("GET /api/events");
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    /**
     * Get a single event by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable Long id) {
        log.info("GET /api/events/{}", id);
        try {
            Event event = eventService.getEvent(id);
            return ResponseEntity.ok(event);
        } catch (NoSuchElementException e) {
            log.warn("Event not found: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // ── Admin Endpoints ──

    /**
     * Admin: Create a new event.
     */
    @PostMapping("/admin")
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        log.info("POST /api/events/admin, title={}", event.getTitle());
        Event created = eventService.createEvent(event);
        return ResponseEntity.ok(created);
    }

    /**
     * Admin: Update an existing event.
     */
    @PutMapping("/admin/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @RequestBody Event update) {
        log.info("PUT /api/events/admin/{}", id);
        try {
            Event updated = eventService.updateEvent(id, update);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            log.warn("Event not found for update: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Admin: Delete an event.
     */
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        log.info("DELETE /api/events/admin/{}", id);
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.warn("Event not found for deletion: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
