'use client';

import { useState } from 'react';
import { IngredientForm } from '@/components/ingredient-form';
import { RecipeList } from '@/components/recipe-list';
import { Recipe, RecipeRequest } from '@/types';

export default function Home() {
  const [isLoading, setIsLoading] = useState(false);
  const [recipes, setRecipes] = useState<Recipe[]>([]);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (ingredients: string[]) => {
    setIsLoading(true);
    setError(null);
    
    try {
      // リクエストの準備
      const request: RecipeRequest = {
        ingredients,
        preferences: {
          cookingTime: 'UNDER_30_MIN',
          difficulty: 'EASY',
          servingSize: 2
        }
      };
      
      // APIを呼び出す
      const response = await fetch('http://localhost:8080/api/v1/recipes/suggest', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(request),
      });
      
      if (!response.ok) {
        throw new Error(`APIエラー: ${response.status}`);
      }
      
      const result = await response.json();
      
      if (!result.success) {
        throw new Error(result.message || 'レシピの取得に失敗しました');
      }
      
      setRecipes(result.data.recipes);
      setIsLoading(false);
    } catch (err) {
      console.error('レシピ検索エラー:', err);
      setError('レシピの取得に失敗しました。もう一度お試しください。');
      setIsLoading(false);
    }
  };

  return (
    <main className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-7xl mx-auto">
        <h1 className="text-3xl font-bold text-center text-gray-900 mb-8">
          AIレシピ提案
        </h1>
        
        <IngredientForm onSubmit={handleSubmit} isLoading={isLoading} />
        
        {error && (
          <div className="mt-8 p-4 bg-red-50 border border-red-200 rounded-lg text-red-600">
            {error}
          </div>
        )}
        
        <RecipeList recipes={recipes} isLoading={isLoading} />
      </div>
    </main>
  );
}
