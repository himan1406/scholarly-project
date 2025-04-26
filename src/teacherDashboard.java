import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class teacherDashboard extends JFrame {
    private String enrollmentNumber;  // To store the enrollment number

    // Constructor that accepts the enrollment number
    public teacherDashboard(String enrollmentNumber) {
        this.enrollmentNumber = enrollmentNumber;

        // Frame settings
        setTitle("Teacher Dashboard");
        setSize(1100, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // Main panel with navy blue background
        JPanel panel = new JPanel();
        panel.setBackground(new Color(10, 25, 74));
        panel.setLayout(null);
        panel.setBounds(0, 0, 1100, 700);

        // Orange header panel
        JPanel orangePanel = new JPanel();
        orangePanel.setBackground(new Color(255, 140, 0));
        orangePanel.setBounds(0, 0, 1100, 100);
        orangePanel.setLayout(null);

        Font montserratBold = loadMontserratBoldFont();

        // Header
        JLabel header = new JLabel("TEACHER DASHBOARD", JLabel.LEFT);
        header.setBounds(20, 5, 400, 50);
        header.setForeground(new Color(10, 25, 74));
        header.setFont(montserratBold.deriveFont(Font.BOLD, 30f));

        // Logo
        ImageIcon logoIcon = new ImageIcon("D:\\testing\\new_logo.png");
        JLabel logo = new JLabel(logoIcon);
        logo.setBounds(990, 4, 80, 55);

        // Enrollment ID label
        ImageIcon idIcon = new ImageIcon(new ImageIcon("D:\\testing\\id_logo.png").getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        JLabel idLabel = new JLabel(idIcon);
        idLabel.setBounds(20, 57, 30, 30);

        JLabel enrollmentLabel = new JLabel(enrollmentNumber);
        enrollmentLabel.setBounds(60, 57, 200, 30);
        enrollmentLabel.setForeground(new Color(10, 25, 74));
        enrollmentLabel.setFont(montserratBold.deriveFont(Font.BOLD, 20f));

        // Add components to the orange header panel
        orangePanel.add(header);
        orangePanel.add(logo);
        orangePanel.add(idLabel);
        orangePanel.add(enrollmentLabel);

        // Buttons
        JButton profileButton = createDashboardButton("PROFILE", "D:\\testing\\user3.png", 25);
        JButton attendanceButton = createDashboardButton("ATTENDANCE", "D:\\testing\\attendance2.png", 290);
        JButton resultsButton = createDashboardButton("RESULTS", "D:\\testing\\exam.png", 555);
        JButton coursesButton = createDashboardButton("COURSES", "D:\\testing\\learning.png", 820);

        // Add ActionListener to Profile Button
        profileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new teacherProfilePage(enrollmentNumber); // Pass the enrollment number
                dispose();   // Close Student Dashboard
            }
        });

        resultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new teacherResults(enrollmentNumber); // Pass the enrollment number
                dispose();   // Close Student Dashboard
            }
        });

        coursesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TeacherCourseRequest(enrollmentNumber); // Pass the enrollment number
                dispose();   // Close Student Dashboard
            }
        });

        // Add buttons to panel
        panel.add(profileButton);
        panel.add(attendanceButton);
        panel.add(resultsButton);
        panel.add(coursesButton);

        // Add the panels to the frame
        add(orangePanel);
        add(panel);

        // Make the frame visible
        setVisible(true);
    }

    // Helper method to create buttons
    private JButton createDashboardButton(String text, String iconPath, int x) {
        JButton button = new JButton(text);
        button.setBounds(x, 120, 240, 461);
        button.setBackground(Color.WHITE);
        button.setFont(loadMontserratBoldFont().deriveFont(Font.BOLD, 22f));
        button.setForeground(new Color(10, 25, 74));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setIcon(new ImageIcon(iconPath));
        return button;
    }

    // Helper method to load Montserrat Bold Font
    private Font loadMontserratBoldFont() {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, new File("src/Montserrat-Bold.ttf")).deriveFont(18f);
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("Arial", Font.BOLD, 18);
        }
    }

    public static void main(String[] args) {
        new teacherDashboard("T24CSEUXXXX"); // Pass the enrollment number here
    }
}
