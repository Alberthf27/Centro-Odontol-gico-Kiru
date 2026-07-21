package pe.edu.upao.kiru.odontologos.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "odontologo", schema = "public")
class OdontologoJpaEntity {

    @Id
    @Column(name = "id_odontologo")
    private Integer idOdontologo;

    @Column(name = "id_empleado", nullable = false)
    private Integer idEmpleado;

    @Column(nullable = false)
    private String especialidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", referencedColumnName = "id_empleado", insertable = false, updatable = false)
    private EmpleadoJpaEntity empleado;

    protected OdontologoJpaEntity() {
    }

    Integer getIdOdontologo() { return idOdontologo; }
    String getEspecialidad() { return especialidad; }
    String getNombreCompleto() {
        return empleado == null ? null : empleado.getNombres() + " " + empleado.getApellidos();
    }
}
