package mx.lux.pos.service.impl

import mx.lux.pos.model.Parametro
import mx.lux.pos.model.TipoPago
import mx.lux.pos.model.TipoParametro
import mx.lux.pos.repository.ParametroRepository
import mx.lux.pos.repository.TipoPagoRepository
import mx.lux.pos.service.TipoPagoService
import mx.lux.pos.service.business.Registry
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource

@Service( 'tipoPagoService' )
@Transactional( readOnly = true )
class TipoPagoServiceImpl implements TipoPagoService {

  private static final Logger log = LoggerFactory.getLogger( TipoPagoServiceImpl.class )

  @Resource
  private TipoPagoRepository tipoPagoRepository

  @Resource
  private ParametroRepository parametroRepository

  private List<TipoPago> listarTiposPagoRegistrados( ) {
    List<TipoPago> resultados = tipoPagoRepository.findAll() ?: [ ]
    resultados.retainAll { TipoPago tipoPago ->
      StringUtils.isNotBlank( tipoPago?.id )
    }
    return resultados.sort { TipoPago tipoPago ->
      tipoPago.tipoCon
    }
  }

  @Override
  TipoPago obtenerTipoPagoPorDefecto( ) {
    log.info( "obteniendo tipo pago por defecto" )
    return tipoPagoRepository.findOne( 'EFM' )
  }

  @Override
  List<TipoPago> listarTiposPago( ) {
    log.info( "listando tipos de pago" )
    return listarTiposPagoRegistrados()
  }

  @Override
  List<TipoPago> listarTiposPagoActivos( ) {
    log.info( "listando tipos de pago activos" )
    List<TipoPago> tiposPago = [ ]
    List<TipoPago> tiposPagoTmp = [ ]
    Parametro parametro = parametroRepository.findOne( TipoParametro.TIPO_PAGO.value )
    String[] valores = parametro?.valores
    log.debug( "obteniendo parametro de formas de pago activas id: ${parametro?.id} valores: ${valores}" )
    if ( valores.any() ) {
      List<TipoPago> resultados = listarTiposPagoRegistrados()
      log.debug( "tipos de pago existentes: ${resultados*.id}" )
      tiposPagoTmp = resultados.findAll { TipoPago tipoPago ->
        valores.contains( tipoPago?.id?.trim() )
      }
      tiposPago.addAll(tiposPagoTmp)
      if( Registry.activeTpv ){
        for(TipoPago tipoPago : tiposPagoTmp){
          if(StringUtils.trimToEmpty(tipoPago.id).startsWith("TC") ||
                  StringUtils.trimToEmpty(tipoPago.id).startsWith("TD")){
            TipoPago tipoPagoTpv = new TipoPago()
            tipoPagoTpv.id = tipoPago.id+"TPV"
            tipoPagoTpv.descripcion = "[TPV]"+tipoPago.descripcion
            tipoPagoTpv.tipoSoi = tipoPago.tipoSoi
            tipoPagoTpv.tipoCon = tipoPago.tipoCon
            tipoPagoTpv.f1 = tipoPago.f1
            tipoPagoTpv.f2 = tipoPago.f2
            tipoPagoTpv.f3 = tipoPago.f3
            tipoPagoTpv.f4 = tipoPago.f4
            tipoPagoTpv.f5 = tipoPago.f5
            tiposPago.add(tipoPagoTpv)
          }
        }
      }
      log.debug( "tipos de pago obtenidos: ${tiposPago*.id}" )
    }
    return tiposPago
  }
}
