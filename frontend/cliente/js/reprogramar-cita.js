/* Reprogramar cita médica: flujo dedicado alineado con la especificación del informe. */
const WEEKDAY_LABELS = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
const CLINIC_TIME_ZONE = 'America/Lima';

const state = {
    client: null,
    appointment: null,
    odontologists: [],
    odontologistId: '',
    date: null,
    selectedFranja: null,
    franjasById: new Map(),
    canEdit: false,
};

let calendarViewDate = todayInClinic();
let availabilityRequest = 0;

document.addEventListener('DOMContentLoaded', () => {
    initCalendar();
    initOdontologistSelect();
    document.getElementById('cancelReprogram').addEventListener('click', () => {
        window.location.href = 'mis-citas.html';
    });
    document.getElementById('confirmReprogram').addEventListener('click', confirmReprogram);
    loadReprogramData();
});

async function loadReprogramData() {
    const idCita = new URLSearchParams(window.location.search).get('cita');
    if (!idCita) {
        failPage('No se recibió la cita que se desea reprogramar. Regresa a “Mis citas” y selecciona una cita.');
        return;
    }

    try {
        const [citas, client, odontologists] = await Promise.all([
            KIRU_API.citas(),
            KIRU_API.clienteActual(),
            KIRU_API.odontologos(),
        ]);
        const appointment = citas.find((cita) => cita.nroCita === idCita);
        if (!appointment) {
            failPage('No se encontró la cita seleccionada o ya no pertenece al cliente actual.');
            return;
        }

        state.appointment = appointment;
        state.client = client;
        state.odontologists = odontologists;
        state.date = appointment.fecha >= todayIso() ? appointment.fecha : todayIso();
        calendarViewDate = toLocalDate(state.date);
        state.canEdit = tieneAnticipacionMinima(appointment)
            && (appointment.estado === 'PENDIENTE' || appointment.estado === 'REPROGRAMADA');

        populateOdontologists();
        renderAppointment();
        renderCalendar();
        renderNewSelection();
        document.getElementById('reprogramLoading').hidden = true;
        document.getElementById('reprogramContent').hidden = false;

        if (!state.canEdit) {
            const message = appointment.estado !== 'PENDIENTE' && appointment.estado !== 'REPROGRAMADA'
                ? 'Solo se pueden reprogramar citas pendientes o reprogramadas.'
                : 'No se puede reprogramar la cita faltando menos de 24 horas para su inicio.';
            setDeadlineMessage(message, true);
            disableEditing();
        } else {
            setDeadlineMessage('La reprogramación exige al menos 24 horas de anticipación.', false);
            renderWeekTable();
        }
    } catch (error) {
        failPage(error.message);
    }
}

function initCalendar() {
    document.getElementById('calPrev').addEventListener('click', () => {
        calendarViewDate = new Date(calendarViewDate.getFullYear(), calendarViewDate.getMonth() - 1, 1);
        renderCalendar();
    });
    document.getElementById('calNext').addEventListener('click', () => {
        calendarViewDate = new Date(calendarViewDate.getFullYear(), calendarViewDate.getMonth() + 1, 1);
        renderCalendar();
    });
}

function initOdontologistSelect() {
    document.getElementById('odontologistSelect').addEventListener('change', (event) => {
        state.odontologistId = event.target.value;
        state.selectedFranja = null;
        renderNewSelection();
        renderWeekTable();
    });
}

function populateOdontologists() {
    const select = document.getElementById('odontologistSelect');
    select.innerHTML = '<option value="">Seleccionar odontólogo</option>'
        + state.odontologists.map((odontologist) => `
            <option value="${escapeHtml(odontologist.idOdontologo)}">
                ${escapeHtml(odontologist.nombreCompleto || `Odontólogo ${odontologist.idOdontologo}`)}
            </option>
        `).join('');
    select.disabled = !state.canEdit || state.odontologists.length === 0;
}

function renderAppointment() {
    const appointment = state.appointment;
    setText('currentId', codigoVisibleCita(appointment.nroCita));
    document.getElementById('currentId').title = appointment.nroCita;
    setText('currentClient', state.client ? `${state.client.nombres} ${state.client.apellidos}` : 'Cliente actual');
    setText('currentType', appointment.tratamiento
        ? `${appointment.tratamiento}${appointment.sesion ? ` · ${appointment.sesion}` : ''}`
        : 'Consulta');
    setText('currentDentist', appointment.nombreOdontologo || `Odontólogo ${appointment.idOdontologo}`);
    setText('currentDate', formatDateLong(appointment.fecha));
    setText('currentTime', `${formatTimeDisplay(appointment.horaInicio)} - ${formatTimeDisplay(appointment.horaFin)}`);
}

function renderCalendar() {
    document.getElementById('calMonthLabel').textContent = calendarViewDate.toLocaleDateString('es-PE', {
        month: 'long', year: 'numeric',
    });

    const minDate = toLocalDate(todayIso());
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
        button.disabled = !state.canEdit || cellDate < minDate || cellDate.getDay() === 0;
        if (!button.disabled) button.addEventListener('click', () => selectDate(iso));
        if (iso === todayIso()) button.classList.add('is-today');
        if (iso === state.date) button.classList.add('is-selected');
        grid.appendChild(button);
    }
}

