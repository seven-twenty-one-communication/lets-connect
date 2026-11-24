-- ============================================================================
-- Flyway Migration V8: Create Expert Leads table for Host Enrollment Funnel
-- ============================================================================

CREATE TABLE IF NOT EXISTS expert_leads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    gender VARCHAR(20) NOT NULL,
    primary_language VARCHAR(50) NOT NULL,
    preferred_slot VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_REVIEW',
    notes TEXT,
    reviewed_by UUID REFERENCES users(id),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_expert_leads_phone ON expert_leads(phone_number);
CREATE INDEX IF NOT EXISTS idx_expert_leads_status ON expert_leads(status);
CREATE INDEX IF NOT EXISTS idx_expert_leads_created ON expert_leads(created_at DESC);
