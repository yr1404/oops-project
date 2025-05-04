package login.user;

import com.gov.landportal.FirebaseInitializer;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import login.forms.LoginCard;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class UserDashboard extends JFrame {

    private JButton profileButton;
    private JButton refreshButton;
    private JButton logoutButton;
    private JLabel loadingLabel;
    private String userUid;
    private JTable applicationsTable;

    // Government color scheme
    private final Color PRIMARY_DARK = new Color(0, 51, 102);     // Dark blue
    private final Color PRIMARY_LIGHT = new Color(214, 232, 248); // Light blue background
    private final Color ACCENT = new Color(180, 24, 24);          // Dark red for important elements
    private final Color TEXT_COLOR = new Color(33, 33, 33);       // Nearly black text
    private final Color HEADER_BG = new Color(240, 240, 240);     // Light gray for headers

    // Fonts
    private final Font HEADER_FONT = new Font("Serif", Font.BOLD, 18);
    private final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 14);
    private final Font NORMAL_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private final Font TABLE_HEADER_FONT = new Font("SansSerif", Font.BOLD, 14);

    public UserDashboard(String uid) {
        this.userUid = uid;
        setTitle("Government Land Portal - User Dashboard");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set the look and feel to a more professional appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create loading indicator first so it's available
        createLoadingIndicator();

        // Main panel with a border
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(PRIMARY_LIGHT);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header panel
        createHeaderPanel(mainPanel);

        // Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setOpaque(false);

        // Applications Panel (Center)
        createApplicationsPanel(contentPanel);

        // Actions Panel (Bottom)
        createActionsPanel(contentPanel);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Setup button actions
        setupButtonActions();

        // Fetch user data
        fetchUserName(uid);
        fetchUserApplications(uid);

        setVisible(true);
    }

    private void createHeaderPanel(JPanel mainPanel) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_DARK);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Government logo/title
        JLabel govLogo = new JLabel("GOVERNMENT LAND PORTAL");
        govLogo.setFont(new Font("Serif", Font.BOLD, 22));
        govLogo.setForeground(Color.WHITE);

        // User controls panel (right side of header)
        JPanel userControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userControls.setOpaque(false);

        profileButton = new JButton("Loading...");
        styleButton(profileButton, false);
        // Safely try to load icon, skip if not found
        try {
            ImageIcon userIcon = new ImageIcon(getClass().getResource("/assets/user_icon.png"));
            profileButton.setIcon(userIcon);
        } catch (Exception e) {
            // Icon not found, continue without it
        }
        profileButton.setForeground(Color.WHITE);

        refreshButton = new JButton("Refresh");
        styleButton(refreshButton, false);
        // Safely try to load icon, skip if not found
        try {
            ImageIcon refreshIcon = new ImageIcon(getClass().getResource("/assets/refresh_icon.png"));
            refreshButton.setIcon(refreshIcon);
        } catch (Exception e) {
            // Icon not found, continue without it
        }
        refreshButton.setForeground(Color.WHITE);

        logoutButton = new JButton("Logout");
        styleButton(logoutButton, true);
        // Safely try to load icon, skip if not found
        try {
            ImageIcon logoutIcon = new ImageIcon(getClass().getResource("/assets/logout_icon.png"));
            logoutButton.setIcon(logoutIcon);
        } catch (Exception e) {
            // Icon not found, continue without it
        }
        logoutButton.setForeground(Color.WHITE);

        userControls.add(profileButton);
        userControls.add(refreshButton);
        userControls.add(logoutButton);

        headerPanel.add(govLogo, BorderLayout.WEST);
        headerPanel.add(userControls, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
    }

    private void createApplicationsPanel(JPanel contentPanel) {
        JPanel applicationPanel = new JPanel(new BorderLayout(0, 10));
        applicationPanel.setOpaque(false);

        // Applications section header
        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setBackground(HEADER_BG);
        sectionHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, PRIMARY_DARK),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JLabel appLabel = new JLabel("YOUR APPLICATIONS & REQUESTS");
        appLabel.setFont(HEADER_FONT);
        appLabel.setForeground(PRIMARY_DARK);
        sectionHeader.add(appLabel, BorderLayout.WEST);

        // Create a styled table
        applicationsTable = new JTable(new Object[][]{}, new String[]{"Request ID", "Request Title", "Status", "Submission Date"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Style the table
        applicationsTable.setFont(NORMAL_FONT);
        applicationsTable.setRowHeight(30);
        applicationsTable.setShowGrid(true);
        applicationsTable.setGridColor(new Color(220, 220, 220));

        // Table header styling
        JTableHeader header = applicationsTable.getTableHeader();
        header.setFont(TABLE_HEADER_FONT);
        header.setBackground(PRIMARY_DARK);
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT));

        // Status cell renderer for color-coding
        applicationsTable.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());

        JScrollPane tableScrollPane = new JScrollPane(applicationsTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(PRIMARY_DARK));

        applicationPanel.add(sectionHeader, BorderLayout.NORTH);
        applicationPanel.add(tableScrollPane, BorderLayout.CENTER);

        contentPanel.add(applicationPanel, BorderLayout.CENTER);
    }

    private void createActionsPanel(JPanel contentPanel) {
        JPanel actionsPanelWrapper = new JPanel(new BorderLayout());
        actionsPanelWrapper.setOpaque(false);

        // Actions section header
        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setBackground(HEADER_BG);
        sectionHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, PRIMARY_DARK),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JLabel actionsLabel = new JLabel("QUICK ACTIONS");
        actionsLabel.setFont(HEADER_FONT);
        actionsLabel.setForeground(PRIMARY_DARK);
        sectionHeader.add(actionsLabel, BorderLayout.WEST);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JButton browsePlotsBtn = createActionButton("Browse Available Plots", "/assets/browse_icon.png");
        JButton yourPlotsBtn = createActionButton("Your Land Plots", "/assets/plot_icon.png");
        JButton createAppBtn = createActionButton("New Application", "/assets/new_app_icon.png");

        buttonsPanel.add(browsePlotsBtn);
        buttonsPanel.add(yourPlotsBtn);
        buttonsPanel.add(createAppBtn);

        actionsPanelWrapper.add(sectionHeader, BorderLayout.NORTH);
        actionsPanelWrapper.add(buttonsPanel, BorderLayout.CENTER);

        contentPanel.add(actionsPanelWrapper, BorderLayout.SOUTH);
    }

    private JButton createActionButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(TEXT_COLOR);

        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_DARK, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PRIMARY_LIGHT);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
            }
        });

        return button;
    }

    private void styleButton(JButton button, boolean isAccent) {
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        if (isAccent) {
            button.setBackground(ACCENT);
        } else {
            button.setBackground(PRIMARY_DARK);
        }

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isAccent) {
                    button.setBackground(new Color(140, 20, 20)); // Darker red on hover
                } else {
                    button.setBackground(new Color(0, 41, 82)); // Darker blue on hover
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isAccent) {
                    button.setBackground(ACCENT);
                } else {
                    button.setBackground(PRIMARY_DARK);
                }
            }
        });
    }

    private void setupButtonActions() {
        // Find and set action listeners for the quick action buttons
        JButton browsePlotsBtn = null;
        JButton yourPlotsBtn = null;
        JButton createAppBtn = null;

        // Find buttons in the actions panel by traversing the component hierarchy
        Container contentPane = getContentPane();
        if (contentPane.getComponent(0) instanceof JPanel) {
            JPanel mainPanel = (JPanel) contentPane.getComponent(0);
            if (mainPanel.getLayout() instanceof BorderLayout) {
                Component centerComp = ((BorderLayout)mainPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                if (centerComp instanceof JPanel) {
                    JPanel contentPanel = (JPanel) centerComp;
                    if (contentPanel.getLayout() instanceof BorderLayout) {
                        Component southComp = ((BorderLayout)contentPanel.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
                        if (southComp instanceof JPanel) {
                            JPanel actionsPanelWrapper = (JPanel) southComp;
                            if (actionsPanelWrapper.getLayout() instanceof BorderLayout) {
                                Component buttonPanelComp = ((BorderLayout)actionsPanelWrapper.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                                if (buttonPanelComp instanceof JPanel) {
                                    JPanel buttonsPanel = (JPanel) buttonPanelComp;

                                    // Now we can safely look for our buttons in the buttons panel
                                    for (Component comp : buttonsPanel.getComponents()) {
                                        if (comp instanceof JButton) {
                                            JButton btn = (JButton) comp;
                                            String text = btn.getText();

                                            if (text.equals("Browse Available Plots")) {
                                                browsePlotsBtn = btn;
                                            } else if (text.equals("Your Land Plots")) {
                                                yourPlotsBtn = btn;
                                            } else if (text.equals("New Application")) {
                                                createAppBtn = btn;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Set the action listeners for each button if found
        if (browsePlotsBtn != null) {
            browsePlotsBtn.addActionListener(e -> new BrowsePlotsPage());
        }

        if (yourPlotsBtn != null) {
            yourPlotsBtn.addActionListener(e -> new YourPlotsPage());
        }

        if (createAppBtn != null) {
            createAppBtn.addActionListener(e -> openCreateApplicationDialog());
        }

        // Refresh button
        refreshButton.addActionListener(e -> {
            loadingLabel.setVisible(true);
            fetchUserApplications(userUid);
        });

        // Logout button
        logoutButton.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (option == JOptionPane.YES_OPTION) {
                // Hide this window instead of disposing it
                setVisible(false);

                // Create and show the login card
                SwingUtilities.invokeLater(() -> {
                    new LoginCard().setVisible(true);
                });

                // Now dispose this window after login is visible
                dispose();
            }
        });
    }

    private void createLoadingIndicator() {
        // Create a loading label - use text if GIF not available
        loadingLabel = new JLabel("Loading...");

        try {
            ImageIcon loadingIcon = new ImageIcon(getClass().getResource("/assets/loading.gif"));
            if (loadingIcon.getIconWidth() > 0) {
                loadingLabel = new JLabel(loadingIcon);
            }
        } catch (Exception e) {
            // GIF not found, continue with text label
            loadingLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        }

        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLabel.setVerticalAlignment(SwingConstants.CENTER);
        loadingLabel.setBounds(0, 0, getWidth(), getHeight());
        loadingLabel.setOpaque(true);
        loadingLabel.setBackground(new Color(255, 255, 255, 200)); // Semi-transparent white
        loadingLabel.setVisible(false);

        // Add to the layered pane correctly
        JLayeredPane layeredPane = getLayeredPane();
        layeredPane.add(loadingLabel, JLayeredPane.POPUP_LAYER);
        layeredPane.setLayer(loadingLabel, JLayeredPane.POPUP_LAYER);
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
                    String name = get();
                    if (name != null) {
                        profileButton.setText(name);
                    } else {
                        profileButton.setText("Unknown User");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    profileButton.setText("Error");
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
                        .whereEqualTo("uid", uid)
                        .get();

                QuerySnapshot querySnapshot = future.get();
                return querySnapshot.getDocuments();
            }

            @Override
            protected void done() {
                try {
                    List<QueryDocumentSnapshot> applications = get();

                    if (applications.isEmpty()) {
                        showNoApplicationsMessage();
                    }

                    // Prepare data for JTable with additional columns
                    Object[][] data = new Object[applications.size()][4];
                    for (int i = 0; i < applications.size(); i++) {
                        QueryDocumentSnapshot document = applications.get(i);
                        String requestId = document.getId().substring(0, 8).toUpperCase(); // First 8 chars of document ID
                        String requestTitle = document.getString("title");
                        String status = document.getString("status");
                        // If there's a timestamp field, use it, otherwise use a placeholder
                        String date = document.contains("timestamp") ?
                                document.getTimestamp("timestamp").toDate().toString().substring(0, 10) :
                                "N/A";

                        data[i][0] = requestId;
                        data[i][1] = requestTitle;
                        data[i][2] = status;
                        data[i][3] = date;
                    }

                    // Update the JTable with the fetched data
                    applicationsTable.setModel(new DefaultTableModel(
                            data,
                            new String[]{"Request ID", "Request Title", "Status", "Submission Date"}
                    ));

                    // Set column widths
                    TableColumnModel columnModel = applicationsTable.getColumnModel();
                    columnModel.getColumn(0).setPreferredWidth(100);  // ID column
                    columnModel.getColumn(1).setPreferredWidth(300);  // Title column
                    columnModel.getColumn(2).setPreferredWidth(120);  // Status column
                    columnModel.getColumn(3).setPreferredWidth(150);  // Date column

                    // Re-apply the status cell renderer
                    applicationsTable.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                            UserDashboard.this,
                            "Failed to load applications. Please try again later.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    refreshButton.setEnabled(true);
                    loadingLabel.setVisible(false);
                    revalidate();
                    repaint();
                }
            }
        };
        worker.execute();
    }

    private void showNoApplicationsMessage() {
        // Create a panel to display when no applications are found
        JPanel noAppsPanel = new JPanel(new BorderLayout());
        noAppsPanel.setBackground(Color.WHITE);

        JLabel noAppsLabel = new JLabel("No applications found. Create a new application to get started.", JLabel.CENTER);
        noAppsLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        noAppsLabel.setForeground(new Color(100, 100, 100));
        noAppsPanel.add(noAppsLabel, BorderLayout.CENTER);

        // Use the default table model with no data
        applicationsTable.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Request ID", "Request Title", "Status", "Submission Date"}
        ));
    }

    private void openCreateApplicationDialog() {
        // Create a styled dialog for new applications
        JDialog dialog = new JDialog(this, "Create New Application", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // Dialog header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_DARK);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel headerLabel = new JLabel("NEW APPLICATION");
        headerLabel.setFont(HEADER_FONT);
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Request Title
        JLabel titleLabel = new JLabel("Request Title:");
        titleLabel.setFont(NORMAL_FONT);
        JTextField requestField = new JTextField(20);
        requestField.setFont(NORMAL_FONT);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(titleLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        formPanel.add(requestField, gbc);

        // Description
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(NORMAL_FONT);
        JTextArea descriptionField = new JTextArea(5, 20);
        descriptionField.setFont(NORMAL_FONT);
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionField);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(descLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(scrollPane, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setBackground(HEADER_BG);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(BUTTON_FONT);
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton submitButton = new JButton("Submit");
        submitButton.setFont(BUTTON_FONT);
        submitButton.setBackground(PRIMARY_DARK);
        submitButton.setForeground(Color.WHITE);
        submitButton.addActionListener(e -> {
            String requestTitle = requestField.getText();
            String description = descriptionField.getText();

            if (requestTitle.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Both fields are required to create an application.",
                        "Missing Information",
                        JOptionPane.WARNING_MESSAGE
                );
            } else {
                FirebaseInitializer.createApplication(userUid, requestTitle, description);
                dialog.dispose();
                JOptionPane.showMessageDialog(
                        this,
                        "Your application has been submitted successfully!",
                        "Application Created",
                        JOptionPane.INFORMATION_MESSAGE
                );

                // Refresh after creating application
                loadingLabel.setVisible(true);
                fetchUserApplications(userUid);
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(submitButton);

        // Add all panels to dialog
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // Custom cell renderer for the Status column
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value != null) {
                String status = value.toString().toLowerCase();

                if (status.contains("approved")) {
                    c.setForeground(new Color(0, 128, 0)); // Green for approved
                } else if (status.contains("pending")) {
                    c.setForeground(new Color(205, 133, 0)); // Orange for pending
                } else if (status.contains("rejected") || status.contains("denied")) {
                    c.setForeground(ACCENT); // Red for rejected
                } else {
                    c.setForeground(TEXT_COLOR); // Default color
                }

                // Make status text bold
                setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize()));
            }

            return c;
        }
    }
}