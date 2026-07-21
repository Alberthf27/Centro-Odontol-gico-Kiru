package pe.edu.upao.kiru.clientes.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cliente", schema = "public")
class ClienteJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(name = "id_cuenta")
    private Integer idCuenta;

    @Column(nullable = false, unique = true)
    private String dni;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false)
    private String celular;

    private String domicilio;

    @Column(nullable = false)
    private boolean estado;

    @Column(name = "auth_user_id")
    private UUID authUserId;

    protected ClienteJpaEntity() {
    }

    ClienteJpaEntity(String dni, String nombres, String apellidos, LocalDate fechaNacimiento, String celular, String domicilio, UUID authUserId) {
        this.dni = dni;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.celular = celular;
        this.domicilio = domicilio;
        this.authUserId = authUserId;
        this.estado = true;
    }

    Integer getIdCliente() { return idCliente; }
    String getDni() { return dni; }
    String getNombres() { return nombres; }
    String getApellidos() { return apellidos; }
    LocalDate getFechaNacimiento() { return fechaNacimiento; }
    String getCelular() { return celular; }
    String getDomicilio() { return domicilio; }
    boolean isEstado() { return estado; }
}
