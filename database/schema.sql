-- ============================================================================
-- CONNECTO CALLING APP: PRODUCTION DATABASE SCHEMA (PostgreSQL)
-- Architecture: 1-on-1 Anonymous Voice Support & Companionship Platform
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- ENUM TYPES
-- ============================================================================
CREATE TYPE user_role AS ENUM ('USER', 'LISTENER', 'ADMIN');
CREATE TYPE user_status AS ENUM ('ACTIVE', 'SUSPENDED', 'BANNED');
CREATE TYPE kyc_status AS ENUM ('NOT_SUBMITTED', 'PENDING', 'VERIFIED', 'REJECTED');
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
CREATE TYPE disconnect_reason AS ENUM (
    'CALLER_HANGUP',
    'LISTENER_HANGUP',
    'BALANCE_EXHAUSTED',
    'NETWORK_TIMEOUT',
    'SYSTEM_TERMINATED'
);
CREATE TYPE transaction_type AS ENUM (
    'PLAY_PURCHASE',         -- Caller bought coins via Google Play Billing
    'CALL_DEBIT',             -- Caller deducted coins for call minutes
    'CALL_CREDIT_EARNING',   -- Listener earned share of call coins
    'ADMIN_CREDIT',          -- Promo / refund / bonus coins
    'ADMIN_DEBIT',           -- Penalties / adjustments
    'PAYOUT_WITHDRAWAL'      -- Listener redeemed coins to bank/UPI
);
CREATE TYPE payout_status AS ENUM ('REQUESTED', 'PROCESSING', 'PAID', 'FAILED', 'REJECTED');

-- ============================================================================
-- 1. USERS TABLE (Shared Identity for Callers & Listeners)
-- ============================================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    nickname VARCHAR(50) NOT NULL,
    avatar_url TEXT NOT NULL,                        -- Predefined 3D/AI avatars for anonymity
    gender VARCHAR(20),                             -- Optional
    age_group VARCHAR(20),                          -- e.g. '18-24', '25-34', '35+'
    preferred_languages TEXT[] DEFAULT '{"English", "Hindi"}',
    role user_role NOT NULL DEFAULT 'USER',
    status user_status NOT NULL DEFAULT 'ACTIVE',
    device_token TEXT,                              -- FCM Token for push notifications / call ringing
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_phone ON users(phone_number);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);

-- ============================================================================
-- 2. CATEGORIES TABLE (Emotional topics & tags)
-- ============================================================================
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,              -- e.g. "Breakup & Heartbreak", "Career Stress", "Late Night Venting"
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
CREATE TABLE listener_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    bio TEXT,
    voice_intro_url TEXT,                           -- 10-15s voice audio sample preview
    price_per_minute_coins INT NOT NULL DEFAULT 10, -- Coins charged per minute
    payout_percentage NUMERIC(5,2) DEFAULT 60.00,   -- Platform gives 60% of coins to listener
    
    -- Real-time Presence
    is_online BOOLEAN DEFAULT FALSE,
    is_on_call BOOLEAN DEFAULT FALSE,
    last_seen_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Aggregated Stats
    rating_avg NUMERIC(3,2) DEFAULT 5.00,
    total_reviews INT DEFAULT 0,
    total_calls_completed INT DEFAULT 0,
    total_minutes_spoken BIGINT DEFAULT 0,
    
    -- Verification & KYC
    kyc_status kyc_status NOT NULL DEFAULT 'NOT_SUBMITTED',
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

CREATE INDEX idx_listener_online ON listener_profiles(is_online, is_on_call);
CREATE INDEX idx_listener_rating ON listener_profiles(rating_avg DESC);
CREATE INDEX idx_listener_kyc ON listener_profiles(kyc_status);

-- Junction: Listeners <-> Categories (Many to Many)
CREATE TABLE listener_categories (
    listener_id UUID NOT NULL REFERENCES listener_profiles(id) ON DELETE CASCADE,
    category_id INT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (listener_id, category_id)
);

-- ============================================================================
-- 4. WALLETS TABLE (Caller coins & Listener earnings)
-- ============================================================================
CREATE TABLE wallets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    coin_balance BIGINT NOT NULL DEFAULT 0 CHECK (coin_balance >= 0),
    earned_coins_balance BIGINT NOT NULL DEFAULT 0 CHECK (earned_coins_balance >= 0), -- Listener redeemable balance
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_wallet_user ON wallets(user_id);

