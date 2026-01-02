package com.nexus.crew.service;

import com.nexus.crew.client.LifeSupportClient;
import com.nexus.crew.dto.*;
import com.nexus.crew.entity.CrewMember;
import com.nexus.crew.entity.Section;
import com.nexus.crew.repository.CrewMemberRepository;
import com.nexus.crew.repository.SectionRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class CrewService {
    
    private static final Logger log = LoggerFactory.getLogger(CrewService.class);
    
    private static final String REDIS_CREW_COUNT = "crew:count:total";
    private static final String REDIS_CREW_ACTIVE = "crew:count:active";
    
    private final CrewMemberRepository crewMemberRepository;
    private final SectionRepository sectionRepository;
    private final LifeSupportClient lifeSupportClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final Tracer tracer;
    private final boolean customSpansEnabled;
    
    public CrewService(
            CrewMemberRepository crewMemberRepository,
            SectionRepository sectionRepository,
            LifeSupportClient lifeSupportClient,
            RedisTemplate<String, String> redisTemplate,
            Tracer tracer,
            @Value("${nexus.telemetry.custom-spans:false}") boolean customSpansEnabled) {
        this.crewMemberRepository = crewMemberRepository;
        this.sectionRepository = sectionRepository;
        this.lifeSupportClient = lifeSupportClient;
        this.redisTemplate = redisTemplate;
        this.tracer = tracer;
        this.customSpansEnabled = customSpansEnabled;
    }
    
    public List<CrewMemberDto> getAllCrew() {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("crew.getAllCrew").startSpan();
            try (Scope scope = span.makeCurrent()) {
                List<CrewMember> allCrew = crewMemberRepository.findAll();
                List<Section> allSections = sectionRepository.findAll();
                
                List<CrewMemberDto> crew = allCrew.stream()
                        .map(member -> mapToDto(member, allSections))
                        .toList();
                        
                span.setAttribute("crew.count", crew.size());
                return crew;
            } finally {
                span.end();
            }
        }
        
        List<CrewMember> allCrew = crewMemberRepository.findAll();
        List<Section> allSections = sectionRepository.findAll();
        
        return allCrew.stream()
                .map(member -> mapToDto(member, allSections))
                .toList();
    }
    
    public Optional<CrewMemberDto> getCrewById(Long id) {
        return crewMemberRepository.findById(id)
                .map(member -> {
                    String sectionName = null;
                    if (member.getSectionId() != null) {
                        sectionName = sectionRepository.findById(member.getSectionId())
                                .map(Section::getName)
                                .orElse("Unknown");
                    }
                    return CrewMemberDto.fromEntity(member, sectionName);
                });
    }
    
    public CrewSummary getCrewCount() {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("crew.getCrewCount").startSpan();
            try (Scope scope = span.makeCurrent()) {
                CrewSummary summary = buildCrewSummary();
                span.setAttribute("crew.total", summary.totalCrew());
                span.setAttribute("crew.active", summary.activeCrew());
                return summary;
            } finally {
                span.end();
            }
        }
        return buildCrewSummary();
    }
    
    private CrewSummary buildCrewSummary() {
        List<CrewMember> allCrew = crewMemberRepository.findAll();
        
        long totalCrew = allCrew.size();
        long activeCrew = allCrew.stream()
                .filter(c -> c.getStatus() == CrewMember.CrewStatus.ACTIVE)
                .count();
        long onLeaveCrew = allCrew.stream()
                .filter(c -> c.getStatus() == CrewMember.CrewStatus.ON_LEAVE)
                .count();
        long offDutyCrew = allCrew.stream()
                .filter(c -> c.getStatus() == CrewMember.CrewStatus.OFF_DUTY)
                .count();
        long inTransitCrew = allCrew.stream()
                .filter(c -> c.getStatus() == CrewMember.CrewStatus.IN_TRANSIT)
                .count();
        
        List<Section> sections = sectionRepository.findAll();
        int totalSections = sections.size();
        int totalCapacity = sections.stream()
                .mapToInt(Section::getMaxCapacity)
                .sum();
        int totalOccupancy = sections.stream()
                .mapToInt(Section::getCurrentOccupancy)
                .sum();
        double overallOccupancyPercent = totalCapacity > 0 
                ? (totalOccupancy * 100.0) / totalCapacity 
                : 0;
        
        // Update Redis cache
        try {
            redisTemplate.opsForValue().set(REDIS_CREW_COUNT, String.valueOf(totalCrew), Duration.ofMinutes(1));
            redisTemplate.opsForValue().set(REDIS_CREW_ACTIVE, String.valueOf(activeCrew), Duration.ofMinutes(1));
        } catch (Exception e) {
            log.warn("Failed to update Redis cache: {}", e.getMessage());
        }
        
        return new CrewSummary(
                totalCrew,
                activeCrew,
                onLeaveCrew,
                offDutyCrew,
                inTransitCrew,
                totalSections,
                totalCapacity,
                totalOccupancy,
                overallOccupancyPercent
        );
    }
    
    public List<CrewMemberDto> getCrewBySection(Long sectionId) {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("crew.getCrewBySection")
                    .setAttribute("section.id", sectionId)
                    .startSpan();
            try (Scope scope = span.makeCurrent()) {
                String sectionName = sectionRepository.findById(sectionId)
                        .map(Section::getName)
                        .orElse("Unknown");
                        
                List<CrewMemberDto> crew = crewMemberRepository.findBySectionId(sectionId).stream()
                        .map(member -> CrewMemberDto.fromEntity(member, sectionName))
                        .toList();
                span.setAttribute("crew.count", crew.size());
                return crew;
            } finally {
                span.end();
            }
        }
        
        String sectionName = sectionRepository.findById(sectionId)
                .map(Section::getName)
                .orElse("Unknown");
                
        return crewMemberRepository.findBySectionId(sectionId).stream()
                .map(member -> CrewMemberDto.fromEntity(member, sectionName))
                .toList();
    }
    
    public List<CrewMemberDto> getAvailableCrew() {
        List<Section> allSections = sectionRepository.findAll();
        return crewMemberRepository.findByStatus(CrewMember.CrewStatus.ACTIVE).stream()
                .map(member -> mapToDto(member, allSections))
                .toList();
    }
    
    @Transactional
    public CrewMemberDto relocateCrew(RelocateRequest request) {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("crew.relocate")
                    .setAttribute("crew.id", request.crewId())
                    .setAttribute("section.target", request.targetSectionId())
                    .startSpan();
            try (Scope scope = span.makeCurrent()) {
                span.addEvent("validating_relocation");
                CrewMemberDto result = performRelocation(request);
                span.addEvent("relocation_complete");
                return result;
            } finally {
                span.end();
            }
        }
        return performRelocation(request);
    }
    
    private CrewMemberDto performRelocation(RelocateRequest request) {
        log.info("Relocating crew member {} to section {}", request.crewId(), request.targetSectionId());
        
        CrewMember crewMember = crewMemberRepository.findById(request.crewId())
                .orElseThrow(() -> new CrewNotFoundException(
                        "Crew member not found: " + request.crewId()));
        
        Section targetSection = sectionRepository.findById(request.targetSectionId())
                .orElseThrow(() -> new SectionNotFoundException(
                        "Section not found: " + request.targetSectionId()));
        
        // Check capacity
        if (targetSection.getCurrentOccupancy() >= targetSection.getMaxCapacity()) {
            throw new SectionAtCapacityException(
                    "Section " + targetSection.getName() + " is at maximum capacity");
        }
        
        Long previousSectionId = crewMember.getSectionId();
        
        // Update old section occupancy
        if (previousSectionId != null) {
            Section previousSection = sectionRepository.findById(previousSectionId).orElse(null);
            if (previousSection != null) {
                previousSection.setCurrentOccupancy(
                        Math.max(0, previousSection.getCurrentOccupancy() - 1));
                sectionRepository.save(previousSection);
                
                // Notify life support of decrease
                try {
                    lifeSupportClient.adjustCapacity(previousSectionId, -1);
                } catch (Exception e) {
                    log.warn("Failed to notify life support of departure from section {}: {}", 
                            previousSectionId, e.getMessage());
                }
            }
        }
        
        // Update new section occupancy
        targetSection.setCurrentOccupancy(targetSection.getCurrentOccupancy() + 1);
        sectionRepository.save(targetSection);
        
        // Update crew member
        crewMember.setSectionId(request.targetSectionId());
        crewMember = crewMemberRepository.save(crewMember);
        
        // Notify life support of increase
        try {
            lifeSupportClient.adjustCapacity(request.targetSectionId(), 1);
        } catch (Exception e) {
            log.warn("Failed to notify life support of arrival at section {}: {}", 
                    request.targetSectionId(), e.getMessage());
        }
        
        log.info("Successfully relocated crew member {} from section {} to section {}",
                crewMember.getName(), previousSectionId, request.targetSectionId());
        
        return CrewMemberDto.fromEntity(crewMember, targetSection.getName());
    }
    
    @Transactional
    public List<CrewMemberDto> registerArrival(RegisterArrivalRequest request) {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("crew.registerArrival")
                    .setAttribute("ship.id", request.shipId())
                    .setAttribute("crew.count", request.crewCount())
                    .startSpan();
            try (Scope scope = span.makeCurrent()) {
                span.addEvent("registering_arrivals");
                List<CrewMemberDto> result = performArrivalRegistration(request);
                span.addEvent("arrivals_registered");
                return result;
            } finally {
                span.end();
            }
        }
        return performArrivalRegistration(request);
    }
    
    private List<CrewMemberDto> performArrivalRegistration(RegisterArrivalRequest request) {
        log.info("Registering {} crew arrivals from ship {}", request.crewCount(), request.shipId());
        
        // Find a section with available capacity
        Section arrivalSection = sectionRepository.findAll().stream()
                .filter(s -> s.getCurrentOccupancy() < s.getMaxCapacity())
                .findFirst()
                .orElseThrow(() -> new SectionAtCapacityException(
                        "No sections available with capacity for incoming crew"));
        
        List<CrewMember> newCrewMembers = new java.util.ArrayList<>();
        
        for (int i = 0; i < request.crewCount(); i++) {
            CrewMember crewMember = new CrewMember();
            crewMember.setName("Crew-Ship" + request.shipId() + "-" + (i + 1));
            crewMember.setRank("Ensign");
            crewMember.setRole("General Duty");
            crewMember.setSectionId(arrivalSection.getId());
            crewMember.setStatus(CrewMember.CrewStatus.IN_TRANSIT);
            crewMember.setArrivedAt(Instant.now());
            newCrewMembers.add(crewMemberRepository.save(crewMember));
        }
        
        // Update section occupancy
        arrivalSection.setCurrentOccupancy(arrivalSection.getCurrentOccupancy() + request.crewCount());
        sectionRepository.save(arrivalSection);
        
        // Notify life support
        try {
            lifeSupportClient.adjustCapacity(arrivalSection.getId(), request.crewCount());
        } catch (Exception e) {
            log.warn("Failed to notify life support of crew arrival: {}", e.getMessage());
        }
        
        log.info("Registered {} new crew members in section {}", 
                request.crewCount(), arrivalSection.getName());
        
        return newCrewMembers.stream()
                .map(member -> CrewMemberDto.fromEntity(member, arrivalSection.getName()))
                .toList();
    }
    
    public List<SectionDto> getAllSections() {
        return sectionRepository.findAll().stream()
                .map(SectionDto::fromEntity)
                .toList();
    }
    
    public SectionDto getSectionById(Long id) {
        return sectionRepository.findById(id)
                .map(SectionDto::fromEntity)
                .orElseThrow(() -> new SectionNotFoundException("Section not found: " + id));
    }
    
    public Integer getSectionHeadcount(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new SectionNotFoundException("Section not found: " + sectionId));
        return section.getCurrentOccupancy();
    }
    
    // Exception classes
    public static class CrewNotFoundException extends RuntimeException {
        public CrewNotFoundException(String message) {
            super(message);
        }
    }
    
    public static class SectionNotFoundException extends RuntimeException {
        public SectionNotFoundException(String message) {
            super(message);
        }
    }
    
    public static class SectionAtCapacityException extends RuntimeException {
        public SectionAtCapacityException(String message) {
            super(message);
        }
    }
    
    // Helper method to map entity to DTO efficiently
    private CrewMemberDto mapToDto(CrewMember member, List<Section> sections) {
        String sectionName = null;
        if (member.getSectionId() != null) {
            sectionName = sections.stream()
                    .filter(s -> s.getId().equals(member.getSectionId()))
                    .findFirst()
                    .map(Section::getName)
                    .orElse("Unknown");
        }
        return CrewMemberDto.fromEntity(member, sectionName);
    }
}
