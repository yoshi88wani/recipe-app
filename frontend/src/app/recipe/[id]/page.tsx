'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import { Recipe } from '@/types';
import Link from 'next/link';

export default function RecipeDetail() {
  const params = useParams();
  const recipeId = params.id as string;

  const [recipe, setRecipe] = useState<Recipe | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchRecipe = async () => {
      setIsLoading(true);
      setError(null);

      try {
        // APIを呼び出す
        const response = await fetch(`http://localhost:8080/api/v1/recipes/${recipeId}`);

        if (!response.ok) {
          throw new Error(`APIエラー: ${response.status}`);
        }

        const result = await response.json();

        if (!result.success) {
          throw new Error(result.message || 'レシピの取得に失敗しました');
        }

        setRecipe(result.data.recipe);
      } catch (err) {
        console.error('レシピ取得エラー:', err);
        setError('レシピの取得に失敗しました。もう一度お試しください。');
      } finally {
        setIsLoading(false);
      }
    };

    fetchRecipe();
  }, [recipeId]);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 py-12 px-4">
        <div className="max-w-4xl mx-auto">
          <div className="flex justify-center mt-12">
            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 py-12 px-4">
        <div className="max-w-4xl mx-auto">
          <div className="p-6 bg-red-50 border border-red-200 rounded-lg">
            <h1 className="text-xl font-semibold text-red-600 mb-2">エラーが発生しました</h1>
            <p className="text-red-600">{error}</p>
            <Link href="/" className="mt-4 inline-block px-4 py-2 bg-blue-600 text-white rounded-lg">
              トップに戻る
            </Link>
          </div>
        </div>
      </div>
    );
  }

  if (!recipe) {
    return (
      <div className="min-h-screen bg-gray-50 py-12 px-4">
        <div className="max-w-4xl mx-auto">
          <div className="p-6 bg-yellow-50 border border-yellow-200 rounded-lg">
            <h1 className="text-xl font-semibold text-yellow-600 mb-2">レシピが見つかりません</h1>
            <p className="text-yellow-600">指定されたレシピは存在しないか、削除された可能性があります。</p>
            <Link href="/" className="mt-4 inline-block px-4 py-2 bg-blue-600 text-white rounded-lg">
              トップに戻る
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-4xl mx-auto">
        <div className="mb-6">
          <Link href="/" className="text-blue-600 hover:underline flex items-center">
            <span className="mr-1">←</span> トップに戻る
          </Link>
        </div>

        <div className="bg-white rounded-xl shadow-md overflow-hidden">
          <div className="p-6">
            <h1 className="text-3xl font-bold mb-2 text-gray-900">{recipe.title}</h1>
            <p className="text-lg text-gray-600 mb-6">{recipe.description}</p>

            <div className="flex flex-wrap gap-2 mb-6">
              {recipe.tags && recipe.tags.map((tag, index) => (
                <span
                  key={`${tag}-${index}`}
                  className="px-3 py-1 bg-blue-100 text-blue-800 text-sm rounded-full"
                >
                  {tag}
                </span>
              ))}
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
              <div className="flex flex-col items-center justify-center p-4 bg-gray-50 rounded-lg">
                <span className="text-sm text-gray-500">調理時間</span>
                <span className="text-xl font-semibold">{recipe.cookingTime}分</span>
              </div>
              <div className="flex flex-col items-center justify-center p-4 bg-gray-50 rounded-lg">
                <span className="text-sm text-gray-500">難易度</span>
                <span className="text-xl font-semibold">
                  {recipe.difficulty === 'EASY' ? '簡単' :
                   recipe.difficulty === 'MEDIUM' ? '普通' : '難しい'}
                </span>
              </div>
              <div className="flex flex-col items-center justify-center p-4 bg-gray-50 rounded-lg">
                <span className="text-sm text-gray-500">人数</span>
                <span className="text-xl font-semibold">{recipe.servingSize}人分</span>
              </div>
            </div>

            <div className="mb-8">
              <h2 className="text-xl font-semibold mb-4 pb-2 border-b border-gray-200">材料</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-2">
                {recipe.ingredients && recipe.ingredients.map((ingredient, index) => (
                  <div key={index} className="flex justify-between py-2">
                    <span className="text-gray-800">
                      {ingredient.name}
                      {ingredient.note && <span className="text-gray-500 text-sm ml-1">（{ingredient.note}）</span>}
                    </span>
                    <span className="text-gray-600">
                      {ingredient.quantity} {ingredient.unit}
                    </span>
                  </div>
                ))}
              </div>
            </div>

            <div>
              <h2 className="text-xl font-semibold mb-4 pb-2 border-b border-gray-200">作り方</h2>
              <ol className="list-decimal pl-6 space-y-4">
                {recipe.instructions && recipe.instructions.map((step, index) => (
                  <li key={index} className="text-gray-800 pl-2">{step}</li>
                ))}
              </ol>
            </div>

            {recipe.nutritionInfo && (
              <div className="mt-8 pt-6 border-t border-gray-200">
                <h2 className="text-xl font-semibold mb-4">栄養成分（1人前）</h2>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <div className="p-3 bg-gray-50 rounded-lg text-center">
                    <span className="block text-sm text-gray-500">カロリー</span>
                    <span className="font-semibold">{recipe.nutritionInfo.calories}kcal</span>
                  </div>
                  <div className="p-3 bg-gray-50 rounded-lg text-center">
                    <span className="block text-sm text-gray-500">タンパク質</span>
                    <span className="font-semibold">{recipe.nutritionInfo.protein}</span>
                  </div>
                  <div className="p-3 bg-gray-50 rounded-lg text-center">
                    <span className="block text-sm text-gray-500">炭水化物</span>
                    <span className="font-semibold">{recipe.nutritionInfo.carbs}</span>
                  </div>
                  <div className="p-3 bg-gray-50 rounded-lg text-center">
                    <span className="block text-sm text-gray-500">脂質</span>
                    <span className="font-semibold">{recipe.nutritionInfo.fat}</span>
                  </div>
                </div>
              </div>
            )}

            <div className="mt-8 flex justify-center">
              <button className="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg">
                お気に入りに追加
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
} 