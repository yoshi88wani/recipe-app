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
          value = "http://localhost:8080" # サービスディスカバリまたはAPIパスによるルーティングを使用
        }
      ]

      # ヘルスチェック
      healthCheck = {
        command     = ["CMD-SHELL", "curl -f http://localhost:${var.frontend_container_port}/ || exit 1"]
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
          value = var.environment
        },
        {
          name  = "SERVER_PORT"
          value = tostring(var.backend_container_port)
        },
        {
          name  = "AWS_REGION"
          value = var.aws_region
        }
      ]

      # ヘルスチェック
      healthCheck = {
        command     = ["CMD-SHELL", "curl -f http://localhost:${var.backend_container_port}/actuator/health || exit 1"]
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