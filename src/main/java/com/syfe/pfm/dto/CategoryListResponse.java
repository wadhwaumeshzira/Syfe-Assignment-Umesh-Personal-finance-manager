package com.syfe.pfm.dto;

import java.util.List;

public class CategoryListResponse {
    private List<CategoryResponse> categories;

    public CategoryListResponse() {
    }

    public CategoryListResponse(List<CategoryResponse> categories) {
        this.categories = categories;
    }

    public List<CategoryResponse> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryResponse> categories) {
        this.categories = categories;
    }
}
