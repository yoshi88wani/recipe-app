/**
 * security.tf - セキュリティグループ設定
 * このファイルでは、ALBとECSサービスに必要なセキュリティグループを定義します
 */

# ALB用のセキュリティグループ
resource "aws_security_group" "alb" {
  name        = "${var.app_name}-${var.environment}-alb-sg"
  description = "Security group for ALB"
  vpc_id      = module.vpc.vpc_id

  # インターネットからのHTTPトラフィックを許可
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTP from internet"
  }

  # インターネットからのHTTPSトラフィックを許可（将来的に設定）
  # ingress {
  #   from_port   = 443
  #   to_port     = 443
  #   protocol    = "tcp"
  #   cidr_blocks = ["0.0.0.0/0"]
  #   description = "HTTPS from internet"
  # }

  # アウトバウンドトラフィックを全て許可
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = {
    Name = "${var.app_name}-${var.environment}-alb-sg"
  }
}

# フロントエンドコンテナ用のセキュリティグループ
resource "aws_security_group" "frontend" {
  name        = "${var.app_name}-${var.environment}-frontend-sg"
  description = "Security group for frontend containers"
  vpc_id      = module.vpc.vpc_id

  # ALBからのトラフィックのみを許可
  ingress {
    from_port       = var.frontend_container_port
    to_port         = var.frontend_container_port
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
    description     = "Allow traffic from ALB"
  }

  # アウトバウンドトラフィックを全て許可
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = {
    Name = "${var.app_name}-${var.environment}-frontend-sg"
  }
}

# バックエンドコンテナ用のセキュリティグループ
resource "aws_security_group" "backend" {
  name        = "${var.app_name}-${var.environment}-backend-sg"
  description = "Security group for backend containers"
  vpc_id      = module.vpc.vpc_id

  # ALBからのトラフィックを許可
  ingress {
    from_port       = var.backend_container_port
    to_port         = var.backend_container_port
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
    description     = "Allow traffic from ALB"
  }

  # フロントエンドからのトラフィックを許可
  ingress {
    from_port       = var.backend_container_port
    to_port         = var.backend_container_port
    protocol        = "tcp"
    security_groups = [aws_security_group.frontend.id]
    description     = "Allow traffic from frontend containers"
  }

  # アウトバウンドトラフィックを全て許可
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = {
    Name = "${var.app_name}-${var.environment}-backend-sg"
  }
} 