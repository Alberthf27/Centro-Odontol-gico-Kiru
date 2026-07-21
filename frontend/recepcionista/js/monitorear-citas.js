/* CU "Monitorear citas" — Recepcionista.
   Flujo: citas del día por defecto, búsqueda de cliente por DNI y acciones
   (nueva cita, asistencia, reprogramar, cancelar) según el estado de la cita. */
const view = {
    fecha: null,
    cliente: null,
    citas: [],
    citaSeleccionadaId: null,
    modo: 'dia',
    odontologos: [],
    tratamientos: [],
};

document.addEventListener('DOMContentLoaded', async () => {
    document.getElementById('todayLabel').textContent = new Date().toLocaleDateString('es-PE', {
        weekday: 'long', day: 'numeric', month: 'long', year: 'numeric',
    });

    document.getElementById('searchClient').addEventListener('click', buscarCliente);
    document.getElementById('dniInput').addEventListener('keydown', (event) => {
        if (event.key === 'Enter') buscarCliente();
    });
    document.getElementById('dniInput').addEventListener('input', () => showDniError(''));
    document.getElementById('newClient').addEventListener('click', () => openNuevoClienteModal());
    document.getElementById('clearSearch').addEventListener('click', limpiarBusqueda);
    document.getElementById('searchByDate').addEventListener('click', () => {
        // Sin fecha el monitor vuelve a mostrar todas las citas registradas.
        view.fecha = document.getElementById('dateFilter').value || null;
        view.modo = 'dia';
        cargarCitasMonitor();
    });
    document.getElementById('actionNuevaCita').addEventListener('click', () => {
        const cliente = clienteEnContexto();
        if (cliente) openNuevaCitaModal(cliente);
    });
    document.getElementById('actionAsistencia').addEventListener('click', () => {
        const cita = getCitaSeleccionada();
        if (cita) openAsistenciaModal(cita);
    });
    document.getElementById('actionReprogramar').addEventListener('click', () => {
        const cita = getCitaSeleccionada();
        if (cita) openReprogramarModal(cita);
    });
    document.getElementById('actionCancelar').addEventListener('click', () => {
        const cita = getCitaSeleccionada();
        if (cita) openCancelarModal(cita);
    });
    document.getElementById('modalOverlay').addEventListener('click', (event) => {
        if (event.target.id === 'modalOverlay') closeModal();
    });

    cargarCatalogos();
    await cargarCitasMonitor();
});

/* ---- Catálogos compartidos con el módulo de cliente ---- */
async function cargarCatalogos() {
    const [odontologos, tratamientos] = await Promise.allSettled([
        KIRU_API.odontologos(),
        KIRU_API.tratamientos(),
    ]);
    view.odontologos = odontologos.status === 'fulfilled' ? odontologos.value : [];
    view.tratamientos = tratamientos.status === 'fulfilled' ? tratamientos.value : [];
}

/* ---- Carga de la tabla: por defecto todas las citas; con fecha, filtradas ---- */
async function cargarCitasMonitor() {
    try {
        view.citas = await KIRU_RECEPCION_API.citasPorFecha(view.fecha);
        view.citaSeleccionadaId = null;
        renderTabla();
    } catch (error) {
        view.citas = [];
        mostrarVacio(error.message);
        actualizarAcciones();
    }
}

async function cargarCitasDelCliente() {
    try {
        view.citas = await KIRU_RECEPCION_API.citasDeCliente(view.cliente.idCliente);
        view.citaSeleccionadaId = null;
        if (view.citas.length === 0) {
            // Flujo alterno 8.1: cliente sin citas → se habilita "Nueva cita".
            mostrarVacio('El cliente no tiene citas registradas.');
            showToast('El cliente no tiene citas registradas.');
        }
        renderTabla();
    } catch (error) {
        view.citas = [];
        mostrarVacio(error.message);
        actualizarAcciones();
    }
}

