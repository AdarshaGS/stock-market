# 🚀 Deployment Guide for MoneyPulse

This guide covers how to deploy the MoneyPulse application using Docker.

## Prerequisites

- Docker Engine 20.10 or higher
- Docker Compose 2.0 or higher
- At least 2GB of available RAM
- Ports 8080 and 3306 available on your host machine

## Quick Start

### 1. Deploy with Docker Compose (Recommended)

The easiest way to deploy the entire stack (application + database):

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Check service status
docker-compose ps

# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: This deletes all data)
docker-compose down -v
```

The application will be available at:
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **Health Check**: http://localhost:8080/actuator/health

### 2. Build Docker Image Only

If you want to build just the application image:

```bash
# Build the image
docker build -t moneypulse:latest .

# Run with external MySQL database
docker run -d \
  --name moneypulse-app \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://your-mysql-host:3306/stock-market \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=mysql \
  moneypulse:latest
```

## Configuration

### Environment Variables

You can customize the application by setting these environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | MySQL JDBC connection URL | `jdbc:mysql://mysql:3306/stock-market` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `mysql` |
| `JWT_SECRET` | Secret key for JWT token generation | (see docker-compose.yml) |
| `JWT_EXPIRATION` | JWT token expiration time in ms | `86400000` (24 hours) |
| `SPRING_FLYWAY_ENABLED` | Enable Flyway migrations | `true` |

### Custom Configuration

To override configuration, you can:

1. **Edit docker-compose.yml** - Modify environment variables in the `app` service
2. **Use .env file** - Create a `.env` file in the same directory:

```env
MYSQL_ROOT_PASSWORD=your-secure-password
JWT_SECRET=your-secret-key
```

3. **Mount custom application.properties**:

```yaml
services:
  app:
    volumes:
      - ./custom-application.properties:/app/config/application.properties
```

## Production Deployment

### Security Recommendations

1. **Change default passwords**:
   ```yaml
   environment:
     MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
     JWT_SECRET: ${JWT_SECRET}
   ```

2. **Use secrets management** (Docker Swarm or Kubernetes)

3. **Enable HTTPS** - Use a reverse proxy like Nginx or Traefik

4. **Restrict database access** - Don't expose MySQL port publicly

### Resource Limits

Add resource constraints in docker-compose.yml:

```yaml
services:
  app:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G
```

### Persistent Data

Database data is stored in a Docker volume named `mysql-data`. To backup:

```bash
# Backup
docker exec stock-market-db mysqldump -u root -pmysql stock-market > backup.sql

# Restore
docker exec -i stock-market-db mysql -u root -pmysql stock-market < backup.sql
```

## Monitoring

### Health Checks

Both services have health checks configured:

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Check MySQL health
docker exec stock-market-db mysqladmin ping -h localhost -u root -pmysql
```

### Logs

```bash
# View all logs
docker-compose logs

# Follow logs for specific service
docker-compose logs -f app

# View last 100 lines
docker-compose logs --tail=100 app
```

## Troubleshooting

### Application won't start

1. Check if MySQL is healthy:
   ```bash
   docker-compose ps
   ```

2. View application logs:
   ```bash
   docker-compose logs app
   ```

3. Ensure Flyway migrations succeeded

### Database connection issues

1. Verify MySQL is running:
   ```bash
   docker-compose exec mysql mysql -u root -pmysql -e "SELECT 1"
   ```

2. Check network connectivity:
   ```bash
   docker-compose exec app ping mysql
   ```

### Port conflicts

If ports 8080 or 3306 are already in use, modify docker-compose.yml:

```yaml
services:
  app:
    ports:
      - "9090:8080"  # Use port 9090 instead
  mysql:
    ports:
      - "3307:3306"  # Use port 3307 instead
```

## Scaling

To run multiple application instances (requires external load balancer):

```bash
docker-compose up -d --scale app=3
```

## Cloud Deployment

### AWS ECS/Fargate
- Push image to ECR
- Create task definition using the Dockerfile
- Configure RDS MySQL instance
- Update environment variables

### Google Cloud Run
- Build and push to GCR
- Deploy with Cloud SQL MySQL instance
- Set environment variables in Cloud Run

### Kubernetes
- Create Deployment and Service manifests
- Use ConfigMaps for configuration
- Use Secrets for sensitive data
- Consider using Helm charts

## Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker/)
- [MySQL Docker Hub](https://hub.docker.com/_/mysql)
