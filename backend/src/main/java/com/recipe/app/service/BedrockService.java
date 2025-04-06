package com.recipe.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.List;
import java.util.Map;

@Service
public class BedrockService {

    private final BedrockRuntimeClient bedrockClient;
    private final ObjectMapper objectMapper;
    
    @Value("${aws.bedrock.model-id}")
    private String modelId;
    
    @Value("${aws.region}")
    private String awsRegion;
    
    public BedrockService() {
        this.bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.of(awsRegion != null ? awsRegion : "ap-northeast-1"))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    public String generateRecipe(List<String> ingredients, Map<String, Object> preferences) {
        try {
            // プロンプトの構築
            String prompt = buildRecipePrompt(ingredients, preferences);
            System.out.println("生成プロンプト: " + prompt);
            
            // Claude 3のリクエスト形式
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("anthropic_version", "bedrock-2023-05-31");
            requestBody.put("max_tokens", 4000);
            requestBody.put("temperature", 0.7);
            
            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            
            ArrayNode messages = objectMapper.createArrayNode();
            messages.add(userMessage);
            
            requestBody.set("messages", messages);
            
            // リクエストの準備
            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .body(software.amazon.awssdk.core.SdkBytes.fromUtf8String(requestBody.toString()))
                    .build();
            
            System.out.println("Bedrock APIリクエスト送信中...");
            
            // モデル呼び出し
            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();
            
            System.out.println("Bedrock APIレスポンス受信完了");
            
            // レスポンスの解析
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            
            // Claude 3のレスポンス形式に対応（content[0].text）
            StringBuilder generatedText = new StringBuilder();
            if (jsonResponse.has("content") && jsonResponse.get("content").isArray()) {
                JsonNode contentArray = jsonResponse.get("content");
                System.out.println("コンテンツ配列サイズ: " + contentArray.size());
                
                for (JsonNode contentItem : contentArray) {
                    if (contentItem.has("type") && contentItem.get("type").asText().equals("text")) {
                        String text = contentItem.get("text").asText();
                        System.out.println("テキストコンテンツを抽出: " + text.substring(0, Math.min(100, text.length())) + "...");
                        generatedText.append(text);
                    }
                }
            } else {
                System.out.println("予期しないレスポンス形式: " + jsonResponse.toString().substring(0, 500) + "...");
            }
            
            if (generatedText.length() == 0) {
                throw new RuntimeException("AIモデルからの応答を解析できませんでした");
            }
            
            // JSONを抽出する処理を追加
            String cleanedJson = extractJsonArray(generatedText.toString());
            return cleanedJson;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("AIモデル呼び出し中にエラーが発生しました: " + e.getMessage(), e);
        }
    }
    
    private String buildRecipePrompt(List<String> ingredients, Map<String, Object> preferences) {
        StringBuilder promptBuilder = new StringBuilder();
        
        // プロンプトの基本構造
        promptBuilder.append("以下の食材から3種類のレシピを作成してください。結果はJSON形式の配列で返してください。\n\n");
        
        // 食材リスト
        promptBuilder.append("食材:\n");
        for (String ingredient : ingredients) {
            promptBuilder.append("- ").append(ingredient).append("\n");
        }
        
        // 好み/条件
        if (preferences != null && !preferences.isEmpty()) {
            promptBuilder.append("\n条件:\n");
            
            if (preferences.containsKey("cookingTime")) {
                promptBuilder.append("- 調理時間: ").append(preferences.get("cookingTime")).append("\n");
            }
            
            if (preferences.containsKey("difficulty")) {
                promptBuilder.append("- 難易度: ").append(preferences.get("difficulty")).append("\n");
            }
            
            if (preferences.containsKey("servingSize")) {
                promptBuilder.append("- 人数: ").append(preferences.get("servingSize")).append("人分\n");
            }
            
            if (preferences.containsKey("cuisineType")) {
                promptBuilder.append("- 料理タイプ: ").append(preferences.get("cuisineType")).append("\n");
            }
        }
        
        // 期待する出力形式
        promptBuilder.append("\n以下のJSON形式の配列で3種類のレシピを出力してください:\n");
        promptBuilder.append("[\n");
        promptBuilder.append("  {\n");
        promptBuilder.append("    \"title\": \"レシピタイトル\",\n");
        promptBuilder.append("    \"description\": \"簡単な説明\",\n");
        promptBuilder.append("    \"ingredients\": [\n");
        promptBuilder.append("      { \"name\": \"材料名\", \"quantity\": \"数量\", \"unit\": \"単位\", \"note\": \"備考\" }\n");
        promptBuilder.append("    ],\n");
        promptBuilder.append("    \"instructions\": [\"手順1\", \"手順2\", ...],\n");
        promptBuilder.append("    \"cookingTime\": 30,\n");
        promptBuilder.append("    \"difficulty\": \"EASY\",\n");
        promptBuilder.append("    \"servingSize\": 2,\n");
        promptBuilder.append("    \"tags\": [\"和食\", \"煮物\", ...]\n");
        promptBuilder.append("  },\n");
        promptBuilder.append("  {...},\n");
        promptBuilder.append("  {...}\n");
        promptBuilder.append("]\n");
        
        return promptBuilder.toString();
    }

    /**
     * テキストからJSON配列部分を抽出します
     * @param text 処理対象のテキスト
     * @return 抽出されたJSON配列文字列
     */
    private String extractJsonArray(String text) {
        System.out.println("JSON抽出前のテキスト長: " + text.length());
        
        // 角括弧で囲まれた部分を探す
        int startIdx = text.indexOf('[');
        int endIdx = text.lastIndexOf(']') + 1;
        
        if (startIdx >= 0 && endIdx > startIdx) {
            String extractedJson = text.substring(startIdx, endIdx);
            System.out.println("JSON抽出後のテキスト長: " + extractedJson.length());
            return extractedJson;
        }
        
        // 角括弧がない場合は中括弧のみを探す（単一オブジェクト）
        startIdx = text.indexOf('{');
        endIdx = text.lastIndexOf('}') + 1;
        
        if (startIdx >= 0 && endIdx > startIdx) {
            String extractedJson = text.substring(startIdx, endIdx);
            System.out.println("JSON抽出後のテキスト長: " + extractedJson.length());
            return extractedJson;
        }
        
        // どちらも見つからなかった場合は元のテキストを返す
        System.out.println("JSONが見つかりませんでした");
        return text;
    }
} 