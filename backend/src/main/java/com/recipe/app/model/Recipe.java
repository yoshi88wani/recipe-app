package com.recipe.app.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Index;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes", indexes = {
    @jakarta.persistence.Index(name = "idx_recipe_title", columnList = "title")
})
@Data
public class Recipe {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String description;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "recipe_ingredients", 
        joinColumns = @JoinColumn(name = "recipe_id"),
        indexes = @jakarta.persistence.Index(name = "idx_ingredient_name", columnList = "name")
    )
    private List<Ingredient> ingredients = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "recipe_instructions", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "instruction_step")
    private List<String> instructions = new ArrayList<>();
    
    private Integer cookingTime;
    
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
    
    private Integer servingSize;
    
    @ElementCollection
    @CollectionTable(name = "recipe_tags", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();
    
    @Embedded
    private NutritionInfo nutritionInfo;
    
    private String imageUrl;
    
    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
    
    // JPA用の引数なしコンストラクタ
    public Recipe() {}
} 