'use client';

import { useState } from 'react';
import { PlusCircle, Trash2 } from 'lucide-react';

interface IngredientFormProps {
  onSubmit: (ingredients: string[]) => void;
  isLoading?: boolean;
}

export function IngredientForm({ onSubmit, isLoading = false }: IngredientFormProps) {
  const [ingredients, setIngredients] = useState<string[]>(['']);
  
  const handleAddIngredient = () => {
    setIngredients([...ingredients, '']);
  };
  
  const handleRemoveIngredient = (index: number) => {
    if (ingredients.length === 1) return;
    const newIngredients = [...ingredients];
    newIngredients.splice(index, 1);
    setIngredients(newIngredients);
  };
  
  const handleChange = (index: number, value: string) => {
    const newIngredients = [...ingredients];
    newIngredients[index] = value;
    setIngredients(newIngredients);
  };
  
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const validIngredients = ingredients.filter(ingredient => ingredient.trim() !== '');
    if (validIngredients.length > 0) {
      onSubmit(validIngredients);
    }
  };
  
  return (
    <div className="w-full max-w-2xl mx-auto bg-white rounded-xl shadow-md p-6">
      <h2 className="text-2xl font-bold mb-4 text-gray-800">食材を入力してください</h2>
      <p className="text-gray-600 mb-6">冷蔵庫にある食材を入力して、AIにレシピを提案してもらいましょう</p>
      
      <form onSubmit={handleSubmit}>
        <div className="space-y-3">
          {ingredients.map((ingredient, index) => (
            <div key={index} className="flex items-center gap-2">
              <input
                type="text"
                value={ingredient}
                onChange={(e) => handleChange(index, e.target.value)}
                placeholder="例：じゃがいも、玉ねぎ、鶏もも肉"
                className="flex-1 p-3 border border-gray-300 rounded-lg bg-white text-gray-800 font-medium placeholder:text-gray-400 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 shadow-sm"
              />
              <button
                type="button"
                onClick={() => handleRemoveIngredient(index)}
                disabled={ingredients.length === 1}
                className="p-2 text-gray-500 hover:text-red-500 disabled:opacity-30"
                aria-label="食材を削除"
              >
                <Trash2 size={20} />
              </button>
            </div>
          ))}
        </div>
        
        <div className="mt-4 flex flex-col sm:flex-row sm:items-center gap-3">
          <button
            type="button"
            onClick={handleAddIngredient}
            className="flex items-center justify-center gap-1 text-blue-600 hover:text-blue-800"
          >
            <PlusCircle size={18} />
            <span>食材を追加</span>
          </button>
          
          <div className="flex-1 sm:text-right">
            <button
              type="submit"
              disabled={isLoading}
              className={`px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 ${
                isLoading ? 'opacity-70 cursor-not-allowed' : ''
              }`}
            >
              {isLoading ? '検索中...' : 'レシピを提案してもらう'}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}

export default IngredientForm; 