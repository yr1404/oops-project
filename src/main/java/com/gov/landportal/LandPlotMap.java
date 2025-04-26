package com.gov.landportal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

public class LandPlotMap extends JPanel {

    static class Plot {
        Polygon polygon;
        String id;
        String owner;
        boolean isSellable;

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
    }

    static class PlotDetailsPanel extends JPanel {
        private JLabel plotIdLabel = new JLabel("Plot ID: ");
        private JLabel ownerLabel = new JLabel("Owner: ");
        private JLabel sellableLabel = new JLabel("Sellable: ");

        public PlotDetailsPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(Color.LIGHT_GRAY);
            setPreferredSize(new Dimension(250, 0));
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
    private PlotDetailsPanel detailsPanel;

    public LandPlotMap(PlotDetailsPanel detailsPanel) {
        this.detailsPanel = detailsPanel;

        FirebaseInitializer.init();
        loadPlotsFromFirebase();

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
                        detailsPanel.updatePlotDetails(plot.id, plot.owner, plot.isSellable);
                        break;
                    }
                }
                repaint();
            }
        });
    }

    private void loadPlotsFromFirebase() {
        Firestore db = FirebaseInitializer.getDB();
        ApiFuture<QuerySnapshot> future = db.collection("plots").get();

        try {
            List<QueryDocumentSnapshot> docs = future.get().getDocuments();
            for (QueryDocumentSnapshot doc : docs) {
                String id = doc.getString("id");
                String owner = doc.getString("owner");
                boolean isSellable = Boolean.TRUE.equals(doc.getBoolean("isSellable"));

                int[] xPoints = toIntArray(doc.getString("xPoints"));
                int[] yPoints = toIntArray(doc.getString("yPoints"));

                plots.add(new Plot(id, xPoints, yPoints, owner, isSellable));
            }
            repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int[] toIntArray(String csv) {
        if (csv == null || csv.isEmpty()) return new int[0];
        String cleaned = csv.replaceAll("[\\[\\]\\s]", ""); // remove brackets and whitespace
        return Arrays.stream(cleaned.split(","))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.BLACK);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));

        for (Plot plot : plots) {
            if (plot == selectedPlot) {
                g2.setColor(new Color(100, 149, 237));
            } else if (plot == hoveredPlot) {
                g2.setColor(new Color(255, 215, 0));
            } else {
                g2.setColor(new Color(255, 229, 180));
            }

            g2.fillPolygon(plot.polygon);
            g2.setColor(Color.BLACK);
            g2.drawPolygon(plot.polygon);

            // Ensure that plot.id is not null before using it
            String plotId = plot.id != null ? plot.id : "";
            Point center = plot.getCenter();
            g2.drawString(plotId, center.x - g2.getFontMetrics().stringWidth(plotId) / 2, center.y);
        }
    }


    public static void main(String[] args) {
        PlotDetailsPanel detailsPanel = new PlotDetailsPanel();
        JFrame frame = new JFrame("Land Plot Map");
        frame.setLayout(new BorderLayout());

        LandPlotMap panel = new LandPlotMap(detailsPanel);
        frame.add(panel, BorderLayout.CENTER);
        frame.add(detailsPanel, BorderLayout.EAST);

        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}