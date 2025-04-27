package login.officer;

import com.gov.landportal.FirebaseInitializer;

import javax.swing.*;
import java.awt.*;

public class ApplicationDetailsPage extends JFrame {

    private Application application;

    public ApplicationDetailsPage(Application application) {
        this.application = application;  // Store the passed application

        setTitle("Application Details");
        setSize(350, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 1, 5, 5));

        // Displaying the details dynamically
        JLabel nameLabel = new JLabel("Title: " + application.getTitle());
        JLabel plotNoLabel = new JLabel("Description: " + application.getDescription());
        JLabel otherDetailsLabel = new JLabel("Status: " + application.getStatus());

        JPanel buttonPanel = getJPanel();

        // Adding components to the frame
        add(new JLabel("Application Details", JLabel.CENTER));
        add(nameLabel);
        add(plotNoLabel);
        add(otherDetailsLabel);
        add(new JLabel("")); // Empty space
        add(buttonPanel);

        setVisible(true);
    }

    private JPanel getJPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton approveButton = new JButton("Approve");
        JButton rejectButton = new JButton("Reject");

        // Action listeners for approve and reject buttons
        approveButton.addActionListener(e -> {
            // Handle the approve logic here (e.g., update application status to "Approved")
            FirebaseInitializer.updateApplicationStatus(application.getUid(), "Approved");
            JOptionPane.showMessageDialog(this, "Application Approved!");
            dispose(); // Close the dialog after approval
        });

        rejectButton.addActionListener(e -> {
            // Handle the reject logic here (e.g., update application status to "Rejected")
            FirebaseInitializer.updateApplicationStatus(application.getUid(), "Rejected");
            JOptionPane.showMessageDialog(this, "Application Rejected!");
            dispose(); // Close the dialog after rejection
        });

        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        return buttonPanel;
    }
}
