CREATE DATABASE IF NOT EXISTS planoradb;
USE planoradb;

DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE events (
    event_id INT PRIMARY KEY AUTO_INCREMENT,
    event_name VARCHAR(150) NOT NULL,
    artist VARCHAR(150) NOT NULL,
    event_date DATE NOT NULL,
    event_time TIME NOT NULL,
    venue VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    ticket_price DECIMAL(10,2) NOT NULL,
    available_tickets INT NOT NULL,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    event_id INT NOT NULL,
    ticket_quantity INT NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE
);

INSERT INTO users (name,email,phone,password,role)
VALUES ('PlaNora Admin','admin@planora.com','9999999999','admin123','ADMIN');

INSERT INTO users (name,email,phone,password,role)
VALUES ('Demo User','user@planora.com','8888888888','user123','USER');

INSERT INTO events
(event_name,artist,event_date,event_time,venue,description,ticket_price,available_tickets,image_url)
VALUES
('Summer Beats','Arijit Singh','2026-10-20','19:00:00','Chandigarh Arena',
'An unforgettable live music evening with Arijit Singh.',1499,500,
'https://images.unsplash.com/photo-1501386761578-eac5c94b800a');

INSERT INTO events
(event_name,artist,event_date,event_time,venue,description,ticket_price,available_tickets,image_url)
VALUES
('Melody Night','Shreya Ghoshal','2026-11-05','18:30:00','Delhi Stadium',
'An evening of beautiful melodies and live performances.',1999,300,
'https://images.unsplash.com/photo-1524368535928-5b5e00ddc76b');

INSERT INTO events
(event_name,artist,event_date,event_time,venue,description,ticket_price,available_tickets,image_url)
VALUES
('Campus Vibes','The Local Train','2026-11-18','19:30:00','University Ground',
'High-energy live music night for students and music lovers.',999,800,
'https://images.unsplash.com/photo-1524368535928-5b5e00ddc76b');
