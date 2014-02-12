package mx.lux.pos.service

import mx.lux.pos.model.CriterioDet
import mx.lux.pos.model.Empleado
import mx.lux.pos.model.Incidencia

interface EmpleadoService {

  Empleado obtenerEmpleado( String id )

  String gerente( )

  void actualizarPass( empleado )

  Boolean sesionPrimeraVez()

  List<CriterioDet> obtenerCriterios()

  Incidencia saveIncidencia( Incidencia incidencia )

  void creaArchivoIncidencia( Incidencia incidencia )

  void enviaIncidencia( Incidencia incidencia )
}
