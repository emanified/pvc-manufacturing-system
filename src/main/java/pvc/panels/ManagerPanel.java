package pvc.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManagerPanel extends JPanel {
    private final JComboBox<String> requestCombo;
    private final JTextArea approvalLog;

    public ManagerPanel() {
        setLayout(new BorderLayout(10, 10));
        setOpaque(false); // Use FlatLaf theme's background

        // Key Metrics Panel
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        metricsPanel.setBorder(BorderFactory.createTitledBorder("Key Metrics"));
        metricsPanel.setOpaque(false);
        metricsPanel.add(createMetricCard("Daily Production", "1,250 kg", new Color(85, 170, 85)));
        metricsPanel.add(createMetricCard("Defect Rate", "2.3%", new Color(255, 204, 0)));
        metricsPanel.add(createMetricCard("Labor Cost", "$3,420", new Color(255, 85, 85)));

        // Approval Section
        JPanel approvalPanel = new JPanel(new BorderLayout(10, 10));
        approvalPanel.setBorder(BorderFactory.createTitledBorder("Pending Approvals"));
        approvalPanel.setOpaque(false);

        requestCombo = new JComboBox<>(new String[]{
            "Worker #101: Overtime Request (4hrs)",
            "Supplier: Raw Material Price Increase",
            "New PVC Type Proposal"
        });

        approvalPanel.add(requestCombo, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttons.setOpaque(false);
        JButton approve = new JButton("Approve");
        approve.setBackground(new Color(84, 160, 255));  // soft blue
        approve.setForeground(Color.WHITE);
        approve.setFocusPainted(false);

        JButton reject = new JButton("Reject");
        reject.setBackground(new Color(180, 180, 180));  // soft grey
        reject.setForeground(Color.BLACK);
        reject.setFocusPainted(false);

        approve.addActionListener(this::processApproval);
        reject.addActionListener(this::processApproval);
        buttons.add(approve);
        buttons.add(reject);

        approvalPanel.add(buttons, BorderLayout.CENTER);

        approvalLog = new JTextArea(8, 30);
        approvalLog.setEditable(false);
        JScrollPane logScroll = new JScrollPane(approvalLog);
        approvalPanel.add(logScroll, BorderLayout.SOUTH);

        // Quick Actions
        JPanel quickActions = new JPanel(new GridLayout(5, 1, 10, 10));
        quickActions.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
        quickActions.setOpaque(false);

        String[] actions = {
            "Generate Production Report", "View Labor Costs",
            "Check Inventory", "Supplier Contacts", "Emergency Stop"
        };

        for (String label : actions) {
    JButton actionBtn = new JButton(label);
    actionBtn.setBackground(new Color(150, 150, 150)); // light grey
    actionBtn.setForeground(Color.BLACK);
    actionBtn.setFocusPainted(false);
    actionBtn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200))); // subtle border
    actionBtn.addActionListener(this::handleQuickAction);
    quickActions.add(actionBtn);
}

        // Center Layout
        JPanel center = new JPanel(new GridLayout(1, 2, 10, 10));
        center.setOpaque(false);
        center.add(approvalPanel);
        center.add(quickActions);

        // Combine All
        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setOpaque(false);
        mainContent.add(metricsPanel, BorderLayout.NORTH);
        mainContent.add(center, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createMetricCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(color, 2));
        card.setOpaque(false);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 14f));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 18f));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void processApproval(ActionEvent e) {
        String request = (String) requestCombo.getSelectedItem();
        String action = ((JButton) e.getSource()).getText();
        String status = action.equalsIgnoreCase("Approve") ? "Approved" : "Rejected";

        if (request != null && request.startsWith("Worker #")) {
            try {
                Pattern pattern = Pattern.compile("#(\\d+).*\\((\\d+)hrs\\)");
                Matcher matcher = pattern.matcher(request);
                if (matcher.find()) {
                    int workerId = Integer.parseInt(matcher.group(1));
                    int hours = Integer.parseInt(matcher.group(2));
                    boolean updated = DatabaseManager.updateOvertimeRequestStatus(workerId, hours, status);
                    if (updated) {
                        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                        approvalLog.append(String.format("[%s] %s: %s\n", timestamp, status.toUpperCase(), request));
                        requestCombo.removeItem(request);
                        JOptionPane.showMessageDialog(this, "Request " + status.toLowerCase() + " successfully!");
                    } else {
                        JOptionPane.showMessageDialog(this, "No matching pending request found.");
                    }
                } else {
                    throw new IllegalArgumentException("Invalid request format.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Only worker overtime requests are supported.");
        }
    }

    private void handleQuickAction(ActionEvent e) {
        String action = ((JButton) e.getSource()).getText();
        switch (action) {
            case "Generate Production Report":
                showLargeDialog("Production Report", DatabaseManager.getProductionReport());
                break;
            case "View Labor Costs":
                showLargeDialog("Labor Cost Report", DatabaseManager.getLaborCostReport());
                break;
            case "Check Inventory":
                showLargeDialog("Inventory Report", DatabaseManager.getInventoryStatus());
                break;
            case "Supplier Contacts":
                showLargeDialog("Supplier Contacts", DatabaseManager.getSupplierContacts());
                break;
            case "Emergency Stop":
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to activate EMERGENCY STOP?",
                        "Confirm Emergency Stop", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(this, "⚠ Emergency Stop Activated", "Alert", JOptionPane.WARNING_MESSAGE);
                }
                break;
        }
    }

    private void showLargeDialog(String title, String content) {
        JTextArea area = new JTextArea(content, 20, 80);
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        JOptionPane.showMessageDialog(this, scroll, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
