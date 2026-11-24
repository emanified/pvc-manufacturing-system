package pvc.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SalaryPanel extends JPanel {

    private JTextField hoursField, rateField, totalSalaryField;
    private JTextArea reportArea;

    public SalaryPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(UIManager.getColor("Panel.background")); // Adapts to FlatLaf theme

        // ===== MANUAL SALARY CALCULATOR =====
        JPanel calcPanel = new JPanel(new GridBagLayout());
        calcPanel.setOpaque(false);
        calcPanel.setBorder(BorderFactory.createTitledBorder("Manual Salary Calculator"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        calcPanel.add(new JLabel("Hours Worked:"), gbc);
        gbc.gridx = 1;
        hoursField = new JTextField(12);
        calcPanel.add(hoursField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        calcPanel.add(new JLabel("Rate per Hour:"), gbc);
        gbc.gridx = 1;
        rateField = new JTextField(12);
        calcPanel.add(rateField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        calcPanel.add(new JLabel("Total Salary:"), gbc);
        gbc.gridx = 1;
        totalSalaryField = new JTextField(12);
        totalSalaryField.setEditable(false);
        totalSalaryField.setBackground(UIManager.getColor("TextField.inactiveBackground"));
        calcPanel.add(totalSalaryField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton calcBtn = new JButton("Calculate");
        calcBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        calcBtn.addActionListener(this::calculateSalary);
        calcPanel.add(calcBtn, gbc);

        add(calcPanel, BorderLayout.NORTH);

        // ===== REPORT AREA =====
        reportArea = new JTextArea(20, 70);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setEditable(false);
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Labor Cost Report"));
        scrollPane.setBackground(UIManager.getColor("ScrollPane.background"));

        add(scrollPane, BorderLayout.CENTER);

        // ===== GENERATE REPORT BUTTON =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomPanel.setOpaque(false);
        JButton generateBtn = new JButton("Generate Labor Cost Report");
        generateBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        generateBtn.addActionListener(this::generateReport);
        bottomPanel.add(generateBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void calculateSalary(ActionEvent e) {
        try {
            double hours = Double.parseDouble(hoursField.getText().trim());
            double rate = Double.parseDouble(rateField.getText().trim());
            double total = hours * rate;
            totalSalaryField.setText(String.format("%.2f", total));
        } catch (NumberFormatException ex) {
            UIManager.getLookAndFeel().provideErrorFeedback(this);
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid input. Please enter valid numeric values.",
                    "Calculation Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void generateReport(ActionEvent e) {
        String report = DatabaseManager.getLaborCostReport();
        if (report == null || report.trim().isEmpty()) {
            reportArea.setText("No data found or error retrieving report.");
        } else if (report.length() > 5000) {
            JTextArea area = new JTextArea(report, 20, 80);
            area.setFont(new Font("Monospaced", Font.PLAIN, 12));
            area.setEditable(false);
            JScrollPane scroll = new JScrollPane(area);
            JOptionPane.showMessageDialog(this, scroll, "Labor Cost Report", JOptionPane.INFORMATION_MESSAGE);
        } else {
            reportArea.setText(report);
        }
    }
}
