# Sử dụng image Java từ OpenJDK làm base image
FROM openjdk:17-jdk-slim

# Cài đặt thư viện cần thiết cho môi trường
RUN apt-get update && apt-get install -y curl \
    && apt-get install -y libfreetype6 libfontconfig1

# Đặt thư mục làm việc trong container
WORKDIR /app

# Copy file jar đã build vào container
COPY build/libs/exportexcel-application.jar app.jar

# Expose port mà ứng dụng Spring Boot sẽ chạy
EXPOSE 8081

# Lệnh chạy ứng dụng Spring Boot khi container khởi động
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
