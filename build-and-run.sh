#!/bin/bash

# Dừng script nếu có bất kỳ lỗi nào xảy ra
set -e

# Tên file JAR
JAR_NAME="exportexcel-application.jar"

# Đường dẫn đến thư mục build/libs
BUILD_DIR="build/libs"

echo "----------------------------------"
echo "Bắt đầu build project với Gradle"
echo "----------------------------------"

# Build project bằng Gradle với bootJar
./gradlew clean bootJar -x test

# Kiểm tra xem file JAR đã được tạo chưa
if [ -f "$BUILD_DIR/$JAR_NAME" ]; then
    echo "----------------------------------"
    echo "Build thành công: $BUILD_DIR/$JAR_NAME"
    echo "----------------------------------"
else
    echo "----------------------------------"
    echo "Lỗi: File JAR không tồn tại ở $BUILD_DIR"
    echo "----------------------------------"
    exit 1
fi

echo "----------------------------------"
echo "Bắt đầu build và chạy Docker Compose"
echo "----------------------------------"

# Thực hiện docker-compose build và up
docker-compose up --build -d

echo "----------------------------------"
echo "Ứng dụng đã khởi chạy thành công!"
echo "Truy cập tại (Hãy chờ 1 chút nhé container đang được khởi động): http://localhost:8081/swagger-ui/index.html"
echo "----------------------------------"
