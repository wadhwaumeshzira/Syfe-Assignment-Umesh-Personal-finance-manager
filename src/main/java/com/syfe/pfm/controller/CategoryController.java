package com.syfe.pfm.controller;

import com.syfe.pfm.dto.CategoryListResponse;
import com.syfe.pfm.dto.CategoryRequest;
import com.syfe.pfm.dto.CategoryResponse;
import com.syfe.pfm.dto.MessageResponse;
import com.syfe.pfm.model.Category;
import com.syfe.pfm.security.UserPrincipal;
import com.syfe.pfm.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller handling default and custom category endpoints.
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<CategoryListResponse> getAllCategories(@AuthenticationPrincipal UserPrincipal principal) {
        List<Category> categories = categoryService.getAllCategories(principal.getId());
        List<CategoryResponse> categoryResponses = categories.stream()
                .map(CategoryResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new CategoryListResponse(categoryResponses));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCustomCategory(
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        Category category = categoryService.createCustomCategory(
                request.getName(), 
                request.getType(), 
                principal.getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(new CategoryResponse(category));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<MessageResponse> deleteCustomCategory(
            @PathVariable String name,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        categoryService.deleteCustomCategory(name, principal.getId());
        return ResponseEntity.ok(new MessageResponse("Category deleted successfully"));
    }
}
