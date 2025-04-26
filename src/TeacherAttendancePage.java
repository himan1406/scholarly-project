import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.List;

public class TeacherAttendancePage extends JFrame {
    private Connection conn;
    private Font montserratFont;
    private JComboBox<String> batchComboBox, courseComboBox;
    private JTable table;
    private DefaultTableModel tableModel;
    private Map<String, Boolean> attendanceMap = new HashMap<>();
    private String teacherEmail;

    public TeacherAttendancePage(String teacherEmail) {
        this.teacherEmail = teacherEmail;
        conn = DatabaseConnection.getConnection();
        montserratFont = loadMontserratBoldFont();
        initUI();
    }

    private void initUI() {
        setTitle("Teacher Attendance Page");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(255, 102, 0));
        topPanel.setPreferredSize(new Dimension(900, 70));

        JLabel titleLabel = new JLabel("ATTENDANCE MARKING");
        titleLabel.setFont(montserratFont.deriveFont(Font.BOLD, 28f));
        titleLabel.setForeground(new Color(0, 0, 102));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        topPanel.add(titleLabel, BorderLayout.WEST);

        ImageIcon logoIcon = new ImageIcon("D:\\testing\\logo.png");
        Image logoImage = logoIcon.getImage().getScaledInstance(100, 40, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(logoImage));
        logoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 20));
        topPanel.add(logoLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Center Panel
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(new Color(0, 0, 102));
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Batch and Course Selector
        JPanel selectionPanel = new JPanel();
        selectionPanel.setBackground(new Color(0, 0, 102));
        selectionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        batchComboBox = new JComboBox<>(getBatches());
        batchComboBox.setFont(montserratFont);
        courseComboBox = new JComboBox<>();
        courseComboBox.setFont(montserratFont);
        updateCourseOptions(); // initial course list

        batchComboBox.addActionListener(e -> updateCourseOptions());

        JButton loadButton = new JButton("LOAD STUDENTS");
        loadButton.setFont(montserratFont);
        loadButton.setBackground(new Color(255, 102, 0));
        loadButton.setForeground(Color.WHITE);
        loadButton.addActionListener(e -> loadStudents());

        selectionPanel.add(new JLabel("Batch:")).setForeground(Color.WHITE);
        selectionPanel.add(batchComboBox);
        selectionPanel.add(new JLabel("Course:")).setForeground(Color.WHITE);
        selectionPanel.add(courseComboBox);
        selectionPanel.add(loadButton);

        centerPanel.add(selectionPanel);

        // Table
        String[] columnNames = {"Enrollment Number", "Name", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        };

        table = new JTable(tableModel);
        table.setFont(montserratFont.deriveFont(14f));
        table.setRowHeight(30);
        table.getColumnModel().getColumn(2).setCellRenderer(new AttendanceCellRenderer());
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JComboBox<>(new String[]{"Present", "Absent"})));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(scrollPane);

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(0, 0, 102));
        JButton markButton = new JButton("MARK ATTENDANCE");
        markButton.setFont(montserratFont);
        markButton.setBackground(new Color(255, 102, 0));
        markButton.setForeground(Color.WHITE);
        markButton.addActionListener(e -> markAttendance());

        JButton backButton = new JButton("BACK TO DASHBOARD");
        backButton.setFont(montserratFont);
        backButton.setBackground(new Color(255, 102, 0));
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> {
            dispose();
            // Replace with actual navigation
            JOptionPane.showMessageDialog(this, "Returning to Dashboard...");
        });

        buttonPanel.add(markButton);
        buttonPanel.add(backButton);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(buttonPanel);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void updateCourseOptions() {
        courseComboBox.removeAllItems();
        try {
            String sql = "SELECT course_code FROM teacher_courses WHERE teacher_email = ? AND batch = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, teacherEmail);
            ps.setString(2, (String) batchComboBox.getSelectedItem());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                courseComboBox.addItem(rs.getString("course_code"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadStudents() {
        tableModel.setRowCount(0);
        attendanceMap.clear();
        String batch = (String) batchComboBox.getSelectedItem();
        String course = (String) courseComboBox.getSelectedItem();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        try {
            String sql = "SELECT enrollment_number, name FROM user_info WHERE batch = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, batch);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String enroll = rs.getString("enrollment_number");
                String name = rs.getString("name");
                String status = fetchStatus(enroll, course, today);
                tableModel.addRow(new Object[]{enroll, name, status});
                attendanceMap.put(enroll, status.equals("Present"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String fetchStatus(String enrollment, String course, String date) throws SQLException {
        String query = "SELECT status FROM attendance WHERE enrollment_number = ? AND course_code = ? AND date = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, enrollment);
        ps.setString(2, course);
        ps.setString(3, date);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getString("status");
        }
        return "Present"; // default
    }

    private void markAttendance() {
        String course = (String) courseComboBox.getSelectedItem();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        try {
            String insertSQL = "REPLACE INTO attendance (enrollment_number, course_code, date, status) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(insertSQL);

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String enrollment = (String) tableModel.getValueAt(i, 0);
                String status = (String) tableModel.getValueAt(i, 2);
                ps.setString(1, enrollment);
                ps.setString(2, course);
                ps.setString(3, date);
                ps.setString(4, status);
                ps.addBatch();
            }
            ps.executeBatch();
            JOptionPane.showMessageDialog(this, "Attendance Marked Successfully!");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String[] getBatches() {
        List<String> batches = new ArrayList<>();
        try {
            String sql = "SELECT DISTINCT batch FROM user_info ORDER BY batch";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                batches.add(rs.getString("batch"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return batches.toArray(new String[0]);
    }

    private Font loadMontserratBoldFont() {
        try {
            File fontFile = new File("D:\\testing\\src\\Montserrat-Bold.ttf");
            if (!fontFile.exists()) return new Font("Arial", Font.BOLD, 16);
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            return font.deriveFont(Font.BOLD, 16f);
        } catch (Exception e) {
            return new Font("Arial", Font.BOLD, 16);
        }
    }

    private static class AttendanceCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if ("Present".equals(value)) {
                c.setBackground(new Color(144, 238, 144)); // light green
            } else {
                c.setBackground(new Color(255, 99, 71)); // light red
            }
            c.setForeground(Color.BLACK);
            return c;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TeacherAttendancePage("Tteacher@example.com").setVisible(true));
    }
}
