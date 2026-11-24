package pvc;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;

import pvc.panels.AgreementPanel;
import pvc.panels.LoginPage;
import pvc.panels.ManagerPanel;
import pvc.panels.MenuPanel;
import pvc.panels.ProducePanel;
import pvc.panels.SalaryPanel;
import pvc.panels.SupervisorPanel;
import pvc.panels.SupplierPanel;
import pvc.panels.WorkerPanel;

public class Main {
    public static JTabbedPane tabs = new JTabbedPane();
    public static JFrame mainFrame = new JFrame("PVC Factory Management System");
    public static CardLayout cardLayout = new CardLayout();
    public static JPanel cardPanel = new JPanel(cardLayout);
    public static LoginPage.User currentUser;

    public static void main(String[] args) {
        
       mainFrame = new JFrame("PVC Factory Management System");
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);
        
        SwingUtilities.invokeLater(() -> {
            initializeMainFrame();
            cardPanel.add(new LoginPage(), "login");
            showLoginScreen();
        });
    }
    private static void initializeMainFrame() {
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        cardPanel.add(new LoginPage(), "login");

        mainFrame.add(cardPanel);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    private static void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("TabbedPane.tabInsets", new Insets(10, 20, 10, 20));
            UIManager.put("TabbedPane.selectedBackground", new Color(60, 63, 65));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JPanel wrapMainWithMenu() {
        JPanel wrapper = new JPanel(new BorderLayout());

        // Sidebar
        MenuPanel menu = new MenuPanel(currentUser);
        wrapper.add(menu, BorderLayout.WEST);

        // Tabs
        wrapper.add(tabs, BorderLayout.CENTER);

        return wrapper;
    }

    // Call this after successful login
    public static void showMainApplicationWithTab(LoginPage.User user, String tabName) {
        currentUser = user;

        System.out.println("Login successful for: " + user.getName() + ", Role: " + user.getRole());

        tabs = new JTabbedPane(); // reset tabs

        // Common tabs with error protection
        try { tabs.addTab("Dashboard", new AgreementPanel()); } catch (Exception e) { e.printStackTrace(); }
        try { tabs.addTab("Produce", new ProducePanel()); } catch (Exception e) { e.printStackTrace(); }
        try { tabs.addTab("Worker", new WorkerPanel(user)); } catch (Exception e) { e.printStackTrace(); }
        try { tabs.addTab("Salary", new SalaryPanel()); } catch (Exception e) { e.printStackTrace(); }

        // Role-specific tabs
        switch (user.getRole()) {
            case "Supervisor":
                try { tabs.addTab("Supervisor", new SupervisorPanel(user)); } catch (Exception e) { e.printStackTrace(); }
                break;
            case "Manager":
                try { tabs.addTab("Manager", new ManagerPanel()); } catch (Exception e) { e.printStackTrace(); }
                break;
            case "Supplier":
                try { tabs.addTab("Supplier", new SupplierPanel()); } catch (Exception e) { e.printStackTrace(); }
                break;
            // Add Worker panel above (already done)
        }

        // Select the requested tab
        for (int i = 0; i < tabs.getTabCount(); i++) {
            if (tabs.getTitleAt(i).equalsIgnoreCase(tabName)) {
                tabs.setSelectedIndex(i);
                break;
            }
        }

        // Wrap with menu
        JPanel wrappedPanel = wrapMainWithMenu();
        cardPanel.removeAll(); // remove old panels
        cardPanel.add(wrappedPanel, "main");

        // ✅ Fix: Force repaint so it shows immediately
        cardPanel.revalidate();
        cardPanel.repaint();

        cardLayout.show(cardPanel, "main");

        mainFrame.setTitle("PVC System - " + user.getName() + " (" + user.getRole() + ")");
    }
    //logout
   public static void showLoginScreen() {
    currentUser = null; // Clear current user
    
    // Remove all panels except login
    Component[] components = cardPanel.getComponents();
    for (Component comp : components) {
        if (comp != null && !(comp instanceof LoginPage)) {
            cardPanel.remove(comp);
        }
    }
    
    // Show login panel
    cardLayout.show(cardPanel, "login");
    mainFrame.setTitle("PVC Factory Management System");
    
    // Force UI update
    cardPanel.revalidate();
    cardPanel.repaint();
}
}
