import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ExpenseTracker extends JFrame {
    // Change these values to match your local MySQL setup.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/expense_tracker?serverTimezone=Asia/Kolkata";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Ch@030306";

    private Connection connection;

    private final JTextField amountField = new JTextField(12);
    private final JComboBox<String> categoryBox = new JComboBox<>(new String[] {
            "Food", "Travel", "Rent", "Shopping", "Education",
            "Bills", "Health", "Entertainment", "Other"
    });
    private final JTextField dateField = new JTextField(LocalDate.now().toString(), 12);
    private final JTextField descriptionField = new JTextField(18);

    private final JLabel totalLabel = new JLabel("₹0.00");
    private final JLabel todayLabel = new JLabel("₹0.00");
    private final JLabel countLabel = new JLabel("0");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[] { "ID", "Date", "Category", "Description", "Amount (₹)" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable expenseTable = new JTable(tableModel);

    public ExpenseTracker() {
        setTitle("Expense Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setMinimumSize(new Dimension(780, 560));
        setLocationRelativeTo(null);

        buildUI();
        connectToDatabase();
        refreshExpenses();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeDatabase();
            }
        });
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.setBackground(new Color(243, 246, 250));
        setContentPane(root);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(23, 43, 77));
        header.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("Expense Tracker");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 25));

        JLabel subtitle = new JLabel("Track your daily spending");
        subtitle.setForeground(new Color(215, 226, 242));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(subtitle);
        header.add(titleBox, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        root.add(center, BorderLayout.CENTER);

        JPanel summary = new JPanel(new GridLayout(1, 3, 12, 0));
        summary.setOpaque(false);
        summary.add(makeSummaryCard("TOTAL SPENT", totalLabel, new Color(36, 87, 167)));
        summary.add(makeSummaryCard("SPENT TODAY", todayLabel, new Color(38, 116, 71)));
        summary.add(makeSummaryCard("TRANSACTIONS", countLabel, new Color(120, 66, 164)));

        JPanel topCenter = new JPanel(new BorderLayout(0, 14));
        topCenter.setOpaque(false);
        topCenter.add(summary, BorderLayout.NORTH);
        topCenter.add(makeEntryPanel(), BorderLayout.CENTER);
        center.add(topCenter, BorderLayout.NORTH);

        JPanel historyPanel = new JPanel(new BorderLayout(0, 8));
        historyPanel.setBackground(Color.WHITE);
        historyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 227, 235)),
                new EmptyBorder(12, 12, 12, 12)));

        JLabel historyTitle = new JLabel("Expense History");
        historyTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
        historyTitle.setForeground(new Color(23, 43, 77));
        historyPanel.add(historyTitle, BorderLayout.NORTH);

        expenseTable.setRowHeight(27);
        expenseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        expenseTable.setFillsViewportHeight(true);
        expenseTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        expenseTable.getTableHeader().setBackground(new Color(233, 238, 246));
        expenseTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        expenseTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        expenseTable.getColumnModel().getColumn(2).setPreferredWidth(115);
        expenseTable.getColumnModel().getColumn(3).setPreferredWidth(360);
        expenseTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        historyPanel.add(new JScrollPane(expenseTable), BorderLayout.CENTER);

        JPanel historyButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        historyButtons.setOpaque(false);
        JButton refreshButton = makeButton("Refresh", new Color(230, 235, 243),
                new Color(23, 43, 77));
        JButton deleteButton = makeButton("Delete Selected", new Color(181, 52, 61),
                Color.WHITE);
        refreshButton.addActionListener(e -> refreshExpenses());
        deleteButton.addActionListener(e -> deleteSelectedExpense());
        historyButtons.add(refreshButton);
        historyButtons.add(deleteButton);
        historyPanel.add(historyButtons, BorderLayout.SOUTH);

        center.add(historyPanel, BorderLayout.CENTER);
    }

    private JPanel makeSummaryCard(String heading, JLabel value, Color valueColor) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 7));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 227, 235)),
                new EmptyBorder(12, 16, 12, 16)));

        JLabel label = new JLabel(heading);
        label.setForeground(new Color(102, 112, 133));
        label.setFont(new Font("SansSerif", Font.BOLD, 11));

        value.setForeground(valueColor);
        value.setFont(new Font("SansSerif", Font.BOLD, 23));

        card.add(label);
        card.add(value);
        return card;
    }

    private JPanel makeEntryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 227, 235)),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel heading = new JLabel("Add a New Expense");
        heading.setForeground(new Color(23, 43, 77));
        heading.setFont(new Font("SansSerif", Font.BOLD, 17));
        panel.add(heading, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 5, 4, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        addField(fields, c, 0, 0, "Amount (₹)", amountField);
        addField(fields, c, 1, 0, "Category", categoryBox);
        addField(fields, c, 0, 1, "Date (YYYY-MM-DD)", dateField);
        addField(fields, c, 1, 1, "Description", descriptionField);
        panel.add(fields, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton addButton = makeButton("+ Add Expense", new Color(36, 87, 167), Color.WHITE);
        JButton clearButton = makeButton("Clear", new Color(230, 235, 243),
                new Color(23, 43, 77));
        addButton.addActionListener(e -> addExpense());
        clearButton.addActionListener(e -> clearForm());
        actions.add(clearButton);
        actions.add(addButton);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints c, int column, int row,
            String labelText, JComponent field) {
        c.gridx = column * 2;
        c.gridy = row;
        c.weightx = 0;
        JLabel label = new JLabel(labelText);
        label.setForeground(new Color(52, 64, 84));
        panel.add(label, c);

        c.gridx = column * 2 + 1;
        c.weightx = 1;
        field.setPreferredSize(new Dimension(150, 30));
        panel.add(field, c);
    }

    private JButton makeButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBorder(new EmptyBorder(9, 14, 9, 14));
        return button;
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not connect to MySQL.\n\n"
                            + "1. Make sure MySQL Server is running.\n"
                            + "2. Check DB_USER and DB_PASSWORD in ExpenseTracker.java.\n"
                            + "3. Run schema.sql to create the database.\n\n"
                            + "Details: " + e.getMessage(),
                    "Database Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addExpense() {
        if (connection == null) {
            showDatabaseUnavailable();
            return;
        }

        BigDecimal amount;
        LocalDate date;
        try {
            amount = new BigDecimal(amountField.getText().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Enter a valid amount greater than zero.",
                    "Invalid Amount", JOptionPane.WARNING_MESSAGE);
            amountField.requestFocus();
            return;
        }

        try {
            date = LocalDate.parse(dateField.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Enter the date in YYYY-MM-DD format, for example 2026-10-15.",
                    "Invalid Date", JOptionPane.WARNING_MESSAGE);
            dateField.requestFocus();
            return;
        }

        String sql = "INSERT INTO expenses (amount, category, expense_date, description) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setString(2, (String) categoryBox.getSelectedItem());
            ps.setDate(3, Date.valueOf(date));
            ps.setString(4, descriptionField.getText().trim());
            ps.executeUpdate();

            clearForm();
            refreshExpenses();
            JOptionPane.showMessageDialog(this, "Expense added successfully!");
        } catch (SQLException ex) {
            showSqlError(ex);
        }
    }

    private void refreshExpenses() {
        if (connection == null) {
            tableModel.setRowCount(0);
            totalLabel.setText("₹0.00");
            todayLabel.setText("₹0.00");
            countLabel.setText("0");
            return;
        }

        tableModel.setRowCount(0);
        String sql = "SELECT id, expense_date, category, description, amount "
                + "FROM expenses ORDER BY expense_date DESC, id DESC";

        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String description = rs.getString("description");
                if (description == null)
                    description = "";
                tableModel.addRow(new Object[] {
                        rs.getInt("id"),
                        rs.getDate("expense_date"),
                        rs.getString("category"),
                        description,
                        rs.getBigDecimal("amount").toPlainString()
                });
            }
            updateSummary();
        } catch (SQLException ex) {
            showSqlError(ex);
        }
    }

    private void updateSummary() {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total, "
                + "COALESCE(SUM(CASE WHEN expense_date = ? THEN amount ELSE 0 END), 0) "
                + "AS today_total, COUNT(*) AS total_count FROM expenses";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalLabel.setText("₹" + rs.getBigDecimal("total").toPlainString());
                    todayLabel.setText("₹" + rs.getBigDecimal("today_total").toPlainString());
                    countLabel.setText(String.valueOf(rs.getInt("total_count")));
                }
            }
        } catch (SQLException ex) {
            showSqlError(ex);
        }
    }

    private void deleteSelectedExpense() {
        if (connection == null) {
            showDatabaseUnavailable();
            return;
        }

        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an expense from the table first.");
            return;
        }

        int id = ((Number) tableModel.getValueAt(selectedRow, 0)).intValue();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete the selected expense?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION)
            return;

        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM expenses WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            refreshExpenses();
        } catch (SQLException ex) {
            showSqlError(ex);
        }
    }

    private void clearForm() {
        amountField.setText("");
        categoryBox.setSelectedIndex(0);
        dateField.setText(LocalDate.now().toString());
        descriptionField.setText("");
        amountField.requestFocus();
    }

    private void showDatabaseUnavailable() {
        JOptionPane.showMessageDialog(this,
                "Database is not connected. Check the MySQL settings and restart the app.",
                "Database Unavailable", JOptionPane.ERROR_MESSAGE);
    }

    private void showSqlError(SQLException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    private void closeDatabase() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // Application is closing.
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Use the default Swing look and feel.
            }
            new ExpenseTracker().setVisible(true);
        });
    }
}
