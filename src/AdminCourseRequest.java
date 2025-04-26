import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;

public class AdminCourseRequest extends JFrame {
    private JTable requestTable;
    private DefaultTableModel pendingModel, approvedModel, rejectedModel;
    private JTabbedPane tabbedPane;

    public AdminCourseRequest(String adminEnrollmentNumber) {
        setTitle("Manage Course Requests");
        setSize(900, 610);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        JPanel header = new JPanel();
        header.setBackground(new Color(255, 140, 0));
        header.setBounds(0, 0, 900, 70);
        header.setLayout(null);

        JLabel title = new JLabel("COURSE REQUESTS MANAGEMENT");
        title.setFont(loadMontserratFont().deriveFont(26f));
        title.setForeground(new Color(10, 25, 74));
        title.setBounds(20, 10, 600, 50);
        header.add(title);

        ImageIcon logoIcon = new ImageIcon("D:\\testing\\logo.png");
        JLabel logo = new JLabel(logoIcon);
        logo.setBounds(790, 5, 80, 55);
        header.add(logo);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(10, 25, 74));
        panel.setBounds(0, 70, 900, 530);
        panel.setLayout(null);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(20, 20, 850, 400);
        tabbedPane.setFont(loadMontserratFont().deriveFont(16f));

        // Create models for each tab
        pendingModel = createTableModel();
        approvedModel = createTableModel();
        rejectedModel = createTableModel();

        // Create tables for each tab
        requestTable = new JTable(pendingModel);
        requestTable.setFont(loadMontserratFont().deriveFont(16f));
        requestTable.setRowHeight(28);
        requestTable.getTableHeader().setFont(loadMontserratFont().deriveFont(16f));
        requestTable.getTableHeader().setBackground(new Color(255, 140, 0));
        requestTable.getTableHeader().setForeground(new Color(10, 25, 74));

        JTable approvedTable = new JTable(approvedModel);
        styleTable(approvedTable);

        JTable rejectedTable = new JTable(rejectedModel);
        styleTable(rejectedTable);

        // Add tabs
        tabbedPane.addTab("Pending Requests", new JScrollPane(requestTable));
        tabbedPane.addTab("Approved Requests", new JScrollPane(approvedTable));
        tabbedPane.addTab("Rejected Requests", new JScrollPane(rejectedTable));

        panel.add(tabbedPane);

        // Action buttons
        JButton approveButton = new JButton("APPROVE");
        approveButton.setBounds(20, 430, 180, 50);
        styleButton(approveButton);
        panel.add(approveButton);

        JButton rejectButton = new JButton("REJECT");
        rejectButton.setBounds(220, 430, 180, 50);
        styleButton(rejectButton);
        panel.add(rejectButton);

        JButton refreshButton = new JButton("REFRESH");
        refreshButton.setBounds(420, 430, 180, 50);
        styleButton(refreshButton);
        panel.add(refreshButton);

        // Back button
        JButton backButton = createBackButton();
        backButton.setBounds(620, 430, 250, 50);
        panel.add(backButton);