function renderTabla() {
    const citas = [...view.citas].sort((left, right) =>
        `${left.fecha}${left.horaInicio}`.localeCompare(`${right.fecha}${right.horaInicio}`));
    const list = document.getElementById('citasList');
    const empty = document.getElementById('emptyCitas');

    if (!citas.some((cita) => cita.nroCita === view.citaSeleccionadaId)) {
        view.citaSeleccionadaId = null;
    }
    if (citas.length > 0) {
        empty.hidden = true;
    } else {
        mostrarVacio(view.modo === 'cliente'
            ? 'El cliente no tiene citas registradas.'
            : (view.fecha ? 'No hay citas registradas para esta fecha.' : 'No hay citas registradas.'));
    }
    list.innerHTML = citas.map(renderFila).join('');
    list.querySelectorAll('[data-action="select"]').forEach((button) => {
        button.addEventListener('click', () => {
            const id = button.closest('tr').dataset.id;
            view.citaSeleccionadaId = view.citaSeleccionadaId === id ? null : id;
            renderTabla();
        });
    });
    actualizarAcciones();
}

function renderFila(cita) {
    const seleccionada = cita.nroCita === view.citaSeleccionadaId;
    const tipo = cita.tratamiento
        ? `${cita.tratamiento}${cita.sesion ? ` · ${cita.sesion}` : ''}`
        : 'Consulta';
    const clienteLinea = view.modo === 'dia' && cita.nombresCliente
        ? `<span class="cita-cliente">${escapeHtml(cita.nombresCliente)} ${escapeHtml(cita.apellidosCliente || '')}</span>`
        : '';
    const asistencia = cita.asistenciaPaciente ? '<span class="asistencia-flag">Asistió</span>' : '';
    return `
        <tr data-id="${escapeHtml(cita.nroCita)}" class="${seleccionada ? 'is-selected' : ''}">
            <td class="cita-id" title="${escapeHtml(cita.nroCita)}">${escapeHtml(codigoVisibleCita(cita.nroCita))}${clienteLinea}</td>
            <td>${formatTableDate(cita.fecha)}</td>
            <td>${escapeHtml(formatTimeDisplay(cita.horaInicio))}</td>
            <td>${escapeHtml(tipo)}</td>
            <td><span class="status-pill status-${statusCss(cita.estado)}">${statusLabel(cita.estado)}</span>${asistencia}</td>
            <td>${escapeHtml(cita.nombreOdontologo || `Odontólogo ${cita.idOdontologo}`)}</td>
            <td>
                <button type="button" class="detail-button ${seleccionada ? 'is-selected' : ''}" data-action="select" aria-label="Seleccionar cita ${escapeHtml(cita.nroCita)}" title="Seleccionar cita">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 16.5V20h3.5L18 9.5 14.5 6 4 16.5Z"/><path d="m13.5 7 3.5 3.5"/></svg>
                </button>
            </td>
        </tr>
    `;
}

function mostrarVacio(mensaje) {
    const empty = document.getElementById('emptyCitas');
    empty.textContent = mensaje;
    empty.hidden = false;
}

/* ---- Flujo básico: buscar cliente por DNI ---- */
async function buscarCliente() {
    const dni = document.getElementById('dniInput').value.trim();
    // Flujo alterno 5.1: validación de formato del DNI.
    if (!/^\d{8}$/.test(dni)) {
        showDniError('El DNI debe tener 8 dígitos. Corrija el DNI ingresado.');
        return;
    }
    showDniError('');
    try {
        const cliente = await KIRU_RECEPCION_API.buscarCliente(dni);
        view.cliente = cliente;
        view.modo = 'cliente';
        pintarCliente(cliente);
        await cargarCitasDelCliente();
    } catch (error) {
        if (error.status === 404) {
            // Flujo alterno 6.1: cliente no encontrado → opción "Nuevo cliente".
            view.cliente = null;
            pintarCliente(null);
            showToast('Cliente no encontrado. Puede registrarlo con "Nuevo cliente".');
            actualizarAcciones();
        } else {
            showToast(error.message);
        }
    }
}

function pintarCliente(cliente) {
    document.getElementById('clienteNombres').value = cliente?.nombres || '';
    document.getElementById('clienteApellidos').value = cliente?.apellidos || '';
    document.getElementById('clienteCelular').value = cliente?.celular || '';
}

