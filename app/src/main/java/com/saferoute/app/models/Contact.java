package com.saferoute.app.models;

/** Contact — stored in SQLite emergency_contacts table. Used by SosActivity. */
public class Contact {
    private int    id;
    private String name;
    private String phone;

    public Contact() {}
    public Contact(String name, String phone) { this.name = name; this.phone = phone; }

    public int    getId()            { return id; }
    public void   setId(int id)      { this.id = id; }
    public String getName()          { return name; }
    public void   setName(String v)  { this.name = v; }
    public String getPhone()         { return phone; }
    public void   setPhone(String v) { this.phone = v; }
}
