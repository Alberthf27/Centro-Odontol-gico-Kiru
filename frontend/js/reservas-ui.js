/* Respuesta de la pasarela simulada: confirma el pago en el backend. */
(function () {
  const DRAFT_KEY = 'kiru-pedido-pago';
  const CONFIRMATION_KEY = 'kiru-pago-confirmado';

  function getDraft() {
    try {
      return JSON.parse(sessionStorage.getItem(DRAFT_KEY));
    } catch {
      return null;
    }
  }

  function currency(value) {
    return `S/ ${Number(value).toFixed(2)}`;
  }

  function showMessage(message, isError = false) {
    const element = document.getElementById('gateway-message');
    element.hidden = !message;
    element.textContent = message;
    element.classList.toggle('is-error', Boolean(message && isError));
  }

  function initGateway() {
    const approve = document.getElementById('approve-payment');
    if (!approve) return;
    const draft = getDraft();
    if (!draft?.idPedido || Number(draft.montoTotal) <= 0) {
      window.location.replace('cliente/agendar-cita.html');
      return;
    }

    document.getElementById('gateway-title').textContent = `Pagar pedido ${draft.nroPedido}`;
    document.getElementById('gateway-total').textContent = currency(draft.montoTotal);
    approve.addEventListener('click', async () => {
      approve.disabled = true;
      approve.textContent = 'Confirmando…';
      showMessage('Registrando la confirmación de pago…');
      try {
        const payment = await KIRU_API.registrarPago(draft.idPedido, Number(draft.montoTotal));
        sessionStorage.setItem(CONFIRMATION_KEY, JSON.stringify({
          nroPedido: draft.nroPedido,
          nroComprobante: payment.nroComprobante,
        }));
        sessionStorage.removeItem(DRAFT_KEY);
        window.location.href = 'cliente/mis-citas.html';
      } catch (error) {
        showMessage(error.message, true);
        approve.disabled = false;
        approve.textContent = 'Simular pago aprobado';
      }
    });
  }

  document.addEventListener('DOMContentLoaded', initGateway);
}());
