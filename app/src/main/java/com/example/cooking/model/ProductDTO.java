package com.example.cooking.model;

public class ProductDTO {
    private Long id;
    private String name;
    private String category;
    private Boolean isCommon;
    private String addedAt;

    public ProductDTO(){}
    public ProductDTO(Long id, String name, String category, Boolean isCommon, String addedAt){
        this.id = id;
        this.name = name;
        this.category = category;
        this.isCommon=isCommon;
        this.addedAt = addedAt;
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

    public Boolean getCommon() {
        return isCommon;
    }

    public void setCommon(Boolean common) {
        isCommon = common;
    }

    public String getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(String addedAt) {
        this.addedAt = addedAt;
    }
}
