# Smart Faculty Timetable Management System

A web-based Faculty Timetable Management System developed to organize and manage faculty timetables efficiently. The system consists of a Java-based backend, a React.js frontend, and a MySQL database.

## Technologies Used

### Backend
- Java
- Spring Boot
- Maven

### Frontend
- React.js
- Vite
- JavaScript
- HTML
- CSS

### Database
- MySQL

### Development Tools
- IntelliJ IDEA
- Visual Studio Code
- MySQL Workbench

## Project Structure

```text
Smart-Faculty-Timetable-Management-System/
│
├── smartfaculty-backend/
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── smartfaculty-frontend/
│   ├── public/
│   ├── src/
│   ├── package.json
│   ├── package-lock.json
│   ├── index.html
│   └── vite.config.js
│
├── database/
│   └── smart_faculty_timetable.sql
│
└── README.md
```

## Installation and Setup

### 1. Clone the Repository

```bash
git clone YOUR_GITHUB_REPOSITORY_URL
```

### 2. Database Setup

1. Open MySQL Workbench.
2. Create the required MySQL database.
3. Import the SQL file from the `smart_faculty_timetable` folder.
4. Configure the database connection settings in the backend application.

### 3. Backend Setup

1. Open the `smartfaculty-backend` folder using IntelliJ IDEA.
2. Configure the MySQL database connection.
3. Load the Maven dependencies.
4. Run the Java backend application.

### 4. Frontend Setup

1. Open the `smartfaculty-frontend` folder using Visual Studio Code.
2. Install the required dependencies:

```bash
npm install
```

3. Start the development server:

```bash
npm run dev
```

4. Open the local URL displayed in the terminal to access the frontend.

## Project Purpose

The purpose of this project is to develop a centralized web-based solution for faculty timetable management.

## Developer

Developed as an academic project.
