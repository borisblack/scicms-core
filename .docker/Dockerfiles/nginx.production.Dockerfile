FROM docker-hub-proxy.iss-reshetnev.ru/nginx:1.23.3-alpine

ARG APP_HOSTNAME
ARG ENVIRONMENT

COPY ./.docker/tls/ /etc/pki/tls/nginx/
COPY .docker/etc/nginx/nginx.conf /tmp/nginx.conf
RUN envsubst '$APP_HOSTNAME $ENVIRONMENT' < /tmp/nginx.conf > /etc/nginx/conf.d/nginx.conf

EXPOSE 80
EXPOSE 443
