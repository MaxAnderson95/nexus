"""
NEXUS Station Load Generator

This Locust file simulates realistic user behavior for the space station
management system. All requests go through the CORTEX BFF at /api/*.

Features tested:
- Dashboard status aggregation
- Docking: dock/undock ships
- Crew: view roster, relocate crew members
- Life Support: environment monitoring, self-tests, adjustments, alerts
- Power: grid status, allocate/deallocate power
- Inventory: supplies, consume, resupply requests, cargo manifests
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
                    self.client.post(f"/api/docking/dock/{ship_id}",
                                    name="/api/docking/dock/{id}")
    
    @task
    def undock_ship(self):
        """Attempt to undock a docked ship"""
        response = self.client.get("/api/docking/bays",
                                   name="/api/docking/bays [undock prep]")
        if response.ok:
            bays = response.json()
            occupied = [b for b in bays if b.get("status") == "OCCUPIED" and b.get("currentShipId")]
            if occupied:
                bay = random.choice(occupied)
                ship_id = bay.get("currentShipId")
                if ship_id:
                    self.client.post(f"/api/docking/undock/{ship_id}",
                                    name="/api/docking/undock/{id}")
    
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
                    self.client.get(f"/api/crew/section/{section_id}",
                                   name="/api/crew/section/{id}")
    
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
                # Pick a crew member that's not in transit
                eligible = [c for c in roster if c.get("status") != "IN_TRANSIT"]
                if eligible:
                    crew_member = random.choice(eligible)
                    current_section = crew_member.get("sectionId")
                    # Find sections with available capacity
                    available_sections = [s for s in sections 
                                         if s.get("id") != current_section 
                                         and s.get("currentOccupancy", 0) < s.get("maxCapacity", 10)]
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
    def run_self_test(self):
        """Run self-test diagnostic on a section (2-3s delay)"""
        response = self.client.get("/api/life-support/environment",
                                   name="/api/life-support/environment [self-test prep]")
        if response.ok:
            sections = response.json()
            if sections and len(sections) > 0:
                section = random.choice(sections)
                section_id = section.get("sectionId")
                if section_id:
                    # Self-test has artificial 2-3s delay
                    with self.client.post(
                        f"/api/life-support/environment/section/{section_id}/self-test",
                        name="/api/life-support/environment/section/{id}/self-test",
                        catch_response=True,
                        timeout=10
                    ) as response:
                        if response.status_code == 200:
                            result = response.json()
                            if result.get("passed"):
                                response.success()
                            else:
                                response.success()  # Failed test is still a valid response
                        else:
                            response.failure(f"Self-test failed: {response.status_code}")
    
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
                        f"/api/life-support/environment/section/{section_id}/adjust",
                        json={
                            "targetTemperature": round(random.uniform(18.0, 24.0), 1),
                            "targetO2": round(random.uniform(20.5, 21.5), 1)
                        },
                        name="/api/life-support/environment/section/{id}/adjust")
    
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
    def view_allocations(self):
        """View current power allocations"""
        self.client.get("/api/power/allocations", name="/api/power/allocations")
    
    @task
    def allocate_power(self):
        """Allocate power to a system"""
        systems = [
            "research_lab", "medical_bay", "communications", 
            "sensors", "defense_systems", "cargo_handling",
            "recreation", "hydroponics", "fabrication"
        ]
        system = f"{random.choice(systems)}_{random.randint(1, 5)}"
        self.client.post("/api/power/allocate",
                        json={
                            "system": system,
                            "amountKw": random.randint(20, 150),
                            "priority": random.randint(3, 8)
                        },
                        name="/api/power/allocate")
    
    @task
    def deallocate_power(self):
        """Deallocate power from a system"""
        response = self.client.get("/api/power/allocations",
                                   name="/api/power/allocations [dealloc prep]")
        if response.ok:
            allocations = response.json()
            # Only deallocate lower priority systems (5+)
            deallocatable = [a for a in allocations if a.get("priority", 5) >= 5]
            if deallocatable:
                alloc = random.choice(deallocatable)
                system_name = alloc.get("systemName")
                if system_name:
                    self.client.post("/api/power/deallocate",
                                    json={"system": system_name},
                                    name="/api/power/deallocate")
        self.interrupt()


class InventoryBehavior(SequentialTaskSet):
    """User manages inventory and supplies"""
    
    @task
    def view_inventory(self):
        """View all supplies"""
        self.client.get("/api/inventory/supplies", name="/api/inventory/supplies")
    
    @task
    def consume_supplies(self):
        """Consume some supplies"""
        response = self.client.get("/api/inventory/supplies",
                                   name="/api/inventory/supplies [consume prep]")
        if response.ok:
            supplies = response.json()
            # Only consume from supplies with quantity > 10
            consumable = [s for s in supplies if s.get("quantity", 0) > 10]
            if consumable:
                supply = random.choice(consumable)
                supply_id = supply.get("id")
                max_consume = min(supply.get("quantity", 10), 20)
                if supply_id:
                    self.client.post("/api/inventory/consume",
                                    json={
                                        "supplyId": supply_id,
                                        "quantity": random.randint(1, max_consume)
                                    },
                                    name="/api/inventory/consume")
    
    @task
    def request_resupply(self):
        """Request resupply for items"""
        response = self.client.get("/api/inventory/supplies",
                                   name="/api/inventory/supplies [resupply prep]")
        if response.ok:
            supplies = response.json()
            # Prefer low stock items but occasionally resupply others
            low_stock = [s for s in supplies if s.get("isLowStock")]
            if low_stock:
                item = random.choice(low_stock)
            elif supplies:
                item = random.choice(supplies)
            else:
                return
            
            item_id = item.get("id")
            min_threshold = item.get("minThreshold", 50)
            if item_id:
                self.client.post("/api/inventory/resupply",
                                json={
                                    "supplyId": item_id,
                                    "quantity": random.randint(min_threshold, min_threshold * 3)
                                },
                                name="/api/inventory/resupply")
    
    @task
    def unload_manifests(self):
        """View and unload cargo manifests"""
        response = self.client.get("/api/inventory/cargo-manifests",
                                   name="/api/inventory/cargo-manifests")
        if response.ok:
            manifests = response.json()
            pending = [m for m in manifests if m.get("status") == "PENDING"]
            if pending:
                manifest = random.choice(pending)
                manifest_id = manifest.get("id")
                if manifest_id:
                    self.client.post(
                        f"/api/inventory/cargo-manifests/{manifest_id}/unload",
                        name="/api/inventory/cargo-manifests/{id}/unload")
    
    @task
    def view_resupply_requests(self):
        """View resupply request status"""
        self.client.get("/api/inventory/resupply-requests", 
                       name="/api/inventory/resupply-requests")
        self.interrupt()


class StationOperator(HttpUser):
    """
    Simulates a station operations controller.
    
    Behavior weights reflect realistic usage patterns:
    - Dashboard is checked most frequently
    - Docking operations are common
    - Other systems are checked less frequently
    
    Interactive features tested:
    - Docking: dock/undock ships
    - Crew: relocate crew members between sections
    - Life Support: run self-tests (2-3s delay), adjust environment, acknowledge alerts
    - Power: allocate/deallocate power to systems
    - Inventory: consume supplies, request resupply, unload cargo
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
        LifeSupportBehavior: 3,    # Higher weight for self-test coverage
        PowerBehavior: 2,          # Moderate - power management
        InventoryBehavior: 2,      # Moderate - supply tracking
    }
