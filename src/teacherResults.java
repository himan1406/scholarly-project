import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class teacherResults extends JFrame {
    private String teacherEnrollmentNumber;
    private Connection connection;
    private JComboBox<String> courseComboBox;
    private JComboBox<Integer> batchComboBox;
    private JTextField enrollmentField;
    private JTable resultsTable;

    public teacherResults(String teacherEnrollmentNumber) {
        this.teacherEnrollmentNumber = teacherEnrollmentNumber;
        this.connection = DatabaseConnection.getConnection();

        // Frame settings (same as studentResults)
        setTitle("Teacher Results Management");
        setSize(935, 710);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // Main panel with navy blue background
        JPanel panel = new JPanel();
        panel.setBackground(new Color(10, 25, 74));
        panel.setLayout(null);
        panel.setBounds(0, 0, 935, 720);

        // Orange header panel (same as studentResults)
        JPanel orangePanel = new JPanel();
        orangePanel.setBackground(new Color(255, 140, 0));
        orangePanel.setBounds(0, 0, 935, 100);
        orangePanel.setLayout(null);

        Font montserratBold = loadMontserratBoldFont();

        // Header (modified for teacher)
        JLabel header = new JLabel("RESULTS", JLabel.LEFT);
        header.setBounds(20, 5, 600, 50);
        header.setForeground(new Color(10, 25, 74));
        header.setFont(montserratBold.deriveFont(Font.BOLD, 30f));

        // Logo (same as studentResults)
        ImageIcon logoIcon = new ImageIcon("D:\\testing\\logo.png");
        JLabel logo = new JLabel(logoIcon);
        logo.setBounds(825, 4, 80, 55);

        // Teacher ID label (using enrollment number)
        ImageIcon idIcon = new ImageIcon(new ImageIcon("D:\\testing\\id_logo.png").getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        JLabel idLabel = new JLabel(idIcon);
        idLabel.setBounds(20, 57, 30, 30);

        JLabel enrollmentLabel = new JLabel(teacherEnrollmentNumber);
        enrollmentLabel.setBounds(60, 57, 200, 30);
        enrollmentLabel.setForeground(new Color(10, 25, 74));
        enrollmentLabel.setFont(montserratBold.deriveFont(Font.BOLD, 20f));

        // Back button (same style as studentResults)
        JButton backButton = createBackButton(montserratBold);
        orangePanel.add(backButton);

        // Add components to header
        orangePanel.add(header);
        orangePanel.add(logo);
        orangePanel.add(idLabel);
        orangePanel.add(enrollmentLabel);

        // Course and batch selection panel
        JPanel selectionPanel = new JPanel();
        selectionPanel.setBounds(15, 120, 895, 85);
        selectionPanel.setBackground(Color.WHITE);
        selectionPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));

        courseComboBox = new JComboBox<>();
        populateCourseComboBox();
        courseComboBox.addActionListener(e -> populateBatchComboBox());

        batchComboBox = new JComboBox<>();
        batchComboBox.addActionListener(e -> loadResults());

        enrollmentField = new JTextField(15);

        JButton loadButton = new JButton("Load Results");
        loadButton.addActionListener(e -> loadResults());
        styleButton(loadButton, montserratBold);

        JButton searchButton = new JButton("Search Student");
        searchButton.addActionListener(e -> searchStudent());
        styleButton(searchButton, montserratBold);

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updateResults());
        styleButton(updateButton, montserratBold);

        JButton addButton = new JButton("Add Results");
        addButton.addActionListener(e -> addResults());
        styleButton(addButton, montserratBold);

        selectionPanel.add(new JLabel("Course:"));
        selectionPanel.add(courseComboBox);
        selectionPanel.add(new JLabel("Batch:"));
        selectionPanel.add(batchComboBox);
        selectionPanel.add(new JLabel("Enrollment:"));
        selectionPanel.add(enrollmentField);
        selectionPanel.add(loadButton);
        selectionPanel.add(searchButton);
        selectionPanel.add(updateButton);
        selectionPanel.add(addButton);

        // Results table panel
        JPanel resultsPanel = new JPanel();
        resultsPanel.setBounds(15, 220, 895, 430);
        resultsPanel.setBackground(Color.WHITE);
        resultsPanel.setLayout(new BorderLayout());

        String[] columnNames = {"Enrollment", "Student Name", "Course Code", "Batch", "Marks", "Grade"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only marks column is editable
            }
        };

        resultsTable = new JTable(model);
        resultsTable.setFont(montserratBold.deriveFont(Font.PLAIN, 16f));
        resultsTable.setRowHeight(30);
        resultsTable.getTableHeader().setFont(montserratBold.deriveFont(Font.BOLD, 18f));
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Grade coloring (same as studentResults)
        resultsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                if (column == 5) { // Grade column
                    String grade = (String)value;
                    switch (grade) {
                        case "A+": c.setBackground(new Color(0, 100, 0)); break;
                        case "A": c.setBackground(new Color(34, 139, 34)); break;
                        case "B": c.setBackground(new Color(154, 205, 50)); break;
                        case "C": c.setBackground(new Color(255, 215, 0)); break;
                        case "D": c.setBackground(new Color(255, 140, 0)); break;
                        case "F": c.setBackground(new Color(178, 34, 34)); break;
                        default: c.setBackground(Color.WHITE);
                    }
                    c.setForeground(column == 5 ? Color.WHITE : Color.BLACK);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        resultsPanel.add(scrollPane, BorderLayout.CENTER);

        // Add panels to main panel
        panel.add(selectionPanel);
        panel.add(resultsPanel);

        // Add to frame
        add(orangePanel);
        add(panel);

        setVisible(true);
    }

    private JButton createBackButton(Font montserratBold) {
        JButton backButton = new JButton();
        backButton.setBounds(700, 57, 210, 40);
        backButton.setBackground(new Color(10, 25, 74));
        backButton.setBorder(BorderFactory.createEmptyBorder());
        backButton.setLayout(new BorderLayout(10, 0));

        // White arrow icon
        ImageIcon arrowIcon = new ImageIcon("D:\\testing\\left-arrow.png");
        if (arrowIcon != null) {
            arrowIcon = new ImageIcon(arrowIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
            JLabel arrowLabel = new JLabel(arrowIcon);
            arrowLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            backButton.add(arrowLabel, BorderLayout.WEST);
        }

        // Text panel with vertically stacked labels
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 10));

        JLabel backText = new JLabel("BACK TO DASHBOARD");
        backText.setFont(montserratBold.deriveFont(Font.BOLD, 12f));
        backText.setForeground(Color.WHITE);
        backText.setAlignmentX(Component.CENTER_ALIGNMENT);

        textPanel.add(backText);
        textPanel.add(Box.createVerticalStrut(0));

        backButton.add(textPanel, BorderLayout.CENTER);

        // Hover effects
        backButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                backButton.setBackground(new Color(20, 40, 90));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                backButton.setBackground(new Color(10, 25, 74));
            }
        });

        // Action listener
        backButton.addActionListener(e -> {
            new teacherDashboard(teacherEnrollmentNumber);
            dispose();
        });

        return backButton;
    }

    private void styleButton(JButton button, Font font) {
        button.setBackground(new Color(10, 25, 74));
        button.setForeground(Color.WHITE);
        button.setFont(font.deriveFont(Font.BOLD, 14f));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }

    private Font loadMontserratBoldFont() {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, new File("src/Montserrat-Bold.ttf")).deriveFont(18f);
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("Arial", Font.BOLD, 18);
        }
    }

    // Database methods (same functionality as previous implementation)
    private void populateCourseComboBox() {
        courseComboBox.removeAllItems();
        String query = "SELECT DISTINCT c.course_code, c.course_title " +
                "FROM courses c " +
                "JOIN teacher_courses tc ON c.course_code = tc.course_code " +
                "WHERE tc.teacher_enrollment_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, teacherEnrollmentNumber);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String courseInfo = rs.getString("course_code") + " - " + rs.getString("course_title");
                courseComboBox.addItem(courseInfo);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateBatchComboBox() {
        batchComboBox.removeAllItems();
        String selectedCourse = (String) courseComboBox.getSelectedItem();
        if (selectedCourse == null) return;

        String courseCode = selectedCourse.split(" - ")[0];
        String query = "SELECT DISTINCT batch_number FROM results " +
                "WHERE course_code = ? ORDER BY batch_number";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, courseCode);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                batchComboBox.addItem(rs.getInt("batch_number"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading batches: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadResults() {
        DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();
        model.setRowCount(0);

        String selectedCourse = (String) courseComboBox.getSelectedItem();
        Integer selectedBatch = (Integer) batchComboBox.getSelectedItem();

        if (selectedCourse == null || selectedBatch == null) return;

        String courseCode = selectedCourse.split(" - ")[0];
        String query = "SELECT r.enrollment_number, s.student_name, r.course_code, " +
                "r.batch_number, r.marks " +
                "FROM results r " +
                "JOIN students s ON r.enrollment_number = s.enrollment_number " +
                "WHERE r.course_code = ? AND r.batch_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, courseCode);
            stmt.setInt(2, selectedBatch);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int marks = rs.getInt("marks");
                model.addRow(new Object[]{
                        rs.getString("enrollment_number"),
                        rs.getString("student_name"),
                        rs.getString("course_code"),
                        rs.getInt("batch_number"),
                        marks,
                        calculateGrade(marks)
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading results: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchStudent() {
        String enrollment = enrollmentField.getText().trim();
        if (enrollment.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an enrollment number",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedCourse = (String) courseComboBox.getSelectedItem();
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Please select a course first",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String courseCode = selectedCourse.split(" - ")[0];
        String query = "SELECT r.enrollment_number, s.student_name, r.course_code, " +
                "r.batch_number, r.marks " +
                "FROM results r " +
                "JOIN students s ON r.enrollment_number = s.enrollment_number " +
                "WHERE r.enrollment_number = ? AND r.course_code = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, enrollment);
            stmt.setString(2, courseCode);
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();
            model.setRowCount(0);

            if (rs.next()) {
                int marks = rs.getInt("marks");
                model.addRow(new Object[]{
                        rs.getString("enrollment_number"),
                        rs.getString("student_name"),
                        rs.getString("course_code"),
                        rs.getInt("batch_number"),
                        marks,
                        calculateGrade(marks)
                });
                batchComboBox.setSelectedItem(rs.getInt("batch_number"));
            } else {
                JOptionPane.showMessageDialog(this, "No results found for this student in selected course",
                        "Not Found", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching student: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateResults() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to update",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String enrollment = (String) resultsTable.getValueAt(selectedRow, 0);
            String courseCode = (String) resultsTable.getValueAt(selectedRow, 2);
            int batch = (Integer) resultsTable.getValueAt(selectedRow, 3);
            int marks = Integer.parseInt(resultsTable.getValueAt(selectedRow, 4).toString());

            String query = "UPDATE results SET marks = ? " +
                    "WHERE enrollment_number = ? AND course_code = ? AND batch_number = ?";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, marks);
                stmt.setString(2, enrollment);
                stmt.setString(3, courseCode);
                stmt.setInt(4, batch);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    resultsTable.setValueAt(calculateGrade(marks), selectedRow, 5);
                    JOptionPane.showMessageDialog(this, "Results updated successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid marks",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating results: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addResults() {
        String enrollment = enrollmentField.getText().trim();
        if (enrollment.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an enrollment number",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedCourse = (String) courseComboBox.getSelectedItem();
        Integer selectedBatch = (Integer) batchComboBox.getSelectedItem();

        if (selectedCourse == null || selectedBatch == null) {
            JOptionPane.showMessageDialog(this, "Please select both course and batch",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String courseCode = selectedCourse.split(" - ")[0];
        String marksStr = JOptionPane.showInputDialog(this, "Enter marks for student " + enrollment,
                "Add New Results", JOptionPane.QUESTION_MESSAGE);

        if (marksStr == null || marksStr.trim().isEmpty()) return;

        try {
            int marks = Integer.parseInt(marksStr);
            String query = "INSERT INTO results (enrollment_number, course_code, batch_number, marks) " +
                    "VALUES (?, ?, ?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, enrollment);
                stmt.setString(2, courseCode);
                stmt.setInt(3, selectedBatch);
                stmt.setInt(4, marks);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Results added successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadResults(); // Refresh the table
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid marks",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding results: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String calculateGrade(int marks) {
        if (marks >= 90) return "A+";
        if (marks >= 80) return "A";
        if (marks >= 70) return "B";
        if (marks >= 60) return "C";
        if (marks >= 50) return "D";
        return "F";
    }

    @Override
    public void dispose() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        super.dispose();
    }
}