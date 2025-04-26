import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Login Page");
        frame.setSize(650, 480);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(10, 25, 74));
        panel.setLayout(null);

        JPanel orangePanel = new JPanel();
        orangePanel.setBackground(new Color(255, 140, 0));
        orangePanel.setBounds(0, 0, 650, 70);
        orangePanel.setLayout(null);

        Font montserratBold = loadMontserratBoldFont();

        JLabel header = new JLabel("LOGIN", JLabel.LEFT);
        header.setBounds(20, 10, 300, 50);
        header.setForeground(new Color(10, 25, 74));
        header.setFont(montserratBold.deriveFont(Font.BOLD, 36f));

        ImageIcon logoIcon = new ImageIcon("D:\\testing\\new_logo.png");
        JLabel logo = new JLabel(logoIcon);
        logo.setBounds(530, 5, 100, 60);

        orangePanel.add(header);
        orangePanel.add(logo);

        JLabel emailLabel = new JLabel("EMAIL");
        emailLabel.setForeground(Color.WHITE);
        emailLabel.setBounds(80, 120, 400, 30);
        emailLabel.setFont(montserratBold.deriveFont(22f));

        JTextField emailField = new JTextField("Enter your email");
        emailField.setBounds(80, 155, 480, 40);
        emailField.setForeground(Color.GRAY);
        emailField.setFont(montserratBold.deriveFont(20f));

        emailField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (emailField.getText().equals("Enter your email")) {
                    emailField.setText("");
                    emailField.setForeground(Color.BLACK);
                }
            }

            public void focusLost(FocusEvent e) {
                if (emailField.getText().isEmpty()) {
                    emailField.setText("Enter your email");
                    emailField.setForeground(Color.GRAY);
                }
            }
        });

        JLabel passwordLabel = new JLabel("PASSWORD");
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setBounds(80, 220, 400, 30);
        passwordLabel.setFont(montserratBold.deriveFont(22f));

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(80, 255, 480, 40);
        passwordField.setForeground(Color.BLACK);
        passwordField.setFont(montserratBold.deriveFont(20f));

        JButton loginButton = new JButton("LOGIN");
        loginButton.setBounds(80, 330, 150, 50);
        loginButton.setBackground(new Color(255, 140, 0));
        loginButton.setForeground(new Color(10, 25, 74));
        loginButton.setFont(montserratBold.deriveFont(Font.BOLD, 22f));

        JLabel registerLabel = new JLabel("Don't have an account? Register.");
        registerLabel.setBounds(275, 340, 300, 30);
        registerLabel.setForeground(new Color(255, 140, 0));
        registerLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLabel.setFont(montserratBold.deriveFont(16f));

        // Add mouse hover effect
        registerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                registerLabel.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                registerLabel.setForeground(new Color(255, 140, 0));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                RegisterForm.main(new String[]{});
                frame.dispose();
            }
        });

        loginButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            if (email.equals("admin@bennett.edu.in") && password.equals("adminpass")) {
                JOptionPane.showMessageDialog(frame, "Admin Login successful!");
                new adminDashboard("ADMIN");
                frame.dispose();
                return;
            }

            boolean isTeacher = email.toLowerCase().startsWith("t");
            String tableName = isTeacher ? "teachers_info" : "user_info";

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "SELECT enrollment_number FROM " + tableName + " WHERE email = ? AND password = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, email);
                stmt.setString(2, password);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String enrollmentNumber = rs.getString("enrollment_number");
                    JOptionPane.showMessageDialog(frame, "Login successful!");

                    if (isTeacher) {
                        new teacherDashboard(enrollmentNumber);
                    } else {
                        new studentDashboard(enrollmentNumber);
                    }
                    frame.dispose();
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid email or password.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Database error.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerLabel);

        frame.add(orangePanel);
        frame.add(panel);
        frame.setVisible(true);
    }

    private static Font loadMontserratBoldFont() {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, new File("D:\\testing\\src\\Montserrat-Bold.ttf")).deriveFont(18f);
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("Arial", Font.BOLD, 18);
        }
    }
}