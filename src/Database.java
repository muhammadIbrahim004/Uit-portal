import java.sql.*;

public class Database {
    private static final String URL = "jdbc:sqlite:users.db";

    static final String[][] CATALOG = {
            {"CS101", "Programming Fundamentals", "4", "Dr. Sana Malik", "LAB-1", "Mon 09:30 - 12:25"},
            {"MT101", "Calculus I", "3", "Prof. Adeel Raza", "R-101", "Tue 08:30 - 10:25"},
            {"EN101", "English Composition", "3", "Ms. Hina Qureshi", "R-102", "Wed 10:30 - 12:25"},
            {"PH101", "Applied Physics", "3", "Dr. Omar Farooq", "R-201", "Thu 09:30 - 11:25"},
            {"IS101", "Islamic Studies", "2", "Mr. Bilal Ahmed", "R-103", "Fri 08:30 - 09:25"},
            {"CS102", "Object Oriented Programming", "4", "Dr. Sana Malik", "LAB-2", "Mon 13:30 - 16:25"},
            {"MT102", "Linear Algebra", "3", "Prof. Adeel Raza", "R-104", "Tue 10:30 - 12:25"},
            {"CS103", "Digital Logic Design", "3", "Engr. Kamran Ali", "LAB-3", "Wed 13:30 - 15:25"},
            {"EN102", "Communication Skills", "3", "Ms. Hina Qureshi", "R-105", "Thu 11:30 - 13:25"},
            {"PK101", "Pakistan Studies", "2", "Mr. Bilal Ahmed", "R-106", "Fri 10:30 - 11:25"},
            {"CS201", "Data Structures", "4", "Dr. Ayesha Noor", "LAB-1", "Mon 10:30 - 12:25 Wed 10:30 - 11:25"},
            {"CS202", "Database Systems", "4", "Dr. Zain Abbas", "R-301", "Tue 13:30 - 15:25 Thu 13:30 - 14:25"},
            {"CS203", "Computer Architecture", "3", "Engr. Kamran Ali", "R-302", "Wed 14:30 - 16:25"},
            {"MT201", "Multivariate Calculus", "3", "Prof. Rabia Khan", "R-303", "Fri 11:30 - 13:25"},
            {"CS204", "Artificial Intelligence", "3", "Dr. Faisal Mehmood", "LAB-4", "Sat 10:30 - 13:25"},
            {"CS205", "Operating Systems", "4", "Dr. Ayesha Noor", "R-304", "Sat 14:30 - 17:25"},
            {"CS206", "Software Engineering", "3", "Ms. Mahnoor Iqbal", "R-305", "Mon 15:30 - 17:25"},
            {"MT202", "Probability and Statistics", "3", "Prof. Rabia Khan", "R-306", "Tue 15:30 - 17:25"}
    };

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void init() {
        String[] tables = {
                """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                salt TEXT NOT NULL,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP)""",
                """
            CREATE TABLE IF NOT EXISTS profiles (
                username TEXT PRIMARY KEY, full_name TEXT NOT NULL,
                program TEXT, section TEXT, enrollment_no TEXT)""",
                """
            CREATE TABLE IF NOT EXISTS courses (
                code TEXT PRIMARY KEY, title TEXT, credits INTEGER,
                teacher TEXT, room TEXT, timings TEXT)""",
                """
            CREATE TABLE IF NOT EXISTS enrollments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT, code TEXT, semester TEXT,
                grade_points REAL, attendance REAL,
                UNIQUE(username, code, semester))""",
                """
            CREATE TABLE IF NOT EXISTS fees (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT, semester TEXT, amount INTEGER, paid INTEGER DEFAULT 0)"""
        };
        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            for (String t : tables) st.execute(t);
            try (PreparedStatement ps = c.prepareStatement("INSERT OR IGNORE INTO courses VALUES (?,?,?,?,?,?)")) {
                for (String[] r : CATALOG) {
                    ps.setString(1, r[0]); ps.setString(2, r[1]); ps.setInt(3, Integer.parseInt(r[2]));
                    ps.setString(4, r[3]); ps.setString(5, r[4]); ps.setString(6, r[5]);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB init failed", e);
        }
    }
}