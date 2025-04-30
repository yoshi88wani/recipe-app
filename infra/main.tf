/**
 * main.tf - Terraformの基本設定とプロバイダー設定
 * このファイルでは、Terraformのバージョン要件とAWSプロバイダーの設定を行います
 */

# Terraformの必要バージョンを指定
terraform {
  required_version = ">= 1.0.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # 後で状態ファイルをS3に保存する設定を追加することもできます
  # backend "s3" {
  #   bucket = "recipe-app-terraform-state"
  #   key    = "terraform.tfstate"
  #   region = "ap-northeast-1"
  # }
}

# AWSプロバイダーの設定
provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "recipe-app"
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
} 