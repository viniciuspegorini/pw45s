# BUILD: gera os arquivos estáticos da aplicação (pasta dist) com as variáveis do arquivo .env.production
FROM node:22-alpine AS build-step
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

# DELIVERY: o Nginx serve os arquivos estáticos gerados no build
FROM nginx:stable-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build-step /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