function showDniError(mensaje) {
    const error = document.getElementById('dniError');
    error.textContent = mensaje;
    error.hidden = !mensaje;
}

/* Flujo alterno 10.1: limpiar búsqueda y volver a la vista general de citas. */
async function limpiarBusqueda() {
    view.cliente = null;
    view.modo = 'dia';
    view.fecha = null;
    document.getElementById('dniInput').value = '';
    document.getElementById('dateFilter').value = '';
    pintarCliente(null);
    showDniError('');
    await cargarCitasMonitor();
}

/* ---- Selección y habilitación de acciones (paso 11 del flujo básico) ---- */
function getCitaSeleccionada() {
    return view.citas.find((cita) => cita.nroCita === view.citaSeleccionadaId);
}

function clienteEnContexto() {
    if (view.cliente) return view.cliente;
    const cita = getCitaSeleccionada();
    if (cita && cita.idCliente) {
        return {
            idCliente: cita.idCliente,
            dni: cita.dniCliente,
            nombres: cita.nombresCliente,
            apellidos: cita.apellidosCliente,
        };
    }
    return null;
}

function actualizarAcciones() {
    const cita = getCitaSeleccionada();
    const permiteCambios = cita?.estado === 'PENDIENTE' || cita?.estado === 'REPROGRAMADA';
    const asistenciaHabilitada = Boolean(cita)
        && cita.fecha === hoyISO()
        && permiteCambios
        && !cita.asistenciaPaciente;

    document.getElementById('actionNuevaCita').disabled = !clienteEnContexto();
    document.getElementById('actionAsistencia').disabled = !asistenciaHabilitada;
    document.getElementById('actionReprogramar').disabled = !permiteCambios;
    document.getElementById('actionCancelar').disabled = !cita
        || cita.estado === 'CANCELADA' || cita.estado === 'ATENDIDA';
}

async function recargarTablaActual() {
    if (view.modo === 'cliente' && view.cliente) {
        await cargarCitasDelCliente();
    } else {
        await cargarCitasMonitor();
    }
}

/* ==========================================================================
   Nuevo cliente (CU Registrar nuevo cliente, flujo 1.2: desde Monitorear citas)
   ========================================================================== */
function openNuevoClienteModal() {
    const dniPrevio = document.getElementById('dniInput').value.trim();
    openModal(`
        <h3 class="modal-title">Registrar nuevo cliente</h3>
        <p class="modal-subtext">Complete los datos del cliente para habilitar el registro de citas.</p>
        <form class="modal-form" id="nuevoClienteForm" novalidate>
            <div class="form-row">
                <label class="form-field"><span>DNI *</span>
                    <input type="text" id="ncDni" inputmode="numeric" maxlength="8" value="${/^\d{8}$/.test(dniPrevio) ? dniPrevio : ''}" required>
                </label>
                <label class="form-field"><span>Celular *</span>
                    <input type="text" id="ncCelular" inputmode="numeric" maxlength="15" required>
                </label>
            </div>
            <div class="form-row">
                <label class="form-field"><span>Nombres *</span>
                    <input type="text" id="ncNombres" maxlength="80" required>
                </label>
                <label class="form-field"><span>Apellidos *</span>
                    <input type="text" id="ncApellidos" maxlength="80" required>
                </label>
            </div>
            <div class="form-row">
                <label class="form-field"><span>Fecha de nacimiento *</span>
                    <input type="date" id="ncFechaNacimiento" max="${hoyISO()}" required>
                </label>
                <label class="form-field"><span>Domicilio</span>
                    <input type="text" id="ncDomicilio" maxlength="180">
                </label>
            </div>
        </form>
        <div class="modal-actions">
            <button type="button" class="btn-ghost" id="modalClose">Volver</button>
            <button type="button" class="btn-primary" id="confirmNuevoCliente">Registrar cliente</button>
        </div>
    `);
    document.getElementById('modalClose').addEventListener('click', closeModal);
    document.getElementById('confirmNuevoCliente').addEventListener('click', async () => {
        const payload = {
            dni: document.getElementById('ncDni').value.trim(),
            nombres: document.getElementById('ncNombres').value.trim(),
            apellidos: document.getElementById('ncApellidos').value.trim(),
            fechaNacimiento: document.getElementById('ncFechaNacimiento').value || null,
            celular: document.getElementById('ncCelular').value.trim(),
            domicilio: document.getElementById('ncDomicilio').value.trim() || null,
        };
        // Flujo alterno 4.1 del CU Registrar nuevo cliente: datos inválidos.
        if (!/^\d{8}$/.test(payload.dni)) return showModalError('El DNI debe tener 8 dígitos.');
        if (!payload.nombres || !payload.apellidos) return showModalError('Ingrese nombres y apellidos.');
        if (!payload.fechaNacimiento) return showModalError('Ingrese la fecha de nacimiento.');
        if (!payload.celular) return showModalError('Ingrese el celular.');

        const button = document.getElementById('confirmNuevoCliente');
        button.disabled = true;
        button.textContent = 'Registrando…';
        try {
            const cliente = await KIRU_RECEPCION_API.registrarCliente(payload);
            closeModal();
            // El CU retorna a Monitorear citas con el cliente registrado como parámetro.
            view.cliente = cliente;
            view.modo = 'cliente';
            document.getElementById('dniInput').value = cliente.dni;
            pintarCliente(cliente);
            await cargarCitasDelCliente();
            showToast('Cliente registrado correctamente.');
        } catch (error) {
            button.disabled = false;
            button.textContent = 'Registrar cliente';
            showModalError(error.message);
        }
    });
}

