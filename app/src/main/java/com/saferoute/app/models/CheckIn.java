package com.saferoute.app.models;

/** CheckIn — Firebase Realtime Database "checkins" node. Used by CheckInActivity. */
public class CheckIn {
    private String id;
    private String userId;
    private String displayName;
    private String location;
    private long   timestamp;

    public CheckIn() {} // Required empty constructor

    public String getId()                   { return id; }
    public void   setId(String v)           { this.id = v; }
    public String getUserId()               { return userId; }
    public void   setUserId(String v)       { this.userId = v; }
    public String getDisplayName()          { return displayName; }
    public void   setDisplayName(String v)  { this.displayName = v; }
    public String getLocation()             { return location; }
    public void   setLocation(String v)     { this.location = v; }
    public long   getTimestamp()            { return timestamp; }
    public void   setTimestamp(long v)      { this.timestamp = v; }

    public String getRelativeTime() {
        if (timestamp == 0) return "Just now";
        long diffMs  = System.currentTimeMillis() - timestamp;
        long diffMin = diffMs / 60000;
        if (diffMin < 1)  return "Just now";
        if (diffMin < 60) return diffMin + " min ago";
        return (diffMin / 60) + " hr ago";
    }
}
