package com.saferoute.app.models;

/** SafePath — stored in SQLite safe_paths table. Used by MapActivity. */
public class SafePath {
    private int    id;
    private String label;
    private double latStart, lngStart, latEnd, lngEnd;

    public SafePath() {}
    public SafePath(String label, double latStart, double lngStart, double latEnd, double lngEnd) {
        this.label    = label;
        this.latStart = latStart;
        this.lngStart = lngStart;
        this.latEnd   = latEnd;
        this.lngEnd   = lngEnd;
    }

    public int    getId()               { return id; }
    public void   setId(int id)         { this.id = id; }
    public String getLabel()            { return label; }
    public void   setLabel(String v)    { this.label = v; }
    public double getLatStart()         { return latStart; }
    public void   setLatStart(double v) { this.latStart = v; }
    public double getLngStart()         { return lngStart; }
    public void   setLngStart(double v) { this.lngStart = v; }
    public double getLatEnd()           { return latEnd; }
    public void   setLatEnd(double v)   { this.latEnd = v; }
    public double getLngEnd()           { return lngEnd; }
    public void   setLngEnd(double v)   { this.lngEnd = v; }
}
