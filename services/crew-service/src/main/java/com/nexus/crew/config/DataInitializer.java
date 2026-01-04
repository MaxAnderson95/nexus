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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String INIT_LOCK_KEY = "init:lock:crew";
    private static final Duration LOCK_TTL = Duration.ofMinutes(5);
    private static final Duration LOCK_WAIT_TIMEOUT = Duration.ofSeconds(30);

    private final SectionRepository sectionRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final CrewAssignmentRepository crewAssignmentRepository;
    private final EntityManager entityManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final Random random = new Random();
    private final String instanceId;

    public DataInitializer(SectionRepository sectionRepository,
                          CrewMemberRepository crewMemberRepository,
                          CrewAssignmentRepository crewAssignmentRepository,
                          EntityManager entityManager,
                          RedisTemplate<String, String> redisTemplate) {
        this.sectionRepository = sectionRepository;
        this.crewMemberRepository = crewMemberRepository;
        this.crewAssignmentRepository = crewAssignmentRepository;
        this.entityManager = entityManager;
        this.redisTemplate = redisTemplate;
        this.instanceId = UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Checking demo data for Crew Service...");

        if (!acquireInitLock()) {
            log.info("Another instance is initializing data, waiting for completion...");
            waitForInitCompletion();
            log.info("Crew Service demo data check complete (initialized by another instance)");
            return;
        }

        try {
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
        } finally {
            releaseInitLock();
        }

        log.info("Crew Service demo data check complete");
    }

    private boolean acquireInitLock() {
        try {
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(INIT_LOCK_KEY, instanceId, LOCK_TTL);
            return Boolean.TRUE.equals(acquired);
        } catch (Exception e) {
            log.warn("Failed to acquire Redis lock, proceeding without lock: {}", e.getMessage());
            return true;
        }
    }

    private void releaseInitLock() {
        try {
            redisTemplate.delete(INIT_LOCK_KEY);
        } catch (Exception e) {
            log.warn("Failed to release Redis lock: {}", e.getMessage());
        }
    }

    private void waitForInitCompletion() {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < LOCK_WAIT_TIMEOUT.toMillis()) {
            try {
                Thread.sleep(500);
                if (!Boolean.TRUE.equals(redisTemplate.hasKey(INIT_LOCK_KEY))) {
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        log.warn("Timeout waiting for initialization lock to be released");
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
        // Section IDs must match life-support service section IDs (1-8)
        // Life-support: 1=Bridge, 2=Engineering, 3=Habitation Deck A, 4=Habitation Deck B,
        //               5=Medical Bay, 6=Science Lab, 7=Cargo Hold, 8=Maintenance Bay

        // ID 1: Bridge/Command Center - Deck 1
        Section command = new Section();
        command.setId(1L);
        command.setName("Command Center");
        command.setDeck(1);
        command.setMaxCapacity(20);
        command.setCurrentOccupancy(0);
        sectionRepository.save(command);

        // ID 2: Engineering - Deck 2
        Section engineering = new Section();
        engineering.setId(2L);
        engineering.setName("Engineering Bay");
        engineering.setDeck(2);
        engineering.setMaxCapacity(30);
        engineering.setCurrentOccupancy(0);
        sectionRepository.save(engineering);

        // ID 3: Habitation Deck A / Crew Quarters Alpha - Deck 4
        Section quartersA = new Section();
        quartersA.setId(3L);
        quartersA.setName("Crew Quarters Alpha");
        quartersA.setDeck(4);
        quartersA.setMaxCapacity(40);
        quartersA.setCurrentOccupancy(0);
        sectionRepository.save(quartersA);

        // ID 4: Habitation Deck B / Crew Quarters Beta - Deck 4
        Section quartersB = new Section();
        quartersB.setId(4L);
        quartersB.setName("Crew Quarters Beta");
        quartersB.setDeck(4);
        quartersB.setMaxCapacity(40);
        quartersB.setCurrentOccupancy(0);
        sectionRepository.save(quartersB);

        // ID 5: Medical Bay - Deck 3
        Section medical = new Section();
        medical.setId(5L);
        medical.setName("Medical Bay");
        medical.setDeck(3);
        medical.setMaxCapacity(15);
        medical.setCurrentOccupancy(0);
        sectionRepository.save(medical);

        // ID 6: Science Lab - Deck 3
        Section scienceLabs = new Section();
        scienceLabs.setId(6L);
        scienceLabs.setName("Science Labs");
        scienceLabs.setDeck(3);
        scienceLabs.setMaxCapacity(25);
        scienceLabs.setCurrentOccupancy(0);
        sectionRepository.save(scienceLabs);

        // ID 7: Cargo Hold/Bay - Deck 5
        Section cargo = new Section();
        cargo.setId(7L);
        cargo.setName("Cargo Bay");
        cargo.setDeck(5);
        cargo.setMaxCapacity(10);
        cargo.setCurrentOccupancy(0);
        sectionRepository.save(cargo);

        // ID 8: Maintenance Bay / Docking Section - Deck 5
        Section docking = new Section();
        docking.setId(8L);
        docking.setName("Docking Section");
        docking.setDeck(5);
        docking.setMaxCapacity(15);
        docking.setCurrentOccupancy(0);
        sectionRepository.save(docking);

        log.info("Created 8 sections with IDs 1-8 matching life-support service");
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
            
            // Assign to section based on role - sections are ordered by ID 1-8
            // ID 1: Command, ID 2: Engineering, ID 3: Quarters Alpha, ID 4: Quarters Beta,
            // ID 5: Medical, ID 6: Science Labs, ID 7: Cargo, ID 8: Docking
            Section section;
            if (i < 5) {
                section = findSectionById(sections, 1L); // Command for top officers
            } else if (crew.getRole().contains("Engineer") || crew.getRole().equals("Technician")) {
                section = findSectionById(sections, 2L); // Engineering
            } else if (crew.getRole().contains("Science")) {
                section = findSectionById(sections, 6L); // Science Labs
            } else if (crew.getRole().contains("Medical")) {
                section = findSectionById(sections, 5L); // Medical Bay
            } else if (crew.getRole().contains("Security")) {
                section = findSectionById(sections, 3L); // Quarters Alpha
            } else {
                // General crew - distribute among quarters (ID 3 and 4)
                section = findSectionById(sections, random.nextBoolean() ? 3L : 4L);
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

    private Section findSectionById(List<Section> sections, Long id) {
        return sections.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Section not found with ID: " + id));
    }
}
