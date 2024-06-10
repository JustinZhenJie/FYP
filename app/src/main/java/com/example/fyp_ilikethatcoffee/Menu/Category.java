package com.example.fyp_ilikethatcoffee.Menu;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private String Category;
    private String CategoryId;
    private String UserAccountId;

    public Category() {
        // Default constructor required for Firestore's automatic data mapping
    }

    public Category(String category, String userAccountId, String categoryId) {

        this.Category = category;
        this.UserAccountId = userAccountId;
        this.CategoryId = categoryId;

    }

    public String getCategory() {
        return Category;
    }

    public String getUserAccountId() {
        return UserAccountId;
    }

    public String getCategoryId() {
        return CategoryId;
    }

    public void setCategory(String category) {
        this.Category = category;
    }

    public void setUserAccountId(String userAccountId) {
        UserAccountId = userAccountId;
    }

    public void setCategoryId(String categoryId) {
        CategoryId = categoryId;
    }

    public CollectionReference getMenuItemsCollection() {
        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the MenuItem collection for the specific Category
        return db.collection("Store")
                .document(UserAccountId)
                .collection("Category")
                .document(CategoryId)
                .collection("MenuItem");
    }

    public Task<List<MenuItem>> getMenuItems() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the MenuItem collection for the specific Category
        return db.collection("Store")
                .document(UserAccountId)
                .collection("Category")
                .document(CategoryId)
                .collection("MenuItem")
                .get()
                .continueWith(task -> {
                    List<MenuItem> menuItems = new ArrayList<>();
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            // Log the number of documents retrieved
                            Log.d("getMenuItems", "Number of documents: " + querySnapshot.size());

                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                // Convert each document to MenuItem object and add to the list
                                MenuItem menuItem = document.toObject(MenuItem.class);
                                if (menuItem != null) {
                                    // Log the details of each MenuItem
                                    Log.d("getMenuItems", "MenuItem: " + menuItem.getName() + ", " + menuItem.getDescription());

                                    menuItems.add(menuItem);
                                }
                            }
                        } else {
                            Log.d("getMenuItems", "Query snapshot is null");
                        }
                    } else {
                        Log.e("getMenuItems", "Error getting documents: ", task.getException());
                    }
                    return menuItems;
                });
    }

}
