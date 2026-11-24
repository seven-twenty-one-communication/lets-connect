CREATE TABLE IF NOT EXISTS play_purchases (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id VARCHAR(100) NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id),
    product_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_play_purchase_order_id UNIQUE (order_id)
);

CREATE TABLE IF NOT EXISTS voice_rooms (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(100) NOT NULL,
    channel_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    language VARCHAR(30) NOT NULL,
    host_id UUID NOT NULL REFERENCES users(id),
    category_id INT REFERENCES categories(id),
    max_participants INT NOT NULL DEFAULT 8,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS voice_room_participants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    room_id UUID NOT NULL REFERENCES voice_rooms(id),
    user_id UUID NOT NULL REFERENCES users(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_voice_room_participant UNIQUE (room_id, user_id)
);

CREATE TABLE IF NOT EXISTS user_languages (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    language VARCHAR(255),
    PRIMARY KEY (user_id, language)
);
