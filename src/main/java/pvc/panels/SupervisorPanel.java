package pvc.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

public class SupervisorPanel extends JPanel {
    // Constants
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Color BUTTON_COLOR = new Color(52, 152, 219);
    
    // UI Components
    private JTable shiftTable;
    private JTextArea issueLog;
    private JComboBox<String> workerCombo;
    private JComboBox<String> severityCombo;
    private JTextField issueField;
    private JButton logBtn, generateReportBtn;

    public SupervisorPanel(LoginPage.User user){
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        String titleText = String.format("Supervisor Dashboard - %s", user.getName());
        JLabel title = new JLabel(titleText, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(0, 0, 150)); 

        // Initialize components
        initializeComponents();
        
        // Build UI
        add(createHeader(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        
        loadShiftData(); // Start timer
        loadWorkers(); // Load worker data
    }

    private void initializeComponents() {
        // Shift Table
        shiftTable = new JTable(new DefaultTableModel(
            new Object[]{"Worker ID", "Name", "Current Task", "Output (kg)", "Quality"}, 0));
        configureTableAppearance(shiftTable);
        
        // Issue Log
        issueLog = new JTextArea(7, 40);
        configureIssueLog(issueLog);
        
        // Combo Boxes
        workerCombo = new JComboBox<>();
        severityCombo = new JComboBox<>(new String[]{"Low", "Medium", "High", "Critical"});
        
        // Issue Field
        issueField = new JTextField();
        configureIssueField(issueField);
        
        // Buttons
        logBtn = new JButton("Log Issue");
        configureLogButton(logBtn);
        
        generateReportBtn = new JButton("Generate Shift Report");
        configureReportButton(generateReportBtn);
    }

    private void configureTableAppearance(JTable table) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.setFont(TABLE_FONT);
        table.getTableHeader().setFont(TABLE_FONT.deriveFont(Font.BOLD));
        table.setAutoCreateRowSorter(true);
    }

    private void configureIssueLog(JTextArea log) {
        log.setEditable(false);
        log.setFont(new Font("Monospaced", Font.PLAIN, 12));
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private void configureIssueField(JTextField field) {
        field.setFont(TABLE_FONT);
        field.setMargin(new Insets(5, 8, 5, 8));
        field.setPreferredSize(new Dimension(280, 70));
    }

    private void configureLogButton(JButton button) {
        button.setFont(TABLE_FONT.deriveFont(Font.BOLD));
        button.setPreferredSize(new Dimension(120, 30));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.addActionListener(this::logIssue);
    }

    private void configureReportButton(JButton button) {
        button.setFont(TABLE_FONT.deriveFont(Font.BOLD));
        button.setBackground(new Color(76, 175, 80));
        button.setForeground(Color.WHITE);
        button.addActionListener(e -> generateShiftReport());
    }

    private JComponent createHeader() {
        JLabel header = new JLabel("Supervisor Dashboard");
        header.setFont(HEADER_FONT);
        header.setHorizontalAlignment(SwingConstants.CENTER);
        return header;
    }

    private JComponent createMainPanel() {
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        verticalSplit.setResizeWeight(0.4);
        verticalSplit.setDividerSize(8);
        verticalSplit.setContinuousLayout(true);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JScrollPane(shiftTable), BorderLayout.CENTER);
        shiftTable.setBorder(BorderFactory.createTitledBorder("Live Shift Monitoring"));

        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        bottomPanel.add(createIssuePanel());
        bottomPanel.add(createActionPanel());

        verticalSplit.setTopComponent(topPanel);
        verticalSplit.setBottomComponent(bottomPanel);
        
        return verticalSplit;
    }

    private JPanel createIssuePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Issue Reporting"));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        inputPanel.add(new JLabel("Worker:"));
        inputPanel.add(workerCombo);
        inputPanel.add(new JLabel("Severity:"));
        inputPanel.add(severityCombo);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(issueField);
        inputPanel.add(logBtn);

        JScrollPane logScroll = new JScrollPane(issueLog);
        logScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(inputPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(logScroll);
        
        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 8, 12));
        panel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));

        String[] actions = {"Call Break", "Request Maintenance", "Emergency Stop", "Generate Shift Report"};
        for (String action : actions) {
            JButton btn = new JButton(action);
            btn.setFont(TABLE_FONT.deriveFont(Font.BOLD));
            btn.addActionListener(this::handleQuickAction);
            panel.add(btn);
        }
        
        return panel;
    }

    private void loadWorkers() {
        workerCombo.removeAllItems();
        workerCombo.addItem("Select Worker");
        
        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT worker_id, name FROM workers")) {
            
            while (rs.next()) {
                workerCombo.addItem(rs.getInt("worker_id") + " - " + rs.getString("name"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading workers: " + ex.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logIssue(ActionEvent e) {
        String worker = (String) workerCombo.getSelectedItem();
        String severity = (String) severityCombo.getSelectedItem();
        String issue = issueField.getText();

        if (worker.equals("Select Worker") || issue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a worker and describe the issue!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int workerId = Integer.parseInt(worker.split("-")[0].trim());
            boolean success = DatabaseManager.logSupervisorIssue(workerId, severity, issue);

            if (success) {
                String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                issueLog.append(String.format("[%s] %s - %s: %s\n", 
                    timestamp, severity, worker, issue));
                issueField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to log issue to DB", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void handleQuickAction(ActionEvent e) {
        String action = ((JButton) e.getSource()).getText();
        switch (action) {
            case "Call Break":
                JOptionPane.showMessageDialog(this, "Break called for current shift", 
                    "Break Time", JOptionPane.INFORMATION_MESSAGE);
                break;
            case "Emergency Stop":
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "STOP ALL MACHINES?", "EMERGENCY", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    issueLog.append("[SYSTEM] EMERGENCY STOP ACTIVATED\n");
                }
                break;
            case "Generate Shift Report":
                generateShiftReport();
                break;
            default:
                JOptionPane.showMessageDialog(this, "Executing: " + action, 
                    "Supervisor Action", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void generateShiftReport() {
        try {
            // Generate report from database
            String report = generateReportFromDB();
            
            // Show report in dialog
            JTextArea reportArea = new JTextArea(report, 15, 40);
            reportArea.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(reportArea), 
                "Shift Report", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + ex.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String generateReportFromDB() throws SQLException {
        StringBuilder report = new StringBuilder();
        report.append("=== SHIFT REPORT ===\n");
        report.append("Generated: ").append(LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        
        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
    "SELECT w.worker_id, w.name, p.pvc_type as task, " +
    "SUM(p.quantity_kg) as output, AVG(p.quality_rating) as quality " +
    "FROM workers w " +
    "LEFT JOIN production p ON w.worker_id = p.worker_id " +
    "AND p.production_date >= datetime('now', '-8 hours') " +
    "GROUP BY w.worker_id"))

{
            while (rs.next()) {
                report.append(String.format("%s - %s: %.1f kg (Quality: %.1f/5)\n",
                    rs.getString("name"),
                    rs.getString("pvc_type"),
                    rs.getDouble("total"),
                    rs.getDouble("quality")));
            }
        }
        
        return report.toString();
    }

    private void loadShiftData() {
    refreshShiftData();
    System.out.println("Refreshing live shift data...");
        Timer timer = new Timer(20000, e -> refreshShiftData());
        timer.start();
        refreshShiftData();
    }

    private void refreshShiftData() {
        DefaultTableModel model = (DefaultTableModel) shiftTable.getModel();
        model.setRowCount(0); // Clear existing data
        
        
        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT w.worker_id, w.name, p.pvc_type as task, " +
                 "SUM(p.quantity_kg) as output, AVG(p.quality_rating) as quality " +
                 "FROM workers w LEFT JOIN production p ON w.worker_id = p.worker_id " +
                 "WHERE p.production_date >= datetime('now', '-8 hours') " +
                 "GROUP BY w.worker_id")) {
            
            while (rs.next()) {
                double quality = rs.getDouble("quality");
                String qualityText = quality >= 4 ? "Excellent" : 
                                   quality >= 3 ? "Good" : 
                                   quality >= 2 ? "Fair" : "Poor";
                
                model.addRow(new Object[]{
                    rs.getInt("worker_id"),
                    rs.getString("name"),
                    rs.getString("task"),
                    rs.getDouble("output"),
                    qualityText
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading shift data: " + ex.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}