/* ==========================================================================
   Nueva cita (CU Registrar cita médica, flujo 1.1: registro por recepcionista)
   ========================================================================== */
async function openNuevaCitaModal(cliente) {
    const planes = await KIRU_RECEPCION_API.planesPendientes(cliente.idCliente).catch(() => []);
    const seleccion = { idFranja: null };

    openModal(`
        <h3 class="modal-title">Registrar nueva cita</h3>
        <p class="modal-summary"><strong>Paciente:</strong> ${escapeHtml(cliente.nombres)} ${escapeHtml(cliente.apellidos)} · DNI ${escapeHtml(cliente.dni || '')}</p>
        <form class="modal-form" id="nuevaCitaForm" novalidate>
            <div class="form-field"><span>Tipo de cita</span>
                <div class="radio-row">
                    <label class="radio-chip"><input type="radio" name="ncTipoCita" value="CONSULTA" checked> Consulta</label>
                    <label class="radio-chip"><input type="radio" name="ncTipoCita" value="TRATAMIENTO"> Tratamiento</label>
                </div>
            </div>
            <div class="form-field" id="ncTratamientoField" hidden><span>Tratamiento</span>
                <select id="ncTratamiento"></select>
            </div>
            <div class="form-field" id="ncSesionField" hidden><span>Sesión del plan</span>
                <select id="ncSesion"></select>
            </div>
            <div class="form-row">
                <div class="form-field"><span>Doctor</span>
                    <select id="ncOdontologo"></select>
                </div>
                <div class="form-field"><span>Fecha</span>
                    <input type="date" id="ncFecha" min="${hoyISO()}" value="${hoyISO()}">
                </div>
            </div>
            <div class="form-field"><span>Franjas horarias disponibles</span>
                <div class="slot-list" id="ncSlots"><p class="modal-subtext">Seleccione doctor y fecha para ver los horarios.</p></div>
            </div>
        </form>
        <div class="modal-actions">
            <button type="button" class="btn-ghost" id="modalClose">Cancelar</button>
            <button type="button" class="btn-primary" id="confirmNuevaCita" disabled>Registrar cita</button>
        </div>
    `, true);

    const tratamientoField = document.getElementById('ncTratamientoField');
    const sesionField = document.getElementById('ncSesionField');
    const tratamientoSelect = document.getElementById('ncTratamiento');
    const sesionSelect = document.getElementById('ncSesion');
    const odontologoSelect = document.getElementById('ncOdontologo');
    const fechaInput = document.getElementById('ncFecha');
    const slotsWrap = document.getElementById('ncSlots');
    const confirmButton = document.getElementById('confirmNuevaCita');

    tratamientoSelect.innerHTML = view.tratamientos.map((tratamiento) =>
        `<option value="${escapeHtml(tratamiento.idTratamiento)}">${escapeHtml(tratamiento.nombre)} — S/ ${escapeHtml(tratamiento.precio)}</option>`).join('');
    odontologoSelect.innerHTML = view.odontologos.map((odontologo) =>
        `<option value="${escapeHtml(odontologo.idOdontologo)}">${escapeHtml(odontologo.nombreCompleto || `Odontólogo ${odontologo.idOdontologo}`)}</option>`).join('');

    const tipoCita = () => document.querySelector('input[name="ncTipoCita"]:checked').value;
    const tratamientoActual = () => view.tratamientos.find(
        (tratamiento) => tratamiento.idTratamiento === tratamientoSelect.value);

    function refrescarSesiones() {
        const tratamiento = tratamientoActual();
        const requiereSesion = tipoCita() === 'TRATAMIENTO' && tratamiento?.multiplesSesiones;
        sesionField.hidden = !requiereSesion;
        if (!requiereSesion) {
            odontologoSelect.disabled = false;
            sesionSelect.innerHTML = '';
            return;
        }
        // Sesiones habilitadas pendientes del plan activo del cliente (flujo 3.1.4).
        const sesiones = planes
            .filter((plan) => plan.idTratamiento === tratamiento.idTratamiento)
            .flatMap((plan) => plan.sesionesPendientes.map((sesion) => ({ ...sesion, plan })));
        if (sesiones.length === 0) {
            sesionSelect.innerHTML = '<option value="">No existen sesiones pendientes de programación</option>';
        } else {
            sesionSelect.innerHTML = sesiones.map((sesion) =>
                `<option value="${escapeHtml(sesion.idSesion)}" data-odontologo="${escapeHtml(sesion.plan.idOdontologo)}">${escapeHtml(sesion.nombreTipo)} — ${escapeHtml(planNombre(sesion.plan))}</option>`).join('');
        }
        fijarOdontologoDelPlan();
    }

    function fijarOdontologoDelPlan() {
        const option = sesionSelect.selectedOptions[0];
        if (option?.dataset.odontologo) {
            odontologoSelect.value = option.dataset.odontologo;
            odontologoSelect.disabled = true;
        } else {
            odontologoSelect.disabled = false;
        }
    }

    async function cargarFranjas() {
        seleccion.idFranja = null;
        confirmButton.disabled = true;
        if (!odontologoSelect.value || !fechaInput.value) {
            slotsWrap.innerHTML = '<p class="modal-subtext">Seleccione doctor y fecha para ver los horarios.</p>';
            return;
        }
        slotsWrap.innerHTML = '<p class="modal-subtext">Consultando disponibilidad…</p>';
        try {
            const franjas = await KIRU_API.disponibilidades(odontologoSelect.value, fechaInput.value);
            if (franjas.length === 0) {
                slotsWrap.innerHTML = '<p class="modal-subtext">No hay franjas horarias para esta fecha.</p>';
                return;
            }
            slotsWrap.innerHTML = franjas.map((franja) => `
                <button type="button" class="slot-btn" data-id="${escapeHtml(franja.idFranja)}" ${franja.disponible ? '' : 'disabled'}>
                    ${escapeHtml(formatTimeDisplay(franja.horaInicio))}
                </button>`).join('');
            slotsWrap.querySelectorAll('.slot-btn:not(:disabled)').forEach((button) => {
                button.addEventListener('click', () => {
                    slotsWrap.querySelectorAll('.slot-btn').forEach((other) => other.classList.remove('is-selected'));
                    button.classList.add('is-selected');
                    seleccion.idFranja = button.dataset.id;
                    confirmButton.disabled = false;
                });
            });
        } catch (error) {
            slotsWrap.innerHTML = `<p class="modal-subtext">${escapeHtml(error.message)}</p>`;
        }
    }

    document.querySelectorAll('input[name="ncTipoCita"]').forEach((radio) => {
        radio.addEventListener('change', () => {
            tratamientoField.hidden = tipoCita() !== 'TRATAMIENTO';
            refrescarSesiones();
        });
    });
    tratamientoSelect.addEventListener('change', refrescarSesiones);
    sesionSelect.addEventListener('change', fijarOdontologoDelPlan);
    odontologoSelect.addEventListener('change', cargarFranjas);
    fechaInput.addEventListener('change', cargarFranjas);
    document.getElementById('modalClose').addEventListener('click', closeModal);
    confirmButton.addEventListener('click', async () => {
        const tratamiento = tipoCita() === 'TRATAMIENTO' ? tratamientoActual() : null;
        const payload = {
            tipoCita: tipoCita(),
            idTratamiento: tratamiento ? tratamiento.idTratamiento : null,
            idSesion: tratamiento?.multiplesSesiones ? (sesionSelect.value || null) : null,
            idOdontologo: odontologoSelect.value,
            idFranja: seleccion.idFranja,
        };
        if (tratamiento?.multiplesSesiones && !payload.idSesion) {
            return showModalError('Selecciona una sesión pendiente del plan de tratamiento.');
        }
        confirmButton.disabled = true;
        confirmButton.textContent = 'Registrando…';
        try {
            const pedido = await KIRU_RECEPCION_API.registrarCita(cliente.idCliente, payload);
            closeModal();
            await recargarTablaActual();
            const nroCita = pedido?.citas?.[0]?.nroCita;
            showToast(`Cita ${nroCita ? codigoVisibleCita(nroCita) : ''} registrada. Pendiente de pago.`.replace('  ', ' '));
        } catch (error) {
            confirmButton.disabled = false;
            confirmButton.textContent = 'Registrar cita';
            showModalError(error.message);
        }
    });

    refrescarSesiones();
    cargarFranjas();
}

