package mx.lux.pos.service.impl

import groovy.util.logging.Slf4j
import mx.lux.pos.model.Pago
import mx.lux.pos.repository.PagoRepository
import mx.lux.pos.service.PagoService
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource
import mx.lux.pos.service.business.Registry

@Slf4j
@Service( "pagoService" )
@Transactional( readOnly = true )
class PagoServiceImpl implements PagoService {

  @Resource
  private PagoRepository pagoRepository

  @Override
  Pago obtenerPago( Integer id ) {
    log.info( "obteniendo pago: ${id}" )
    return pagoRepository.findOne( id ?: 0 )
  }

  @Override
  List<Pago> listarPagosPorIdFactura( String idFactura ) {
    log.info( "listando pagos con idFactura: ${idFactura} " )
    if ( StringUtils.isNotBlank( idFactura ) ) {
      List<Pago> pagos = pagoRepository.findByIdFacturaOrderByFechaAsc( idFactura )
      log.debug( "obtiene pagos: ${pagos?.id}" )
      return pagos?.any() ? pagos : [ ]
    } else {
      log.warn( 'no se obtiene lista de pagos, parametros invalidos' )
    }
    return [ ]
  }

  @Override
  @Transactional
  Pago actualizarPago( Pago pago ) {
    log.info( "actualizando pago id: ${pago?.id}" )
    if ( pago?.id ) {
      return pagoRepository.save( pago )
    }
    return null
  }


  @Override
  Boolean obtenerTipoPagosDolares( String formaPago ) {
    log.info( 'obtenerTipoPagosDolares( )' )
    return Registry.isCardPaymentInDollars( formaPago )
  }
}
