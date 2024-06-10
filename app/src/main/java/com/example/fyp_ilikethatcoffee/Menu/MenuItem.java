package com.example.fyp_ilikethatcoffee.Menu;

public class MenuItem {
    private String Name;
    private String Description;
    private String Category;
    private String CategoryId;

    public MenuItem() {
        // Default constructor required for Firebase Firestore deserialization
    }

    public MenuItem(String Name, String Description){
        this.Name = Name;
        this.Description = Description;
    }
    public MenuItem(String Name, String Description, String Category,String CategoryId){
        this.Name = Name;
        this.Description = Description;
        this.Category = Category;
        this.CategoryId = CategoryId;
    }

    public String getName() {
        return Name;
    }

    public String getDescription() {
        return Description;
    }

    public String getCategory() {
        return Category;
    }

    public String getCategoryId() {
        return CategoryId;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public void setCategoryId(String categoryId) {
        CategoryId = categoryId;
    }
}
