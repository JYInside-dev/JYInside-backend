# 빌드환경
FROM amazoncorretto:21.0.10-al2023 AS build
WORKDIR /app

# xargs가 포함된 findutils 패키지 설치
RUN dnf install -y findutils


# Gradle 래퍼와 설정 파일들을 먼저 복사 (캐시 활용을 통한 빌드 속도 향상)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# gradlew 실행 권한 부여
RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon
# 소스 복사 및 빌드
COPY src src
RUN ./gradlew bootJar --no-daemon


# 배포환경
FROM amazoncorretto:21.0.10-al2023-headless
WORKDIR /app

# jar 만 빼와서 컨테이너에 싣기
COPY --from=build /app/build/libs/*.jar community-backend.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "community-backend.jar"]


