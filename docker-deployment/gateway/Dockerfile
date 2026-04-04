FROM nginx:1.27-alpine

COPY gateway/nginx.conf /etc/nginx/nginx.conf

EXPOSE 80 81

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1/healthz || exit 1

CMD ["nginx", "-g", "daemon off;"]
