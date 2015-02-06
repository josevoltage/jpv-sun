package mx.lux.pos.service.impl

import groovy.util.logging.Slf4j
import mx.lux.pos.model.Pago
import mx.lux.pos.model.QRetorno
import mx.lux.pos.model.Retorno
import mx.lux.pos.repository.NotaVentaRepository
import mx.lux.pos.repository.PagoRepository
import mx.lux.pos.repository.RetornoRepository
import mx.lux.pos.service.PagoService
import mx.lux.pos.service.TicketService
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import subtech.GPAYAPI

import javax.annotation.Resource
import mx.lux.pos.service.business.Registry

import java.text.NumberFormat

@Slf4j
@Service( "pagoService" )
@Transactional( readOnly = true )
class PagoServiceImpl implements PagoService {

  @Resource
  private PagoRepository pagoRepository

  @Resource
  private NotaVentaRepository notaVentaRepository

  @Resource
  private RetornoRepository retornoRepository

  @Resource
  private TicketService ticketService

  private String TAG_TC = "TV"
  private String TAG_TD = "DV"
  private String TAG_TAE = "AV"

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

  @Override
  String obtenerPlanNormalTarjetaCredito( ) {
      log.info( 'obtenerPlanNormalTarjetaCredito( )' )
      return Registry.normalPlanCreditCard()
  }

  @Override
  Retorno obtenerRetorno( String folio ) {
      log.debug( "obteniendo retorno con folio $folio" )
      Retorno retorno = new Retorno()
      Integer folioInt = 0
      try{
          folioInt = NumberFormat.getInstance().parse( folio ).intValue()
      } catch (Exception e){
          retorno = null
      }
      if( folioInt != 0 ){
          QRetorno ret = QRetorno.retorno
          retorno = retornoRepository.findOne( folioInt )
          if( (retorno == null) || (StringUtils.trimToEmpty(retorno.ticketDestino) != '') ){
              retorno = null
          }
      }
      return retorno
  }


  @Override
  @Transactional
  Retorno actualizarRetorno( Retorno retorno ) {
      return retornoRepository.save( retorno )
  }


