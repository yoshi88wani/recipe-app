'use client';

import { Recipe } from '@/types';

interface RecipeListProps {
  recipes: Recipe[];
  isLoading: boolean;
}

export function RecipeList({ recipes, isLoading }: RecipeListProps) {
  if (isLoading) {
    return (
      <div className="mt-8 flex justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  if (recipes.length === 0) {
    return null;
  }

  return (
    <div className="mt-12">
      <h2 className="text-2xl font-bold mb-6 text-gray-800">提案レシピ</h2>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {recipes.map((recipe) => (
          <div key={recipe.id} className="bg-white rounded-xl shadow-md overflow-hidden">
            <div className="p-6">
              <h3 className="text-xl font-semibold mb-2">{recipe.title}</h3>
              <p className="text-gray-600 mb-4">{recipe.description}</p>
              
              <div className="flex flex-wrap gap-2 mb-4">
                {recipe.tags.map((tag) => (
                  <span key={tag} className="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded-full">
                    {tag}
                  </span>
                ))}
              </div>
              
              <div className="flex justify-between text-sm text-gray-500">
                <span>調理時間: {recipe.cookingTime}分</span>
                <span>難易度: {recipe.difficulty === 'EASY' ? '簡単' : recipe.difficulty === 'MEDIUM' ? '普通' : '難しい'}</span>
              </div>
              
              <button className="mt-4 w-full py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg">
                詳細を見る
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default RecipeList; 