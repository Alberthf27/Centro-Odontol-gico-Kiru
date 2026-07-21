package pe.edu.upao.kiru.odontologos.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Vista mínima del empleado necesaria para presentar al odontólogo en la
 * reserva. No incorpora casos de uso de gestión de empleados.
 */
@Entity
@Table(name = "empleado", schema = "public")
class EmpleadoJpaEntity {

    @Id
    @Column(name = "id_empleado")
    private Integer idEmpleado;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false)
    private boolean estado;

    Integer getIdEmpleado() { return idEmpleado; }
    String getNombres() { return nombres; }
    String getApellidos() { return apellidos; }
    boolean isEstado() { return estado; }
}
