package com.recipe.app.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Ingredient {
    private String name;
    private String quantity;
    private String unit;
    private String note;
    
    // JPA用の引数なしコンストラクタ
    public Ingredient() {}
} 