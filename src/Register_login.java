import java.sql.*;

public class Register_login {

    public String register(String username, String password) {
        if (username == null || username.isBlank()) return "Username required.";
        if (password == null || password.length() < 8) return "Password must be at least 8 characters.";

        String salt = PasswordHash.generateSalt();
        String hash = PasswordHash.hash(password, salt);

        String sql = "INSERT INTO users (username, password_hash, salt) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, hash);
            ps.setString(3, salt);
            ps.executeUpdate();
            return "Registered successfully.";
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) return "Username already taken.";
            return "Database error: " + e.getMessage();
        }
    }

    public boolean login(String username, String password) {
        if (username == null || password == null) return false;

        String sql = "SELECT password_hash, salt FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                return PasswordHash.verify(password, rs.getString("salt"), rs.getString("password_hash"));
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }
}