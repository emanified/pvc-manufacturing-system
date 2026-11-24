package pvc.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class WorkerPanel extends JPanel {
    private JButton uploadImageBtn, removeImageBtn;
    private JLabel imageLabel, statusLabel;
    private byte[] imageBytes; // To store the image in memory
    private JTextField nameField, quantityField, generatedIdField;
    private JComboBox<String> roleCombo, shiftCombo, pvcTypeCombo, qualityCombo;
    private JButton generateIdButton, addWorkerBtn, submitProductionBtn, viewAllBtn;
    private JButton editBtn, deleteBtn, refreshBtn, loadDefaultsBtn;
    private BufferedImage selectedImage;
    private JPanel buttonPanel;



private int currentWorkerId = -1;
private final LoginPage.User currentUser;

    public WorkerPanel(LoginPage.User user) {
        this.currentUser = user;
    setLayout(new BorderLayout(10, 10));
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // === Title ===
    setLayout(new BorderLayout(10, 10));
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Create personalized title based on user role
    String titleText = String.format("%s Management - Welcome %s (%s)", 
                      user.getRole().equals("Supervisor") ? "Production" : "Worker",
                      user.getName(),
                      user.getRole());
    
    JLabel title = new JLabel(titleText, SwingConstants.CENTER);
    title.setFont(new Font("Segoe UI", Font.BOLD, 22));
    
    // Add role-specific styling
    switch(user.getRole()) {
        case "Admin":
            title.setForeground(new Color(0, 100, 0));  // Dark green for admin
            break;
        case "Supervisor":
            title.setForeground(new Color(0, 0, 150));  // Blue for supervisor
            break;
        default:
            title.setForeground(Color.BLACK);  // Black for regular workers
    }

    add(title, BorderLayout.NORTH);



    // === Scrollable Main Content ===
    JPanel contentPanel = new JPanel(new BorderLayout());

    // === Worker + Image Panel ===
    JPanel topPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;

    // === Worker Info Panel ===
    JPanel workerDetailsPanel = new JPanel(new GridBagLayout());
    workerDetailsPanel.setBorder(BorderFactory.createTitledBorder("Worker Information"));

    gbc.gridx = 0; gbc.gridy = 0;
    workerDetailsPanel.add(new JLabel("Name:"), gbc);
    gbc.gridx = 1;
    nameField = new JTextField(15);
    workerDetailsPanel.add(nameField, gbc);

    gbc.gridy++; gbc.gridx = 0;
    workerDetailsPanel.add(new JLabel("Role:"), gbc);
    gbc.gridx = 1;
    roleCombo = new JComboBox<>(new String[]{"Operator", "Supervisor", "Technician"});
    workerDetailsPanel.add(roleCombo, gbc);

    gbc.gridy++; gbc.gridx = 0;
    workerDetailsPanel.add(new JLabel("Generated ID:"), gbc);
    gbc.gridx = 1;
    generatedIdField = new JTextField(15);
    generatedIdField.setEditable(false);
    workerDetailsPanel.add(generatedIdField, gbc);

    gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
    generateIdButton = new JButton("Generate ID");
    generateIdButton.addActionListener(e -> {
        int randomId = (int) (Math.random() * 9000 + 1000);
        generatedIdField.setText(String.valueOf(randomId));
    });
    workerDetailsPanel.add(generateIdButton, gbc);

    // === Image Panel ===
    JPanel imagePanel = new JPanel(new BorderLayout(5, 5));
    imagePanel.setPreferredSize(new Dimension(180, 200));
    imagePanel.setBorder(BorderFactory.createTitledBorder("Worker Image"));

    imageLabel = new JLabel();
    imageLabel.setPreferredSize(new Dimension(150, 150));
    imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    imageLabel.setHorizontalAlignment(JLabel.CENTER);
    imagePanel.add(imageLabel, BorderLayout.CENTER);

    JPanel imageButtons = new JPanel(new FlowLayout());
    uploadImageBtn = new JButton("Upload");
    uploadImageBtn.addActionListener(e -> uploadImage());
    removeImageBtn = new JButton("Remove");
    removeImageBtn.addActionListener(e -> removeImage());
    imageButtons.add(uploadImageBtn);
    imageButtons.add(removeImageBtn);
    imagePanel.add(imageButtons, BorderLayout.SOUTH);

    // Add to topPanel
    gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.7;
    gbc.gridwidth = 1;
    topPanel.add(workerDetailsPanel, gbc);

    gbc.gridx = 1; gbc.weightx = 0.3;
    topPanel.add(imagePanel, gbc);

    // === Production Panel ===
    JPanel productionPanel = new JPanel(new GridBagLayout());
    productionPanel.setBorder(BorderFactory.createTitledBorder("Production Data"));
    gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    gbc.gridx = 0; gbc.gridy = 0;
    productionPanel.add(new JLabel("Shift:"), gbc);
    gbc.gridx = 1;
    shiftCombo = new JComboBox<>(new String[]{"Morning", "Afternoon", "Night"});
    productionPanel.add(shiftCombo, gbc);

    gbc.gridy++; gbc.gridx = 0;
    productionPanel.add(new JLabel("PVC Type:"), gbc);
    gbc.gridx = 1;
    pvcTypeCombo = new JComboBox<>(new String[]{"PVC-A", "PVC-B", "PVC-C"});
    productionPanel.add(pvcTypeCombo, gbc);

    gbc.gridy++; gbc.gridx = 0;
    productionPanel.add(new JLabel("Quantity (kg):"), gbc);
    gbc.gridx = 1;
    quantityField = new JTextField(15);
    productionPanel.add(quantityField, gbc);

    gbc.gridy++; gbc.gridx = 0;
    productionPanel.add(new JLabel("Quality Rating:"), gbc);
    gbc.gridx = 1;
    qualityCombo = new JComboBox<>(new String[]{"1", "2", "3", "4", "5"});
    productionPanel.add(qualityCombo, gbc);

    // === Buttons inside Production Panel ===
    gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
    JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

    viewAllBtn = new JButton("View All Workers");
    viewAllBtn.addActionListener(e -> showAllWorkers());
    buttonRow.add(viewAllBtn);

    addWorkerBtn = new JButton("Add Worker");
    addWorkerBtn.addActionListener(e -> addWorker());
    buttonRow.add(addWorkerBtn);

    submitProductionBtn = new JButton("Submit Production");
    submitProductionBtn.addActionListener(e -> submitProductionData());
    buttonRow.add(submitProductionBtn);

    editBtn = new JButton("Edit");
    editBtn.addActionListener(e -> updateWorker());
    buttonRow.add(editBtn);

    deleteBtn = new JButton("Delete");
    deleteBtn.addActionListener(e -> deleteWorker());
    buttonRow.add(deleteBtn);

    refreshBtn = new JButton("Refresh");
    refreshBtn.addActionListener(e -> refreshData());
    buttonRow.add(refreshBtn);

    loadDefaultsBtn = new JButton("Load Defaults");
    loadDefaultsBtn.addActionListener(e -> loadDefaults());
    buttonRow.add(loadDefaultsBtn);

    productionPanel.add(buttonRow, gbc);

    // === Add Panels to Scrollable Content ===
    contentPanel.add(topPanel, BorderLayout.NORTH);
    contentPanel.add(productionPanel, BorderLayout.CENTER);

    JScrollPane scrollPane = new JScrollPane(contentPanel);
    scrollPane.setBorder(null);
    add(scrollPane, BorderLayout.CENTER);

    // === Status Label at Bottom ===
    statusLabel = new JLabel(" ");
    statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
    add(statusLabel, BorderLayout.PAGE_END);
}
    // === Upload Image Method ===
    private void uploadImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                selectedImage = ImageIO.read(file);
                Image scaledImg = selectedImage.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImg));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(selectedImage, "jpg", baos);
                imageBytes = baos.toByteArray();

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage());
            }
        }
    }

    // === Remove Image Method ===
    private void removeImage() {
        selectedImage = null;
        imageBytes = null;
        imageLabel.setIcon(null);
    }

