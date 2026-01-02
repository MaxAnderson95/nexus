package com.nexus.cortex.controller;

import com.nexus.cortex.client.CrewClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.nexus.cortex.validation.RequestValidator.*;

@RestController
@RequestMapping("/api/crew")
public class CrewProxyController {

    private final CrewClient crewClient;

    public CrewProxyController(CrewClient crewClient) {
        this.crewClient = crewClient;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllCrew() {
        return ResponseEntity.ok(crewClient.getAllCrew());
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCrewCount() {
        return ResponseEntity.ok(crewClient.getCrewCount());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCrewById(@PathVariable Long id) {
        validatePositiveId(id, "Crew ID");
        return ResponseEntity.ok(crewClient.getCrewById(id));
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<Map<String, Object>>> getCrewBySection(@PathVariable Long sectionId) {
        validatePositiveId(sectionId, "Section ID");
        return ResponseEntity.ok(crewClient.getCrewBySection(sectionId));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Map<String, Object>>> getAvailableCrew() {
        return ResponseEntity.ok(crewClient.getAvailableCrew());
    }

    @PostMapping("/relocate")
    public ResponseEntity<Map<String, Object>> relocateCrew(@RequestBody Map<String, Object> request) {
        validateRequired(request, "crewId", "targetSectionId");
        validatePositiveNumber(request.get("crewId"), "crewId");
        validatePositiveNumber(request.get("targetSectionId"), "targetSectionId");
        return ResponseEntity.ok(crewClient.relocateCrew(request));
    }

    @PostMapping("/arrival")
    public ResponseEntity<List<Map<String, Object>>> registerArrival(@RequestBody Map<String, Object> request) {
        validateRequired(request, "shipId", "crewIds");
        validatePositiveNumber(request.get("shipId"), "shipId");
        return ResponseEntity.ok(crewClient.registerArrival(request));
    }

    @GetMapping("/sections")
    public ResponseEntity<List<Map<String, Object>>> getAllSections() {
        return ResponseEntity.ok(crewClient.getAllSections());
    }

    @GetMapping("/sections/{id}")
    public ResponseEntity<Map<String, Object>> getSectionById(@PathVariable Long id) {
        validatePositiveId(id, "Section ID");
        return ResponseEntity.ok(crewClient.getSectionById(id));
    }

    @GetMapping("/sections/{id}/headcount")
    public ResponseEntity<Map<String, Object>> getSectionHeadcount(@PathVariable Long id) {
        validatePositiveId(id, "Section ID");
        return ResponseEntity.ok(crewClient.getSectionHeadcount(id));
    }
}
