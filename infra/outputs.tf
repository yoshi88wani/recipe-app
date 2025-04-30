/**
 * outputs.tf - 出力値定義ファイル
 * このファイルでは、Terraformの適用後に出力される値を定義します
 */

# ALBのDNS名（アプリケーションへのアクセスURL）
output "alb_dns_name" {
  description = "アプリケーションロードバランサーのDNS名"
  value       = module.alb.lb_dns_name
}

# ECSクラスター名
output "ecs_cluster_name" {
  description = "ECSクラスターの名前"
  value       = aws_ecs_cluster.main.name
}

# フロントエンドサービス名
output "frontend_service_name" {
  description = "フロントエンドECSサービスの名前"
  value       = aws_ecs_service.frontend.name
}

# バックエンドサービス名
output "backend_service_name" {
  description = "バックエンドECSサービスの名前"
  value       = aws_ecs_service.backend.name
}

# VPC ID
output "vpc_id" {
  description = "作成されたVPCのID"
  value       = module.vpc.vpc_id
}

# パブリックサブネットIDs
output "public_subnet_ids" {
  description = "パブリックサブネットのID一覧"
  value       = module.vpc.public_subnets
}

# プライベートサブネットIDs
output "private_subnet_ids" {
  description = "プライベートサブネットのID一覧"
  value       = module.vpc.private_subnets
} 