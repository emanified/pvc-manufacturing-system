package pvc.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignupDialog extends JDialog {
    private JTextField userField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;

    public SignupDialog(JFrame parent) {
        super(parent, "Sign Up", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        gbc.gridx = 0; gbc.gridy = 0;
        add(userLabel, gbc);

        userField = new JTextField(20);
        gbc.gridx = 1;
        add(userField, gbc);

        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0; gbc.gridy = 1;
        add(passLabel, gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        add(passwordField, gbc);

        JLabel roleLabel = new JLabel("Role:");
        gbc.gridx = 0; gbc.gridy = 2;
        add(roleLabel, gbc);

        String[] roles = {"Worker", "Supervisor", "Manager", "Supplier"};
        roleCombo = new JComboBox<>(roles);
        gbc.gridx = 1;
        add(roleCombo, gbc);

        JButton signUpBtn = new JButton("Sign Up");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(signUpBtn, gbc);

        signUpBtn.addActionListener(this::handleSignup);

        setVisible(true);
    }

    private void handleSignup(ActionEvent e) {
        String username = userField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleCombo.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        String hashedPassword = hashPassword(password);

        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "User registered successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to register user.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
}
