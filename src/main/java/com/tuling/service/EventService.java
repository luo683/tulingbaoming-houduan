package com.tuling.service;

import com.tuling.entity.Event;
import com.tuling.repository.EventRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    @Autowired
    private EventRepository eventRepository;

    /**
     * Get all events, ordered by event date descending (newest first).
     */
    public List<Event> getAllEvents() {
        log.debug("Fetching all events");
        return eventRepository.findAllByOrderByEventDateDesc();
    }

    /**
     * Get events filtered by status, ordered by event date ascending.
     */
    public List<Event> getEventsByStatus(String status) {
        log.debug("Fetching events with status: {}", status);
        return eventRepository.findByStatusOrderByEventDateAsc(status);
    }

    /**
     * Get a single event by id.
     */
    public Event getEvent(Long id) {
        log.debug("Fetching event id: {}", id);
        return eventRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Event not found with id: " + id));
    }

    /**
     * Create a new event.
     */
    @Transactional
    public Event createEvent(Event event) {
        log.info("Creating new event: {}", event.getTitle());
        Event saved = eventRepository.save(event);
        log.info("Event created with id: {}", saved.getId());
        return saved;
    }

    /**
     * Update an existing event. Only non-null fields from the update are applied.
     */
    @Transactional
    public Event updateEvent(Long id, Event update) {
        log.info("Updating event id: {}", id);
        Event existing = eventRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Event not found with id: " + id));

        if (update.getTitle() != null) {
            existing.setTitle(update.getTitle());
        }
        if (update.getDescription() != null) {
            existing.setDescription(update.getDescription());
        }
        if (update.getEventDate() != null) {
            existing.setEventDate(update.getEventDate());
        }
        if (update.getEventTime() != null) {
            existing.setEventTime(update.getEventTime());
        }
        if (update.getLocation() != null) {
            existing.setLocation(update.getLocation());
        }
        if (update.getStatus() != null) {
            existing.setStatus(update.getStatus());
        }
        if (update.getCoverEmoji() != null) {
            existing.setCoverEmoji(update.getCoverEmoji());
        }

        Event saved = eventRepository.save(existing);
        log.info("Event updated: {}", saved.getId());
        return saved;
    }

    /**
     * Delete an event by id.
     */
    @Transactional
    public void deleteEvent(Long id) {
        log.info("Deleting event id: {}", id);
        if (!eventRepository.existsById(id)) {
            throw new NoSuchElementException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
        log.info("Event deleted: {}", id);
    }
}
