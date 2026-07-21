/* Listar, cancelar y reprogramar citas del cliente autenticado. */
const view = { estado: '', citas: [], citaSeleccionadaId: null };

document.addEventListener('DOMContentLoaded', async () => {
    const statusFilter = document.getElementById('statusFilter');
    const applyStatusFilter = () => {
        view.estado = statusFilter.value;
        renderList();
    };

    document.getElementById('searchCitas').addEventListener('click', applyStatusFilter);
    statusFilter.addEventListener('change', applyStatusFilter);
    document.getElementById('clearFilters').addEventListener('click', () => {
        view.estado = '';
        statusFilter.value = '';
        renderList();
    });
    document.getElementById('rescheduleSelected').addEventListener('click', () => {
        const cita = getCitaSeleccionada();
        if (cita) openRescheduleModal(cita);
    });
    document.getElementById('cancelSelected').addEventListener('click', () => {
        const cita = getCitaSeleccionada();
        if (cita) openCancelModal(cita);
    });

    await loadCitas();
    showPaymentConfirmation();
});

async function loadCitas() {
    try {
        view.citas = await KIRU_API.citas();
        renderList();
    } catch (error) {
        view.citas = [];
        document.getElementById('emptyCitas').textContent = error.message;
        document.getElementById('emptyCitas').hidden = false;
        updateSelectedActions();
    }
}

function renderList() {
    const appointments = view.citas
        .filter((appointment) => !view.estado || appointment.estado === view.estado)
        .sort((left, right) => `${left.fecha}${left.horaInicio}`.localeCompare(`${right.fecha}${right.horaInicio}`));
    const list = document.getElementById('citasList');
    const empty = document.getElementById('emptyCitas');

    if (!appointments.some((appointment) => appointment.nroCita === view.citaSeleccionadaId)) {
        view.citaSeleccionadaId = null;
    }
    empty.textContent = view.citas.length ? 'No se encontraron citas con estos filtros.' : 'No existen citas registradas.';
    empty.hidden = appointments.length > 0;
    list.innerHTML = appointments.map(renderRow).join('');
    list.querySelectorAll('[data-action="select"]').forEach((button) => {
        button.addEventListener('click', () => {
            view.citaSeleccionadaId = button.closest('tr').dataset.id;
            renderList();
        });
    });
    updateSelectedActions();
}

function renderRow(appointment) {
    const selected = appointment.nroCita === view.citaSeleccionadaId;
    const tipo = appointment.tratamiento
        ? `${appointment.tratamiento}${appointment.sesion ? ` · ${appointment.sesion}` : ''}`
        : 'Consulta';
    return `
        <tr data-id="${escapeHtml(appointment.nroCita)}" class="${selected ? 'is-selected' : ''}">
            <td class="cita-id">${escapeHtml(appointment.nroCita)}</td>
            <td>${formatTableDate(appointment.fecha)}</td>
            <td>${escapeHtml(timeShort(appointment.horaInicio))}</td>
            <td>${escapeHtml(tipo)}</td>
            <td><span class="status-pill status-${statusCss(appointment.estado)}">${statusLabel(appointment.estado)}</span></td>
            <td>${escapeHtml(appointment.nombreOdontologo || `Odontólogo ${appointment.idOdontologo}`)}</td>
            <td>
                <button type="button" class="detail-button ${selected ? 'is-selected' : ''}" data-action="select" aria-label="Seleccionar cita ${escapeHtml(appointment.nroCita)}" title="Seleccionar cita">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 16.5V20h3.5L18 9.5 14.5 6 4 16.5Z"/><path d="m13.5 7 3.5 3.5"/></svg>
                </button>
            </td>
        </tr>
    `;
}

function getCitaSeleccionada() {
    return view.citas.find((appointment) => appointment.nroCita === view.citaSeleccionadaId);
}

function updateSelectedActions() {
    const cita = getCitaSeleccionada();
    const permiteCambios = cita?.estado === 'PENDIENTE' || cita?.estado === 'REPROGRAMADA';
    document.getElementById('rescheduleSelected').disabled = !permiteCambios;
    document.getElementById('cancelSelected').disabled = !permiteCambios;
}

function openCancelModal(appointment) {
    if (!tieneAnticipacionMinima(appointment)) {
        openModal('No se puede cancelar', 'La cancelación exige al menos 24 horas de anticipación.', '<button type="button" class="btn-primary" id="modalClose">Entendido</button>');
        document.getElementById('modalClose').addEventListener('click', closeModal);
        return;
    }
    openModal(
        'Cancelar cita',
        `${formatTableDate(appointment.fecha)} · ${timeShort(appointment.horaInicio)} · ${appointment.tratamiento || 'Consulta'}${appointment.sesion ? ` · ${appointment.sesion}` : ''}. La franja volverá a estar disponible.`,
        '<button type="button" class="btn-ghost" id="modalClose">Volver</button><button type="button" class="btn-primary btn-danger" id="confirmCancel">Confirmar cancelación</button>'
    );
    document.getElementById('modalClose').addEventListener('click', closeModal);
    document.getElementById('confirmCancel').addEventListener('click', async () => {
        const button = document.getElementById('confirmCancel');
        button.disabled = true;
        button.textContent = 'Cancelando…';
        try {
            await KIRU_API.cancelarCita(appointment.nroCita);
            closeModal();
            await loadCitas();
            showToast('Cita cancelada y franja liberada.');
        } catch (error) {
            showModalError(error.message);
        }
    });
}

