#!/bin/bash

echo "🚀 Starting E-Commerce Microservices Application..."
echo ""

# Start services
echo "📦 Building and starting Docker containers..."
docker-compose up -d

echo ""
echo "⏳ Waiting for services to be ready..."
echo ""

# Wait for MySQL
echo "Waiting for MySQL..."
until docker-compose exec -T mysql mysqladmin ping -h localhost --silent; do
    printf '.'
    sleep 2
done
echo " ✅ MySQL is ready!"

# Wait for Product Service
echo "Waiting for Product Service..."
until curl -s http://localhost:8081/api/products > /dev/null 2>&1; do
    printf '.'
    sleep 2
done
echo " ✅ Product Service is ready!"

# Wait for Order Service
echo "Waiting for Order Service..."
until curl -s http://localhost:8082/api/orders > /dev/null 2>&1; do
    printf '.'
    sleep 2
done
echo " ✅ Order Service is ready!"

# Wait for Notification Service
echo "Waiting for Notification Service..."
until curl -s http://localhost:8083/api/notifications > /dev/null 2>&1; do
    printf '.'
    sleep 2
done
echo " ✅ Notification Service is ready!"

# Wait for Frontend
echo "Waiting for Frontend..."
until curl -s http://localhost:4200 > /dev/null 2>&1; do
    printf '.'
    sleep 2
done
echo " ✅ Frontend is ready!"

echo ""
echo "✨ All services are running!"
echo ""
echo "🌐 Access the application:"
echo "   Frontend:              http://localhost:4200"
echo "   Product Service API:   http://localhost:8081/swagger-ui.html"
echo "   Order Service API:     http://localhost:8082/swagger-ui.html"
echo "   Notification Service:  http://localhost:8083/swagger-ui.html"
echo ""
echo "📊 View logs:"
echo "   docker-compose logs -f"
echo ""
echo "🛑 Stop the application:"
echo "   docker-compose down"
echo ""