private void loadWorkerById(int id) {
    try (Connection conn = DatabaseConnector.connect();
         PreparedStatement stmt = conn.prepareStatement("SELECT * FROM workers WHERE worker_id = ?")) {

        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            loadWorker(rs);  // ✅ uses your existing display logic
            statusLabel.setText("Worker loaded.");
        } else {
            statusLabel.setText("Worker not found.");
        }

    } catch (SQLException ex) {
        statusLabel.setText("Error loading worker by ID: " + ex.getMessage());
    }
}

     private void showAllWorkers() {
    String[] columnNames = {"ID", "Name", "Role"};
    DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            // Make only the Role column editable
            return column == 2;
        }
    };

    String sql = "SELECT worker_id, name, role FROM workers ORDER BY worker_id ASC";

    try (Connection conn = DatabaseConnector.connect();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            int id = rs.getInt("worker_id");
            String name = rs.getString("name");
            String role = rs.getString("role");
            model.addRow(new Object[]{id, name, role});
        }

        JTable table = new JTable(model);

        // Add ComboBox editor for Role column
        String[] roles = {"Operator", "Supervisor", "Technician"};
        JComboBox<String> roleEditor = new JComboBox<>(roles);
        TableColumn roleColumn = table.getColumnModel().getColumn(2);
        roleColumn.setCellEditor(new DefaultCellEditor(roleEditor));

        JScrollPane scrollPane = new JScrollPane(table);

        int result = JOptionPane.showConfirmDialog(this, scrollPane, "All Workers (Editable Roles)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            // Save updates to DB
            for (int i = 0; i < model.getRowCount(); i++) {
                int id = (int) model.getValueAt(i, 0);
                String updatedRole = (String) model.getValueAt(i, 2);

                try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE workers SET role = ? WHERE worker_id = ?")) {
                    updateStmt.setString(1, updatedRole);
                    updateStmt.setInt(2, id);
                    updateStmt.executeUpdate();
                }
            }
            JOptionPane.showMessageDialog(this, "Worker roles updated successfully.");
        }

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error fetching workers: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}


    private void addWorker() {
    String name = nameField.getText().trim();
    String role = (String) roleCombo.getSelectedItem();
    String generatedId = generatedIdField.getText().trim();

    if (name.isEmpty() || generatedId.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Name and ID required", "Validation Error", JOptionPane.WARNING_MESSAGE);
        return;
    }

    int workerId;
    try {
        workerId = Integer.parseInt(generatedId);
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Invalid ID format", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    

    String sql = "INSERT INTO workers (worker_id, name, role, image) VALUES (?, ?, ?, ?)";

try (Connection conn = DatabaseConnector.connect();
     PreparedStatement stmt = conn.prepareStatement(sql)) {

    stmt.setInt(1, workerId);
    stmt.setString(2, name);
    stmt.setString(3, role);
    stmt.setBytes(4, imageBytes);  // set this AFTER stmt is created

    stmt.executeUpdate();

    JOptionPane.showMessageDialog(this, "Worker added successfully");

    loadWorkerById(workerId);

} catch (SQLException ex) {
    ex.printStackTrace();
    statusLabel.setText("Error adding worker: " + ex.getMessage());
}
    }

    private void submitProductionData() {
        if (currentWorkerId <= 0) {
            JOptionPane.showMessageDialog(this, "No worker selected", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String qtyText = quantityField.getText().trim();
        if (qtyText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter quantity", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double quantity = Double.parseDouble(qtyText);
            String shift = (String) shiftCombo.getSelectedItem();
            String pvcType = (String) pvcTypeCombo.getSelectedItem();
            int quality = Integer.parseInt((String) qualityCombo.getSelectedItem());

            String sql = "INSERT INTO production (worker_id, shift, pvc_type, quantity_kg, quality_rating) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnector.connect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, currentWorkerId);
                stmt.setString(2, shift);
                stmt.setString(3, pvcType);
                stmt.setDouble(4, quantity);
                stmt.setInt(5, quality);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Production data submitted");
                clearProductionFields();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadWorker(ResultSet rs) throws SQLException {
    currentWorkerId = rs.getInt("worker_id");
    nameField.setText(rs.getString("name"));
    roleCombo.setSelectedItem(rs.getString("role"));
    generatedIdField.setText(String.valueOf(currentWorkerId));  // ✅ Add this line

    // Load latest production for this worker
    try (Connection conn = DatabaseConnector.connect();
     PreparedStatement stmt = conn.prepareStatement(
             "SELECT * FROM production WHERE worker_id = ? ORDER BY id DESC LIMIT 1")) {
    stmt.setInt(1, currentWorkerId);
    ResultSet prodRs = stmt.executeQuery();

    if (prodRs.next()) {
        shiftCombo.setSelectedItem(prodRs.getString("shift"));
        pvcTypeCombo.setSelectedItem(prodRs.getString("pvc_type"));
        quantityField.setText(String.valueOf(prodRs.getDouble("quantity_kg")));
        qualityCombo.setSelectedItem(String.valueOf(prodRs.getInt("quality_rating")));
    } else {
        clearProductionFields();
    }

} catch (SQLException ex) {
    ex.printStackTrace(); // ✅ ADD THIS LINE
    statusLabel.setText("Error loading production data.");
}


    updateNavButtons();
}

    private void updateWorker() {
        String name = nameField.getText().trim();
        String role = (String) roleCombo.getSelectedItem();

        if (currentWorkerId <= 0 || name.isEmpty()) {
            statusLabel.setText("Select a worker to update.");
            return;
        }

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement("UPDATE workers SET name=?, role=? WHERE worker_id=?")) {
            stmt.setString(1, name);
            stmt.setString(2, role);
            stmt.setInt(3, currentWorkerId);
            stmt.executeUpdate();
            statusLabel.setText("Worker updated.");
        } catch (SQLException ex) {
            statusLabel.setText("Error updating worker.");
        }
    }

    private void deleteWorker() {
        if (currentWorkerId <= 0) return;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM workers WHERE worker_id=?")) {
            stmt.setInt(1, currentWorkerId);
            stmt.executeUpdate();
            clearProductionFields();
            statusLabel.setText("Worker deleted.");
            refreshData();
        } catch (SQLException ex) {
            statusLabel.setText("Error deleting worker.");
        }
    }

    private void refreshData() {

    }

private void clearProductionFields() {
    shiftCombo.setSelectedIndex(0);
    pvcTypeCombo.setSelectedIndex(0);
    quantityField.setText("");
    qualityCombo.setSelectedIndex(0);
}


    private void loadDefaults() {
        shiftCombo.setSelectedIndex(0);
        pvcTypeCombo.setSelectedIndex(0);
        qualityCombo.setSelectedIndex(2);
        quantityField.setText("0");
        statusLabel.setText("Default values loaded");
    }

    private void updateNavButtons() {
        boolean enabled = currentWorkerId > 0;
        editBtn.setEnabled(enabled);
        deleteBtn.setEnabled(enabled);
        
    }
   

}
