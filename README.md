# Sistema MVP de Control de Asistencia

Sistema web fullstack para control de asistencia con integración de lector ZKTeco ZKB202S.

## Tecnologías

- **Backend**: Spring Boot 3.x + Java 17
- **Frontend**: Vue.js 3 + Vuetify + TypeScript
- **Base de datos**: PostgreSQL 15
- **Containerización**: Docker + Docker Compose

## Estructura del Proyecto

```
├── backend/                 # Aplicación Spring Boot
│   ├── src/main/java/      # Código fuente Java
│   ├── src/main/resources/ # Configuraciones
│   ├── Dockerfile          # Docker para backend
│   └── pom.xml            # Dependencias Maven
├── frontend/               # Aplicación Vue.js
│   ├── src/               # Código fuente TypeScript/Vue
│   ├── Dockerfile         # Docker para frontend
│   ├── nginx.conf         # Configuración Nginx
│   └── package.json       # Dependencias npm
├── database/              # Scripts de base de datos
│   └── init.sql          # Inicialización DB
├── docker-compose.yml     # Orquestación de servicios
└── README.md             # Documentación
```

## Instalación y Ejecución

### Prerrequisitos

- Docker y Docker Compose
- Java 17+ (para desarrollo local)
- Node.js 18+ (para desarrollo local)

### Ejecución con Docker

1. Clonar el repositorio
2. Copiar variables de entorno:
   ```bash
   cp .env.example .env
   ```
3. Levantar los servicios:
   ```bash
   docker-compose up -d
   ```

### Acceso a la aplicación

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **Base de datos**: localhost:5432

### Desarrollo Local

#### Backend
```bash
cd backend
./mvnw spring-boot:run
```

#### Frontend
```bash
cd frontend
npm install
npm run dev
```

## Funcionalidades

- ✅ Registro de asistencia con lector ZKTeco
- ✅ Gestión de empleados
- ✅ Reportes de asistencia
- ✅ Autenticación JWT
- ✅ Configuración de horarios
- ✅ Exportación Excel/PDF

## Estado del Proyecto

Este es un sistema MVP (Minimum Viable Product) en desarrollo. La configuración inicial está completa y lista para implementar las funcionalidades específicas.