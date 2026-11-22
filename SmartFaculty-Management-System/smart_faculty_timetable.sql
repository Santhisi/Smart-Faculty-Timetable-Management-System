-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: Nov 04, 2026 at 10:57 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `smart_faculty_timetable`
--

-- --------------------------------------------------------

--
-- Table structure for table `classrooms`
--

CREATE TABLE `classrooms` (
  `classroom_id` bigint(20) NOT NULL,
  `active` bit(1) NOT NULL,
  `capacity` int(11) NOT NULL,
  `room_code` varchar(255) NOT NULL,
  `room_name` varchar(255) NOT NULL,
  `room_type` enum('CLASSROOM','LAB','LECTURE_HALL','SEMINAR_ROOM') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `classrooms`
--

INSERT INTO `classrooms` (`classroom_id`, `active`, `capacity`, `room_code`, `room_name`, `room_type`) VALUES
(1, b'1', 40, 'CC1', 'ICT Laboratory 01', 'LAB'),
(2, b'1', 120, 'C1', 'Lecture Hall 01', 'LECTURE_HALL'),
(3, b'1', 40, 'CC2', 'Computer Laboratory 2', 'LAB'),
(4, b'1', 35, 'A1', 'Classroom 01', 'CLASSROOM'),
(5, b'1', 35, 'A2', 'Classroom 02', 'CLASSROOM'),
(6, b'1', 35, 'A3', 'Classroom 03', 'CLASSROOM'),
(7, b'1', 35, 'A4', 'Classroom 04', 'CLASSROOM'),
(8, b'1', 35, 'B1', 'Classroom 05', 'CLASSROOM'),
(9, b'1', 35, 'B2', 'Classroom 06', 'CLASSROOM'),
(10, b'1', 35, 'B3', 'Classroom 07', 'CLASSROOM'),
(11, b'1', 35, 'B4', 'Classroom 08', 'CLASSROOM'),
(12, b'1', 35, 'D1', 'Classroom 09', 'CLASSROOM'),
(13, b'1', 35, 'D2', 'Classroom 10', 'CLASSROOM'),
(14, b'1', 35, 'D3', 'Classroom 11', 'CLASSROOM'),
(15, b'1', 35, 'D4', 'Classroom 12', 'CLASSROOM'),
(16, b'1', 35, 'D5', 'Classroom 13', 'CLASSROOM'),
(17, b'1', 35, 'D6', 'Classroom 14', 'CLASSROOM'),
(18, b'1', 35, 'D7', 'Classroom 15', 'CLASSROOM'),
(19, b'1', 50, 'SR1', 'Seminar Room 1', 'SEMINAR_ROOM'),
(20, b'1', 50, 'SR2', 'Seminar Room 2', 'SEMINAR_ROOM');

-- --------------------------------------------------------

--
-- Table structure for table `departments`
--

CREATE TABLE `departments` (
  `department_id` bigint(20) NOT NULL,
  `department_code` varchar(255) NOT NULL,
  `department_name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `departments`
--

INSERT INTO `departments` (`department_id`, `department_code`, `department_name`) VALUES
(1, 'DIT', 'Department of Information and Communication Technology'),
(2, 'DELT', 'Department of English Language Teaching'),
(3, 'DES', 'Department of Economics and Statistics'),
(4, 'GEM', 'Department of Geography and Environmental Management'),
(5, 'DEL', 'Department of Languages'),
(6, 'DSS', 'Department of Social Sciences');

-- --------------------------------------------------------

--
-- Table structure for table `lecturers`
--

CREATE TABLE `lecturers` (
  `lecturer_id` bigint(20) NOT NULL,
  `email` varchar(255) NOT NULL,
  `lecturer_code` varchar(255) NOT NULL,
  `lecturer_name` varchar(255) NOT NULL,
  `department_id` bigint(20) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `lecturers`
--

INSERT INTO `lecturers` (`lecturer_id`, `email`, `lecturer_code`, `lecturer_name`, `department_id`, `user_id`) VALUES
(1, 'perera@ssl.sab.ac.lk', 'LEC001', 'Dr. Sunil Perera', 1, 2),
(2, 'nimal@ssl.sab.ac.lk', 'LEC002', 'Nimal Silva', 1, 3),
(3, 'kasun@ssl.sab.ac.lk', 'LEC003', 'Kasun Fernando', 1, 4),
(6, 'shanthaperera@ssl.sab.ac.lk', 'LEC004', 'Shantha Perera', 3, 14),
(7, 'indikaperera@ssl.sab.ac.lk', 'LEC005', 'Dr. Indika Perera', 3, 32),
(8, 'hiruniweera@ssl.sab.ac.lk', 'LEC006', 'Hiruni Weerakoon', 1, 33),
(9, 'nishashama@ssl.sab.ac.lk', 'LEC007', 'Nisha Shamali', 3, 35);

-- --------------------------------------------------------

--
-- Table structure for table `lecturer_allocations`
--

CREATE TABLE `lecturer_allocations` (
  `allocation_id` bigint(20) NOT NULL,
  `academic_year` int(11) NOT NULL,
  `semester` int(11) NOT NULL,
  `lecturer_id` bigint(20) NOT NULL,
  `subject_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `lecturer_allocations`
--

INSERT INTO `lecturer_allocations` (`allocation_id`, `academic_year`, `semester`, `lecturer_id`, `subject_id`) VALUES
(1, 1, 1, 2, 1),
(2, 1, 1, 6, 7),
(3, 2, 1, 3, 51),
(4, 3, 1, 1, 59),
(5, 2, 1, 9, 22),
(6, 3, 1, 7, 34),
(7, 2, 1, 6, 21),
(9, 2, 1, 2, 49),
(10, 3, 1, 8, 55),
(11, 3, 1, 7, 27),
(12, 3, 2, 3, 61),
(13, 3, 2, 3, 60),
(14, 4, 1, 8, 75);

-- --------------------------------------------------------

--
-- Table structure for table `lecturer_notifications`
--

CREATE TABLE `lecturer_notifications` (
  `notification_id` bigint(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `is_read` bit(1) NOT NULL,
  `message` text NOT NULL,
  `notification_type` varchar(255) NOT NULL,
  `title` varchar(255) NOT NULL,
  `lecturer_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `lecturer_notifications`
--

INSERT INTO `lecturer_notifications` (`notification_id`, `created_at`, `is_read`, `message`, `notification_type`, `title`, `lecturer_id`) VALUES
(1, '2026-08-18 22:43:28.000000', b'1', 'Your timetable change request for ICT111 has been approved. Please check your timetable for the updated schedule.', 'TIMETABLE_CHANGE_APPROVED', 'Timetable Change Approved', 2),
(2, '2026-09-03 18:44:10.000000', b'1', 'Your timetable change request for subject ICT111 has been approved by the administrator. Request approved.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', 2),
(3, '2026-09-03 18:59:28.000000', b'1', 'Your timetable change request for ICT111 has been approved. Please check your timetable for the updated schedule.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', 2),
(4, '2026-09-04 08:30:33.000000', b'1', 'Your timetable change request for ICT111 has been approved. Please check your timetable for the updated schedule.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', 2),
(5, '2026-09-04 08:39:38.000000', b'1', 'Your timetable change request for ICT111 has been approved. Please check your timetable for the updated schedule.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', 2),
(6, '2026-09-04 08:46:48.000000', b'0', 'Your timetable change request for subject ICT111 has been rejected by the administrator. Request rejected.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Rejected', 2),
(7, '2026-09-22 19:32:51.000000', b'0', 'Your timetable change request for subject ECO111 has been rejected by the administrator. Request rejected.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Rejected', 6),
(8, '2026-09-23 10:24:33.000000', b'1', 'Your timetable change request for ICT213 has been approved. Please check your timetable for the updated schedule.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', 3),
(9, '2026-09-23 10:49:11.000000', b'1', 'Your timetable change request for subject ICT213 has been rejected by the administrator. Request rejected.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Rejected', 3),
(10, '2026-09-23 11:17:58.000000', b'1', 'Your timetable change request for ICT213 has been approved. Please check your timetable for the updated schedule.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', 3),
(11, '2026-09-30 09:36:23.000000', b'0', 'Your timetable change request for ECO212 has been approved. Please check your timetable for the updated schedule.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', 9),
(12, '2026-09-30 13:38:17.000000', b'0', 'Your timetable change request for ICT111 has been approved. Please check your timetable for the updated schedule.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', 2),
(13, '2026-11-03 18:44:16.000000', b'0', 'Your timetable change request for ECO212 has been approved. Please check your timetable for the updated schedule.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', 9),
(14, '2026-11-04 11:27:27.000000', b'0', 'Your timetable change request for subject ECO111 has been rejected by the administrator. Reject', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Rejected', 6),
(15, '2026-11-04 12:09:18.000000', b'0', 'Your timetable change request for ECO212 has been approved. Please check your timetable for the updated schedule.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', 9);

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `notification_id` bigint(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `is_read` bit(1) NOT NULL,
  `message` text NOT NULL,
  `notification_type` varchar(255) NOT NULL,
  `title` varchar(255) NOT NULL,
  `student_id` bigint(20) DEFAULT NULL,
  `lecturer_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`notification_id`, `created_at`, `is_read`, `message`, `notification_type`, `title`, `student_id`, `lecturer_id`) VALUES
(1, '2026-08-16 15:17:58.000000', b'1', 'The timetable for subject ICT111 has been updated. Please check your timetable for the new schedule.', 'TIMETABLE_CHANGE', 'Timetable Updated', 1, NULL),
(2, '2026-08-17 22:34:01.000000', b'1', 'The timetable for subject ICT111 has been rescheduled. Please check your timetable for the updated schedule.', 'TIMETABLE_RESCHEDULE', 'Timetable Rescheduled', 1, NULL),
(3, '2026-08-18 13:58:55.000000', b'1', 'The timetable for subject ICT111 has been updated. Please check your timetable for the new schedule.', 'TIMETABLE_UPDATE', 'Timetable Updated', 1, NULL),
(5, '2026-08-18 22:43:28.000000', b'1', 'The timetable for subject ICT111 has been updated. Please check your timetable for the new schedule.', 'TIMETABLE_CHANGE', 'Timetable Updated', 1, NULL),
(6, '2026-09-02 15:14:52.000000', b'1', 'The timetable for subject ICT111 has been updated. Please check your timetable for the new schedule.', 'TIMETABLE_CHANGE', 'Timetable Updated', 1, NULL),
(7, '2026-09-02 15:14:52.000000', b'0', 'Your timetable change request has been approved. Request approved.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', NULL, 2),
(8, '2026-09-02 15:14:53.000000', b'1', 'A timetable change request submitted by Nimal Silva has been approved by the administrator.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', NULL, 1),
(9, '2026-09-03 09:12:44.000000', b'1', 'The timetable for subject ICT111 has been updated. Please check your timetable for the new schedule.', 'TIMETABLE_CHANGE', 'Timetable Updated', 1, NULL),
(10, '2026-09-03 09:12:45.000000', b'0', 'Your timetable change request for subject ICT111 has been approved by the administrator. Request approved.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', NULL, 2),
(11, '2026-09-03 09:12:45.000000', b'1', 'The timetable change request for subject ICT111 submitted by Nimal Silva has been approved by the administrator. Please check the updated timetable.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', NULL, 1),
(12, '2026-09-03 18:44:10.000000', b'1', 'The timetable for subject ICT111 has been updated. Please check your timetable for the new schedule.', 'TIMETABLE_CHANGE', 'Timetable Updated', 1, NULL),
(13, '2026-09-03 18:44:10.000000', b'1', 'The timetable change request for subject ICT111 submitted by Nimal Silva has been approved by the administrator.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', NULL, 1),
(14, '2026-09-03 18:59:27.000000', b'1', 'The timetable for subject ICT111 has been updated. Please check your timetable for the new schedule.', 'TIMETABLE_CHANGE', 'Timetable Updated', 1, NULL),
(15, '2026-09-03 18:59:28.000000', b'1', 'The timetable change request for subject ICT111 submitted by Nimal Silva has been approved by the administrator.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', NULL, 1),
(16, '2026-09-04 08:30:32.000000', b'1', 'The timetable for subject ICT111 has been updated. Please check your timetable for the new schedule.', 'TIMETABLE_CHANGE', 'Timetable Updated', 1, NULL),
(17, '2026-09-04 08:30:33.000000', b'1', 'The timetable change request for subject ICT111 submitted by Nimal Silva has been approved by the administrator.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', NULL, 1),
(18, '2026-09-04 08:39:37.000000', b'1', 'The timetable for subject ICT111 has been updated. Please check your timetable for the new schedule.', 'TIMETABLE_CHANGE', 'Timetable Updated', 1, NULL),
(19, '2026-09-04 08:39:38.000000', b'1', 'The timetable change request for subject ICT111 submitted by Nimal Silva has been approved by the administrator.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', NULL, 1),
(20, '2026-09-04 08:46:48.000000', b'1', 'The timetable change request for subject ICT111 submitted by Nimal Silva has been rejected by the administrator.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Rejected', NULL, 1),
(21, '2026-09-22 19:32:52.000000', b'1', 'The timetable change request for subject ECO111 submitted by Shantha Perera has been rejected by the administrator.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Rejected', NULL, 7),
(22, '2026-09-23 10:24:33.000000', b'1', 'The timetable for subject ICT213 has been updated. Please check your timetable for the new schedule.', 'TIMETABLE_CHANGE', 'Timetable Updated', 15, NULL),
(23, '2026-09-23 10:24:33.000000', b'1', 'The timetable change request for subject ICT213 submitted by Kasun Fernando has been approved by the administrator.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', NULL, 1),
(24, '2026-09-23 10:49:11.000000', b'1', 'The timetable change request for subject ICT213 submitted by Kasun Fernando has been rejected by the administrator.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Rejected', NULL, 1),
(25, '2026-09-23 11:17:58.000000', b'1', 'The timetable for subject ICT213 has been updated. Please check your timetable for the new schedule.', 'TIMETABLE_CHANGE', 'Timetable Updated', 15, NULL),
(26, '2026-09-23 11:17:58.000000', b'1', 'The timetable change request for subject ICT213 submitted by Kasun Fernando has been approved by the administrator.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', NULL, 1),
(27, '2026-09-30 09:36:23.000000', b'0', 'The timetable for subject ECO212 has been updated. Please check your timetable for the new schedule.', 'TIMETABLE_CHANGE', 'Timetable Updated', 21, NULL),
(29, '2026-09-30 09:36:23.000000', b'0', 'The timetable change request for subject ECO212 submitted by Nisha Shamali has been approved by the administrator.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', NULL, 7),
(30, '2026-09-30 13:38:17.000000', b'0', 'The timetable for subject ICT111 has been updated. Please check your timetable for the new schedule.', 'TIMETABLE_CHANGE', 'Timetable Updated', 1, NULL),
(31, '2026-09-30 13:38:17.000000', b'0', 'The timetable for subject ICT111 has been updated. Please check your timetable for the new schedule.', 'TIMETABLE_CHANGE', 'Timetable Updated', 13, NULL),
(32, '2026-09-30 13:38:17.000000', b'0', 'The timetable change request for subject ICT111 submitted by Nimal Silva has been approved by the administrator.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', NULL, 1),
(33, '2026-11-03 18:44:16.000000', b'0', 'The timetable for subject ECO212 has been updated. Please check your timetable for the new schedule.', 'TIMETABLE_CHANGE', 'Timetable Updated', 21, NULL),
(34, '2026-11-03 18:44:16.000000', b'0', 'The timetable change request for subject ECO212 submitted by Nisha Shamali has been approved by the administrator.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', NULL, 7),
(35, '2026-11-04 11:27:27.000000', b'0', 'The timetable change request for subject ECO111 submitted by Shantha Perera has been rejected by the administrator.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Rejected', NULL, 7),
(36, '2026-11-04 12:09:18.000000', b'0', 'The timetable for subject ECO212 has been updated. Please check your timetable for the new schedule.', 'TIMETABLE_CHANGE', 'Timetable Updated', 21, NULL),
(37, '2026-11-04 12:09:18.000000', b'0', 'The timetable change request for subject ECO212 submitted by Nisha Shamali has been approved by the administrator.', 'TIMETABLE_CHANGE_REQUEST', 'Timetable Change Request Approved', NULL, 7);

-- --------------------------------------------------------

--
-- Table structure for table `reschedule_requests`
--

CREATE TABLE `reschedule_requests` (
  `request_id` bigint(20) NOT NULL,
  `admin_comment` text DEFAULT NULL,
  `processed_at` datetime(6) DEFAULT NULL,
  `reason` text NOT NULL,
  `requested_at` datetime(6) NOT NULL,
  `status` varchar(255) NOT NULL,
  `lecturer_id` bigint(20) NOT NULL,
  `requested_classroom_id` bigint(20) DEFAULT NULL,
  `requested_time_slot_id` bigint(20) DEFAULT NULL,
  `timetable_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reschedule_requests`
--

INSERT INTO `reschedule_requests` (`request_id`, `admin_comment`, `processed_at`, `reason`, `requested_at`, `status`, `lecturer_id`, `requested_classroom_id`, `requested_time_slot_id`, `timetable_id`) VALUES
(1, 'Reschedule request approved.', '2026-08-17 22:34:01.000000', 'The current classroom is unavailable and the class needs to be rescheduled.', '2026-08-17 22:27:48.000000', 'APPROVED', 2, 2, 1, 1),
(2, 'The requested classroom is not available.', '2026-08-17 22:44:31.000000', 'The current classroom is unavailable and the class needs to be rescheduled.', '2026-08-17 22:42:47.000000', 'REJECTED', 2, 1, 1, 1);

-- --------------------------------------------------------

--
-- Table structure for table `specializations`
--

CREATE TABLE `specializations` (
  `specialization_id` bigint(20) NOT NULL,
  `active` bit(1) NOT NULL,
  `specialization_code` varchar(255) NOT NULL,
  `specialization_name` varchar(255) NOT NULL,
  `department_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `specializations`
--

INSERT INTO `specializations` (`specialization_id`, `active`, `specialization_code`, `specialization_name`, `department_id`) VALUES
(1, b'1', 'ICT', 'Information and Communication Technology', 1),
(2, b'1', 'JPN', 'Japanese', 5),
(3, b'1', 'TRL', 'Translation Studies', 5),
(4, b'1', 'CEL', 'Compulsary English Language', 2),
(5, b'1', 'TESL', 'Teaching English as a Second Language', 2),
(6, b'1', 'ECO', 'Economics', 3),
(7, b'1', 'STC', 'Statistics', 3),
(8, b'1', 'SHN', 'Sinhala', 5),
(9, b'1', 'TML', 'Tamil', 5),
(10, b'1', 'ENG', 'English', 5),
(11, b'1', 'GMN', 'German', 5),
(12, b'1', 'CHN', 'Chinese', 5),
(13, b'1', 'HND', 'Hindi', 5),
(14, b'1', 'GEO', 'Geography', 4),
(15, b'1', 'CIT', 'Compulsary Information Technology', 1),
(16, b'1', 'CGS', 'Compulsary General Subjects', 6),
(17, b'1', 'POL', 'Political Science', 6),
(18, b'1', 'SOC', 'Sociology', 6);

-- --------------------------------------------------------

--
-- Table structure for table `students`
--

CREATE TABLE `students` (
  `student_id` bigint(20) NOT NULL,
  `email` varchar(255) NOT NULL,
  `registration_number` varchar(255) NOT NULL,
  `semester` int(11) NOT NULL,
  `student_name` varchar(255) NOT NULL,
  `year_level` int(11) NOT NULL,
  `major_specialization_id` bigint(20) DEFAULT NULL,
  `minor_specialization_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `degree_type` enum('GENERAL','SPECIAL') DEFAULT NULL,
  `registration_completed` bit(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `students`
--

INSERT INTO `students` (`student_id`, `email`, `registration_number`, `semester`, `student_name`, `year_level`, `major_specialization_id`, `minor_specialization_id`, `user_id`, `degree_type`, `registration_completed`) VALUES
(1, 'santhisi@gmail.com', '24SSL1234', 1, 'Santhisi Silva', 1, NULL, NULL, 5, NULL, b'1'),
(2, 'amal@gmail.com', '23SSL1234', 1, 'Amal Perera', 2, 1, 2, 6, 'SPECIAL', b'1'),
(3, 'kamal@gmail.com', '22SSL1234', 1, 'Kamal Silva', 3, 1, 2, 7, 'GENERAL', NULL),
(4, 'nadeesha@gmail.com', '21SSL1234', 2, 'Nadeesha Perera', 4, 1, NULL, 8, 'SPECIAL', NULL),
(5, 'newstudent@gmail.com', '23SSL5678', 1, 'New Student', 2, 6, 7, 9, 'GENERAL', NULL),
(13, 'aa@gmail.com', '24SSL0000', 1, 'aaaa', 1, NULL, NULL, 21, NULL, b'1'),
(14, 'f@gmail.com', '23SSL0000', 1, 'fff', 2, 1, 8, 22, 'SPECIAL', b'1'),
(15, 'nethulidissa@gmail.com', '23SSL1111', 1, 'N.H.Dissanayake', 2, 1, 6, 23, 'SPECIAL', b'1'),
(16, 'dani@gmail.com', '23SSL2222', 2, 'D.S.Perera', 2, 6, 1, 24, 'GENERAL', b'1'),
(17, 'esandi@gmail.com', '22SSL1111', 1, 'E.S.Silva', 3, 1, 6, 25, 'GENERAL', b'1'),
(18, 'yuthmik@gmail.com', '22SSL3333', 2, 'Y.K.Perera', 3, 1, 6, 26, 'SPECIAL', b'1'),
(19, 'roshanichamo@gmail.com', '22SSL4444', 1, 'R.C.Kulathunga', 3, 1, 6, 27, 'SPECIAL', b'1'),
(20, 'pasindu@gmail.com', '22SSL5555', 2, 'P.O.Perera', 3, 1, 6, 28, 'GENERAL', b'1'),
(21, 'amanda@gmail.com', '23SSL3333', 1, 'W.A.Silva', 2, 1, 6, 29, 'GENERAL', b'1'),
(22, 'sanduni@gmail.com', '23SSL6666', 2, 'S.A.Silva', 2, 1, 6, 30, 'SPECIAL', b'1'),
(23, 'lalithmaduranga@gmail.com', '21SSL0000', 1, 'L.M.Silva', 4, 6, NULL, 31, 'SPECIAL', b'1'),
(29, 'menukisilva@gmail.com', '24SSL8989', 1, 'Menuki Silva', 1, NULL, NULL, 41, NULL, b'0');

-- --------------------------------------------------------

--
-- Table structure for table `student_subject_registrations`
--

CREATE TABLE `student_subject_registrations` (
  `registration_id` bigint(20) NOT NULL,
  `academic_year` int(11) NOT NULL,
  `semester` int(11) NOT NULL,
  `student_id` bigint(20) NOT NULL,
  `subject_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `student_subject_registrations`
--

INSERT INTO `student_subject_registrations` (`registration_id`, `academic_year`, `semester`, `student_id`, `subject_id`) VALUES
(1, 1, 1, 1, 1),
(2, 1, 1, 1, 2),
(3, 1, 1, 1, 3),
(4, 1, 1, 1, 4),
(5, 1, 1, 1, 5),
(6, 1, 1, 1, 6),
(44, 2, 1, 2, 11),
(45, 2, 1, 2, 14),
(46, 2, 1, 2, 17),
(48, 1, 1, 13, 1),
(38, 1, 1, 13, 2),
(39, 1, 1, 13, 3),
(40, 1, 1, 13, 4),
(47, 1, 1, 13, 6),
(49, 1, 1, 13, 7),
(41, 2, 1, 14, 11),
(42, 2, 1, 14, 14),
(43, 2, 1, 14, 17),
(50, 2, 1, 15, 11),
(51, 2, 1, 15, 14),
(52, 2, 1, 15, 17),
(56, 2, 1, 15, 21),
(53, 2, 1, 15, 49),
(54, 2, 1, 15, 50),
(55, 2, 1, 15, 51),
(57, 2, 2, 16, 12),
(58, 2, 2, 16, 15),
(59, 2, 2, 16, 18),
(60, 2, 2, 16, 24),
(61, 2, 2, 16, 25),
(62, 2, 2, 16, 52),
(63, 2, 2, 16, 53),
(67, 3, 1, 17, 27),
(64, 3, 1, 17, 55),
(65, 3, 1, 17, 56),
(66, 3, 1, 17, 57),
(93, 3, 2, 18, 37),
(88, 3, 2, 18, 60),
(89, 3, 2, 18, 61),
(90, 3, 2, 18, 62),
(91, 3, 2, 18, 63),
(92, 3, 2, 18, 64),
(73, 3, 1, 19, 27),
(68, 3, 1, 19, 55),
(69, 3, 1, 19, 56),
(70, 3, 1, 19, 57),
(71, 3, 1, 19, 58),
(72, 3, 1, 19, 59),
(98, 3, 2, 20, 37),
(94, 3, 2, 20, 60),
(95, 3, 2, 20, 61),
(96, 3, 2, 20, 62),
(97, 3, 2, 20, 65),
(74, 2, 1, 21, 11),
(75, 2, 1, 21, 14),
(76, 2, 1, 21, 17),
(79, 2, 1, 21, 21),
(80, 2, 1, 21, 22),
(77, 2, 1, 21, 49),
(78, 2, 1, 21, 50),
(81, 2, 2, 22, 12),
(82, 2, 2, 22, 15),
(83, 2, 2, 22, 18),
(87, 2, 2, 22, 24),
(84, 2, 2, 22, 52),
(85, 2, 2, 22, 53),
(86, 2, 2, 22, 54),
(99, 4, 1, 23, 43),
(100, 4, 1, 23, 44),
(101, 4, 1, 23, 45),
(102, 4, 1, 23, 46),
(103, 4, 1, 23, 47),
(115, 1, 1, 29, 2),
(116, 1, 1, 29, 3),
(117, 1, 1, 29, 4);

-- --------------------------------------------------------

--
-- Table structure for table `subjects`
--

CREATE TABLE `subjects` (
  `subject_id` bigint(20) NOT NULL,
  `academic_year` int(11) NOT NULL,
  `compulsory` bit(1) NOT NULL,
  `credits` int(11) NOT NULL,
  `semester` int(11) NOT NULL,
  `subject_code` varchar(255) NOT NULL,
  `subject_name` varchar(255) NOT NULL,
  `department_id` bigint(20) NOT NULL,
  `specialization_id` bigint(20) NOT NULL,
  `active` bit(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `subjects`
--

INSERT INTO `subjects` (`subject_id`, `academic_year`, `compulsory`, `credits`, `semester`, `subject_code`, `subject_name`, `department_id`, `specialization_id`, `active`) VALUES
(1, 1, b'0', 3, 1, 'ICT111', 'Computer Architecture and Fundamentals of Programming', 1, 1, b'0'),
(2, 1, b'1', 3, 1, 'CEL111', 'English Language - Level 1', 2, 4, b'0'),
(3, 1, b'1', 3, 1, 'CIT111', 'Preparatory CIT Part I', 1, 15, b'0'),
(4, 1, b'1', 3, 1, 'CGS111', 'Mother Tongue', 6, 16, b'0'),
(5, 1, b'0', 3, 1, 'JPN111', 'Japanese Language I', 5, 2, b'0'),
(6, 1, b'0', 3, 1, 'TESL111', 'Introduction to TESL', 2, 5, b'0'),
(7, 1, b'0', 3, 1, 'ECO111', 'Introduction to Microeconomics', 3, 6, b'0'),
(10, 1, b'1', 3, 2, 'CEL121', 'English Language - Level 2', 2, 4, b'0'),
(11, 2, b'1', 2, 1, 'CEL211', 'English Language - Level 3', 2, 4, b'0'),
(12, 2, b'1', 2, 2, 'CEL221', 'English Language -Level 4', 2, 4, b'0'),
(13, 1, b'1', 3, 2, 'CIT121', 'Preparatory CIT Part II', 1, 15, b'0'),
(14, 2, b'1', 2, 1, 'CIT211', 'Principles in Web Design', 1, 15, b'0'),
(15, 2, b'1', 2, 2, 'CIT221', 'Advanced Data Analysis Tools', 1, 15, b'0'),
(16, 1, b'1', 2, 2, 'CGS121', 'Basic Mathematics', 6, 16, b'0'),
(17, 2, b'1', 2, 1, 'CGS211', 'Third Language', 6, 16, b'0'),
(18, 2, b'1', 2, 2, 'CGS221', 'Soft Skills', 6, 16, b'0'),
(19, 1, b'0', 3, 2, 'ECO121', 'Introduction to Macroeconomics', 3, 6, b'0'),
(21, 2, b'0', 3, 1, 'ECO211', 'Intermediate Microeconomics', 3, 6, b'0'),
(22, 2, b'0', 3, 1, 'ECO212', 'Economy of Sri Lanka', 3, 6, b'0'),
(23, 2, b'0', 3, 1, 'ECO213', 'Mathematics for Economics', 3, 6, b'0'),
(24, 2, b'0', 3, 2, 'ECO221', 'Intermediate Macroeconomics', 3, 6, b'0'),
(25, 2, b'0', 3, 2, 'ECO222', 'Applied Economics', 3, 6, b'0'),
(26, 2, b'0', 3, 2, 'ECO223', 'Advanced Microeconomics', 3, 6, b'0'),
(27, 3, b'0', 3, 1, 'ECO311', 'International Economics', 3, 6, b'0'),
(34, 3, b'0', 3, 1, 'ECO313', 'Public Finance', 3, 6, b'0'),
(35, 3, b'0', 3, 1, 'ECO314', 'Statistics for Economics', 3, 6, b'0'),
(36, 3, b'0', 3, 1, 'ECO315', 'Advanced Macroeconomics', 3, 6, b'0'),
(37, 3, b'0', 3, 2, 'ECO321', 'Project Evaluation', 3, 6, b'0'),
(38, 3, b'0', 3, 2, 'ECO322', 'Comparative Economic Systems', 3, 6, b'0'),
(39, 3, b'0', 3, 2, 'ECO323', 'Agricultural Economics', 3, 6, b'0'),
(40, 3, b'0', 3, 2, 'ECO324', 'Development Economics', 3, 6, b'0'),
(41, 3, b'0', 3, 2, 'ECO325', 'Econometrics I', 3, 6, b'0'),
(42, 3, b'0', 3, 2, 'ECO326', 'Scientific Report Writing', 3, 6, b'0'),
(43, 4, b'0', 3, 1, 'ECO411', 'Research Methods for Economics', 3, 6, b'0'),
(44, 4, b'0', 3, 1, 'ECO412', 'Econometrics II', 3, 6, b'0'),
(45, 4, b'0', 3, 1, 'ECO413', 'Business Economics', 3, 6, b'0'),
(46, 4, b'0', 3, 1, 'ECO414', 'Environmental Economics', 3, 6, b'0'),
(47, 4, b'0', 3, 1, 'ECO415', 'Human Resource Economics', 3, 6, b'0'),
(48, 1, b'0', 3, 2, 'ICT121', 'Information Systems (IS)', 1, 1, b'0'),
(49, 2, b'0', 3, 1, 'ICT211', 'Social and Professional Issues in IT', 1, 1, b'0'),
(50, 2, b'0', 3, 1, 'ICT212', 'Fundamentals of System Design', 1, 1, b'0'),
(51, 2, b'0', 3, 1, 'ICT213', 'E-Commerce Technology', 1, 1, b'0'),
(52, 2, b'0', 3, 2, 'ICT221', 'Database Management Systems', 1, 1, b'0'),
(53, 2, b'0', 3, 2, 'ICT222', 'Applications of System Design', 1, 1, b'0'),
(54, 2, b'0', 3, 2, 'ICT223', 'ICT Base Interactive Learning & Teaching Methods', 1, 1, b'0'),
(55, 3, b'0', 3, 1, 'ICT311', 'Advanced Web Development', 1, 1, b'0'),
(56, 3, b'0', 3, 1, 'ICT312', 'Data Communication and Computer Network', 1, 1, b'0'),
(57, 3, b'0', 3, 1, 'ICT313', 'Advanced Mathematics for ICT', 1, 1, b'0'),
(58, 3, b'0', 3, 1, 'ICT314', 'Object Oriented Programming Languages', 1, 1, b'0'),
(59, 3, b'0', 3, 1, 'ICT315', 'Application of Open Sources Software', 1, 1, b'0'),
(60, 3, b'0', 3, 2, 'ICT321', 'Application of Interactive Multimedia Design', 1, 1, b'0'),
(61, 3, b'0', 3, 2, 'ICT322', 'Computer Hardware Technology and Troubleshooting', 1, 1, b'0'),
(62, 3, b'0', 3, 2, 'ICT323', 'Computer Aided Design (CAD)', 1, 1, b'0'),
(63, 3, b'0', 3, 2, 'ICT324', 'Graphics Design Applications', 1, 1, b'0'),
(64, 3, b'0', 3, 2, 'ICT325', 'Object Oriented System Design', 1, 1, b'0'),
(65, 3, b'0', 3, 2, 'ICT326', 'Scientific Report Writing', 1, 1, b'0'),
(72, 4, b'0', 3, 1, 'ICT411', 'Research Methodology', 1, 1, b'1'),
(73, 4, b'0', 3, 1, 'ICT412', 'Audio and Video Editing Technology', 1, 1, b'1'),
(74, 4, b'0', 3, 1, 'ICT413', 'Object Oriented Programming', 1, 1, b'1'),
(75, 4, b'0', 3, 1, 'ICT414', 'Application of System Development', 1, 1, b'1'),
(76, 4, b'0', 3, 1, 'ICT415', 'Human Resource Management', 1, 1, b'1');

-- --------------------------------------------------------

--
-- Table structure for table `timetables`
--

CREATE TABLE `timetables` (
  `timetable_id` bigint(20) NOT NULL,
  `active` bit(1) NOT NULL,
  `semester` enum('SEMESTER_1','SEMESTER_2') NOT NULL,
  `year_level` enum('YEAR_1','YEAR_2','YEAR_3','YEAR_4') NOT NULL,
  `classroom_id` bigint(20) NOT NULL,
  `lecturer_allocation_id` bigint(20) NOT NULL,
  `specialization_id` bigint(20) NOT NULL,
  `time_slot_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `timetables`
--

INSERT INTO `timetables` (`timetable_id`, `active`, `semester`, `year_level`, `classroom_id`, `lecturer_allocation_id`, `specialization_id`, `time_slot_id`) VALUES
(1, b'1', 'SEMESTER_1', 'YEAR_1', 2, 1, 1, 21),
(2, b'1', 'SEMESTER_1', 'YEAR_1', 2, 2, 6, 8),
(3, b'1', 'SEMESTER_1', 'YEAR_2', 1, 3, 1, 23),
(4, b'1', 'SEMESTER_1', 'YEAR_3', 1, 4, 1, 36),
(5, b'1', 'SEMESTER_1', 'YEAR_2', 10, 5, 6, 6),
(6, b'1', 'SEMESTER_1', 'YEAR_2', 5, 7, 6, 29),
(8, b'1', 'SEMESTER_1', 'YEAR_3', 1, 10, 1, 37),
(9, b'1', 'SEMESTER_1', 'YEAR_3', 7, 11, 6, 38),
(10, b'1', 'SEMESTER_1', 'YEAR_4', 1, 14, 1, 7),
(11, b'1', 'SEMESTER_1', 'YEAR_4', 1, 14, 1, 6);

-- --------------------------------------------------------

--
-- Table structure for table `timetable_change_requests`
--

CREATE TABLE `timetable_change_requests` (
  `request_id` bigint(20) NOT NULL,
  `admin_response` text DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `processed_at` datetime(6) DEFAULT NULL,
  `reason` text NOT NULL,
  `requested_classroom` varchar(255) DEFAULT NULL,
  `requested_day` varchar(255) DEFAULT NULL,
  `requested_end_time` varchar(255) DEFAULT NULL,
  `requested_start_time` varchar(255) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `lecturer_id` bigint(20) NOT NULL,
  `timetable_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `timetable_change_requests`
--

INSERT INTO `timetable_change_requests` (`request_id`, `admin_response`, `created_at`, `processed_at`, `reason`, `requested_classroom`, `requested_day`, `requested_end_time`, `requested_start_time`, `status`, `lecturer_id`, `timetable_id`) VALUES
(5, 'Request approved.', '2026-09-02 15:01:20.000000', '2026-09-02 15:14:52.000000', 'Time slot is unavailable', 'C1', 'TUESDAY', '09:30', '08:00', 'APPROVED', 2, 1),
(6, 'Request approved.', '2026-09-02 19:37:59.000000', '2026-09-03 09:12:44.000000', 'Time slot is unavailable', 'C1', 'MONDAY', '09:30', '08:00', 'APPROVED', 2, 1),
(7, 'Request approved.', '2026-09-03 18:43:51.000000', '2026-09-03 18:44:10.000000', 'Time slot is unavailable', 'C1', 'TUESDAY', '09:30', '08:00', 'APPROVED', 2, 1),
(8, 'Request approved.', '2026-09-03 18:57:38.000000', '2026-09-03 18:59:27.000000', 'Time slot is unavailable', 'C1', 'MONDAY', '09:30', '08:00', 'APPROVED', 2, 1),
(9, 'Request approved.', '2026-09-04 08:30:13.000000', '2026-09-04 08:30:30.000000', 'Classroom is unavailable', 'CC1', NULL, '09:30', '08:00', 'APPROVED', 2, 1),
(10, 'Request approved.', '2026-09-04 08:39:20.000000', '2026-09-04 08:39:37.000000', 'Classroom is unavailable', 'C1', NULL, '09:30', '08:00', 'APPROVED', 2, 1),
(11, 'Request rejected.', '2026-09-04 08:45:58.000000', '2026-09-04 08:46:48.000000', 'Classroom is unavailable', 'CC1', NULL, '09:30', '08:00', 'REJECTED', 2, 1),
(13, 'Request approved.', '2026-09-23 10:23:03.000000', '2026-09-23 10:24:33.000000', 'Time slot is unavailable', 'CC1', 'WEDNESDAY', '12:30', '11:00', 'APPROVED', 3, 3),
(14, 'Request rejected.', '2026-09-23 10:47:33.000000', '2026-09-23 10:49:11.000000', 'Time slot is unavailable.', 'CC1', 'WEDNESDAY', '12:30', '11:00', 'REJECTED', 3, 3),
(15, 'Request approved.', '2026-09-23 10:50:36.000000', '2026-09-23 11:17:58.000000', 'The time slot is unavailable.', 'CC1', 'WEDNESDAY', '12:30', '11:00', 'APPROVED', 3, 3),
(23, 'Request approved.', '2026-11-03 18:10:05.000000', '2026-11-03 18:44:16.000000', 'AAAAAA', 'B3', 'TUESDAY', '12:30', '11:00', 'APPROVED', 9, 5),
(25, 'Reject', '2026-11-04 11:23:33.000000', '2026-11-04 11:27:27.000000', 'Time slot is unavailable.', 'C1', 'WEDNESDAY', '09:30', '08:00', 'REJECTED', 6, 2),
(26, 'Request approved.', '2026-11-04 11:26:20.000000', '2026-11-04 12:09:18.000000', 'Time slot is unavailable.', 'B3', 'TUESDAY', '18:00', '16:30', 'APPROVED', 9, 5),
(27, NULL, '2026-11-04 12:04:29.000000', NULL, 'Time slot is unavailable.', 'CC1', 'FRIDAY', '12:30', '11:00', 'PENDING', 1, 4);

-- --------------------------------------------------------

--
-- Table structure for table `timetable_entries`
--

CREATE TABLE `timetable_entries` (
  `timetable_id` bigint(20) NOT NULL,
  `academic_year` int(11) NOT NULL,
  `day` varchar(255) NOT NULL,
  `end_time` varchar(255) NOT NULL,
  `room` varchar(255) NOT NULL,
  `semester` int(11) NOT NULL,
  `start_time` varchar(255) NOT NULL,
  `lecturer_id` bigint(20) NOT NULL,
  `subject_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `time_slots`
--

CREATE TABLE `time_slots` (
  `time_slot_id` bigint(20) NOT NULL,
  `active` bit(1) NOT NULL,
  `day` enum('FRIDAY','MONDAY','THURSDAY','TUESDAY','WEDNESDAY') NOT NULL,
  `end_time` time NOT NULL,
  `slot_number` int(11) NOT NULL,
  `start_time` time NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `time_slots`
--

INSERT INTO `time_slots` (`time_slot_id`, `active`, `day`, `end_time`, `slot_number`, `start_time`) VALUES
(1, b'1', 'MONDAY', '09:30:00', 1, '08:00:00'),
(2, b'1', 'MONDAY', '11:00:00', 2, '09:30:00'),
(3, b'1', 'MONDAY', '12:30:00', 3, '11:00:00'),
(4, b'1', 'MONDAY', '15:00:00', 4, '13:30:00'),
(5, b'1', 'MONDAY', '16:30:00', 5, '15:00:00'),
(6, b'1', 'MONDAY', '18:00:00', 6, '16:30:00'),
(7, b'1', 'MONDAY', '19:30:00', 7, '18:00:00'),
(8, b'1', 'TUESDAY', '09:30:00', 1, '08:00:00'),
(10, b'1', 'TUESDAY', '11:00:00', 2, '09:30:00'),
(11, b'1', 'TUESDAY', '12:30:00', 3, '11:00:00'),
(12, b'1', 'TUESDAY', '15:00:00', 4, '13:30:00'),
(13, b'1', 'TUESDAY', '16:30:00', 5, '15:00:00'),
(19, b'1', 'TUESDAY', '18:00:00', 7, '16:30:00'),
(20, b'1', 'TUESDAY', '19:30:00', 8, '18:00:00'),
(21, b'1', 'WEDNESDAY', '09:30:00', 1, '08:00:00'),
(22, b'1', 'WEDNESDAY', '11:00:00', 2, '09:30:00'),
(23, b'1', 'WEDNESDAY', '12:30:00', 3, '11:00:00'),
(24, b'1', 'WEDNESDAY', '15:00:00', 4, '13:30:00'),
(25, b'1', 'WEDNESDAY', '16:30:00', 5, '15:00:00'),
(26, b'1', 'WEDNESDAY', '18:00:00', 6, '16:30:00'),
(27, b'1', 'WEDNESDAY', '19:30:00', 7, '18:00:00'),
(28, b'1', 'THURSDAY', '09:30:00', 1, '08:00:00'),
(29, b'1', 'THURSDAY', '11:00:00', 2, '09:30:00'),
(30, b'1', 'THURSDAY', '12:30:00', 3, '11:00:00'),
(31, b'1', 'THURSDAY', '15:00:00', 4, '13:30:00'),
(32, b'1', 'THURSDAY', '16:30:00', 5, '15:00:00'),
(33, b'1', 'THURSDAY', '18:00:00', 6, '16:30:00'),
(34, b'1', 'THURSDAY', '19:30:00', 7, '18:00:00'),
(35, b'1', 'FRIDAY', '09:30:00', 1, '08:00:00'),
(36, b'1', 'FRIDAY', '11:00:00', 2, '09:30:00'),
(37, b'1', 'FRIDAY', '12:30:00', 3, '11:00:00'),
(38, b'1', 'FRIDAY', '15:00:00', 4, '13:30:00'),
(39, b'1', 'FRIDAY', '16:30:00', 5, '15:00:00'),
(40, b'1', 'FRIDAY', '18:00:00', 6, '16:30:00'),
(41, b'1', 'FRIDAY', '19:30:00', 7, '18:00:00');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` bigint(20) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ADMIN','HOD','LECTURER','STUDENT') NOT NULL,
  `username` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `enabled`, `password`, `role`, `username`) VALUES
(1, b'1', '$2a$10$VGRBeacfVhltUPFZWMPIAu6j03qRlYlweDIcvBdMt9lYzFNLXPADu', 'ADMIN', 'admin'),
(2, b'1', '$2a$10$EWlNfYkIgglRttoe1q/BA.qLJ1fTAaip0cWphTQycQ/XedwHbPcoC', 'HOD', 'hod_ict'),
(3, b'1', '$2a$10$HzI5yePjMXTUlQJrWZNCYeA3yvAKP2Ff5YmhV1oGgzV8E9Cj2TbT.', 'LECTURER', 'lec_nimal'),
(4, b'1', '$2a$10$Q5R1Mq8zP5zBMhq.qC8YMOkKlYmAqNQFI37tIcDnP7DHNnYH8NYSm', 'LECTURER', 'lec_kasun'),
(5, b'1', '$2a$10$f7a88HW93.rbTw78UKZ9CulxXnOzse5rqYvKxhbR73W9PnBwUU9VS', 'STUDENT', '24SSL1234'),
(6, b'1', '$2a$10$EFS9nfidtHnmoADrrhuLD.eOlv9.aTlguaQ0.LAeuPuN6crU4jRpa', 'STUDENT', '23SSL1234'),
(7, b'1', '$2a$10$7sCqJ0ESC4sTJvtVxKIW0O5xXALadnGW4XjdN.XE5YnkHU0ahg9X.', 'STUDENT', '22SSL1234'),
(8, b'1', '$2a$10$4MJPlmApCe.LBPAJshgNmOcPUhv5y4i6VfRax2lmgFvKPw6eAqr42', 'STUDENT', '21SSL1234'),
(9, b'0', '$2a$10$aovzh.MPIRKX2KCvqrDZE.bvMvo7HnRRD1dWbRQGj2W3sRyqsSqU2', 'STUDENT', '23SSL5678'),
(14, b'1', '$2a$10$PVT2OMalzPlzv9Y5Bzys3uQLGcS9iWOM/9aKk/bQBM4FR3M7Asfby', 'LECTURER', 'LEC004'),
(21, b'1', '$2a$10$puQmrqpt0qK46dzeLf9Pw.8jlb.Z.ahmpnwwkhUQVUupigsZTv.4e', 'STUDENT', '24SSL0000'),
(22, b'1', '$2a$10$nVsGbX1pW/CI0ljg8FFCSeyaCgzGZHtrYkakuD8zDdDvKeGz5T/Ke', 'STUDENT', '23SSL0000'),
(23, b'1', '$2a$10$QVcWr20XnqUSU8e/zCltE.znSlkCK8lMMrsXqJR8gY0yc4DD2whkS', 'STUDENT', '23SSL1111'),
(24, b'1', '$2a$10$maXyah1Ub1OIQE5cdVN6Ie56xgKBDOXxCPUBNqsTgrAfxw4jT2seS', 'STUDENT', '23SSL2222'),
(25, b'1', '$2a$10$fHwcfjwbyw6.wsafcxoWouj1rVG50d9UpPTYPx1a6sZuiToxK7USq', 'STUDENT', '22SSL1111'),
(26, b'1', '$2a$10$qTWfvnV6/s50OCUZi6ogzO35pKeUAYYKCrcc2oV/urG2kiafuVOty', 'STUDENT', '22SSL3333'),
(27, b'1', '$2a$10$U2BYJA4K944Rv.EF56GC7.F7Cxv26bwMfONkkd4MP32xhtpjw7xpm', 'STUDENT', '22SSL4444'),
(28, b'1', '$2a$10$HLdU.r4cCFg7IRtr0SIiHOQC7hJ7lMfcFRX7H4R/m59jsoO3Uet.m', 'STUDENT', '22SSL5555'),
(29, b'1', '$2a$10$yb1mVNAt7XFTdtKmpqcJLu7uu5wJLu7OmlMaei/Yl1WKwGlRlimnK', 'STUDENT', '23SSL3333'),
(30, b'1', '$2a$10$HXRAN023jFRyY5FKlO4tFelzQ6dFRC6FlMOy5lzHMLIPU7L5X9dCe', 'STUDENT', '23SSL6666'),
(31, b'1', '$2a$10$K7mJ8ix8s4LkY3Mgifclr.DhVw6RG0dVuSXxj1vC/mOuDRuaKpbb6', 'STUDENT', '21SSL0000'),
(32, b'1', '$2a$10$UvEmTrykh/UvQqxtaRMqge7Y.ckzvTOHtq/6n41CMUm75wOO9j9D2', 'HOD', 'LEC005'),
(33, b'1', '$2a$10$4SsweTQzTcsPli2DcqkshuRt7qE8l3i8.MbON0H.h4/bKRS7mYJYO', 'LECTURER', 'LEC006'),
(35, b'1', '$2a$10$6A3zGDXkVrbXKlCMiFNYhOy7pD7MJ7D14BKr1OnDjzBfSML0z.VFm', 'LECTURER', 'LEC007'),
(41, b'1', '$2a$10$Zfsq86Hl702cjBLghRnOuO4OM4Xzboww7t8EjVuHAnkEYqDxn0av.', 'STUDENT', '24SSL8989');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `classrooms`
--
ALTER TABLE `classrooms`
  ADD PRIMARY KEY (`classroom_id`),
  ADD UNIQUE KEY `UK9nv0tn7jsqbpkl3sw5m8ljbms` (`room_code`);

--
-- Indexes for table `departments`
--
ALTER TABLE `departments`
  ADD PRIMARY KEY (`department_id`),
  ADD UNIQUE KEY `UK89g8qie2y696a3tarmty43sq9` (`department_code`);

--
-- Indexes for table `lecturers`
--
ALTER TABLE `lecturers`
  ADD PRIMARY KEY (`lecturer_id`),
  ADD UNIQUE KEY `UK843xkfafq5501ytxnj4p5w85j` (`email`),
  ADD UNIQUE KEY `UKk6vsb5jy1uq700gx4a6brvtpv` (`lecturer_code`),
  ADD UNIQUE KEY `UK6m2kb7yyubmpyo8f349gbrq2y` (`user_id`),
  ADD KEY `FKpowy6g9rglsdytsdu6c9fymox` (`department_id`);

--
-- Indexes for table `lecturer_allocations`
--
ALTER TABLE `lecturer_allocations`
  ADD PRIMARY KEY (`allocation_id`),
  ADD KEY `FK406de5tof8honc5q0fgxl0rhs` (`lecturer_id`),
  ADD KEY `FK2c1ljor6ys26ai16dp7iaiapo` (`subject_id`);

--
-- Indexes for table `lecturer_notifications`
--
ALTER TABLE `lecturer_notifications`
  ADD PRIMARY KEY (`notification_id`),
  ADD KEY `FKljxid9qdap3b2gf5bd6p5twdi` (`lecturer_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`notification_id`),
  ADD KEY `FKlchct0805t9w7mapbehsi6dcw` (`student_id`),
  ADD KEY `fk_notification_lecturer` (`lecturer_id`);

--
-- Indexes for table `reschedule_requests`
--
ALTER TABLE `reschedule_requests`
  ADD PRIMARY KEY (`request_id`),
  ADD KEY `FKafxnc6p8kg4ghj2kllbwsco73` (`lecturer_id`),
  ADD KEY `FK19xu6h8a2g5j9pqxwncov1e2k` (`requested_classroom_id`),
  ADD KEY `FKom3lorlyvht1jgk0tpmp6xprc` (`requested_time_slot_id`),
  ADD KEY `FK5s8f2ofp1ghat1qkmrdmw2t12` (`timetable_id`);

--
-- Indexes for table `specializations`
--
ALTER TABLE `specializations`
  ADD PRIMARY KEY (`specialization_id`),
  ADD UNIQUE KEY `UKgy8dakj283hqsmg0hneowcylv` (`specialization_code`),
  ADD KEY `FKmbosp83ckb8d8yjponpvhmbp8` (`department_id`);

--
-- Indexes for table `students`
--
ALTER TABLE `students`
  ADD PRIMARY KEY (`student_id`),
  ADD UNIQUE KEY `UKe2rndfrsx22acpq2ty1caeuyw` (`email`),
  ADD UNIQUE KEY `UKkmee6ogkot83ohmeuid4c6omh` (`registration_number`),
  ADD UNIQUE KEY `UKg4fwvutq09fjdlb4bb0byp7t` (`user_id`),
  ADD KEY `FKqq0lvcjmlm70ieaitswnbo5np` (`major_specialization_id`),
  ADD KEY `FKkmmq7d44c9fng7fqq9tn3ma6a` (`minor_specialization_id`);

--
-- Indexes for table `student_subject_registrations`
--
ALTER TABLE `student_subject_registrations`
  ADD PRIMARY KEY (`registration_id`),
  ADD UNIQUE KEY `UK69vor5v5vat010dnv4k5ab5el` (`student_id`,`subject_id`,`academic_year`,`semester`),
  ADD KEY `FKj494fbxc076mksd58nfy2vr47` (`subject_id`);

--
-- Indexes for table `subjects`
--
ALTER TABLE `subjects`
  ADD PRIMARY KEY (`subject_id`),
  ADD UNIQUE KEY `UKqt734ivq9gq4yo4p1j1lhhk8l` (`subject_code`),
  ADD KEY `FKgh0j5ejuox2kr2av0l8158c0a` (`department_id`),
  ADD KEY `FKi2b54mc3ihs41kf9lfdb6lk5i` (`specialization_id`);

--
-- Indexes for table `timetables`
--
ALTER TABLE `timetables`
  ADD PRIMARY KEY (`timetable_id`),
  ADD UNIQUE KEY `UKkxp8tfses8dwch91q6l53ix0p` (`classroom_id`,`time_slot_id`),
  ADD UNIQUE KEY `UKhgrycivwjguq6n9t3ldfe1oxa` (`lecturer_allocation_id`,`time_slot_id`),
  ADD KEY `FKelbkeaoovsi57wwkj3jcv5ti3` (`specialization_id`),
  ADD KEY `FKd38thoowj328e3dnm48gk3m4j` (`time_slot_id`);

--
-- Indexes for table `timetable_change_requests`
--
ALTER TABLE `timetable_change_requests`
  ADD PRIMARY KEY (`request_id`),
  ADD KEY `FK3locueyxn9om9c9mo6bj24rd8` (`lecturer_id`),
  ADD KEY `FKkc8iof83eu4i4mde831h6ea0g` (`timetable_id`);

--
-- Indexes for table `timetable_entries`
--
ALTER TABLE `timetable_entries`
  ADD PRIMARY KEY (`timetable_id`),
  ADD UNIQUE KEY `UKko4kb0uvqq0cp4ob96na2lbyb` (`academic_year`,`semester`,`day`,`start_time`,`end_time`,`room`) USING HASH,
  ADD KEY `FKsw04ujp0h8xnksfgwcckusii` (`lecturer_id`),
  ADD KEY `FKi8jv4d492h85hygx2v35aytjh` (`subject_id`);

--
-- Indexes for table `time_slots`
--
ALTER TABLE `time_slots`
  ADD PRIMARY KEY (`time_slot_id`),
  ADD UNIQUE KEY `UK3ivlkxy75c9emu2o9xwe8ukvc` (`day`,`slot_number`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `classrooms`
--
ALTER TABLE `classrooms`
  MODIFY `classroom_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `departments`
--
ALTER TABLE `departments`
  MODIFY `department_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `lecturers`
--
ALTER TABLE `lecturers`
  MODIFY `lecturer_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `lecturer_allocations`
--
ALTER TABLE `lecturer_allocations`
  MODIFY `allocation_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT for table `lecturer_notifications`
--
ALTER TABLE `lecturer_notifications`
  MODIFY `notification_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `notification_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=38;

--
-- AUTO_INCREMENT for table `reschedule_requests`
--
ALTER TABLE `reschedule_requests`
  MODIFY `request_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `specializations`
--
ALTER TABLE `specializations`
  MODIFY `specialization_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `students`
--
ALTER TABLE `students`
  MODIFY `student_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30;

--
-- AUTO_INCREMENT for table `student_subject_registrations`
--
ALTER TABLE `student_subject_registrations`
  MODIFY `registration_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=118;

--
-- AUTO_INCREMENT for table `subjects`
--
ALTER TABLE `subjects`
  MODIFY `subject_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=77;

--
-- AUTO_INCREMENT for table `timetables`
--
ALTER TABLE `timetables`
  MODIFY `timetable_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `timetable_change_requests`
--
ALTER TABLE `timetable_change_requests`
  MODIFY `request_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- AUTO_INCREMENT for table `timetable_entries`
--
ALTER TABLE `timetable_entries`
  MODIFY `timetable_id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `time_slots`
--
ALTER TABLE `time_slots`
  MODIFY `time_slot_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=42;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=42;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `lecturers`
--
ALTER TABLE `lecturers`
  ADD CONSTRAINT `FKp8mocspquan2i6fjx36qghvct` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  ADD CONSTRAINT `FKpowy6g9rglsdytsdu6c9fymox` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`);

--
-- Constraints for table `lecturer_allocations`
--
ALTER TABLE `lecturer_allocations`
  ADD CONSTRAINT `FK2c1ljor6ys26ai16dp7iaiapo` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`subject_id`),
  ADD CONSTRAINT `FK406de5tof8honc5q0fgxl0rhs` FOREIGN KEY (`lecturer_id`) REFERENCES `lecturers` (`lecturer_id`);

--
-- Constraints for table `lecturer_notifications`
--
ALTER TABLE `lecturer_notifications`
  ADD CONSTRAINT `FKljxid9qdap3b2gf5bd6p5twdi` FOREIGN KEY (`lecturer_id`) REFERENCES `lecturers` (`lecturer_id`);

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `FKlchct0805t9w7mapbehsi6dcw` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`),
  ADD CONSTRAINT `fk_notification_lecturer` FOREIGN KEY (`lecturer_id`) REFERENCES `lecturers` (`lecturer_id`);

--
-- Constraints for table `reschedule_requests`
--
ALTER TABLE `reschedule_requests`
  ADD CONSTRAINT `FK19xu6h8a2g5j9pqxwncov1e2k` FOREIGN KEY (`requested_classroom_id`) REFERENCES `classrooms` (`classroom_id`),
  ADD CONSTRAINT `FK5s8f2ofp1ghat1qkmrdmw2t12` FOREIGN KEY (`timetable_id`) REFERENCES `timetables` (`timetable_id`),
  ADD CONSTRAINT `FKafxnc6p8kg4ghj2kllbwsco73` FOREIGN KEY (`lecturer_id`) REFERENCES `lecturers` (`lecturer_id`),
  ADD CONSTRAINT `FKom3lorlyvht1jgk0tpmp6xprc` FOREIGN KEY (`requested_time_slot_id`) REFERENCES `time_slots` (`time_slot_id`);

--
-- Constraints for table `specializations`
--
ALTER TABLE `specializations`
  ADD CONSTRAINT `FKmbosp83ckb8d8yjponpvhmbp8` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`);

--
-- Constraints for table `students`
--
ALTER TABLE `students`
  ADD CONSTRAINT `FKdt1cjx5ve5bdabmuuf3ibrwaq` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  ADD CONSTRAINT `FKkmmq7d44c9fng7fqq9tn3ma6a` FOREIGN KEY (`minor_specialization_id`) REFERENCES `specializations` (`specialization_id`),
  ADD CONSTRAINT `FKqq0lvcjmlm70ieaitswnbo5np` FOREIGN KEY (`major_specialization_id`) REFERENCES `specializations` (`specialization_id`);

--
-- Constraints for table `student_subject_registrations`
--
ALTER TABLE `student_subject_registrations`
  ADD CONSTRAINT `FK68nye55mxn4u7vfonfi6wn85p` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`),
  ADD CONSTRAINT `FKj494fbxc076mksd58nfy2vr47` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`subject_id`);

--
-- Constraints for table `subjects`
--
ALTER TABLE `subjects`
  ADD CONSTRAINT `FKgh0j5ejuox2kr2av0l8158c0a` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`),
  ADD CONSTRAINT `FKi2b54mc3ihs41kf9lfdb6lk5i` FOREIGN KEY (`specialization_id`) REFERENCES `specializations` (`specialization_id`);

--
-- Constraints for table `timetables`
--
ALTER TABLE `timetables`
  ADD CONSTRAINT `FK18lp6tw732sd0od8jh4aytdn8` FOREIGN KEY (`classroom_id`) REFERENCES `classrooms` (`classroom_id`),
  ADD CONSTRAINT `FKavj28kdlq3985xe8eko6loyuk` FOREIGN KEY (`lecturer_allocation_id`) REFERENCES `lecturer_allocations` (`allocation_id`),
  ADD CONSTRAINT `FKd38thoowj328e3dnm48gk3m4j` FOREIGN KEY (`time_slot_id`) REFERENCES `time_slots` (`time_slot_id`),
  ADD CONSTRAINT `FKelbkeaoovsi57wwkj3jcv5ti3` FOREIGN KEY (`specialization_id`) REFERENCES `specializations` (`specialization_id`);

--
-- Constraints for table `timetable_change_requests`
--
ALTER TABLE `timetable_change_requests`
  ADD CONSTRAINT `FK3locueyxn9om9c9mo6bj24rd8` FOREIGN KEY (`lecturer_id`) REFERENCES `lecturers` (`lecturer_id`),
  ADD CONSTRAINT `FKkc8iof83eu4i4mde831h6ea0g` FOREIGN KEY (`timetable_id`) REFERENCES `timetables` (`timetable_id`);

--
-- Constraints for table `timetable_entries`
--
ALTER TABLE `timetable_entries`
  ADD CONSTRAINT `FKi8jv4d492h85hygx2v35aytjh` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`subject_id`),
  ADD CONSTRAINT `FKsw04ujp0h8xnksfgwcckusii` FOREIGN KEY (`lecturer_id`) REFERENCES `lecturers` (`lecturer_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