function planNombre(plan) {
    return plan.odontologo ? `${plan.odontologo}` : `Plan ${plan.idPlan}`;
}

/* ==========================================================================
   Asistencia (flujo 11.4: registrar asistencia del paciente)
   ========================================================================== */
function openAsistenciaModal(cita) {
    openModal(`
        <h3 class="modal-title">Registrar asistencia</h3>
        <p class="modal-subtext">${formatTableDate(cita.fecha)} · ${formatTimeDisplay(cita.horaInicio)} · ${escapeHtml(cita.tratamiento || 'Consulta')}<br>
        Paciente: ${escapeHtml(cita.nombresCliente || '')} ${escapeHtml(cita.apellidosCliente || '')}</p>
        <div class="modal-actions">
            <button type="button" class="btn-ghost" id="modalClose">Volver</button>
            <button type="button" class="btn-primary" id="confirmAsistencia">Confirmar asistencia</button>
        </div>
    `);
    document.getElementById('modalClose').addEventListener('click', closeModal);
    document.getElementById('confirmAsistencia').addEventListener('click', async () => {
        const button = document.getElementById('confirmAsistencia');
        button.disabled = true;
        button.textContent = 'Registrando…';
        try {
            await KIRU_RECEPCION_API.registrarAsistencia(cita.nroCita);
            closeModal();
            await recargarTablaActual();
            showToast('Asistencia registrada correctamente.');
        } catch (error) {
            button.disabled = false;
            button.textContent = 'Confirmar asistencia';
            // Diagrama de actividades: asistencia no disponible para esta cita.
            showModalError(error.message || 'Asistencia no habilitada.');
        }
    });
}

