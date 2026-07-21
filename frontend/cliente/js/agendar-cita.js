/* Registrar cita médica: datos reales desde el backend del módulo de reservas. */
const WEEKDAY_LABELS = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
const CLINIC_TIME_ZONE = 'America/Lima';

const state = {
    client: null,
    treatments: [],
    treatmentPlans: [],
    odontologists: [],
    consultationPrice: 0,
    type: null,
    treatmentId: null,
    planId: null,
    sessionId: null,
    odontologistId: null,
    date: null,
    selectedFranja: null,
    citas: [],
    pedido: null,
};

let calendarViewDate = todayInClinic();
let availabilityRequest = 0;

document.addEventListener('DOMContentLoaded', async () => {
    initTypeGrid();
    initTreatmentGrid();
    initOdontologistSelect();
    initMiniCalendar();
    initOrderActions();
    initCancelBooking();

    state.date = toISODate(todayInClinic());
    renderMiniCalendar();
    renderScheduleSummary();
    renderOrderSummary();
    updateAddButton();
    await loadReservationData();
});

async function loadReservationData() {
    showFeedback('Cargando los datos de reserva…');
    try {
        const [client, treatments, treatmentPlans, odontologists, configuration] = await Promise.all([
            KIRU_API.clienteActual(),
            KIRU_API.tratamientos(),
            KIRU_API.planesTratamientoPendientes(),
            KIRU_API.odontologos(),
            KIRU_API.configuracionReservas(),
        ]);
        state.client = client;
        state.treatments = treatments;
        state.treatmentPlans = treatmentPlans;
        state.odontologists = odontologists;
        state.consultationPrice = Number(configuration.precioConsulta);
        // La persona puede elegir el tipo mientras llegan los datos. Volver a
        // renderizar evita dejar el panel vacío si esa selección fue temprana.
        renderTreatmentGrid();
        populateOdontologists();
        renderScheduleSummary();
        renderOrderSummary();
        showFeedback('');
    } catch (error) {
        showFeedback(error.message, true);
        disableReservationControls();
    }
}

function initTypeGrid() {
    document.getElementById('typeGrid').addEventListener('click', (event) => {
        const card = event.target.closest('.option-card');
        if (!card) return;

        document.querySelectorAll('#typeGrid .option-card').forEach((item) => item.classList.remove('is-selected'));
        card.classList.add('is-selected');
        state.type = card.dataset.type;
        state.treatmentId = null;
        state.planId = null;
        state.sessionId = null;
        state.odontologistId = null;
        state.selectedFranja = null;
        renderTreatmentGrid();
        populateOdontologists();
        renderScheduleSummary();
        renderWeekTable();
        updateAddButton();
    });
}

function initTreatmentGrid() {
    document.getElementById('treatmentGrid').addEventListener('click', (event) => {
        const card = event.target.closest('.option-card');
        if (!card) return;
        document.querySelectorAll('#treatmentGrid .option-card').forEach((item) => item.classList.remove('is-selected'));
        card.classList.add('is-selected');
        state.treatmentId = card.dataset.treatmentId;
        state.planId = card.dataset.planId || null;
        state.sessionId = card.dataset.sessionId || null;
        state.odontologistId = card.dataset.odontologistId || null;
        state.selectedFranja = null;
        populateOdontologists();
        renderScheduleSummary();
        renderWeekTable();
        updateAddButton();
    });
}

function initOdontologistSelect() {
    document.getElementById('odontologistSelect').addEventListener('change', (event) => {
        state.odontologistId = event.target.value || null;
        state.selectedFranja = null;
        renderScheduleSummary();
        renderWeekTable();
        updateAddButton();
    });
}

function populateOdontologists() {
    const select = document.getElementById('odontologistSelect');
    const plan = selectedPlan();
    const availableOdontologists = plan
        ? state.odontologists.filter((odontologist) => odontologist.idOdontologo === plan.idOdontologo)
        : state.odontologists;
    select.innerHTML = '<option value="">Seleccionar odontólogo</option>'
        + availableOdontologists.map((odontologist) => `
            <option value="${escapeHtml(odontologist.idOdontologo)}">
                ${escapeHtml(odontologist.nombreCompleto || `Odontólogo ${odontologist.idOdontologo}`)}
            </option>
        `).join('');
    select.disabled = availableOdontologists.length === 0 || !state.type
        || (state.type === 'tratamiento' && !state.treatmentId);
    if (state.odontologistId && availableOdontologists.some(
        (odontologist) => odontologist.idOdontologo === state.odontologistId
    )) {
        select.value = state.odontologistId;
    }
    if (!availableOdontologists.length) {
        showFeedback('No hay odontólogos habilitados para mostrar disponibilidad.', true);
    }
}

