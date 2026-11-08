# Spring Boot を Heroku で動かすための設計図。
# Herokuはこのファイルを読んで、2つの段階でアプリを作る。

# ---- 1. Spring Bootをビルドする（.jarファイルを作る）----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY . ./
# テストはDBが必要なのでここでは飛ばす（テストは手元で ./mvnw test を実行する）
# Windowsの改行コード(CRLF)が混ざっていると mvnw が動かないので、念のためLFに直す
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -DskipTests package

# ---- 2. 実行用の小さなイメージ（Javaを動かすのに必要なものだけ入れる）----
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
COPY start.sh ./
CMD ["sh", "start.sh"]
