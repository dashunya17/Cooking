package com.example.cooking.model;

import java.util.List;

public class RecipeDTO {
    private Long id;
    private String title;
    private String description;
    private String cookingSteps;
    private Integer cookingTimeMinutes;
    private String difficulty;
    private Integer servings;
    private String category;
    private String imageUrl;
    private Double matchPercentage;
    private List<String> missingIngredients;
    private List<IngredientDTO> ingredients;
    private Boolean isFavorite;
    public RecipeDTO(){}
    public RecipeDTO(Long id,String title, String description,String cookingSteps, Integer cookingTimeMinutes,String difficulty,Integer servings, String category,
                     String imageUrl,Double matchPercentage,List<String> missingIngredients,List<IngredientDTO> ingredients,Boolean isFavorite){
        this.id = id;
        this.title = title;
        this.description = description;
        this.cookingSteps = cookingSteps;
        this.cookingTimeMinutes =cookingTimeMinutes;
        this.difficulty = difficulty;
        this.servings = servings;
        this.category = category;
        this.imageUrl = imageUrl;
        this.matchPercentage =matchPercentage;
        this.missingIngredients = missingIngredients;
        this.ingredients = ingredients;
        this.isFavorite = isFavorite;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCookingSteps() {
        return cookingSteps;
    }

    public void setCookingSteps(String cookingSteps) {
        this.cookingSteps = cookingSteps;
    }

    public Integer getCookingTimeMinutes() {
        return cookingTimeMinutes;
    }

    public void setCookingTimeMinutes(Integer cookingTimeMinutes) {
        this.cookingTimeMinutes = cookingTimeMinutes;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getServings() {
        return servings;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(Double matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

    public List<String> getMissingIngredients() {
        return missingIngredients;
    }

    public void setMissingIngredients(List<String> missingIngredients) {
        this.missingIngredients = missingIngredients;
    }

    public List<IngredientDTO> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<IngredientDTO> ingredients) {
        this.ingredients = ingredients;
    }

    public Boolean getFavorite() {
        return isFavorite;
    }

    public void setFavorite(Boolean favorite) {
        isFavorite = favorite;
    }
}
