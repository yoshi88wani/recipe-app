/**
 * ecs.tf - ECS(Elastic Container Service)の設定
 * このファイルでは、ECSクラスター、タスク定義、サービスを定義します
 */

# ECSクラスター
resource "aws_ecs_cluster" "main" {
  name = "${var.app_name}-${var.environment}-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = {
    Name = "${var.app_name}-${var.environment}-cluster"
  }
}

# CloudWatch Logsグループ - フロントエンド
resource "aws_cloudwatch_log_group" "frontend" {
  name              = "/ecs/${var.app_name}-${var.environment}-frontend"
  retention_in_days = 30

  tags = {
    Name = "${var.app_name}-${var.environment}-frontend-logs"
  }
}

# CloudWatch Logsグループ - バックエンド
resource "aws_cloudwatch_log_group" "backend" {
  name              = "/ecs/${var.app_name}-${var.environment}-backend"
  retention_in_days = 30

  tags = {
    Name = "${var.app_name}-${var.environment}-backend-logs"
  }
}

# フロントエンドタスク定義
resource "aws_ecs_task_definition" "frontend" {
  family                   = "${var.app_name}-${var.environment}-frontend"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.frontend_cpu
  memory                   = var.frontend_memory
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "frontend"
      image     = var.frontend_container_image
      essential = true

      # ポートマッピング
      portMappings = [
        {
          containerPort = var.frontend_container_port
          hostPort      = var.frontend_container_port
          protocol      = "tcp"
        }
      ]

      # 環境変数
      environment = [
        {
          name  = "NODE_ENV"
          value = var.environment
        },
        {
          name  = "BACKEND_URL"
          value = "http://${module.alb.lb_dns_name}/api" # ALB経由でAPIにアクセス
        }
      ]

      # ヘルスチェック（より単純なバージョン）
      healthCheck = {
        command     = ["CMD-SHELL", "node -e \"process.exit(0)\""]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }

      # ログ設定
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.frontend.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "frontend"
        }
      }
    }
  ])

  tags = {
    Name = "${var.app_name}-${var.environment}-frontend-task"
  }
}

# バックエンドタスク定義
resource "aws_ecs_task_definition" "backend" {
  family                   = "${var.app_name}-${var.environment}-backend"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.backend_cpu
  memory                   = var.backend_memory
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "backend"
      image     = var.backend_container_image
      essential = true

      # ポートマッピング
      portMappings = [
        {
          containerPort = var.backend_container_port
          hostPort      = var.backend_container_port
          protocol      = "tcp"
        }
      ]

      # 環境変数
      environment = [
        {
          name  = "SPRING_PROFILES_ACTIVE"
          value = "docker" # コンテナ環境用のプロファイル
        },
        {
          name  = "SERVER_PORT"
          value = tostring(var.backend_container_port)
        },
        {
          name  = "AWS_REGION"
          value = var.aws_region
        },
        {
          name  = "SPRING_DATASOURCE_URL"
          value = "jdbc:postgresql://${aws_db_instance.postgres.endpoint}/${aws_db_instance.postgres.db_name}"
        },
        {
          name  = "SPRING_DATASOURCE_USERNAME"
          value = var.db_username
        },
        {
          name  = "SPRING_DATASOURCE_PASSWORD"
          value = var.db_password
        },
        {
          name  = "AWS_BEDROCK_MODEL_ID"
          value = var.bedrock_model_id
        },
        {
          name  = "AWS_BEDROCK_MAX_TOKENS"
          value = tostring(var.bedrock_max_tokens)
        },
        {
          name  = "AWS_BEDROCK_TEMPERATURE"
          value = tostring(var.bedrock_temperature)
        },
        {
          name  = "APP_CORS_ALLOWED_ORIGINS"
          value = "http://${module.alb.lb_dns_name}"
        }
      ]

      # ヘルスチェック - シンプルな実行チェック
      healthCheck = {
        command     = ["CMD-SHELL", "echo hello"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }

      # ログ設定
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.backend.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "backend"
        }
      }
    }
  ])

  tags = {
    Name = "${var.app_name}-${var.environment}-backend-task"
  }
}

# フロントエンドECSサービス
resource "aws_ecs_service" "frontend" {
  name            = "${var.app_name}-${var.environment}-frontend-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.frontend.arn
  launch_type     = "FARGATE"
  desired_count   = 1

  network_configuration {
    subnets          = module.vpc.private_subnets
    security_groups  = [aws_security_group.frontend.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = module.alb.target_group_arns[0]
    container_name   = "frontend"
    container_port   = var.frontend_container_port
  }

  # サービスの自動スケーリング（将来的に設定）
  # capacity_provider_strategy {
  #   capacity_provider = "FARGATE_SPOT"
  #   weight            = 100
  # }

  depends_on = [
    module.alb.lb_arn,
    aws_iam_role_policy_attachment.ecs_task_execution_role_policy
  ]

  lifecycle {
    ignore_changes = [desired_count]
  }

  tags = {
    Name = "${var.app_name}-${var.environment}-frontend-service"
  }
}

# バックエンドECSサービス
resource "aws_ecs_service" "backend" {
  name            = "${var.app_name}-${var.environment}-backend-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.backend.arn
  launch_type     = "FARGATE"
  desired_count   = 1

  network_configuration {
    subnets          = module.vpc.private_subnets
    security_groups  = [aws_security_group.backend.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = module.alb.target_group_arns[1]
    container_name   = "backend"
    container_port   = var.backend_container_port
  }

  # サービスの自動スケーリング（将来的に設定）
  # capacity_provider_strategy {
  #   capacity_provider = "FARGATE_SPOT"
  #   weight            = 100
  # }

  depends_on = [
    module.alb.lb_arn,
    aws_iam_role_policy_attachment.ecs_task_execution_role_policy
  ]

  lifecycle {
    ignore_changes = [desired_count]
  }

  tags = {
    Name = "${var.app_name}-${var.environment}-backend-service"
  }
} 