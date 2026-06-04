package com.tuling.service;

import com.tuling.entity.Member;
import com.tuling.repository.MemberRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    @Autowired
    private MemberRepository memberRepository;

    /**
     * Get all members, ordered by sort order ascending.
     */
    public List<Member> getAllMembers() {
        log.debug("Fetching all members");
        return memberRepository.findAllByOrderBySortOrderAsc();
    }

    /**
     * Get a single member by id.
     */
    public Member getMember(Long id) {
        log.debug("Fetching member id: {}", id);
        return memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Member not found with id: " + id));
    }

    /**
     * Create a new member.
     */
    @Transactional
    public Member createMember(Member member) {
        log.info("Creating new member: {}", member.getName());
        Member saved = memberRepository.save(member);
        log.info("Member created with id: {}", saved.getId());
        return saved;
    }

    /**
     * Update an existing member. Only non-null fields from the update are applied.
     */
    @Transactional
    public Member updateMember(Long id, Member update) {
        log.info("Updating member id: {}", id);
        Member existing = memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Member not found with id: " + id));

        if (update.getName() != null) {
            existing.setName(update.getName());
        }
        if (update.getRole() != null) {
            existing.setRole(update.getRole());
        }
        if (update.getDepartment() != null) {
            existing.setDepartment(update.getDepartment());
        }
        if (update.getGrade() != null) {
            existing.setGrade(update.getGrade());
        }
        if (update.getBio() != null) {
            existing.setBio(update.getBio());
        }
        if (update.getTags() != null) {
            existing.setTags(update.getTags());
        }
        if (update.getAvatarGradient() != null) {
            existing.setAvatarGradient(update.getAvatarGradient());
        }
        existing.setSortOrder(update.getSortOrder());
        if (update.getIsLead() != null) {
            existing.setIsLead(update.getIsLead());
        }

        Member saved = memberRepository.save(existing);
        log.info("Member updated: {}", saved.getId());
        return saved;
    }

    /**
     * Delete a member by id.
     */
    @Transactional
    public void deleteMember(Long id) {
        log.info("Deleting member id: {}", id);
        if (!memberRepository.existsById(id)) {
            throw new NoSuchElementException("Member not found with id: " + id);
        }
        memberRepository.deleteById(id);
        log.info("Member deleted: {}", id);
    }
}
