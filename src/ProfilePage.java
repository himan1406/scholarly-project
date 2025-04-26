import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;

public class ProfilePage extends JFrame {
    private Font montserratBold;
    private String enrollmentNumber;

    private JLabel firstNameLabel, lastNameLabel, emailLabel, phoneNumberLabel, enrollmentLabel;
    private JLabel passwordLabel, bloodGroupLabel, genderLabel, fatherNameLabel, cityLabel, stateLabel;

    public ProfilePage(String enrollmentNumber) {
        this.enrollmentNumber = enrollmentNumber;
        montserratBold = loadMontserratBoldFont();

        setTitle("User Profile");
        setSize(600, 690);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(10, 25, 49));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(243, 144, 41));
        headerPanel.setBounds(0, 0, 600, 60);
        headerPanel.setLayout(null);
        add(headerPanel);

        JLabel titleLabel = new JLabel("USER PROFILE");
        titleLabel.setFont(montserratBold.deriveFont(28f));
        titleLabel.setForeground(new Color(10, 25, 74));
        titleLabel.setBounds(20, 14, 300, 30);
        headerPanel.add(titleLabel);

        JPanel profilePanel = new JPanel();
        profilePanel.setBackground(new Color(200, 200, 200));
        profilePanel.setBounds(30, 80, 530, 450);
        profilePanel.setLayout(null);
        add(profilePanel);

        firstNameLabel = createLabel("FIRST NAME:", 20, 15, 400);
        lastNameLabel = createLabel("LAST NAME:", 280, 15, 400);
        emailLabel = createLabel("EMAIL:", 20, 70, 450);
        phoneNumberLabel = createLabel("PHONE NUMBER:", 20, 125, 450);
        enrollmentLabel = createLabel("ENROLLMENT NUMBER:", 20, 180, 450);
        passwordLabel = createLabel("PASSWORD:", 20, 235, 450);
        bloodGroupLabel = createLabel("BLOOD GROUP:", 20, 290, 400);
        genderLabel = createLabel("GENDER:", 280, 290, 400);
        fatherNameLabel = createLabel("FATHER'S NAME:", 20, 345, 450);
        cityLabel = createLabel("CITY:", 20, 400, 400);
        stateLabel = createLabel("STATE:", 280, 400, 400);

        profilePanel.add(firstNameLabel);
        profilePanel.add(lastNameLabel);
        profilePanel.add(emailLabel);
        profilePanel.add(phoneNumberLabel);
        profilePanel.add(enrollmentLabel);
        profilePanel.add(passwordLabel);
        profilePanel.add(bloodGroupLabel);
        profilePanel.add(genderLabel);
        profilePanel.add(fatherNameLabel);
        profilePanel.add(cityLabel);
        profilePanel.add(stateLabel);

        fetchUserDataAndDisplay();

        JButton editButton = new JButton("EDIT PROFILE");
        editButton.setFont(montserratBold.deriveFont(22f));
        editButton.setBackground(new Color(243, 144, 41));
        editButton.setForeground(Color.BLACK);
        editButton.setBounds(360, 560, 200, 50);
        editButton.addActionListener(e -> showEditOptions());
        add(editButton);

        JButton backButton = new JButton();
        backButton.setBackground(new Color(243, 144, 41));
        backButton.setBounds(30, 560, 200, 50);
        backButton.setLayout(null);

        ImageIcon arrowIcon = new ImageIcon("D:\\testing\\left-arrow.png");
        JLabel arrowLabel = new JLabel(new ImageIcon(arrowIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
        arrowLabel.setBounds(10, 10, 30, 30);
        backButton.add(arrowLabel);

        JLabel backText = new JLabel("BACK TO", SwingConstants.RIGHT);
        backText.setFont(montserratBold.deriveFont(18f));
        backText.setForeground(Color.BLACK);
        backText.setBounds(-10, 5, 180, 20);
        backButton.add(backText);

        JLabel dashboardText = new JLabel("DASHBOARD", SwingConstants.RIGHT);
        dashboardText.setFont(montserratBold.deriveFont(18f));
        dashboardText.setForeground(Color.BLACK);
        dashboardText.setBounds(10, 24, 180, 20);
        backButton.add(dashboardText);

        backButton.addActionListener(e -> {
            dispose();
            new studentDashboard(enrollmentNumber);
        });
        add(backButton);

        setVisible(true);
    }

    private void showEditOptions() {
        String[] options = {
                "First Name",
                "Last Name",
                "Email",
                "Phone Number",
                "Password",
                "Blood Group",
                "Gender",
                "Father's Name",
                "City",
                "State"
        };

        String selectedOption = (String) JOptionPane.showInputDialog(
                this,
                "Select field to edit:",
                "Edit Profile",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (selectedOption != null) {
            editField(selectedOption);
        }
    }

    private void editField(String field) {
        String currentValue = "";
        String newValue = "";
        String columnName = "";

        switch (field) {
            case "First Name":
                currentValue = firstNameLabel.getText().replace("FIRST NAME: ", "");
                columnName = "first_name";
                break;
            case "Last Name":
                currentValue = lastNameLabel.getText().replace("LAST NAME: ", "");
                columnName = "last_name";
                break;
            case "Email":
                currentValue = emailLabel.getText().replace("EMAIL: ", "");
                columnName = "email";
                break;
            case "Phone Number":
                currentValue = phoneNumberLabel.getText().replace("PHONE NUMBER: ", "");
                columnName = "phone_number";
                break;
            case "Password":
                currentValue = "";
                columnName = "password";
                newValue = JOptionPane.showInputDialog(this, "Enter new Password:", "");
                break;
            case "Blood Group":
                currentValue = bloodGroupLabel.getText().replace("BLOOD GROUP: ", "");
                columnName = "blood_group";
                break;
            case "Gender":
                currentValue = genderLabel.getText().replace("GENDER: ", "");
                columnName = "gender";
                break;
            case "Father's Name":
                currentValue = fatherNameLabel.getText().replace("FATHER'S NAME: ", "");
                columnName = "father_name";
                break;
            case "City":
                currentValue = cityLabel.getText().replace("CITY: ", "");
                columnName = "city";
                break;
            case "State":
                currentValue = stateLabel.getText().replace("STATE: ", "");
                columnName = "state";
                break;
        }

        if (!field.equals("Password")) {
            newValue = JOptionPane.showInputDialog(this, "Enter new " + field + ":", currentValue);
        }

        if (newValue != null && !newValue.isEmpty()) {
            updateDatabase(columnName, newValue, field);
        }
    }

    private void updateDatabase(String columnName, String newValue, String fieldName) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "UPDATE user_info SET " + columnName + " = ? WHERE enrollment_number = ?")) {

            preparedStatement.setString(1, newValue);
            preparedStatement.setString(2, enrollmentNumber);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, fieldName + " updated successfully!");
                fetchUserDataAndDisplay();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update " + fieldName, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating " + fieldName + ": " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private JLabel createLabel(String text, int x, int y, int width) {
        JLabel label = new JLabel(text);
        label.setFont(montserratBold.deriveFont(16f));
        label.setForeground(Color.BLACK);
        label.setBounds(x, y, width, 30);
        return label;
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

    private void fetchUserDataAndDisplay() {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM user_info WHERE enrollment_number = ?")) {
            preparedStatement.setString(1, enrollmentNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                firstNameLabel.setText("FIRST NAME: " + resultSet.getString("first_name"));
                lastNameLabel.setText("LAST NAME: " + resultSet.getString("last_name"));
                emailLabel.setText("EMAIL: " + resultSet.getString("email"));
                phoneNumberLabel.setText("PHONE NUMBER: " + resultSet.getString("phone_number"));
                enrollmentLabel.setText("ENROLLMENT NUMBER: " + resultSet.getString("enrollment_number"));
                passwordLabel.setText("PASSWORD: *********");
                bloodGroupLabel.setText("BLOOD GROUP: " + resultSet.getString("blood_group"));
                genderLabel.setText("GENDER: " + resultSet.getString("gender"));
                fatherNameLabel.setText("FATHER'S NAME: " + resultSet.getString("father_name"));
                cityLabel.setText("CITY: " + resultSet.getString("city"));
                stateLabel.setText("STATE: " + resultSet.getString("state"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ProfilePage("S24CSEUXXXX");
    }
}