/**
 * SPRING AUTH SYSTEM - API CLIENT
 * 
 * Client JavaScript per l'integrazione con il sistema Spring Boot
 * Gestisce autenticazione, API calls e gestione errori
 */

// Configurazione API centralizzata - VM Configuration
// Per testare in locale, cambiare gli IP con 'localhost'
const API_CONFIG = {
    AUTH_SERVICE: 'http://localhost:9081',
    CHAT_SERVICE: 'http://localhost:9082', 
    GATEWAY: 'http://localhost:9080',
    ENDPOINTS: {
        // Auth endpoints
        LOGIN: '/auth/login',
        REGISTER: '/auth/register',
        PROFILE: '/auth/me',
        VALIDATE: '/auth/validate',
        REFRESH: '/auth/refresh',
        CHECK_USERNAME: '/auth/check-username',
        CHECK_EMAIL: '/auth/check-email',
        
        // Movies endpoints
        MOVIES: '/movies',
        
        // Health endpoints
        HEALTH: '/actuator/health'
    }
};

/**
 * Classe principale per gestire le API calls
 */
class SpringAuthClient {
    constructor() {
        this.token = localStorage.getItem('authToken');
        this.refreshToken = localStorage.getItem('refreshToken');
        this.user = JSON.parse(localStorage.getItem('user') || 'null');
    }

    /**
     * Headers standard per le richieste autenticate
     */
    getAuthHeaders() {
        const headers = {
            'Content-Type': 'application/json'
        };
        
        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }
        
