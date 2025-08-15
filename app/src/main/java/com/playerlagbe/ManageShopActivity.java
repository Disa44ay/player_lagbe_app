package com.playerlagbe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageShopActivity extends AppCompatActivity {

    private EditText productNameInput, imageLinkInput, productDetailsInput, priceInput;
    private RadioGroup featuredGroup;
    private Button addToStoreButton;
    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private FirebaseFirestore firestore;
    private ListenerRegistration productsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_shop);

        // Check admin status
        checkAdminAccess();

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();

        // Initialize views
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup listeners
        setupClickListeners();

        // Start listening for product updates
        startProductListener();
    }

    private void checkAdminAccess() {
        FirebaseAuthManager authManager = new FirebaseAuthManager(this);
        authManager.checkAdminStatus(new FirebaseAuthManager.AdminCheckListener() {
            @Override
            public void onAdminCheckResult(boolean isAdmin) {
                if (!isAdmin) {
                    Toast.makeText(ManageShopActivity.this, "Access denied. Admin only.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onAdminCheckError(String error) {
                Toast.makeText(ManageShopActivity.this, "Could not verify admin status. Access denied.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void initializeViews() {
        productNameInput = findViewById(R.id.productName);
        imageLinkInput = findViewById(R.id.imageLink);
        productDetailsInput = findViewById(R.id.productDetails);
        priceInput = findViewById(R.id.price);
        featuredGroup = findViewById(R.id.featuredGroup);
        addToStoreButton = findViewById(R.id.addToStoreButton);
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(productList, this::showEditDialog, this::showDeleteDialog);
        productsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productsRecyclerView.setAdapter(productAdapter);
    }

    private void setupClickListeners() {
        addToStoreButton.setOnClickListener(v -> addProduct());
        
        // Setup hamburger menu
        ImageView hamburgerMenuIcon = findViewById(R.id.hamburgerMenuIcon);
        if (hamburgerMenuIcon != null) {
            hamburgerMenuIcon.setOnClickListener(this::showHamburgerMenu);
        }
    }

    private void showHamburgerMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.menu_hamburger, popup.getMenu());
        
        // Show admin menu items
        popup.getMenu().findItem(R.id.menu_manage_team).setVisible(true);
        popup.getMenu().findItem(R.id.menu_manage_shop).setVisible(true);
        popup.getMenu().findItem(R.id.menu_manage_orders).setVisible(true);
        
        // Set theme toggle text
        SharedPreferences prefs = getSharedPreferences("settings", 0);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        popup.getMenu().findItem(R.id.menu_theme).setTitle(isDarkMode ? "Light Mode" : "Dark Mode");
        
        popup.setOnMenuItemClickListener(item -> {
            handleHamburgerMenuAction(item.getItemId());
            return true;
        });
        popup.show();
    }

    private void handleHamburgerMenuAction(int actionId) {
        if (actionId == R.id.menu_profile) {
            // Navigate back to main activity and show profile
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("fragment", "profile");
            startActivity(intent);
        } else if (actionId == R.id.menu_cart) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("fragment", "cart");
            startActivity(intent);
        } else if (actionId == R.id.menu_theme) {
            toggleTheme();
        } else if (actionId == R.id.menu_manage_team) {
            startActivity(new Intent(this, ManageTeamActivity.class));
        } else if (actionId == R.id.menu_manage_shop) {
            // Already in manage shop
        } else if (actionId == R.id.menu_manage_orders) {
            startActivity(new Intent(this, ManageOrdersActivity.class));
        } else if (actionId == R.id.menu_logout) {
            logout();
        }
    }

    private void toggleTheme() {
        SharedPreferences prefs = getSharedPreferences("settings", 0);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            prefs.edit().putBoolean("dark_mode", false).apply();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            prefs.edit().putBoolean("dark_mode", true).apply();
        }
        recreate();
    }

    private void logout() {
        FirebaseAuthManager authManager = new FirebaseAuthManager(this);
        authManager.signOut();
        Intent intent = new Intent(this, AuthenticationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
    }

    private void addProduct() {
        String name = productNameInput.getText().toString().trim();
        String imageLink = imageLinkInput.getText().toString().trim();
        String details = productDetailsInput.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            productNameInput.setError("Product name is required");
            return;
        }
        if (details.isEmpty()) {
            productDetailsInput.setError("Product details are required");
            return;
        }
        if (priceStr.isEmpty()) {
            priceInput.setError("Price is required");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            priceInput.setError("Invalid price format");
            return;
        }

        // Get featured status
        int selectedId = featuredGroup.getCheckedRadioButtonId();
        boolean featured = selectedId == R.id.radioFeaturedYes;

        // Create product data
        Map<String, Object> productData = new HashMap<>();
        productData.put("name", name);
        if (!imageLink.isEmpty()) {
            productData.put("imageLink", imageLink);
        }
        productData.put("details", details);
        productData.put("price", price);
        productData.put("featured", featured);
        productData.put("createdAt", Timestamp.now());

        // Add to Firestore
        firestore.collection("shop")
                .add(productData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();
                    clearForm();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearForm() {
        productNameInput.setText("");
        imageLinkInput.setText("");
        productDetailsInput.setText("");
        priceInput.setText("");
        featuredGroup.check(R.id.radioFeaturedNo);
    }

    private void startProductListener() {
        productsListener = firestore.collection("shop")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        productList.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Product product = doc.toObject(Product.class);
                            if (product != null) {
                                product.setProductId(doc.getId());
                                productList.add(product);
                            }
                        }
                        productAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void showEditDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View editView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_product, null);
        
        EditText editName = editView.findViewById(R.id.editProductName);
        EditText editImageLink = editView.findViewById(R.id.editImageLink);
        EditText editDetails = editView.findViewById(R.id.editProductDetails);
        EditText editPrice = editView.findViewById(R.id.editPrice);
        RadioGroup editFeaturedGroup = editView.findViewById(R.id.editFeaturedGroup);
        
        // Pre-fill with current values
        editName.setText(product.getName());
        editImageLink.setText(product.getImageLink() != null ? product.getImageLink() : "");
        editDetails.setText(product.getDetails());
        editPrice.setText(String.valueOf(product.getPrice()));
        editFeaturedGroup.check(product.isFeatured() ? R.id.editRadioFeaturedYes : R.id.editRadioFeaturedNo);
        
        builder.setView(editView)
                .setTitle("Edit Product")
                .setPositiveButton("Update", (dialog, which) -> {
                    updateProduct(product.getProductId(), editName, editImageLink, editDetails, editPrice, editFeaturedGroup);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateProduct(String productId, EditText editName, EditText editImageLink, 
                             EditText editDetails, EditText editPrice, RadioGroup editFeaturedGroup) {
        String name = editName.getText().toString().trim();
        String imageLink = editImageLink.getText().toString().trim();
        String details = editDetails.getText().toString().trim();
        String priceStr = editPrice.getText().toString().trim();

        if (name.isEmpty() || details.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean featured = editFeaturedGroup.getCheckedRadioButtonId() == R.id.editRadioFeaturedYes;

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        if (!imageLink.isEmpty()) {
            updates.put("imageLink", imageLink);
        } else {
            updates.put("imageLink", null);
        }
        updates.put("details", details);
        updates.put("price", price);
        updates.put("featured", featured);

        firestore.collection("shop").document(productId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteDialog(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete '" + product.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    firestore.collection("shop").document(product.getProductId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to delete product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (productsListener != null) {
            productsListener.remove();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    // Product class
    public static class Product {
        private String productId;
        private String name;
        private String imageLink;
        private String details;
        private double price;
        private boolean featured;
        private Timestamp createdAt;

        public Product() {
            // Default constructor required for Firestore
        }

        // Getters and setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getImageLink() { return imageLink; }
        public void setImageLink(String imageLink) { this.imageLink = imageLink; }
        
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        
        public boolean isFeatured() { return featured; }
        public void setFeatured(boolean featured) { this.featured = featured; }
        
        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    }

    // RecyclerView Adapter
    private static class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
        private List<Product> products;
        private OnProductEditListener editListener;
        private OnProductDeleteListener deleteListener;

        interface OnProductEditListener {
            void onEdit(Product product);
        }

        interface OnProductDeleteListener {
            void onDelete(Product product);
        }

        public ProductAdapter(List<Product> products, OnProductEditListener editListener, OnProductDeleteListener deleteListener) {
            this.products = products;
            this.editListener = editListener;
            this.deleteListener = deleteListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_admin, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Product product = products.get(position);
            holder.productName.setText(product.getName());
            holder.productDetails.setText(product.getDetails());
            holder.productPrice.setText(String.format("$%.2f", product.getPrice()));
            holder.featuredBadge.setVisibility(product.isFeatured() ? View.VISIBLE : View.GONE);

            holder.editButton.setOnClickListener(v -> editListener.onEdit(product));
            holder.deleteButton.setOnClickListener(v -> deleteListener.onDelete(product));
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView productName, productDetails, productPrice, featuredBadge;
            Button editButton, deleteButton;

            ViewHolder(View itemView) {
                super(itemView);
                productName = itemView.findViewById(R.id.productName);
                productDetails = itemView.findViewById(R.id.productDetails);
                productPrice = itemView.findViewById(R.id.productPrice);
                featuredBadge = itemView.findViewById(R.id.featuredBadge);
                editButton = itemView.findViewById(R.id.editButton);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }
    }
}