package com.saferoute.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.saferoute.app.models.Contact;
import com.saferoute.app.models.SafePath;

import java.util.ArrayList;
import java.util.List;

/**
 * SafeRouteDatabase — SQLiteOpenHelper
 * Manages two local tables:
 *   1. emergency_contacts — personal, offline, used by SOS
 *   2. safe_paths         — pre-seeded safe route coordinates for the map
 *
 * Everything here works offline — no internet required.
 * Member 3 owns this class.
 */
public class SafeRouteDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME    = "saferoute.db";
    private static final int    DB_VERSION = 1;

    // Table: emergency_contacts
    public static final String TABLE_CONTACTS    = "emergency_contacts";
    public static final String COL_ID            = "id";
    public static final String COL_NAME          = "name";
    public static final String COL_PHONE         = "phone";

    // Table: safe_paths
    public static final String TABLE_PATHS       = "safe_paths";
    public static final String COL_PATH_LABEL    = "label";
    public static final String COL_LAT_START     = "lat_start";
    public static final String COL_LNG_START     = "lng_start";
    public static final String COL_LAT_END       = "lat_end";
    public static final String COL_LNG_END       = "lng_end";

    public SafeRouteDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create emergency_contacts table
        db.execSQL("CREATE TABLE " + TABLE_CONTACTS + " ("
            + COL_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_NAME  + " TEXT NOT NULL, "
            + COL_PHONE + " TEXT NOT NULL"
            + ")");

        // Create safe_paths table
        db.execSQL("CREATE TABLE " + TABLE_PATHS + " ("
            + COL_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_PATH_LABEL + " TEXT NOT NULL, "
            + COL_LAT_START  + " REAL NOT NULL, "
            + COL_LNG_START  + " REAL NOT NULL, "
            + COL_LAT_END    + " REAL NOT NULL, "
            + COL_LNG_END    + " REAL NOT NULL"
            + ")");

        // Seed pre-loaded safe paths around Taylor's University campus
        // Update these coordinates to match the actual campus layout
        seedSafePaths(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATHS);
        onCreate(db);
    }

    /**
     * Pre-load known well-lit safe paths around Taylor's University.
     * These are inserted once on first app launch.
     * The team should survey the campus and update these coordinates.
     */
    private void seedSafePaths(SQLiteDatabase db) {
        insertPath(db, "Main Gate to Block A",       3.0690, 101.6020, 3.0675, 101.6010);
        insertPath(db, "Library to Student Hub",     3.0672, 101.6007, 3.0668, 101.6015);
        insertPath(db, "Block B to Car Park A",      3.0665, 101.6000, 3.0660, 101.5995);
        insertPath(db, "Medical Centre to Main Gate",3.0662, 101.6005, 3.0690, 101.6020);
        insertPath(db, "Sports Complex to Block A",  3.0658, 101.6012, 3.0675, 101.6010);
    }

    private void insertPath(SQLiteDatabase db, String label,
                            double latStart, double lngStart,
                            double latEnd,   double lngEnd) {
        ContentValues cv = new ContentValues();
        cv.put(COL_PATH_LABEL, label);
        cv.put(COL_LAT_START,  latStart);
        cv.put(COL_LNG_START,  lngStart);
        cv.put(COL_LAT_END,    latEnd);
        cv.put(COL_LNG_END,    lngEnd);
        db.insert(TABLE_PATHS, null, cv);
    }

    // ── Emergency Contacts CRUD ───────────────────────────────────────────────

    public long insertContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv  = new ContentValues();
        cv.put(COL_NAME,  contact.getName());
        cv.put(COL_PHONE, contact.getPhone());
        return db.insert(TABLE_CONTACTS, null, cv);
    }

    public List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS, null, null, null, null, null, COL_NAME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                int idIndex    = cursor.getColumnIndex(COL_ID);
                int nameIndex  = cursor.getColumnIndex(COL_NAME);
                int phoneIndex = cursor.getColumnIndex(COL_PHONE);

                Contact c = new Contact(
                    cursor.getString(nameIndex),
                    cursor.getString(phoneIndex)
                );
                c.setId(cursor.getInt(idIndex));
                contacts.add(c);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return contacts;
    }

    public int deleteContact(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_CONTACTS, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // ── Safe Paths ────────────────────────────────────────────────────────────

    public List<SafePath> getAllSafePaths() {
        List<SafePath> paths = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PATHS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int idIdx    = cursor.getColumnIndex(COL_ID);
                int lblIdx   = cursor.getColumnIndex(COL_PATH_LABEL);
                int latSIdx  = cursor.getColumnIndex(COL_LAT_START);
                int lngSIdx  = cursor.getColumnIndex(COL_LNG_START);
                int latEIdx  = cursor.getColumnIndex(COL_LAT_END);
                int lngEIdx  = cursor.getColumnIndex(COL_LNG_END);

                SafePath p = new SafePath(
                    cursor.getString(lblIdx),
                    cursor.getDouble(latSIdx), cursor.getDouble(lngSIdx),
                    cursor.getDouble(latEIdx), cursor.getDouble(lngEIdx)
                );
                p.setId(cursor.getInt(idIdx));
                paths.add(p);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return paths;
    }

    public long insertSafePath(SafePath path) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv  = new ContentValues();
        cv.put(COL_PATH_LABEL, path.getLabel());
        cv.put(COL_LAT_START,  path.getLatStart());
        cv.put(COL_LNG_START,  path.getLngStart());
        cv.put(COL_LAT_END,    path.getLatEnd());
        cv.put(COL_LNG_END,    path.getLngEnd());
        return db.insert(TABLE_PATHS, null, cv);
    }
}
