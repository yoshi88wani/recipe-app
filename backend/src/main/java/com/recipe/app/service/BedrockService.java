package com.recipe.app.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

@Service
public class BedrockService {

    private final BedrockRuntimeClient bedrockClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.bedrock.model-id}")
    private String modelId;

    @Value("${aws.region}")
    private String awsRegion;

    // 新しいパラメータ設定
    @Value("${aws.bedrock.parameters.temperature:0.7}")
    private double temperature;

    @Value("${aws.bedrock.parameters.max-tokens:4000}")
    private int maxTokens;

    @Value("${aws.bedrock.parameters.top-p:0.9}")
    private double topP;

    @Value("${aws.bedrock.connection.max-retries:3}")
    private int maxRetries;

    @Value("${aws.bedrock.logging.enabled:false}")
    private boolean loggingEnabled;

    @Value("${aws.bedrock.logging.include-request-body:false}")
    private boolean includeRequestBody;


    public BedrockService() {
        this.bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.of(awsRegion != null ? awsRegion : "ap-northeast-1"))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        this.objectMapper = new ObjectMapper();
    }
    /**
     * レシピの生成を行います
     */
    public String generateRecipe(List<String> ingredients, Map<String, Object> preferences) {
        try {
            // プロンプトの構築
            String prompt = buildRecipePrompt(ingredients, preferences);

            // ロギングが有効な場合はプロンプトを出力
            if (loggingEnabled) {
                System.out.println("生成プロンプト: " + prompt);

                if (includeRequestBody) {
                    System.out.println("食材リスト: " + ingredients);
                    System.out.println("条件: " + preferences);
                }
            }

            // Claude 3のリクエスト形式
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("anthropic_version", "bedrock-2023-05-31");
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);
            requestBody.put("top_p", topP);

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

            if (loggingEnabled) {
                System.out.println("Bedrock APIリクエスト送信中...");
            }

            // モデル呼び出し
            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();

            if (loggingEnabled) {
                System.out.println("Bedrock APIレスポンス受信完了");
            }

            // レスポンスの解析
            JsonNode jsonResponse = objectMapper.readTree(responseBody);

            // Claude 3のレスポンス形式に対応（content[0].text）
            StringBuilder generatedText = new StringBuilder();
            if (jsonResponse.has("content") && jsonResponse.get("content").isArray()) {
                JsonNode contentArray = jsonResponse.get("content");

                if (loggingEnabled) {
                    System.out.println("コンテンツ配列サイズ: " + contentArray.size());
                }

                for (JsonNode contentItem : contentArray) {
                    if (contentItem.has("type") && contentItem.get("type").asText().equals("text")) {
                        String text = contentItem.get("text").asText();

                        if (loggingEnabled) {
                            System.out.println("テキストコンテンツを抽出: " + text.substring(0, Math.min(100, text.length())) + "...");
                        }

                        generatedText.append(text);
                    }
                }
            } else if (loggingEnabled) {
                System.out.println("予期しないレスポンス形式: " + jsonResponse.toString().substring(0, 500) + "...");
            }

            if (generatedText.isEmpty()) {
                throw new RuntimeException("AIモデルからの応答を解析できませんでした");
            }

            // JSONを抽出する処理を追加
            return extractJsonArray(generatedText.toString());

        } catch (Exception e) {
            // ここで例外をスローせず、上位のリトライロジックに任せる
            throw new RuntimeException(e);
        }
    }

    private String buildRecipePrompt(List<String> ingredients, Map<String, Object> preferences) {
        StringBuilder promptBuilder = new StringBuilder();

        // 難易度コードとデフォルト値の準備
        String difficultyCode = "EASY"; // デフォルト値
        String difficultyDescription = "簡単（初心者でも作れる料理）";

        // レシピ数の取得（デフォルトは3）
        int recipeCount = 3;

        // 難易度コードを取得
        if (preferences != null && preferences.containsKey("difficulty")) {
            difficultyCode = (String) preferences.get("difficulty");

            // 難易度に基づいた詳細な説明を設定（簡潔化）
            switch (difficultyCode) {
                case "MEDIUM":
                    difficultyDescription = "普通（基本的な料理知識が必要）";
                    break;
                case "HARD":
                    difficultyDescription = "難しい（料理上級者向け）";
                    break;
                case "EASY":
                default:
                    difficultyDescription = "簡単（初心者でも作れる料理）";
                    difficultyCode = "EASY";
                    break;
            }
        }

        // プロンプト
        promptBuilder.append("あなたは料理のプロフェッショナルです。以下の食材を使った")
                .append(recipeCount).append("つのレシピを作成してください。\n")
                .append("難易度は【").append(difficultyDescription).append("】です。\n\n");

        // 食材リスト
        promptBuilder.append("食材:\n");
        for (String ingredient : ingredients) {
            promptBuilder.append("- ").append(ingredient).append("\n");
        }

        // 好み/条件（簡略化）
        promptBuilder.append("\n【条件】\n");
        promptBuilder.append("- 難易度: ").append(difficultyCode).append("\n");

        if (preferences != null) {
            if (preferences.containsKey("cookingTime")) {
                promptBuilder.append("- 調理時間: ").append(preferences.get("cookingTime")).append("分\n");
            }

            if (preferences.containsKey("servingSize")) {
                promptBuilder.append("- 人数: ").append(preferences.get("servingSize")).append("人分\n");
            }

            if (preferences.containsKey("cuisineType")) {
                promptBuilder.append("- 料理タイプ: ").append(preferences.get("cuisineType")).append("\n");
            }
        }

        // JSON出力形式を厳格に指定
        promptBuilder.append("\n【重要】以下のJSON形式の配列でレシピを出力してください。説明文は一切不要です。:\n");
        promptBuilder.append("[\n");
        promptBuilder.append("  {\n");
        promptBuilder.append("    \"title\": \"レシピタイトル\",\n");
        promptBuilder.append("    \"description\": \"簡単な説明\",\n");
        promptBuilder.append("    \"ingredients\": [\n");
        promptBuilder.append("      { \"name\": \"材料名\", \"quantity\": \"数量\", \"unit\": \"単位\", \"note\": \"備考\" }\n");
        promptBuilder.append("    ],\n");
        promptBuilder.append("    \"instructions\": [\"手順1\", \"手順2\", \"手順3\"],\n");
        promptBuilder.append("    \"cookingTime\": 30,\n");
        promptBuilder.append("    \"difficulty\": \"" + difficultyCode + "\",\n");
        promptBuilder.append("    \"servingSize\": 2,\n");
        promptBuilder.append("    \"tags\": [\"和食\", \"煮物\", \"簡単\"]\n");
        promptBuilder.append("  }\n");
        promptBuilder.append("]\n\n");
        promptBuilder.append("【注意】\n");
        promptBuilder.append("・JSONのみを出力し、他の説明文は一切不要です\n");
        promptBuilder.append("・有効なJSONフォーマットに必ず従ってください\n");
        promptBuilder.append("・レシピ数は").append(recipeCount).append("つのみにしてください\n");
        promptBuilder.append("・手順（instructions）には番号（1., 2., 1.1.など）を付けないでください。単純な文として記述してください");

        return promptBuilder.toString();
    }

    /**
     * テキストからJSON配列部分を抽出します
     *
     * @param text 処理対象のテキスト
     * @return 抽出されたJSON配列文字列
     */
    private String extractJsonArray(String text) {
        if (loggingEnabled) {
            System.out.println("JSON抽出前のテキスト長: " + text.length());
        }

        // 角括弧で囲まれた部分を探す
        int startIdx = text.indexOf('[');
        int endIdx = text.lastIndexOf(']') + 1;

        if (startIdx >= 0 && endIdx > startIdx) {
            String extractedJson = text.substring(startIdx, endIdx);

            if (loggingEnabled) {
                System.out.println("JSON抽出後のテキスト長: " + extractedJson.length());
            }

            return extractedJson;
        }

        // 角括弧がない場合は中括弧のみを探す（単一オブジェクト）
        startIdx = text.indexOf('{');
        endIdx = text.lastIndexOf('}') + 1;

        if (startIdx >= 0 && endIdx > startIdx) {
            String extractedJson = text.substring(startIdx, endIdx);

            if (loggingEnabled) {
                System.out.println("JSON抽出後のテキスト長: " + extractedJson.length());
            }

            return extractedJson;
        }

        // どちらも見つからなかった場合は元のテキストを返す
        if (loggingEnabled) {
            System.out.println("JSONが見つかりませんでした");
        }

        return text;
    }
}