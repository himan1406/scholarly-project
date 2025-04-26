import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class StudentAttendancePage extends JFrame {
    private String enrollmentNumber;
    private Connection conn;
    private Font montserratFont;

    public StudentAttendancePage(String enrollmentNumber) {
        this.enrollmentNumber = enrollmentNumber;
        conn = DatabaseConnection.getConnection();
        montserratFont = loadMontserratBoldFont();
        initUI();
    }

    private void initUI() {
        setTitle("Student Attendance");
        setSize(900, 750); // Reduced height to better fit everything
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Orange Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(255, 102, 0));
        topPanel.setPreferredSize(new Dimension(900, 70));

        JLabel titleLabel = new JLabel("ATTENDANCE");
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

        // Blue Background Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(0, 0, 102));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        add(mainPanel, BorderLayout.CENTER);

        // White Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(new Color(230, 230, 230));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(800, 610)); // Leaves room for header
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrollPane.getViewport().setBackground(new Color(230, 230, 230));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(scrollPane);

        // Attendance Data
        Map<String, int[]> courseAttendance = fetchAttendance();
        if (courseAttendance.isEmpty()) {
            courseAttendance.put("CSE101", new int[]{0, 10});
            courseAttendance.put("CSE102", new int[]{0, 8});
        }

        // Bar Chart
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
        int totalPresent = 0, totalClasses = 0;

        for (String course : courseAttendance.keySet()) {
            int present = courseAttendance.get(course)[0];
            int total = courseAttendance.get(course)[1];
            double percentage = (total == 0) ? 0 : (present * 100.0 / total);
            barDataset.addValue(percentage, "Attendance %", course);
            totalPresent += present;
            totalClasses += total;
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Course-wise Attendance", "Course", "Attendance (%)",
                barDataset, PlotOrientation.VERTICAL, false, true, false
        );

        CategoryPlot barPlot = barChart.getCategoryPlot();
        barPlot.setBackgroundPaint(Color.white);
        BarRenderer barRenderer = (BarRenderer) barPlot.getRenderer();
        barRenderer.setSeriesPaint(0, new Color(255, 102, 0));
        barRenderer.setBarPainter(new StandardBarPainter());

        ChartPanel barChartPanel = new ChartPanel(barChart);
        barChartPanel.setPreferredSize(new Dimension(700, 220)); // Reduced height
        barChartPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(barChartPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Pie Chart
        int absent = totalClasses - totalPresent;
        DefaultPieDataset pieDataset = new DefaultPieDataset();
        pieDataset.setValue("Present", totalPresent);
        pieDataset.setValue("Absent", absent);

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Overall Attendance", pieDataset, false, true, false
        );

        PiePlot piePlot = (PiePlot) pieChart.getPlot();
        piePlot.setSectionPaint("Present", new Color(204, 255, 0));
        piePlot.setSectionPaint("Absent", new Color(51, 204, 255));
        piePlot.setBackgroundPaint(Color.white);
        piePlot.setOutlineVisible(false);

        ChartPanel pieChartPanel = new ChartPanel(pieChart);
        pieChartPanel.setPreferredSize(new Dimension(400, 200)); // Reduced height
        pieChartPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(pieChartPanel);
        contentPanel.add(Box.createVerticalStrut(25));

        // Back Button
        JButton backButton = new JButton("BACK TO DASHBOARD");
        backButton.setFont(montserratFont.deriveFont(16f));
        backButton.setBackground(new Color(255, 102, 0));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> {
            dispose();
            new studentDashboard(enrollmentNumber);
        });

        contentPanel.add(backButton);
        contentPanel.add(Box.createVerticalStrut(10));
    }

    private Map<String, int[]> fetchAttendance() {
        Map<String, int[]> attendanceData = new HashMap<>();
        try {
            String sql = "SELECT course_code, classes_present, total_classes FROM attendance WHERE enrollment_number = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, enrollmentNumber);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String course = rs.getString("course_code");
                int present = rs.getInt("classes_present");
                int total = rs.getInt("total_classes");
                attendanceData.put(course, new int[]{present, total});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return attendanceData;
    }

    private Font loadMontserratBoldFont() {
        try {
            File fontFile = new File("D:\\testing\\src\\Montserrat-Bold.ttf");
            if (!fontFile.exists()) return new Font("Arial", Font.BOLD, 18);
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            return font.deriveFont(Font.BOLD, 18f);
        } catch (IOException | FontFormatException e) {
            return new Font("Arial", Font.BOLD, 18);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentAttendancePage("24CSEU1001").setVisible(true));
    }
}
