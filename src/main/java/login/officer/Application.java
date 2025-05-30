package login.officer;

public class Application {

    private String id;            // Firestore Document ID (application ID)
    private String title;         // The title of the application
    private String description;   // A description of the application
    private String status;        // Status of the application (e.g., "Pending")
    private String uid;           // User ID who created this application
    private String plotId;        // ID of the plot this application refers to

    public Application() {
    }

    public Application(String title, String description, String status, String uid) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.uid = uid;
    }

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPlotId() {
        return plotId;
    }

    public void setPlotId(String plotId) {
        this.plotId = plotId;
    }

    @Override
    public String toString() {
        return "Application{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", uid='" + uid + '\'' +
                ", plotId='" + plotId + '\'' +
                '}';
    }
}
