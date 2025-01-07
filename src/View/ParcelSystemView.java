package View;

import Model.Customer;
import Model.Parcel;
import Model.ParcelMap;
import Model.QueueOfCustomers;
import Utility.Log;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Optional;

public class ParcelSystemView extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private ParcelMap parcelMap;
    private QueueOfCustomers queueOfCustomers;
    private DefaultTableModel parcelTableModel, customerTableModel;
    private JTable parcelTable, customerTable;

    public ParcelSystemView() {
        initializeComponents();
        parcelMap = new ParcelMap();
        queueOfCustomers = new QueueOfCustomers();

        JButton calculateFeeButton = new JButton("Calculate Fee");
        calculateFeeButton.addActionListener(e -> calculateFee());
        add(calculateFeeButton, BorderLayout.SOUTH);
    }

    private void initializeComponents() {
        setTitle("Depot System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.add(createWorkerPanel(), "Worker");
        cardPanel.add(createAdminPanel(), "Admin");
        cardPanel.add(createCustomerPanel(), "Customer");
        add(cardPanel, BorderLayout.CENTER);
    }

    private JPanel createWorkerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel tablePanel = new JPanel(new GridLayout(1, 2));

        parcelTableModel = new DefaultTableModel(new Object[]{"ID", "Days in Depot", "Weight", "Length", "Width", "Height", "Status"}, 0);
        parcelTable = new JTable(parcelTableModel);
        JScrollPane parcelScrollPane = new JScrollPane(parcelTable);

        customerTableModel = new DefaultTableModel(new Object[]{"Queue No", "Name", "ID"}, 0);
        customerTable = new JTable(customerTableModel);
        JScrollPane customerScrollPane = new JScrollPane(customerTable);

        tablePanel.add(parcelScrollPane);
        tablePanel.add(customerScrollPane);
        panel.add(tablePanel, BorderLayout.CENTER);

        // Creating the button panel with all action buttons
        JPanel buttonPanel = new JPanel(new GridLayout(4, 2));  // Adjusted to accommodate all buttons
        buttonPanel.add(createButton("Load Parcels", "src/Parcels.csv", parcelTableModel));
        buttonPanel.add(createButton("Load Customers", "src/Custs.csv", customerTableModel));
        buttonPanel.add(createSearchCustomerButton(panel));  // Add Search Customer button
        buttonPanel.add(createButton("Add Customer", null, null, e -> addCustomer()));
        buttonPanel.add(createButton("Remove Customer", null, null, e -> removeCustomer()));
        buttonPanel.add(createButton("Add Parcel", null, null, e -> addParcel())); // Fixed Add Parcel button
        buttonPanel.add(createButton("Mark Parcel", null, null, e -> markParcel())); // Fixed Mark Parcel button
        buttonPanel.add(createButton("Calculate Fee", null, null, e -> calculateFee()));  // Example for Calculate Fee button

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel();
        JButton generateReportButton = new JButton("Generate Report");
        generateReportButton.addActionListener(this::generateReport);
        panel.add(generateReportButton);
        return panel;
    }

    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JTextField searchField = new JTextField(15);
        JButton searchButton = new JButton("Search Customer");
        searchButton.addActionListener(e -> searchCustomer(searchField.getText(), panel));

        panel.add(new JLabel("Enter Customer Name or ID:"));
        panel.add(searchField);
        panel.add(searchButton);
        return panel;
    }

    private JButton createButton(String title, String filePath, DefaultTableModel model) {
        JButton button = new JButton(title);
        button.addActionListener(e -> loadData(filePath, model));
        return button;
    }

    private JButton createButton(String title, String filePath, DefaultTableModel model, ActionListener action) {
        JButton button = new JButton(title);
        button.addActionListener(action);
        return button;
    }

    private JButton createSearchCustomerButton(JPanel panel) {
        JButton button = new JButton("Search Customer");
        button.addActionListener(e -> searchCustomerFromWorker(panel));
        return button;
    }

    private void loadData(String filePath, DefaultTableModel model) {
        model.setRowCount(0);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (model == parcelTableModel) {
                    String[] fullData = new String[data.length + 1];
                    System.arraycopy(data, 0, fullData, 0, data.length);
                    fullData[data.length] = "Unprocessed";
                    Parcel parcel = new Parcel(data[0], Integer.parseInt(data[1]), Double.parseDouble(data[2]), Integer.parseInt(data[3]), Integer.parseInt(data[4]), Integer.parseInt(data[5]));
                    parcelMap.addParcel(parcel);
                    model.addRow(fullData);
                } else if (model == customerTableModel) {
                    if (parcelMap.containsParcel(data[1])) {
                        Customer customer = new Customer(0, data[0], data[1]);
                        model.addRow(new Object[]{String.valueOf(model.getRowCount() + 1), customer.getName(), customer.getParcelId()}); // Fixed row adding
                        queueOfCustomers.enqueue(customer);
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchCustomer(String query, JPanel panel) {
        for (Customer customer : queueOfCustomers.getAllCustomers()) {
            if (customer.getName().contains(query) || customer.getParcelId().equals(query)) {
                JOptionPane.showMessageDialog(panel, "Customer Found: " + customer.getName() + "\nParcel ID: " + customer.getParcelId(), "Customer Details", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        JOptionPane.showMessageDialog(panel, "Customer not found", "Search", JOptionPane.INFORMATION_MESSAGE);
    }

    private void searchCustomerFromWorker(JPanel panel) {
        JTextField searchField = new JTextField(15);
        JButton searchButton = new JButton("Search Customer");

        searchButton.addActionListener(e -> searchCustomer(searchField.getText(), panel));

        // Create a dialog for the user to input the search query
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Enter Customer Name or ID:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Show the dialog for searching
        int result = JOptionPane.showConfirmDialog(this, searchPanel, "Search Customer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            searchCustomer(searchField.getText(), panel);
        }
    }

    private void calculateFee() {
        int selectedRow = parcelTable.getSelectedRow();
        if (selectedRow >= 0) {
            double weight = Double.parseDouble(parcelTableModel.getValueAt(selectedRow, 2).toString());
            double feeRate = 5.0;
            double fee = weight * feeRate;
            JOptionPane.showMessageDialog(this, "The fee for the selected parcel is: $" + fee, "Calculate Fee", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No parcel selected.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateReport(ActionEvent event) {
        try (PrintWriter writer = new PrintWriter(new File("src/report.csv"))) {
            for (int row = 0; row < parcelTableModel.getRowCount(); row++) {
                for (int col = 0; col < parcelTableModel.getColumnCount(); col++) {
                    writer.print(parcelTableModel.getValueAt(row, col) + ",");
                }
                writer.println();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCustomer() {
        JTextField nameField = new JTextField(10);
        JTextField parcelIdField = new JTextField(10);
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Parcel ID:"));
        panel.add(parcelIdField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Customer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            String parcelId = parcelIdField.getText();
            if (parcelMap.containsParcel(parcelId)) {
                Customer customer = new Customer(queueOfCustomers.getSize() + 1, name, parcelId);
                queueOfCustomers.enqueue(customer);
                updateCustomerPanel();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow >= 0) {
            String parcelId = customerTableModel.getValueAt(selectedRow, 2).toString();
            queueOfCustomers.remove(parcelId);
            customerTableModel.removeRow(selectedRow);
        }
    }

    private void addParcel() {
        JTextField idField = new JTextField(10);
        JTextField daysField = new JTextField(10);
        JTextField weightField = new JTextField(10);
        JTextField lengthField = new JTextField(10);
        JTextField widthField = new JTextField(10);
        JTextField heightField = new JTextField(10);
        JPanel panel = new JPanel(new GridLayout(6, 2));
        panel.add(new JLabel("ID:"));
        panel.add(idField);
        panel.add(new JLabel("Days in Depot:"));
        panel.add(daysField);
        panel.add(new JLabel("Weight:"));
        panel.add(weightField);
        panel.add(new JLabel("Length:"));
        panel.add(lengthField);
        panel.add(new JLabel("Width:"));
        panel.add(widthField);
        panel.add(new JLabel("Height:"));
        panel.add(heightField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Parcel", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Parcel parcel = new Parcel(idField.getText(), Integer.parseInt(daysField.getText()), Double.parseDouble(weightField.getText()), Integer.parseInt(lengthField.getText()), Integer.parseInt(widthField.getText()), Integer.parseInt(heightField.getText()));
            parcelMap.addParcel(parcel);
            updateParcelPanel();
        }
    }

    private void markParcel() {
        int selectedRow = parcelTable.getSelectedRow();
        if (selectedRow >= 0) {
            parcelTableModel.setValueAt("Processed", selectedRow, 6);
            String parcelId = parcelTableModel.getValueAt(selectedRow, 0).toString();
            parcelMap.removeParcel(parcelId);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a customer to remove.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateParcelPanel() {
        parcelTableModel.setRowCount(0);
        for (Parcel parcel : parcelMap.getAllParcels()) {
            parcelTableModel.addRow(new Object[]{parcel.getId(), parcel.getDaysInDepot(), parcel.getWeight(), parcel.getLength(), parcel.getWidth(), parcel.getHeight(), parcel.getStatus()});
        }
    }

    private void updateCustomerPanel() {
    customerTableModel.setRowCount(0);
    int queueNo = 1;
    for (Customer customer : queueOfCustomers.getAllCustomers()) {
        customerTableModel.addRow(new Object[]{queueNo++, customer.getName(), customer.getParcelId()});
    }
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ParcelSystemView().setVisible(true));
    }
}

