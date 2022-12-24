#!/bin/bash -xe

export COMPOSE_DOCKER_CLI_BUILD=1 DOCKER_BUILDKIT=1
docker-compose -f "docker-compose.development.yml" -p scicms-core--development down  --remove-orphans
docker-compose -f "docker-compose.development.yml" -p scicms-core--development build --no-cache
docker-compose -f "docker-compose.development.yml" -p scicms-core--development up -d
