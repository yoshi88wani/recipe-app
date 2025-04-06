package com.recipe.app.controller;

import com.recipe.app.dto.RecipeRequest;
import com.recipe.app.dto.RecipeResponse;
import com.recipe.app.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    
    // コンストラクタインジェクション
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping("/suggest")
    public ResponseEntity<RecipeResponse> suggestRecipes(@Valid @RequestBody RecipeRequest request) {
        RecipeResponse response = recipeService.suggestRecipes(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> getRecipeById(@PathVariable String id) {
        try {
            RecipeResponse response = recipeService.getRecipeById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("message", e.getMessage());
            
            RecipeResponse errorResponse = new RecipeResponse(false, null, "レシピの取得に失敗しました: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}
