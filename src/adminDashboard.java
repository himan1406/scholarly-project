import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class adminDashboard extends JFrame {
    private String enrollmentNumber;  // To store the enrollment number

    // Constructor that accepts the enrollment number
    public adminDashboard(String enrollmentNumber) {
        this.enrollmentNumber = enrollmentNumber;

        // Frame settings
        setTitle("Admin Dashboard");
        setSize(935, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // Main panel with navy blue background
        JPanel panel = new JPanel();
        panel.setBackground(new Color(10, 25, 74));
        panel.setLayout(null);
        panel.setBounds(0, 0, 935, 640);

        // Orange header panel
        JPanel orangePanel = new JPanel();
        orangePanel.setBackground(new Color(255, 140, 0));
        orangePanel.setBounds(0, 0, 935, 70);
        orangePanel.setLayout(null);

        Font montserratBold = loadMontserratBoldFont();

        // Header
        JLabel header = new JLabel("ADMIN DASHBOARD", JLabel.LEFT);
        header.setBounds(20, 10, 400, 50);
        header.setForeground(new Color(10, 25, 74));
        header.setFont(montserratBold.deriveFont(Font.BOLD, 32f));

        // Logo
        ImageIcon logoIcon = new ImageIcon("D:\\testing\\new_logo.png");
        JLabel logo = new JLabel(logoIcon);
        logo.setBounds(825, 9, 80, 55);

        // Add components to the orange header panel
        orangePanel.add(header);
        orangePanel.add(logo);

        // Buttons
        JButton databaseButton = createDashboardButton("DATABASE", "D:\\testing\\file-protection.png", 25);
        JButton courseButton = createDashboardButton("COURSE MANAGEMENT", "D:\\testing\\school.png", 475);

        // Add ActionListener to Profile Button
        databaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AdminDatabase.main(new String[]{}); // Pass the enrollment number
                dispose();   // Close Student Dashboard
            }
        });

        courseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AdminCourseRequest("admin");
                dispose();   // Close Student Dashboard
            }
        });

        // Add buttons to panel
        panel.add(databaseButton);
        panel.add(courseButton);

        // Add the panels to the frame
        add(orangePanel);
        add(panel);

        // Make the frame visible
        setVisible(true);
    }

    // Helper method to create buttons
    private JButton createDashboardButton(String text, String iconPath, int x) {
        JButton button = new JButton(text);
        button.setBounds(x, 90, 420, 490);
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
        new adminDashboard("S24CSEUXXXX"); // Pass the enrollment number here
    }
}
