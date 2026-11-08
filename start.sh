#!/bin/sh
# アプリを起動するスクリプト（Dockerfile の最後で実行される）
#
# Heroku Postgres は接続情報を DATABASE_URL という1つの環境変数で渡してくる:
#   postgres://ユーザー名:パスワード@ホスト:ポート/DB名
# Spring Boot（JDBC）はこの形のままでは読めないので、3つに分けて渡し直す。
# SPRING_DATASOURCE_URL などの環境変数は application.properties より優先される。
if [ -n "$DATABASE_URL" ]; then
  rest=${DATABASE_URL#*://}       # ユーザー名:パスワード@ホスト:ポート/DB名
  creds=${rest%%@*}               # ユーザー名:パスワード
  export SPRING_DATASOURCE_USERNAME=${creds%%:*}
  export SPRING_DATASOURCE_PASSWORD=${creds#*:}
  # Heroku Postgres はSSL（暗号化通信）が必須。ローカルで試すときは DB_SSLMODE=disable にする
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${rest#*@}?sslmode=${DB_SSLMODE:-require}"
fi

# exec でこのスクリプトをJavaに置き換える（Herokuからの停止の合図がJavaに直接届くように）
exec java -jar app.jar
