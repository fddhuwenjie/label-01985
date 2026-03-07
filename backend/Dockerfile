# syntax=docker/dockerfile:1
# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /app

# 配置阿里云 Maven 镜像加速
RUN mkdir -p /root/.m2 && \
    echo '<settings><mirrors><mirror><id>aliyun</id><mirrorOf>central</mirrorOf><url>https://maven.aliyun.com/repository/central</url></mirror></mirrors></settings>' > /root/.m2/settings.xml

COPY pom.xml .
# 利用 BuildKit 缓存 Maven 本地仓库，第二次构建秒过
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn dependency:resolve -B

COPY src ./src
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn clean package -DskipTests -B

# ---- Runtime Stage ----
FROM eclipse-temurin:11-jre
WORKDIR /app
ENV TZ=Asia/Shanghai
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=Asia/Shanghai", "-Dfile.encoding=UTF-8", "-jar", "app.jar", "--spring.profiles.active=docker"]
