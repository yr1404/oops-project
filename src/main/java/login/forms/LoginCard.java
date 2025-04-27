package login.forms;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.UIScale;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class LoginCard extends JPanel {

    private JRadioButton officerRadioButton;
    private JRadioButton userRadioButton;
    private ButtonGroup userTypeGroup;

    public LoginCard() {
        init();
    }

    private void init() {
        setOpaque(false); // Make the panel transparent
        setLayout(new MigLayout("wrap,fillx,insets 45 45 50 45", "[fill]")); // MigLayout for flexible layout

        // Title Label
        JLabel title = new JLabel("Login to your account", SwingConstants.CENTER);
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");
        add(title, "span, align center, gapbottom 30");

        // Username Field
        JTextField txtUsername = new JTextField();
        txtUsername.putClientProperty(FlatClientProperties.STYLE,
                "margin:5,10,5,10;" +
                        "focusWidth:1;" +
                        "innerFocusWidth:0");
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your username");
        add(new JLabel("Username"), "gapy 20");
        add(txtUsername);

        // Password Field
        JPasswordField txtPassword = new JPasswordField();
        txtPassword.putClientProperty(FlatClientProperties.STYLE,
                "margin:5,10,5,10;" +
                        "focusWidth:1;" +
                        "innerFocusWidth:0;" +
                        "showRevealButton:true");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your password");
        add(new JLabel("Password"), "gapy 10");
        add(txtPassword);

        // User Type Toggle (Radio Buttons)
        JLabel lblUserType = new JLabel("Select User Type:");
        officerRadioButton = new JRadioButton("Officer");
        userRadioButton = new JRadioButton("User", true); // Default is "User"

        // Group the radio buttons to ensure only one is selected at a time
        userTypeGroup = new ButtonGroup();
        userTypeGroup.add(officerRadioButton);
        userTypeGroup.add(userRadioButton);

        JPanel userTypePanel = new JPanel();
        userTypePanel.setOpaque(false);
        userTypePanel.add(officerRadioButton);
        userTypePanel.add(userRadioButton);

        add(lblUserType, "gapy 10");
        add(userTypePanel);

        // Remember Me Checkbox
        JCheckBox chRememberMe = new JCheckBox("Remember me");
        add(chRememberMe);

        // Login Button
        JButton cmdLogin = new JButton("Login");
        cmdLogin.putClientProperty(FlatClientProperties.STYLE,
                "background:$Component.accentColor;" +
                        "borderWidth:0;" +
                        "focusWidth:0;" +
                        "innerFocusWidth:0");
        cmdLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));  // Change cursor on hover
        add(cmdLogin, "gapy 30");

        // Add MouseListener to button for hover effects
        cmdLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cmdLogin.setBackground(new Color(40, 120, 220)); // Darker blue on hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cmdLogin.setBackground(new Color(50, 150, 255)); // Original blue
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = UIScale.scale(20); // Rounded corner radius
        g2.setColor(getBackground());
        g2.setComposite(AlphaComposite.SrcOver.derive(0.5f)); // Semi-transparent effect
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arc, arc)); // Round the edges
        g2.dispose();
        super.paintComponent(g);
    }

    // Method to get selected user type
    public String getSelectedUserType() {
        if (officerRadioButton.isSelected()) {
            return "Officer";
        } else if (userRadioButton.isSelected()) {
            return "User";
        }
        return "User"; // Default return value
    }
}
