package com.gov.landportal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

public class LandPlotMap extends JPanel {

    static class Plot {
        Polygon polygon;
        String id;
        String owner;
        boolean isSellable; // true or false

        public Plot(String id, int[] xPoints, int[] yPoints, String owner, boolean isSellable) {
            this.id = id;
            this.polygon = new Polygon(xPoints, yPoints, xPoints.length);
            this.owner = owner;
            this.isSellable = isSellable;
        }

        public boolean contains(Point p) {
            return polygon.contains(p);
        }

        public Point getCenter() {
            Rectangle bounds = polygon.getBounds();
            return new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
        }

        public String getOwner() {
            return owner;
        }

        public boolean isSellable() {
            return isSellable;
        }
    }

    // PlotDetailsPanel to display plot information in the side panel
    static class PlotDetailsPanel extends JPanel {
        private JLabel plotIdLabel;
        private JLabel ownerLabel;
        private JLabel sellableLabel;

        public PlotDetailsPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(Color.LIGHT_GRAY);
            setPreferredSize(new Dimension(250, getHeight()));

            plotIdLabel = new JLabel("Plot ID: ");
            ownerLabel = new JLabel("Owner: ");
            sellableLabel = new JLabel("Sellable: ");

            add(plotIdLabel);
            add(ownerLabel);
            add(sellableLabel);
        }

        public void updatePlotDetails(String id, String owner, boolean isSellable) {
            plotIdLabel.setText("Plot ID: " + id);
            ownerLabel.setText("Owner: " + owner);
            sellableLabel.setText("Sellable: " + (isSellable ? "Yes" : "No"));
        }
    }

    private List<Plot> plots = new ArrayList<>();
    private Plot hoveredPlot = null;
    private Plot selectedPlot = null;
    @SuppressWarnings("unused")
    private PlotDetailsPanel detailsPanel;

    public LandPlotMap(PlotDetailsPanel detailsPanel) {
        this.detailsPanel = detailsPanel;

        FirebaseInitializer.init();

        // Sample plots — replace with DB-loaded data
        plots.add(new Plot("725", new int[]{300, 400, 420, 340}, new int[]{100, 100, 200, 200}, "John Doe", true));
        plots.add(new Plot("724", new int[]{400, 500, 520, 420}, new int[]{100, 100, 200, 200}, "Jane Smith", false));
        plots.add(new Plot("745", new int[]{340, 420, 440, 360}, new int[]{200, 200, 300, 300}, "Robert Brown", true));

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                hoveredPlot = null;
                for (Plot plot : plots) {
                    if (plot.contains(e.getPoint())) {
                        hoveredPlot = plot;
                        break;
                    }
                }
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                for (Plot plot : plots) {
                    if (plot.contains(e.getPoint())) {
                        selectedPlot = plot;
                        // Update the side panel with plot details
                        detailsPanel.updatePlotDetails(plot.id, plot.getOwner(), plot.isSellable());
                        break;
                    }
                }
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.BLACK);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));

        for (Plot plot : plots) {
            if (plot == selectedPlot) {
                g2.setColor(new Color(100, 149, 237)); // Light Blue for selected
            } else if (plot == hoveredPlot) {
                g2.setColor(new Color(255, 215, 0)); // Yellow for hovered
            } else {
                g2.setColor(new Color(255, 229, 180)); // Light Beige for normal
            }

            g2.fillPolygon(plot.polygon);
            g2.setColor(Color.BLACK);
            g2.drawPolygon(plot.polygon);

            // Draw plot number
            FontMetrics fm = g2.getFontMetrics();
            Point center = plot.getCenter();
            int textWidth = fm.stringWidth(plot.id);
            g2.drawString(plot.id, center.x - textWidth / 2, center.y);
        }
    }

    public static void main(String[] args) {
        // Create the plot details panel (side panel)
        PlotDetailsPanel detailsPanel = new PlotDetailsPanel();

        // Create the main frame
        JFrame frame = new JFrame("Land Plot Map");
        frame.setLayout(new BorderLayout());

        // Create the main map panel
        LandPlotMap panel = new LandPlotMap(detailsPanel);
        frame.add(panel, BorderLayout.CENTER);

        // Add the plot details side panel
        frame.add(detailsPanel, BorderLayout.EAST);

        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}