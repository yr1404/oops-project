package login.forms;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.firebase.auth.UserRecord;
import com.gov.landportal.FirebaseInitializer;
import login.officer.OfficerDashboard;
import login.user.UserDashboard;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginCard extends JPanel {

    private JRadioButton officerRadioButton;
    private JRadioButton userRadioButton;
    private ButtonGroup userTypeGroup;
    private boolean isLogin = true;

    private JLayeredPane layeredPane;
    private JPanel formPanel;
    private JLabel loadingLabel;

    // Declare txtFullName here, so it's available across the methods
    private JTextField txtFullName;

    public LoginCard() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        setOpaque(false);

        layeredPane = new JLayeredPane();
        add(layeredPane, BorderLayout.CENTER);

        formPanel = new JPanel(new MigLayout("wrap,fillx,insets 45 45 50 45", "[fill]"));
        formPanel.setOpaque(false);
        layeredPane.add(formPanel, JLayeredPane.DEFAULT_LAYER);

        loadingLabel = new JLabel(new ImageIcon("assets/loading.gif"));
        loadingLabel.setOpaque(false);
        loadingLabel.setVisible(false);
        layeredPane.add(loadingLabel, JLayeredPane.PALETTE_LAYER);

        buildForm();

        // Initial bounds
        formPanel.setBounds(0, 0, 500, 600);
        layeredPane.setBounds(0, 0, 500, 600);
        updateLoadingPosition();

        // Resize handling
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layeredPane.setBounds(0, 0, getWidth(), getHeight());
                formPanel.setBounds(0, 0, getWidth(), getHeight());
                updateLoadingPosition();
            }
        });
    }

    private void buildForm() {
        formPanel.removeAll();

        JLabel title = new JLabel(isLogin ? "Login to your account" : "Register a new account", SwingConstants.CENTER);
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");
        formPanel.add(title, "span, align center, gapbottom 30");

        JTextField txtEmail = new JTextField();
        txtEmail.putClientProperty(FlatClientProperties.STYLE,
                "margin:5,10,5,10;" +
                        "focusWidth:1;" +
                        "innerFocusWidth:0");
        txtEmail.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your email");
        formPanel.add(new JLabel("Email"), "gapy 20");
        formPanel.add(txtEmail);

        JPasswordField txtPassword = new JPasswordField();
        txtPassword.putClientProperty(FlatClientProperties.STYLE,
                "margin:5,10,5,10;" +
                        "focusWidth:1;" +
                        "innerFocusWidth:0;" +
                        "showRevealButton:true");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your password");
        formPanel.add(new JLabel("Password"), "gapy 10");
        formPanel.add(txtPassword);

        // Register page: Add Full Name field
        if (!isLogin) {
            // Initialize txtFullName when registering
            txtFullName = new JTextField();
            txtFullName.putClientProperty(FlatClientProperties.STYLE,
                    "margin:5,10,5,10;" +
                            "focusWidth:1;" +
                            "innerFocusWidth:0");
            txtFullName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your full name");
            formPanel.add(new JLabel("Full Name"), "gapy 10");
            formPanel.add(txtFullName);
        }

        JLabel lblUserType = new JLabel("Select User Type:");
        officerRadioButton = new JRadioButton("Officer");
        userRadioButton = new JRadioButton("User", true);

        userTypeGroup = new ButtonGroup();
        userTypeGroup.add(officerRadioButton);
        userTypeGroup.add(userRadioButton);

        JPanel userTypePanel = new JPanel();
        userTypePanel.setOpaque(false);
        userTypePanel.add(officerRadioButton);
        userTypePanel.add(userRadioButton);

        formPanel.add(lblUserType, "gapy 10");
        formPanel.add(userTypePanel);

        JCheckBox chRememberMe = new JCheckBox("Remember me");
        formPanel.add(chRememberMe);

        JButton cmdSubmit = new JButton(isLogin ? "Login" : "Register");
        cmdSubmit.putClientProperty(FlatClientProperties.STYLE,
                "background:$Component.accentColor;" +
                        "borderWidth:0;" +
                        "focusWidth:0;" +
                        "innerFocusWidth:0");
        cmdSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formPanel.add(cmdSubmit, "gapy 30");

        cmdSubmit.addActionListener(e -> {
            String email = txtEmail.getText();
            String password = new String(txtPassword.getPassword());
            String selectedUserType = getSelectedUserType();
            String fullName;

            // If it's Register mode, get the full name
            if (!isLogin) {
                fullName = txtFullName.getText();
                if (fullName == null || fullName.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(LoginCard.this, "Full name is required for registration.");
                    return;
                }
            } else {
                fullName = null;
            }

            // Show loading
            showLoadingIndicator();

            // Execute in background
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        if (isLogin) {
                            UserRecord userRecord = FirebaseInitializer.authenticateUser(email, password);
                            if (userRecord != null) {
                                String role = FirebaseInitializer.getUserRole(userRecord.getUid());
                                if (role.equals(selectedUserType)) {
                                    if (role.equals("Officer")) {
                                        openOfficerDashboard(userRecord.getUid());
                                    } else {
                                        openUserDashboard(userRecord.getUid());
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(LoginCard.this, "Selected wrong user type!");
                                }
                            } else {
                                JOptionPane.showMessageDialog(LoginCard.this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            FirebaseInitializer.registerUser(email, password, fullName, selectedUserType);
                            JOptionPane.showMessageDialog(LoginCard.this, "User registered successfully!");
                            isLogin = true;
                            updateView();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(LoginCard.this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    hideLoadingIndicator();
                }
            }.execute();
        });

        cmdSubmit.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cmdSubmit.setBackground(new Color(40, 120, 220));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cmdSubmit.setBackground(new Color(50, 150, 255));
            }
        });

        JButton toggleButton = new JButton(isLogin ? "Don't have an account? Register here!" : "Already have an account? Login here!");
        toggleButton.addActionListener(e -> {
            isLogin = !isLogin;
            updateView();
        });
        formPanel.add(toggleButton, "span, align center, gapy 20");

        formPanel.revalidate();
        formPanel.repaint();
    }

    private void showLoadingIndicator() {
        loadingLabel.setVisible(true);
        loadingLabel.setSize(100, 100);  // Set size of loading gif
        updateLoadingPosition();
        loadingLabel.repaint();
    }

    private void hideLoadingIndicator() {
        loadingLabel.setVisible(false);
    }

    private void updateLoadingPosition() {
        if (loadingLabel != null && getWidth() > 0 && getHeight() > 0) {
            loadingLabel.setLocation(getWidth() / 2 - loadingLabel.getWidth() / 2,
                    getHeight() / 2 - loadingLabel.getHeight() / 2);
        }
    }

    private void updateView() {
        buildForm();
    }

    public String getSelectedUserType() {
        if (officerRadioButton.isSelected()) {
            return "Officer";
        } else {
            return "User";
        }
    }

    private void openOfficerDashboard(String uid) {
        SwingUtilities.invokeLater(() -> {
            JFrame mainFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            mainFrame.dispose();
            new OfficerDashboard(uid).setVisible(true);
        });
    }

    private void openUserDashboard(String uid) {
        SwingUtilities.invokeLater(() -> {
            JFrame mainFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            mainFrame.dispose();
            new UserDashboard(uid).setVisible(true);
        });
    }
}