  @Override
  Pago leerTarjeta( String idOrder, Pago tmpPago ){
    Pago pago = tmpPago
    String host = Registry.hostTpv
    Integer puerto = Registry.portTpv
    Integer timeout = Registry.timeoutTpv
    String user = Registry.userTpv
    String pass = Registry.passTpv
    String pagoSelect = StringUtils.trimToEmpty(tmpPago.idFPago)
    String pagoReturn = ""
    GPAYAPI ctx = new GPAYAPI();
      ctx.SetAttribute( "HOST", host );
      ctx.SetAttribute( "PORT", puerto )
      ctx.SetAttribute( "TIMEOUT", timeout );
      ctx.SetString( "dcs_form", "T060S000" )
      ctx.SetString( "trn_usr_id", user )
      ctx.SetString( "trn_password", pass )
      ctx.SetString( "dcs_reply_get", "localhost" )
      if( StringUtils.trimToEmpty(pago.idFPago).contains("UV") ){
        Double montoDolares = 0.00
        try{
          montoDolares = NumberFormat.getInstance().parse(StringUtils.trimToEmpty(pago.idPlan)).doubleValue()
        } catch( NumberFormatException e ){ println e }
        ctx.SetFloat( "trn_amount", montoDolares )
      } else{
        ctx.SetFloat( "trn_amount", pago.monto.doubleValue() )
      }

      if( StringUtils.trimToEmpty(pago.idFPago).startsWith("DV") || StringUtils.trimToEmpty(pago.idFPago).contains("UV") ){
        ctx.SetInteger( "trn_qty_pay", 1 )
      } else {
        Integer plan = 1
        String sub = (pago?.plan != null && StringUtils.trimToEmpty(pago?.plan?.descripcion).length() > 1) ? StringUtils.trimToEmpty(pago?.plan?.descripcion).substring(0,2) : ""
        if(StringUtils.trimToEmpty(sub).isNumber() ){
          try{
            plan = NumberFormat.getInstance().parse( StringUtils.trimToEmpty(sub) )
          } catch ( NumberFormatException e ) { println e }
        }
        ctx.SetInteger( "trn_qty_pay", plan <= 0 ? 1 : plan )
      }
      ctx.SetFloat( "trn_tip_amount", 0 )
      ctx.SetFloat( "trn_cashback_amount", 0.00 )
      Socket socket = ctx.TCP_Open();

      int execute = ctx.Execute()
      println "Respuesta de la ejecucion: "+execute
      if ( execute == 0 && StringUtils.trimToEmpty(ctx.GetString("trn_auth_code")).length() > 0 ){
          String lecturaTar = ""
          pago.idFactura = idOrder
          pago.referenciaPago = ctx.GetString( "trn_card_number" )
          pago.clave = ctx.GetString( "trn_card_number" )
          pago.referenciaClave = ctx.GetString( "trn_auth_code" )
          if( StringUtils.trimToEmpty(ctx.GetString('trn_input_mode')).equalsIgnoreCase("7") ){
            lecturaTar = "CHIP"
          } else if( StringUtils.trimToEmpty(ctx.GetString('trn_input_mode')).equalsIgnoreCase("2") ){
              lecturaTar = "DIGITADA"
          } else {
            lecturaTar = "BANDA"
          }
          pago.idTerminal = ctx.GetString("trn_pro_name")+"|"+ctx.GetString("trn_id")+"|"+ctx.GetString("trn_aid")+"|"+ctx.GetString("trn_arqc ")+"|"+ctx.GetString("trn_cardholder_name")+"|"+lecturaTar+"|"
          String tipo = ""
          if(StringUtils.trimToEmpty(ctx.GetString("trn_pre_type")).equalsIgnoreCase("1")){
            pagoReturn = "TARJETA CREDITO"
            if( pago.idFPago.startsWith(TAG_TD) || pago.idFPago.startsWith(TAG_TAE) ){
              pago.idFPago = "TV"
            }
          } else if(StringUtils.trimToEmpty(ctx.GetString("trn_pre_type")).equalsIgnoreCase("2")){
            pagoReturn = "TARJETA DEBITO"
            if( pago.idFPago.startsWith(TAG_TC) || pago.idFPago.startsWith(TAG_TAE) ){
                  pago.idFPago = "DV"
            }
          } else if(StringUtils.trimToEmpty(ctx.GetString("trn_pre_type")).equalsIgnoreCase("0")){
              pagoReturn = "DESCONOCIDA"
              if( pago.idFPago.startsWith(TAG_TD) || pago.idFPago.startsWith(TAG_TC) ){
                pago.idFPago = "AV"
              }
          }
          try{
              if( pago.idFPago.equalsIgnoreCase("UV")){
                pago.idPlan = NumberFormat.getInstance().parse(ctx.GetString( "trn_amount" ).replace(",",""))
              } else {
                pago.monto = NumberFormat.getInstance().parse(ctx.GetString( "trn_amount" ).replace(",",""))
                pago.idPlan = ctx.GetInteger( "trn_qty_pay" )
              }
          } catch ( NumberFormatException e ){ println e }
          ctx.TCP_Close();
      } else {
          println( "ERROR AL VALIDAR PAGO" ) ;
          ctx.ClearAttributes()
          ctx.ClearFields()
          ctx.TCP_Close();
          pago = null
      }

      String ruta = "${Registry.processedFilesPath}/logTpv.txt"
      File archivo = new File(ruta);
      FileWriter escribir = new FileWriter(archivo,true);
      if(!archivo.exists()) {
        archivo.createNewFile()
      }
      escribir.write("IdFactura: ${StringUtils.trimToEmpty(pago?.idFactura)} Fecha:${new Date().format('dd-MM-yyyy HH:mm')} PagoSeleccionado:${pagoSelect} PagoRegresoTpv:${pagoReturn}")
      escribir.close()
      ctx.ClearFields();

    return pago
  }


}
