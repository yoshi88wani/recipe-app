# AI Recipe Generator(※実装途中です)

## 📑 概要

AI Recipe Generatorは、冷蔵庫にある食材からレシピを自動生成するアプリケーションです。AWS Bedrockを活用したAI機能により、ユーザーの好みや条件に合わせたパーソナライズされたレシピを提案します。
Cursorの検証も兼ねておりますので、ドキュメントと実装の多少のズレはご容赦ください。

## 🎯 目的

- 食材を無駄なく活用し、フードロスを削減
- 料理の幅を広げる創造的なレシピの提案
- 使いやすいUIでユーザー体験を向上

## 💻 技術スタック

```mermaid
graph TD
    A[フロントエンド: Next.js] --- B[バックエンド: Spring Boot]
    B --- C[AI: AWS Bedrock/Claude]
    B --- D[認証: Amazon Cognito]
    B --- E[データ: Amazon RDS]
    A --- F[配信: CloudFront/S3]
    B --- G[API管理: API Gateway]
```

### フロントエンド
- **Next.js 14** (App Router)
- **TypeScript**
- **Tailwind CSS** / **shadcn/ui**
- **Zustand** (状態管理)

### バックエンド
- **Spring Boot 3.2**
- **AWS Lambda** (サーバーレス)(予定)
- **Amazon RDS** (PostgreSQL)(予定)

### クラウドサービス
- **AWS Bedrock** (Claude AI モデル)
- **Amazon Cognito** (認証)(予定)
- **API Gateway** (REST API)(予定)
- **AWS CDK** (インフラのコード化)(予定)

## 🚀 主要機能

1. **🥗 食材ベースのレシピ生成**
   - 冷蔵庫の食材を入力してAIレシピを生成
   - 調理時間、難易度、料理タイプでカスタマイズ

2. **👤 ユーザー管理**
   - 好みや食事制限の設定(予定)
   - お気に入りレシピの保存(予定)

3. **🛒 食材管理**
   - 在庫食材の管理と賞味期限通知(予定)
   - 買い物リスト生成(予定)

## 📂 プロジェクト構成

```
recipe-app/
├── docs/           # プロジェクトドキュメント
├── frontend/       # Next.jsフロントエンド
├── backend/        # Spring Bootバックエンド
└── infrastructure/ # AWS CDKインフラコード(予定)
```

## 📖 ドキュメント

- [システム設計](./system-design.md) - アーキテクチャとAI連携の詳細
- [機能仕様](./functional-specs.md) - 機能とAPI仕様
- [運用ガイド](./operations.md) - 認証と運用手順
