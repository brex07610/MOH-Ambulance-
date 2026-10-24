CREATE DATABASE IF NOT EXISTS ministry_of_health_ambulance;
USE ministry_of_health_ambulance;

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100),
    phone_number VARCHAR(20),
    is_admin BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS ambulances (
    ambulance_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_number VARCHAR(20) UNIQUE NOT NULL,
    transport_type ENUM('Road', 'Air') NOT NULL,
    status ENUM('Available', 'Dispatched', 'Maintenance', 'In Transit') NOT NULL DEFAULT 'Available',
    current_location VARCHAR(255) NOT NULL,
    last_updated DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    ambulance_id INT,
    requested_transport ENUM('Road','Air') NOT NULL,
    pickup_location VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    medical_problem TEXT NOT NULL,
    emergency_type VARCHAR(50) NOT NULL,
    patient_history TEXT,
    booking_time DATETIME NOT NULL,
    current_status ENUM('Pending', 'Confirmed', 'Dispatched', 'In Transit', 'Arrived', 'Complete', 'Cancelled') NOT NULL DEFAULT 'Pending',
    estimated_arrival DATETIME,
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_ambulance FOREIGN KEY (ambulance_id) REFERENCES ambulances(ambulance_id) ON DELETE SET NULL
);