-- Coin Packages available in Google Play Billing
CREATE TABLE coin_packages (
    id SERIAL PRIMARY KEY,
    play_product_id VARCHAR(100) NOT NULL UNIQUE,   -- e.g. 'com.connecto.coins_100'
    coin_amount INT NOT NULL,
    bonus_coins INT NOT NULL DEFAULT 0,
    price_in_inr NUMERIC(10,2) NOT NULL,
    display_title VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Audit Ledger for every single coin movement
CREATE TABLE coin_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet_id UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    amount BIGINT NOT NULL,                         -- Can be positive (credit) or negative (debit)
    balance_after BIGINT NOT NULL,
    transaction_type transaction_type NOT NULL,
    reference_id VARCHAR(100),                      -- call_session_id or Google Play order_id
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_wallet ON coin_transactions(wallet_id);
CREATE INDEX idx_transactions_user ON coin_transactions(user_id);
CREATE INDEX idx_transactions_type ON coin_transactions(transaction_type);

-- ============================================================================
-- 5. CALL SESSIONS TABLE (Audio Calls & Real-time Billing State)
-- ============================================================================
CREATE TABLE call_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    channel_name VARCHAR(100) NOT NULL UNIQUE,      -- Agora / Zego RTC Channel ID
    caller_id UUID NOT NULL REFERENCES users(id),
    listener_id UUID NOT NULL REFERENCES users(id),
    category_id INT REFERENCES categories(id),
    
    status call_status NOT NULL DEFAULT 'INITIATED',
    rate_per_minute_coins INT NOT NULL,
    
    -- Timestamps
    initiated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ringing_at TIMESTAMP WITH TIME ZONE,
    connected_at TIMESTAMP WITH TIME ZONE,
    ended_at TIMESTAMP WITH TIME ZONE,
    duration_seconds INT DEFAULT 0,
    
    -- Financial settlement for this call
    total_coins_charged BIGINT DEFAULT 0,
    listener_coins_earned BIGINT DEFAULT 0,
    platform_commission_coins BIGINT DEFAULT 0,
    
    disconnect_reason disconnect_reason,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_call_caller ON call_sessions(caller_id);
CREATE INDEX idx_call_listener ON call_sessions(listener_id);
CREATE INDEX idx_call_status ON call_sessions(status);
CREATE INDEX idx_call_channel ON call_sessions(channel_name);

-- ============================================================================
-- 6. RATINGS & REVIEWS TABLE
-- ============================================================================
CREATE TABLE call_reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    call_session_id UUID NOT NULL UNIQUE REFERENCES call_sessions(id) ON DELETE CASCADE,
    caller_id UUID NOT NULL REFERENCES users(id),
    listener_id UUID NOT NULL REFERENCES users(id),
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    feedback_tags TEXT[],                           -- e.g. 'Good Listener', 'Empathetic', 'Helped me calm down'
    review_text TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reviews_listener ON call_reviews(listener_id);

-- ============================================================================
-- 7. SAFETY, REPORTING & BLOCK LIST TABLE (Mandatory for Google Play UGC)
-- ============================================================================
CREATE TABLE user_blocks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    blocker_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    blocked_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (blocker_id, blocked_user_id)
);

CREATE TABLE abuse_reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reporter_id UUID NOT NULL REFERENCES users(id),
    reported_user_id UUID NOT NULL REFERENCES users(id),
    call_session_id UUID REFERENCES call_sessions(id),
    reason VARCHAR(100) NOT NULL,                   -- e.g. 'Harassment', 'Sexual misconduct', 'Scam'
    details TEXT,
    status VARCHAR(30) DEFAULT 'PENDING',           -- 'PENDING', 'INVESTIGATED', 'DISMISSED', 'ACTIONED'
    action_taken TEXT,
    resolved_by UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_abuse_reported ON abuse_reports(reported_user_id);
CREATE INDEX idx_abuse_status ON abuse_reports(status);

-- ============================================================================
-- 8. LISTENER PAYOUT REDEMPTIONS TABLE
-- ============================================================================
CREATE TABLE payout_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    listener_id UUID NOT NULL REFERENCES users(id),
    coins_redeemed BIGINT NOT NULL,
    amount_inr NUMERIC(10,2) NOT NULL,
    payout_method VARCHAR(30) NOT NULL,             -- 'UPI' or 'BANK_TRANSFER'
    payout_details JSONB NOT NULL,
    status payout_status NOT NULL DEFAULT 'REQUESTED',
    payout_reference VARCHAR(100),                  -- Bank UTR / RazorpayX Transfer ID
    rejection_reason TEXT,
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payout_listener ON payout_requests(listener_id);
CREATE INDEX idx_payout_status ON payout_requests(status);

-- ============================================================================
-- 10. VIRTUAL GIFTS TABLE (In-Call Listener Tipping & Appreciation)
-- ============================================================================
CREATE TABLE gifts (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,                      -- e.g. 'Red Rose', 'Comfort Chai', 'Warm Hug', 'Golden Crown'
    emoji VARCHAR(20) NOT NULL,
    coin_price INT NOT NULL,                        -- e.g. 5, 20, 50, 100 coins
    animation_effect VARCHAR(50) DEFAULT 'FLOAT',   -- Animation trigger on recipient's call screen
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE gift_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    call_session_id UUID REFERENCES call_sessions(id) ON DELETE SET NULL,
    sender_id UUID NOT NULL REFERENCES users(id),
    recipient_id UUID NOT NULL REFERENCES users(id),
    gift_id INT NOT NULL REFERENCES gifts(id),
    coins_spent INT NOT NULL,
    recipient_coins_earned INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_gifts_call ON gift_transactions(call_session_id);
CREATE INDEX idx_gifts_recipient ON gift_transactions(recipient_id);

-- ============================================================================
-- 11. ANONYMOUS 1-ON-1 TEXT CHAT MESSAGES
-- ============================================================================
CREATE TABLE chat_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sender_id UUID NOT NULL REFERENCES users(id),
    recipient_id UUID NOT NULL REFERENCES users(id),
    message_text TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chat_conversation ON chat_messages(sender_id, recipient_id, created_at);

-- ============================================================================
-- 12. EMERGENCY CRISIS HELPLINES (Google Play Policy Compliance)
-- ============================================================================
CREATE TABLE crisis_helplines (
    id SERIAL PRIMARY KEY,
    country_code VARCHAR(10) NOT NULL DEFAULT 'IN',
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(50) NOT NULL,
    description TEXT,
    hours VARCHAR(50) DEFAULT '24/7',
    is_active BOOLEAN DEFAULT TRUE
);
