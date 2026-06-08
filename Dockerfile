# 빌드환경
FROM amazoncorretto:21.0.10-al2023 AS build
WORKDIR /app

# xargs가 포함된 findutils 패키지 설치
RUN dnf install -y findutils

# 1. 💡 레이어 분리(캐싱)를 잠시 포기하고 전체 소스 코드를 한 번에 통째로 복사
COPY . .

# 2. 실행 권한 부여 및 바로 스프링 부트 최종 빌드
RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

# 배포환경
FROM amazoncorretto:21.0.10-al2023-headless
WORKDIR /app

COPY --from=build /app/build/libs/*.jar community-backend.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "community-backend.jar"]