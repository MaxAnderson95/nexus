package com.nexus.lifesupport.dto;

import java.time.Instant;
import java.util.List;

public record SelfTestResult(
    Long sectionId,
    String sectionName,
    boolean passed,
    String overallStatus,
    List<SubsystemTest> subsystems,
    long durationMs,
    Instant timestamp
) {
    public record SubsystemTest(
        String name,
        boolean passed,
        String message
    ) {}
    
    public static SelfTestResult success(Long sectionId, String sectionName, 
            List<SubsystemTest> subsystems, long durationMs) {
        return new SelfTestResult(
            sectionId, sectionName, true, "PASSED", subsystems, durationMs, Instant.now()
        );
    }
    
    public static SelfTestResult failure(Long sectionId, String sectionName, 
            List<SubsystemTest> subsystems, long durationMs) {
        return new SelfTestResult(
            sectionId, sectionName, false, "FAILED", subsystems, durationMs, Instant.now()
        );
    }
}
