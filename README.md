# E-Commerce Microservices Project 

A complete, production-ready microservices application with:

### ✅ 3 Spring Boot Microservices
- **Product Service** - Manages products and inventory
- **Order Service** - Handles orders, calls other services
- **Notification Service** - Sends notifications

### ✅ Angular Frontend
- Modern UI with multiple views
- Product browsing, cart, orders, notifications
- Creates diverse user flows for tracing

### ✅ MySQL Database
- Pre-configured with sample data
- 8 products ready to use
- Proper schema with relationships

### ✅ Complete Docker Setup
- Single `docker-compose up` to run everything
- All services containerized
- Automatic dependency management

### ✅ API Documentation
- Swagger UI on all services
- Interactive API testing
- Clear endpoint documentation

## 🚀 Quick Start (3 Easy Steps)

```bash
# 1. Navigate to the project
cd microservices-app

# 2. Start everything
docker-compose up -d

# 3. Wait 2-3 minutes, then open
# http://localhost:4200
```

That's it! The app is running.

### Service Communication Flow
```
User clicks "Place Order" in UI
    ↓
Frontend calls Order Service API
    ↓
Order Service validates with Product Service
    ↓
Product Service checks and reduces stock in MySQL
    ↓
Order Service saves order to MySQL
    ↓
Order Service calls Notification Service
    ↓
Notification Service logs notification to MySQL
```

### Multiple User Flows

1. **Browse Products** (Simple)
   - Frontend → Product Service → MySQL
   
2. **Place Order** (Complex)
   - Frontend → Order → Product → MySQL
   - Frontend → Order → Notification → MySQL
   
3. **View Orders**
   - Frontend → Order Service → MySQL
   
4. **Check Notifications**
   - Frontend → Notification Service → MySQL

## 📁 Project Structure

```
microservices-app/
├── README.md                       # Main documentation
├── docker-compose.yml              # Runs everything
├── start.sh                        # Helper script
├── init-db.sql                     # Database setup
│
├── product-service/                # Microservice 1
│   ├── Dockerfile
│   ├── pom.xml                     
│   └── src/main/java/...           
│
├── order-service/                  # Microservice 2
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/...
│
├── notification-service/           # Microservice 3
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/...
│
└── frontend/                       # Angular UI
    ├── Dockerfile
    ├── package.json                # NPM config
    └── src/app/...                 # Angular code
```

