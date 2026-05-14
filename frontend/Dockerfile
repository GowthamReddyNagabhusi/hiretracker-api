# ============================================================
# HireTrack Frontend — Production Dockerfile
# ============================================================
# Multi-stage: build React app, serve with Nginx
# ============================================================

# ── Stage 1: Build React App ─────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /build

COPY package.json package-lock.json* ./
RUN npm ci --silent

COPY . .
RUN npm run build

# ── Stage 2: Serve with Nginx ────────────────────────────
FROM nginx:alpine AS runtime

# Copy built static files
COPY --from=builder /build/dist /usr/share/nginx/html

# Nginx config will be mounted via docker-compose
EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:80/ || exit 1
