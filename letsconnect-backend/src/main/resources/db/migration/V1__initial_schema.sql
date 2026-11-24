-- ============================================================================
-- CONNECTO CALLING APP: PRODUCTION DATABASE SCHEMA (PostgreSQL)
-- Architecture: 1-on-1 Social Audio & Friendship Platform
-- Flyway Migration V1: Initial Baseline Schema
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- ENUM TYPES
-- ============================================================================
DO $$ BEGIN
    CREATE TYPE user_role AS ENUM ('USER', 'LISTENER', 'ADMIN');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE user_status AS ENUM ('ACTIVE', 'SUSPENDED', 'BANNED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE kyc_status AS ENUM ('NOT_SUBMITTED', 'PENDING', 'VERIFIED', 'REJECTED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE call_status AS ENUM (
        'INITIATED',
        'RINGING',
        'CONNECTED',
        'COMPLETED',
        'REJECTED',
        'MISSED',
        'BUSY',
        'FAILED'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE disconnect_reason AS ENUM (
        'CALLER_HANGUP',
        'LISTENER_HANGUP',
        'BALANCE_EXHAUSTED',
        'NETWORK_TIMEOUT',
        'SYSTEM_TERMINATED'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE transaction_type AS ENUM (
        'PLAY_PURCHASE',         -- Caller bought coins via Google Play Billing
        'CALL_DEBIT',            -- Caller deducted coins for call minutes
        'CALL_CREDIT_EARNING',   -- Listener earned share of call coins
        'ADMIN_CREDIT',          -- Promo / refund / bonus coins
        'ADMIN_DEBIT',           -- Penalties / adjustments
        'PAYOUT_WITHDRAWAL'      -- Listener redeemed coins to bank/UPI
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE payout_status AS ENUM ('REQUESTED', 'PROCESSING', 'PAID', 'FAILED', 'REJECTED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- ============================================================================
-- 1. USERS TABLE (Shared Identity for Callers & Listeners)
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    nickname VARCHAR(50) NOT NULL,
    avatar_url TEXT NOT NULL,
    gender VARCHAR(20),
    age_group VARCHAR(20),
    preferred_languages TEXT[] DEFAULT '{"English", "Hindi"}',
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    device_token TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone_number);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

-- ============================================================================
-- 2. CATEGORIES TABLE (Social & Friendship Topics)
-- ============================================================================
CREATE TABLE IF NOT EXISTS categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon_url TEXT,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- 3. LISTENERS PROFILE TABLE (Extended details for Verified Companions)
-- ============================================================================
CREATE TABLE IF NOT EXISTS listener_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    bio TEXT,
    voice_intro_url TEXT,
    price_per_minute_coins INT NOT NULL DEFAULT 10,
    payout_percentage NUMERIC(5,2) DEFAULT 60.00,
    
    -- Real-time Presence
    is_online BOOLEAN DEFAULT FALSE,
    is_on_call BOOLEAN DEFAULT FALSE,
    last_seen_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Aggregated Stats
    rating_avg NUMERIC(3,2) DEFAULT 5.00,
    total_reviews INT DEFAULT 0,
    total_calls_completed INT DEFAULT 0,
    total_minutes_spoken BIGINT DEFAULT 0,
    
    -- Verification & Payout Compliance (for withdrawals)
    kyc_status VARCHAR(30) NOT NULL DEFAULT 'NOT_SUBMITTED',
    kyc_document_url TEXT,
    kyc_submitted_at TIMESTAMP WITH TIME ZONE,
    verified_at TIMESTAMP WITH TIME ZONE,
    verified_by UUID REFERENCES users(id),
    
    -- Payout Banking Info (Encrypted)
    payout_upi_id VARCHAR(100),
    payout_bank_account_no VARCHAR(50),
    payout_bank_ifsc VARCHAR(20),
    payout_beneficiary_name VARCHAR(100),

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_listener_online ON listener_profiles(is_online, is_on_call);
CREATE INDEX IF NOT EXISTS idx_listener_rating ON listener_profiles(rating_avg DESC);
CREATE INDEX IF NOT EXISTS idx_listener_kyc ON listener_profiles(kyc_status);

-- Junction: Listeners <-> Categories (Many to Many)
CREATE TABLE IF NOT EXISTS listener_categories (
    listener_id UUID NOT NULL REFERENCES listener_profiles(id) ON DELETE CASCADE,
    category_id INT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (listener_id, category_id)
);

-- ============================================================================
-- 4. WALLETS TABLE (Caller coins & Listener earnings)
-- ============================================================================
CREATE TABLE IF NOT EXISTS wallets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    coin_balance BIGINT NOT NULL DEFAULT 0 CHECK (coin_balance >= 0),
    earned_coins_balance BIGINT NOT NULL DEFAULT 0 CHECK (earned_coins_balance >= 0),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_wallet_user ON wallets(user_id);

-- Coin Packages available in Google Play Billing
CREATE TABLE IF NOT EXISTS coin_packages (
    id SERIAL PRIMARY KEY,
    play_product_id VARCHAR(100) NOT NULL UNIQUE,
    coin_amount INT NOT NULL,
    bonus_coins INT NOT NULL DEFAULT 0,
    price_in_inr NUMERIC(10,2) NOT NULL,
    display_title VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Audit Ledger for coin movements
CREATE TABLE IF NOT EXISTS coin_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet_id UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    amount BIGINT NOT NULL,
    balance_after BIGINT NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    reference_id VARCHAR(100),
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transactions_wallet ON coin_transactions(wallet_id);
CREATE INDEX IF NOT EXISTS idx_transactions_user ON coin_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_type ON coin_transactions(transaction_type);

-- ============================================================================
-- 5. CALL SESSIONS TABLE (Audio Calls & Real-time Billing State)
-- ============================================================================
CREATE TABLE IF NOT EXISTS call_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    channel_name VARCHAR(100) NOT NULL UNIQUE,
    caller_id UUID NOT NULL REFERENCES users(id),
    listener_id UUID NOT NULL REFERENCES users(id),
    category_id INT REFERENCES categories(id),
    
    status VARCHAR(30) NOT NULL DEFAULT 'INITIATED',
    rate_per_minute_coins INT NOT NULL,
    
    -- Timestamps
    initiated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ringing_at TIMESTAMP WITH TIME ZONE,
    connected_at TIMESTAMP WITH TIME ZONE,
    last_billed_at TIMESTAMP WITH TIME ZONE,
    ended_at TIMESTAMP WITH TIME ZONE,
    duration_seconds INT DEFAULT 0,
    
    -- Financial settlement for this call
    total_coins_charged BIGINT DEFAULT 0,
    listener_coins_earned BIGINT DEFAULT 0,
    platform_commission_coins BIGINT DEFAULT 0,
    
    disconnect_reason VARCHAR(30),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_call_caller ON call_sessions(caller_id);
CREATE INDEX IF NOT EXISTS idx_call_listener ON call_sessions(listener_id);
CREATE INDEX IF NOT EXISTS idx_call_status ON call_sessions(status);
CREATE INDEX IF NOT EXISTS idx_call_channel ON call_sessions(channel_name);

-- ============================================================================
-- 6. RATINGS & REVIEWS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS call_reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    call_session_id UUID NOT NULL UNIQUE REFERENCES call_sessions(id) ON DELETE CASCADE,
    caller_id UUID NOT NULL REFERENCES users(id),
    listener_id UUID NOT NULL REFERENCES users(id),
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    feedback_tags TEXT[],
    review_text TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reviews_listener ON call_reviews(listener_id);

-- ============================================================================
-- 7. SAFETY, REPORTING & BLOCK LIST TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS user_blocks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    blocker_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    blocked_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (blocker_id, blocked_id)
);

CREATE TABLE IF NOT EXISTS user_reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reporter_id UUID NOT NULL REFERENCES users(id),
    reported_user_id UUID NOT NULL REFERENCES users(id),
    call_session_id UUID REFERENCES call_sessions(id),
    reason VARCHAR(100) NOT NULL,
    details TEXT,
    status VARCHAR(30) DEFAULT 'PENDING',
    action_taken TEXT,
    resolved_by UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_reported ON user_reports(reported_user_id);
CREATE INDEX IF NOT EXISTS idx_user_report_status ON user_reports(status);

-- ============================================================================
-- 8. LISTENER PAYOUT REDEMPTIONS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS payout_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    listener_id UUID NOT NULL REFERENCES users(id),
    coins_redeemed BIGINT NOT NULL,
    amount_inr NUMERIC(10,2) NOT NULL,
    payout_method VARCHAR(30) NOT NULL,
    payout_details JSONB NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED',
    payout_reference VARCHAR(100),
    rejection_reason TEXT,
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payout_listener ON payout_requests(listener_id);
CREATE INDEX IF NOT EXISTS idx_payout_status ON payout_requests(status);

-- ============================================================================
-- 9. VIRTUAL GIFTS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS gifts (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    emoji VARCHAR(20) NOT NULL,
    coin_price INT NOT NULL,
    animation_effect VARCHAR(50) DEFAULT 'FLOAT',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS gift_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    call_session_id UUID REFERENCES call_sessions(id) ON DELETE SET NULL,
    sender_id UUID NOT NULL REFERENCES users(id),
    recipient_id UUID NOT NULL REFERENCES users(id),
    gift_id INT NOT NULL REFERENCES gifts(id),
    coins_spent INT NOT NULL,
    recipient_coins_earned INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_gifts_call ON gift_transactions(call_session_id);
CREATE INDEX IF NOT EXISTS idx_gifts_recipient ON gift_transactions(recipient_id);

-- ============================================================================
-- 10. 1-ON-1 TEXT CHAT MESSAGES
-- ============================================================================
CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sender_id UUID NOT NULL REFERENCES users(id),
    recipient_id UUID NOT NULL REFERENCES users(id),
    message_text TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_chat_conversation ON chat_messages(sender_id, recipient_id, created_at);

-- ============================================================================
-- 11. EMERGENCY CRISIS HELPLINES (Policy Compliance)
-- ============================================================================
CREATE TABLE IF NOT EXISTS crisis_helplines (
    id SERIAL PRIMARY KEY,
    country_code VARCHAR(10) NOT NULL DEFAULT 'IN',
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(50) NOT NULL,
    description TEXT,
    hours VARCHAR(50) DEFAULT '24/7',
    is_active BOOLEAN DEFAULT TRUE
);
