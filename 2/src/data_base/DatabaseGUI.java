package data_base;

import data_base.commands.Command;
import data_base.commands.SQLParser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Map;

public class DatabaseGUI {
    private final Database database;
    private final DatabaseIO databaseIO;
    private final JFrame frame;
    private JList<String> tablesList;
    private JTable dataTable;
    private JTextArea sqlTextArea;

    public DatabaseGUI(Database database, DatabaseIO databaseIO) {
        this.database = database;
        this.databaseIO = databaseIO;

        frame = new JFrame();
        frame.setTitle("DATABASE");
        frame.setSize(800, 500);
        frame.setLayout(new BorderLayout());
        ImageIcon icon = new ImageIcon("cat.png");
        frame.setIconImage(icon.getImage());

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(createWindowCloseHandler()); // создаем обработчик, чтобы при закрытии просиходило сохранение

        JPanel tables = createTablesPanel();
        frame.add(tables, BorderLayout.WEST);

        JScrollPane dataScrollPane = createDataTablePanel();
        frame.add(dataScrollPane, BorderLayout.EAST);

        JPanel bottomPanel = createSqlPanel();
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
        frame.setResizable(false);

        refreshTablesList();
    }

    private JPanel createTablesPanel() {
        JPanel tables = new JPanel();
        tables.setPreferredSize(new Dimension(300, 300)); // указывает желаемый размер панели

        // создание и настройка списка таблиц с возможностью прокрутки(JScrollPane)
        tablesList = new JList<>();
        refreshTablesList();
        tables.add(new JScrollPane(tablesList), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        JButton addTableBtn = new JButton("Добавить таблицу");
        JButton deleteTableBtn = new JButton("Удалить таблицу");

        addTableBtn.addActionListener(_ -> addTable());
        deleteTableBtn.addActionListener(_ -> deleteTable());

        // создает панель с прокруткой
        JScrollPane listScrollPane = new JScrollPane(tablesList);
        listScrollPane.setPreferredSize(new Dimension(250, 200));
        tables.add(listScrollPane, BorderLayout.CENTER);

        buttonPanel.add(addTableBtn);
        buttonPanel.add(deleteTableBtn);
        tables.add(buttonPanel, BorderLayout.SOUTH);

        tablesList.addListSelectionListener(_ -> showTableData());
        return tables;
    }

    private void refreshTablesList() {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String tableName : database.getTables().keySet()) {
            model.addElement(tableName);
        }

        tablesList.setModel(model);
    }

    private void addTable() {
        String tableName = JOptionPane.showInputDialog(frame, "Введите название таблицы:");
        if (tableName != null && !tableName.trim().isEmpty()) {
            try {
                database.addTable(tableName, null);
                refreshTablesList();
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteTable() {
        String tableName = tablesList.getSelectedValue();
        if (tableName != null) {
            try {
                database.dropTable(tableName);
                refreshTablesList();
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showTableData() {
        String tableName = tablesList.getSelectedValue();
        if (tableName == null) return;

        ArrayList<Column> columns = new ArrayList<>(database.getColumns(tableName));

        DefaultTableModel model = new DefaultTableModel();

        for (Column column : columns) {
            model.addColumn(column.getName());
        }

        for (Map<String, Object> row : database.getRows(tableName)) {
            Object[] rowData = new Object[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                rowData[i] = row.get(columns.get(i).getName());
            }
            model.addRow(rowData);
        }

        dataTable.setModel(model);
    }

    private JScrollPane createDataTablePanel() {
        dataTable = new JTable();
        dataTable.setPreferredSize(new Dimension(500, 355));
        return new JScrollPane(dataTable);
    }

    private JPanel createSqlPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // кнопки управления столбцами
        JPanel columnButtonPanel = new JPanel(new GridLayout(1, 2));
        JButton addColumnBtn = new JButton("Добавить столбец");
        JButton removeColumnBtn = new JButton("Удалить столбец");

        addColumnBtn.addActionListener(_ -> addColumn());
        removeColumnBtn.addActionListener(_ -> removeColumn());

        columnButtonPanel.add(addColumnBtn);
        columnButtonPanel.add(removeColumnBtn);
        panel.add(columnButtonPanel, BorderLayout.NORTH);

        sqlTextArea = new JTextArea(3, 60);
        JButton executeSqlBtn = new JButton("Выполнить команду");
        executeSqlBtn.addActionListener(_ -> executeSql());

        panel.add(new JScrollPane(sqlTextArea), BorderLayout.CENTER);
        panel.add(executeSqlBtn, BorderLayout.SOUTH);

        refreshTablesList();

        return panel;
    }

    private void addColumn() {
        String tableName = tablesList.getSelectedValue();
        if (tableName == null) {
            JOptionPane.showMessageDialog(frame, "Выберите таблицу", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(3, 2));
        JTextField nameField = new JTextField();
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"int", "string", "boolean", "date"});
        JCheckBox uniqueCheck = new JCheckBox("Unique");
        JCheckBox notNullCheck = new JCheckBox("Not Null");

        panel.add(new JLabel("Название столбца:"));
        panel.add(nameField);
        panel.add(new JLabel("Тип:"));
        panel.add(typeCombo);
        panel.add(uniqueCheck);
        panel.add(notNullCheck);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Добавить столбец", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Column column = new Column(nameField.getText(), (String)typeCombo.getSelectedItem(), uniqueCheck.isSelected(), notNullCheck.isSelected());
                database.getTable(tableName).addColumn(column);
                showTableData();
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeColumn() {
        String tableName = tablesList.getSelectedValue();
        if (tableName == null) {
            JOptionPane.showMessageDialog(frame, "Пожалуйста выберите таблицу", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Table table = database.getTable(tableName);
        String columnName = (String)JOptionPane.showInputDialog(frame, "Выберите столбец для удаления:", "Удалить столбец", JOptionPane.PLAIN_MESSAGE, null, table.getColumns().keySet().toArray(), null);

        if (columnName != null) {
            try {
                table.removeColumn(columnName);
                showTableData();
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void executeSql() {
        String sql = sqlTextArea.getText().trim();
        if (sql.isEmpty()) return;

        try {
            SQLParser parser = new SQLParser();
            Command command = parser.parse(sql);
            command.execute(database);
            refreshTablesList();
            showTableData();
            JOptionPane.showMessageDialog(frame, "Команда выполнена успешно");
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

        sqlTextArea.setText("");
    }

    private WindowAdapter createWindowCloseHandler() {
        return new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    databaseIO.saveToFile();
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Ошибка при сохранении: " + ex.getMessage());
                }
                System.exit(0);
            }
        };
    }
}
