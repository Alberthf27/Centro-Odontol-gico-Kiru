package pe.edu.upao.kiru.shared.config;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Datos mínimos y reconocibles para recorrer la iteración de reservas en modo demostración. */
@Component
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final UUID demoAuthUserId;

    public DemoDataInitializer(
            JdbcTemplate jdbcTemplate,
            @Value("${app.demo.auth-user-id}") UUID demoAuthUserId
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.demoAuthUserId = demoAuthUserId;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        insertarOrganizacion();
        insertarRecepcionista();
        insertarClienteYTratamientos();
        insertarClientesPresenciales();
        insertarPlanConSesiones();
        insertarFranjas();
        insertarFranjasDeHoy();
        insertarCitasDeHoy();
        LOGGER.info("Datos de demostración de reservas preparados.");
    }

    private void insertarRecepcionista() {
        jdbcTemplate.update("""
                insert into public.cargo (id_cargo, nombre, descripcion, estado)
                values (-9002, 'Recepcionista', 'Monitoreo y gestión de citas en recepción', true)
                on conflict (id_cargo) do update set estado = true
                """);
        insertarCuenta(-9103, "demo.recepcionista", "recepcionista@demo.kiru");
        jdbcTemplate.update("""
                insert into public.empleado (
                    id_empleado, id_cuenta, id_cargo, dni, nombres, apellidos,
                    celular, direccion, fecha_nacimiento, estado
                ) values (-9203, -9103, -9002, '70100003', 'María', 'Torres Vega',
                          '999100003', 'Trujillo', date '1994-03-10', true)
                on conflict (id_empleado) do update
                set nombres = excluded.nombres, apellidos = excluded.apellidos, estado = true
                """);
    }

    private void insertarClientesPresenciales() {
        insertarClientePresencial(-9402, "70654321", "Luis Alberto", "Paredes León", "1995-08-20", "988222333");
        insertarClientePresencial(-9403, "70987654", "María Fernanda", "Quispe Ramos", "1988-11-02", "977333444");
    }

    private void insertarClientePresencial(
            int idCliente,
            String dni,
            String nombres,
            String apellidos,
            String fechaNacimiento,
            String celular
    ) {
        // Clientes registrados en recepción: sin cuenta web (auth_user_id nulo).
        jdbcTemplate.update("""
                insert into public.cliente (
                    id_cliente, dni, nombres, apellidos, fecha_nacimiento,
                    celular, domicilio, estado, auth_user_id
                ) values (?, ?, ?, ?, ?, ?, 'Trujillo', true, null)
                on conflict (id_cliente) do nothing
                """, idCliente, dni, nombres, apellidos, LocalDate.parse(fechaNacimiento), celular);
    }

    /** Franjas del día actual para que el monitor de recepción siempre tenga agenda. */
    private void insertarFranjasDeHoy() {
        LocalDate hoy = LocalDate.now();
        int base = -(1_150_000 + (hoy.getYear() % 100) * 40_000 + hoy.getDayOfYear() * 100);
        int id = base;
        for (int odontologo : new int[]{-9301, -9302}) {
            for (LocalTime inicio : new LocalTime[]{
                    LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(10, 0),
                    LocalTime.of(11, 0), LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0)
            }) {
                jdbcTemplate.update("""
                        insert into public.franja_horaria (
                            id_franja, id_odontologo, fecha, hora_inicio, hora_fin, disponible
                        ) values (?, ?, current_date, ?, ?, true)
                        on conflict (id_franja) do nothing
                        """, id--, odontologo, inicio, inicio.plusHours(1));
            }
        }
    }

    /** Citas del día actual (una por cliente de prueba) para poblar el monitor de recepción. */
    private void insertarCitasDeHoy() {
        String sufijo = LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        insertarCitaDeHoy(1, sufijo, -9401, -9301, null, LocalTime.of(8, 0), "50.00");
        insertarCitaDeHoy(2, sufijo, -9402, -9302, null, LocalTime.of(10, 0), "50.00");
        insertarCitaDeHoy(3, sufijo, -9403, -9301, -9501, LocalTime.of(15, 0), "85.00");
    }

    private void insertarCitaDeHoy(
            int correlativo,
            String sufijo,
            int idCliente,
            int idOdontologo,
            Integer idTratamiento,
            LocalTime horaInicio,
            String monto
    ) {
        String nroPedido = "PED-DEMO-" + sufijo + "-" + correlativo;
        String nroCita = "CITA-DEMO-" + sufijo + "-" + correlativo;
        jdbcTemplate.update("""
                insert into public.pedido_reserva (
                    id_cliente, nro_pedido, fecha_creacion, monto_total, estado_pago
                )
                select ?, ?, now(), cast(? as numeric), cast('PENDIENTE' as pedido_reserva_estado)
                where not exists (select 1 from public.pedido_reserva where nro_pedido = ?)
                """, idCliente, nroPedido, monto, nroPedido);
        jdbcTemplate.update("""
                insert into public.cita (
                    id_pedido, id_cliente, id_odontologo, id_tratamiento, nro_cita,
                    fecha_programada, hora_inicio, hora_fin, estado,
                    asistencia_cliente, asistencia_odontologo
                )
                select p.id_pedido, ?, ?, ?, ?, current_date, ?, ?,
                       cast('PENDIENTE' as estado_cita_enum), false, false
                from public.pedido_reserva p
                where p.nro_pedido = ?
                  and not exists (select 1 from public.cita c where c.nro_cita = ?)
                """, idCliente, idOdontologo, idTratamiento, nroCita,
                horaInicio, horaInicio.plusHours(1), nroPedido, nroCita);
        jdbcTemplate.update("""
                update public.franja_horaria f
                set disponible = false, id_cita = c.id_cita
                from public.cita c
                where c.nro_cita = ?
                  and f.id_odontologo = c.id_odontologo
                  and f.fecha = c.fecha_programada
                  and f.hora_inicio = c.hora_inicio
                  and f.id_cita is null
                """, nroCita);
    }

    private void insertarOrganizacion() {
        jdbcTemplate.update("""
                insert into public.cargo (id_cargo, nombre, descripcion, estado)
                values (-9001, 'Odontólogo', 'Cargo de demostración', true)
                on conflict (id_cargo) do update set estado = true
                """);
        insertarCuenta(-9101, "demo.odontologo1", "odontologo1@demo.kiru");
        insertarCuenta(-9102, "demo.odontologo2", "odontologo2@demo.kiru");
        insertarEmpleado(-9201, -9101, "70100001", "Renato", "Deza Alfaro", "999100001");
        insertarEmpleado(-9202, -9102, "70100002", "Lucía", "Salazar Ruiz", "999100002");
        jdbcTemplate.update("""
                insert into public.odontologo (id_odontologo, id_empleado, especialidad)
                values (-9301, -9201, 'Odontología general')
                on conflict (id_odontologo) do update
                set id_empleado = excluded.id_empleado, especialidad = excluded.especialidad
                """);
        jdbcTemplate.update("""
                insert into public.odontologo (id_odontologo, id_empleado, especialidad)
                values (-9302, -9202, 'Ortodoncia')
                on conflict (id_odontologo) do update
                set id_empleado = excluded.id_empleado, especialidad = excluded.especialidad
                """);
    }

    private void insertarCuenta(int id, String usuario, String correo) {
        jdbcTemplate.update("""
                insert into public.cuenta (id_cuenta, usuario, correo, clave_hash, estado)
                values (?, ?, ?, 'NO_UTILIZADA_EN_MODO_DEMO', true)
                on conflict (id_cuenta) do update set estado = true
                """, id, usuario, correo);
    }

    private void insertarEmpleado(
            int idEmpleado,
            int idCuenta,
            String dni,
            String nombres,
            String apellidos,
            String celular
    ) {
        jdbcTemplate.update("""
                insert into public.empleado (
                    id_empleado, id_cuenta, id_cargo, dni, nombres, apellidos,
                    celular, direccion, fecha_nacimiento, estado
                ) values (?, ?, -9001, ?, ?, ?, ?, 'Trujillo', date '1990-01-01', true)
                on conflict (id_empleado) do update
                set nombres = excluded.nombres, apellidos = excluded.apellidos, estado = true
                """, idEmpleado, idCuenta, dni, nombres, apellidos, celular);
    }

    private void insertarClienteYTratamientos() {
        jdbcTemplate.update("""
                insert into public.cliente (
                    id_cliente, dni, nombres, apellidos, fecha_nacimiento,
                    celular, domicilio, estado, auth_user_id
                ) values (-9401, '71234567', 'Ana', 'Rojas Pérez', date '2000-04-15',
                          '999111222', 'Trujillo', true, ?)
                on conflict (id_cliente) do update
                set auth_user_id = excluded.auth_user_id, estado = true
                """, demoAuthUserId);
        insertarTratamiento(-9501, "Limpieza dental", false, "85.00");
        insertarTratamiento(-9502, "Ortodoncia", true, "120.00");
    }

    private void insertarTratamiento(int id, String nombre, boolean multiplesSesiones, String precio) {
        jdbcTemplate.update("""
                insert into public.tratamiento (
                    id_tratamiento, nombre, multiples_sesiones, precio, estado
                ) values (?, ?, ?, cast(? as numeric), true)
                on conflict (id_tratamiento) do update
                set nombre = excluded.nombre,
                    multiples_sesiones = excluded.multiples_sesiones,
                    precio = excluded.precio,
                    estado = true
                """, id, nombre, multiplesSesiones, precio);
    }

    private void insertarPlanConSesiones() {
        jdbcTemplate.update("""
                insert into public.plan_tratamiento (
                    id_plan, id_cliente, id_odontologo, id_tratamiento,
                    descripcion, fecha_inicio, estado, progreso
                ) values (-9601, -9401, -9302, -9502,
                          'Plan de ortodoncia asignado por la odontóloga',
                          current_date - 7, cast('ACTIVO' as estado_plan_tratamiento_enum), 0)
                on conflict (id_plan) do update
                set estado = cast('ACTIVO' as estado_plan_tratamiento_enum)
                """);
        insertarSesion(-9701, 1, "Instalación de brackets", "Primera sesión del plan");
        insertarSesion(-9702, 2, "Control de ortodoncia", "Segunda sesión pendiente");
    }

    private void insertarSesion(int id, int orden, String nombre, String descripcion) {
        jdbcTemplate.update("""
                insert into public.sesion_plan (
                    id_sesion, id_plan, nombre_tipo, orden, descripcion
                ) values (?, -9601, ?, ?, ?)
                on conflict (id_sesion) do update
                set nombre_tipo = excluded.nombre_tipo,
                    orden = excluded.orden,
                    descripcion = excluded.descripcion
                """, id, nombre, orden, descripcion);
    }

    private void insertarFranjas() {
        LocalDate primeraFecha = siguienteDiaLaborable(LocalDate.now().plusDays(2));
        LocalDate segundaFecha = siguienteDiaLaborable(primeraFecha.plusDays(1));
        LocalDate terceraFecha = siguienteDiaLaborable(segundaFecha.plusDays(1));
        int id = -9801;
        for (int odontologo : new int[]{-9301, -9302}) {
            for (LocalDate fecha : new LocalDate[]{primeraFecha, segundaFecha, terceraFecha}) {
                for (LocalTime inicio : new LocalTime[]{LocalTime.of(9, 0), LocalTime.of(10, 0)}) {
                    insertarFranja(id--, odontologo, fecha, inicio);
                }
            }
        }

        // Se usa otro rango para no alterar franjas de demostraciones ya realizadas.
        int idAdicional = -100001;
        for (int odontologo : new int[]{-9301, -9302}) {
            for (LocalDate fecha : new LocalDate[]{primeraFecha, segundaFecha, terceraFecha}) {
                for (LocalTime inicio : new LocalTime[]{
                        LocalTime.of(8, 0),
                        LocalTime.of(11, 0),
                        LocalTime.of(14, 0),
                        LocalTime.of(15, 0),
                        LocalTime.of(16, 0)
                }) {
                    insertarFranja(idAdicional--, odontologo, fecha, inicio);
                }
            }
        }
    }

    private void insertarFranja(int id, int odontologo, LocalDate fecha, LocalTime inicio) {
        jdbcTemplate.update("""
                insert into public.franja_horaria (
                    id_franja, id_odontologo, fecha, hora_inicio, hora_fin, disponible
                ) values (?, ?, ?, ?, ?, true)
                on conflict do nothing
                """, id, odontologo, fecha, inicio, inicio.plusHours(1));
    }

    private LocalDate siguienteDiaLaborable(LocalDate fecha) {
        LocalDate resultado = fecha;
        while (resultado.getDayOfWeek() == DayOfWeek.SUNDAY) {
            resultado = resultado.plusDays(1);
        }
        return resultado;
    }
}