function openRescheduleModal(appointment) {
    if (!tieneAnticipacionMinima(appointment)) {
        openModal('No se puede reprogramar', 'La reprogramación exige al menos 24 horas de anticipación.', '<button type="button" class="btn-primary" id="modalClose">Entendido</button>');
        document.getElementById('modalClose').addEventListener('click', closeModal);
        return;
    }
    const minDate = tomorrowIso();
    openModal(
        'Reprogramar cita',
        `<label class="date-field"><span>Nueva fecha</span><input type="date" id="rescheduleDate" min="${minDate}" value="${appointment.fecha < minDate ? minDate : appointment.fecha}"></label><div class="slot-grid" id="rescheduleSlots"></div><p class="modal-note">La franja anterior será liberada al confirmar el cambio.</p>`,
        '<button type="button" class="btn-ghost" id="modalClose">Volver</button><button type="button" class="btn-primary" id="confirmReschedule" disabled>Confirmar cambio</button>'
    );

    const date = document.getElementById('rescheduleDate');
    const slots = document.getElementById('rescheduleSlots');
    const confirm = document.getElementById('confirmReschedule');
    let idFranjaNueva = null;
    const renderSlots = async () => {
        idFranjaNueva = null;
        confirm.disabled = true;
        slots.innerHTML = '<p class="empty-slots">Consultando franjas…</p>';
        try {
            const franjas = await KIRU_API.disponibilidades(appointment.idOdontologo, date.value);
            const disponibles = franjas.filter((franja) => franja.disponible);
            slots.innerHTML = disponibles.length
                ? disponibles.map((franja) => `<button type="button" class="slot-btn" data-franja="${escapeHtml(franja.idFranja)}">${timeShort(franja.horaInicio)} - ${timeShort(franja.horaFin)}</button>`).join('')
                : '<p class="empty-slots">No hay franjas disponibles ese día.</p>';
            slots.querySelectorAll('[data-franja]').forEach((button) => button.addEventListener('click', () => {
                slots.querySelectorAll('[data-franja]').forEach((item) => item.classList.remove('is-selected'));
                button.classList.add('is-selected');
                idFranjaNueva = button.dataset.franja;
                confirm.disabled = false;
            }));
        } catch (error) {
            slots.innerHTML = `<p class="empty-slots">${escapeHtml(error.message)}</p>`;
        }
    };
    date.addEventListener('change', renderSlots);
    renderSlots();
    document.getElementById('modalClose').addEventListener('click', closeModal);
    confirm.addEventListener('click', async () => {
        if (!idFranjaNueva) return;
        confirm.disabled = true;
        confirm.textContent = 'Guardando…';
        try {
            await KIRU_API.reprogramarCita(appointment.nroCita, idFranjaNueva);
            closeModal();
            await loadCitas();
            showToast('Cita reprogramada correctamente.');
        } catch (error) {
            showModalError(error.message);
        }
    });
}

function openModal(title, content, actions) {
    document.getElementById('modalBox').innerHTML = `<h3 class="modal-title">${title}</h3><p class="modal-subtext">${content}</p><div class="modal-actions">${actions}</div>`;
    document.getElementById('modalOverlay').hidden = false;
}

function closeModal() {
    document.getElementById('modalOverlay').hidden = true;
    document.getElementById('modalBox').innerHTML = '';
}

function showModalError(message) {
    const modal = document.getElementById('modalBox');
    const error = document.createElement('p');
    error.className = 'modal-note modal-note--error';
    error.textContent = message;
    modal.appendChild(error);
}

document.getElementById('modalOverlay')?.addEventListener('click', (event) => {
    if (event.target.id === 'modalOverlay') closeModal();
});

function tieneAnticipacionMinima(appointment) {
    const inicio = new Date(`${appointment.fecha}T${timeShort(appointment.horaInicio)}:00`);
    return (inicio.getTime() - Date.now()) >= 24 * 60 * 60 * 1000;
}

function statusLabel(status) {
    return { PENDIENTE: 'Pendiente', EN_ATENCION: 'En atención', ATENDIDA: 'Atendida', CANCELADA: 'Cancelada', REPROGRAMADA: 'Reprogramada' }[status] || status;
}

function statusCss(status) {
    return { PENDIENTE: 'pending', EN_ATENCION: 'confirmed', ATENDIDA: 'done', CANCELADA: 'cancelled', REPROGRAMADA: 'pending' }[status] || 'pending';
}

function formatTableDate(date) {
    return new Date(`${date}T00:00:00`).toLocaleDateString('es-PE', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function showPaymentConfirmation() {
    const payment = sessionStorage.getItem('kiru-pago-confirmado');
    if (!payment) return;
    try {
        const confirmation = JSON.parse(payment);
        showToast(`Pago aprobado. Comprobante ${confirmation.nroComprobante} generado.`);
    } catch {
        showToast('Pago aprobado. Las citas quedaron confirmadas.');
    }
    sessionStorage.removeItem('kiru-pago-confirmado');
}

function showToast(message) {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.classList.add('is-visible');
    setTimeout(() => toast.classList.remove('is-visible'), 3000);
}

function tomorrowIso() {
    const date = new Date();
    date.setDate(date.getDate() + 1);
    return date.toISOString().slice(0, 10);
}

function timeShort(time) { return String(time || '').slice(0, 5); }
function escapeHtml(value) { return String(value ?? '').replace(/[&<>'"]/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[char])); }
