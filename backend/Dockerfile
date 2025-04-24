FROM gradle:8.4-jdk17 as build

WORKDIR /app

# 依存関係をキャッシュするために先にコピー
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

# 依存関係をダウンロード
RUN ./gradlew dependencies --no-daemon

# ソースコードをコピー
COPY src ./src

# アプリケーションをビルド
RUN ./gradlew build --no-daemon -x test

# ランタイムイメージ
FROM openjdk:17-slim

WORKDIR /app

# ビルドステージからjarファイルをコピー
COPY --from=build /app/build/libs/*.jar app.jar

# AWSの設定ファイルをコピー（AWS SDK用）
RUN mkdir -p /root/.aws
COPY aws-config/config /root/.aws/config
COPY aws-config/credentials /root/.aws/credentials

# 環境変数
ENV SPRING_PROFILES_ACTIVE=docker

# アプリケーションの起動
CMD ["./gradlew", "bootRun"]