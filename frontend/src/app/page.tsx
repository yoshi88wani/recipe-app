'use client';

import { useState, useEffect } from 'react';
import { IngredientForm } from '@/components/ingredient-form';
import { RecipeList } from '@/components/recipe-list';
import { Recipe, RecipeRequest } from '@/types';

// 検索結果の永続化のための型
interface StoredRecipeData {
  recipes: Recipe[];
  timestamp: number;
}

// 検索結果を保存する有効期限（30分）
const STORAGE_EXPIRY_TIME = 30 * 60 * 1000;

export default function Home() {
  const [isLoading, setIsLoading] = useState(false);
  const [recipes, setRecipes] = useState<Recipe[]>([]);
  const [error, setError] = useState<string | null>(null);

  // コンポーネントのマウント時に保存された検索結果をロード
  useEffect(() => {
    try {
      const storedData = localStorage.getItem('recipeSearchResults');
      if (storedData) {
        const parsedData: StoredRecipeData = JSON.parse(storedData);
        const now = Date.now();
        
        // 有効期限内のデータのみ使用
        if (now - parsedData.timestamp < STORAGE_EXPIRY_TIME) {
          setRecipes(parsedData.recipes);
        } else {
          // 期限切れの場合はデータを削除
          localStorage.removeItem('recipeSearchResults');
        }
      }
    } catch (e) {
      console.error('保存されたレシピデータの読み込みエラー:', e);
      localStorage.removeItem('recipeSearchResults');
    }
  }, []);

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
      console.log('APIレスポンス:', result);
      
      if (!result.success) {
        throw new Error(result.message || 'レシピの取得に失敗しました');
      }
      
      // データの存在確認と安全な処理
      if (result.data && Array.isArray(result.data.recipes)) {
        console.log(`${result.data.recipes.length}件のレシピを取得しました`);
        setRecipes(result.data.recipes);
        
        // localStorageに検索結果を保存
        const storageData: StoredRecipeData = {
          recipes: result.data.recipes,
          timestamp: Date.now()
        };
        localStorage.setItem('recipeSearchResults', JSON.stringify(storageData));
      } else if (result.data && result.data.recipes) {
        // 配列でない場合は配列にラップする
        const recipesArray = Array.isArray(result.data.recipes) ? result.data.recipes : [result.data.recipes];
        console.log(`${recipesArray.length}件のレシピ（配列変換後）を取得しました`);
        setRecipes(recipesArray);
        
        // localStorageに保存
        const storageData: StoredRecipeData = {
          recipes: recipesArray,
          timestamp: Date.now()
        };
        localStorage.setItem('recipeSearchResults', JSON.stringify(storageData));
      } else {
        console.error('レシピデータが不正な形式です:', result);
        setError('レシピデータの形式が正しくありません。');
      }
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