function renderTreatmentGrid() {
    const grid = document.getElementById('treatmentGrid');
    if (state.type !== 'tratamiento') {
        grid.hidden = true;
        grid.innerHTML = '';
        return;
    }

    const uniqueTreatments = state.treatments.filter((treatment) => !treatment.multiplesSesiones);
    const planSessions = state.treatmentPlans.flatMap((plan) => plan.sesionesPendientes.map((session) => ({
        plan,
        session,
    })));
    grid.hidden = false;
    grid.innerHTML = uniqueTreatments.length || planSessions.length
        ? uniqueTreatments.map((treatment) => `
            <button type="button" class="option-card" data-treatment-id="${escapeHtml(treatment.idTratamiento)}">
                <span class="option-title">${escapeHtml(treatment.nombre)}</span>
                <span class="option-desc">Atención única · S/ ${Number(treatment.precio).toFixed(2)}</span>
            </button>
        `).join('') + planSessions.map(({ plan, session }) => `
            <button type="button" class="option-card"
                    data-treatment-id="${escapeHtml(plan.idTratamiento)}"
                    data-plan-id="${escapeHtml(plan.idPlan)}"
                    data-session-id="${escapeHtml(session.idSesion)}"
                    data-odontologist-id="${escapeHtml(plan.idOdontologo)}">
                <span class="option-title">${escapeHtml(plan.tratamiento)} · sesión ${session.orden}</span>
                <span class="option-desc">${escapeHtml(session.nombreTipo)} · ${escapeHtml(plan.odontologo)}</span>
            </button>
        `).join('')
        : '<p class="selection-empty">No existen tratamientos ni sesiones pendientes disponibles para este cliente.</p>';
}

function initMiniCalendar() {
    document.getElementById('calPrev').addEventListener('click', () => {
        calendarViewDate = new Date(calendarViewDate.getFullYear(), calendarViewDate.getMonth() - 1, 1);
        renderMiniCalendar();
    });
    document.getElementById('calNext').addEventListener('click', () => {
        calendarViewDate = new Date(calendarViewDate.getFullYear(), calendarViewDate.getMonth() + 1, 1);
        renderMiniCalendar();
    });
}

function renderMiniCalendar() {
    document.getElementById('calMonthLabel').textContent = calendarViewDate.toLocaleDateString('es-PE', {
        month: 'long', year: 'numeric',
    });

    const minDate = todayInClinic();
    minDate.setHours(0, 0, 0, 0);
    const year = calendarViewDate.getFullYear();
    const month = calendarViewDate.getMonth();
    const firstDay = new Date(year, month, 1);
    const firstWeekday = (firstDay.getDay() + 6) % 7;
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const grid = document.getElementById('calGrid');
    grid.innerHTML = '';

    for (let index = 0; index < firstWeekday; index += 1) {
        const empty = document.createElement('span');
        empty.className = 'cal-day is-empty';
        grid.appendChild(empty);
    }

    for (let day = 1; day <= daysInMonth; day += 1) {
        const cellDate = new Date(year, month, day);
        const iso = toISODate(cellDate);
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'cal-day';
        button.textContent = day;
        button.disabled = cellDate < minDate || cellDate.getDay() === 0;
        if (!button.disabled) button.addEventListener('click', () => selectDate(iso));
        if (iso === toISODate(todayInClinic())) button.classList.add('is-today');
        if (iso === state.date) button.classList.add('is-selected');
        grid.appendChild(button);
    }
}

function selectDate(iso) {
    state.date = iso;
    state.selectedFranja = null;
    renderMiniCalendar();
    renderScheduleSummary();
    renderWeekTable();
    updateAddButton();
}

