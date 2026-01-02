package com.nexus.crew.config;

import com.nexus.crew.entity.CrewMember;
import com.nexus.crew.entity.Section;
import com.nexus.crew.repository.CrewAssignmentRepository;
import com.nexus.crew.repository.CrewMemberRepository;
import com.nexus.crew.repository.SectionRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Component
public class DataInitializer implements ApplicationRunner {
    
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    
    private final SectionRepository sectionRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final CrewAssignmentRepository crewAssignmentRepository;
    private final EntityManager entityManager;
    private final Random random = new Random();
    
    public DataInitializer(SectionRepository sectionRepository,
                          CrewMemberRepository crewMemberRepository,
                          CrewAssignmentRepository crewAssignmentRepository,
                          EntityManager entityManager) {
        this.sectionRepository = sectionRepository;
        this.crewMemberRepository = crewMemberRepository;
        this.crewAssignmentRepository = crewAssignmentRepository;
        this.entityManager = entityManager;
    }
    
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Checking demo data for Crew Service...");
        
        if (sectionRepository.count() == 0) {
            log.info("Initializing sections...");
            initializeSections();
        } else {
            log.info("Sections already exist, skipping initialization");
        }
        
        if (crewMemberRepository.count() == 0) {
            log.info("Initializing crew members...");
            initializeCrewMembers();
        } else {
            log.info("Crew members already exist, skipping initialization");
        }
        
