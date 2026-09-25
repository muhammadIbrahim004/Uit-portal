import java.sql.*;
import java.util.*;

/** All portal data logic: profile, courses, results, fees, registration. */
public class StudentService {
    public static final String CURRENT = "Fall 2026";
    public static final int MAX_CREDITS = 21;
    private static final String JOIN =
            " FROM enrollments e JOIN courses c ON c.code = e.code WHERE e.username = ? ";

    // ---------- tiny JDBC helpers ----------
    private static List<Object[]> query(String sql, Object... args) {
        List<Object[]> rows = new ArrayList<>();
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            try (ResultSet rs = ps.executeQuery()) {
                int n = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Object[] r = new Object[n];
                    for (int i = 0; i < n; i++) r[i] = rs.getObject(i + 1);
                    rows.add(r);
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return rows;
    }

    private static void update(String sql, Object... args) {
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private static double scalar(String sql, Object... args) {
        List<Object[]> r = query(sql, args);
        return r.isEmpty() || r.get(0)[0] == null ? 0 : ((Number) r.get(0)[0]).doubleValue();
    }

    // ---------- profile (seeded per student, so every account has its own results) ----------
    public static void createProfile(String user, String name) {
        update("INSERT OR IGNORE INTO profiles VALUES (?,?,?,?,?)",
                user, name, "BS Computer Science", "A", "ENR-" + user);
        Random rnd = new Random(user.hashCode());
        double[] gp = {4.0, 3.67, 3.33, 3.0, 2.67, 2.33, 2.0, 1.67};
        String[] sems = {"Fall 2025", "Spring 2026", CURRENT};
        for (int i = 0; i < 15; i++) {
            int s = i / 5;
            Double grade = s < 2 ? gp[rnd.nextInt(gp.length)] : null;
            double att = s < 2 ? 100 : 85 + rnd.nextInt(16);
            update("INSERT OR IGNORE INTO enrollments (username, code, semester, grade_points, attendance) VALUES (?,?,?,?,?)",
                    user, Database.CATALOG[i][0], sems[s], grade, att);
        }
        for (int s = 0; s < 3; s++)
            update("INSERT INTO fees (username, semester, amount, paid) VALUES (?,?,?,?)",
                    user, sems[s], 60000 + rnd.nextInt(30000), s < 2 ? 1 : 0);
    }

    /** {full_name, program, section, enrollment_no} or null. */
    public static String[] profile(String user) {
        List<Object[]> r = query("SELECT full_name, program, section, enrollment_no FROM profiles WHERE username = ?", user);
        return r.isEmpty() ? null : Arrays.copyOf(r.get(0), 4, String[].class);
    }

    // ---------- dashboard numbers (all computed from the database) ----------
    public static double cgpa(String user) {
        return scalar("SELECT SUM(c.credits * e.grade_points) / SUM(c.credits)" + JOIN + "AND e.grade_points IS NOT NULL", user);
    }
    public static double avgAttendance(String user) {
        return scalar("SELECT AVG(e.attendance)" + JOIN + "AND e.semester = ?", user, CURRENT);
    }
    public static int registeredCount(String user) {
        return (int) scalar("SELECT COUNT(*) FROM enrollments WHERE username = ? AND semester = ?", user, CURRENT);
    }
    public static int balance(String user) {
        return (int) scalar("SELECT SUM(amount) FROM fees WHERE username = ? AND paid = 0", user);
    }

    // ---------- lists ----------
    public static List<Object[]> currentCourses(String user) {   // code,title,credits,teacher,room,timings,attendance
        return query("SELECT c.code, c.title, c.credits, c.teacher, c.room, c.timings, e.attendance" + JOIN + "AND e.semester = ?", user, CURRENT);
    }
    public static List<Object[]> results(String user) {          // semester,code,title,credits,grade_points
        return query("SELECT e.semester, c.code, c.title, c.credits, e.grade_points" + JOIN + "AND e.grade_points IS NOT NULL ORDER BY e.id", user);
    }
    public static List<Object[]> fees(String user) {             // id,semester,amount,paid
        return query("SELECT id, semester, amount, paid FROM fees WHERE username = ? ORDER BY id", user);
    }
    public static List<Object[]> registered(String user) {       // code,title,credits,teacher,timings
        return query("SELECT c.code, c.title, c.credits, c.teacher, c.timings" + JOIN + "AND e.semester = ? AND e.grade_points IS NULL", user, CURRENT);
    }
    public static List<Object[]> available(String user) {
        return query("SELECT code, title, credits, teacher, timings FROM courses WHERE code NOT IN "
                + "(SELECT code FROM enrollments WHERE username = ?) ORDER BY code", user);
    }

    // ---------- actions ----------
    public static void payFees(String user) {
        update("UPDATE fees SET paid = 1 WHERE username = ?", user);
    }

    public static String register(String user, String code) {
        List<Object[]> c = query("SELECT credits FROM courses WHERE code = ?", code);
        if (c.isEmpty()) return "Course not found.";
        double used = scalar("SELECT SUM(c.credits)" + JOIN + "AND e.semester = ?", user, CURRENT);
        if (used + ((Number) c.get(0)[0]).doubleValue() > MAX_CREDITS)
            return "Credit limit (" + MAX_CREDITS + ") exceeded. Drop a course first.";
        update("INSERT OR IGNORE INTO enrollments (username, code, semester, attendance) VALUES (?,?,?,100)", user, code, CURRENT);
        return "Registered " + code + ".";
    }

    public static String drop(String user, String code) {
        update("DELETE FROM enrollments WHERE username = ? AND code = ? AND semester = ? AND grade_points IS NULL", user, code, CURRENT);
        return "Dropped " + code + ".";
    }

    public static String changePassword(String user, String oldPw, String newPw) {
        if (newPw.length() < 8) return "New password must be at least 8 characters.";
        if (!new Register_login().login(user, oldPw)) return "Current password is incorrect.";
        String salt = PasswordHash.generateSalt();
        update("UPDATE users SET password_hash = ?, salt = ? WHERE username = ?", PasswordHash.hash(newPw, salt), salt, user);
        return "Password updated.";
    }
}