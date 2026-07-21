package pe.edu.upao.kiru.servicios.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sesion_plan", schema = "public")
public class SesionJpaEntity {

    @Id
    @Column(name = "id_sesion")
    private Integer idSesion;

    @Column(name = "id_plan", nullable = false)
    private Integer idPlan;

    @Column(name = "id_cita")
    private Integer idCita;

    @Column(name = "nombre_tipo", nullable = false)
    private String nombreTipo;

    @Column(nullable = false)
    private Integer orden;

    private String descripcion;

    protected SesionJpaEntity() {
    }

    public Integer getIdSesion() { return idSesion; }
    public Integer getIdPlan() { return idPlan; }
    public String getNombreTipo() { return nombreTipo; }
    public Integer getOrden() { return orden; }
    public String getDescripcion() { return descripcion; }
}
