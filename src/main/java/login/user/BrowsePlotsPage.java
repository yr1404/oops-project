package login.user;

import javax.swing.*;
import java.awt.*;

public class BrowsePlotsPage extends JFrame {

    public BrowsePlotsPage() {
        setTitle("Browse Plots");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        // Dummy search filters
        panel.add(new JLabel("Location:"));
        panel.add(new JTextField());

        panel.add(new JLabel("Plot Size:"));
        panel.add(new JTextField());

        panel.add(new JLabel("Owner Name:"));
        panel.add(new JTextField());

        JButton searchButton = new JButton("Search");
        panel.add(searchButton);

        add(panel);

        setVisible(true);
    }
}