async function renderWeekTable() {
    const weekStart = getWeekStart(state.date);
    const weekDays = Array.from({ length: 7 }, (_, index) => addDays(weekStart, index));
    const tableBody = document.getElementById('weekTableBody');
    const tableHead = document.getElementById('weekTableHead');

    document.getElementById('weekRangeLabel').textContent =
        `${formatDisplayDate(toISODate(weekDays[0]))} – ${formatDisplayDate(toISODate(weekDays[6]))}`;
    tableHead.innerHTML = '<th>Horas/días</th>' + weekDays.map((day, index) => {
        const selected = toISODate(day) === state.date ? ' class="is-selected-day"' : '';
        return `<th${selected}>${WEEKDAY_LABELS[index]}<br>${day.getDate()}</th>`;
    }).join('');

    if (!state.odontologistId) {
        tableBody.innerHTML = '<tr><td colspan="8" class="availability-empty">Selecciona un odontólogo para consultar sus franjas horarias.</td></tr>';
        return;
    }

    const requestId = ++availabilityRequest;
    tableBody.innerHTML = '<tr><td colspan="8" class="availability-empty">Consultando disponibilidad…</td></tr>';
    try {
        const today = toISODate(todayInClinic());
        const responses = await Promise.all(weekDays.map((day) => {
            const date = toISODate(day);
            // La semana puede comenzar antes de hoy. Esos días se muestran
            // vacíos, pero no se consultan porque el backend los rechaza.
            return date < today
                ? Promise.resolve([])
                : KIRU_API.disponibilidades(state.odontologistId, date);
        }));
        if (requestId !== availabilityRequest) return;
        renderAvailabilityRows(weekDays, responses);
    } catch (error) {
        if (requestId !== availabilityRequest) return;
        tableBody.innerHTML = `<tr><td colspan="8" class="availability-empty">${escapeHtml(error.message)}</td></tr>`;
        showFeedback(error.message, true);
    }
}

function renderAvailabilityRows(weekDays, responses) {
    const tableBody = document.getElementById('weekTableBody');
    const rows = [...new Set(responses.flat().map((franja) => `${timeShort(franja.horaInicio)}|${timeShort(franja.horaFin)}`))]
        .sort((left, right) => left.localeCompare(right));

    if (!rows.length) {
        tableBody.innerHTML = '<tr><td colspan="8" class="availability-empty">No hay franjas horarias configuradas para esta semana.</td></tr>';
        return;
    }

    tableBody.innerHTML = rows.map((row) => {
        const [horaInicio, horaFin] = row.split('|');
        const cells = weekDays.map((day, index) => renderAvailabilityCell(
            day,
            horaInicio,
            horaFin,
            responses[index]
        )).join('');
        return `<tr><td>${formatTimeDisplay(horaInicio)} - ${formatTimeDisplay(horaFin)}</td>${cells}</tr>`;
    }).join('');
    bindAvailabilityEvents();
}

function renderAvailabilityCell(day, horaInicio, horaFin, franjas) {
    const date = toISODate(day);
    const selectedDay = date === state.date ? ' is-selected-day' : '';
    if (day.getDay() === 0) return `<td class="slot-cell slot-cell-closed${selectedDay}">Cerrado</td>`;

    const franja = franjas.find((item) => timeShort(item.horaInicio) === horaInicio && timeShort(item.horaFin) === horaFin);
    if (!franja) return `<td class="slot-cell${selectedDay}"><span class="slot-cell-empty">—</span></td>`;
    if (state.citas.some((cita) => cita.idFranja === franja.idFranja)) {
        return `<td class="slot-cell${selectedDay}"><span class="slot-cell-busy">En pedido</span></td>`;
    }
    if (!franja.disponible) return `<td class="slot-cell${selectedDay}"><span class="slot-cell-busy">Ocupado</span></td>`;

    const selected = state.selectedFranja?.idFranja === franja.idFranja ? ' is-selected' : '';
    return `<td class="slot-cell${selectedDay}"><button type="button" class="slot-subtime${selected}" data-franja='${escapeHtml(JSON.stringify(franja))}'>Disponible<br><small>${formatTimeDisplay(horaInicio)}</small></button></td>`;
}

function bindAvailabilityEvents() {
    document.querySelectorAll('[data-franja]').forEach((button) => {
        button.addEventListener('click', () => {
            state.selectedFranja = JSON.parse(button.dataset.franja);
            state.date = state.selectedFranja.fecha;
            renderMiniCalendar();
            renderScheduleSummary();
            renderWeekTable();
            updateAddButton();
        });
    });
}

function initOrderActions() {
    document.getElementById('addCitaBtn').addEventListener('click', addCitaToOrder);
    document.getElementById('nextBtn').addEventListener('click', registrarPedido);
    document.getElementById('payBtn').addEventListener('click', prepararPago);
}

