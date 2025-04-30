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