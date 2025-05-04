package login.user;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.WriteResult;
import com.gov.landportal.FirebaseInitializer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.*;
import java.util.List;

public class YourPlotsPage extends JFrame {

    private DefaultListModel<String> plotListModel;
    private java.util.List<Plot> plotList;
    private final String userUid;

    public YourPlotsPage(String userUid) {
        this.userUid = userUid;

        setTitle("Your Land Plots");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        plotList = new ArrayList<>();
        plotListModel = new DefaultListModel<>();

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // MY PLOTS SECTION
        JLabel plotsLabel = new JLabel("My Plots");
        plotsLabel.setFont(new Font("Serif", Font.BOLD, 18));

        JList<String> plotListView = new JList<>(plotListModel);
        JScrollPane plotScrollPane = new JScrollPane(plotListView);

        // ADD PLOT BUTTON
        JButton addPlotButton = new JButton("Add Plot");
        addPlotButton.addActionListener(e -> showAddPlotDialog());

        // BUTTONS PANEL
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10)); // Add GridLayout for 2 buttons
        JButton editSellableButton = new JButton("Toggle IsSellable");
        editSellableButton.addActionListener(e -> {
            int index = plotListView.getSelectedIndex();
            if (index >= 0) {
                Plot p = plotList.get(index);
                p.isSellable = !p.isSellable; // Toggle the local 'isSellable' status
                plotListModel.set(index, p.toString()); // Update the UI

                // Firebase update
                updatePlotIsSellableInFirebase(p);
            }
        });

        // Open Map Button
        JButton openMapButton = new JButton("Open Map");
        openMapButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = plotListView.getSelectedIndex();
                if (index >= 0) {
                    Plot selectedPlot = plotList.get(index);
                    String mapLink = selectedPlot.mapLink;
                    if (mapLink != null && !mapLink.isEmpty()) {
                        openMapInBrowser(mapLink);
                    } else {
                        JOptionPane.showMessageDialog(YourPlotsPage.this,
                                "Map link is not available for this plot.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // Add buttons to buttonPanel
        buttonPanel.add(editSellableButton); // Add Toggle IsSellable button here
        buttonPanel.add(openMapButton); // Add Open Map button here

        JPanel plotsTopPanel = new JPanel(new BorderLayout());
        plotsTopPanel.add(plotsLabel, BorderLayout.NORTH);
        plotsTopPanel.add(plotScrollPane, BorderLayout.CENTER);

        // Add the panels to mainPanel
        mainPanel.add(plotsTopPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH); // Attach button panel here
        mainPanel.add(addPlotButton, BorderLayout.NORTH); // Add the "Add Plot" button to the top

        add(mainPanel);

        // Fetch all plots from Firebase for the user
        fetchUserPlotsFromFirebase();

        setVisible(true);
    }

    private void fetchUserPlotsFromFirebase() {
        Firestore db = FirebaseInitializer.getDB();
        Query query = db.collection("plots").whereEqualTo("uid", userUid);

        // Fetch all plots for the user
        ApiFuture<QuerySnapshot> future = query.get();
        future.addListener(() -> {
            try {
                QuerySnapshot querySnapshot = future.get();
                for (DocumentSnapshot documentSnapshot : querySnapshot) {
                    String plotNo = documentSnapshot.getString("plotNo");
                    String address = documentSnapshot.getString("address");
                    boolean isSellable = documentSnapshot.getBoolean("isSellable");
                    String mapLink = documentSnapshot.getString("mapLink");
                    boolean approved = documentSnapshot.getBoolean("approved");

                    // Add the plot to the plot list
                    Plot plot = new Plot(plotNo, isSellable, address, mapLink, approved);
                    plotList.add(plot);
                    plotListModel.addElement(plot.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Runnable::run);
    }

    private void showAddPlotDialog() {
        JDialog dialog = new JDialog(this, "Add New Plot", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));

        JTextField plotNoField = new JTextField();
        JCheckBox isSellableBox = new JCheckBox("Is Sellable");

        // Dropdowns
        JComboBox<String> stateCombo = new JComboBox<>(new String[]{"Bihar", "Uttar Pradesh"});
        JComboBox<String> districtCombo = new JComboBox<>();
        JComboBox<String> tehsilCombo = new JComboBox<>();

        // Dummy cascading logic
        stateCombo.addActionListener(e -> {
            districtCombo.removeAllItems();
            tehsilCombo.removeAllItems();
            if (stateCombo.getSelectedItem().equals("Bihar")) {
                districtCombo.addItem("Patna");
                districtCombo.addItem("Gaya");
            } else {
                districtCombo.addItem("Lucknow");
                districtCombo.addItem("Varanasi");
            }
        });

        districtCombo.addActionListener(e -> {
            tehsilCombo.removeAllItems();
            if (districtCombo.getSelectedItem() != null) {
                tehsilCombo.addItem(districtCombo.getSelectedItem() + " Tehsil 1");
                tehsilCombo.addItem(districtCombo.getSelectedItem() + " Tehsil 2");
            }
        });

        stateCombo.setSelectedIndex(0); // Trigger initial population

        JTextField mapLinkField = new JTextField();

        JButton saveButton = new JButton("Save Plot");
        saveButton.addActionListener(e -> {
            String plotNo = plotNoField.getText().trim();
            boolean isSellable = isSellableBox.isSelected();
            String address = stateCombo.getSelectedItem() + ", "
                    + districtCombo.getSelectedItem() + ", "
                    + tehsilCombo.getSelectedItem();
            String mapLink = mapLinkField.getText().trim();

            Plot newPlot = new Plot(plotNo, isSellable, address, mapLink, false); // Default to 'not approved'
            plotList.add(newPlot);
            plotListModel.addElement(newPlot.toString());

            // ✅ Firebase write
            savePlotToFirebase(newPlot);

            dialog.dispose();
        });

        dialog.add(new JLabel("Plot No:")); dialog.add(plotNoField);
        dialog.add(new JLabel("Is Sellable:")); dialog.add(isSellableBox);
        dialog.add(new JLabel("State:")); dialog.add(stateCombo);
        dialog.add(new JLabel("District:")); dialog.add(districtCombo);
        dialog.add(new JLabel("Tehsil:")); dialog.add(tehsilCombo);
        dialog.add(new JLabel("Map Link:")); dialog.add(mapLinkField);
        dialog.add(new JLabel()); dialog.add(saveButton);

        dialog.setVisible(true);
    }

    private void savePlotToFirebase(Plot plot) {
        Firestore db = FirebaseInitializer.getDB();
        DocumentReference docRef = db.collection("plots").document(plot.plotNo); // Assuming plotNo is unique

        Map<String, Object> data = new HashMap<>();
        data.put("uid", userUid);
        data.put("plotNo", plot.plotNo);
        data.put("address", plot.address);
        data.put("isSellable", plot.isSellable);
        data.put("mapLink", plot.mapLink);
        data.put("approved", plot.approved); // Default status to false

        ApiFuture<WriteResult> future = docRef.set(data);
        future.addListener(() -> {
            try {
                WriteResult result = future.get();
                System.out.println("Plot saved to Firebase successfully: " + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Runnable::run);
    }

    private void updatePlotIsSellableInFirebase(Plot plot) {
        Firestore db = FirebaseInitializer.getDB();
        DocumentReference docRef = db.collection("plots").document(plot.plotNo); // Assuming plotNo is unique

        // Update the 'isSellable' field in Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("isSellable", plot.isSellable);

        ApiFuture<WriteResult> future = docRef.update(updates);
        future.addListener(() -> {
            try {
                WriteResult result = future.get();
                System.out.println("Plot 'isSellable' updated in Firebase: " + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Runnable::run);
    }

    // Method to open the map link in the default browser
    private void openMapInBrowser(String mapLink) {
        try {
            URI uri = new URI(mapLink);
            Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to open map link.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Inner class to hold plot data
    static class Plot {
        String plotNo, address, mapLink;
        boolean isSellable, approved;

        Plot(String plotNo, boolean isSellable, String address, String mapLink, boolean approved) {
            this.plotNo = plotNo;
            this.isSellable = isSellable;
            this.address = address;
            this.mapLink = mapLink;
            this.approved = approved;
        }

        public String toString() {
            return plotNo + " | " + address + " | Sellable: " + isSellable + " | " +
                    (approved ? "Added to Portal" : "Not added to Portal");
        }
    }
}
