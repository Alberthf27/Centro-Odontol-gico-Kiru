/* Sesión mínima del módulo: Supabase autentica y Kiru consume el JWT. */
(function () {
    const ACCESS_TOKEN_KEY = 'kiru_access_token';
    const REFRESH_TOKEN_KEY = 'kiru_refresh_token';
    const SESSION_KEY = 'kiru_auth_session';
    const DEFAULT_API_BASE_URL = 'http://localhost:8080';

    function apiBaseUrl() {
        return (window.KIRU_API_BASE_URL || DEFAULT_API_BASE_URL).replace(/\/$/, '');
    }

    function readSession() {
        try {
            return JSON.parse(sessionStorage.getItem(SESSION_KEY) || 'null');
        } catch {
            return null;
        }
    }

    function normalizeRole(role) {
        const normalized = String(role || '').trim().toUpperCase();
        return normalized === 'RECEPTIONIST' ? 'RECEPCIONISTA' : normalized || 'CLIENTE';
    }

    function redirectToLogin() {
        const returnTo = `${window.location.pathname}${window.location.search}${window.location.hash}`;
        const loginPath = window.location.pathname.includes('/cliente/') ? '../login.html' : 'login.html';
        const login = new URL(loginPath, window.location.href);
        login.searchParams.set('return', returnTo);
        window.location.replace(login.href);
    }

    function redirectAfterLogin(session) {
        const returnTo = new URLSearchParams(window.location.search).get('return');
        if (session.role === 'RECEPCIONISTA') {
            window.location.replace('acceso-denegado.html?rol=RECEPCIONISTA');
            return;
        }
        const target = returnTo && returnTo.startsWith('/')
            ? returnTo
            : 'cliente/mis-citas.html';
        window.location.replace(target);
    }

    function saveSession(authResponse, backendSession) {
        const user = authResponse.user || {};
        const role = normalizeRole(backendSession?.role || user.app_metadata?.role);
        const session = {
            userId: backendSession?.userId || user.id || null,
            email: backendSession?.email || user.email || null,
            role,
            demo: false,
        };
        sessionStorage.setItem(ACCESS_TOKEN_KEY, authResponse.access_token);
        if (authResponse.refresh_token) sessionStorage.setItem(REFRESH_TOKEN_KEY, authResponse.refresh_token);
        sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
        return session;
    }

    async function loadSupabaseConfig() {
        const response = await fetch(`${apiBaseUrl()}/api/auth/config`);
        const config = await response.json().catch(() => null);
        if (!response.ok || !config?.configured) {
            throw new Error('Falta configurar SUPABASE_URL y SUPABASE_PUBLISHABLE_KEY en backend/.env.');
        }
        return config;
    }

    async function signIn(email, password) {
        const config = await loadSupabaseConfig();
        const response = await fetch(`${config.supabaseUrl.replace(/\/$/, '')}/auth/v1/token?grant_type=password`, {
            method: 'POST',
            headers: {
                apikey: config.publishableKey,
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email, password }),
        });
        const body = await response.json().catch(() => null);
        if (!response.ok) {
            throw new Error(body?.error_description || body?.msg || 'No se pudo iniciar sesión.');
        }

        let backendSession = null;
        const sessionResponse = await fetch(`${apiBaseUrl()}/api/auth/me`, {
            headers: { Authorization: `Bearer ${body.access_token}` },
        });
        if (sessionResponse.ok) backendSession = await sessionResponse.json();
        return saveSession(body, backendSession);
    }

    function demoLogin() {
        sessionStorage.removeItem(ACCESS_TOKEN_KEY);
        sessionStorage.removeItem(REFRESH_TOKEN_KEY);
        const session = { userId: null, email: 'demo@kiru.local', role: 'CLIENTE', demo: true };
        sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
        return session;
    }

    function signOut() {
        sessionStorage.removeItem(ACCESS_TOKEN_KEY);
        sessionStorage.removeItem(REFRESH_TOKEN_KEY);
        sessionStorage.removeItem(SESSION_KEY);
        const loginPath = window.location.pathname.includes('/cliente/') ? '../login.html' : 'login.html';
        window.location.replace(loginPath);
    }

    function guardPage(expectedRole) {
        const session = readSession();
        if (!session) {
            redirectToLogin();
            return false;
        }
        if (expectedRole && normalizeRole(session.role) !== normalizeRole(expectedRole)) {
            const deniedPath = window.location.pathname.includes('/cliente/')
                ? '../acceso-denegado.html'
                : 'acceso-denegado.html';
            window.location.replace(`${deniedPath}?rol=${encodeURIComponent(session.role)}`);
            return false;
        }
        return true;
    }

    function bindLogout() {
        document.querySelectorAll('[data-action="logout"]').forEach((button) => {
            button.addEventListener('click', signOut);
        });
    }

    document.addEventListener('DOMContentLoaded', () => {
        bindLogout();
        const requiredRole = document.body.dataset.requiresRole;
        if (requiredRole) guardPage(requiredRole);
    });

    window.KIRU_AUTH = Object.freeze({
        currentSession: readSession,
        role: () => normalizeRole(readSession()?.role),
        signIn,
        demoLogin,
        signOut,
        guardPage,
        redirectAfterLogin,
    });
}());
