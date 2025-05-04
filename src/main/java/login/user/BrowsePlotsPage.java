package login.user;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.gov.landportal.FirebaseInitializer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class BrowsePlotsPage extends JFrame {

    private JComboBox<String> stateCombo, districtCombo, tehsilCombo;
    private JButton searchButton;
    private JTable table;
    private DefaultTableModel tableModel;

    public BrowsePlotsPage() {
        setTitle("Browse Approved Plots");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));
        JPanel filterPanel = new JPanel(new GridLayout(2, 4, 10, 10));

        stateCombo = new JComboBox<>(new String[]{"All", "Bihar", "Uttar Pradesh"});
        districtCombo = new JComboBox<>();
        tehsilCombo = new JComboBox<>();

        stateCombo.addActionListener(e -> updateDistricts());
        districtCombo.addActionListener(e -> updateTehsils());

        filterPanel.add(new JLabel("State:"));
        filterPanel.add(stateCombo);
        filterPanel.add(new JLabel("District:"));
        filterPanel.add(districtCombo);
        filterPanel.add(new JLabel("Tehsil:"));
        filterPanel.add(tehsilCombo);

        searchButton = new JButton("Search");
        searchButton.addActionListener(this::performSearch);
        filterPanel.add(new JLabel());
        filterPanel.add(searchButton);

        add(filterPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Plot No", "Owner", "Sellable", "Map Link", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // All cells non-editable
            }
        };

        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Handle clicks on "Map Link" column
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.rowAtPoint(evt.getPoint());
                int col = table.columnAtPoint(evt.getPoint());
                if (col == 3) {
                    String url = (String) tableModel.getValueAt(row, col);
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Failed to open map link.");
                    }
                }
            }
        });

        updateDistricts(); // Populate districts based on default state

        setVisible(true);
    }

    private void updateDistricts() {
        districtCombo.removeAllItems();
        tehsilCombo.removeAllItems();

        String selectedState = (String) stateCombo.getSelectedItem();
        if ("Bihar".equals(selectedState)) {
            districtCombo.addItem("All");
            districtCombo.addItem("Patna");
            districtCombo.addItem("Gaya");
        } else if ("Uttar Pradesh".equals(selectedState)) {
            districtCombo.addItem("All");
            districtCombo.addItem("Lucknow");
            districtCombo.addItem("Varanasi");
        } else {
            districtCombo.addItem("All");
        }
    }

    private void updateTehsils() {
        tehsilCombo.removeAllItems();
        String selectedDistrict = (String) districtCombo.getSelectedItem();
        if (selectedDistrict != null && !"All".equals(selectedDistrict)) {
            tehsilCombo.addItem("All");
            tehsilCombo.addItem(selectedDistrict + " Tehsil 1");
            tehsilCombo.addItem(selectedDistrict + " Tehsil 2");
        } else {
            tehsilCombo.addItem("All");
        }
    }

    private void performSearch(ActionEvent e) {
        tableModel.setRowCount(0); // clear table

        Firestore db = FirebaseInitializer.getDB();

        // Fetch all approved plots
        ApiFuture<QuerySnapshot> future = db.collection("plots")
                .whereEqualTo("approved", true)
                .get();

        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot doc : documents) {
                String address = (String) doc.get("address");
                String plotNo = (String) doc.get("plotNo");
                String mapLink = (String) doc.get("mapLink");
                Boolean isSellable = (Boolean) doc.get("isSellable");
                String uid = (String) doc.get("uid");

                if (!matchFilters(address)) continue;

                String ownerName = FirebaseInitializer.getUserName(uid);
                String status = "Added on Portal";

                tableModel.addRow(new Object[]{plotNo, ownerName, isSellable, mapLink, status});
            }
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch plots.");
        }
    }

    private boolean matchFilters(String address) {
        String state = (String) stateCombo.getSelectedItem();
        String district = (String) districtCombo.getSelectedItem();
        String tehsil = (String) tehsilCombo.getSelectedItem();

        if (!"All".equals(state) && (address == null || !address.contains(state))) return false;
        if (!"All".equals(district) && (address == null || !address.contains(district))) return false;
        if (!"All".equals(tehsil) && (address == null || !address.contains(tehsil))) return false;

        return true;
    }
}