/* ==========================================================================
   Reprogramar (CU Reprogramar cita médica, flujo 2.1: por recepcionista)
   ========================================================================== */
function openReprogramarModal(cita) {
    if (!tieneAnticipacionMinima(cita)) {
        openModal(`
            <h3 class="modal-title">No se puede reprogramar</h3>
            <p class="modal-subtext">No se puede reprogramar la cita faltando menos de 24 horas para su inicio.</p>
            <div class="modal-actions">
                <button type="button" class="btn-primary" id="modalClose">Entendido</button>
            </div>
        `);
        document.getElementById('modalClose').addEventListener('click', closeModal);
        return;
    }
    const seleccion = { idFranja: null };
    openModal(`
        <h3 class="modal-title">Reprogramar cita</h3>
        <p class="modal-summary"><strong>Actual:</strong> ${formatTableDate(cita.fecha)} · ${formatTimeDisplay(cita.horaInicio)} · ${escapeHtml(cita.tratamiento || 'Consulta')}<br>
        <strong>Doctor actual:</strong> ${escapeHtml(cita.nombreOdontologo || '')}</p>
        <form class="modal-form" novalidate>
            <div class="form-row">
                <div class="form-field"><span>Doctor</span>
                    <select id="rpOdontologo">${view.odontologos.map((odontologo) =>
                        `<option value="${escapeHtml(odontologo.idOdontologo)}" ${odontologo.idOdontologo === cita.idOdontologo ? 'selected' : ''}>${escapeHtml(odontologo.nombreCompleto || `Odontólogo ${odontologo.idOdontologo}`)}</option>`).join('')}</select>
                </div>
                <div class="form-field"><span>Nueva fecha</span>
                    <input type="date" id="rpFecha" min="${hoyISO()}" value="${cita.fecha}">
                </div>
            </div>
            <div class="form-field"><span>Franjas horarias disponibles</span>
                <div class="slot-list" id="rpSlots"><p class="modal-subtext">Consultando disponibilidad…</p></div>
            </div>
        </form>
        <div class="modal-actions">
            <button type="button" class="btn-ghost" id="modalClose">Cancelar</button>
            <button type="button" class="btn-primary" id="confirmReprogramar" disabled>Confirmar cambio</button>
        </div>
    `, true);

    const odontologoSelect = document.getElementById('rpOdontologo');
    const fechaInput = document.getElementById('rpFecha');
    const slotsWrap = document.getElementById('rpSlots');
    const confirmButton = document.getElementById('confirmReprogramar');

    async function cargarFranjas() {
        seleccion.idFranja = null;
        confirmButton.disabled = true;
        slotsWrap.innerHTML = '<p class="modal-subtext">Consultando disponibilidad…</p>';
        try {
            const franjas = await KIRU_API.disponibilidades(odontologoSelect.value, fechaInput.value);
            const libres = franjas.filter((franja) => franja.disponible);
            if (libres.length === 0) {
                slotsWrap.innerHTML = '<p class="modal-subtext">No hay franjas horarias disponibles para esta fecha.</p>';
                return;
            }
            slotsWrap.innerHTML = libres.map((franja) => `
                <button type="button" class="slot-btn" data-id="${escapeHtml(franja.idFranja)}">${escapeHtml(formatTimeDisplay(franja.horaInicio))}</button>`).join('');
            slotsWrap.querySelectorAll('.slot-btn').forEach((button) => {
                button.addEventListener('click', () => {
                    slotsWrap.querySelectorAll('.slot-btn').forEach((other) => other.classList.remove('is-selected'));
                    button.classList.add('is-selected');
                    seleccion.idFranja = button.dataset.id;
                    confirmButton.disabled = false;
                });
            });
        } catch (error) {
            slotsWrap.innerHTML = `<p class="modal-subtext">${escapeHtml(error.message)}</p>`;
        }
    }

    odontologoSelect.addEventListener('change', cargarFranjas);
    fechaInput.addEventListener('change', cargarFranjas);
    document.getElementById('modalClose').addEventListener('click', closeModal);
    confirmButton.addEventListener('click', async () => {
        confirmButton.disabled = true;
        confirmButton.textContent = 'Reprogramando…';
        try {
            await KIRU_RECEPCION_API.reprogramarCita(cita.nroCita, seleccion.idFranja);
            closeModal();
            await recargarTablaActual();
            showToast('Cita reprogramada correctamente.');
        } catch (error) {
            confirmButton.disabled = false;
            confirmButton.textContent = 'Confirmar cambio';
            showModalError(error.message);
        }
    });

    cargarFranjas();
}

