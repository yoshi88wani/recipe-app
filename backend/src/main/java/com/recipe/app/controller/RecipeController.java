package com.recipe.app.controller;

import com.recipe.app.dto.RecipeRequest;
import com.recipe.app.dto.RecipeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recipes")
public class RecipeController {

    @PostMapping("/suggest")
    public ResponseEntity<RecipeResponse> suggestRecipes(@RequestBody RecipeRequest request) {
        // TODO: 実際には食材リストから適切なレシピを検索するロジックを実装
        // 現在はモックデータを返す
        
        Map<String, Object> recipe = Map.of(
            "id", "recipe-123",
            "title", "鶏肉と野菜の簡単煮物",
            "description", "ほくほくじゃがいもと柔らかい鶏肉の優しい味わい",
            "ingredients", Arrays.asList(
                Map.of("name", "鶏もも肉", "quantity", "300", "unit", "g", "note", "一口大に切る"),
                Map.of("name", "じゃがいも", "quantity", "2", "unit", "個", "note", "4等分に切る"),
                Map.of("name", "人参", "quantity", "1", "unit", "本", "note", "乱切り")
            ),
            "instructions", Arrays.asList(
                "鶏肉を一口大に切ります",
                "じゃがいもは皮をむき、4等分に切ります",
                "人参は乱切りにします",
                "鍋に調味料と水を入れて沸騰させます",
                "具材を入れて中火で15分煮込みます"
            ),
            "cookingTime", 25,
            "difficulty", "EASY",
            "servingSize", 2,
            "tags", Arrays.asList("和食", "煮物", "晩ごはん"),
            "nutritionInfo", Map.of(
                "calories", 450,
                "protein", "28g",
                "carbs", "30g",
                "fat", "22g"
            )
        );
        
        List<Map<String, Object>> recipes = List.of(recipe);
        
        Map<String, Object> data = Map.of(
            "recipes", recipes,
            "generationId", "gen-" + System.currentTimeMillis()
        );
        
        RecipeResponse response = new RecipeResponse(true, data, null);
        return ResponseEntity.ok(response);
    }
}
