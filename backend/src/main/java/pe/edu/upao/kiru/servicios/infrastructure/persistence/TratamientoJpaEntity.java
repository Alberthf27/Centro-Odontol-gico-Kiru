package pe.edu.upao.kiru.servicios.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "tratamiento", schema = "public")
class TratamientoJpaEntity {

    @Id
    @Column(name = "id_tratamiento")
    private Integer idTratamiento;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "multiples_sesiones", nullable = false)
    private boolean multiplesSesiones;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(nullable = false)
    private boolean estado;

    protected TratamientoJpaEntity() {
    }

    Integer getIdTratamiento() { return idTratamiento; }
    String getNombre() { return nombre; }
    boolean isMultiplesSesiones() { return multiplesSesiones; }
    BigDecimal getPrecio() { return precio; }
    boolean isEstado() { return estado; }
}
