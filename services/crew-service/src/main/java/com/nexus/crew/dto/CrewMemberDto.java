package com.nexus.crew.dto;

import com.nexus.crew.entity.CrewMember;

import java.time.Instant;

public record CrewMemberDto(
    Long id,
    String name,
    String rank,
    String role,
    Long sectionId,
    String sectionName,
    String status,
    Instant arrivedAt,
    Instant createdAt,
    Instant updatedAt
) {
    public static CrewMemberDto fromEntity(CrewMember entity, String sectionName) {
        return new CrewMemberDto(
            entity.getId(),
            entity.getName(),
            entity.getRank(),
            entity.getRole(),
            entity.getSectionId(),
            sectionName,
            entity.getStatus().name(),
            entity.getArrivedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
