-- ============================================================================
-- CONNECTO APP SEED DATA: Categories, Coin Packages, and Emergency Helplines
-- ============================================================================

-- 1. EMOTIONAL SUPPORT CATEGORIES
INSERT INTO categories (name, slug, description, display_order, is_active) VALUES
('Breakup & Heartbreak', 'breakup-heartbreak', 'Talk to someone who understands the pain of separation and moving on.', 1, true),
('Late Night Thoughts', 'late-night-thoughts', 'Cannot sleep? Need a friendly companion to talk your mind out.', 2, true),
('Career & Exam Stress', 'career-exam-stress', 'Dealing with workplace pressure, job hunts, or academic burnout.', 3, true),
('Loneliness & Companionship', 'loneliness-companionship', 'When you feel alone and simply want a listening, non-judgmental ear.', 4, true),
('Overthinking & Anxiety', 'overthinking-anxiety', 'Vent out your racing thoughts and find grounded reassurance.', 5, true),
('Family & Marriage Advice', 'family-marriage-advice', 'Guidance and peer perspectives on family or domestic friction.', 6, true),
('Spiritual & Daily Motivation', 'motivation-positivity', 'Start your day with positive energy and purposeful conversations.', 7, true)
ON CONFLICT (slug) DO NOTHING;

-- 2. GOOGLE PLAY COIN PACKAGES
-- Assumption: 1 Coin = ~₹1 (or 10 Coins = 1 Minute of standard call @ ₹10/min)
INSERT INTO coin_packages (play_product_id, coin_amount, bonus_coins, price_in_inr, display_title, is_active) VALUES
('com.connecto.coins_50', 50, 0, 49.00, 'Starter Pack (50 Coins)', true),
('com.connecto.coins_120', 100, 20, 99.00, 'Popular Pack (120 Coins)', true),
('com.connecto.coins_350', 300, 50, 249.00, 'Value Pack (350 Coins)', true),
('com.connecto.coins_800', 650, 150, 499.00, 'Mega Pack (800 Coins)', true)
ON CONFLICT (play_product_id) DO NOTHING;

-- 3. NATIONAL CRISIS HELPLINES (Crucial for Google Play Mental Health Policy)
INSERT INTO crisis_helplines (country_code, name, phone_number, description, hours, is_active) VALUES
('IN', 'Tele-MANAS (Govt of India)', '14416', 'National Tele Mental Health Programme of India', '24/7', true),
('IN', 'KIRAN (Govt Helpline)', '1800-599-0019', 'Mental health rehabilitation helpline', '24/7', true),
('IN', 'Vandrevala Foundation', '+91 9999 666 555', 'Free mental health support and crisis intervention', '24/7', true),
('IN', 'AASRA Helpline', '+91 98204 66726', 'Suicide prevention and crisis emotional support', '24/7', true);

-- 4. VIRTUAL GIFTS SEED DATA
INSERT INTO gifts (name, emoji, coin_price, animation_effect, is_active) VALUES
('Red Rose', '🌹', 5, 'FLOAT', true),
('Warm Chai', '☕', 10, 'STEAM', true),
('Sweet Chocolate', '🍫', 25, 'SPARKLE', true),
('Big Heart', '💖', 50, 'PULSE', true),
('Golden Crown', '👑', 100, 'CONFETTI', true);
