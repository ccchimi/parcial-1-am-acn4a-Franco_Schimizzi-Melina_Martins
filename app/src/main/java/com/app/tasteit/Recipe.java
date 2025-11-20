package com.app.tasteit;

public class Recipe {
    private final String title;
    private final String description;
    private final String imageUrl;
    private final String cookingTime;

    public Recipe(String title, String description, String imageUrl, String cookingTime) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.cookingTime = cookingTime;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCookingTime() {
        return cookingTime;
    }
}