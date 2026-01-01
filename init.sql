-- =============================================================================
-- NEXUS STATION Database Initialization
-- =============================================================================
-- This script creates the schemas for each service.
-- Each service is responsible for creating its own tables and seeding demo data
-- on startup (via Spring Boot DataInitializer).
-- =============================================================================

-- Create schemas for each service
CREATE SCHEMA IF NOT EXISTS power;
CREATE SCHEMA IF NOT EXISTS life_support;
CREATE SCHEMA IF NOT EXISTS crew;
CREATE SCHEMA IF NOT EXISTS docking;
CREATE SCHEMA IF NOT EXISTS inventory;

-- Grant permissions to the nexus user
GRANT ALL PRIVILEGES ON SCHEMA power TO nexus;
GRANT ALL PRIVILEGES ON SCHEMA life_support TO nexus;
GRANT ALL PRIVILEGES ON SCHEMA crew TO nexus;
GRANT ALL PRIVILEGES ON SCHEMA docking TO nexus;
GRANT ALL PRIVILEGES ON SCHEMA inventory TO nexus;

-- Grant default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA power GRANT ALL ON TABLES TO nexus;
ALTER DEFAULT PRIVILEGES IN SCHEMA life_support GRANT ALL ON TABLES TO nexus;
ALTER DEFAULT PRIVILEGES IN SCHEMA crew GRANT ALL ON TABLES TO nexus;
ALTER DEFAULT PRIVILEGES IN SCHEMA docking GRANT ALL ON TABLES TO nexus;
ALTER DEFAULT PRIVILEGES IN SCHEMA inventory GRANT ALL ON TABLES TO nexus;

ALTER DEFAULT PRIVILEGES IN SCHEMA power GRANT ALL ON SEQUENCES TO nexus;
ALTER DEFAULT PRIVILEGES IN SCHEMA life_support GRANT ALL ON SEQUENCES TO nexus;
ALTER DEFAULT PRIVILEGES IN SCHEMA crew GRANT ALL ON SEQUENCES TO nexus;
ALTER DEFAULT PRIVILEGES IN SCHEMA docking GRANT ALL ON SEQUENCES TO nexus;
ALTER DEFAULT PRIVILEGES IN SCHEMA inventory GRANT ALL ON SEQUENCES TO nexus;