        // Button actions
        approveButton.addActionListener(e -> {
            int selectedRow = requestTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a request.");
                return;
            }

            int batchNumber = (int) pendingModel.getValueAt(selectedRow, 4);
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to approve this request for batch " + batchNumber + "?",
                    "Confirm Approval",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                handleRequest("Approved", adminEnrollmentNumber);
            }
        });

        rejectButton.addActionListener(e -> {
            int selectedRow = requestTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a request.");
                return;
            }

            int batchNumber = (int) pendingModel.getValueAt(selectedRow, 4);
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to reject this request for batch " + batchNumber + "?",
                    "Confirm Rejection",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                handleRequest("Rejected", adminEnrollmentNumber);
            }
        });

        refreshButton.addActionListener(e -> refreshAllData());

        backButton.addActionListener(e -> {
            dispose();
            new adminDashboard(adminEnrollmentNumber);
        });

        // Load initial data
        refreshAllData();

        add(header);
        add(panel);
        setVisible(true);
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(
                new Object[]{"Request ID", "Teacher ID", "Course Code", "Course Title", "Batch Number", "Requested At", "Status"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void styleTable(JTable table) {
        table.setFont(loadMontserratFont().deriveFont(16f));
        table.setRowHeight(28);
        table.getTableHeader().setFont(loadMontserratFont().deriveFont(16f));
        table.getTableHeader().setBackground(new Color(255, 140, 0));
        table.getTableHeader().setForeground(new Color(10, 25, 74));
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(255, 140, 0));
        button.setForeground(new Color(10, 25, 74));
        button.setFont(loadMontserratFont().deriveFont(20f));
    }

    private JButton createBackButton() {
        JButton backButton = new JButton();
        backButton.setForeground(new Color(10, 25, 74));
        backButton.setBackground(new Color(255, 140, 0));
        backButton.setLayout(null);

        ImageIcon arrowIcon = new ImageIcon("D:\\testing\\left-arrow.png");
        JLabel arrowLabel = new JLabel(new ImageIcon(arrowIcon.getImage().getScaledInstance(30, 25, Image.SCALE_SMOOTH)));
        arrowLabel.setBounds(10, 9, 30, 30);
        backButton.add(arrowLabel);

        JLabel backText = new JLabel("BACK TO DASHBOARD", SwingConstants.RIGHT);
        backText.setFont(loadMontserratFont().deriveFont(16f));
        backText.setForeground(new Color(10, 25, 74));
        backText.setBounds(40, 14, 200, 20);
        backButton.add(backText);

        return backButton;
    }

    private void refreshAllData() {
        loadRequestsByStatus(pendingModel, "Pending");
        loadRequestsByStatus(approvedModel, "Approved");
        loadRequestsByStatus(rejectedModel, "Rejected");
    }

    private void loadRequestsByStatus(DefaultTableModel model, String status) {
        model.setRowCount(0); // Clear existing data

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT r.request_id, r.teacher_enrollment_number, r.course_code, c.course_title, " +
                            "r.batch_number, r.requested_at, r.status, r.processed_at, r.processed_by " +
                            "FROM course_requests r JOIN courses c ON r.course_code = c.course_code " +
                            "WHERE r.status = ? ORDER BY r.requested_at DESC"
            );
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("request_id"),
                        rs.getString("teacher_enrollment_number"),
                        rs.getString("course_code"),
                        rs.getString("course_title"),
                        rs.getInt("batch_number"),
                        rs.getTimestamp("requested_at"),
                        rs.getString("status")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading " + status.toLowerCase() + " requests.");
        }
    }

    private void handleRequest(String newStatus, String adminEnrollmentNumber) {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1) return;

        int requestId = (int) pendingModel.getValueAt(selectedRow, 0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Update the request status
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE course_requests SET status = ?, processed_by = ?, processed_at = CURRENT_TIMESTAMP " +
                            "WHERE request_id = ?"
            );
            ps.setString(1, newStatus);
            ps.setString(2, adminEnrollmentNumber);
            ps.setInt(3, requestId);
            ps.executeUpdate();

            // If approved, add the course assignment
            if (newStatus.equals("Approved")) {
                String teacherId = (String) pendingModel.getValueAt(selectedRow, 1);
                String courseCode = (String) pendingModel.getValueAt(selectedRow, 2);
                int batchNumber = (int) pendingModel.getValueAt(selectedRow, 4);

                // Check if assignment already exists
                PreparedStatement checkPs = conn.prepareStatement(
                        "SELECT 1 FROM teacher_courses WHERE teacher_enrollment_number = ? " +
                                "AND course_code = ? AND batch_number = ?"
                );
                checkPs.setString(1, teacherId);
                checkPs.setString(2, courseCode);
                checkPs.setInt(3, batchNumber);

                ResultSet rs = checkPs.executeQuery();
                if (!rs.next()) {
                    // Add new assignment if it doesn't exist
                    PreparedStatement assignPs = conn.prepareStatement(
                            "INSERT INTO teacher_courses (teacher_enrollment_number, course_code, batch_number) " +
                                    "VALUES (?, ?, ?)"
                    );
                    assignPs.setString(1, teacherId);
                    assignPs.setString(2, courseCode);
                    assignPs.setInt(3, batchNumber);
                    assignPs.executeUpdate();
                }
            }

            JOptionPane.showMessageDialog(this, "Request " + newStatus + " successfully.");
            refreshAllData();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error processing request.");
        }
    }

    private Font loadMontserratFont() {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, new File("D:\\testing\\src\\Montserrat-Bold.ttf"));
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("Arial", Font.BOLD, 18);
        }
    }

    public static void main(String[] args) {
        new AdminCourseRequest("T24CSEUXXXX");
    }
}