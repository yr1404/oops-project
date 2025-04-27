package login.forms;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class Home extends JPanel {
    private MediaPlayerFactory factory;
    private EmbeddedMediaPlayer mediaPlayer;
    private Canvas canvas;
    private LoginCard loginCard;
    private JLayeredPane layeredPane;

    public Home() {
        init();
    }

    public void init() {
        factory = new MediaPlayerFactory();
        mediaPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer();

        // Create the layered pane (without BorderLayout)
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);  // Use null layout for absolute positioning

        // Create the video canvas
        canvas = new Canvas();
        canvas.setBackground(Color.BLACK);
        canvas.setBounds(0, 0, 1920, 1080); // Set the canvas size for the video
        layeredPane.add(canvas, Integer.valueOf(0)); // Add video canvas to the background layer

        // Set video surface
        mediaPlayer.videoSurface().set(factory.videoSurfaces().newVideoSurface(canvas));

        // Create the login card (overlay) with increased width and height
        loginCard = new LoginCard();
        int loginCardWidth = 400;  // New width for login card
        int loginCardHeight = 250;  // New height for login card

        // Add the login card to the layered pane (in front of the video)
        layeredPane.add(loginCard, Integer.valueOf(1));

        // Set the layered pane as the content pane
        setLayout(null);  // Remove BorderLayout and set null layout for Home panel
        add(layeredPane);

        // Resize the layered pane according to the window size
        layeredPane.setBounds(0, 0, getWidth(), getHeight());  // Set size of layered pane to fit the window

        // Add a component listener to handle resizing of the panel and center the login card
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                centerLoginCard();
            }
        });
    }

    // Method to center the login card
    private void centerLoginCard() {
        int loginCardWidth = 400;  // Width of the login card
        int loginCardHeight = 420;  // Height of the login card

        // Center the login card based on the panel's width and height
        int x = (getWidth() - loginCardWidth) / 2;
        int y = (getHeight() - loginCardHeight) / 2;

        loginCard.setBounds(x, y, loginCardWidth, loginCardHeight); // Set centered position with new size
    }

    public void play() {
        if (mediaPlayer.status().isPlaying()) {
            mediaPlayer.controls().stop();
        }
        mediaPlayer.media().play("video/video 1.mp4"); // Replace with your actual video path
    }

    public void stop() {
        mediaPlayer.controls().stop();
        mediaPlayer.release();
        factory.release();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Ensure the layeredPane resizes with the Home panel
        layeredPane.setBounds(0, 0, getWidth(), getHeight());
    }
}
