/**
 * iam.tf - IAMロールとポリシー設定
 * このファイルでは、ECSタスク実行ロールとECSタスクロールを定義します
 */

# ECSタスク実行ロール (タスクの実行に必要なAWSリソースへのアクセス権を付与)
resource "aws_iam_role" "ecs_task_execution_role" {
  name = "${var.app_name}-${var.environment}-task-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      },
    ]
  })

  tags = {
    Name = "${var.app_name}-${var.environment}-task-execution-role"
  }
}

# ECSタスク実行ロールにAmazonECSTaskExecutionRolePolicyをアタッチ
resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# CloudWatch Logsへの書き込み権限を付与するポリシー
resource "aws_iam_policy" "cloudwatch_logs" {
  name        = "${var.app_name}-${var.environment}-cloudwatch-logs-policy"
  description = "CloudWatch Logsへの書き込み権限を付与するポリシー"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogStreams"
        ]
        Effect   = "Allow"
        Resource = "arn:aws:logs:*:*:*"
      },
    ]
  })
}

# CloudWatch Logsポリシーをタスク実行ロールにアタッチ
resource "aws_iam_role_policy_attachment" "ecs_task_execution_cloudwatch" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = aws_iam_policy.cloudwatch_logs.arn
}

# ECSタスクロール (コンテナが他のAWSサービスにアクセスする際に使用)
resource "aws_iam_role" "ecs_task_role" {
  name = "${var.app_name}-${var.environment}-task-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      },
    ]
  })

  tags = {
    Name = "${var.app_name}-${var.environment}-task-role"
  }
}

# バックエンドがAWS Bedrockにアクセスするためのポリシー
resource "aws_iam_policy" "bedrock_access" {
  name        = "${var.app_name}-${var.environment}-bedrock-access-policy"
  description = "Amazon Bedrockへのアクセス権限を付与するポリシー"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "bedrock:InvokeModel",
          "bedrock:ListFoundationModels",
          "bedrock:GetFoundationModel"
        ]
        Effect   = "Allow"
        Resource = "*"
      },
    ]
  })
}

# BedrockアクセスポリシーをECSタスクロールにアタッチ
resource "aws_iam_role_policy_attachment" "ecs_task_bedrock" {
  role       = aws_iam_role.ecs_task_role.name
  policy_arn = aws_iam_policy.bedrock_access.arn
} 