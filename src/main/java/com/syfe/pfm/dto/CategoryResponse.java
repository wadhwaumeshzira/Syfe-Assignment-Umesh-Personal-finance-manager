package com.syfe.pfm.dto;

import com.syfe.pfm.model.Category;

public class CategoryResponse {
    private String name;
    private String type;
    private boolean isCustom;

    public CategoryResponse() {
    }

    public CategoryResponse(Category category) {
        this.name = category.getName();
        this.type = category.getType();
        this.isCustom = category.isCustom();
    }

    public CategoryResponse(String name, String type, boolean isCustom) {
        this.name = name;
        this.type = type;
        this.isCustom = isCustom;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean getIsCustom() {
        return isCustom;
    }

    public boolean getCustom() {
        return isCustom;
    }

    public void setIsCustom(boolean custom) {
        isCustom = custom;
    }
}
