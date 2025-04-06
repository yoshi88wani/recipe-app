package com.recipe.app.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class RecipeRequest {
    // ゲッター・セッター
    @NotEmpty(message = "食材リストは必須です")
    @Size(min = 1, max = 20, message = "食材は1〜20個まで指定できます")
    private List<String> ingredients;
    private Map<String, Object> preferences;
    private List<String> excludedIngredients;
    
    // コンストラクタ
    public RecipeRequest() {}

}