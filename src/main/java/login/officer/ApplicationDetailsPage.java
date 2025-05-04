package login.officer;

import com.gov.landportal.FirebaseInitializer;
import com.google.cloud.firestore.DocumentSnapshot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

public class ApplicationDetailsPage extends JFrame {

    private Application application;

    public ApplicationDetailsPage(Application application) {
        this.application = application;

        setTitle("Application Details");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        try {
            // Fetch application details
            DocumentSnapshot appSnapshot = FirebaseInitializer.getDB()
                    .collection("applications")
                    .document(application.getUid())
                    .get()
                    .get();

            if (appSnapshot.exists()) {
                // Get application data
                String name = FirebaseInitializer.getUserName(appSnapshot.getString("uid"));
                String title = appSnapshot.getString("title");
                String description = appSnapshot.getString("description");
                String plotId = appSnapshot.getString("plotId");
                boolean isSellable = Boolean.TRUE.equals(appSnapshot.getBoolean("isSellable"));
                String status = appSnapshot.getString("status");

                // Update local application instance
                application.setTitle(title);
                application.setPlotId(plotId);

                JLabel headerLabel = new JLabel("APPLICATION DETAILS", JLabel.CENTER);
                headerLabel.setFont(new Font("Serif", Font.BOLD, 18));
                contentPanel.add(headerLabel);

                if (name != null && !name.isEmpty()) contentPanel.add(new JLabel("Name: " + name));
                if (title != null && !title.isEmpty()) contentPanel.add(new JLabel("Title: " + title));
                if (description != null && !description.isEmpty()) contentPanel.add(new JLabel("Description: " + description));
                if (plotId != null && !plotId.isEmpty()) contentPanel.add(new JLabel("Plot ID: " + plotId));
                contentPanel.add(new JLabel("Is Sellable: " + isSellable));
                if (status != null && !status.isEmpty()) contentPanel.add(new JLabel("Status: " + status));

                // Fetch plot details if plotId exists
                if (plotId != null && !plotId.isEmpty()) {
                    DocumentSnapshot plotSnapshot = FirebaseInitializer.getDB()
                            .collection("plots")
                            .document(plotId)
                            .get()
                            .get();

                    if (plotSnapshot.exists()) {
                        String plotAddress = plotSnapshot.getString("address");
                        if (plotAddress != null && !plotAddress.isEmpty()) {
                            contentPanel.add(new JLabel("Plot Address: " + plotAddress));
                        }

                        String mapLink = plotSnapshot.getString("mapLink");
                        if (mapLink != null && !mapLink.isEmpty()) {
                            JButton openMapBtn = new JButton("Open Map Link");
                            openMapBtn.addActionListener((ActionEvent e) -> {
                                try {
                                    Desktop.getDesktop().browse(new URI(mapLink));
                                } catch (IOException | URISyntaxException ex) {
                                    ex.printStackTrace();
                                    JOptionPane.showMessageDialog(this,
                                            "Failed to open map link.",
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            });
                            contentPanel.add(openMapBtn);
                        }
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load application details.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        // Add approve/reject buttons
        JPanel buttonPanel = getButtonPanel();

        // Add panels to frame
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton approveButton = new JButton("Approve");
        JButton rejectButton = new JButton("Reject");

        approveButton.setBackground(Color.GREEN.darker());
        approveButton.setForeground(Color.WHITE);

        rejectButton.setBackground(Color.RED.darker());
        rejectButton.setForeground(Color.WHITE);

        approveButton.addActionListener(e -> {
            try {
                FirebaseInitializer.updateApplicationStatus(application.getUid(), "Approved");
                JOptionPane.showMessageDialog(this, "Application Approved!");

                String title = application.getTitle();
                if (title != null && title.toLowerCase().contains("add plot to portal")) {
                    String plotId = application.getPlotId();
                    if (plotId != null && !plotId.isEmpty()) {
                        FirebaseInitializer.getDB()
                                .collection("plots")
                                .document(plotId)
                                .update("approved", true);
                    }
                }

                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Failed to approve and update plot.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        rejectButton.addActionListener(e -> {
            FirebaseInitializer.updateApplicationStatus(application.getUid(), "Rejected");
            JOptionPane.showMessageDialog(this, "Application Rejected!");
            dispose();
        });

        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);

        return buttonPanel;
    }
}
