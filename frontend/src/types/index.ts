export interface Recipe {
  id: string;
  title: string;
  description: string;
  ingredients: Ingredient[];
  instructions: string[];
  cookingTime: number;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  servingSize: number;
  tags: string[];
  nutritionInfo?: NutritionInfo;
  imageUrl?: string;
  isFavorite?: boolean;
}

export interface Ingredient {
  name: string;
  quantity: string;
  unit: string;
  note?: string;
}

export interface NutritionInfo {
  calories: number;
  protein: string;
  carbs: string;
  fat: string;
}

export interface RecipeRequest {
  ingredients: string[];
  preferences?: {
    cookingTime?: string;
    difficulty?: string;
    cuisineType?: string;
    mealType?: string;
    calorieLevel?: string;
    servingSize?: number;
  };
  excludedIngredients?: string[];
}

export interface RecipeResponse {
  success: boolean;
  data: {
    recipes: Recipe[];
    generationId: string;
  };
  message?: string;
} 