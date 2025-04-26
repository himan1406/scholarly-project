import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;

public class RegisterForm {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Register Page");
        frame.setSize(900, 750);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        JPanel orangePanel = new JPanel();
        orangePanel.setBackground(new Color(255, 140, 0));
        orangePanel.setBounds(0, 0, 900, 100);
        orangePanel.setLayout(null);

        Font montserratBold = loadMontserratBoldFont();

        JLabel header = new JLabel("REGISTER", JLabel.LEFT);
        header.setBounds(20, 10, 300, 50);
        header.setForeground(new Color(10, 25, 74));
        header.setFont(montserratBold.deriveFont(Font.BOLD, 36f));

        ImageIcon logoIcon = new ImageIcon("D:\\testing\\logo.png");
        JLabel logo = new JLabel(logoIcon);
        logo.setBounds(780, 5, 100, 60);

        JToggleButton roleToggle = new JToggleButton("STUDENT");
        roleToggle.setBounds(20, 62, 150, 30);
        roleToggle.setBackground(new Color(10, 25, 74));
        roleToggle.setForeground(new Color(255, 140, 0));
        roleToggle.setFont(montserratBold.deriveFont(18f));

        orangePanel.add(header);
        orangePanel.add(logo);
        orangePanel.add(roleToggle);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(10, 25, 74));
        panel.setBounds(0, 100, 900, 630);
        panel.setLayout(null);

        JLabel loginLabel = new JLabel("Already Have an Account? Login.");
        loginLabel.setBounds(260, 535, 350, 30);
        loginLabel.setForeground(new Color(255, 140, 0));
        loginLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLabel.setFont(montserratBold.deriveFont(20f));

        // Add mouse hover effect
        loginLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginLabel.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginLabel.setForeground(new Color(255, 140, 0));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                Main.main(new String[]{});
                frame.dispose();
            }
        });

        String[] labels = {"FIRST NAME", "LAST NAME", "EMAIL", "PHONE NUMBER", "PASSWORD", "GENDER", "FATHER'S NAME", "BLOOD GROUP", "CITY", "STATE", "ENROLLMENT NUMBER"};
        JTextField[] textFields = new JTextField[labels.length];
        JComponent[] fields = new JComponent[labels.length];
        JLabel[] fieldLabels = new JLabel[labels.length];

        int x = 50, y = 20, width = 350, height = 40;
        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setForeground(Color.WHITE);
            label.setFont(montserratBold.deriveFont(18f));
            label.setBounds(x, y, width, 30);
            panel.add(label);
            fieldLabels[i] = label;

            if (i == 5) {
                fields[i] = new JComboBox<>(new String[]{"Male", "Female", "Rather Not Say"});
            } else if (i == 7) {
                fields[i] = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"});
            } else if (i == 4) {
                fields[i] = new JPasswordField();
            } else {
                JTextField textField = new JTextField("Enter " + labels[i]);
                fields[i] = textField;
                textFields[i] = textField;
                textField.setForeground(Color.GRAY);
                textField.setFont(montserratBold.deriveFont(Font.PLAIN, 18f));
                final String placeholder = textField.getText();

                textField.addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) {
                        if (textField.getText().equals(placeholder)) {
                            textField.setText("");
                            textField.setForeground(Color.BLACK);
                            textField.setFont(montserratBold.deriveFont(Font.BOLD, 18f));
                        }
                    }
                    public void focusLost(FocusEvent e) {
                        if (textField.getText().isEmpty()) {
                            textField.setText(placeholder);
                            textField.setForeground(Color.GRAY);
                            textField.setFont(montserratBold.deriveFont(Font.PLAIN, 18f));
                        }
                    }
                });
            }

            fields[i].setBounds(x, y + 30, width, height);
            panel.add(fields[i]);

            if ((i + 1) % 2 == 0) {
                x = 50;
                y += 80;
            } else {
                x = 480;
            }
        }

        roleToggle.addItemListener(e -> {
            boolean isTeacher = roleToggle.isSelected();
            roleToggle.setText(isTeacher ? "TEACHER" : "STUDENT");
            fieldLabels[6].setVisible(!isTeacher);
            fields[6].setVisible(!isTeacher);
        });

        JButton registerButton = new JButton("REGISTER");
        registerButton.setBounds(550, y + 25, 200, 50);
        registerButton.setBackground(new Color(255, 140, 0));
        registerButton.setForeground(new Color(10, 25, 74));
        registerButton.setFont(montserratBold.deriveFont(Font.BOLD, 22f));
        panel.add(registerButton);

        registerButton.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) {
                    JOptionPane.showMessageDialog(frame, "Database connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                boolean isTeacher = roleToggle.isSelected();
                String query = "INSERT INTO " + (isTeacher ? "teachers_info" : "user_info") + " (first_name, last_name, email, phone_number, password, gender, " + (isTeacher ? "blood_group, city, state, enrollment_number" : "fathers_name, blood_group, city, state, enrollment_number") + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);

                int paramIndex = 1;
                for (int j = 0; j < textFields.length; j++) {
                    if (j == 6 && isTeacher) continue;
                    if (fields[j] instanceof JTextField) {
                        JTextField tf = (JTextField) fields[j];
                        pstmt.setString(paramIndex++, tf.getText().trim());
                    } else if (fields[j] instanceof JComboBox) {
                        pstmt.setString(paramIndex++, ((JComboBox<?>) fields[j]).getSelectedItem().toString());
                    } else if (fields[j] instanceof JPasswordField) {
                        pstmt.setString(paramIndex++, new String(((JPasswordField) fields[j]).getPassword()));
                    }
                }
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Registration successful!");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Registration failed!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.add(orangePanel);
        frame.add(panel);
        frame.setVisible(true);
        panel.add(loginLabel);
    }

    private static Font loadMontserratBoldFont() {
        try {
            File fontFile = new File("D:\\testing\\src\\Montserrat-Bold.ttf");
            if (!fontFile.exists()) {
                throw new Exception("Font file not found: " + fontFile.getAbsolutePath());
            }
            return Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(18f);
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("Arial", Font.BOLD, 18);
        }
    }
}