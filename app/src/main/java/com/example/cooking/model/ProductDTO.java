package com.example.cooking.model;


public class ProductDTO {
    private Long id;
    private String name;
    private String category;
    private Boolean isCommon;
    private String addedAt;


    public ProductDTO() {
    }

    public ProductDTO(Long id, String name, String category, Boolean isCommon) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.isCommon = isCommon;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public Boolean getIsCommon() {
        return isCommon;
    }

    public void setIsCommon(Boolean isCommon) {
        this.isCommon = isCommon;
    }

    public String getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(String addedAt) {
        this.addedAt = addedAt;
    }
}