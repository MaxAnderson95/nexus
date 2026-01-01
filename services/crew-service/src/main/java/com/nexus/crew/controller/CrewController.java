package com.nexus.crew.controller;

import com.nexus.crew.dto.*;
import com.nexus.crew.service.CrewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crew")
public class CrewController {
    
    private final CrewService crewService;
    
    public CrewController(CrewService crewService) {
        this.crewService = crewService;
    }
    
    @GetMapping
    public ResponseEntity<List<CrewMemberDto>> getAllCrew() {
        return ResponseEntity.ok(crewService.getAllCrew());
    }
    
    @GetMapping("/count")
    public ResponseEntity<CrewSummary> getCrewCount() {
        return ResponseEntity.ok(crewService.getCrewCount());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CrewMemberDto> getCrewById(@PathVariable Long id) {
        return crewService.getCrewById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<CrewMemberDto>> getCrewBySection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(crewService.getCrewBySection(sectionId));
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<CrewMemberDto>> getAvailableCrew() {
        return ResponseEntity.ok(crewService.getAvailableCrew());
    }
    
    @PostMapping("/relocate")
    public ResponseEntity<CrewMemberDto> relocateCrew(@Valid @RequestBody RelocateRequest request) {
        CrewMemberDto result = crewService.relocateCrew(request);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/arrival")
    public ResponseEntity<List<CrewMemberDto>> registerArrival(@Valid @RequestBody RegisterArrivalRequest request) {
        List<CrewMemberDto> result = crewService.registerArrival(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    @GetMapping("/sections")
    public ResponseEntity<List<SectionDto>> getAllSections() {
        return ResponseEntity.ok(crewService.getAllSections());
    }
    
    @GetMapping("/sections/{id}")
    public ResponseEntity<SectionDto> getSectionById(@PathVariable Long id) {
        return ResponseEntity.ok(crewService.getSectionById(id));
    }
    
    @GetMapping("/sections/{id}/headcount")
    public ResponseEntity<Map<String, Integer>> getSectionHeadcount(@PathVariable Long id) {
        Integer headcount = crewService.getSectionHeadcount(id);
        return ResponseEntity.ok(Map.of("headcount", headcount));
    }
    
    // Exception handlers
    @ExceptionHandler(CrewService.CrewNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCrewNotFound(CrewService.CrewNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
    
    @ExceptionHandler(CrewService.SectionNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSectionNotFound(CrewService.SectionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
    
    @ExceptionHandler(CrewService.SectionAtCapacityException.class)
    public ResponseEntity<Map<String, String>> handleSectionAtCapacity(CrewService.SectionAtCapacityException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
