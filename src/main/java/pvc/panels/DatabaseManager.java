package pvc.panels;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.awt.GraphicsEnvironment;
import javax.swing.JOptionPane;

public class DatabaseManager {

    /** Shows a dialog, or prints to the console when no display is available (e.g. tests, servers). */
    private static void showInfo(String message, String title, int type) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("[" + title + "] " + message);
        } else {
            JOptionPane.showMessageDialog(null, message, title,
                    "Error".equals(title) ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void initializeDatabase() {
        String[] createTables = {
            """
            CREATE TABLE IF NOT EXISTS workers (
                worker_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                role TEXT NOT NULL,
                image BLOB
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS users (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                role TEXT NOT NULL,
                name TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS issues (
                issue_id INTEGER PRIMARY KEY AUTOINCREMENT,
                worker_id INTEGER NOT NULL,
                severity TEXT NOT NULL CHECK(severity IN ('Low', 'Medium', 'High', 'Critical')),
                description TEXT NOT NULL,
                resolved BOOLEAN DEFAULT 0,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (worker_id) REFERENCES workers(worker_id) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS production (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                worker_id INTEGER NOT NULL,
                shift TEXT NOT NULL,
                pvc_type TEXT NOT NULL,
                quantity_kg REAL,
                quality_rating INTEGER,
                production_date TEXT,
                FOREIGN KEY(worker_id) REFERENCES workers(worker_id) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS agreements (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pvc_type TEXT NOT NULL UNIQUE,
                pricing REAL NOT NULL,
                wastage_allowance REAL NOT NULL,
                delivery_schedule TEXT NOT NULL,
                payment_terms TEXT NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS deliveries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pellets_type TEXT NOT NULL,
                quantity_received REAL NOT NULL,
                status TEXT CHECK(status IN ('Delivered', 'Pending')) NOT NULL,
                scheduled_date TEXT,
                delivery_date TEXT
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS overtime_requests (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                worker_id INTEGER NOT NULL,
                hours_requested INTEGER NOT NULL,
                status TEXT DEFAULT 'Pending',
                request_date TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(worker_id) REFERENCES workers(worker_id) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS emergency_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                message TEXT NOT NULL,
                timestamp TEXT DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS employees (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                designation TEXT NOT NULL,
                department TEXT,
                join_date TEXT
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS inventory (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pellets_type TEXT NOT NULL,
                quantity REAL NOT NULL,
                last_updated TEXT DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS labor_costs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                worker_id INTEGER NOT NULL,
                role TEXT NOT NULL,
                hours_worked REAL NOT NULL,
                rate_per_hour REAL NOT NULL,
                total_cost REAL NOT NULL,
                generated_on TEXT DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS login_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                ip_address TEXT,
                device_info TEXT,
                success BOOLEAN,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS password_reset_tokens (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                token TEXT NOT NULL,
                expires_at TIMESTAMP NOT NULL,
                used BOOLEAN DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS pellets_received (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pellets_type TEXT,
                amount REAL,
                received_on TEXT DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS produce_collected (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                worker_id INTEGER,
                shift TEXT,
                quantity_collected REAL,
                collection_time TEXT DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS shift_reports (
                report_id INTEGER PRIMARY KEY AUTOINCREMENT,
                report_date TIMESTAMP NOT NULL,
                report_content TEXT NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS supplier_requests (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                supplier_name TEXT NOT NULL,
                request_details TEXT NOT NULL,
                status TEXT DEFAULT 'Pending'
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS suppliers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                contact TEXT NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS supervisor_issues (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                worker_id INTEGER NOT NULL,
                severity TEXT NOT NULL,
                description TEXT NOT NULL,
                logged_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(worker_id) REFERENCES workers(worker_id) ON DELETE CASCADE
            )""",
            "CREATE INDEX IF NOT EXISTS idx_issues_resolved ON issues(resolved)",
            "CREATE INDEX IF NOT EXISTS idx_issues_timestamp ON issues(timestamp)",
            "CREATE INDEX IF NOT EXISTS idx_issues_worker ON issues(worker_id)"
        };

        Connection conn = null;
        try {
            conn = DatabaseConnector.connect();
            conn.setAutoCommit(false); // Start transaction
            
            try (Statement stmt = conn.createStatement()) {
                // Enable foreign keys
                stmt.execute("PRAGMA foreign_keys = ON");
                
                // Execute all create statements
                for (String sql : createTables) {
                    stmt.execute(sql);
                }
                
                // Create default admin if needed
                createDefaultAdmin(conn);
                
                conn.commit(); // Commit transaction
                showInfo( 
                    "Database initialized successfully", 
                    "Success", 
                    0);
            } catch (SQLException e) {
                if (conn != null) {
                    conn.rollback(); // Rollback on error
                }
                showInfo( 
                    "Database initialization failed: " + e.getMessage(), 
                    "Error", 
                    0);
                throw new RuntimeException("Database initialization failed", e);
            } finally {
                if (conn != null) {
                    conn.setAutoCommit(true); // Reset to default
                }
            }
        } catch (SQLException e) {
            showInfo( 
                "Database connection failed: " + e.getMessage(), 
                "Error", 
                0);
            throw new RuntimeException("Database connection failed", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close(); // Ensure connection is closed
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private static void createDefaultAdmin(Connection conn) throws SQLException {
    String checkSql = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(checkSql)) {
        
        if (rs.getInt(1) == 0) {
            String hashedPassword = hashPassword("admin123");
            String insertSql = "INSERT INTO users (username, password, role, name) VALUES (?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, "admin");
                pstmt.setString(2, hashedPassword);
                pstmt.setString(3, "Admin");
                pstmt.setString(4, "System Administrator");
                pstmt.executeUpdate();
            }
        }
    }
}

private static String hashPassword(String password) {
    try {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("Hashing algorithm not available", e);
    }
}


    public static boolean updateOvertimeRequestStatus(int workerId, int hoursRequested, String status) {
        String sql = "UPDATE overtime_requests SET status = ? " +
                     "WHERE worker_id = ? AND hours_requested = ? AND status = 'Pending' LIMIT 1";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            pstmt.setString(1, status);
            pstmt.setInt(2, workerId);
            pstmt.setInt(3, hoursRequested);
            int updated = pstmt.executeUpdate();
            conn.commit();
            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getProductionReport() {
        StringBuilder report = new StringBuilder();
        String sql = "SELECT p.id, w.name, w.role, p.shift, p.pvc_type, p.quantity_kg AS quantity, p.quality_rating AS quality, p.production_date AS date " +
                     "FROM production p JOIN workers w ON p.worker_id = w.worker_id ORDER BY p.production_date DESC";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            report.append(String.format("%-5s %-15s %-10s %-10s %-10s %-10s %-8s %-20s\n",
                    "ID", "Worker", "Role", "Shift", "PVC Type", "Qty", "Quality", "Date"));
            report.append("---------------------------------------------------------------------------------------------\n");
            while (rs.next()) {
                report.append(String.format("%-5d %-15s %-10s %-10s %-10s %-10.2f %-8d %-20s\n",
                        rs.getInt("id"), rs.getString("name"), rs.getString("role"), rs.getString("shift"),
                        rs.getString("pvc_type"), rs.getDouble("quantity"), rs.getInt("quality"), rs.getString("date")));
            }
        } catch (SQLException e) {
            report.append("Error fetching production data: ").append(e.getMessage());
        }
        return report.toString();
    }
    // log issues method
    public static boolean logSupervisorIssue(int workerId, String severity, String description) {
    String createTable = "CREATE TABLE IF NOT EXISTS supervisor_issues (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "worker_id INTEGER NOT NULL, " +
                         "severity TEXT NOT NULL, " +
                         "description TEXT NOT NULL, " +
                         "logged_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                         "FOREIGN KEY(worker_id) REFERENCES workers(worker_id))";
    String insert = "INSERT INTO supervisor_issues (worker_id, severity, description) VALUES (?, ?, ?)";

    try (Connection conn = DatabaseConnector.connect();
         Statement stmt = conn.createStatement();
         PreparedStatement pstmt = conn.prepareStatement(insert)) {

        stmt.executeUpdate(createTable); // Ensure table exists

        pstmt.setInt(1, workerId);
        pstmt.setString(2, severity);
        pstmt.setString(3, description);
        pstmt.executeUpdate();
        return true;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
// Get all unresolved issues
public static ResultSet getUnresolvedIssues() throws SQLException {
    Connection conn = DatabaseConnector.connect();
    Statement stmt = conn.createStatement();
    return stmt.executeQuery(
        "SELECT i.*, w.name FROM issues i " +
        "JOIN workers w ON i.worker_id = w.worker_id " +
        "WHERE i.resolved = 0 ORDER BY i.timestamp DESC");
}

// Mark issue as resolved
public static boolean resolveIssue(int issueId) {
    String sql = "UPDATE issues SET resolved = 1 WHERE issue_id = ?";
    try (Connection conn = DatabaseConnector.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setInt(1, issueId);
        return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
        showError("Failed to resolve issue: " + e.getMessage());
        return false;
    }
}




    public static String getLaborCostReport() {
        final double RATE_OPERATOR = 15.0;
        final double RATE_SUPERVISOR = 25.0;
        final double HOURS_PER_SHIFT = 8.0;
        double totalCost = 0.0;
        StringBuilder report = new StringBuilder();
        String sql = "SELECT w.worker_id, w.name, w.role, COUNT(p.id) as shifts " +
                     "FROM production p JOIN workers w ON p.worker_id = w.worker_id " +
                     "GROUP BY w.worker_id, w.name, w.role";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            report.append(String.format("%-15s %-10s %-8s %-10s\n", "Worker", "Role", "Shifts", "Cost"));
            report.append("-----------------------------------------------------\n");
            while (rs.next()) {
                String name = rs.getString("name");
                String role = rs.getString("role");
                int shifts = rs.getInt("shifts");
                double rate = role.equalsIgnoreCase("Supervisor") ? RATE_SUPERVISOR : RATE_OPERATOR;
                double cost = shifts * HOURS_PER_SHIFT * rate;
                totalCost += cost;
                report.append(String.format("%-15s %-10s %-8d $%-10.2f\n", name, role, shifts, cost));
            }
            report.append("\nTotal Labor Cost: $").append(String.format("%.2f", totalCost));
        } catch (SQLException e) {
            report.append("Error fetching labor cost data: ").append(e.getMessage());
        }
        return report.toString();
    }

    //save delivery method
    public static boolean saveDelivery(String pelletsType, double quantity, String deliveryDate) {
    String sql = "INSERT INTO deliveries (pellets_type, quantity_received, status, delivery_date) VALUES (?, ?, 'Delivered', ?)";
    try (Connection conn = DatabaseConnector.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        conn.setAutoCommit(false);
        pstmt.setString(1, pelletsType);
        pstmt.setDouble(2, quantity);
        pstmt.setString(3, deliveryDate);
        pstmt.executeUpdate();
        conn.commit();
        return true;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

//add update agreement method
public static boolean addOrUpdateAgreement(String pvcType, double pricing, double wastage, String schedule, String payment) {
    String sql = """
        INSERT INTO agreements (pvc_type, pricing, wastage_allowance, delivery_schedule, payment_terms)
        VALUES (?, ?, ?, ?, ?)
        ON CONFLICT(pvc_type) DO UPDATE SET
            pricing = excluded.pricing,
            wastage_allowance = excluded.wastage_allowance,
            delivery_schedule = excluded.delivery_schedule,
            payment_terms = excluded.payment_terms
    """;
    try (Connection conn = DatabaseConnector.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        conn.setAutoCommit(false);
        pstmt.setString(1, pvcType);
        pstmt.setDouble(2, pricing);
        pstmt.setDouble(3, wastage);
        pstmt.setString(4, schedule);
        pstmt.setString(5, payment);
        pstmt.executeUpdate();
        conn.commit();
        return true;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}


    public static String getInventoryStatus() {
        StringBuilder report = new StringBuilder();
        String sql = "SELECT pellets_type, SUM(quantity_received) as total_received FROM deliveries GROUP BY pellets_type";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            report.append(String.format("%-20s %-10s\n", "Pellets Type", "Total Qty"));
            report.append("-------------------------------------\n");
            while (rs.next()) {
                report.append(String.format("%-20s %-10.2f\n",
                        rs.getString("pellets_type"), rs.getDouble("total_received")));
            }
        } catch (SQLException e) {
            report.append("Error fetching inventory data: ").append(e.getMessage());
        }
        return report.toString();
    }

    public static String getSupplierContacts() {
        return """
            1. PVC Pellets Inc – +1-555-123-4567
            2. PolyTech Supply – +1-555-987-6543
            3. ResinCo Distributors – +1-555-000-1122
            """;
    }

    public static boolean saveDelivery(String type, double quantity, String status, String scheduledDate, String deliveryDate) {
    String sql = "INSERT INTO deliveries(pellets_type, quantity_received, status, scheduled_date, delivery_date) VALUES (?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConnector.connect();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, type);
        stmt.setDouble(2, quantity);
        stmt.setString(3, status);
        stmt.setString(4, status.equals("Pending") ? scheduledDate : deliveryDate);
        stmt.setString(5, deliveryDate);

        stmt.executeUpdate();
        return true;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
// Worker Management
    public static boolean addWorker(String name, String role) {
        String sql = "INSERT INTO workers (name, role) VALUES (?, ?)";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, role);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            showError("Error adding worker: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateWorker(int workerId, String name, String role) {
        String sql = "UPDATE workers SET name = ?, role = ? WHERE worker_id = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, role);
            pstmt.setInt(3, workerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            showError("Error updating worker: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteWorker(int workerId) {
        String sql = "DELETE FROM workers WHERE worker_id = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, workerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            showError("Error deleting worker: " + e.getMessage());
            return false;
        }
    }

    public static boolean workerExists(int workerId) {
        String sql = "SELECT 1 FROM workers WHERE worker_id = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, workerId);
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            showError("Error checking worker: " + e.getMessage());
            return false;
        }
    }

    // Production Management
    public static boolean addProductionRecord(int workerId, String shift, String pvcType, 
                                           double quantity, int quality) {
        String sql = "INSERT INTO production(worker_id, shift, pvc_type, quantity_kg, quality_rating, production_date) " +
                     "VALUES (?, ?, ?, ?, ?, datetime('now'))";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, workerId);
            pstmt.setString(2, shift);
            pstmt.setString(3, pvcType);
            pstmt.setDouble(4, quantity);
            pstmt.setInt(5, quality);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            showError("Error saving production: " + e.getMessage());
            return false;
        }
    }

    // Issue Management (Single Implementation)
    public static boolean logIssue(int workerId, String severity, String description) {
    // First validate severity level
    if (!severity.matches("Low|Medium|High|Critical")) {
        showError("Invalid severity level. Must be Low, Medium, High, or Critical");
        return false;
    }

    String sql = "INSERT INTO issues (worker_id, severity, description) VALUES (?, ?, ?)";
    
    try (Connection conn = DatabaseConnector.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        conn.setAutoCommit(false);
        pstmt.setInt(1, workerId);
        pstmt.setString(2, severity);
        pstmt.setString(3, description);
        
        int affectedRows = pstmt.executeUpdate();
        conn.commit();
        
        return affectedRows > 0;
        
    } catch (SQLException e) {
        showError("Failed to log issue: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

    // Report Generation
    public static boolean saveShiftReport(String reportContent) {
        String sql = "INSERT INTO shift_reports (report_date, report_content) VALUES (datetime('now'), ?)";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, reportContent);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            showError("Error saving report: " + e.getMessage());
            return false;
        }
    }

    // Data Retrieval Methods
    public static ResultSet getCurrentShiftWorkers() throws SQLException {
        Connection conn = DatabaseConnector.connect();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(
            "SELECT w.worker_id, w.name, p.pvc_type as task, " +
            "SUM(p.quantity_kg) as output, AVG(p.quality_rating) as quality_score " +
            "FROM workers w LEFT JOIN production p ON w.worker_id = p.worker_id " +
            "WHERE p.production_date >= datetime('now', '-8 hours') " +
            "GROUP BY w.worker_id");
    }

    public static ResultSet getCurrentIssues() throws SQLException {
        Connection conn = DatabaseConnector.connect();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(
            "SELECT i.*, w.name FROM issues i JOIN workers w ON i.worker_id = w.worker_id " +
            "WHERE i.resolved = 0 ORDER BY i.timestamp DESC LIMIT 20");
    }

    //login
    public static boolean createUser(String username, String password, String role) {
    String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
    
    try (Connection conn = DatabaseConnector.connect();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, username);
        stmt.setString(2, hashPassword(password));
        stmt.setString(3, role);
        
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
public static boolean verifyLogin(String username, String password) {
    String sql = "SELECT password FROM users WHERE username = ?";
    try (Connection conn = DatabaseConnector.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            String storedHash = rs.getString("password");
            String inputHash = hashPassword(password);
            return storedHash.equals(inputHash);
        }
        return false;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

public static String getUserRole(String username) {
    String sql = "SELECT role FROM users WHERE username = ?";
    try (Connection conn = DatabaseConnector.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            return rs.getString("role");
        }
        return null;
    } catch (SQLException e) {
        e.printStackTrace();
        return null;
    }
}

public static boolean userExists(String username) {
    String sql = "SELECT 1 FROM users WHERE username = ?";
    try (Connection conn = DatabaseConnector.connect();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, username);
        return stmt.executeQuery().next();
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

    // Utility Methods
    private static void showError(String msg) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("[Database Error] " + msg);
        } else {
            JOptionPane.showMessageDialog(null, msg, "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }}