package com.saferoute.app.models;

/**
 * Alert model — maps to Firebase Realtime Database "incidents" node.
 * Realtime Database stores plain objects (no Timestamp type like Firestore),
 * so we use a Long (server time millis) instead.
 */
public class Alert {
    private String id;
    private String userId;
    private String description;
    private String location;
    private String type;       // "ALERT", "INFO", "SAFE"
    private boolean verified;
    private double latitude;
    private double longitude;
    private long   timestamp;  // milliseconds since epoch

    public Alert() {} // Required empty constructor for Realtime Database deserialization

    public Alert(String description, String location, String type) {
        this.description = description;
        this.location    = location;
        this.type        = type;
        this.verified     = false;
        this.timestamp    = System.currentTimeMillis();
    }

    public String getId()                  { return id; }
    public void   setId(String id)         { this.id = id; }
    public String getUserId()              { return userId; }
    public void   setUserId(String v)      { this.userId = v; }
    public String getDescription()         { return description; }
    public void   setDescription(String v) { this.description = v; }
    public String getLocation()            { return location; }
    public void   setLocation(String v)    { this.location = v; }
    public String getType()                { return type; }
    public void   setType(String v)        { this.type = v; }
    public boolean isVerified()            { return verified; }
    public void   setVerified(boolean v)   { this.verified = v; }
    public double getLatitude()            { return latitude; }
    public void   setLatitude(double v)    { this.latitude = v; }
    public double getLongitude()           { return longitude; }
    public void   setLongitude(double v)   { this.longitude = v; }
    public long   getTimestamp()           { return timestamp; }
    public void   setTimestamp(long v)     { this.timestamp = v; }

    /** Returns a human-readable relative time string */
    public String getRelativeTime() {
        if (timestamp == 0) return "Just now";
        long diffMs  = System.currentTimeMillis() - timestamp;
        long diffMin = diffMs / 60000;
        if (diffMin < 1)  return "Just now";
        if (diffMin < 60) return diffMin + " min ago";
        long diffHr = diffMin / 60;
        if (diffHr  < 24) return diffHr + " hr ago";
        return (diffHr / 24) + " days ago";
    }
}