        log.info("Crew Service demo data check complete");
    }
    
    /**
     * Resets all tables and re-initializes demo data.
     */
    @Transactional
    public void resetTables() {
        log.info("Resetting Crew Service tables...");
        
        // Delete in order respecting foreign key constraints (batch delete)
        crewAssignmentRepository.deleteAllInBatch();
        crewMemberRepository.deleteAllInBatch();
        sectionRepository.deleteAllInBatch();
        
        // Flush to ensure deletes are committed before inserts
        entityManager.flush();
        entityManager.clear();
        
        // Re-initialize demo data
        initializeSections();
        initializeCrewMembers();
        
        log.info("Crew Service tables reset complete");
    }
    
    private void initializeSections() {
        // Command Section - Deck 1
        Section command = new Section();
        command.setName("Command Center");
        command.setDeck(1);
        command.setMaxCapacity(20);
        command.setCurrentOccupancy(0);
        sectionRepository.save(command);
        
        // Engineering - Deck 2
        Section engineering = new Section();
        engineering.setName("Engineering Bay");
        engineering.setDeck(2);
        engineering.setMaxCapacity(30);
        engineering.setCurrentOccupancy(0);
        sectionRepository.save(engineering);
        
        // Science Labs - Deck 3
        Section scienceLabs = new Section();
        scienceLabs.setName("Science Labs");
        scienceLabs.setDeck(3);
        scienceLabs.setMaxCapacity(25);
        scienceLabs.setCurrentOccupancy(0);
        sectionRepository.save(scienceLabs);
        
        // Medical Bay - Deck 3
        Section medical = new Section();
        medical.setName("Medical Bay");
        medical.setDeck(3);
        medical.setMaxCapacity(15);
        medical.setCurrentOccupancy(0);
        sectionRepository.save(medical);
        
        // Crew Quarters A - Deck 4
        Section quartersA = new Section();
        quartersA.setName("Crew Quarters Alpha");
        quartersA.setDeck(4);
        quartersA.setMaxCapacity(40);
        quartersA.setCurrentOccupancy(0);
        sectionRepository.save(quartersA);
        
        // Crew Quarters B - Deck 4
        Section quartersB = new Section();
        quartersB.setName("Crew Quarters Beta");
        quartersB.setDeck(4);
        quartersB.setMaxCapacity(40);
        quartersB.setCurrentOccupancy(0);
        sectionRepository.save(quartersB);
        
        // Cargo Bay - Deck 5
        Section cargo = new Section();
        cargo.setName("Cargo Bay");
        cargo.setDeck(5);
        cargo.setMaxCapacity(10);
        cargo.setCurrentOccupancy(0);
        sectionRepository.save(cargo);
        
        // Docking Section - Deck 5
        Section docking = new Section();
        docking.setName("Docking Section");
        docking.setDeck(5);
        docking.setMaxCapacity(15);
        docking.setCurrentOccupancy(0);
        sectionRepository.save(docking);
        
        log.info("Created 8 sections");
    }
    
    private void initializeCrewMembers() {
        List<Section> sections = sectionRepository.findAll();
        
        String[] ranks = {"Admiral", "Captain", "Commander", "Lieutenant Commander", 
                         "Lieutenant", "Ensign", "Chief Petty Officer", "Petty Officer"};
        
        String[] roles = {"Station Commander", "Executive Officer", "Chief Engineer", 
                         "Chief Science Officer", "Chief Medical Officer", "Security Chief",
                         "Navigation Officer", "Communications Officer", "Engineer",
                         "Science Officer", "Medical Officer", "Security Officer",
                         "Technician", "Specialist", "General Duty"};
        
        String[] firstNames = {"James", "Sarah", "Michael", "Elena", "David", "Lisa",
                              "Robert", "Maria", "William", "Jennifer", "Thomas", "Anna",
                              "Richard", "Emily", "Charles", "Sophia", "Daniel", "Olivia",
                              "Marcus", "Zara", "Viktor", "Yuki", "Hassan", "Priya"};
        
        String[] lastNames = {"Chen", "Rodriguez", "Nakamura", "Patel", "Kowalski",
                             "O'Brien", "Johansson", "Kim", "Mueller", "Santos",
                             "Volkov", "Hassan", "Andersson", "Park", "Singh",
                             "Yamamoto", "Hernandez", "Svensson", "Okonkwo", "Jensen"};
        
        CrewMember.CrewStatus[] statuses = CrewMember.CrewStatus.values();
        
        int crewCount = 0;
        for (int i = 0; i < 50; i++) {
            CrewMember crew = new CrewMember();
            
            String firstName = firstNames[random.nextInt(firstNames.length)];
            String lastName = lastNames[random.nextInt(lastNames.length)];
            crew.setName(firstName + " " + lastName);
            
            // Higher ranks for first few crew members
            if (i < 5) {
                crew.setRank(ranks[i]);
                crew.setRole(roles[i]);
            } else {
                crew.setRank(ranks[4 + random.nextInt(ranks.length - 4)]);
                crew.setRole(roles[6 + random.nextInt(roles.length - 6)]);
            }
            
            // Assign to section based on role
            Section section;
            if (i < 5) {
                section = sections.get(0); // Command for top officers
            } else if (crew.getRole().contains("Engineer") || crew.getRole().equals("Technician")) {
                section = sections.get(1); // Engineering
            } else if (crew.getRole().contains("Science")) {
                section = sections.get(2); // Science Labs
            } else if (crew.getRole().contains("Medical")) {
                section = sections.get(3); // Medical Bay
            } else if (crew.getRole().contains("Security")) {
                section = sections.stream()
                        .filter(s -> s.getName().contains("Quarters"))
                        .findFirst()
                        .orElse(sections.get(4));
            } else {
                // General crew - distribute among quarters
                section = random.nextBoolean() ? sections.get(4) : sections.get(5);
            }
            
            crew.setSectionId(section.getId());
            
            // Most crew are active
            if (i < 40) {
                crew.setStatus(CrewMember.CrewStatus.ACTIVE);
            } else {
                crew.setStatus(statuses[1 + random.nextInt(statuses.length - 1)]);
            }
            
            // Stagger arrival times
            crew.setArrivedAt(Instant.now().minus(random.nextInt(365), ChronoUnit.DAYS));
            
            crewMemberRepository.save(crew);
            
            // Update section occupancy
            section.setCurrentOccupancy(section.getCurrentOccupancy() + 1);
            sectionRepository.save(section);
            
            crewCount++;
        }
        
        log.info("Created {} crew members", crewCount);
    }
}
