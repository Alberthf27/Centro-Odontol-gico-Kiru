/* Cliente HTTP del módulo de reservas. El token lo emite Supabase Auth. */
(function () {
    const DEFAULT_API_BASE_URL = 'http://localhost:8080';

    class KiruApiError extends Error {
        constructor(message, status) {
            super(message);
            this.name = 'KiruApiError';
            this.status = status;
        }
    }

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
            throw new KiruApiError(body?.message || 'No se pudo completar la operación solicitada.', response.status);
        }
        return body;
    }

    window.KIRU_API = Object.freeze({
        error: KiruApiError,
        clienteActual: () => request('/api/clientes/me'),
        tratamientos: () => request('/api/tratamientos'),
        planesTratamientoPendientes: () => request('/api/planes-tratamiento/pendientes'),
        configuracionReservas: () => request('/api/reservas/configuracion'),
        odontologos: () => request('/api/odontologos'),
        disponibilidades: (idOdontologo, fecha) => request(
            `/api/disponibilidades?idOdontologo=${encodeURIComponent(idOdontologo)}&fecha=${encodeURIComponent(fecha)}`
        ),
        crearPedido: (citas) => request('/api/reservas', {
            method: 'POST', body: JSON.stringify({ citas }),
        }),
        registrarPago: (idPedido, monto) => request('/api/pagos', {
            method: 'POST',
            body: JSON.stringify({ idPedido, metodoPago: 'PASARELA_SIMULADA', monto }),
        }),
        citas: () => request('/api/citas'),
        cancelarCita: (nroCita) => request(`/api/citas/${encodeURIComponent(nroCita)}/cancelacion`, {
            method: 'PATCH',
        }),
        reprogramarCita: (nroCita, idFranjaNueva) => request(
            `/api/citas/${encodeURIComponent(nroCita)}/reprogramacion`, {
                method: 'PATCH', body: JSON.stringify({ idFranjaNueva }),
            }
        ),
    });
}());
