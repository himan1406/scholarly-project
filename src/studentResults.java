import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class studentResults extends JFrame {
    private String enrollmentNumber;
    private Connection connection;

    public studentResults(String enrollmentNumber) {
        this.enrollmentNumber = enrollmentNumber;

        this.connection = DatabaseConnection.getConnection();
        if (connection == null) {
            JOptionPane.showMessageDialog(this, "Failed to establish database connection", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setTitle("Student Results");
        setSize(935, 640);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(10, 25, 74));
        panel.setLayout(null);
        panel.setBounds(0, 0, 935, 640);

        JPanel orangePanel = new JPanel();
        orangePanel.setBackground(new Color(255, 140, 0));
        orangePanel.setBounds(0, 0, 935, 100);
        orangePanel.setLayout(null);

        Font montserratBold = loadMontserratBoldFont();

        JLabel header = new JLabel("RESULTS", JLabel.LEFT);
        header.setBounds(20, 5, 400, 50);
        header.setForeground(new Color(10, 25, 74));
        header.setFont(montserratBold.deriveFont(Font.BOLD, 30f));

        ImageIcon logoIcon = loadImageIcon("logo.png", "Logo", 80, 55);
        JLabel logo = new JLabel(logoIcon);
        logo.setBounds(825, 4, 80, 55);

        ImageIcon idIcon = loadImageIcon("id_logo.png", "ID", 30, 30);
        JLabel idLabel = new JLabel(idIcon);
        idLabel.setBounds(20, 57, 30, 30);

        JLabel enrollmentLabel = new JLabel(enrollmentNumber);
        enrollmentLabel.setBounds(60, 57, 200, 30);
        enrollmentLabel.setForeground(new Color(10, 25, 74));
        enrollmentLabel.setFont(montserratBold.deriveFont(Font.BOLD, 20f));

        JButton backButton = createBackButton(montserratBold);
        orangePanel.add(backButton);
        orangePanel.add(header);
        orangePanel.add(logo);
        orangePanel.add(idLabel);
        orangePanel.add(enrollmentLabel);

        JPanel resultsPanel = createResultsPanel(montserratBold);
        panel.add(resultsPanel);

        add(orangePanel);
        add(panel);

        setVisible(true);
    }

    private JButton createBackButton(Font montserratBold) {
        JButton backButton = new JButton();
        backButton.setBounds(680, 53, 220, 40);
        backButton.setBackground(new Color(10, 25, 74));
        backButton.setBorder(BorderFactory.createEmptyBorder());
        backButton.setLayout(new BorderLayout(10, 0));

        ImageIcon arrowIcon = loadImageIcon("left-arrow.png", "Back", 20, 20);
        if (arrowIcon != null) {
            arrowIcon = makeIconWhite(arrowIcon);
            JLabel arrowLabel = new JLabel(arrowIcon);
            arrowLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            backButton.add(arrowLabel, BorderLayout.WEST);
        }

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 5, 10));

        JLabel backText = new JLabel("BACK TO DASHBOARD");
        backText.setFont(montserratBold.deriveFont(Font.BOLD, 14f));
        backText.setForeground(Color.WHITE);
        backText.setAlignmentX(Component.CENTER_ALIGNMENT);

        textPanel.add(backText);
        textPanel.add(Box.createVerticalStrut(2));

        backButton.add(textPanel, BorderLayout.CENTER);

        backButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                backButton.setBackground(new Color(20, 40, 90));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                backButton.setBackground(new Color(10, 25, 74));
            }
        });

        backButton.addActionListener(e -> {
            new studentDashboard(enrollmentNumber);
            dispose();
        });

        return backButton;
    }

    private JPanel createResultsPanel(Font montserratBold) {
        JPanel resultsPanel = new JPanel();
        resultsPanel.setBounds(15, 120, 895, 480);
        resultsPanel.setBackground(Color.WHITE);
        resultsPanel.setLayout(new BorderLayout());

        String[] columnNames = {"Course Code", "Course Title", "Batch", "Marks", "Grade"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable resultsTable = new JTable(model);
        resultsTable.setFont(montserratBold.deriveFont(Font.PLAIN, 16f));
        resultsTable.setRowHeight(30);
        resultsTable.getTableHeader().setFont(montserratBold.deriveFont(Font.BOLD, 18f));
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        resultsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 4) {
                    String grade = (String) value;
                    switch (grade) {
                        case "A+" -> c.setBackground(new Color(0, 100, 0));
                        case "A" -> c.setBackground(new Color(34, 139, 34));
                        case "B" -> c.setBackground(new Color(154, 205, 50));
                        case "C" -> c.setBackground(new Color(255, 215, 0));
                        case "D" -> c.setBackground(new Color(255, 140, 0));
                        case "F" -> c.setBackground(new Color(178, 34, 34));
                        default -> c.setBackground(Color.WHITE);
                    }
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        });

        List<CourseResult> results = fetchStudentResults();
        for (CourseResult result : results) {
            model.addRow(new Object[]{
                    result.getCourseCode(),
                    result.getCourseTitle(),
                    result.getBatchNumber(),
                    result.getMarks(),
                    calculateGrade(result.getMarks())
            });
        }

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        resultsPanel.add(scrollPane, BorderLayout.CENTER);

        JLabel summaryLabel = new JLabel("Academic Summary for " + enrollmentNumber, JLabel.CENTER);
        summaryLabel.setFont(montserratBold.deriveFont(Font.BOLD, 20f));
        summaryLabel.setForeground(new Color(10, 25, 74));
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        resultsPanel.add(summaryLabel, BorderLayout.NORTH);

        return resultsPanel;
    }

    private List<CourseResult> fetchStudentResults() {
        List<CourseResult> results = new ArrayList<>();
        String query = "SELECT r.course_code, c.course_title, u.batch_number, r.marks " +
                "FROM results r " +
                "JOIN courses c ON r.course_code = c.course_code " +
                "JOIN user_info u ON r.enrollment_number = u.enrollment_number " +
                "WHERE r.enrollment_number = ? " +
                "ORDER BY u.batch_number, r.course_code";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, enrollmentNumber);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(new CourseResult(
                        rs.getString("course_code"),
                        rs.getString("course_title"),
                        rs.getInt("batch_number"),
                        rs.getInt("marks")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching results: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        return results;
    }

    private String calculateGrade(int marks) {
        if (marks >= 90) return "A+";
        if (marks >= 80) return "A";
        if (marks >= 70) return "B";
        if (marks >= 60) return "C";
        if (marks >= 50) return "D";
        return "F";
    }

    private Font loadMontserratBoldFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/Montserrat-Bold.ttf");
            if (is != null) return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(18f);
            File fontFile = new File("Montserrat-Bold.ttf");
            if (fontFile.exists()) return Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(18f);
        } catch (Exception e) {
            System.err.println("Error loading font: " + e.getMessage());
        }
        return new Font("SansSerif", Font.BOLD, 18);
    }

    private ImageIcon loadImageIcon(String path, String description, int width, int height) {
        try {
            InputStream is = getClass().getResourceAsStream("/" + path);
            if (is != null) {
                Image image = ImageIO.read(is);
                return new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
            }
            File file = new File(path);
            if (file.exists()) {
                Image image = ImageIO.read(file);
                return new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + path);
        }
        return new ImageIcon();
    }

    private ImageIcon makeIconWhite(ImageIcon icon) {
        BufferedImage img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgba = img.getRGB(x, y);
                if ((rgba >> 24) != 0x00) {
                    img.setRGB(x, y, 0xFFFFFFFF);
                }
            }
        }

        return new ImageIcon(img.getScaledInstance(icon.getIconWidth(), icon.getIconHeight(), Image.SCALE_SMOOTH));
    }

    private static class CourseResult {
        private String courseCode;
        private String courseTitle;
        private int batchNumber;
        private int marks;

        public CourseResult(String courseCode, String courseTitle, int batchNumber, int marks) {
            this.courseCode = courseCode;
            this.courseTitle = courseTitle;
            this.batchNumber = batchNumber;
            this.marks = marks;
        }

        public String getCourseCode() { return courseCode; }
        public String getCourseTitle() { return courseTitle; }
        public int getBatchNumber() { return batchNumber; }
        public int getMarks() { return marks; }
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

    public static void main(String[] args) {
        new studentResults("S24CSEUXXXX");
    }
}
