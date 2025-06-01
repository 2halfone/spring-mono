/**
 * CONFIGURAZIONE VM PER SPRING AUTH SYSTEM
 * 
 * Configurazione specifica per deployment su Virtual Machine
 * Sostituisce la configurazione localhost per testing su VM
 */

// Configurazione API per VM deployment
const API_CONFIG_VM = {
    AUTH_SERVICE: 'http://192.168.1.146:9081',
    CHAT_SERVICE: 'http://192.168.1.146:9082', 
    GATEWAY: 'http://192.168.1.146:9080',
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

// Override della configurazione principale
if (typeof API_CONFIG !== 'undefined') {
    // Sovrascrive la configurazione esistente
    Object.assign(API_CONFIG, API_CONFIG_VM);
} else {
    // Crea la configurazione se non esiste
    const API_CONFIG = API_CONFIG_VM;
}

console.log('🚀 VM Configuration loaded:', API_CONFIG);
