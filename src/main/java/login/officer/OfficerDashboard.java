package login.officer;

import com.gov.landportal.FirebaseInitializer;
import login.forms.LoginCard;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class OfficerDashboard extends JFrame {

    private JLabel nameLabel;
    private String uid;
    private JButton logoutButton;
    private JButton refreshButton;
    private JPanel buttonPanel;
    private JLabel loadingLabel;

    // Government color scheme (matching UserDashboard)
    private final Color PRIMARY_DARK = new Color(0, 51, 102);     // Dark blue
    private final Color PRIMARY_LIGHT = new Color(214, 232, 248); // Light blue background
    private final Color ACCENT = new Color(180, 24, 24);          // Dark red for important elements
    private final Color TEXT_COLOR = new Color(33, 33, 33);       // Nearly black text
    private final Color HEADER_BG = new Color(240, 240, 240);     // Light gray for headers

    // Fonts
    private final Font HEADER_FONT = new Font("Serif", Font.BOLD, 18);
    private final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 14);
    private final Font NORMAL_FONT = new Font("SansSerif", Font.PLAIN, 14);

    public OfficerDashboard(String uid) {
        setTitle("Government Land Portal - Officer Dashboard");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        this.uid = uid;

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

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Fetch the officer's name after UI is visible
        fetchOfficerName();

        // Fetch pending applications from Firebase and display them
        fetchPendingApplications();

        // Setup button actions
        setupButtonActions();

        setVisible(true);
    }

    private void createHeaderPanel(JPanel mainPanel) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_DARK);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Government logo/title
        JLabel govLogo = new JLabel("GOVERNMENT LAND PORTAL - OFFICER");
        govLogo.setFont(new Font("Serif", Font.BOLD, 22));
        govLogo.setForeground(Color.WHITE);

        // User controls panel (right side of header)
        JPanel userControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userControls.setOpaque(false);

        nameLabel = new JLabel("Loading...");
        nameLabel.setFont(BUTTON_FONT);
        nameLabel.setForeground(Color.WHITE);

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

        userControls.add(nameLabel);
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

        JLabel appLabel = new JLabel("PENDING APPLICATIONS");
        appLabel.setFont(HEADER_FONT);
        appLabel.setForeground(PRIMARY_DARK);
        sectionHeader.add(appLabel, BorderLayout.WEST);

        // Button panel for applications
        buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridLayout(0, 1, 10, 10)); // Dynamic rows based on applications
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(buttonPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(PRIMARY_DARK));

        applicationPanel.add(sectionHeader, BorderLayout.NORTH);
        applicationPanel.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(applicationPanel, BorderLayout.CENTER);
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

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Color.WHITE);
        button.setForeground(PRIMARY_DARK);
        button.setFocusPainted(false);
        button.setFont(BUTTON_FONT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_DARK, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

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

    private void setupButtonActions() {
        // Refresh button
        refreshButton.addActionListener(e -> {
            loadingLabel.setVisible(true);
            fetchPendingApplications();
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

    private void fetchOfficerName() {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return FirebaseInitializer.getUserName(uid);
            }

            @Override
            protected void done() {
                try {
                    String name = get();
                    if (name != null) {
                        nameLabel.setText(name);
                    } else {
                        nameLabel.setText("Unknown Officer");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    nameLabel.setText("Error");
                }
            }
        };
        worker.execute();
    }

    private void fetchPendingApplications() {
        // Show loading indicator
        loadingLabel.setVisible(true);

        // Clear existing buttons
        buttonPanel.removeAll();

        // Add initial loading indicator to the panel
        JLabel panelLoadingLabel = new JLabel("Loading applications...", JLabel.CENTER);
        panelLoadingLabel.setFont(NORMAL_FONT);
        buttonPanel.add(panelLoadingLabel);
        revalidate();
        repaint();

        SwingWorker<List<Application>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Application> doInBackground() throws Exception {
                return FirebaseInitializer.getPendingApplications();
            }

            @Override
            protected void done() {
                try {
                    // Remove loading label once data is fetched
                    buttonPanel.removeAll();

                    List<Application> applications = get();
                    if (applications != null && !applications.isEmpty()) {
                        for (Application app : applications) {
                            JButton appButton = createStyledButton(app.getTitle());
                            appButton.addActionListener(e -> {
                                new ApplicationDetailsPage(app);
                            });
                            buttonPanel.add(appButton);
                        }
                    } else {
                        JLabel noAppsLabel = new JLabel("No pending applications", JLabel.CENTER);
                        noAppsLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
                        noAppsLabel.setForeground(new Color(100, 100, 100));
                        buttonPanel.add(noAppsLabel);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    buttonPanel.removeAll();
                    JLabel errorLabel = new JLabel("Error loading applications", JLabel.CENTER);
                    errorLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
                    errorLabel.setForeground(ACCENT);
                    buttonPanel.add(errorLabel);
                } finally {
                    loadingLabel.setVisible(false);
                    revalidate();
                    repaint();
                }
            }
        };
        worker.execute();
    }
}