package pvc.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class ProducePanel extends JPanel {
    private JTextArea reportArea;

    public ProducePanel() {
        setLayout(new BorderLayout(20, 20));
        setOpaque(false); // Allow theme background (FlatLaf compatible)

        // ====== TITLE ======
        JLabel titleLabel = new JLabel("📊 Production & Inventory Reports");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(UIManager.getColor("Label.foreground"));
        titleLabel.setBorder(new EmptyBorder(15, 15, 0, 0));

        // ====== BUTTON PANEL ======
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 20, 10, 20),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")),
                        "Generate Reports",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("SansSerif", Font.BOLD, 14),
                        UIManager.getColor("Label.foreground")
                )
        ));

        addReportButton(buttonPanel, "📦 Production Report", DatabaseManager::getProductionReport, new Color(72, 139, 72)); // Forest Green
        addReportButton(buttonPanel, "🛠 Labor Cost Report", DatabaseManager::getLaborCostReport, new Color(199, 88, 111)); // Dusty Rose
        addReportButton(buttonPanel, "📦 Inventory Status", DatabaseManager::getInventoryStatus, new Color(70, 130, 180)); // Steel Blue
        addReportButton(buttonPanel, "📇 Supplier Contacts", DatabaseManager::getSupplierContacts, new Color(123, 104, 238)); // Medium Slate Blue

        // ====== REPORT AREA ======
        reportArea = new JTextArea(15, 80);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        reportArea.setEditable(false);
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 20, 20, 20),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")),
                        "Report Output",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("SansSerif", Font.BOLD, 14),
                        UIManager.getColor("Label.foreground")
                )
        ));

        // ====== ADD TO MAIN PANEL ======
        add(titleLabel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
    }

    private void addReportButton(JPanel panel, String title, ReportFetcher fetcher, Color color) {
    JButton btn = new JButton(title);
    styleButton(btn, color);

    // Set a taller preferred size for each button (width will be managed by GridLayout)
    btn.setPreferredSize(new Dimension(0, 60)); // 60px height, width auto-adjusted

    btn.addActionListener(e -> displayReport(title, fetcher.fetch()));
    panel.add(btn);
}


    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Increased vertical padding for taller buttons
        button.setBorder(new EmptyBorder(20, 15, 20, 15)); // top, left, bottom, right
    }

    private void displayReport(String title, String content) {
        if (content == null || content.trim().isEmpty()) {
            reportArea.setText("⚠️ " + title + " returned no data.");
        } else if (content.length() > 5000) {
            JTextArea area = new JTextArea(content, 20, 80);
            area.setFont(new Font("Monospaced", Font.PLAIN, 12));
            area.setEditable(false);
            area.setWrapStyleWord(true);
            area.setLineWrap(true);
            JScrollPane scroll = new JScrollPane(area);
            scroll.setPreferredSize(new Dimension(800, 400));
            JOptionPane.showMessageDialog(this, scroll, title, JOptionPane.INFORMATION_MESSAGE);
        } else {
            reportArea.setText(content);
        }
    }

    @FunctionalInterface
    interface ReportFetcher {
        String fetch();
    }
}
