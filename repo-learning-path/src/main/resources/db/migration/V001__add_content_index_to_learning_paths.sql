-- Migration: Add content_index column to learning_paths table
-- This migration adds the content_index field inherited from BaseContentItem
-- and ensures study_set_id has proper foreign key constraint

-- Add content_index column (nullable, for optional ordering within StudySet)
ALTER TABLE learning_paths 
ADD COLUMN content_index INT NULL COMMENT 'Optional ordering index within StudySet';

-- Ensure study_set_id has foreign key constraint
-- Note: This may fail if the constraint already exists or if there's data integrity issues
-- In that case, you may need to adjust this migration

-- Check if constraint exists before adding (MySQL 8.0+)
-- If using older MySQL or constraint already exists, you can skip this part

ALTER TABLE learning_paths
ADD CONSTRAINT fk_learning_path_study_set
FOREIGN KEY (study_set_id) 
REFERENCES study_sets(id)
ON DELETE CASCADE
ON UPDATE CASCADE;

-- Note: The column name 'study_set_id' remains the same due to 
-- @JoinColumn(name = "study_set_id") annotation in BaseContentItem
