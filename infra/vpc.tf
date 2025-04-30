/**
 * vpc.tf - VPCとネットワーク設定
 * このファイルでは、アプリケーションのネットワークインフラ（VPC、サブネット、ルートテーブルなど）を定義します
 */

# VPCモジュールの使用
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "5.0.0"

  name = "${var.app_name}-${var.environment}-vpc"
  cidr = var.vpc_cidr

  # AZとサブネットの設定
  azs             = ["${var.aws_region}a", "${var.aws_region}c"]
  private_subnets = var.private_subnet_cidrs
  public_subnets  = var.public_subnet_cidrs

  # NATゲートウェイの設定（プライベートサブネットからのインターネットアクセス用）
  enable_nat_gateway = true
  single_nat_gateway = var.environment != "prod" # 開発環境では1つのNATゲートウェイで節約

  # DNSサポートの有効化
  enable_dns_hostnames = true
  enable_dns_support   = true

  # パブリックサブネットのタグ設定（ALBが使用）
  public_subnet_tags = {
    "kubernetes.io/role/elb" = 1 # 将来的にEKSを使う場合にも役立つ
  }

  # プライベートサブネットのタグ設定（ECSタスクが使用）
  private_subnet_tags = {
    "kubernetes.io/role/internal-elb" = 1
  }

  # タグ設定
  tags = {
    Environment = var.environment
    Terraform   = "true"
  }
} 