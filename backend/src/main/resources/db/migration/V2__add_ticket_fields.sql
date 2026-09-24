ALTER TABLE tickets 
ADD COLUMN business_unit VARCHAR(100),
ADD COLUMN work_location VARCHAR(100),
ADD COLUMN asset_tag VARCHAR(100);

ALTER TABLE ticket_resolutions
ADD COLUMN resolution_code VARCHAR(8);
