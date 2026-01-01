"""
NEXUS Station Load Generator

This Locust file simulates realistic user behavior for the space station
management system. All requests go through the CORTEX BFF at /api/*.
"""

from locust import HttpUser, task, between, SequentialTaskSet
import random


class DashboardBehavior(SequentialTaskSet):
    """User checks the dashboard - most common behavior"""
    
    @task
    def load_dashboard(self):
        """Load aggregated dashboard status"""
        with self.client.get("/api/dashboard/status", 
                            name="/api/dashboard/status",
                            catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Dashboard failed: {response.status_code}")
        self.interrupt()


class DockingBehavior(SequentialTaskSet):
    """User manages ship docking operations"""
    
    @task
    def view_bays(self):
        """View all docking bays"""
        self.client.get("/api/docking/bays", name="/api/docking/bays")
    
    @task
    def view_incoming(self):
        """View incoming ships"""
        self.client.get("/api/docking/ships/incoming", 
                       name="/api/docking/ships/incoming")
    
    @task
    def dock_ship(self):
        """Attempt to dock an incoming ship"""
        response = self.client.get("/api/docking/ships/incoming",
                                   name="/api/docking/ships/incoming [dock prep]")
        if response.ok:
            ships = response.json()
            if ships and len(ships) > 0:
                ship_id = ships[0].get("id")
                if ship_id:
                    self.client.post(f"/api/docking/ships/{ship_id}/dock",
                                    name="/api/docking/ships/{id}/dock")
    
    @task
    def view_logs(self):
        """View docking logs"""
        self.client.get("/api/docking/logs", name="/api/docking/logs")
        self.interrupt()


class CrewBehavior(SequentialTaskSet):
    """User manages crew roster"""
    
    @task
    def view_roster(self):
        """View full crew roster"""
        self.client.get("/api/crew", name="/api/crew")
    
    @task
    def view_sections(self):
        """View station sections"""
        self.client.get("/api/crew/sections", name="/api/crew/sections")
    
    @task
    def view_section_members(self):
        """View members of a specific section"""
        response = self.client.get("/api/crew/sections",
                                   name="/api/crew/sections [members prep]")
        if response.ok:
            sections = response.json()
            if sections and len(sections) > 0:
                section_id = random.choice(sections).get("id")
                if section_id:
                    self.client.get(f"/api/crew/sections/{section_id}/members",
                                   name="/api/crew/sections/{id}/members")
    
    @task
    def relocate_crew(self):
        """Relocate a crew member to a different section"""
        roster_resp = self.client.get("/api/crew", name="/api/crew [relocate prep]")
        sections_resp = self.client.get("/api/crew/sections",
                                        name="/api/crew/sections [relocate prep]")
        
        if roster_resp.ok and sections_resp.ok:
            roster = roster_resp.json()
            sections = sections_resp.json()
            
            if roster and len(sections) > 1:
                crew_member = random.choice(roster)
                current_section = crew_member.get("sectionId")
                available_sections = [s for s in sections 
                                     if s.get("id") != current_section]
                if available_sections:
                    target = random.choice(available_sections)
                    self.client.post("/api/crew/relocate",
                                    json={
                                        "crewId": crew_member.get("id"),
                                        "targetSectionId": target.get("id")
                                    },
                                    name="/api/crew/relocate")
        self.interrupt()


class LifeSupportBehavior(SequentialTaskSet):
    """User manages life support systems"""
    
    @task
    def view_environment(self):
        """View environmental readings for all sections"""
        self.client.get("/api/life-support/environment",
                       name="/api/life-support/environment")
    
    @task
    def view_section(self):
        """View environment for a specific section"""
        response = self.client.get("/api/life-support/environment",
                                   name="/api/life-support/environment [section prep]")
        if response.ok:
            sections = response.json()
            if sections and len(sections) > 0:
                section_id = random.choice(sections).get("sectionId")
                if section_id:
                    self.client.get(
                        f"/api/life-support/environment/sections/{section_id}",
                        name="/api/life-support/environment/sections/{id}")
    
    @task
    def adjust_environment(self):
        """Adjust environmental settings for a section"""
        response = self.client.get("/api/life-support/environment",
                                   name="/api/life-support/environment [adjust prep]")
        if response.ok:
            sections = response.json()
            if sections and len(sections) > 0:
                section = random.choice(sections)
                section_id = section.get("sectionId")
                if section_id:
                    self.client.post(
                        f"/api/life-support/environment/sections/{section_id}/adjust",
                        json={
                            "targetTemperature": random.randint(18, 24),
                            "targetO2": round(random.uniform(20.5, 21.5), 1)
                        },
                        name="/api/life-support/environment/sections/{id}/adjust")
    
    @task
    def check_alerts(self):
        """Check and acknowledge alerts"""
        response = self.client.get("/api/life-support/alerts",
                                   name="/api/life-support/alerts")
        if response.ok:
            alerts = response.json()
            unacknowledged = [a for a in alerts if not a.get("acknowledged")]
            if unacknowledged:
                alert = random.choice(unacknowledged)
                alert_id = alert.get("id")
                if alert_id:
                    self.client.post(
                        f"/api/life-support/alerts/{alert_id}/acknowledge",
                        name="/api/life-support/alerts/{id}/acknowledge")
        self.interrupt()


class PowerBehavior(SequentialTaskSet):
    """User manages power grid"""
    
    @task
    def view_grid(self):
        """View power grid status"""
        self.client.get("/api/power/grid", name="/api/power/grid")
    
    @task
    def view_sources(self):
        """View all power sources"""
        self.client.get("/api/power/sources", name="/api/power/sources")
    
    @task
    def view_source_detail(self):
        """View details of a specific power source"""
        response = self.client.get("/api/power/sources",
                                   name="/api/power/sources [detail prep]")
        if response.ok:
            sources = response.json()
            if sources and len(sources) > 0:
                source_id = random.choice(sources).get("id")
                if source_id:
                    self.client.get(f"/api/power/sources/{source_id}",
                                   name="/api/power/sources/{id}")
    
    @task
    def allocate_power(self):
        """Allocate power to a system"""
        systems = ["life_support", "docking", "sensors", "communications", 
                  "research", "defense"]
        self.client.post("/api/power/allocate",
                        json={
                            "system": random.choice(systems),
                            "amountKw": random.randint(10, 100)
                        },
                        name="/api/power/allocate")
        self.interrupt()


class InventoryBehavior(SequentialTaskSet):
    """User manages inventory and supplies"""
    
    @task
    def view_inventory(self):
        """View all supplies"""
        self.client.get("/api/inventory/supplies", name="/api/inventory/supplies")
    
    @task
    def check_low_stock(self):
        """Check low stock items"""
        self.client.get("/api/inventory/low-stock", name="/api/inventory/low-stock")
    
    @task
    def request_resupply(self):
        """Request resupply for low stock items"""
        response = self.client.get("/api/inventory/low-stock",
                                   name="/api/inventory/low-stock [resupply prep]")
        if response.ok:
            low_stock = response.json()
            if low_stock and len(low_stock) > 0:
                item = random.choice(low_stock)
                item_id = item.get("id")
                if item_id:
                    self.client.post("/api/inventory/resupply",
                                    json={
                                        "supplyId": item_id,
                                        "quantity": random.randint(50, 200)
                                    },
                                    name="/api/inventory/resupply")
    
    @task
    def view_manifests(self):
        """View and unload cargo manifests"""
        response = self.client.get("/api/inventory/cargo/manifests",
                                   name="/api/inventory/cargo/manifests")
        if response.ok:
            manifests = response.json()
            pending = [m for m in manifests if m.get("status") == "PENDING"]
            if pending:
                manifest = random.choice(pending)
                manifest_id = manifest.get("id")
                if manifest_id:
                    self.client.post(
                        f"/api/inventory/cargo/manifests/{manifest_id}/unload",
                        name="/api/inventory/cargo/manifests/{id}/unload")
        self.interrupt()


class StationOperator(HttpUser):
    """
    Simulates a station operations controller.
    
    Behavior weights reflect realistic usage patterns:
    - Dashboard is checked most frequently
    - Docking operations are common
    - Other systems are checked less frequently
    """
    
    # Wait between 1-5 seconds between tasks
    wait_time = between(1, 5)
    
    # Default host (overridden by LOCUST_HOST env var)
    host = "http://nexus.local"
    
    # Task weights determine frequency
    tasks = {
        DashboardBehavior: 5,      # Most common - checking status
        DockingBehavior: 3,        # Frequent - ship operations
        CrewBehavior: 2,           # Moderate - crew management
        LifeSupportBehavior: 2,    # Moderate - environmental checks
        PowerBehavior: 2,          # Moderate - power management
        InventoryBehavior: 2,      # Moderate - supply tracking
    }
