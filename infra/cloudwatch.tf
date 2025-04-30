/**
 * cloudwatch.tf - CloudWatch設定
 * このファイルでは、アプリケーションのモニタリングに必要なCloudWatchリソースを定義します
 */

# ECSクラスターのCPU使用率アラーム
resource "aws_cloudwatch_metric_alarm" "ecs_cpu_high" {
  alarm_name          = "${var.app_name}-${var.environment}-ecs-cpu-high"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = 60
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "This metric monitors ECS CPU utilization"
  alarm_actions       = [] # SNSトピックをここに追加することでアラート通知を設定可能
  ok_actions          = []

  dimensions = {
    ClusterName = aws_ecs_cluster.main.name
  }

  tags = {
    Name = "${var.app_name}-${var.environment}-ecs-cpu-high"
  }
}

# ECSクラスターのメモリ使用率アラーム
resource "aws_cloudwatch_metric_alarm" "ecs_memory_high" {
  alarm_name          = "${var.app_name}-${var.environment}-ecs-memory-high"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 2
  metric_name         = "MemoryUtilization"
  namespace           = "AWS/ECS"
  period              = 60
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "This metric monitors ECS memory utilization"
  alarm_actions       = [] # SNSトピックをここに追加することでアラート通知を設定可能
  ok_actions          = []

  dimensions = {
    ClusterName = aws_ecs_cluster.main.name
  }

  tags = {
    Name = "${var.app_name}-${var.environment}-ecs-memory-high"
  }
}

# ALBのターゲット5xxエラーアラーム
resource "aws_cloudwatch_metric_alarm" "alb_5xx_errors" {
  alarm_name          = "${var.app_name}-${var.environment}-alb-5xx-errors"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "HTTPCode_Target_5XX_Count"
  namespace           = "AWS/ApplicationELB"
  period              = 60
  statistic           = "Sum"
  threshold           = 5
  alarm_description   = "This metric monitors ALB 5xx errors"
  alarm_actions       = [] # SNSトピックをここに追加することでアラート通知を設定可能
  ok_actions          = []

  dimensions = {
    LoadBalancer = module.alb.lb_arn_suffix
  }

  tags = {
    Name = "${var.app_name}-${var.environment}-alb-5xx-errors"
  }
}

# ALBのターゲット応答時間アラーム
resource "aws_cloudwatch_metric_alarm" "alb_target_response_time" {
  alarm_name          = "${var.app_name}-${var.environment}-alb-target-response-time"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "TargetResponseTime"
  namespace           = "AWS/ApplicationELB"
  period              = 60
  statistic           = "Average"
  threshold           = 1 # 1秒以上の応答時間をアラート
  alarm_description   = "This metric monitors ALB target response time"
  alarm_actions       = [] # SNSトピックをここに追加することでアラート通知を設定可能
  ok_actions          = []

  dimensions = {
    LoadBalancer = module.alb.lb_arn_suffix
  }

  tags = {
    Name = "${var.app_name}-${var.environment}-alb-target-response-time"
  }
}

# アプリケーションダッシュボード（将来的に追加）
# resource "aws_cloudwatch_dashboard" "main" {
#   dashboard_name = "${var.app_name}-${var.environment}-dashboard"
#   
#   dashboard_body = jsonencode({
#     widgets = [
#       {
#         type   = "metric"
#         x      = 0
#         y      = 0
#         width  = 12
#         height = 6
#         
#         properties = {
#           metrics = [
#             ["AWS/ECS", "CPUUtilization", "ClusterName", aws_ecs_cluster.main.name]
#           ]
#           period = 300
#           stat   = "Average"
#           region = var.aws_region
#           title  = "ECS Cluster CPU Utilization"
#         }
#       },
#       {
#         type   = "metric"
#         x      = 12
#         y      = 0
#         width  = 12
#         height = 6
#         
#         properties = {
#           metrics = [
#             ["AWS/ECS", "MemoryUtilization", "ClusterName", aws_ecs_cluster.main.name]
#           ]
#           period = 300
#           stat   = "Average"
#           region = var.aws_region
#           title  = "ECS Cluster Memory Utilization"
#         }
#       }
#       # 他のウィジェットも追加可能
#     ]
#   })
# } 