package pe.edu.upao.kiru.odontologos.domain;

import pe.edu.upao.kiru.empleados.domain.Empleado;

public class Odontologo extends Empleado {

    private String idOdontologo;
    private String especialidad;

    public String getIdOdontologo() {
        return idOdontologo;
    }

    public String getEspecialidad() {
        return especialidad;
    }
}
