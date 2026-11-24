DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'user_blocks'
          AND column_name = 'blocked_user_id'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'user_blocks'
          AND column_name = 'blocked_id'
    ) THEN
        ALTER TABLE user_blocks RENAME COLUMN blocked_user_id TO blocked_id;
    END IF;
END $$;
