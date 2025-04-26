import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminDatabase {
    JFrame frame;
    DefaultTableModel model;
    JComboBox<String> tableSelector;
    String currentTable = "user_info"; // Default to student info

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDatabase().initialize());
    }

    public void initialize() {
        frame = new JFrame("Admin Dashboard");
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(10, 25, 74));
        panel.setLayout(new BorderLayout());

        JPanel orangePanel = new JPanel();
        orangePanel.setBackground(new Color(255, 140, 0));
        orangePanel.setPreferredSize(new Dimension(900, 70));
        orangePanel.setLayout(null);

        Font montserratBold = loadMontserratBoldFont();

        JLabel header = new JLabel("ADMIN DASHBOARD", JLabel.LEFT);
        header.setBounds(20, 10, 350, 50);
        header.setForeground(new Color(10, 25, 74));
        header.setFont(montserratBold.deriveFont(Font.BOLD, 32f));

        // Add table selector dropdown
        String[] tableOptions = {"Student Info", "Teacher Info", "Results", "Courses", "Teacher Courses"};
        tableSelector = new JComboBox<>(tableOptions);
        tableSelector.setBounds(400, 15, 200, 40);
        tableSelector.setBackground(new Color(10, 25, 74));
        tableSelector.setForeground(Color.WHITE);
        tableSelector.setFont(montserratBold.deriveFont(Font.BOLD, 14f));
        tableSelector.addActionListener(e -> {
            String selected = (String) tableSelector.getSelectedItem();
            switch (selected) {
                case "Student Info": currentTable = "user_info"; break;
                case "Teacher Info": currentTable = "teachers_info"; break;
                case "Results": currentTable = "results"; break;
                case "Courses": currentTable = "courses"; break;
                case "Teacher Courses": currentTable = "teacher_courses"; break;
            }
            refreshTableData();
        });
        orangePanel.add(tableSelector);

        ImageIcon logoIcon = new ImageIcon("D:\\testing\\new_logo.png");
        JLabel logo = new JLabel(logoIcon);
        logo.setBounds(780, 5, 100, 60);

        orangePanel.add(header);
        orangePanel.add(logo);

        // Initialize with empty model
        model = new DefaultTableModel();
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultEditor(Object.class, null);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setBackground(new Color(10, 25, 74));
        tableHeader.setForeground(new Color(255, 140, 0));
        tableHeader.setFont(montserratBold.deriveFont(10f));

        refreshTableData(); // Load initial data

        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        buttonPanel.setBackground(new Color(10, 25, 74));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton refreshButton = new JButton("REFRESH");
        JButton filterButton = new JButton("FILTER");
        JButton editButton = new JButton("EDIT");
        JButton deleteButton = new JButton("DELETE");

        // Style all buttons consistently
        for (JButton button : new JButton[]{refreshButton, filterButton, editButton, deleteButton}) {
            button.setBackground(new Color(255, 140, 0));
            button.setForeground(new Color(10, 25, 74));
            button.setFont(montserratBold.deriveFont(Font.BOLD, 18f));
        }

        // Refresh button action
        refreshButton.addActionListener(e -> refreshTableData());

        // Filter button action
        filterButton.addActionListener(e -> showFilterDialog());

        // Edit button action
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            int selectedColumn = table.getSelectedColumn();
            if (selectedRow != -1 && selectedColumn != -1) {
                String currentValue = (String) model.getValueAt(selectedRow, selectedColumn);
                String columnName = model.getColumnName(selectedColumn);

                if (columnName.equalsIgnoreCase("password")) {
                    String currentPassword = JOptionPane.showInputDialog("Enter current password:");
                    String email = getPrimaryKeyValue(selectedRow);

                    if (currentPassword != null && validatePassword(currentPassword, email)) {
                        String newPassword = JOptionPane.showInputDialog("Enter new password:");
                        if (newPassword != null && !newPassword.trim().isEmpty()) {
                            model.setValueAt("********", selectedRow, selectedColumn);
                            updateDatabaseValue(columnName.toLowerCase(), newPassword, selectedRow);
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid password. Cannot proceed with editing.");
                    }
                } else {
                    String newValue = JOptionPane.showInputDialog("Enter new value:", currentValue);
                    if (newValue != null && !newValue.trim().isEmpty()) {
                        model.setValueAt(newValue, selectedRow, selectedColumn);
                        updateDatabaseValue(columnName.toLowerCase(), newValue, selectedRow);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a cell to edit.");
            }
        });

        // Delete button action
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int confirm = JOptionPane.showConfirmDialog(frame,
                        "Are you sure you want to delete this record?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteRecord(selectedRow);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a row to delete.");
            }
        });

        // Add buttons to panel
        buttonPanel.add(refreshButton);
        buttonPanel.add(filterButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        panel.add(orangePanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void refreshTableData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Clear existing data
            model.setRowCount(0);

            // Get column names and data based on selected table
            String query = "SELECT * FROM " + currentTable;
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();

            // Set column names
            int columnCount = metaData.getColumnCount();
            String[] columns = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                columns[i-1] = metaData.getColumnName(i);
            }
            model.setColumnIdentifiers(columns);

            // Add data rows
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    // Mask password fields
                    if (metaData.getColumnName(i).equalsIgnoreCase("password")) {
                        row[i-1] = "********";
                    } else {
                        row[i-1] = rs.getObject(i);
                    }
                }
                model.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error refreshing data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showFilterDialog() {
        JDialog filterDialog = new JDialog(frame, "Filter Data", true);
        filterDialog.setSize(500, 400);
        filterDialog.setLocationRelativeTo(frame);
        filterDialog.setLayout(new BorderLayout());

        // Create panel for filter criteria
        JPanel filterPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Get column names from model
        String[] columns = new String[model.getColumnCount()];
        for (int i = 0; i < model.getColumnCount(); i++) {
            columns[i] = model.getColumnName(i);
        }

        // Create components for each filter
        JComboBox<String> columnCombo = new JComboBox<>(columns);
        JTextField valueField = new JTextField();
        JButton addFilterButton = new JButton("Add Filter");

        // Panel to display active filters
        JPanel activeFiltersPanel = new JPanel();
        activeFiltersPanel.setLayout(new BoxLayout(activeFiltersPanel, BoxLayout.Y_AXIS));
        JScrollPane filtersScrollPane = new JScrollPane(activeFiltersPanel);

        // Apply/Cancel buttons
        JPanel buttonPanel = new JPanel();
        JButton applyButton = new JButton("Apply Filters");
        JButton clearButton = new JButton("Clear All");
        JButton cancelButton = new JButton("Cancel");

        // Style buttons to match theme
        Font montserratBold = loadMontserratBoldFont();
        for (JButton button : new JButton[]{addFilterButton, applyButton, clearButton, cancelButton}) {
            button.setBackground(new Color(255, 140, 0));
            button.setForeground(new Color(10, 25, 74));
            button.setFont(montserratBold.deriveFont(Font.BOLD, 14f));
        }

        // Add filter button action
        addFilterButton.addActionListener(e -> {
            String selectedColumn = (String) columnCombo.getSelectedItem();
            String filterValue = valueField.getText().trim();

            if (!filterValue.isEmpty()) {
                JPanel singleFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JLabel filterLabel = new JLabel(selectedColumn + ": " + filterValue);
                JButton removeButton = new JButton("X");

                // Style remove button
                removeButton.setBackground(new Color(255, 140, 0));
                removeButton.setForeground(new Color(10, 25, 74));
                removeButton.setFont(montserratBold.deriveFont(Font.BOLD, 10f));
                removeButton.setMargin(new Insets(0, 5, 0, 5));

                removeButton.addActionListener(ev -> {
                    activeFiltersPanel.remove(singleFilterPanel);
                    activeFiltersPanel.revalidate();
                    activeFiltersPanel.repaint();
                });

                singleFilterPanel.add(filterLabel);
                singleFilterPanel.add(removeButton);
                activeFiltersPanel.add(singleFilterPanel);
                activeFiltersPanel.revalidate();
                activeFiltersPanel.repaint();

                valueField.setText("");
            }
        });

        // Apply filters button action
        applyButton.addActionListener(e -> {
            // Collect all active filters
            List<String> filters = new ArrayList<>();
            for (Component comp : activeFiltersPanel.getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    Component[] components = panel.getComponents();
                    if (components.length > 0 && components[0] instanceof JLabel) {
                        String labelText = ((JLabel) components[0]).getText();
                        filters.add(labelText);
                    }
                }
            }

            if (!filters.isEmpty()) {
                applyFilters(filters);
                filterDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(filterDialog, "No filters added!", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Clear all button action
        clearButton.addActionListener(e -> {
            activeFiltersPanel.removeAll();
            activeFiltersPanel.revalidate();
            activeFiltersPanel.repaint();
        });

        // Cancel button action
        cancelButton.addActionListener(e -> filterDialog.dispose());

        // Add components to dialog
        JPanel inputPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        inputPanel.add(new JLabel("Select Column:"));
        inputPanel.add(columnCombo);
        inputPanel.add(new JLabel("Filter Value:"));
        inputPanel.add(valueField);
        inputPanel.add(addFilterButton);

        buttonPanel.add(applyButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(cancelButton);

        filterDialog.add(inputPanel, BorderLayout.NORTH);
        filterDialog.add(filtersScrollPane, BorderLayout.CENTER);
        filterDialog.add(buttonPanel, BorderLayout.SOUTH);

        filterDialog.setVisible(true);
    }

    private void applyFilters(List<String> filters) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Clear existing data
            model.setRowCount(0);

            StringBuilder whereClause = new StringBuilder();
            List<String> params = new ArrayList<>();

            for (String filter : filters) {
                String[] parts = filter.split(": ");
                if (parts.length == 2) {
                    String column = parts[0].trim();
                    String value = parts[1].trim();

                    if (whereClause.length() > 0) {
                        whereClause.append(" AND ");
                    }
                    whereClause.append(column).append(" LIKE ?");
                    params.add("%" + value + "%");
                }
            }

            String query = "SELECT * FROM " + currentTable;
            if (whereClause.length() > 0) {
                query += " WHERE " + whereClause.toString();
            }

            PreparedStatement stmt = conn.prepareStatement(query);
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    // Mask password fields
                    if (metaData.getColumnName(i).equalsIgnoreCase("password")) {
                        row[i-1] = "********";
                    } else {
                        row[i-1] = rs.getObject(i);
                    }
                }
                model.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error applying filters.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDatabaseValue(String columnName, String newValue, int selectedRow) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String primaryKey = getPrimaryKeyColumn();
            String primaryValue = getPrimaryKeyValue(selectedRow);

            String query = "UPDATE " + currentTable + " SET " + columnName + " = ? WHERE " + primaryKey + " = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, newValue);
            stmt.setString(2, primaryValue);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(frame, "Record updated successfully!");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error updating record.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRecord(int selectedRow) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String primaryKey = getPrimaryKeyColumn();
            String primaryValue = getPrimaryKeyValue(selectedRow);

            String query = "DELETE FROM " + currentTable + " WHERE " + primaryKey + " = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, primaryValue);

            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                model.removeRow(selectedRow);
                JOptionPane.showMessageDialog(frame, "Record deleted successfully!");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error deleting record.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getPrimaryKeyColumn() {
        // This is a simplified approach - in a real application, you might want to
        // query the database for the actual primary key column
        switch (currentTable) {
            case "user_info": return "enrollment_number";
            case "teachers_info": return "enrollment_number";
            case "results": return "result_id"; // Assuming this exists
            case "courses": return "course_id"; // Assuming this exists
            case "teacher_courses": return "id"; // Assuming this exists
            default: return "id"; // Fallback
        }
    }

    private String getPrimaryKeyValue(int selectedRow) {
        // Find which column is the primary key
        String primaryKeyColumn = getPrimaryKeyColumn();
        for (int i = 0; i < model.getColumnCount(); i++) {
            if (model.getColumnName(i).equalsIgnoreCase(primaryKeyColumn)) {
                return model.getValueAt(selectedRow, i).toString();
            }
        }
        return null;
    }

    private boolean validatePassword(String currentPassword, String email) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT password FROM " + currentTable + " WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("password");
                return currentPassword.equals(dbPassword);
            }
            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private static Font loadMontserratBoldFont() {
        try {
            File fontFile = new File("D:\\testing\\src\\Montserrat-Bold.ttf");
            if (fontFile.exists()) {
                return Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(18f);
            } else {
                System.out.println("Font file not found!");
                return new Font("Arial", Font.BOLD, 18);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("Arial", Font.BOLD, 18);
        }
    }
}