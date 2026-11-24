package pvc.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class AgreementPanel extends JPanel {
    private JTextField pvcTypeField;
    private JTextField pricingField;
    private JTextField wastageField;
    private JTextField scheduleField;
    private JTextField paymentField;

    public AgreementPanel() {
        setLayout(new BorderLayout());
        setOpaque(false); // Keep transparency for FlatLaf dark themes

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(25, 30, 25, 30),
                BorderFactory.createTitledBorder(null, "PVC Agreement Details", 0, 0,
                        new Font("SansSerif", Font.BOLD, 16), UIManager.getColor("Label.foreground"))
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        pvcTypeField = createTextField();
        pricingField = createTextField();
        wastageField = createTextField();
        scheduleField = createTextField();
        paymentField = createTextField();

        addField(form, gbc, "PVC Type:", pvcTypeField);
        addField(form, gbc, "Pricing (USD):", pricingField);
        addField(form, gbc, "Wastage Allowance (%):", wastageField);
        addField(form, gbc, "Delivery Schedule:", scheduleField);
        addField(form, gbc, "Payment Terms:", paymentField);

        JButton saveBtn = new JButton("💾 Save Agreement");
        styleButton(saveBtn, new Color(33, 150, 243));
        saveBtn.setBackground(new Color(52, 152, 219));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(this::saveAgreement);

        JButton viewBtn = new JButton("📄 View All Agreements");
        styleButton(viewBtn, new Color(120, 120, 120));
        viewBtn.addActionListener(e -> DatabaseViewer.viewTable("agreements"));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(saveBtn);
        buttonPanel.add(viewBtn);

        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(form, BorderLayout.CENTER);
        container.add(buttonPanel, BorderLayout.SOUTH);

        add(container, BorderLayout.CENTER);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String labelText, JTextField field) {
        JLabel label = createLabel(labelText);
        gbc.gridx = 0;
        gbc.weightx = 0.2;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        panel.add(field, gbc);

        gbc.gridy++;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        label.setForeground(UIManager.getColor("Label.foreground"));
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return field;
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void saveAgreement(ActionEvent e) {
        try {
            String pvcType = pvcTypeField.getText().trim();
            double pricing = Double.parseDouble(pricingField.getText().trim());
            double wastage = Double.parseDouble(wastageField.getText().trim());
            String schedule = scheduleField.getText().trim();
            String payment = paymentField.getText().trim();

            if (pvcType.isEmpty() || schedule.isEmpty() || payment.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean saved = DatabaseManager.addOrUpdateAgreement(pvcType, pricing, wastage, schedule, payment);
            if (saved) {
                JOptionPane.showMessageDialog(this, "Agreement saved successfully!");
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save agreement", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for pricing and wastage", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        pvcTypeField.setText("");
        pricingField.setText("");
        wastageField.setText("");
        scheduleField.setText("");
        paymentField.setText("");
    }
}