function addCitaToOrder() {
    if (!puedeAgregarCita()) return;
    const treatment = selectedTreatment();
    const session = selectedSession();
    const odontologist = selectedOdontologist();
    const franja = state.selectedFranja;
    state.citas.push({
        tipoCita: state.type.toUpperCase(),
        idTratamiento: treatment?.idTratamiento || null,
        idSesion: session?.idSesion || null,
        idOdontologo: odontologist.idOdontologo,
        idFranja: franja.idFranja,
        fecha: franja.fecha,
        horaInicio: timeShort(franja.horaInicio),
        horaFin: timeShort(franja.horaFin),
        tipo: state.type === 'consulta'
            ? 'Consulta'
            : `${treatment.nombre}${session ? ` · ${session.nombreTipo}` : ''}`,
        tratamiento: treatment?.nombre || null,
        sesion: session?.nombreTipo || null,
        odontologo: odontologist.nombreCompleto || `Odontólogo ${odontologist.idOdontologo}`,
        monto: state.type === 'consulta' ? state.consultationPrice : Number(treatment.precio),
    });

    state.type = null;
    state.treatmentId = null;
    state.planId = null;
    state.sessionId = null;
    state.odontologistId = null;
    state.selectedFranja = null;
    document.querySelectorAll('#typeGrid .option-card').forEach((item) => item.classList.remove('is-selected'));
    renderTreatmentGrid();
    renderScheduleSummary();
    renderWeekTable();
    renderOrderSummary();
    updateAddButton();
}

async function registrarPedido() {
    if (!state.citas.length || state.pedido) return;
    setOrderSubmitting(true);
    showFeedback('Registrando el pedido y validando las franjas seleccionadas…');
    try {
        state.pedido = await KIRU_API.crearPedido(state.citas.map((cita) => ({
            tipoCita: cita.tipoCita,
            idTratamiento: cita.idTratamiento,
            idSesion: cita.idSesion,
            idOdontologo: cita.idOdontologo,
            idFranja: cita.idFranja,
        })));
        state.citas = state.pedido.citas.map((cita, index) => ({
            ...state.citas[index],
            nroCita: cita.nroCita,
            fecha: cita.fecha,
            horaInicio: timeShort(cita.horaInicio),
            horaFin: timeShort(cita.horaFin),
            tratamiento: cita.tratamiento || state.citas[index].tratamiento,
            sesion: cita.sesion || state.citas[index].sesion,
            odontologo: cita.nombreOdontologo || state.citas[index].odontologo,
        }));
        showFeedback('');
        openPayment();
    } catch (error) {
        showFeedback(error.message, true);
        await renderWeekTable();
    } finally {
        setOrderSubmitting(false);
    }
}

function renderOrderSummary() {
    setText('orderId', state.pedido?.nroPedido || 'Se generará');
    setText('orderClient', state.client ? `${state.client.nombres} ${state.client.apellidos}` : '—');
    const body = document.getElementById('orderTableBody');
    body.innerHTML = state.citas.length
        ? state.citas.map((cita) => `<tr><td>${escapeHtml(cita.nroCita || 'Nueva')}</td><td>${formatShortDate(cita.fecha)}</td><td>${escapeHtml(cita.horaInicio)}</td><td>${escapeHtml(cita.tipo)}</td></tr>`).join('')
        : '<tr><td colspan="4" class="empty-order">Añade una cita al pedido.</td></tr>';
    setText('orderTotal', `S/ ${getOrderTotal().toFixed(2)}`);
    document.getElementById('nextBtn').disabled = state.citas.length === 0 || Boolean(state.pedido);
}

function openPayment() {
    document.getElementById('registrationView').hidden = true;
    document.getElementById('paymentView').hidden = false;
    setText('pageContext', 'Reserva pendiente de confirmación');
    setText('pageTitle', 'Registrar pago');
    renderPaymentSummary();
}

function renderPaymentSummary() {
    setText('paymentOrderId', state.pedido.nroPedido);
    setText('paymentClient', `${state.client.nombres} ${state.client.apellidos}`);
    setText('paymentDni', state.client.dni);
    setText('paymentIssueDate', formatFullDate(state.pedido.fecha));
    document.getElementById('paymentTableBody').innerHTML = state.citas.map((cita) => `
        <tr><td>${escapeHtml(state.pedido.nroPedido)}</td><td>${formatFullDate(cita.fecha)}</td><td>${escapeHtml(cita.horaInicio)}</td><td>${escapeHtml(cita.tipo)}</td><td>${escapeHtml(cita.odontologo)}</td><td>S/ ${Number(cita.monto).toFixed(2)}</td></tr>
    `).join('');
    setText('paymentTotal', `S/ ${Number(state.pedido.montoTotal).toFixed(2)}`);
}

function prepararPago() {
    if (!state.pedido) return;
    sessionStorage.setItem('kiru-pedido-pago', JSON.stringify({
        idPedido: state.pedido.idPedido,
        nroPedido: state.pedido.nroPedido,
        montoTotal: state.pedido.montoTotal,
    }));
    window.location.href = '../pasarela-simulada.html';
}

