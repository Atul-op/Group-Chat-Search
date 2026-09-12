-- Enable the pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Create the chat messages table
CREATE TABLE IF NOT EXISTS chat_messages (
                                             id BIGSERIAL PRIMARY KEY,
                                             sender_name VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    content TEXT NOT NULL,
    embedding vector(3072)
    );
