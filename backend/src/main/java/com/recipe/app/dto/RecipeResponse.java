package com.recipe.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class RecipeResponse {
    // ゲッター・セッター
    private boolean success;
    private Map<String, Object> data;
    private String message;
    
    // コンストラクタ
    public RecipeResponse() {}
    
    public RecipeResponse(boolean success, Map<String, Object> data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

}