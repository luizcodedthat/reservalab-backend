-- V12__add_active_to_tickets.sql
ALTER TABLE tickets ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;