function selectDate(iso) {
    if (!state.canEdit || iso < todayIso()) return;
    state.date = iso;
    state.selectedFranja = null;
    calendarViewDate = toLocalDate(iso);
    renderCalendar();
    renderNewSelection();
    renderWeekTable();
}

async function renderWeekTable() {
    const weekStart = getWeekStart(state.date || todayIso());
    const weekDays = Array.from({ length: 7 }, (_, index) => addDays(weekStart, index));
    const tableBody = document.getElementById('weekTableBody');
    const tableHead = document.getElementById('weekTableHead');
    const today = todayIso();

    document.getElementById('weekRangeLabel').textContent =
        `${formatDisplayDate(toISODate(weekDays[0]))} – ${formatDisplayDate(toISODate(weekDays[6]))}`;
    tableHead.innerHTML = '<th>Horas/días</th>' + weekDays.map((day, index) => {
        const selected = toISODate(day) === state.date ? ' class="is-selected-day"' : '';
        return `<th${selected}>${WEEKDAY_LABELS[index]}<br>${day.getDate()}</th>`;
    }).join('');

    if (!state.canEdit || !state.odontologists.length) {
        tableBody.innerHTML = '<tr><td colspan="8" class="availability-empty">No hay odontólogos disponibles para consultar franjas.</td></tr>';
        return;
    }

    const requestId = ++availabilityRequest;
    tableBody.innerHTML = '<tr><td colspan="8" class="availability-empty">Consultando disponibilidad…</td></tr>';
    try {
        const doctors = state.odontologistId
            ? state.odontologists.filter((doctor) => doctor.idOdontologo === state.odontologistId)
            : state.odontologists;
        const responses = await Promise.all(weekDays.map(async (day) => {
            const date = toISODate(day);
            if (date < today) return [];
            const byDoctor = await Promise.all(doctors.map(async (doctor) => {
                const franjas = await KIRU_API.disponibilidades(doctor.idOdontologo, date);
                return franjas.map((franja) => ({
                    ...franja,
                    nombreOdontologo: doctor.nombreCompleto || `Odontólogo ${doctor.idOdontologo}`,
                }));
            }));
            return byDoctor.flat();
        }));
        if (requestId !== availabilityRequest) return;
        renderAvailabilityRows(weekDays, responses);
    } catch (error) {
        if (requestId !== availabilityRequest) return;
        tableBody.innerHTML = `<tr><td colspan="8" class="availability-empty">${escapeHtml(error.message)}</td></tr>`;
    }
}

function renderAvailabilityRows(weekDays, responses) {
    const tableBody = document.getElementById('weekTableBody');
    state.franjasById = new Map();
    const rows = [...new Set(responses.flat().map((franja) => `${timeShort(franja.horaInicio)}|${timeShort(franja.horaFin)}`))]
        .sort((left, right) => left.localeCompare(right));

    if (!rows.length) {
        tableBody.innerHTML = '<tr><td colspan="8" class="availability-empty">No hay franjas horarias configuradas para esta semana.</td></tr>';
        return;
    }

    tableBody.innerHTML = rows.map((row) => {
        const [horaInicio, horaFin] = row.split('|');
        const cells = weekDays.map((day, index) => renderAvailabilityCell(
            day, horaInicio, horaFin, responses[index]
        )).join('');
        return `<tr><td>${formatTimeDisplay(horaInicio)} - ${formatTimeDisplay(horaFin)}</td>${cells}</tr>`;
    }).join('');

    tableBody.querySelectorAll('[data-franja-id]').forEach((button) => {
        button.addEventListener('click', () => selectFranja(state.franjasById.get(button.dataset.franjaId)));
    });
}

function renderAvailabilityCell(day, horaInicio, horaFin, franjas) {
    const date = toISODate(day);
    const selectedDay = date === state.date ? ' is-selected-day' : '';
    if (day.getDay() === 0) return `<td class="slot-cell slot-cell-closed${selectedDay}">Cerrado</td>`;

    const matching = franjas.filter((item) =>
        timeShort(item.horaInicio) === horaInicio && timeShort(item.horaFin) === horaFin
    );
    if (!matching.length) return `<td class="slot-cell${selectedDay}"><span class="slot-cell-empty">—</span></td>`;

    const available = matching.filter((item) => item.disponible && item.idFranja !== state.appointment?.idFranja);
    if (!available.length) return `<td class="slot-cell${selectedDay}"><span class="slot-cell-busy">Ocupado</span></td>`;

    const franja = available[0];
    state.franjasById.set(franja.idFranja, franja);
    const selected = state.selectedFranja?.idFranja === franja.idFranja ? ' is-selected' : '';
    const doctorHint = state.odontologistId ? '' : `<small>${escapeHtml(franja.nombreOdontologo)}</small>`;
    return `<td class="slot-cell${selectedDay}"><button type="button" class="slot-subtime${selected}" data-franja-id="${escapeHtml(franja.idFranja)}" title="Disponible con ${escapeHtml(franja.nombreOdontologo)}">Disponible<br><small>${formatTimeDisplay(horaInicio)}</small>${doctorHint}</button></td>`;
}

