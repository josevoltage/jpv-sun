package mx.lux.pos.service.impl

import groovy.util.logging.Slf4j
import mx.lux.pos.model.Empleado
import mx.lux.pos.repository.EmpleadoRepository
import mx.lux.pos.service.EmpleadoService
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource

@Slf4j
@Service( 'empleadoService' )
@Transactional( readOnly = true )
class EmpleadoServiceImpl implements EmpleadoService {

  @Resource
  private EmpleadoRepository empleadoRepository

  @Override
  Empleado obtenerEmpleado( String id ) {
    log.info( "obteniendo empleado id: ${id}" )
    if ( StringUtils.isNotBlank( id ) ) {
      Empleado empleado = empleadoRepository.findOne( id )
      if ( empleado?.id ) {
        return empleado
      } else {
        log.warn( "empleado no existe" )
      }
    } else {
      log.warn( "no se obtiene empleado, parametros invalidos" )
    }
    return null
  }

  @Override
  void actualizarPass( empleado ){
    log.info( "actualizando password de empleado id: ${empleado.id}" )
    if ( StringUtils.isNotBlank( empleado.id ) ) {
        empleadoRepository.save( empleado )
        empleadoRepository.flush()
    }
  }

}