        return headers;
    }

    /**
     * Gestione centralizata degli errori
     */
    handleError(error, context = '') {
        console.error(`[SpringAuthClient] Error in ${context}:`, error);
        
        if (error.status === 401) {
            this.logout();
            throw new Error('Sessione scaduta. Effettua nuovamente il login.');
        }
        
        if (error.status === 403) {
            throw new Error('Accesso negato. Privilegi insufficienti.');
        }
        
        if (error.status >= 500) {
            throw new Error('Errore del server. Riprova più tardi.');
        }
        
        throw error;
    }

    /**
     * LOGIN
     */
    async login(username, password) {
        try {
            const response = await fetch(`${API_CONFIG.AUTH_SERVICE}${API_CONFIG.ENDPOINTS.LOGIN}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || 'Credenziali non valide');
            }

            const data = await response.json();
            
            // Salva dati autenticazione
            this.token = data.token;
            this.refreshToken = data.refreshToken;
            this.user = data.user;
            
            localStorage.setItem('authToken', this.token);
            localStorage.setItem('refreshToken', this.refreshToken);
            localStorage.setItem('user', JSON.stringify(this.user));
            
            return data;
        } catch (error) {
            this.handleError(error, 'login');
        }
    }

    /**
     * REGISTRAZIONE
     */
    async register(userData) {
        try {
            const response = await fetch(`${API_CONFIG.AUTH_SERVICE}${API_CONFIG.ENDPOINTS.REGISTER}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(userData)
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || 'Errore durante la registrazione');
            }

            return await response.json();
        } catch (error) {
            this.handleError(error, 'register');
        }
    }

    /**
     * PROFILO UTENTE
     */
    async getProfile() {
        try {
            const response = await fetch(`${API_CONFIG.AUTH_SERVICE}${API_CONFIG.ENDPOINTS.PROFILE}`, {
                headers: this.getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error('Impossibile caricare il profilo');
            }

            const userData = await response.json();
            this.user = userData;
            localStorage.setItem('user', JSON.stringify(userData));
            
            return userData;
        } catch (error) {
            this.handleError(error, 'getProfile');
        }
    }

    /**
     * VALIDAZIONE TOKEN
     */
    async validateToken() {
        try {
            const response = await fetch(`${API_CONFIG.AUTH_SERVICE}${API_CONFIG.ENDPOINTS.VALIDATE}`, {
                method: 'POST',
                headers: this.getAuthHeaders(),
                body: JSON.stringify({ token: this.token })
            });

            if (!response.ok) {
                throw new Error('Token non valido');
            }

            return await response.json();
        } catch (error) {
            this.handleError(error, 'validateToken');
        }
    }

    /**
     * REFRESH TOKEN
     */
    async refreshAuthToken() {
        try {
            const response = await fetch(`${API_CONFIG.AUTH_SERVICE}${API_CONFIG.ENDPOINTS.REFRESH}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ refreshToken: this.refreshToken })
            });

            if (!response.ok) {
                throw new Error('Impossibile rinnovare il token');
            }

            const data = await response.json();
            this.token = data.token;
            localStorage.setItem('authToken', this.token);
            
            return data;
        } catch (error) {
            this.handleError(error, 'refreshToken');
        }
    }

    /**
     * MOVIES API
     */    async getMovies() {
        try {
            const response = await fetch(`${API_CONFIG.CHAT_SERVICE}${API_CONFIG.ENDPOINTS.MOVIES}`, {
                headers: this.getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error('Impossibile caricare i movies');
            }

            return await response.json();
        } catch (error) {
            this.handleError(error, 'getMovies');
        }
    }    async addMovie(movieData) {
        try {
            const response = await fetch(`${API_CONFIG.CHAT_SERVICE}${API_CONFIG.ENDPOINTS.MOVIES}`, {
                method: 'POST',
                headers: this.getAuthHeaders(),
                body: JSON.stringify(movieData)
            });

            if (!response.ok) {
                throw new Error('Impossibile aggiungere il movie');
            }

            return await response.json();
        } catch (error) {
            this.handleError(error, 'addMovie');
        }
    }

    /**
     * HEALTH CHECK
     */
    async checkServiceHealth(service = 'auth') {
        try {
            const baseUrl = service === 'auth' ? API_CONFIG.AUTH_SERVICE : API_CONFIG.CHAT_SERVICE;
            const response = await fetch(`${baseUrl}${API_CONFIG.ENDPOINTS.HEALTH}`);
            
            return {
                service,
                status: response.ok ? 'online' : 'offline',
                data: response.ok ? await response.json() : null
            };
        } catch (error) {
            return {
                service,
                status: 'offline',
                error: error.message
            };
        }
    }

    /**
     * CHECK USERNAME DISPONIBILITÀ
     */
    async checkUsername(username) {
        try {
            const response = await fetch(`${API_CONFIG.AUTH_SERVICE}${API_CONFIG.ENDPOINTS.CHECK_USERNAME}/${username}`);
            return await response.json();
        } catch (error) {
            this.handleError(error, 'checkUsername');
        }
    }

    /**
     * CHECK EMAIL DISPONIBILITÀ
     */
    async checkEmail(email) {
        try {
            const response = await fetch(`${API_CONFIG.AUTH_SERVICE}${API_CONFIG.ENDPOINTS.CHECK_EMAIL}/${email}`);
            return await response.json();
        } catch (error) {
            this.handleError(error, 'checkEmail');
        }
    }

    /**
     * LOGOUT
     */
    logout() {
        this.token = null;
        this.refreshToken = null;
        this.user = null;
        
        localStorage.removeItem('authToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        
        // Redirect al login
        if (window.location.pathname !== '/login.html') {
            window.location.href = 'login.html';
        }
    }

    /**
     * CHECK SE UTENTE È AUTENTICATO
     */
    isAuthenticated() {
        return !!this.token;
    }

    /**
     * CHECK SE UTENTE HA RUOLO SPECIFICO
     */
    hasRole(role) {
        return this.user && this.user.roles && this.user.roles.includes(role);
    }

    /**
     * GET CURRENT USER
     */
    getCurrentUser() {
        return this.user;
    }
}

/**
 * UTILITY FUNCTIONS
 */

// Decode JWT Token
function decodeJWTToken(token) {
    try {
        const parts = token.split('.');
        const payload = JSON.parse(atob(parts[1]));
        return payload;
    } catch (error) {
        console.error('Error decoding JWT token:', error);
        return null;
    }
}

// Format date
function formatDate(timestamp) {
    return new Date(timestamp * 1000).toLocaleString('it-IT');
}

// Show message in UI
function showMessage(containerId, message, type = 'info') {
    const container = document.getElementById(containerId);
    if (!container) return;
    
    const messageClass = type === 'error' ? 'error' : 
                        type === 'success' ? 'success' : 'info';
    
    container.innerHTML = `<div class="${messageClass}">${message}</div>`;
    
    // Auto-hide after 5 seconds
    setTimeout(() => {
        container.innerHTML = '';
    }, 5000);
}

// Loading state management
function setLoadingState(elementId, isLoading, loadingText = 'Caricamento...') {
    const element = document.getElementById(elementId);
    if (!element) return;
    
    if (isLoading) {
        element.innerHTML = `<div class="loading">${loadingText}</div>`;
        element.disabled = true;
    } else {
        element.disabled = false;
    }
}

// Form validation utilities
function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

function validatePassword(password) {
    return password.length >= 6;
}

function validateUsername(username) {
    return username.length >= 3 && /^[a-zA-Z0-9_]+$/.test(username);
}

/**
 * GLOBAL INSTANCE
 */
const springAuthClient = new SpringAuthClient();

// Auto-redirect se non autenticato (except login/register pages)
document.addEventListener('DOMContentLoaded', () => {
    const publicPages = ['login.html', 'register.html', 'index.html'];
    const currentPage = window.location.pathname.split('/').pop();
    
    if (!publicPages.includes(currentPage) && !springAuthClient.isAuthenticated()) {
        window.location.href = 'login.html';
    }
});

// Export per uso in moduli
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { SpringAuthClient, springAuthClient };
}
