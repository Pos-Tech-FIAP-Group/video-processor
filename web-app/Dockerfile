# Build (context = raiz do repositório)
FROM node:20-alpine AS build
WORKDIR /app

COPY web-app/package.json web-app/package-lock.json ./
RUN npm ci

COPY web-app/ .
RUN npm run build -- --configuration production

# Serve
FROM nginx:alpine
COPY --from=build /app/dist/web-app/browser /usr/share/nginx/html
RUN rm /etc/nginx/conf.d/default.conf
COPY web-app/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
