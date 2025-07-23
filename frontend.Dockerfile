FROM node:18-alpine AS builder

WORKDIR /app
COPY code/js/package*.json ./
RUN npm install
COPY code/js/ ./
RUN npm run build


FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY code/js/default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]