package pe.edu.upao.kiru.odontologos.domain;

import pe.edu.upao.kiru.empleados.domain.Empleado;

public class Odontologo extends Empleado {

    private String idOdontologo;
    private String especialidad;
    private String nombreCompleto;

    public static Odontologo reconstituir(String idOdontologo, String especialidad) {
        return reconstituir(idOdontologo, especialidad, null);
    }

    public static Odontologo reconstituir(
            String idOdontologo,
            String especialidad,
            String nombreCompleto
    ) {
        Odontologo odontologo = new Odontologo();
        odontologo.idOdontologo = idOdontologo;
        odontologo.especialidad = especialidad;
        odontologo.nombreCompleto = nombreCompleto;
        return odontologo;
    }

    public String getIdOdontologo() {
        return idOdontologo;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }
}
