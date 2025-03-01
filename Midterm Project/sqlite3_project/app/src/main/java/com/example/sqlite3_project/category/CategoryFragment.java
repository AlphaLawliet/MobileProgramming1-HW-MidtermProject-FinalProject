package com.example.sqlite3_project.category;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.sqlite3_project.DatabaseHelper;
import com.example.sqlite3_project.product.Product;
import com.example.sqlite3_project.product.ProductAdapter;
import com.example.sqlite3_project.R;
import com.example.sqlite3_project.admin.adminProductDetail;
import com.example.sqlite3_project.customer.ProductDetails;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class CategoryFragment extends Fragment implements ProductAdapter.OnProductClickListener {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private String categoryName;
    private String userID;
    private DatabaseHelper dbHelper;
    private CategoryAdapter categoryAdapter;

    public CategoryFragment() {
    }

    static final int UPLOAD_PRODUCT_REQUEST_CODE = 1;

    public static CategoryFragment newInstance(String categoryName, String userID, CategoryAdapter categoryAdapter, String userType) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString("categoryName", categoryName);
        args.putString("userID", userID); // Add adminID to the arguments
        args.putString("userType", userType);
        fragment.setArguments(args);
        fragment.setCategoryAdapter(categoryAdapter); // Set the category adapter
        return fragment;
    }
    private void setCategoryAdapter(CategoryAdapter adapter) {
        this.categoryAdapter = adapter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());
        if (getArguments() != null) {
            categoryName = getArguments().getString("categoryName");
            userID = getArguments().getString("userID"); // Retrieve adminID from arguments
        }
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.category_fragment, container, false);
        productList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.productList);
        // Modified code to retrieve userType with null check
        String userType = null;
        Bundle arguments = getArguments();
        if (arguments != null) {
            userType = arguments.getString("userType");
        }
        adapter = new ProductAdapter(getContext(),productList, this,userType,userID);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);
        TabLayout tabLayout = getActivity().findViewById(R.id.tabLayout);
        if (categoryName != null && userType.equals("customer")) {
            loadProductsForCategoryUser(categoryName);
        }
        if (categoryName != null && userType.equals("admin")) {
            loadProductsForCategory(categoryName, userID);
        }
        return view;
    }
    public void filterProducts(String searchText) {
        if (adapter != null) {
            adapter.filter(searchText);
        }
    }
    public void loadProductsForCategoryUser(String categoryName) {
        if (categoryName.equals("All")) {
            loadAllProducts();
        } else {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM products WHERE categoryID = (SELECT categoryID FROM categories WHERE categoryName = ?)", new String[]{categoryName});
            if (cursor.moveToFirst()) {
                do {
                    Product product = new Product(
                            cursor.getString(cursor.getColumnIndex("productID")),
                            cursor.getString(cursor.getColumnIndex("description")),
                            cursor.getBlob(cursor.getColumnIndex("productImage")),
                            cursor.getString(cursor.getColumnIndex("productName")),
                            cursor.getDouble(cursor.getColumnIndex("price")),
                            cursor.getString(cursor.getColumnIndex("adminID")),
                            cursor.getInt(cursor.getColumnIndex("categoryID")),
                            cursor.getInt(cursor.getColumnIndex("inStock")) == 1,
                            cursor.getInt(cursor.getColumnIndex("quantity"))
                    );
                    productList.add(product);
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            adapter.notifyDataSetChanged();
        }
    }

    public void loadProductsForCategory(String categoryName, String adminID) {
        if (categoryName.equals("All")) {
            loadAllProducts(adminID);
        } else {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM products WHERE categoryID = (SELECT categoryID FROM categories WHERE categoryName = ?) AND adminID = ?", new String[]{categoryName, adminID});
            if (cursor.moveToFirst()) {
                do {
                    Product product = new Product(
                            cursor.getString(cursor.getColumnIndex("productID")),
                            cursor.getString(cursor.getColumnIndex("description")),
                            cursor.getBlob(cursor.getColumnIndex("productImage")),
                            cursor.getString(cursor.getColumnIndex("productName")),
                            cursor.getDouble(cursor.getColumnIndex("price")),
                            cursor.getString(cursor.getColumnIndex("adminID")),
                            cursor.getInt(cursor.getColumnIndex("categoryID")),
                            cursor.getInt(cursor.getColumnIndex("inStock")) == 1,
                            cursor.getInt(cursor.getColumnIndex("quantity"))
                    );
                    productList.add(product);
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            adapter.notifyDataSetChanged();
        }
    }

    public void refreshProductList() {
        // Clear the existing product list
        productList.clear();
        // Reload products based on the category name
        if (categoryName.equals("All")) {
            loadAllProducts(userID); // Load all products
        } else {
            loadProductsForCategory(categoryName, userID); // Load products for the specific category
        }
        // Notify the adapter that the data has changed
        adapter.notifyDataSetChanged();
    }
    private void setProductList(Product product){
        productList.add(product);
    }
    private void loadAllProducts() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM products", null);
        if (cursor.moveToFirst()) {
            do {
                Product product = new Product(
                        cursor.getString(cursor.getColumnIndex("productID")),
                        cursor.getString(cursor.getColumnIndex("description")),
                        cursor.getBlob(cursor.getColumnIndex("productImage")),
                        cursor.getString(cursor.getColumnIndex("productName")),
                        cursor.getDouble(cursor.getColumnIndex("price")),
                        cursor.getString(cursor.getColumnIndex("adminID")),
                        cursor.getInt(cursor.getColumnIndex("categoryID")),
                        cursor.getInt(cursor.getColumnIndex("inStock")) == 1,
                        cursor.getInt(cursor.getColumnIndex("quantity"))
                );
                productList.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        adapter.notifyDataSetChanged();
    }
    private void loadAllProducts(String adminID) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM products WHERE adminID = ?", new String[]{adminID});
        if (cursor.moveToFirst()) {
            do {
                Product product = new Product(
                        cursor.getString(cursor.getColumnIndex("productID")),
                        cursor.getString(cursor.getColumnIndex("description")),
                        cursor.getBlob(cursor.getColumnIndex("productImage")),
                        cursor.getString(cursor.getColumnIndex("productName")),
                        cursor.getDouble(cursor.getColumnIndex("price")),
                        cursor.getString(cursor.getColumnIndex("adminID")),
                        cursor.getInt(cursor.getColumnIndex("categoryID")),
                        cursor.getInt(cursor.getColumnIndex("inStock")) == 1,
                        cursor.getInt(cursor.getColumnIndex("quantity"))
                );
                productList.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        adapter.notifyDataSetChanged();
    }
    public void userClickProduct(Product product) {
        Bundle bundle = new Bundle();
        bundle.putString("productID", product.getId());
        bundle.putString("userID", userID);
        Intent intent = new Intent(getContext(), ProductDetails.class);
        intent.putExtras(bundle);
        startActivity(intent); // Start activity
    }

    @Override
    public void onProductClick(Product product) {
        Bundle bundle = new Bundle();
        bundle.putString("productID", product.getId());
        Intent intent = new Intent(getContext(), adminProductDetail.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, UPLOAD_PRODUCT_REQUEST_CODE); // Start activity for result
    }
    public void searchProducts(String query) {
        Log.d("SearchQuery", "Search query: " + query);

        List<Product> filteredProducts = new ArrayList<>();

        // Iterate through the original list of products and add matching ones to the filtered list
        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredProducts.add(product);
            }
        }

        // Log the filtered products
        for (Product product : filteredProducts) {
            Log.d("FilteredProduct", "Product: " + product.getName());
        }

        // Clear the current list and add the filtered products
        productList.clear();
        productList.addAll(filteredProducts);

        // Notify the adapter that the data set has changed
        adapter.notifyDataSetChanged();
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == UPLOAD_PRODUCT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Refresh the product list in CategoryFragment
            refreshProductList();
            // Refresh the category list
            if (categoryAdapter != null) {
                DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
                List<Category> categories = dbHelper.getCategories();
                categoryAdapter.loadCategoriesFromDatabase(categories);
                categoryAdapter.refreshCategories(categories);
            } else {
                // Handle the case where categoryAdapter is not initialized
                Toast.makeText(requireContext(), "Category adapter is not initialized", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
