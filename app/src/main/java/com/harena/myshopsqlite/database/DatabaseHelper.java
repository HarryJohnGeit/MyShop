package com.harena.myshopsqlite.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "myshop.db";
    private static final int DATABASE_VERSION = 7; // Increment the version

    // Table names
    public static final String TABLE_ARTICLE = "article";
    public static final String TABLE_CART = "cart";
    public static final String TABLE_USER = "user";
    public static final String TABLE_COMMANDE = "commande"; // New table for commandes
    public static final String TABLE_COMMANDE_LINE = "commande_line"; // New table for commande details

    // Article Table Columns
    public static final String COLUMN_ARTICLE_ID = "_id";
    public static final String COLUMN_ARTICLE_NAME = "name";
    public static final String COLUMN_ARTICLE_PRICE = "price";
    public static final String COLUMN_ARTICLE_STOCK = "stock";
    public static final String COLUMN_ARTICLE_PHOTO = "photo";

    // Cart Table Columns
    public static final String COLUMN_CART_ID = "_id";
    public static final String COLUMN_CART_ARTICLE_ID = "article_id";
    public static final String COLUMN_CART_QUANTITY = "quantity";
    public static final String COLUMN_CART_TOTAL = "total";
    public static final String COLUMN_CART_USER_ID = "user_id"; // Associate cart with a user

    // User Table Columns
    public static final String COLUMN_USER_ID = "_id";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD = "password";

    // Commande Table Columns
    public static final String COLUMN_COMMANDE_ID = "_id";
    public static final String COLUMN_COMMANDE_USER_ID = "user_id"; // Associate commande with a user
    public static final String COLUMN_COMMANDE_DATE = "date"; // Date of the order
    public static final String COLUMN_COMMANDE_TOTAL = "total"; // Total price of the order

    // Commande Line Table Columns
    public static final String COLUMN_COMMANDE_LINE_ID = "_id";
    public static final String COLUMN_COMMANDE_LINE_COMMANDE_ID = "commande_id"; // Reference to the commande
    public static final String COLUMN_COMMANDE_LINE_ARTICLE_ID = "article_id"; // Reference to the article
    public static final String COLUMN_COMMANDE_LINE_QUANTITY = "quantity";
    public static final String COLUMN_COMMANDE_LINE_TOTAL = "total"; // Total price for this line

    // Create table SQL statements
    private static final String TABLE_CREATE_ARTICLE =
            "CREATE TABLE " + TABLE_ARTICLE + " (" +
                    COLUMN_ARTICLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ARTICLE_NAME + " TEXT, " +
                    COLUMN_ARTICLE_PRICE + " REAL, " +
                    COLUMN_ARTICLE_STOCK + " INTEGER, " +
                    COLUMN_ARTICLE_PHOTO + " TEXT" +
                    ");";

    private static final String TABLE_CREATE_CART =
            "CREATE TABLE " + TABLE_CART + " (" +
                    COLUMN_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CART_ARTICLE_ID + " INTEGER, " +
                    COLUMN_CART_QUANTITY + " INTEGER, " +
                    COLUMN_CART_TOTAL + " REAL, " +
                    COLUMN_CART_USER_ID + " INTEGER, " + // Associate cart with a user
                    "FOREIGN KEY(" + COLUMN_CART_ARTICLE_ID + ") REFERENCES " + TABLE_ARTICLE + "(" + COLUMN_ARTICLE_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_CART_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COLUMN_USER_ID + ")" +
                    ");";

    private static final String TABLE_CREATE_USER =
            "CREATE TABLE " + TABLE_USER + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_EMAIL + " TEXT, " +
                    COLUMN_USER_PASSWORD + " TEXT" +
                    ");";

    private static final String TABLE_CREATE_COMMANDE =
            "CREATE TABLE " + TABLE_COMMANDE + " (" +
                    COLUMN_COMMANDE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_COMMANDE_USER_ID + " INTEGER, " +
                    COLUMN_COMMANDE_DATE + " TEXT, " +
                    COLUMN_COMMANDE_TOTAL + " REAL, " +
                    "FOREIGN KEY(" + COLUMN_COMMANDE_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COLUMN_USER_ID + ")" +
                    ");";

    private static final String TABLE_CREATE_COMMANDE_LINE =
            "CREATE TABLE " + TABLE_COMMANDE_LINE + " (" +
                    COLUMN_COMMANDE_LINE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_COMMANDE_LINE_COMMANDE_ID + " INTEGER, " +
                    COLUMN_COMMANDE_LINE_ARTICLE_ID + " INTEGER, " +
                    COLUMN_COMMANDE_LINE_QUANTITY + " INTEGER, " +
                    COLUMN_COMMANDE_LINE_TOTAL + " REAL, " +
                    "FOREIGN KEY(" + COLUMN_COMMANDE_LINE_COMMANDE_ID + ") REFERENCES " + TABLE_COMMANDE + "(" + COLUMN_COMMANDE_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_COMMANDE_LINE_ARTICLE_ID + ") REFERENCES " + TABLE_ARTICLE + "(" + COLUMN_ARTICLE_ID + ")" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_ARTICLE);
        db.execSQL(TABLE_CREATE_USER);
        db.execSQL(TABLE_CREATE_CART);
        db.execSQL(TABLE_CREATE_COMMANDE);
        db.execSQL(TABLE_CREATE_COMMANDE_LINE); // Create the commande_line table
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARTICLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMANDE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMANDE_LINE);
        // Create new tables
        onCreate(db);
    }

    // Method to add a new user
    public long addUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);
        return db.insert(TABLE_USER, null, values);
    }

    public long addToCart(int articleId, int quantity, double total, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Vérifier le stock disponible
        Cursor cursor = db.query(TABLE_ARTICLE,
                new String[]{COLUMN_ARTICLE_STOCK},
                COLUMN_ARTICLE_ID + " = ?",
                new String[]{String.valueOf(articleId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            int stock = cursor.getInt(cursor.getColumnIndex(COLUMN_ARTICLE_STOCK));
            if (stock >= quantity) {
                // Ajouter l'article au panier
                ContentValues values = new ContentValues();
                values.put(COLUMN_CART_ARTICLE_ID, articleId);
                values.put(COLUMN_CART_QUANTITY, quantity);
                values.put(COLUMN_CART_TOTAL, total);
                values.put(COLUMN_CART_USER_ID, userId); // Associer l'article au panier de l'utilisateur
                long rowId = db.insert(TABLE_CART, null, values);

                // Mettre à jour le stock
                ContentValues stockValues = new ContentValues();
                stockValues.put(COLUMN_ARTICLE_STOCK, stock - quantity);
                db.update(TABLE_ARTICLE, stockValues, COLUMN_ARTICLE_ID + " = ?", new String[]{String.valueOf(articleId)});

                cursor.close();
                return rowId; // Succès, retourne l'ID de la ligne insérée
            } else {
                cursor.close();
                return -1; // Pas assez de stock
            }
        }
        cursor.close();
        return -1; // Article non trouvé
    }


    // Method to create a new order (commande) for a user
    public long addCommande(long userId, String date, double total) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Insert user ID, date, and total into the commande table
        values.put(COLUMN_COMMANDE_USER_ID, userId);
        values.put(COLUMN_COMMANDE_DATE, date); // Ensure the date is in the correct format
        values.put(COLUMN_COMMANDE_TOTAL, total);

        // Insert and return the new row ID
        return db.insert(TABLE_COMMANDE, null, values);
    }

    // Method to add details to a commande
    public long addCommandeLine(long commandeId, int articleId, int quantity, double total) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMMANDE_LINE_COMMANDE_ID, commandeId);
        values.put(COLUMN_COMMANDE_LINE_ARTICLE_ID, articleId);
        values.put(COLUMN_COMMANDE_LINE_QUANTITY, quantity);
        values.put(COLUMN_COMMANDE_LINE_TOTAL, total);
        return db.insert(TABLE_COMMANDE_LINE, null, values);
    }

    // Méthode pour récupérer l'ID utilisateur en fonction de l'email et du mot de passe
    public long getUserIdByEmailAndPassword(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER,
                new String[]{COLUMN_USER_ID},
                COLUMN_USER_EMAIL + " = ? AND " + COLUMN_USER_PASSWORD + " = ?",
                new String[]{email, password},
                null, null, null);

        long userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getLong(cursor.getColumnIndex(COLUMN_USER_ID));
        }
        cursor.close();
        return userId;
    }


    public void addArticle(String name, double price, int stock, String photo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ARTICLE_NAME, name);
        values.put(COLUMN_ARTICLE_PRICE, price);
        values.put(COLUMN_ARTICLE_STOCK, stock);
        values.put(COLUMN_ARTICLE_PHOTO, photo);

        db.insert(TABLE_ARTICLE, null, values);
        db.close();
    }

}
