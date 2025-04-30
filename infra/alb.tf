/**
 * alb.tf - Application Load Balancer設定
 * このファイルでは、アプリケーションへのトラフィックを分散するためのALBを定義します
 */

# Application Load Balancer
module "alb" {
  source  = "terraform-aws-modules/alb/aws"
  version = "~> 8.0"

  name = "${var.app_name}-${var.environment}-alb"

  load_balancer_type = "application"

  vpc_id          = module.vpc.vpc_id
  subnets         = module.vpc.public_subnets
  security_groups = [aws_security_group.alb.id]

  # ロードバランサーのリスナー設定
  http_tcp_listeners = [
    {
      port               = 80
      protocol           = "HTTP"
      target_group_index = 0 # フロントエンドターゲットグループを指定
    }
  ]

  # HTTPSリスナーの設定（将来的に設定）
  # https_listeners = [
  #   {
  #     port               = 443
  #     protocol           = "HTTPS"
  #     certificate_arn    = "arn:aws:acm:ap-northeast-1:123456789012:certificate/abcd1234-abcd-1234-abcd-1234abcd1234"
  #     target_group_index = 0
  #   }
  # ]

  # ターゲットグループ
  target_groups = [
    # フロントエンド用ターゲットグループ
    {
      name                 = "${var.app_name}-${var.environment}-frontend-tg"
      backend_protocol     = "HTTP"
      backend_port         = var.frontend_container_port
      target_type          = "ip"
      deregistration_delay = 60
      health_check = {
        enabled             = true
        interval            = 30
        path                = var.health_check_path_frontend
        port                = "traffic-port"
        healthy_threshold   = 3
        unhealthy_threshold = 3
        timeout             = 5
        protocol            = "HTTP"
        matcher             = "200-299"
      }
    },
    # バックエンド用ターゲットグループ
    {
      name                 = "${var.app_name}-${var.environment}-backend-tg"
      backend_protocol     = "HTTP"
      backend_port         = var.backend_container_port
      target_type          = "ip"
      deregistration_delay = 60
      health_check = {
        enabled             = true
        interval            = 30
        path                = var.health_check_path_backend
        port                = "traffic-port"
        healthy_threshold   = 3
        unhealthy_threshold = 3
        timeout             = 5
        protocol            = "HTTP"
        matcher             = "200-299"
      }
    }
  ]

  # バックエンドAPI用のリスナールール
  http_tcp_listener_rules = [
    {
      http_tcp_listener_index = 0
      priority                = 100

      actions = [{
        type               = "forward"
        target_group_index = 1 # バックエンドターゲットグループにフォワード
      }]

      conditions = [{
        path_patterns = ["/api/*"] # APIパスへのリクエストをバックエンドにルーティング
      }]
    }
  ]

  tags = {
    Name = "${var.app_name}-${var.environment}-alb"
  }
} 