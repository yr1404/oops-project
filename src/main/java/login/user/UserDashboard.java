package login.user;

import com.gov.landportal.FirebaseInitializer;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class UserDashboard extends JFrame {

    private JButton profileButton;
    private JButton refreshButton; // New Refresh button
    private JLabel loadingLabel; // Loading indicator
    private String userUid; // Store UID to use when fetching applications
    private JTable applicationsTable; // JTable to display applications

    public UserDashboard(String uid) {
        this.userUid = uid; // Store the user UID
        setTitle("User Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Top Panel - Applications
        JPanel applicationPanel = new JPanel(new BorderLayout());
        JLabel appLabel = new JLabel("Your Applications/Requests Status", JLabel.LEFT);

        // Right side panel for Profile + Refresh
        JPanel appTop = new JPanel(new BorderLayout());
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightPanel.setOpaque(false);

        profileButton = new JButton("Loading..."); // Initially loading...
        refreshButton = new JButton("Refresh");    // New refresh button

        rightPanel.add(profileButton);
        rightPanel.add(refreshButton);

        appTop.add(appLabel, BorderLayout.WEST);
        appTop.add(rightPanel, BorderLayout.EAST);

        // Create a non-editable JTable
        applicationsTable = new JTable(new Object[][]{}, new String[]{"Request", "Status"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable editing of cells
            }
        };

        JScrollPane tableScrollPane = new JScrollPane(applicationsTable);
        applicationPanel.add(appTop, BorderLayout.NORTH);
        applicationPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Bottom Panel - Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        JButton browsePlotsBtn = new JButton("Browse Plots");
        JButton yourPlotsBtn = new JButton("Your Plots");
        JButton createAppBtn = new JButton("Create Application");

        buttonPanel.add(browsePlotsBtn);
        buttonPanel.add(yourPlotsBtn);
        buttonPanel.add(createAppBtn);

        mainPanel.add(applicationPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Button Actions
        browsePlotsBtn.addActionListener(e -> new BrowsePlotsPage());
        yourPlotsBtn.addActionListener(e -> new YourPlotsPage());
        createAppBtn.addActionListener(e -> openCreateApplicationDialog());

        // Refresh Button Action
        refreshButton.addActionListener(e -> {
            loadingLabel.setVisible(true);
            fetchUserApplications(userUid);
        });

        setVisible(true);

        // Create a loading indicator
        createLoadingIndicator();

        // Fetch the name asynchronously after showing window
        fetchUserName(uid);

        // Fetch the applications for the user
        fetchUserApplications(uid);
    }

    private void createLoadingIndicator() {
        // Create a loading label and add it to the frame while the name is being fetched
        loadingLabel = new JLabel(new ImageIcon("assets/loading.gif")); // Load the GIF
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLabel.setVerticalAlignment(SwingConstants.CENTER);
        loadingLabel.setSize(800, 600);
        loadingLabel.setOpaque(true);
        loadingLabel.setBackground(new Color(255, 255, 255, 150)); // Semi-transparent background
        loadingLabel.setVisible(true);
        add(loadingLabel, BorderLayout.CENTER);
    }

    private void fetchUserName(String uid) {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                // Simulating network call to fetch user name from Firebase
                return FirebaseInitializer.getUserName(uid);
            }

            @Override
            protected void done() {
                try {
                    String name = get(); // Get result from background thread
                    if (name != null) {
                        profileButton.setText(name);
                    } else {
                        profileButton.setText("Unknown User");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    profileButton.setText("Error");
                } finally {
                    // Hide the loading indicator and revalidate the layout
                    loadingLabel.setVisible(false);
                    revalidate();
                    repaint();
                }
            }
        };
        worker.execute();
    }

    private void fetchUserApplications(String uid) {
        SwingWorker<List<QueryDocumentSnapshot>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<QueryDocumentSnapshot> doInBackground() throws Exception {
                // Disable refresh button while loading
                refreshButton.setEnabled(false);

                // Fetching applications from Firestore
                Firestore db = FirebaseInitializer.getDB();
                ApiFuture<QuerySnapshot> future = db.collection("applications")
                        .whereEqualTo("uid", uid) // Fetch only the applications of the logged-in user
                        .get();

                // Wait for the query result
                QuerySnapshot querySnapshot = future.get();
                System.out.println("Applications fetched from Firestore: " + querySnapshot.size()); // Debug log
                return querySnapshot.getDocuments(); // Return the list of documents (applications)
            }

            @Override
            protected void done() {
                try {
                    List<QueryDocumentSnapshot> applications = get();
                    System.out.println("Number of applications: " + applications.size()); // Debug log

                    if (applications.isEmpty()) {
                        JOptionPane.showMessageDialog(UserDashboard.this, "No applications found for this user.");
                    }

                    // Prepare data for JTable
                    Object[][] data = new Object[applications.size()][2];
                    for (int i = 0; i < applications.size(); i++) {
                        QueryDocumentSnapshot document = applications.get(i);
                        String requestTitle = document.getString("title");  // Assuming the field is 'title'
                        String status = document.getString("status"); // Assuming the field is 'status'

                        // Set the data in the table
                        data[i][0] = requestTitle;
                        data[i][1] = status;
                    }

                    // Update the JTable with the fetched data
                    applicationsTable.setModel(new javax.swing.table.DefaultTableModel(
                            data,
                            new String[]{"Request", "Status"}
                    ));

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(UserDashboard.this, "Failed to load applications.");
                } finally {
                    refreshButton.setEnabled(true); // Re-enable refresh button
                    loadingLabel.setVisible(false); // Hide loading
                    revalidate();
                    repaint();
                }
            }
        };
        worker.execute();
    }

    private void openCreateApplicationDialog() {
        // Create a simple dialog where the user can input information to create an application
        JTextField requestField = new JTextField();
        JTextField descriptionField = new JTextField();

        Object[] message = {
                "Request Title:", requestField,
                "Description:", descriptionField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Create New Application", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String requestTitle = requestField.getText();
            String description = descriptionField.getText();

            if (requestTitle.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Both fields are required to create an application.");
            } else {
                FirebaseInitializer.createApplication(userUid, requestTitle, description); // Corrected method call
                JOptionPane.showMessageDialog(this, "Application created successfully!");

                // Refresh after creating application
                loadingLabel.setVisible(true);
                fetchUserApplications(userUid);
            }
        }
    }
}
