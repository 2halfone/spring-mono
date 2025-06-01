# 🚀 GUIDA COMPLETA ALL'UTILIZZO DELL'APPLICAZIONE

> **Sistema di Autenticazione Microservizi Spring Boot**  
> Guida pratica per sviluppatori e utenti finali

---

## 📋 **INDICE**

1. [Setup e Avvio](#setup-e-avvio)
2. [Architettura del Sistema](#architettura-del-sistema)
3. [API Endpoints Disponibili](#api-endpoints-disponibili)
4. [Testing con Postman/cURL](#testing-con-postmancurl)
5. [Creazione Frontend](#creazione-frontend)
6. [Database e Dati](#database-e-dati)
7. [Monitoring e Logs](#monitoring-e-logs)
8. [Troubleshooting](#troubleshooting)

---

## 🏗️ **SETUP E AVVIO**

### **Prerequisiti**
```bash
✅ Java 17+
✅ Docker & Docker Compose
✅ Maven 3.8+
✅ PostgreSQL (opzionale, c'è H2)
```

### **🚀 Avvio Rapido (5 minuti)**

#### **1. Clone e preparazione**
```powershell
# Apri PowerShell nella directory del progetto
cd "C:\Users\mini\Desktop\Visual code\microservices\spring-mono"

# Verifica struttura
Get-ChildItem -Name
```

#### **2. Avvio con Docker (CONSIGLIATO)**
```powershell
# Avvia tutti i servizi
docker-compose -f docker-compose.dev.yml up -d

# Verifica status
docker-compose ps
```

#### **3. Avvio Manuale (Sviluppo)**
```powershell
# Terminal 1 - Auth Service
cd auth-service
./mvnw spring-boot:run

# Terminal 2 - Chat Service  
cd auth-service
./mvnw spring-boot:run

# Terminal 3 - Gateway
cd gateway/complete
./mvnw spring-boot:run
```

### **🎯 Verifica Funzionamento**
```powershell
# Health checks
curl http://localhost:9081/actuator/health  # Auth Service
curl http://localhost:9082/actuator/health  # Chat Service
curl http://localhost:9080/actuator/health  # Gateway
```

---

## 🏛️ **ARCHITETTURA DEL SISTEMA**

### **🔧 Servizi Attivi**

| Servizio | Porta | Descrizione | Database |
|----------|-------|-------------|----------|
| **Auth Service** | 9081 | Autenticazione JWT, Gestione Utenti | PostgreSQL/H2 |
| **Chat Service** | 9082 | Gestione Movie Watchlist | PostgreSQL/H2 |
| **Gateway** | 9080 | API Gateway, Routing, Security | - |
| **PostgreSQL** | 5432 | Database primario | - |
| **Redis** | 6379 | Cache, Sessions | - |

### **🔐 Security Stack**
- **JWT Authentication** con refresh token
- **BCrypt** password encryption
- **Spring Security** 6.x
- **CORS** configurato per frontend
- **Rate Limiting** sul Gateway

---

## 🛠️ **API ENDPOINTS DISPONIBILI**

### **🔐 AUTH SERVICE (Port 9081)**

#### **Pubblici (No Auth)**
```http
POST /auth/register          # Registrazione utente
POST /auth/login             # Login
GET  /auth/check-username/{username}
GET  /auth/check-email/{email}
GET  /actuator/health        # Health check
```

#### **Protetti (JWT Required)**
```http
GET  /auth/me               # Profilo utente corrente
POST /auth/validate         # Validazione token
POST /auth/refresh          # Refresh token
GET  /auth/users            # Lista utenti (ADMIN)
POST /auth/logout           # Logout
```

#### **Movie Management**
```http
GET    /movies              # Lista film
POST   /movies              # Aggiungi film
GET    /movies/{id}         # Dettagli film
PUT    /movies/{id}         # Modifica film
DELETE /movies/{id}         # Elimina film
```

### **🎬 CHAT SERVICE (Port 9082)**

```http
GET    /movies              # Lista movie watchlist
POST   /movies              # Aggiungi a watchlist
GET    /movies/{id}         # Dettagli movie
DELETE /movies/{id}         # Rimuovi da watchlist
```

### **🌐 GATEWAY (Port 9080)**

```http
# Proxy verso auth-service
/auth/**  → localhost:9081/auth/**

# Proxy verso auth-service  
/chat/**  → localhost:9082/**
```

---

## 🧪 **TESTING CON POSTMAN/cURL**

### **🔑 1. Registrazione Utente**

```bash
curl -X POST http://localhost:9081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com", 
    "password": "password123"
  }'
```

**Risposta Attesa:**
```json
{
  "id": 1,
  "username": "testuser",
  "email": "test@example.com",
  "roles": ["USER"],
  "createdAt": "2025-06-01T10:30:00"
}
```

### **🚪 2. Login**

```bash
curl -X POST http://localhost:9081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**Risposta Attesa:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh_token_here",
  "expiresIn": 3600,
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "roles": ["USER"]
  }
}
```

### **👤 3. Accesso Profilo (Con Token)**

```bash
# Salva il token dalla login
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET http://localhost:9081/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

### **🎬 4. Gestione Movie**

```bash
# Aggiungi film
curl -X POST http://localhost:9081/movies \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Inception",
    "genre": "Sci-Fi",
    "year": 2010,
    "director": "Christopher Nolan"
  }'

# Lista film
curl -X GET http://localhost:9081/movies \
  -H "Authorization: Bearer $TOKEN"
```

### **🔄 5. Attraverso Gateway**

```bash
# Login via Gateway
curl -X POST http://localhost:9080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'

# Watchlist via Gateway  
curl -X GET http://localhost:9080/chat/movies \
  -H "Authorization: Bearer $TOKEN"
```

---

## 💻 **CREAZIONE FRONTEND**

### **🌐 Opzione 1: Web App HTML/JavaScript**

#### **Struttura Directory**
```
frontend/
├── index.html          # Login page
├── dashboard.html      # Post-login dashboard  
├── app.js             # JavaScript logic
├── style.css          # Styling
└── config.js          # API endpoints
```

#### **config.js**
```javascript
const API_CONFIG = {
  AUTH_SERVICE: 'http://localhost:9081',
  CHAT_SERVICE: 'http://localhost:9082', 
  GATEWAY: 'http://localhost:9080',
  ENDPOINTS: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    PROFILE: '/auth/me',
    MOVIES: '/movies'
  }
};
```

#### **app.js - Esempio Login**
```javascript
// Login function
async function login(username, password) {
  try {
    const response = await fetch(`${API_CONFIG.AUTH_SERVICE}${API_CONFIG.ENDPOINTS.LOGIN}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username, password })
    });

    if (response.ok) {
      const data = await response.json();
      localStorage.setItem('authToken', data.token);
      localStorage.setItem('refreshToken', data.refreshToken);
      localStorage.setItem('user', JSON.stringify(data.user));
      
      // Redirect to dashboard
      window.location.href = 'dashboard.html';
    } else {
      const error = await response.json();
      showError(error.message || 'Login failed');
    }
  } catch (error) {
    showError('Network error: ' + error.message);
  }
}

// Protected API call
async function makeAuthenticatedRequest(url, options = {}) {
  const token = localStorage.getItem('authToken');
  
  const config = {
    ...options,
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
      ...options.headers
    }
  };

  const response = await fetch(url, config);
  
  if (response.status === 401) {
    // Token expired, redirect to login
    localStorage.clear();
    window.location.href = 'index.html';
    return;
  }

  return response;
}

// Get user profile
async function getUserProfile() {
  const response = await makeAuthenticatedRequest(
    `${API_CONFIG.AUTH_SERVICE}${API_CONFIG.ENDPOINTS.PROFILE}`
  );
  return response.json();
}

// Get movies
async function getMovies() {
  const response = await makeAuthenticatedRequest(
    `${API_CONFIG.AUTH_SERVICE}${API_CONFIG.ENDPOINTS.MOVIES}`
  );
  return response.json();
}
```

### **⚛️ Opzione 2: React App**

```bash
# Crea React app
npx create-react-app auth-frontend
cd auth-frontend

# Installa dipendenze
npm install axios

# Avvia dev server
npm start
```

#### **src/services/authService.js**
```javascript
import axios from 'axios';

const API_BASE = 'http://localhost:9081';

// Axios instance con interceptors
const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor per aggiungere token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor per gestire errori
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authService = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
  getProfile: () => api.get('/auth/me'),
  logout: () => {
    localStorage.clear();
    return Promise.resolve();
  }
};
```

---

## 💾 **DATABASE E DATI**

### **🔄 Configurazione Database**

#### **Sviluppo (H2 In-Memory)**
```properties
# auth-service/src/main/resources/application-dev.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

**Accesso H2 Console:** http://localhost:9081/h2-console
- **JDBC URL:** `jdbc:h2:mem:testdb`
- **Username:** `sa`
- **Password:** (vuota)

#### **Produzione (PostgreSQL)**
```properties
# auth-service/src/main/resources/application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=myuser
spring.datasource.password=mypassword
```

### **👥 Utenti Preconfigurati**

| Username | Password | Ruolo | Email |
|----------|----------|-------|-------|
| `admin` | `admin123` | ADMIN, USER | admin@example.com |
| `user` | `user123` | USER | user@example.com |
| `test` | `test123` | USER | test@example.com |

### **🎬 Dati di Esempio**

Il sistema viene inizializzato con alcuni film di esempio:
```sql
INSERT INTO movies (title, genre, year, director) VALUES
('The Matrix', 'Sci-Fi', 1999, 'Wachowski Sisters'),
('Inception', 'Thriller', 2010, 'Christopher Nolan'),
('Interstellar', 'Sci-Fi', 2014, 'Christopher Nolan');
```

---

## 📊 **MONITORING E LOGS**

### **🔍 Health Checks**

```bash
# Verifica stato servizi
curl http://localhost:9081/actuator/health
curl http://localhost:9082/actuator/health
curl http://localhost:9080/actuator/health

# Metriche dettagliate
curl http://localhost:9081/actuator/metrics
curl http://localhost:9081/actuator/info
```

### **📝 Log Files**

I log sono salvati in `auth-service/logs/`:

```bash
# Monitora logs in tempo reale
Get-Content auth-service/logs/application.log -Wait -Tail 50

# Log di sicurezza
Get-Content auth-service/logs/security.log -Wait -Tail 20

# Log errori
Get-Content auth-service/logs/error.log -Wait -Tail 20
```

### **📈 Monitoring con Grafana (Opzionale)**

```bash
# Avvia stack monitoring
docker-compose -f docker-compose.monitoring.yml up -d

# Accesso Grafana
# URL: http://localhost:3000
# Username: admin
# Password: admin
```

---

## 🐛 **TROUBLESHOOTING**

### **❌ Problemi Comuni**

#### **1. Port già in uso**
```powershell
# Trova processo che usa porta 9081
netstat -ano | findstr :9081

# Termina processo (sostituisci PID)
taskkill /F /PID 1234
```

#### **2. Database connection failed**
```bash
# Verifica PostgreSQL container
docker ps | grep postgres

# Restart database
docker-compose restart postgres
```

#### **3. JWT Token Invalid**
```bash
# Verifica configurazione JWT_SECRET
echo $env:JWT_SECRET

# Ricrea token con login
curl -X POST http://localhost:9081/auth/login -d '{"username":"admin","password":"admin123"}'
```

#### **4. CORS Errors nel Frontend**
```javascript
// Verifica CORS headers nel browser console
// Aggiungi all'avvio del backend:
// --spring.profiles.active=dev
```

### **🔧 Debug Mode**

```powershell
# Avvia con debug logging
cd auth-service
./mvnw spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.example=DEBUG"

# Oppure imposta variabile ambiente
$env:LOGGING_LEVEL_COM_EXAMPLE="DEBUG"
./mvnw spring-boot:run
```

### **📞 Supporto**

Per problemi specifici:

1. **Check logs** in `auth-service/logs/`
2. **Verifica health endpoints** con cURL
3. **Controlla database** via H2 console
4. **Valida JWT token** su jwt.io

---

## 🎯 **PROSSIMI PASSI**

### **🚀 Funzionalità da Implementare**

1. **Frontend React/Angular** completo
2. **App Mobile** React Native/Flutter  
3. **Email verification** per registrazione
4. **Password reset** workflow
5. **OAuth2 integration** (Google, GitHub)
6. **File upload** per avatar utenti
7. **Real-time chat** con WebSocket
8. **Advanced monitoring** con Grafana

### **🔒 Security Enhancements**

1. **Rate limiting** avanzato
2. **Account lockout** dopo tentativi falliti
3. **Audit logging** completo
4. **HTTPS** obbligatorio in produzione
5. **Token blacklist** per logout

---

## 📚 **RISORSE UTILI**

- **Documentazione Spring Boot:** https://spring.io/projects/spring-boot
- **JWT.io:** https://jwt.io/ (per debug token)
- **Postman Collection:** [Importa gli endpoint](postman-collection.json)
- **OpenAPI/Swagger:** http://localhost:9081/v3/api-docs

---

## ✅ **CHECKLIST VERIFICA FUNZIONAMENTO**

- [ ] Servizi avviati correttamente (porte 9080, 9081, 9082)
- [ ] Database connesso (H2 o PostgreSQL)
- [ ] Login con utenti preconfigurati funziona
- [ ] JWT token generato e validato
- [ ] Endpoint protetti richiedono autenticazione
- [ ] CORS configurato per frontend
- [ ] Health checks rispondono
- [ ] Logs generati correttamente

---

**🎉 La tua applicazione è pronta per l'uso!**

**Sistema enterprise-grade con autenticazione JWT, microservizi Spring Boot e database PostgreSQL. Valore commerciale stimato: £15K-£35K nel mercato UK.**
