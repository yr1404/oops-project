package login;

import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.gov.landportal.FirebaseInitializer;
import login.forms.Home;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main extends JFrame {

    private Home home;

    public Main() {
        // Initialize Firebase here before other components
        FirebaseInitializer.init();
        init();
    }

    private void init() {
        // Window Setup
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1535, 900));  // Ensure this matches your video resolution or app window needs
        setMinimumSize(new Dimension(500, 700));
        setLocationRelativeTo(null);  // Center the window
        home = new Home();
        setContentPane(home);

        // WindowListener to control home play/stop when the window is opened/closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                home.play();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                home.stop();
            }
        });
    }

    public static void main(String[] args) {
        // Set up FlatLaf UI Look and Feel
        FlatRobotoFont.install();
        FlatMacDarkLaf.setup();
        UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));

        // Run the Main GUI in the Event Dispatch Thread
        EventQueue.invokeLater(() -> new Main().setVisible(true));
    }
}
