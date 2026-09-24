-- V3__add_employee_fields_to_users.sql
-- Add business_unit, work_location, and asset_tag to the users table

ALTER TABLE users
ADD COLUMN business_unit VARCHAR(100),
ADD COLUMN work_location VARCHAR(100),
ADD COLUMN asset_tag VARCHAR(100);
