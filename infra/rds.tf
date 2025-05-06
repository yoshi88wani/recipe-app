/**
 * rds.tf - Amazon RDS (Relational Database Service) の設定
 * このファイルでは、PostgreSQLデータベースインスタンスとセキュリティグループを定義します
 */

# RDS用セキュリティグループ
resource "aws_security_group" "rds" {
  name        = "${var.app_name}-${var.environment}-rds-sg"
  description = "Allow database connections from ECS backend"
  vpc_id      = module.vpc.vpc_id

  # ECSバックエンドからのPostgreSQL接続を許可
  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.backend.id]
    description     = "Allow PostgreSQL traffic from backend"
  }

  # 外部への接続を許可
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = {
    Name = "${var.app_name}-${var.environment}-rds-sg"
  }
}

# RDSパラメータグループ
resource "aws_db_parameter_group" "postgres" {
  name   = "${var.app_name}-${var.environment}-pg-param-group"
  family = "postgres14"

  parameter {
    name  = "log_statement"
    value = "all"
  }

  tags = {
    Name = "${var.app_name}-${var.environment}-pg-param-group"
  }
}

# RDSサブネットグループ
resource "aws_db_subnet_group" "postgres" {
  name       = "${var.app_name}-${var.environment}-subnet-group"
  subnet_ids = module.vpc.private_subnets

  tags = {
    Name = "${var.app_name}-${var.environment}-subnet-group"
  }
}

# PostgreSQLデータベースインスタンス
resource "aws_db_instance" "postgres" {
  identifier           = "${var.app_name}-${var.environment}-postgres"
  allocated_storage    = var.db_allocated_storage
  storage_type         = "gp2"
  engine               = "postgres"
  engine_version       = "14"
  instance_class       = var.db_instance_class
  username             = var.db_username
  password             = var.db_password
  parameter_group_name = aws_db_parameter_group.postgres.name
  db_subnet_group_name = aws_db_subnet_group.postgres.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  
  # 本番環境ではマルチAZを有効化してもよい
  multi_az                = var.environment == "prod"
  backup_retention_period = var.environment == "prod" ? 7 : 1
  deletion_protection     = var.environment == "prod"
  skip_final_snapshot     = var.environment != "prod"
  
  # データベース名
  db_name = "${replace(var.app_name, "-", "_")}_${var.environment}"
  
  # メンテナンスウィンドウとバックアップウィンドウの設定
  maintenance_window      = "mon:03:00-mon:04:00"
  backup_window           = "02:00-03:00"
  
  # 自動マイナーバージョンアップグレードを有効化
  auto_minor_version_upgrade = true
  
  tags = {
    Name = "${var.app_name}-${var.environment}-postgres"
  }
} 