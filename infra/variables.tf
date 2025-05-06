/**
 * variables.tf - 変数定義ファイル
 * このファイルでは、プロジェクト全体で使用する変数を定義します
 */

# リージョン設定
variable "aws_region" {
  description = "AWS リージョン"
  type        = string
  default     = "ap-northeast-1"
}

# 環境名
variable "environment" {
  description = "デプロイ環境（dev, staging, prod）"
  type        = string
  default     = "dev"
}

# アプリケーション名
variable "app_name" {
  description = "アプリケーションの名前"
  type        = string
  default     = "recipe-app"
}

# VPC設定
variable "vpc_cidr" {
  description = "VPCのCIDRブロック"
  type        = string
  default     = "10.0.0.0/16"
}

# サブネット設定
variable "public_subnet_cidrs" {
  description = "パブリックサブネットのCIDRブロックのリスト"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "プライベートサブネットのCIDRブロックのリスト"
  type        = list(string)
  default     = ["10.0.3.0/24", "10.0.4.0/24"]
}

# コンテナ設定
variable "frontend_container_image" {
  description = "フロントエンドコンテナのイメージURI"
  type        = string
  default     = "761605341397.dkr.ecr.ap-northeast-1.amazonaws.com/recipe-app-frontend:latest"
}

variable "backend_container_image" {
  description = "バックエンドコンテナのイメージURI"
  type        = string
  default     = "761605341397.dkr.ecr.ap-northeast-1.amazonaws.com/recipe-app-backend:latest"
}

variable "backend_container_port" {
  description = "バックエンドコンテナのポート"
  type        = number
  default     = 8080
}

variable "frontend_container_port" {
  description = "フロントエンドコンテナのポート"
  type        = number
  default     = 3000
}

# ECS設定
variable "frontend_cpu" {
  description = "フロントエンドタスクのCPU単位"
  type        = number
  default     = 256
}

variable "frontend_memory" {
  description = "フロントエンドタスクのメモリ（MB）"
  type        = number
  default     = 512
}

variable "backend_cpu" {
  description = "バックエンドタスクのCPU単位"
  type        = number
  default     = 512
}

variable "backend_memory" {
  description = "バックエンドタスクのメモリ（MB）"
  type        = number
  default     = 1024
}

# ALB設定
variable "health_check_path_frontend" {
  description = "フロントエンドのヘルスチェックパス"
  type        = string
  default     = "/"
}

variable "health_check_path_backend" {
  description = "バックエンドのヘルスチェックパス"
  type        = string
  default     = "/actuator/health"
}

# RDS設定
variable "db_allocated_storage" {
  description = "RDSデータベースの割り当てストレージ (GB)"
  type        = number
  default     = 20
}

variable "db_instance_class" {
  description = "RDSインスタンスクラス"
  type        = string
  default     = "db.t3.micro"
}

variable "db_username" {
  description = "データベース管理者ユーザー名"
  type        = string
  default     = "recipeapp"
}

variable "db_password" {
  description = "データベース管理者パスワード"
  type        = string
  default     = "changeme123" # 本番環境では必ず変更。AWS Secrets Managerの利用を推奨
  sensitive   = true
}

# Amazon Bedrock設定
variable "bedrock_model_id" {
  description = "Amazon Bedrockで使用するモデルID"
  type        = string
  default     = "anthropic.claude-3-haiku-20240307-v1:0"
}

variable "bedrock_max_tokens" {
  description = "レスポンスの最大トークン数"
  type        = number
  default     = 1000
}

variable "bedrock_temperature" {
  description = "生成時の温度パラメータ（0.0～1.0）"
  type        = number
  default     = 0.5
} 