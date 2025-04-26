import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import javax.swing.BorderFactory;

public class TeacherCourseRequest extends JFrame {
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JPanel notificationPanel;
    private JLabel notificationLabel;

    public TeacherCourseRequest(String enrollmentNumber) {
        setTitle("Course Request");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        JPanel header = new JPanel();
        header.setBackground(new Color(255, 140, 0));
        header.setBounds(0, 0, 900, 70);
        header.setLayout(null);

        JLabel title = new JLabel("REQUEST COURSE");
        title.setFont(loadMontserratFont().deriveFont(28f));
        title.setForeground(new Color(10, 25, 74));
        title.setBounds(20, 10, 400, 50);
        header.add(title);

        ImageIcon logoIcon = new ImageIcon("D:\\testing\\new_logo.png");
        JLabel logo = new JLabel(logoIcon);
        logo.setBounds(780, 5, 100, 60);
        header.add(logo);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(10, 25, 74));
        panel.setBounds(0, 70, 900, 530);
        panel.setLayout(null);

        // Enhanced notification panel
        notificationPanel = new JPanel();
        notificationPanel.setBounds(30, 20, 830, 50);
        notificationPanel.setBackground(new Color(255, 250, 200));
        notificationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 200, 100), 2),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        notificationPanel.setLayout(new BorderLayout());

        notificationLabel = new JLabel();
        notificationLabel.setFont(loadMontserratFont().deriveFont(16f));
        notificationLabel.setForeground(new Color(50, 50, 50));
        notificationLabel.setHorizontalAlignment(SwingConstants.LEFT);

        // Add icon to notification
        ImageIcon noticeIcon = new ImageIcon("D:\\testing\\notification-icon.png"); // Replace with your icon path
        noticeIcon = new ImageIcon(noticeIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        JLabel iconLabel = new JLabel(noticeIcon);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        JPanel contentPanel = new JPanel(new BorderLayout(10, 0));
        contentPanel.setOpaque(false);
        contentPanel.add(iconLabel, BorderLayout.WEST);
        contentPanel.add(notificationLabel, BorderLayout.CENTER);

        notificationPanel.add(contentPanel, BorderLayout.CENTER);
        panel.add(notificationPanel);

        tableModel = new DefaultTableModel(new Object[]{"Course Code", "Course Title", "Batch Number"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        courseTable = new JTable(tableModel);
        courseTable.setFont(loadMontserratFont().deriveFont(16f));
        courseTable.setRowHeight(30);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.getTableHeader().setFont(loadMontserratFont().deriveFont(18f));
        courseTable.getTableHeader().setBackground(new Color(255, 140, 0));
        courseTable.getTableHeader().setForeground(new Color(10, 25, 74));

        JScrollPane courseScrollPane = new JScrollPane(courseTable);
        courseScrollPane.setBounds(30, 90, 830, 280); // Adjusted y-position for notification panel
        panel.add(courseScrollPane);

        JComboBox<String> batchSelector = new JComboBox<>();
        for (int i = 61; i <= 66; i++) {
            batchSelector.addItem(String.valueOf(i));
        }
        batchSelector.setBounds(30, 385, 100, 40); // Adjusted y-position
        batchSelector.setFont(loadMontserratFont().deriveFont(16f));
        panel.add(batchSelector);

        JButton requestButton = new JButton("Request Selected Course");
        requestButton.setBounds(140, 385, 300, 40); // Adjusted y-position
        styleButton(requestButton, 16f);
        panel.add(requestButton);

        JButton checkStatusButton = new JButton("View Request Status");
        checkStatusButton.setBounds(450, 385, 200, 40); // Adjusted y-position
        styleButton(checkStatusButton, 14f);
        panel.add(checkStatusButton);

        JButton backButton = createBackButton();
        backButton.setBounds(660, 385, 200, 40); // Adjusted y-position
        panel.add(backButton);

        requestButton.addActionListener(e -> {
            int selectedRow = courseTable.getSelectedRow();
            if (selectedRow == -1) {
                showMessage("Please select a course.", Color.RED);
                return;
            }

            String selectedCourseCode = (String) tableModel.getValueAt(selectedRow, 0);
            String selectedBatch = (String) batchSelector.getSelectedItem();

            try (Connection conn = DatabaseConnection.getConnection()) {
                // Check if request already exists
                PreparedStatement checkPs = conn.prepareStatement(
                        "SELECT 1 FROM course_requests WHERE teacher_enrollment_number = ? " +
                                "AND course_code = ? AND batch_number = ? AND status = 'pending'"
                );
                checkPs.setString(1, enrollmentNumber);
                checkPs.setString(2, selectedCourseCode);
                checkPs.setString(3, selectedBatch);

                if (checkPs.executeQuery().next()) {
                    showMessage("You already have a pending request for this course and batch.", Color.ORANGE);
                    return;
                }

                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO course_requests (teacher_enrollment_number, course_code, batch_number, status) " +
                                "VALUES (?, ?, ?, 'pending')"
                );
                ps.setString(1, enrollmentNumber);
                ps.setString(2, selectedCourseCode);
                ps.setString(3, selectedBatch);
                ps.executeUpdate();

                showMessage("Request sent for " + selectedCourseCode + " in batch " + selectedBatch + ".", new Color(0, 150, 0));
                updateNotifications(enrollmentNumber);
            } catch (SQLException ex) {
                ex.printStackTrace();
                showMessage("Error requesting course.", Color.RED);
            }
        });

        checkStatusButton.addActionListener(e -> showRequestStatusDialog(enrollmentNumber));
        backButton.addActionListener(e -> {
            dispose();
            new teacherDashboard(enrollmentNumber);
        });

        updateNotifications(enrollmentNumber);
        loadCourses();

        add(header);
        add(panel);
        setVisible(true);
    }

    private void showMessage(String message, Color color) {
        notificationLabel.setForeground(color);
        notificationLabel.setText("<html><div style='width:750px;'>" + message + "</div></html>");

        // Flash effect
        Timer timer = new Timer(300, e -> {
            notificationPanel.setBackground(new Color(255, 250, 200));
        });
        timer.setRepeats(false);
        notificationPanel.setBackground(color.brighter());
        timer.start();
    }

    private void showRequestStatusDialog(String enrollmentNumber) {
        JTextArea statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setFont(loadMontserratFont().deriveFont(16f));
        statusArea.setMargin(new Insets(10, 10, 10, 10));

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT course_code, batch_number, status, requested_at, processed_at " +
                    "FROM course_requests WHERE teacher_enrollment_number = ? " +
                    "ORDER BY requested_at DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, enrollmentNumber);
            ResultSet rs = ps.executeQuery();

            StringBuilder sb = new StringBuilder("Your Course Request Status:\n\n");
            while (rs.next()) {
                String code = rs.getString("course_code");
                String batch = rs.getString("batch_number");
                String status = rs.getString("status");
                Timestamp requested = rs.getTimestamp("requested_at");
                Timestamp processed = rs.getTimestamp("processed_at");

                sb.append("• Course: ").append(code)
                        .append(" | Batch: ").append(batch)
                        .append("\nStatus: ").append(status.toUpperCase())
                        .append("\nRequested: ").append(requested)
                        .append(processed != null ? "\nProcessed: " + processed : "")
                        .append("\n\n");
            }
            statusArea.setText(sb.length() > 0 ? sb.toString() : "No course requests found.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            statusArea.setText("Error fetching course request status.");
        }

        JScrollPane statusScrollPane = new JScrollPane(statusArea);
        statusScrollPane.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(
                this,
                statusScrollPane,
                "Your Course Request Updates",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void updateNotifications(String enrollmentNumber) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT course_code, batch_number, status FROM course_requests " +
                    "WHERE teacher_enrollment_number = ? AND status IN ('Approved', 'Rejected') " +
                    "ORDER BY request_id DESC LIMIT 1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, enrollmentNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String course = rs.getString("course_code");
                String batch = rs.getString("batch_number");
                String status = rs.getString("status");

                String message = String.format(
                        "<html>Your request for <b>%s</b> (Batch <b>%s</b>) was <b>%s</b>.</html>",
                        course, batch, status.toUpperCase()
                );

                Color color = status.equalsIgnoreCase("Approved") ?
                        new Color(0, 150, 0) : new Color(200, 0, 0);

                notificationLabel.setForeground(color);
                notificationLabel.setText(message);
            } else {
                notificationLabel.setForeground(new Color(50, 50, 50));
                notificationLabel.setText("No recent updates on your course requests.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            notificationLabel.setForeground(Color.RED);
            notificationLabel.setText("Error retrieving notifications.");
        }
    }

    private void loadCourses() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT course_code, course_title FROM courses ORDER BY course_code"
            );
            ResultSet rs = ps.executeQuery();

            tableModel.setRowCount(0); // Clear existing data
            while (rs.next()) {
                String code = rs.getString("course_code");
                String title = rs.getString("course_title");
                tableModel.addRow(new Object[]{code, title, ""});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showMessage("Error loading courses.", Color.RED);
        }
    }

    private JButton createBackButton() {
        JButton backButton = new JButton();
        backButton.setForeground(new Color(10, 25, 74));
        backButton.setBackground(new Color(255, 140, 0));
        backButton.setLayout(new BorderLayout(5, 0));
        backButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        ImageIcon arrowIcon = new ImageIcon("D:\\testing\\left-arrow.png");
        JLabel arrowLabel = new JLabel(new ImageIcon(arrowIcon.getImage().getScaledInstance(25, 20, Image.SCALE_SMOOTH)));

        JLabel backText = new JLabel("BACK TO DASHBOARD");
        backText.setFont(loadMontserratFont().deriveFont(12f));
        backText.setForeground(new Color(10, 25, 74));

        JPanel content = new JPanel(new BorderLayout(10, 0));
        content.setOpaque(false);
        content.add(arrowLabel, BorderLayout.WEST);
        content.add(backText, BorderLayout.CENTER);

        backButton.add(content, BorderLayout.CENTER);
        return backButton;
    }

    private void styleButton(JButton button, float fontSize) {
        button.setBackground(new Color(255, 140, 0));
        button.setForeground(new Color(10, 25, 74));
        button.setFont(loadMontserratFont().deriveFont(fontSize));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 100, 0)), // Added missing parenthesis
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
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
        new TeacherCourseRequest("T24CSEU1156");
    }
}