/* ==========================================================================
   Cancelar (flujo 11.3: confirmación, estado Cancelada y franja liberada)
   ========================================================================== */
function openCancelarModal(cita) {
    openModal(`
        <h3 class="modal-title">Cancelar cita</h3>
        <p class="modal-subtext">¿Confirmar la cancelación de la cita del ${formatTableDate(cita.fecha)} · ${formatTimeDisplay(cita.horaInicio)} · ${escapeHtml(cita.tratamiento || 'Consulta')}?<br>
        Paciente: ${escapeHtml(cita.nombresCliente || '')} ${escapeHtml(cita.apellidosCliente || '')}. La franja volverá a estar disponible.</p>
        <div class="modal-actions">
            <button type="button" class="btn-ghost" id="modalClose">Volver</button>
            <button type="button" class="btn-primary btn-danger" id="confirmCancelar">Confirmar cancelación</button>
        </div>
    `);
    document.getElementById('modalClose').addEventListener('click', closeModal);
    document.getElementById('confirmCancelar').addEventListener('click', async () => {
        const button = document.getElementById('confirmCancelar');
        button.disabled = true;
        button.textContent = 'Cancelando…';
        try {
            await KIRU_RECEPCION_API.cancelarCita(cita.nroCita);
            closeModal();
            await recargarTablaActual();
            showToast('Cita cancelada y franja liberada.');
        } catch (error) {
            button.disabled = false;
            button.textContent = 'Confirmar cancelación';
            showModalError(error.message);
        }
    });
}

