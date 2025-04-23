package com.recipe.app.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.app.dto.RecipeRequest;
import com.recipe.app.dto.RecipeResponse;
import com.recipe.app.model.Ingredient;
import com.recipe.app.model.NutritionInfo;
import com.recipe.app.model.Recipe;
import com.recipe.app.repository.RecipeRepository;
import com.recipe.app.service.BedrockService;
import com.recipe.app.service.RecipeService;

/**
 * レシピサービスの実装クラス
 */
@Service
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final BedrockService bedrockService;
    private final ObjectMapper objectMapper;
    
    // コンストラクタインジェクション
    public RecipeServiceImpl(RecipeRepository recipeRepository, BedrockService bedrockService) {
        this.recipeRepository = recipeRepository;
        this.bedrockService = bedrockService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 指定された食材と条件から、おすすめのレシピを提案します
     *
     * @param request レシピリクエスト情報
     * @return レシピ提案のレスポンス
     */
    @Override
    public RecipeResponse suggestRecipes(RecipeRequest request) {
        List<String> ingredients = request.getIngredients();
        
        try {
            // 1. AIによるレシピ生成
            String aiGeneratedRecipe = bedrockService.generateRecipe(
                    ingredients, 
                    request.getPreferences()
            );
            
            // デバッグログ
            System.out.println("AI生成レスポンス: " + aiGeneratedRecipe);
            
            // 2. AIレスポンスのパース（配列形式に対応）
            List<Recipe> recipes = parseMultipleRecipes(aiGeneratedRecipe);
            
            // 3. データベースに保存
            List<Recipe> savedRecipes = new ArrayList<>();
            for (Recipe recipe : recipes) {
                savedRecipes.add(recipeRepository.save(recipe));
            }
            
            // 4. レスポンス作成
            List<Map<String, Object>> recipeMaps = savedRecipes.stream()
                    .map(this::convertToMap)
                    .collect(Collectors.toList());
            
            Map<String, Object> data = new HashMap<>();
            data.put("recipes", recipeMaps);
            data.put("generationId", "gen-" + System.currentTimeMillis());
            
            return new RecipeResponse(true, data, null);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("エラーの詳細: " + e.getMessage());
            
            // エラー発生時はサンプルデータを返す
            List<Recipe> sampleRecipes = createSampleRecipes();
            List<Recipe> savedRecipes = new ArrayList<>();
            for (Recipe recipe : sampleRecipes) {
                savedRecipes.add(recipeRepository.save(recipe));
            }
            
            List<Map<String, Object>> recipeMaps = savedRecipes.stream()
                    .map(this::convertToMap)
                    .collect(Collectors.toList());
                    
            Map<String, Object> data = new HashMap<>();
            data.put("recipes", recipeMaps);
            data.put("generationId", "gen-" + System.currentTimeMillis());
            
            return new RecipeResponse(true, data, "AI生成でエラーが発生したため、サンプルレシピを返しました: " + e.getMessage());
        }
    }
    
    @Override
    public RecipeResponse getRecipeById(String id) throws Exception {
        try {
            // IDを解析（文字列からLongに変換）
            Long recipeId = Long.parseLong(id);
            
            // レシピの取得
            Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);
            
            if (recipeOpt.isEmpty()) {
                throw new Exception("指定されたIDのレシピが見つかりません: " + id);
            }
            
            Recipe recipe = recipeOpt.get();
            
            // レスポンス作成
            Map<String, Object> recipeMap = convertToMap(recipe);
            Map<String, Object> data = new HashMap<>();
            data.put("recipe", recipeMap);
            
            return new RecipeResponse(true, data, null);
            
        } catch (NumberFormatException e) {
            throw new Exception("無効なレシピIDの形式です: " + id);
        } catch (Exception e) {
            throw new Exception("レシピの取得中にエラーが発生しました: " + e.getMessage());
        }
    }
    
    private List<Recipe> createSampleRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        
        // サンプルレシピ1
        Recipe recipe1 = createSampleRecipe();
        recipes.add(recipe1);
        
        // サンプルレシピ2
        Recipe recipe2 = new Recipe();
        recipe2.setTitle("肉じゃが");
        recipe2.setDescription("ホクホクじゃがいもと甘辛い味付けが美味しい定番おかず");
        
        List<Ingredient> ingredients2 = new ArrayList<>();
        
        Ingredient ingredient1 = new Ingredient();
        ingredient1.setName("牛肉");
        ingredient1.setQuantity("200");
        ingredient1.setUnit("g");
        ingredient1.setNote("薄切り");
        ingredients2.add(ingredient1);
        
        Ingredient ingredient2 = new Ingredient();
        ingredient2.setName("じゃがいも");
        ingredient2.setQuantity("3");
        ingredient2.setUnit("個");
        ingredient2.setNote("一口大に切る");
        ingredients2.add(ingredient2);
        
        Ingredient ingredient3 = new Ingredient();
        ingredient3.setName("人参");
        ingredient3.setQuantity("1");
        ingredient3.setUnit("本");
        ingredient3.setNote("乱切り");
        ingredients2.add(ingredient3);
        
        Ingredient ingredient4 = new Ingredient();
        ingredient4.setName("玉ねぎ");
        ingredient4.setQuantity("1");
        ingredient4.setUnit("個");
        ingredient4.setNote("くし切り");
        ingredients2.add(ingredient4);
        
        recipe2.setIngredients(ingredients2);
        recipe2.setInstructions(Arrays.asList(
            "野菜を切る",
            "牛肉を炒める",
            "調味料と野菜を加えて煮込む",
            "落し蓋をして20分煮る"
        ));
        recipe2.setCookingTime(30);
        recipe2.setDifficulty(Recipe.Difficulty.EASY);
        recipe2.setServingSize(3);
        recipe2.setTags(Arrays.asList("和食", "煮物", "定番"));
        
        NutritionInfo nutritionInfo2 = new NutritionInfo();
        nutritionInfo2.setCalories(420);
        nutritionInfo2.setProtein("25g");
        nutritionInfo2.setCarbs("45g");
        nutritionInfo2.setFat("18g");
        recipe2.setNutritionInfo(nutritionInfo2);
        
        recipes.add(recipe2);
        
        // サンプルレシピ3
        Recipe recipe3 = new Recipe();
        recipe3.setTitle("ポテトと鶏肉のガーリックソテー");
        recipe3.setDescription("簡単なのに本格的な味わいのソテー");
        
        List<Ingredient> ingredients3 = new ArrayList<>();
        
        Ingredient ingredient31 = new Ingredient();
        ingredient31.setName("鶏もも肉");
        ingredient31.setQuantity("250");
        ingredient31.setUnit("g");
        ingredient31.setNote("一口大");
        ingredients3.add(ingredient31);
        
        Ingredient ingredient32 = new Ingredient();
        ingredient32.setName("じゃがいも");
        ingredient32.setQuantity("2");
        ingredient32.setUnit("個");
        ingredient32.setNote("くし切り");
        ingredients3.add(ingredient32);
        
        Ingredient ingredient33 = new Ingredient();
        ingredient33.setName("にんにく");
        ingredient33.setQuantity("2");
        ingredient33.setUnit("片");
        ingredient33.setNote("みじん切り");
        ingredients3.add(ingredient33);
        
        recipe3.setIngredients(ingredients3);
        recipe3.setInstructions(Arrays.asList(
            "じゃがいもをレンジで3分加熱",
            "フライパンでにんにくを炒める",
            "鶏肉を加えて焼く",
            "じゃがいもを加えて塩コショウで味付け"
        ));
        recipe3.setCookingTime(15);
        recipe3.setDifficulty(Recipe.Difficulty.EASY);
        recipe3.setServingSize(2);
        recipe3.setTags(Arrays.asList("洋風", "ソテー", "スピード"));
        
        NutritionInfo nutritionInfo3 = new NutritionInfo();
        nutritionInfo3.setCalories(380);
        nutritionInfo3.setProtein("28g");
        nutritionInfo3.setCarbs("25g");
        nutritionInfo3.setFat("20g");
        recipe3.setNutritionInfo(nutritionInfo3);
        
        recipes.add(recipe3);
        
        return recipes;
    }
    
    private List<Recipe> parseMultipleRecipes(String aiResponse) throws Exception {
        List<Recipe> recipes = new ArrayList<>();
        
        try {
            // JSON配列をパース
            JsonNode recipeArray = objectMapper.readTree(aiResponse);
            System.out.println("パース後のJSON構造: " + recipeArray.getNodeType());
            
            // 配列の場合
            if (recipeArray.isArray()) {
                System.out.println("配列形式のレシピデータを検出: " + recipeArray.size() + "件");
                for (JsonNode jsonNode : recipeArray) {
                    Recipe recipe = parseRecipeJson(jsonNode);
                    recipes.add(recipe);
                }
            } 
            // 単一のオブジェクトの場合
            else {
                System.out.println("単一オブジェクト形式のレシピデータを検出");
                Recipe recipe = parseRecipeJson(recipeArray);
                recipes.add(recipe);
            }
            
            System.out.println("パース完了: " + recipes.size() + "件のレシピを抽出");
            return recipes;
        } catch (Exception e) {
            System.err.println("JSON解析エラー: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("レシピのパースに失敗しました: " + e.getMessage());
        }
    }
    
    private Recipe parseRecipeJson(JsonNode jsonNode) throws Exception {
        Recipe recipe = new Recipe();
        recipe.setTitle(jsonNode.get("title").asText());
        recipe.setDescription(jsonNode.get("description").asText());
        
        // 材料の設定
        List<Ingredient> ingredients = new ArrayList<>();
        JsonNode ingredientNodes = jsonNode.get("ingredients");
        for (JsonNode ingredientNode : ingredientNodes) {
            Ingredient ingredient = new Ingredient();
            ingredient.setName(ingredientNode.get("name").asText());
            ingredient.setQuantity(ingredientNode.get("quantity").asText());
            
            // unitフィールドの処理（nullの場合があるため）
            if (ingredientNode.has("unit") && !ingredientNode.get("unit").isNull()) {
                ingredient.setUnit(ingredientNode.get("unit").asText());
            } else {
                ingredient.setUnit("");
            }
            
            // noteフィールドの処理（オプショナル）
            if (ingredientNode.has("note") && !ingredientNode.get("note").isNull()) {
                ingredient.setNote(ingredientNode.get("note").asText());
            }
            
            ingredients.add(ingredient);
        }
        recipe.setIngredients(ingredients);
        
        // 手順の設定
        List<String> instructions = new ArrayList<>();
        JsonNode instructionNodes = jsonNode.get("instructions");
        for (JsonNode instructionNode : instructionNodes) {
            String instruction = instructionNode.asText();
            
            // 手順の番号フォーマット（1., 1.1., Step 1:など）を削除
            instruction = instruction.replaceAll("^\\s*\\d+\\.\\d+\\.\\s*", ""); // 1.1. 形式を削除
            instruction = instruction.replaceAll("^\\s*\\d+\\.\\s*", "");  // 1. 形式を削除
            instruction = instruction.replaceAll("^\\s*Step\\s+\\d+[:\\. ]*\\s*", ""); // Step 1: 形式を削除
            
            // 先頭の空白を削除
            instruction = instruction.trim();
            
            // 最初の文字を大文字に（あれば）
            if (!instruction.isEmpty()) {
                instruction = Character.toUpperCase(instruction.charAt(0)) + instruction.substring(1);
            }
            
            instructions.add(instruction);
        }
        recipe.setInstructions(instructions);
        
        recipe.setCookingTime(jsonNode.get("cookingTime").asInt());
        recipe.setDifficulty(Recipe.Difficulty.valueOf(jsonNode.get("difficulty").asText()));
        recipe.setServingSize(jsonNode.get("servingSize").asInt());
        
        // タグの設定
        List<String> tags = new ArrayList<>();
        JsonNode tagNodes = jsonNode.get("tags");
        for (JsonNode tagNode : tagNodes) {
            tags.add(tagNode.asText());
        }
        recipe.setTags(tags);
        
        // 栄養情報があれば設定（オプション）
        if (jsonNode.has("nutritionInfo") && !jsonNode.get("nutritionInfo").isNull()) {
            JsonNode nutritionNode = jsonNode.get("nutritionInfo");
            NutritionInfo nutritionInfo = new NutritionInfo();
            
            if (nutritionNode.has("calories") && !nutritionNode.get("calories").isNull()) {
                nutritionInfo.setCalories(nutritionNode.get("calories").asInt());
            }
            if (nutritionNode.has("protein") && !nutritionNode.get("protein").isNull()) {
                nutritionInfo.setProtein(nutritionNode.get("protein").asText());
            }
            if (nutritionNode.has("carbs") && !nutritionNode.get("carbs").isNull()) {
                nutritionInfo.setCarbs(nutritionNode.get("carbs").asText());
            }
            if (nutritionNode.has("fat") && !nutritionNode.get("fat").isNull()) {
                nutritionInfo.setFat(nutritionNode.get("fat").asText());
            }
            
            recipe.setNutritionInfo(nutritionInfo);
        }
        
        return recipe;
    }
    
    private Recipe createSampleRecipe() {
        Recipe recipe = new Recipe();
        recipe.setTitle("鶏肉と野菜の簡単煮物");
        recipe.setDescription("ほくほくじゃがいもと柔らかい鶏肉の優しい味わい");
        
        // 材料の設定
        List<Ingredient> ingredients = new ArrayList<>();
        
        Ingredient ingredient1 = new Ingredient();
        ingredient1.setName("鶏もも肉");
        ingredient1.setQuantity("300");
        ingredient1.setUnit("g");
        ingredient1.setNote("一口大に切る");
        ingredients.add(ingredient1);
        
        Ingredient ingredient2 = new Ingredient();
        ingredient2.setName("じゃがいも");
        ingredient2.setQuantity("2");
        ingredient2.setUnit("個");
        ingredient2.setNote("4等分に切る");
        ingredients.add(ingredient2);
        
        Ingredient ingredient3 = new Ingredient();
        ingredient3.setName("人参");
        ingredient3.setQuantity("1");
        ingredient3.setUnit("本");
        ingredient3.setNote("乱切り");
        ingredients.add(ingredient3);
        
        recipe.setIngredients(ingredients);
        
        // 手順の設定
        recipe.setInstructions(Arrays.asList(
            "鶏肉を一口大に切ります",
            "じゃがいもは皮をむき、4等分に切ります",
            "人参は乱切りにします",
            "鍋に調味料と水を入れて沸騰させます",
            "具材を入れて中火で15分煮込みます"
        ));
        
        // その他の情報
        recipe.setCookingTime(25);
        recipe.setDifficulty(Recipe.Difficulty.EASY);
        recipe.setServingSize(2);
        recipe.setTags(Arrays.asList("和食", "煮物", "晩ごはん"));
        
        // 栄養情報
        NutritionInfo nutritionInfo = new NutritionInfo();
        nutritionInfo.setCalories(450);
        nutritionInfo.setProtein("28g");
        nutritionInfo.setCarbs("30g");
        nutritionInfo.setFat("22g");
        recipe.setNutritionInfo(nutritionInfo);
        
        return recipe;
    }
    
    private Map<String, Object> convertToMap(Recipe recipe) {
        // 材料の変換
        List<Map<String, Object>> ingredientMaps = recipe.getIngredients().stream()
            .map(i -> {
                Map<String, Object> map = new HashMap<>();
                map.put("name", i.getName());
                map.put("quantity", i.getQuantity());
                map.put("unit", i.getUnit());
                map.put("note", i.getNote() != null ? i.getNote() : "");
                return map;
            })
            .collect(Collectors.toList());
        
        // 栄養情報の変換
        Map<String, Object> nutritionMap = null;
        if (recipe.getNutritionInfo() != null) {
            nutritionMap = new HashMap<>();
            nutritionMap.put("calories", recipe.getNutritionInfo().getCalories());
            nutritionMap.put("protein", recipe.getNutritionInfo().getProtein());
            nutritionMap.put("carbs", recipe.getNutritionInfo().getCarbs());
            nutritionMap.put("fat", recipe.getNutritionInfo().getFat());
        }
        
        // レシピ情報の構築
        Map<String, Object> recipeMap = new HashMap<>();
        recipeMap.put("id", recipe.getId().toString());
        recipeMap.put("title", recipe.getTitle());
        recipeMap.put("description", recipe.getDescription());
        recipeMap.put("ingredients", ingredientMaps);
        recipeMap.put("instructions", recipe.getInstructions());
        recipeMap.put("cookingTime", recipe.getCookingTime());
        recipeMap.put("difficulty", recipe.getDifficulty().name());
        recipeMap.put("servingSize", recipe.getServingSize());
        recipeMap.put("tags", recipe.getTags());
        
        if (nutritionMap != null) {
            recipeMap.put("nutritionInfo", nutritionMap);
        }
        
        if (recipe.getImageUrl() != null) {
            recipeMap.put("imageUrl", recipe.getImageUrl());
        }
        
        return recipeMap;
    }
} 