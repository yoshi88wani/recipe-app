package com.recipe.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class RecipeRequest {
    // ゲッター・セッター
    private List<String> ingredients;
    private Map<String, Object> preferences;
    private List<String> excludedIngredients;
    
    // コンストラクタ
    public RecipeRequest() {}

}