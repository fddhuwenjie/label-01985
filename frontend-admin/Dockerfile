# syntax=docker/dockerfile:1
# ---- Build Stage ----
FROM node:20-alpine AS build
WORKDIR /app

# 配置淘宝 npm 镜像加速
RUN npm config set registry https://registry.npmmirror.com

COPY package.json package-lock.json ./
# 利用 BuildKit 缓存 npm，第二次构建秒过
RUN --mount=type=cache,target=/root/.npm \
    npm ci
COPY . .
RUN npm run build

# ---- Runtime Stage ----
FROM nginx:stable-alpine
RUN apk add --no-cache tzdata && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo "Asia/Shanghai" > /etc/timezone
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
