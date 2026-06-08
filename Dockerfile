# 1. 빌드 환경
FROM amazoncorretto:21.0.10-al2023 AS build
WORKDIR /app

# xargs가 포함된 findutils 패키지 설치
RUN dnf install -y findutils

# Gradle 래퍼와 설정 파일들을 먼저 복사 (캐시 활용)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 💡 핵심: sed 명령어로 gradlew 파일의 CRLF(\r\n)를 LF(\n)로 변환 후 실행 권한 부여
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# 의존성 다운로드
RUN ./gradlew dependencies --no-daemon

# 소스 복사 및 빌드 (테스트 생략으로 속도 향상)
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# 2. 배포 환경
FROM amazoncorretto:21.0.10-al2023-headless
WORKDIR /app

# jar 만 빼와서 컨테이너에 싣기
COPY --from=build /app/build/libs/*.jar community-backend.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "community-backend.jar"]