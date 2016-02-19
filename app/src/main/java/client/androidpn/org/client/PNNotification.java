package client.androidpn.org.client;

/**
 * Created by daktak on 2/20/16.
 */
public class PNNotification {
    private long id;
    private String title;
    private String message;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return title+"\n"+message;
    }
}