ALTER TABLE users
    ALTER COLUMN role TYPE VARCHAR(20) USING role::text,
    ALTER COLUMN status TYPE VARCHAR(20) USING status::text;

ALTER TABLE listener_profiles
    ALTER COLUMN kyc_status TYPE VARCHAR(30) USING kyc_status::text;

ALTER TABLE coin_transactions
    ALTER COLUMN transaction_type TYPE VARCHAR(30) USING transaction_type::text;

ALTER TABLE call_sessions
    ALTER COLUMN status TYPE VARCHAR(30) USING status::text,
    ALTER COLUMN disconnect_reason TYPE VARCHAR(30) USING disconnect_reason::text;

ALTER TABLE payout_requests
    ALTER COLUMN status TYPE VARCHAR(30) USING status::text;
