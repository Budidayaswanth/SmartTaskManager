-- Introduce task status enum-like column and migrate legacy completed flag.
ALTER TABLE tasks ADD COLUMN status VARCHAR(20);

UPDATE tasks
SET status = CASE
    WHEN completed = TRUE THEN 'DONE'
    ELSE 'TODO'
END;

ALTER TABLE tasks ALTER COLUMN status SET NOT NULL;
ALTER TABLE tasks ALTER COLUMN status SET DEFAULT 'TODO';

CREATE INDEX IF NOT EXISTS idx_tasks_user_status ON tasks(user_id, status);

