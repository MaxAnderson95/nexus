"""
NEXUS Station Load Generator

This Locust file simulates realistic user behavior for the space station
management system. All requests go through the CORTEX BFF at /api/v1/*.

Features tested:
- Dashboard: full status and individual summaries
- Docking: view bays/ships (list + detail), dock/undock ships, schedule deliveries, view logs
- Crew: view roster (list + detail), sections (list + detail), available crew, relocate crew
- Life Support: environment monitoring, self-tests, adjustments, alerts (active + all history)
- Power: grid status, sources (list + detail), allocations, allocate/deallocate power
- Inventory: supplies (list + detail + low-stock), consume, resupply, manifests (list + detail), unload
"""

from locust import HttpUser, task, between, SequentialTaskSet
import random


class DashboardBehavior(SequentialTaskSet):
    """User checks the dashboard - most common behavior"""

    @task
    def load_dashboard(self):
        """Load aggregated dashboard status"""
        with self.client.get("/api/v1/dashboard/status",
                            name="/api/v1/dashboard/status",
                            catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Dashboard failed: {response.status_code}")

    @task
    def load_docking_summary(self):
        """Load docking summary"""
        self.client.get("/api/v1/dashboard/docking", name="/api/v1/dashboard/docking")

    @task
    def load_crew_summary(self):
        """Load crew summary"""
        self.client.get("/api/v1/dashboard/crew", name="/api/v1/dashboard/crew")

    @task
    def load_life_support_summary(self):
        """Load life support summary"""
        self.client.get("/api/v1/dashboard/life-support", name="/api/v1/dashboard/life-support")

    @task
    def load_power_summary(self):
        """Load power summary"""
        self.client.get("/api/v1/dashboard/power", name="/api/v1/dashboard/power")

    @task
    def load_inventory_summary(self):
        """Load inventory summary"""
        self.client.get("/api/v1/dashboard/inventory", name="/api/v1/dashboard/inventory")
        self.interrupt()


class DockingBehavior(SequentialTaskSet):
    """User manages ship docking operations"""

    @task
    def view_bays(self):
        """View all docking bays"""
        self.client.get("/api/v1/docking/bays", name="/api/v1/docking/bays")

    @task
    def view_single_bay(self):
        """View a single docking bay"""
        response = self.client.get("/api/v1/docking/bays",
                                   name="/api/v1/docking/bays [bay detail prep]")
        if response.ok:
            bays = response.json()
            if bays and len(bays) > 0:
                bay_id = random.choice(bays).get("id")
                if bay_id:
                    self.client.get(f"/api/v1/docking/bays/{bay_id}",
                                   name="/api/v1/docking/bays/{id}")

    @task
    def view_all_ships(self):
        """View all ships"""
        self.client.get("/api/v1/docking/ships", name="/api/v1/docking/ships")

    @task
    def view_single_ship(self):
        """View a single ship's details"""
        response = self.client.get("/api/v1/docking/ships",
                                   name="/api/v1/docking/ships [ship detail prep]")
        if response.ok:
            ships = response.json()
            if ships and len(ships) > 0:
                ship_id = random.choice(ships).get("id")
                if ship_id:
                    self.client.get(f"/api/v1/docking/ships/{ship_id}",
                                   name="/api/v1/docking/ships/{id}")

    @task
    def view_incoming(self):
        """View incoming ships"""
        self.client.get("/api/v1/docking/ships/incoming",
                       name="/api/v1/docking/ships/incoming")

    @task
    def dock_ship(self):
        """Attempt to dock an incoming ship"""
        response = self.client.get("/api/v1/docking/ships/incoming",
                                   name="/api/v1/docking/ships/incoming [dock prep]")
        if response.ok:
            ships = response.json()
            if ships and len(ships) > 0:
                ship_id = ships[0].get("id")
                if ship_id:
                    self.client.post(f"/api/v1/docking/dock/{ship_id}",
                                    name="/api/v1/docking/dock/{id}")

    @task
    def undock_ship(self):
        """Attempt to undock a docked ship"""
        response = self.client.get("/api/v1/docking/bays",
                                   name="/api/v1/docking/bays [undock prep]")
        if response.ok:
            bays = response.json()
            occupied = [b for b in bays if b.get("status") == "OCCUPIED" and b.get("currentShipId")]
            if occupied:
                bay = random.choice(occupied)
                ship_id = bay.get("currentShipId")
                if ship_id:
                    self.client.post(f"/api/v1/docking/undock/{ship_id}",
                                    name="/api/v1/docking/undock/{id}")

    @task
    def schedule_delivery(self):
        """Schedule a new delivery ship"""
        import time
        ship_types = ["Cargo", "Supply", "Freighter", "Transport", "Hauler"]
        cargo_types = ["FOOD", "MEDICAL", "MECHANICAL", "ELECTRONIC", "FUEL", "WATER", "OXYGEN", "GENERAL"]
        
        ship_name = f"{random.choice(ship_types)}-{random.randint(100, 999)}"
        # Schedule arrival 1-24 hours from now
        arrival_time = int((time.time() + random.randint(3600, 86400)) * 1000)
        
        self.client.post("/api/v1/docking/schedule-delivery",
                        json={
                            "shipName": ship_name,
                            "cargoType": random.choice(cargo_types),
                            "estimatedArrival": arrival_time
                        },
                        name="/api/v1/docking/schedule-delivery")

    @task
    def view_logs(self):
        """View docking logs"""
        self.client.get("/api/v1/docking/logs", name="/api/v1/docking/logs")
        self.interrupt()


class CrewBehavior(SequentialTaskSet):
    """User manages crew roster"""

    @task
    def view_roster(self):
        """View full crew roster"""
        self.client.get("/api/v1/crew", name="/api/v1/crew")

    @task
    def view_single_member(self):
        """View a single crew member"""
        response = self.client.get("/api/v1/crew", name="/api/v1/crew [member prep]")
        if response.ok:
            roster = response.json()
            if roster and len(roster) > 0:
                member_id = random.choice(roster).get("id")
                if member_id:
                    self.client.get(f"/api/v1/crew/{member_id}",
                                   name="/api/v1/crew/{id}")

    @task
    def view_sections(self):
        """View station sections"""
        self.client.get("/api/v1/crew/sections", name="/api/v1/crew/sections")

    @task
    def view_section_members(self):
        """View members of a specific section"""
        response = self.client.get("/api/v1/crew/sections",
                                   name="/api/v1/crew/sections [members prep]")
        if response.ok:
            sections = response.json()
            if sections and len(sections) > 0:
                section_id = random.choice(sections).get("id")
                if section_id:
                    self.client.get(f"/api/v1/crew/section/{section_id}",
                                   name="/api/v1/crew/section/{id}")

    @task
    def view_single_section(self):
        """View a single section's details"""
        response = self.client.get("/api/v1/crew/sections",
                                   name="/api/v1/crew/sections [section detail prep]")
        if response.ok:
            sections = response.json()
            if sections and len(sections) > 0:
                section_id = random.choice(sections).get("id")
                if section_id:
                    self.client.get(f"/api/v1/crew/sections/{section_id}",
                                   name="/api/v1/crew/sections/{id}")

    @task
    def view_available_crew(self):
        """View available crew members"""
        self.client.get("/api/v1/crew/available", name="/api/v1/crew/available")

    @task
    def relocate_crew(self):
        """Relocate a crew member to a different section"""
        roster_resp = self.client.get("/api/v1/crew", name="/api/v1/crew [relocate prep]")
        sections_resp = self.client.get("/api/v1/crew/sections",
                                        name="/api/v1/crew/sections [relocate prep]")

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
                        self.client.post("/api/v1/crew/relocate",
                                        json={
                                            "crewId": crew_member.get("id"),
                                            "targetSectionId": target.get("id")
                                        },
                                        name="/api/v1/crew/relocate")
        self.interrupt()


class LifeSupportBehavior(SequentialTaskSet):
    """User manages life support systems"""

    @task
    def view_environment(self):
        """View environmental readings for all sections"""
        self.client.get("/api/v1/life-support/environment",
                       name="/api/v1/life-support/environment")

    @task
    def view_single_section_environment(self):
        """View environmental readings for a single section"""
        response = self.client.get("/api/v1/life-support/environment",
                                   name="/api/v1/life-support/environment [section prep]")
        if response.ok:
            sections = response.json()
            if sections and len(sections) > 0:
                section_id = random.choice(sections).get("sectionId")
                if section_id:
                    self.client.get(f"/api/v1/life-support/environment/section/{section_id}",
                                   name="/api/v1/life-support/environment/section/{id}")

    @task
    def run_self_test(self):
        """Run self-test diagnostic on a section (2-3s delay)"""
        response = self.client.get("/api/v1/life-support/environment",
                                   name="/api/v1/life-support/environment [self-test prep]")
        if response.ok:
            sections = response.json()
            if sections and len(sections) > 0:
                section = random.choice(sections)
                section_id = section.get("sectionId")
                if section_id:
                    # Self-test has artificial 2-3s delay
                    with self.client.post(
                        f"/api/v1/life-support/environment/section/{section_id}/self-test",
                        name="/api/v1/life-support/environment/section/{id}/self-test",
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
        response = self.client.get("/api/v1/life-support/environment",
                                   name="/api/v1/life-support/environment [adjust prep]")
        if response.ok:
            sections = response.json()
            if sections and len(sections) > 0:
                section = random.choice(sections)
                section_id = section.get("sectionId")
                if section_id:
                    self.client.post(
                        f"/api/v1/life-support/environment/section/{section_id}/adjust",
                        json={
                            "targetTemperature": round(random.uniform(18.0, 24.0), 1),
                            "targetO2": round(random.uniform(20.5, 21.5), 1)
                        },
                        name="/api/v1/life-support/environment/section/{id}/adjust")

    @task
    def check_alerts(self):
        """Check and acknowledge alerts"""
        response = self.client.get("/api/v1/life-support/alerts",
                                   name="/api/v1/life-support/alerts")
        if response.ok:
            alerts = response.json()
            unacknowledged = [a for a in alerts if not a.get("acknowledged")]
            if unacknowledged:
                alert = random.choice(unacknowledged)
                alert_id = alert.get("id")
                if alert_id:
                    self.client.post(
                        f"/api/v1/life-support/alerts/{alert_id}/acknowledge",
                        name="/api/v1/life-support/alerts/{id}/acknowledge")

    @task
    def view_all_alerts(self):
        """View all alerts including acknowledged ones"""
        self.client.get("/api/v1/life-support/alerts/all",
                       name="/api/v1/life-support/alerts/all")
        self.interrupt()


class PowerBehavior(SequentialTaskSet):
    """User manages power grid"""

    @task
    def view_grid(self):
        """View power grid status"""
        self.client.get("/api/v1/power/grid", name="/api/v1/power/grid")

    @task
    def view_sources(self):
        """View power sources"""
        self.client.get("/api/v1/power/sources", name="/api/v1/power/sources")

    @task
    def view_single_source(self):
        """View a single power source's details"""
        response = self.client.get("/api/v1/power/sources",
                                   name="/api/v1/power/sources [source detail prep]")
        if response.ok:
            data = response.json()
            sources = data.get("sources", [])
            if sources and len(sources) > 0:
                source_id = random.choice(sources).get("id")
                if source_id:
                    self.client.get(f"/api/v1/power/sources/{source_id}",
                                   name="/api/v1/power/sources/{id}")

    @task
    def view_allocations(self):
        """View current power allocations"""
        self.client.get("/api/v1/power/allocations", name="/api/v1/power/allocations")

    @task
    def allocate_power(self):
        """Allocate power to a system"""
        systems = [
            "research_lab", "medical_bay", "communications",
            "sensors", "defense_systems", "cargo_handling",
            "recreation", "hydroponics", "fabrication"
        ]
        system = f"{random.choice(systems)}_{random.randint(1, 5)}"
        self.client.post("/api/v1/power/allocate",
                        json={
                            "system": system,
                            "amountKw": random.randint(20, 150),
                            "priority": random.randint(3, 8)
                        },
                        name="/api/v1/power/allocate")

    @task
    def deallocate_power(self):
        """Deallocate power from a system"""
        response = self.client.get("/api/v1/power/allocations",
                                   name="/api/v1/power/allocations [dealloc prep]")
        if response.ok:
            allocations = response.json()
            # Only deallocate lower priority systems (5+)
            deallocatable = [a for a in allocations if a.get("priority", 5) >= 5]
            if deallocatable:
                alloc = random.choice(deallocatable)
                system_name = alloc.get("systemName")
                if system_name:
                    self.client.post("/api/v1/power/deallocate",
                                    json={"system": system_name},
                                    name="/api/v1/power/deallocate")
        self.interrupt()


class InventoryBehavior(SequentialTaskSet):
    """User manages inventory and supplies"""

    @task
    def view_inventory(self):
        """View all supplies"""
        self.client.get("/api/v1/inventory/supplies", name="/api/v1/inventory/supplies")

    @task
    def view_single_supply(self):
        """View a single supply item"""
        response = self.client.get("/api/v1/inventory/supplies",
                                   name="/api/v1/inventory/supplies [detail prep]")
        if response.ok:
            supplies = response.json()
            if supplies and len(supplies) > 0:
                supply_id = random.choice(supplies).get("id")
                if supply_id:
                    self.client.get(f"/api/v1/inventory/supplies/{supply_id}",
                                   name="/api/v1/inventory/supplies/{id}")

    @task
    def view_low_stock(self):
        """View low stock items"""
        self.client.get("/api/v1/inventory/supplies/low-stock",
                       name="/api/v1/inventory/supplies/low-stock")

    @task
    def consume_supplies(self):
        """Consume some supplies"""
        response = self.client.get("/api/v1/inventory/supplies",
                                   name="/api/v1/inventory/supplies [consume prep]")
        if response.ok:
            supplies = response.json()
            # Only consume from supplies with quantity > 10
            consumable = [s for s in supplies if s.get("quantity", 0) > 10]
            if consumable:
                supply = random.choice(consumable)
                supply_id = supply.get("id")
                max_consume = min(supply.get("quantity", 10), 20)
                if supply_id:
                    self.client.post("/api/v1/inventory/consume",
                                    json={
                                        "supplyId": supply_id,
                                        "quantity": random.randint(1, max_consume)
                                    },
                                    name="/api/v1/inventory/consume")

    @task
    def request_resupply(self):
        """Request resupply for items"""
        response = self.client.get("/api/v1/inventory/supplies",
                                   name="/api/v1/inventory/supplies [resupply prep]")
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
                self.client.post("/api/v1/inventory/resupply",
                                json={
                                    "supplyId": item_id,
                                    "quantity": random.randint(min_threshold, min_threshold * 3)
                                },
                                name="/api/v1/inventory/resupply")

    @task
    def view_resupply_requests(self):
        """View resupply request status"""
        self.client.get("/api/v1/inventory/resupply-requests",
                       name="/api/v1/inventory/resupply-requests")

    @task
    def view_manifests(self):
        """View cargo manifests"""
        self.client.get("/api/v1/inventory/cargo-manifests",
                       name="/api/v1/inventory/cargo-manifests")

    @task
    def view_single_manifest(self):
        """View a single cargo manifest's details"""
        response = self.client.get("/api/v1/inventory/cargo-manifests",
                                   name="/api/v1/inventory/cargo-manifests [manifest detail prep]")
        if response.ok:
            manifests = response.json()
            if manifests and len(manifests) > 0:
                manifest_id = random.choice(manifests).get("id")
                if manifest_id:
                    self.client.get(f"/api/v1/inventory/cargo-manifests/{manifest_id}",
                                   name="/api/v1/inventory/cargo-manifests/{id}")

    @task
    def unload_manifests(self):
        """Unload pending cargo manifests"""
        response = self.client.get("/api/v1/inventory/cargo-manifests",
                                   name="/api/v1/inventory/cargo-manifests [unload prep]")
        if response.ok:
            manifests = response.json()
            pending = [m for m in manifests if m.get("status") == "PENDING"]
            if pending:
                manifest = random.choice(pending)
                manifest_id = manifest.get("id")
                if manifest_id:
                    self.client.post(
                        f"/api/v1/inventory/cargo-manifests/{manifest_id}/unload",
                        name="/api/v1/inventory/cargo-manifests/{id}/unload")
        self.interrupt()


class StationOperator(HttpUser):
    """
    Simulates a station operations controller.

    Behavior weights reflect realistic usage patterns:
    - Dashboard is checked most frequently
    - Docking operations are common
    - Other systems are checked less frequently

    All UI interactions covered:
    - Dashboard: full status + individual summaries (docking, crew, life-support, power, inventory)
    - Docking: view bays/ships (list + detail), dock/undock ships, schedule deliveries, view logs
    - Crew: view roster (list + detail), sections (list + detail), available crew, section members, relocate crew
    - Life Support: environment (all + single section), self-tests, adjustments, alerts (active + all history)
    - Power: grid status, sources (list + detail), allocations, allocate/deallocate
    - Inventory: supplies (list + detail + low-stock), consume, resupply, manifests (list + detail), unload
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
