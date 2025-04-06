package com.recipe.app.service;

import com.recipe.app.dto.RecipeRequest;
import com.recipe.app.dto.RecipeResponse;

/**
 * レシピ関連のビジネスロジックを提供するサービスインターフェース
 */
public interface RecipeService {
    
    /**
     * 指定された食材と条件から、おすすめのレシピを提案します
     *
     * @param request レシピリクエスト情報
     * @return レシピ提案のレスポンス
     */
    RecipeResponse suggestRecipes(RecipeRequest request);
    
    /**
     * 指定されたIDのレシピを取得します
     *
     * @param id レシピID
     * @return レシピ情報を含むレスポンス
     * @throws Exception レシピが見つからない場合
     */
    RecipeResponse getRecipeById(String id) throws Exception;
} 