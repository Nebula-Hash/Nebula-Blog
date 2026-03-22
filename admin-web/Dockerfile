FROM mcr.microsoft.com/devcontainers/javascript-node:1-22-bookworm
WORKDIR /app

COPY dist /app/dist
COPY docker-server.mjs /app/docker-server.mjs

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD node -e "fetch('http://127.0.0.1/healthz').then(r=>process.exit(r.ok?0:1)).catch(()=>process.exit(1))"

CMD ["node", "/app/docker-server.mjs"]
