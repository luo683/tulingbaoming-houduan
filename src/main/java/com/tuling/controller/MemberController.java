package com.tuling.controller;

import com.tuling.entity.Member;
import com.tuling.service.MemberService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Member REST Controller
 * Public endpoints under /api/members
 * Admin endpoints under /api/members/admin
 */
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private static final Logger log = LoggerFactory.getLogger(MemberController.class);

    @Autowired
    private MemberService memberService;

    // ── Public Endpoints ──

    /**
     * Get all members, ordered by sort order (leads first, then by sortOrder).
     */
    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers() {
        log.info("GET /api/members");
        List<Member> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    /**
     * Get a single member by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Member> getMember(@PathVariable Long id) {
        log.info("GET /api/members/{}", id);
        try {
            Member member = memberService.getMember(id);
            return ResponseEntity.ok(member);
        } catch (NoSuchElementException e) {
            log.warn("Member not found: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // ── Admin Endpoints ──

    /**
     * Admin: Create a new member.
     */
    @PostMapping("/admin")
    public ResponseEntity<Member> createMember(@RequestBody Member member) {
        log.info("POST /api/members/admin, name={}", member.getName());
        Member created = memberService.createMember(member);
        return ResponseEntity.ok(created);
    }

    /**
     * Admin: Update an existing member.
     */
    @PutMapping("/admin/{id}")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @RequestBody Member update) {
        log.info("PUT /api/members/admin/{}", id);
        try {
            Member updated = memberService.updateMember(id, update);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            log.warn("Member not found for update: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Admin: Delete a member.
     */
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        log.info("DELETE /api/members/admin/{}", id);
        try {
            memberService.deleteMember(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            log.warn("Member not found for deletion: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
