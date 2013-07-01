package mx.lux.pos.ui.controller

import groovy.util.logging.Slf4j
import mx.lux.pos.model.Empleado
import mx.lux.pos.service.EmpleadoService
import mx.lux.pos.service.SucursalService
import mx.lux.pos.ui.model.Branch
import mx.lux.pos.ui.model.Session
import mx.lux.pos.ui.model.SessionItem
import mx.lux.pos.ui.model.User
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.text.DateFormat
import java.text.SimpleDateFormat

@Slf4j
@Component
class AccessController {

  private static EmpleadoService empleadoService
  private static SucursalService sucursalService

  @Autowired
  AccessController( EmpleadoService empleadoService, SucursalService sucursalService ) {
    this.empleadoService = empleadoService
    this.sucursalService = sucursalService
  }

  static User getUser( String username ) {
    log.info( "solicitando usuario: ${username}" )
    return User.toUser( empleadoService.obtenerEmpleado( username ) )
  }

  static boolean checkCredentials( String username, String password ) {
    log.info( "comprobando credenciales para el usuario: ${username}" )
    if ( StringUtils.isNotBlank( username ) && StringUtils.isNotBlank( password ) ) {
      User user = getUser( username )
      if ( StringUtils.isNotBlank( user?.username ) ) {
        if ( password.equalsIgnoreCase( user?.password ) ) {
          log.info( "credenciales correctas" )
          return true
        } else {
          log.warn( "acceso denegado, credenciales incorrectas" )
        }
      } else {
        log.warn( "usuario no existente" )
      }
    } else {
      log.warn( "no se comprueban credenciales, parametros invalidos" )
    }
    return false
  }

  static User logIn( String username, String password ) {
    log.info( "solicitando autorizacion de acceso para el usuario: $username" )
    if ( checkCredentials( username, password ) ) {
      User user = getUser( username )
      Branch branch = Branch.toBranch( sucursalService.obtenSucursalActual() )
      Session.put( SessionItem.USER, user )
      Session.put( SessionItem.BRANCH, branch )
      log.info( "acceso autorizado: $username" )
      return user
    } else {
      log.warn( "acceso denegado, credenciales incorrectas" )
    }
    return null
  }

  static void logOut( ) {
    Session.clear()
    log.info( "log out" )
  }

  private static boolean isAuthorizer( Empleado empleado ) {
    log.info( "verificando si empleado es autorizador: ${empleado?.id}" )
    if ( empleado?.id ) {
      if ( ( 1..2 ).contains( empleado.idPuesto ) ) {
        log.info( "usuario es autorizador" )
        return true
      } else {
        log.info( "no es usuario autorizador" )
      }
    } else {
      log.warn( "no se verifica usuario, parametros invalidos" )
    }
    return false
  }

  static boolean isAuthorizerInSession( ) {
    log.info( "comprobando si usuario en sesion requiere autorizacion" )
    User user = Session.get( SessionItem.USER ) as User
    log.debug( "usuario en sesion: ${user?.username}" )
    if ( StringUtils.isNotBlank( user?.username ) ) {
      Empleado empleado = empleadoService.obtenerEmpleado( user.username )
      if ( isAuthorizer( empleado ) ) {
        log.info( "usuario autorizador, no requiere autorizacion" )
        return true
      } else {
        log.info( "usuario requiere autorizacion" )
      }
    } else {
      log.warn( "no se realiza comprobacion, no existe usuario en sesion" )
    }
    return false
  }

  static boolean canAuthorize( String username, String password ) {
    log.info( "solicitando autorizacion por usuario: $username" )
    if ( checkCredentials( username, password ) ) {
      Empleado empleado = empleadoService.obtenerEmpleado( username )
      if ( isAuthorizer( empleado ) ) {
        log.info( "autorizacion realizada: $username" )
        return true
      } else {
        log.info( "autorizacion rechazada, no es usuario autorizador" )
      }
    } else {
      log.warn( "credenciales erroneas" )
    }
    return false
  }

  static String lastDate( ){
      sucursalService.obtenerParametroFecha()
  }

  static Boolean validDate( String date ){
      log.debug( "validando fecha" )
      DateFormat df = new SimpleDateFormat( "dd/MM/yyyy" )
      Boolean compare = df.format(new Date()).equalsIgnoreCase(StringUtils.trimToEmpty(date.trim()))
      if( compare ){
          sucursalService.registrarFechaSistema( new Date() )
      }
      return compare
  }
}
