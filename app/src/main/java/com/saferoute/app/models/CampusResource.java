package com.saferoute.app.models;

/** CampusResource — used by HelpActivity. Hardcoded campus contacts. */
public class CampusResource {
    private String  name;
    private String  distance;
    private String  hours;
    private String  phone;
    private String  accentColor;
    private boolean isOpen;

    public CampusResource(String name, String distance, String hours,
                          String phone, String accentColor, boolean isOpen) {
        this.name        = name;
        this.distance    = distance;
        this.hours       = hours;
        this.phone       = phone;
        this.accentColor = accentColor;
        this.isOpen      = isOpen;
    }

    public String  getName()        { return name; }
    public String  getDistance()    { return distance; }
    public String  getHours()       { return hours; }
    public String  getPhone()       { return phone; }
    public String  getAccentColor() { return accentColor; }
    public boolean isOpen()         { return isOpen; }
}
