package com.nexus.cortex.dto;

import com.nexus.cortex.client.CrewClient.CrewSummary;
import com.nexus.cortex.client.DockingClient.DockingSummary;
import com.nexus.cortex.client.InventoryClient.InventorySummary;
import com.nexus.cortex.client.LifeSupportClient.LifeSupportSummary;
import com.nexus.cortex.client.PowerClient.PowerSummary;

import java.time.Instant;

public record DashboardStatus(
    DockingSummary docking,
    CrewSummary crew,
    LifeSupportSummary lifeSupport,
    PowerSummary power,
    InventorySummary inventory,
    String overallStatus,
    Instant timestamp
) {
    public static DashboardStatus of(
            DockingSummary docking,
            CrewSummary crew,
            LifeSupportSummary lifeSupport,
            PowerSummary power,
            InventorySummary inventory
    ) {
        String overallStatus = calculateOverallStatus(docking, crew, lifeSupport, power, inventory);
        return new DashboardStatus(
                docking,
                crew,
                lifeSupport,
                power,
                inventory,
                overallStatus,
                Instant.now()
        );
    }
    
    private static String calculateOverallStatus(
            DockingSummary docking,
            CrewSummary crew,
            LifeSupportSummary lifeSupport,
            PowerSummary power,
            InventorySummary inventory
    ) {
        int criticalCount = 0;
        int warningCount = 0;
        
        // Check life support
        if (lifeSupport != null) {
            criticalCount += lifeSupport.sectionsCritical();
            warningCount += lifeSupport.sectionsWarning();
            if (lifeSupport.activeAlerts() > 0) {
                warningCount++;
            }
        }
        
        // Check power
        if (power != null) {
            if (power.utilizationPercent() > 90) criticalCount++;
            else if (power.utilizationPercent() > 75) warningCount++;
        }
        
        // Check inventory
        if (inventory != null && inventory.lowStockItems() > 3) {
            warningCount++;
        }
        
        // Check docking capacity
        if (docking != null && docking.availableBays() == 0 && docking.incomingShips() > 0) {
            warningCount++;
        }
        
        if (criticalCount > 0) {
            return "CRITICAL";
        } else if (warningCount > 2) {
            return "WARNING";
        } else {
            return "NOMINAL";
        }
    }
}
