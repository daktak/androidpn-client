package org.androidpn.client;

/**
 * Created by daktak on 2/20/16.
 */
public class PNNotification {
    private long id;
    private String title;
    private String message;
    private String uri;
    private String dttm;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getUri() {
        return uri;
    }

    public String getDttm() { return dttm; }

    public void setUri(String uri){
        this.uri = uri;
    }

    public void setDttm(String dttm) { this.dttm = dttm; }

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