function selectFranja(franja) {
    if (!franja || !state.canEdit) return;
    state.selectedFranja = franja;
    state.date = franja.fecha;
    state.odontologistId = franja.idOdontologo;
    document.getElementById('odontologistSelect').value = state.odontologistId;
    calendarViewDate = toLocalDate(state.date);
    renderCalendar();
    renderNewSelection();
    renderWeekTable();
    updateConfirmButton();
}

function renderNewSelection() {
    const franja = state.selectedFranja;
    const doctor = state.odontologists.find((item) => item.idOdontologo === (franja?.idOdontologo || state.odontologistId));
    setText('newDate', franja ? formatDateLong(franja.fecha) : '—');
    setText('newTime', franja ? `${formatTimeDisplay(franja.horaInicio)} - ${formatTimeDisplay(franja.horaFin)}` : 'Selecciona una franja');
    setText('newDentist', doctor?.nombreCompleto || (franja?.nombreOdontologo || '—'));
    updateConfirmButton();
}

async function confirmReprogram() {
    const button = document.getElementById('confirmReprogram');
    if (!state.appointment || !state.selectedFranja || !state.canEdit) return;
    button.disabled = true;
    button.textContent = 'Guardando…';
    try {
        await KIRU_API.reprogramarCita(state.appointment.nroCita, state.selectedFranja.idFranja);
        showSuccess('Reprogramación realizada exitosamente.');
        setTimeout(() => { window.location.href = 'mis-citas.html'; }, 900);
    } catch (error) {
        showError(error.message);
        button.disabled = false;
        button.textContent = 'Confirmar cambio';
    }
}

function updateConfirmButton() {
    document.getElementById('confirmReprogram').disabled = !state.canEdit || !state.selectedFranja;
}

function disableEditing() {
    document.getElementById('calPrev').disabled = true;
    document.getElementById('calNext').disabled = true;
    document.getElementById('cancelReprogram').disabled = false;
    updateConfirmButton();
}

function setDeadlineMessage(message, isError) {
    const note = document.getElementById('deadlineNote');
    note.textContent = message;
    note.classList.toggle('is-error', isError);
}

function failPage(message) {
    document.getElementById('reprogramLoading').hidden = true;
    const error = document.getElementById('reprogramError');
    error.textContent = message;
    error.hidden = false;
}

function showError(message) {
    const error = document.getElementById('reprogramError');
    error.textContent = message;
    error.hidden = false;
}

function showSuccess(message) {
    const error = document.getElementById('reprogramError');
    error.className = 'reprogram-feedback';
    error.textContent = message;
    error.hidden = false;
}

function tieneAnticipacionMinima(appointment) {
    const inicio = new Date(`${appointment.fecha}T${timeShort(appointment.horaInicio)}:00`);
    return (inicio.getTime() - Date.now()) >= 24 * 60 * 60 * 1000;
}

function getWeekStart(dateString) {
    const day = toLocalDate(dateString);
    day.setDate(day.getDate() - ((day.getDay() + 6) % 7));
    return day;
}

function addDays(date, days) {
    const result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
}

function todayIso() { return toISODate(todayInClinic()); }
function toISODate(date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}
function toLocalDate(date) { return new Date(`${String(date).slice(0, 10)}T00:00:00`); }
function todayInClinic() {
    const parts = new Intl.DateTimeFormat('en-US', {
        timeZone: CLINIC_TIME_ZONE, year: 'numeric', month: '2-digit', day: '2-digit',
    }).formatToParts(new Date());
    const values = Object.fromEntries(parts.map((part) => [part.type, part.value]));
    return new Date(Number(values.year), Number(values.month) - 1, Number(values.day));
}
function timeShort(time) { return String(time || '').slice(0, 5); }
function formatTimeDisplay(time) {
    const [hour = 0, minute = 0] = timeShort(time).split(':').map(Number);
    return `${hour % 12 || 12}:${String(minute).padStart(2, '0')} ${hour >= 12 ? 'PM' : 'AM'}`;
}
function formatDateLong(date) {
    return toLocalDate(date).toLocaleDateString('es-PE', { day: '2-digit', month: '2-digit', year: 'numeric' });
}
function formatDisplayDate(date) {
    return toLocalDate(date).toLocaleDateString('es-PE', { weekday: 'short', day: 'numeric', month: 'short' });
}
function codigoVisibleCita(nroCita) {
    const code = String(nroCita || '');
    if (code.startsWith('CITA-')) return code;
    return `CITA-${code.replace(/-/g, '').slice(-12).toUpperCase()}`;
}
function setText(id, value) { const element = document.getElementById(id); if (element) element.textContent = value; }
function escapeHtml(value) { return String(value ?? '').replace(/[&<>'"]/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[char])); }
