# 빌드환경
FROM amazoncorretto:21.0.10-al2023 AS build
WORKDIR /app

# 💡 수정: dnf를 이용해 findutils와 dos2unix를 한 번에 설치
RUN dnf install -y findutils dos2unix

# Gradle 래퍼와 설정 파일들을 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 💡 수정: dos2unix로 CRLF 변환 후 권한 부여 (apk 명령어 삭제)
RUN dos2unix gradlew && chmod +x gradlew

# 소스 복사
COPY src src

RUN ./gradlew dependencies --no-daemon

# 빌드
RUN ./gradlew bootJar --no-daemon

# 배포환경
FROM amazoncorretto:21.0.10-al2023-headless
WORKDIR /app

# jar 만 빼와서 컨테이너에 싣기
COPY --from=build /app/build/libs/*.jar community-backend.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "community-backend.jar"]