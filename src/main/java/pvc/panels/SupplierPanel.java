package pvc.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import com.formdev.flatlaf.FlatClientProperties;

public class SupplierPanel extends JPanel {
    private JComboBox<String> pelletsTypeCombo;
    private JTextField quantityField;
    private JComboBox<String> statusCombo;
    private JTextField deliveryDateField;
    private JTextField scheduledDateField;

    public SupplierPanel() {
        setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Supplier - Pellets Delivery"));

        pelletsTypeCombo = new JComboBox<>(new String[]{"PVC-A", "PVC-B", "PVC-C", "Recycled Mix", "Custom Blend"});
        quantityField = new JTextField();
        statusCombo = new JComboBox<>(new String[]{"Delivered", "Pending"});
        deliveryDateField = new JTextField();
        scheduledDateField = new JTextField();

        // Apply FlatLaf placeholders (optional, for clarity)
        quantityField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter quantity (kg/tons)");
        deliveryDateField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g., 2025-06-15");
        scheduledDateField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Required if pending");

        panel.add(new JLabel("Pellets Type:"));
        panel.add(pelletsTypeCombo);

        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);

        panel.add(new JLabel("Delivery Status:"));
        panel.add(statusCombo);

        panel.add(new JLabel("Delivery Date (YYYY-MM-DD):"));
        panel.add(deliveryDateField);

        panel.add(new JLabel("Scheduled Date (if pending):"));
        panel.add(scheduledDateField);

        JButton saveBtn = new JButton("Save Delivery Info");
        saveBtn.addActionListener(this::saveDelivery);
        saveBtn.setBackground(new Color(52, 152, 219));
        saveBtn.setForeground(Color.WHITE);
        panel.add(new JLabel()); // Empty cell
        panel.add(saveBtn);

        add(panel, BorderLayout.CENTER);

        JButton viewBtn = new JButton("View Delivery Records");
        viewBtn.addActionListener(e -> DatabaseViewer.viewTable("deliveries"));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.add(viewBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners and logic
        toggleFields();
        statusCombo.addActionListener(e -> toggleFields());
    }

    private void toggleFields() {
        boolean isPending = "Pending".equals(statusCombo.getSelectedItem());
        deliveryDateField.setEnabled(!isPending);
        scheduledDateField.setEnabled(isPending);
    }

    private void saveDelivery(ActionEvent e) {
        try {
            String type = (String) pelletsTypeCombo.getSelectedItem();
            double quantity = Double.parseDouble(quantityField.getText().trim());
            String status = (String) statusCombo.getSelectedItem();
            String deliveryDate = deliveryDateField.getText().trim();
            String scheduledDate = scheduledDateField.getText().trim();

            if (type == null || quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Please fill required fields correctly.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = DatabaseManager.saveDelivery(type, quantity, status, scheduledDate, deliveryDate);
            if (success) {
                JOptionPane.showMessageDialog(this, "Delivery saved!");
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Error saving delivery.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity input", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        pelletsTypeCombo.setSelectedIndex(0);
        quantityField.setText("");
        scheduledDateField.setText("");
        deliveryDateField.setText("");
        statusCombo.setSelectedIndex(0);
        toggleFields();
    }
}
