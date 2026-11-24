package pvc.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import pvc.Main;
import pvc.panels.LoginPage.User;

public class MenuPanel extends JPanel {
    private final User user;

    public MenuPanel(User user) {
        this.user = user;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(180, getHeight()));
        setBackground(new Color(30, 30, 30));
        setBorder(new EmptyBorder(30, 15, 30, 15));

        String role = user.getRole();

        add(Box.createVerticalStrut(10));
        addStyledButton("Agreement", "Agreement");
        addStyledButton("Produce", "Produce Records");
        addStyledButton("Worker", "Worker");

        switch (role) {
            case "Manager":
                addStyledButton("Manager", "Manager");
                break;
            case "Supervisor":
                addStyledButton("Supervisor", "Supervisor");
                break;
            case "Supplier":
                addStyledButton("Supplier", "Supplier");
                break;
        }

        addStyledButton("Salary", "Salary Calculation");

        add(Box.createVerticalGlue()); // Pushes logout to bottom
        add(Box.createVerticalStrut(10));

        addLogoutButton(); // Adds logout button
    }

    private void addStyledButton(String label, String tabTitle) {
        JButton btn = new JButton(label);
        styleButton(btn, new Color(50, 50, 50), new Color(80, 80, 80));
        btn.addActionListener(e -> Main.showMainApplicationWithTab(user, tabTitle));
        add(Box.createVerticalStrut(10));
        add(btn);
    }

   private void addLogoutButton() {
    JButton logoutBtn = new JButton("Logout");
    styleButton(logoutBtn, new Color(180, 50, 50), new Color(220, 70, 70));
    logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

    logoutBtn.addActionListener(e -> {
        int confirm = JOptionPane.showConfirmDialog(
            SwingUtilities.getWindowAncestor(this),
            "Are you sure you want to log out?",
            "Logout Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            Main.showLoginScreen();
        }
    });

    add(Box.createVerticalStrut(10));
    add(logoutBtn);
}
    private void styleButton(JButton btn, Color bg, Color hoverBg) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(150, 35));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(hoverBg);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
    }
}
