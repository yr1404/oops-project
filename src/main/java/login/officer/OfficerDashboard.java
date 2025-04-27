package login.officer;

import com.gov.landportal.FirebaseInitializer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class OfficerDashboard extends JFrame {

    private JLabel nameLabel;
    private String uid;

    public OfficerDashboard(String uid) {
        setTitle("Pending Applications");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        this.uid = uid;

        // Custom gradient background panel
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(240, 248, 255),
                        0, getHeight(), new Color(224, 255, 255));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        add(backgroundPanel);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel heading = new JLabel("Pending Applications", JLabel.LEFT);
        heading.setFont(new Font("Poppins", Font.BOLD, 26));
        heading.setForeground(new Color(25, 25, 112));
        heading.setBorder(BorderFactory.createEmptyBorder(30, 10, 20, 10));
        topPanel.add(heading, BorderLayout.WEST);

        nameLabel = new JLabel("Loading...", JLabel.RIGHT);
        nameLabel.setFont(new Font("Poppins", Font.PLAIN, 18));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(30, 10, 20, 10));
        nameLabel.setForeground(new Color(25, 25, 112));
        topPanel.add(nameLabel, BorderLayout.EAST);

        backgroundPanel.add(topPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridLayout(0, 1, 25, 25)); // Dynamic rows based on applications
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 70, 50, 70));

        backgroundPanel.add(buttonPanel, BorderLayout.CENTER);

        // Fetch the officer's name after UI is visible
        fetchOfficerName();

        // Fetch pending applications from Firebase and display them
        fetchPendingApplications(buttonPanel);

        setVisible(true);
    }

    private void fetchOfficerName() {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return FirebaseInitializer.getUserName(uid); // Assuming this is a method that fetches the logged-in officer's name
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

    private void fetchPendingApplications(JPanel buttonPanel) {
        // 1. Create and add loading label
        JLabel loadingLabel = new JLabel(new ImageIcon("assets/loading.gif"), JLabel.CENTER);
        buttonPanel.add(loadingLabel);
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
                    // 2. Remove loading label once data is fetched
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
                        buttonPanel.add(new JLabel("No pending applications"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    buttonPanel.removeAll();
                    buttonPanel.add(new JLabel("Error loading applications"));
                }
                revalidate();
                repaint();
            }
        };
        worker.execute();
    }


    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(173, 216, 230)); // Light Blue
        button.setForeground(new Color(25, 25, 112)); // Deep Blue text
        button.setFocusPainted(false);
        button.setFont(new Font("Poppins", Font.BOLD, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237), 2, true),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        button.setOpaque(true);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(135, 206, 250)); // Slightly darker blue
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(173, 216, 230)); // Original color
            }
        });

        return button;
    }
}