function initCancelBooking() {
    document.getElementById('cancelBooking').addEventListener('click', () => {
        window.location.href = 'mis-citas.html';
    });
}

function renderScheduleSummary() {
    const treatment = selectedTreatment();
    const session = selectedSession();
    setText('summaryType', state.type === 'consulta'
        ? 'Consulta'
        : treatment
            ? `${treatment.nombre}${session ? ` · ${session.nombreTipo}` : ''}`
            : state.type ? 'Tratamiento' : '—');
    setText('summaryDate', state.selectedFranja ? formatDisplayDate(state.selectedFranja.fecha) : formatDisplayDate(state.date));
    setText('summaryTime', state.selectedFranja ? `${formatTimeDisplay(state.selectedFranja.horaInicio)} - ${formatTimeDisplay(state.selectedFranja.horaFin)}` : 'Elige una franja');
    const amount = state.type === 'consulta'
        ? state.consultationPrice
        : treatment ? Number(treatment.precio) : 0;
    setText('summaryCost', `S/ ${amount.toFixed(2)}`);
}

function updateAddButton() {
    document.getElementById('addCitaBtn').disabled = !puedeAgregarCita();
}

function puedeAgregarCita() {
    const validSelection = state.type === 'consulta'
        || (state.type === 'tratamiento' && selectedTreatment());
    return Boolean(validSelection && selectedOdontologist() && state.selectedFranja);
}

function selectedTreatment() {
    return state.treatments.find((treatment) => treatment.idTratamiento === state.treatmentId) || null;
}

function selectedPlan() {
    return state.treatmentPlans.find((plan) => plan.idPlan === state.planId) || null;
}

function selectedSession() {
    const plan = selectedPlan();
    return plan?.sesionesPendientes.find((session) => session.idSesion === state.sessionId) || null;
}

function selectedOdontologist() {
    return state.odontologists.find((odontologist) => odontologist.idOdontologo === state.odontologistId) || null;
}

function getOrderTotal() {
    return state.pedido ? Number(state.pedido.montoTotal) : state.citas.reduce((total, cita) => total + Number(cita.monto), 0);
}

function setOrderSubmitting(isSubmitting) {
    const button = document.getElementById('nextBtn');
    button.disabled = isSubmitting || state.citas.length === 0 || Boolean(state.pedido);
    button.textContent = isSubmitting ? 'Registrando…' : 'Siguiente';
}

function disableReservationControls() {
    document.querySelectorAll('#typeGrid button, #addCitaBtn, #nextBtn, #cancelBooking').forEach((control) => {
        control.disabled = true;
    });
}

function showFeedback(message, isError = false) {
    const feedback = document.getElementById('bookingFeedback');
    feedback.hidden = !message;
    feedback.textContent = message;
    feedback.classList.toggle('is-error', Boolean(message && isError));
}

function getWeekStart(dateString) {
    const day = new Date(`${dateString}T00:00:00`);
    day.setDate(day.getDate() - ((day.getDay() + 6) % 7));
    return day;
}

function addDays(date, days) {
    const result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
}

function toISODate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function todayInClinic() {
    const parts = new Intl.DateTimeFormat('en-US', {
        timeZone: CLINIC_TIME_ZONE,
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
    }).formatToParts(new Date());
    const values = Object.fromEntries(parts.map((part) => [part.type, part.value]));
    return new Date(Number(values.year), Number(values.month) - 1, Number(values.day));
}
function timeShort(time) { return String(time || '').slice(0, 5); }
function formatTimeDisplay(time) {
    const [hour = 0, minute = 0] = timeShort(time).split(':').map(Number);
    const period = hour >= 12 ? 'PM' : 'AM';
    return `${hour % 12 || 12}:${String(minute).padStart(2, '0')} ${period}`;
}
function toLocalDate(date) {
    return new Date(`${String(date).slice(0, 10)}T00:00:00`);
}

function formatDisplayDate(date) { return toLocalDate(date).toLocaleDateString('es-PE', { weekday: 'short', day: 'numeric', month: 'short' }); }
function formatShortDate(date) { return toLocalDate(date).toLocaleDateString('es-PE', { day: '2-digit', month: '2-digit' }); }
function formatFullDate(date) { return toLocalDate(date).toLocaleDateString('es-PE', { day: '2-digit', month: '2-digit', year: 'numeric' }); }
function setText(id, value) { const element = document.getElementById(id); if (element) element.textContent = value; }
function escapeHtml(value) { return String(value ?? '').replace(/[&<>'"]/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[char])); }
