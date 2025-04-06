package com.recipe.app.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class NutritionInfo {
    private Integer calories;
    private String protein;
    private String carbs;
    private String fat;
    
    // JPA用の引数なしコンストラクタ
    public NutritionInfo() {}
} 