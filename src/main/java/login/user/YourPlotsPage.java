package login.user;

import javax.swing.*;
import java.awt.*;

public class YourPlotsPage extends JFrame {

    public YourPlotsPage() {
        setTitle("Your Plots");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Plot Details:"));
        panel.add(new JTextArea(5, 30));

        panel.add(new JLabel("Map Link:"));
        panel.add(new JTextField(30));

        panel.add(new JLabel("Other Details:"));
        panel.add(new JTextArea(5, 30));

        add(panel);

        setVisible(true);
    }
}
