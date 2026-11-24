package pvc.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import pvc.Main;

public class LoginPage extends JFrame {
    private JTextField userField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;

    public LoginPage() {
        setTitle("PVC Factory Management System - Login");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(70, 70, 70));
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo/Heading
        JLabel heading = new JLabel("PVC Factory Management", SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 32));
        heading.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(heading, gbc);

        // Login Panel
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        loginPanel.setBackground(new Color(90, 90, 90));
        gbc.gridy = 1;
        add(loginPanel, gbc);

        GridBagConstraints gbcPanel = new GridBagConstraints();
        gbcPanel.insets = new Insets(10, 10, 10, 10);
        gbcPanel.fill = GridBagConstraints.HORIZONTAL;

        // Username
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        userLabel.setForeground(Color.WHITE);
        gbcPanel.gridx = 0;
        gbcPanel.gridy = 0;
        loginPanel.add(userLabel, gbcPanel);

        userField = new JTextField(20);
        userField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbcPanel.gridx = 1;
        loginPanel.add(userField, gbcPanel);

        // Password
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        passLabel.setForeground(Color.WHITE);
        gbcPanel.gridx = 0;
        gbcPanel.gridy = 1;
        loginPanel.add(passLabel, gbcPanel);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbcPanel.gridx = 1;
        loginPanel.add(passwordField, gbcPanel);

        // Role
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        roleLabel.setForeground(Color.WHITE);
        gbcPanel.gridx = 0;
        gbcPanel.gridy = 2;
        loginPanel.add(roleLabel, gbcPanel);

        String[] roles = {"Worker", "Supervisor", "Manager","Supplier"};
        roleCombo = new JComboBox<>(roles);
        roleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbcPanel.gridx = 1;
        loginPanel.add(roleCombo, gbcPanel);


        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        loginButton.setBackground(new Color(0, 120, 215));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(150, 40));
        gbcPanel.gridx = 0;
        gbcPanel.gridy = 3;
        gbcPanel.gridwidth = 2;
        gbcPanel.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginButton, gbcPanel);


        // Below the loginButton
            JButton signupButton = new JButton("Sign Up");
            signupButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            signupButton.setBackground(new Color(60, 60, 60));
            signupButton.setForeground(Color.WHITE);
            signupButton.setFocusPainted(false);
            signupButton.setPreferredSize(new Dimension(150, 30));

            gbcPanel.gridy = 4; // Below login button
            loginPanel.add(signupButton, gbcPanel);

            // Action listener
        signupButton.addActionListener(evt -> new SignupDialog(this));
        loginButton.addActionListener(this::performLogin);

        setVisible(true);
    }
       public void resetFields() {
        userField.setText("");
     passwordField.setText("");
     roleCombo.setSelectedIndex(0); // optional: reset role too

       }

    private void performLogin(ActionEvent e) {
        String username = userField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleCombo.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password", 
                "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            User authenticatedUser = authenticateUser(username, password, role);
            if (authenticatedUser != null) {
                JOptionPane.showMessageDialog(this, 
                    "Welcome " + authenticatedUser.getName() + " (" + authenticatedUser.getRole() + ")",
                    "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                
                dispose();
            Main.showMainApplicationWithTab(authenticatedUser, "Dashboard"); // or any tab name
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Invalid username, password or role", 
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Database error during login: " + ex.getMessage(),
                "Login Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private User authenticateUser(String username, String password, String role) throws SQLException {
        String sql = "SELECT user_id, username, role FROM users WHERE username = ? AND password = ? AND role = ?";
        
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password)); // Always hash passwords!
            stmt.setString(3, role);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role")
                    );
                }
            }
        }
        return null;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    public static class User {
        private final int userId;
        private final String name;
        private final String role;

        public User(int userId, String name, String role) {
            this.userId = userId;
            this.name = name;
            this.role = role;
        }

        public int getUserId() { return userId; }
        public String getName() { return name; }
        public String getRole() { return role; }
    }
}