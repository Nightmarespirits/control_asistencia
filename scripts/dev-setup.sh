#!/bin/bash

# Development setup script for Sistema Control de Asistencia

echo "🚀 Setting up Sistema Control de Asistencia development environment..."

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "📝 Creating .env file from template..."
    cp .env.example .env
    echo "✅ .env file created. You can modify it if needed."
fi

# Create logs directory for backend
mkdir -p backend/logs

echo "🐳 Starting services with Docker Compose..."
docker-compose up -d

echo "⏳ Waiting for services to be ready..."
sleep 30

# Check if services are running
if docker-compose ps | grep -q "Up"; then
    echo "✅ Services are running!"
    echo ""
    echo "🌐 Access URLs:"
    echo "   Frontend: http://localhost:3000"
    echo "   Backend API: http://localhost:8080/api"
    echo "   Database: localhost:5432"
    echo ""
    echo "📊 To view logs:"
    echo "   docker-compose logs -f [service_name]"
    echo ""
    echo "🛑 To stop services:"
    echo "   docker-compose down"
else
    echo "❌ Some services failed to start. Check logs with:"
    echo "   docker-compose logs"
fi