/* ==========================================================================
   Utilidades compartidas con el estilo del módulo de cliente
   ========================================================================== */
function openModal(html, wide = false) {
    const box = document.getElementById('modalBox');
    box.className = wide ? 'modal-box modal-box--wide' : 'modal-box';
    box.innerHTML = html;
    document.getElementById('modalOverlay').hidden = false;
}

function closeModal() {
    document.getElementById('modalOverlay').hidden = true;
    document.getElementById('modalBox').innerHTML = '';
}

function showModalError(message) {
    document.querySelector('#modalBox .modal-note--error')?.remove();
    const error = document.createElement('p');
    error.className = 'modal-note modal-note--error';
    error.textContent = message;
    document.getElementById('modalBox').appendChild(error);
}

function showToast(message) {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.classList.add('is-visible');
    setTimeout(() => toast.classList.remove('is-visible'), 3000);
}

function tieneAnticipacionMinima(cita) {
    const inicio = new Date(`${cita.fecha}T${timeShort(cita.horaInicio)}:00`);
    return (inicio.getTime() - Date.now()) >= 24 * 60 * 60 * 1000;
}

function statusLabel(status) {
    return { PENDIENTE: 'Pendiente', EN_ATENCION: 'En atención', ATENDIDA: 'Atendida', CANCELADA: 'Cancelada', REPROGRAMADA: 'Reprogramada' }[status] || status;
}

function statusCss(status) {
    return { PENDIENTE: 'pending', EN_ATENCION: 'confirmed', ATENDIDA: 'done', CANCELADA: 'cancelled', REPROGRAMADA: 'pending' }[status] || 'pending';
}

function hoyISO() {
    const ahora = new Date();
    return `${ahora.getFullYear()}-${String(ahora.getMonth() + 1).padStart(2, '0')}-${String(ahora.getDate()).padStart(2, '0')}`;
}

function formatTableDate(date) {
    return new Date(`${date}T00:00:00`).toLocaleDateString('es-PE', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function timeShort(time) { return String(time || '').slice(0, 5); }

function formatTimeDisplay(time) {
    const [hour = 0, minute = 0] = timeShort(time).split(':').map(Number);
    const period = hour >= 12 ? 'PM' : 'AM';
    return `${hour % 12 || 12}:${String(minute).padStart(2, '0')} ${period}`;
}

function codigoVisibleCita(nroCita) {
    const code = String(nroCita || '');
    if (code.startsWith('CITA-')) return code;
    return `CITA-${code.replace(/-/g, '').slice(-12).toUpperCase()}`;
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>'"]/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[char]));
}
