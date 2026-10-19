# --- Build ---
FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

RUN npm run build

# --- Ausliefern über nginx ---
FROM nginx:1.27-alpine

COPY --from=build /app/www/ /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80