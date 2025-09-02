CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    priority VARCHAR(50),
    due_date TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
