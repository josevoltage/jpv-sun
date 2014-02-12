package mx.lux.pos.service.impl

import groovy.util.logging.Slf4j
import mx.lux.pos.model.CriterioDet
import mx.lux.pos.model.Empleado
import mx.lux.pos.model.Incidencia
import mx.lux.pos.model.Parametro
import mx.lux.pos.model.TipoParametro
import mx.lux.pos.repository.CriterioDetRepository
import mx.lux.pos.repository.EmpleadoRepository
import mx.lux.pos.repository.IncidenciaRepository
import mx.lux.pos.repository.ParametroRepository
import mx.lux.pos.service.EmpleadoService
import mx.lux.pos.service.business.Registry
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat

@Slf4j
@Service( 'empleadoService' )
@Transactional( readOnly = true )
class EmpleadoServiceImpl implements EmpleadoService {

  @Resource
  private EmpleadoRepository empleadoRepository

  @Resource
  private ParametroRepository parametroRepository

  @Resource
  private CriterioDetRepository criterioDetRepository

  @Resource
  private IncidenciaRepository incidenciaRepository

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
  String gerente( ) {
      Parametro parametro = parametroRepository.findOne( TipoParametro.ID_GERENTE.value )
      //Empleado empleado = empleadoRepository.findOne( parametro.valor.trim() )
      return parametro.valor

  }

    @Override
    void actualizarPass( empleado ){
        log.info( "actualizando password de empleado id: ${empleado.id}" )
        if ( StringUtils.isNotBlank( empleado.id ) ) {
            empleadoRepository.save( empleado )
            empleadoRepository.flush()
        }
    }

    @Override
    Boolean sesionPrimeraVez(){
        Boolean sesionPrimera = false
        DateFormat df = new SimpleDateFormat( "dd/MM/yyyy" )
        Parametro parametro = parametroRepository.findOne( TipoParametro.FECHA_ACTUAL.value )
        if( parametro != null ){
            sesionPrimera = !df.format(new Date()).equalsIgnoreCase(parametro.valor)
        } else {
            parametro = new Parametro()
            parametro.id = 'fecha_actual'
            parametro.valor = ''
            parametroRepository.save( parametro )
            parametroRepository.flush()
            sesionPrimera = !df.format(new Date()).equalsIgnoreCase(parametro.valor)
        }
        return sesionPrimera
    }


    @Override
    List<CriterioDet> obtenerCriterios(){
        return criterioDetRepository.findAll()
    }

    @Override
    @Transactional
    Incidencia saveIncidencia( Incidencia incidencia ){
      return  incidenciaRepository.saveAndFlush( incidencia )
    }


    void creaArchivoIncidencia( Incidencia incidencia ){
        String fichero = "${Registry.archivePath}/${incidencia.folioSoi}_${Registry.currentSite}.inc"
        log.debug( "Generando Fichero: ${ fichero }" )
        File file = new File( fichero )
        if ( file.exists() ) { file.delete() }
        log.debug( 'Creando Writer' )
        PrintStream strOut = new PrintStream( file )
        StringBuffer sb = new StringBuffer()
        sb.append("${incidencia.idEmpresa.trim()}|${incidencia.idEmpleado}|${incidencia.nombre}|${incidencia.fecha}|")
        sb.append("${incidencia.idEmpresaCap.trim()}|${incidencia.idEmpleadoCap}|${incidencia.nombreCap}|${incidencia.idGrupo}|${incidencia.idCriterio}|${incidencia.valor}|${incidencia.observacion}|${incidencia.descripcion}|")
        strOut.println sb.toString()
        strOut.close()
    }


    void enviaIncidencia( Incidencia inc ){
      log.debug( "enviaIncidencia( Incidencia incidencia )" )
      DateFormat df = new SimpleDateFormat( "dd/MM/yyyy" )
      String response = ''
      String sucursal = Registry.currentSite
      String url = Registry.getURLIncidence()
      String valor = "id_empresa="+inc.idEmpresa.trim()+"&id_empleado="+inc.idEmpleado+"&id_grupo="+inc.idGrupo+
              "&id_criterio="+inc.idCriterio+"&fecha="+df.format(inc.fecha)+"&id_empresa_cap="+inc.idEmpresaCap+
              "&id_empleado_cap="+inc.idEmpleadoCap+"&nombre_cap="+inc.nombreCap+"&Nombre="+inc.nombre+"&Valor="+
              inc.valor+"&Observacion="+inc.observacion+"&Descripcion="+inc.descripcion+"&Folio_soi="+inc.folioSoi+
              "&id_suc="+sucursal
      url += String.format( '?arg=%s', URLEncoder.encode( String.format( '%s', valor ), 'UTF-8' ) )
      log.debug( "url generada: ${url}" )
      try{
        response = url.toURL().text
        response = response?.find( /<XX>\s*(.*)\s*<\/XX>/ ) {m, r -> return r}
      } catch ( Exception e ){
        println e
      }
      log.debug( "respuesta: ${response}" )
      if( StringUtils.trimToEmpty(response) != '' && response.isNumber() ){
        Integer folio = 0
        try{
          folio = NumberFormat.getInstance().parse( response ).intValue()
        } catch ( NumberFormatException ex ){ println ex }
        inc.setFolio( folio )
      } else {
        log.debug( "folio: ${response} no es valido" )
      }
    }



}
