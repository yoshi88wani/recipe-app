package com.recipe.app.repository;

import com.recipe.app.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    
    // タイトルで検索
    List<Recipe> findByTitleContaining(String title);
    
    // 調理時間と難易度で検索
    List<Recipe> findByCookingTimeLessThanEqualAndDifficulty(int cookingTime, Recipe.Difficulty difficulty);
    
    // 特定の食材を含むレシピを検索
    @Query("SELECT r FROM Recipe r JOIN r.ingredients i WHERE i.name IN :ingredients")
    List<Recipe> findByIngredientsNameIn(List<String> ingredients);
    
    // タグで検索
    List<Recipe> findByTagsContaining(String tag);
}