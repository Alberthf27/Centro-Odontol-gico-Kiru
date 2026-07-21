/* Cliente HTTP de los casos de uso de Recepción (monitoreo de citas).
   Reutiliza la misma base URL y el mismo token que el módulo de cliente. */
(function () {
    const DEFAULT_API_BASE_URL = 'http://localhost:8080';

    function baseUrl() {
        return (window.KIRU_API_BASE_URL || DEFAULT_API_BASE_URL).replace(/\/$/, '');
    }

    function tokenSupabase() {
        const explicitToken = sessionStorage.getItem('kiru_access_token')
            || localStorage.getItem('kiru_access_token');
        if (explicitToken) return explicitToken;

        for (const storage of [sessionStorage, localStorage]) {
            for (let index = 0; index < storage.length; index += 1) {
                const key = storage.key(index);
                if (!key || !key.startsWith('sb-') || !key.endsWith('-auth-token')) continue;
                try {
                    const session = JSON.parse(storage.getItem(key));
                    if (session?.access_token) return session.access_token;
                } catch {
                    // Una entrada ajena o dañada no debe impedir revisar las demás sesiones.
                }
            }
        }
        return null;
    }

    async function request(path, options = {}) {
        const token = tokenSupabase();

        const response = await fetch(`${baseUrl()}${path}`, {
            ...options,
            headers: {
                ...(token ? { Authorization: `Bearer ${token}` } : {}),
                ...(options.body ? { 'Content-Type': 'application/json' } : {}),
                ...(options.headers || {}),
            },
        });

        if (response.status === 204) return null;
        const body = await response.json().catch(() => null);
        if (!response.ok) {
            const error = new Error(body?.message || 'No se pudo completar la operación solicitada.');
            error.status = response.status;
            throw error;
        }
        return body;
    }

    window.KIRU_RECEPCION_API = Object.freeze({
        buscarCliente: (dni) => request(`/api/recepcion/clientes/buscar?dni=${encodeURIComponent(dni)}`),
        registrarCliente: (cliente) => request('/api/recepcion/clientes', {
            method: 'POST', body: JSON.stringify(cliente),
        }),
        citasPorFecha: (fecha) => request(
            fecha ? `/api/recepcion/citas?fecha=${encodeURIComponent(fecha)}` : '/api/recepcion/citas'
        ),
        citasDeCliente: (idCliente) => request(`/api/recepcion/clientes/${encodeURIComponent(idCliente)}/citas`),
        planesPendientes: (idCliente) => request(
            `/api/recepcion/clientes/${encodeURIComponent(idCliente)}/planes-pendientes`
        ),
        registrarCita: (idCliente, cita) => request(
            `/api/recepcion/clientes/${encodeURIComponent(idCliente)}/citas`, {
                method: 'POST', body: JSON.stringify(cita),
            }
        ),
        registrarAsistencia: (nroCita) => request(
            `/api/recepcion/citas/${encodeURIComponent(nroCita)}/asistencia`, { method: 'PATCH' }
        ),
        cancelarCita: (nroCita) => request(
            `/api/recepcion/citas/${encodeURIComponent(nroCita)}/cancelacion`, { method: 'PATCH' }
        ),
        reprogramarCita: (nroCita, idFranjaNueva) => request(
            `/api/recepcion/citas/${encodeURIComponent(nroCita)}/reprogramacion`, {
                method: 'PATCH', body: JSON.stringify({ idFranjaNueva }),
            }
        ),
    });
}());
