package com.example.sqlite3_project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.sqlite3_project.admin.User;
import com.example.sqlite3_project.cart.Cart;
import com.example.sqlite3_project.category.Category;
import com.example.sqlite3_project.product.Product;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    static final String DATABASE = "product.db";
    static final int DB_VERSION = 7;

    public DatabaseHelper(Context context) {
        super(context, DATABASE, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS user (userID INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, email TEXT, phone INTEGER, password TEXT, userImage BLOB)");
        db.execSQL("CREATE TABLE IF NOT EXISTS products (productID INTEGER PRIMARY KEY AUTOINCREMENT, productName TEXT, price REAL,inStock BOOLEAN,quantity INTEGER, description TEXT, categoryID INTEGER, adminID INTEGER,productImage BLOB, FOREIGN KEY (adminID) REFERENCES admin(adminID), FOREIGN KEY (categoryID) REFERENCES categories(categoryID))");
        db.execSQL("CREATE TABLE IF NOT EXISTS categories (categoryID INTEGER PRIMARY KEY AUTOINCREMENT, categoryName TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS admin (adminID INTEGER PRIMARY KEY AUTOINCREMENT, admin_username TEXT, admin_email TEXT, admin_pw TEXT)");
        // cart
        db.execSQL("CREATE TABLE IF NOT EXISTS cart (cartID INTEGER PRIMARY KEY AUTOINCREMENT, userID INTEGER, productID INTEGER, quantity INTEGER, FOREIGN KEY (userID) REFERENCES user(userID),FOREIGN KEY (productID) REFERENCES products(productID))");
    }

    public Product getProductByID(String productID) {
        Product product = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM products WHERE productID = ?", new String[]{productID});
        if (cursor.moveToFirst()) {
            // Retrieve product details from the cursor
            String productName = cursor.getString(cursor.getColumnIndex("productName"));
            String description = cursor.getString(cursor.getColumnIndex("description"));
            byte[] image = cursor.getBlob(cursor.getColumnIndex("productImage"));
            double price = cursor.getDouble(cursor.getColumnIndex("price"));
            boolean inStock = cursor.getInt(cursor.getColumnIndex("inStock")) == 1;
            int quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
            int categoryID = cursor.getInt(cursor.getColumnIndex("categoryID"));
            String userID = cursor.getString(cursor.getColumnIndex("adminID"));
            // Create a Product object
            product = new Product(userID, description, image, productName, price, userID, categoryID, inStock, quantity);
        }
        cursor.close();
        db.close();
        return product;
    }
    // clear cart items after purchasing
    public void clearCart(String userID){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("cart","userID = ?",new String[]{userID});
        db.close();
    }
    // fetch cart items
    public List<Cart> getCartItems(String userID) {
        List<Cart> cartItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM cart where userID = ?", new String[]{userID});
        if (cursor.moveToFirst()) {
            do {
                // Retrieve cart item details from the cursor
                String cartID = cursor.getString(cursor.getColumnIndex("cartID"));
                String productID = cursor.getString(cursor.getColumnIndex("productID"));
                int quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
                // Retrieve product details using getProductByID method
                Product product = getProductByID(productID);

                // Create a Cart object and add it to the list
                if (product != null) {
                    Cart cartItem = new Cart(cartID, userID, productID, quantity, product);
                    cartItems.add(cartItem);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return cartItems;
    }

    // getUsers
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from user", null);
        if (cursor.moveToFirst()) {
            do {
                String userID = cursor.getString(cursor.getColumnIndex("userID"));
                String username = cursor.getString(cursor.getColumnIndex("username"));
                String email = cursor.getString(cursor.getColumnIndex("email"));
                String password = cursor.getString(cursor.getColumnIndex("password"));
                int phone = cursor.getInt(cursor.getColumnIndex("phone"));
                byte[] userImage = cursor.getBlob(cursor.getColumnIndex("userImage"));
                User user = new User(userID, username, email, phone, password, userImage);
                users.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        // Logging the number of users fetched
        Log.d("UserAdapter", "Number of users fetched: " + users.size());

        return users;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS user");
//        db.execSQL("DROP TABLE IF EXISTS products");
//        db.execSQL("DROP TABLE IF EXISTS categories");
        onCreate(db);
    }

    public List<Category> getCategories() {
        List<Category> categories = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Query to fetch categories from the database
            String query = "SELECT * FROM categories";
            cursor = db.rawQuery(query, null);

            // Iterate through the cursor and add categories to the list
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int categoryId = cursor.getInt(cursor.getColumnIndex("categoryID"));
                    String categoryName = cursor.getString(cursor.getColumnIndex("categoryName"));

                    // Create a Category object and add it to the list
                    Category category = new Category(categoryId, categoryName);
                    categories.add(category);

                    // Log the retrieved category
//                    Log.d("CategoryDebug", "Category ID: " + categoryId + ", Name: " + categoryName);
                } while (cursor.moveToNext());
            }
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return categories;
    }

    public void logCartData() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from cart", null);
        logDataFromCursor(cursor);
    }

    // Method to log data from the 'user' table
    public void logUserData() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM user", null);
        logDataFromCursor(cursor);
    }

    // delete product from db
    public void deleteProduct(String productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("products", "productID = ?", new String[]{productId});
        db.close();
    }

    // add to cart
    public void addToCart(int userId, int productId, int quantity) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userID", userId);
        values.put("productID", productId);
        values.put("quantity", quantity);
        db.insert("cart", null, values);
        db.close();
    }

    // Method to log data from the 'products' table
//    public void logProductData() {
//        SQLiteDatabase db = getReadableDatabase();
//        Cursor cursor = db.rawQuery("SELECT * FROM products", null);
//        logDataFromCursor(cursor);
//    }
    public void addAllCategory() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if "All" category already exists
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM categories WHERE categoryName = ?", new String[]{"All"});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        // If "All" category doesn't exist, insert it
        if (count == 0) {
            ContentValues values = new ContentValues();
            values.put("categoryName", "All");
            db.insert("categories", null, values);
        }

        db.close();
    }

    // Method to log data from the 'categories' table
    public void logCategoryData() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM categories", null);
        logDataFromCursor(cursor);
    }

    public void logProductData() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT productID, productName, description, price, categoryID,inStock,quantity FROM products", null);
        logDataFromCursor(cursor);
    }

    // Method to log data from the 'admin' table
    public void logAdminData() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM admin", null);
        logDataFromCursor(cursor);
    }

    // Method to check if a cart item has been removed
    public boolean isCartItemRemoved(String cartId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM cart WHERE cartID = ?", new String[]{cartId});
        boolean isRemoved = cursor.getCount() == 0;
        cursor.close();
        db.close();
        return isRemoved;
    }

    // delete categories if there is no existing products under that category
    public void deleteEmptyCategories() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Query to find categories with no associated products and not named "All"
            String query = "DELETE FROM categories WHERE categoryID NOT IN (SELECT DISTINCT categoryID FROM products) AND categoryName <> 'All'";
            db.execSQL(query);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DatabaseError", "Error deleting empty categories: " + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // delete cart item
    public void deleteCartItem(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("cart", "cartID = ?", new String[]{id});
    }

    // Helper method to log data from a cursor
    private void logDataFromCursor(Cursor cursor) {
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        stringBuilder.append(cursor.getColumnName(i)).append(": ").append(cursor.getString(i)).append(", ");
                    }
                    Log.d("DatabaseDebug", stringBuilder.toString());
                }
            } finally {
                cursor.close();
            }
        }
    }
}
