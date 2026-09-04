package com.syfe.pfm.model;

import jakarta.persistence.*;

/**
 * Entity representing a transaction Category in the Personal Finance Manager system.
 * Categories can be default/system-wide (isCustom = false, userId = null)
 * or custom/user-specific (isCustom = true, userId = <user_id>).
 */
@Entity
@Table(name = "categories", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "name"})
})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // INCOME or EXPENSE

    @Column(nullable = false)
    private boolean isCustom;

    @Column(name = "user_id")
    private Long userId; // Null for default categories, non-null for custom categories

    public Category() {
    }

    public Category(String name, String type, boolean isCustom, Long userId) {
        this.name = name;
        this.type = type;
        this.isCustom = isCustom;
        this.userId = userId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
