package mx.lux.pos.service.impl

import mx.lux.pos.repository.impl.RepositoryFactory
import mx.lux.pos.service.IOService
import mx.lux.pos.service.io.AsynchronousNotificationDispatcher
import mx.lux.pos.util.CustomDateUtils
import mx.lux.pos.util.FileFilterUtil
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang3.time.DateUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import mx.lux.pos.model.*
import mx.lux.pos.repository.*
import mx.lux.pos.service.business.*
import org.apache.commons.lang3.StringUtils

@Service
@Transactional( readOnly = true )
class IOServiceImpl implements IOService {

  private static final String MSG_PART_CLASS_FILE_LOAD = 'Importar Clasificacion de Articulos'
  private static final String MSG_PART_CLASS_FILE_LOADED = 'Clasif de Articulos  Registros:%,d  Actualizados: %,d'

  private static final String TAG_ACK_REMITTANCES = 'REM'
  private static final String TAG_ESTADO_REM_CARGADA = 'cargado'

  private static final String TAG_ACK_SALES = AckType.VENTA_DIA
  private static final String TAG_ACK_ADJUST = AckType.MODIF_VENTA

  private static IOServiceImpl instance

  private Logger logger = LoggerFactory.getLogger( this.getClass() )

  @Autowired
  MonedaRepository currMaster

  @Autowired
  MonedaDetalleRepository currRateDetail

  ArticuloClassReaderTask classReader = new ArticuloClassReaderTask()

  IOServiceImpl( ) {
    instance = this
  }

  static IOService getInstance( ) {
    return instance
  }

  String getPartFilename( ) {
    return Registry.partMasterFile
  }

  void loadPartFile( ) {
    ArticuloSunglassImportTask task = new ArticuloSunglassImportTask()
    try {
      task.filename = Registry.partMasterFile
      task.run()
      File f = new File( Registry.partMasterFile )
      if ( f.exists() ) {
        f.renameTo( new File( Registry.processedFilesPath, f.name ) )
      }
    } catch ( Exception e ) {
      this.logger.error( String.format( "Error loading %s", Registry.partMasterFile ), e )
    }
  }

  String loadPartFile( File pFile ) {
    String skus = ''
    ArticuloSunglassImportTask task = new ArticuloSunglassImportTask()
    try {
      task.filename = pFile.absolutePath
      skus = task.runNew( pFile )
      if ( pFile.exists() ) {
        pFile.renameTo( new File( Registry.processedFilesPath, pFile.name ) )
      }
    } catch ( Exception e ) {
      this.logger.error( String.format( "Error loading %s", pFile.getAbsolutePath() ), e )
    }
    return skus
  }

  String getPartClassFilename( ) {
    return classReader.getFilename()
  }

  Map<String, Object> loadPartClassFile( File pFile  ) {
    logger.debug( MSG_PART_CLASS_FILE_LOAD )
    Map<String, Object> taskSummary = new HashMap<String, Object>()
    classReader.run( pFile )
    taskSummary.put( 'numFiles', classReader.fileCount )
    taskSummary.put( 'filename', classReader.filename )
    taskSummary.put( 'records', classReader.linesRead )
    taskSummary.put( 'items', classReader.partCount )
    taskSummary.put( 'updates', classReader.partUpdatedCount )
    logger.debug( String.format( MSG_PART_CLASS_FILE_LOADED, classReader.linesRead, classReader.partUpdatedCount ) )

    File source = new File( classReader.filename )
    File destination = new File( Registry.find( TipoParametro.RUTA_RECIBIDOS ).valor )
    if ( destination.exists() ) {
        def newFile = new File( destination, source.name )
        source.renameTo( newFile )
    }
    return taskSummary
  }

  File getIncomingLocation( ) {
    return new File( Registry.inputFilePath )
  }

  FilenameFilter getPartMasterFilter( ) {
    String regex = FileFilterUtil.toRegExpLowerCase( Registry.productsFilePattern )
    LocalFilenameFilter filter = new LocalFilenameFilter( regex )
    return filter
  }

  File getArchiveLocation( ) {
    return new File( Registry.processedFilesPath )
  }

  FilenameFilter getAllFilesFilter( ) {
    return LocalFilenameFilter.ALL_FILES
  }

  String getProductsFilePattern( ) {
    return Registry.productsFilePattern
  }

  String getClasificationsFilePattern( ) {
      return Registry.clasificationFilePattern
  }

  String getEmployeeFilePattern( ) {
    return Registry.employeeFilePattern
  }

  String getFxRatesFilePattern( ) {
    return Registry.fxRatesFilePattern
  }

  Map<String, Object> loadEmployeeFile( File pInputFile ) {
    Map<String, Object> taskSummary = new HashMap<String, Object>()
    EmployeeImportTask task = new EmployeeImportTask()
    task.setInputFile( pInputFile )
    task.run()
    taskSummary.put( 'filename', pInputFile.getAbsolutePath() )
    taskSummary.put( 'records', task.getReadCount() )
    taskSummary.put( 'updates', task.getUpdatedCount() )
    if ( pInputFile.exists() ) {
      pInputFile.renameTo( new File( Registry.processedFilesPath, pInputFile.name ) )
    }
    return taskSummary
  }

  Map<String, Object> loadFxRatesFile( File pInputFile ) {
    Map<String, Object> taskSummary = new HashMap<String, Object>()
    FxRatesImportTask task = new FxRatesImportTask()
    task.setInputFile( pInputFile )
    task.run()
    taskSummary.put( 'filename', pInputFile.getAbsolutePath() )
    taskSummary.put( 'records', task.getReadCount() )
    taskSummary.put( 'updates', task.getUpdatedCount() )
    if ( pInputFile.exists() ) {
      pInputFile.renameTo( new File( Registry.processedFilesPath, pInputFile.name ) )
    }
    return taskSummary
  }

  @Transactional
  void logSalesNotification( String pIdFactura ) {
    NotaVentaRepository orders = RepositoryFactory.orders
    NotaVenta order = orders.findOne( pIdFactura )
    if ( order != null ) {
      logger.debug( String.format( 'Notify Sales[Order:%s  Date:%s  Amount:%,.2f', order.id,
          CustomDateUtils.format( order.fechaHoraFactura ), order.ventaTotal ) )
      String strItemList = ''
      for ( DetalleNotaVenta det : order.detalles ) {
        strItemList += String.format( "%d|", det.idArticulo )
      }
      String strPaymentList = ''
      for ( Pago p : order.pagos ) {
        strPaymentList += StringUtils.trimToEmpty( p.idFPago ) + ',' + String.format( '%.2f', p.monto ) + '|'
      }
      AcuseRepository acuses = RepositoryFactory.acknowledgements
      Acuse acuse = new Acuse()
      acuse.idTipo = TAG_ACK_SALES
      try {
        acuse = acuses.saveAndFlush( acuse )
        logger.debug( String.format( 'Acuse: (%d) %s -> %s', acuse.id, acuse.idTipo, acuse.contenido ) )
      } catch ( Exception e ) {
        logger.error( e.getMessage() )
      }
      acuse.contenido = String.format( 'id_acuse=%s', URLEncoder.encode( String.format( '%d', acuse.id ), 'UTF-8' ) )
      acuse.contenido += String.format( '&id_suc=%s', URLEncoder.encode( String.format( '%d', order.idSucursal ), 'UTF-8' ) )
      acuse.contenido += String.format( '&no_soi=%s', URLEncoder.encode( order.id, 'UTF-8' ) )
      acuse.contenido += String.format( '&id_factura=%s', URLEncoder.encode( order.factura, 'UTF-8' ) )
      acuse.contenido += String.format( '&fecha=%s', URLEncoder.encode( CustomDateUtils.format( order.fechaHoraFactura, 'ddMMyyyy' ), 'UTF-8' ) )
      acuse.contenido += String.format( '&Importe=%s', URLEncoder.encode( String.format( '%.2f', order.ventaNeta ), 'UTF-8' ) )
      acuse.contenido += String.format( '&articulos=%s', URLEncoder.encode( strItemList, 'UTF-8' ) )
      acuse.contenido += String.format( '&pagos=%s', URLEncoder.encode( strPaymentList, 'UTF-8' ) )
      acuse.contenido += String.format( '&id_cliente=%s', URLEncoder.encode( String.format( '%d', order.idCliente ), 'UTF-8' ) )
      acuse.fechaCarga = new Date()
      try {
        acuse = acuses.saveAndFlush( acuse )
        logger.debug( String.format( 'Acuse: (%d) %s -> %s', acuse.id, acuse.idTipo, acuse.contenido ) )
      } catch ( Exception e ) {
        logger.error( e.getMessage() )
      }
    }
  }

  @Transactional
  void logAdjustmentNotification( Integer pIdMod ) {
    ModificacionRepository adjustments = RepositoryFactory.adjustments
    Modificacion adjustment = adjustments.findOne( pIdMod )
    if ( adjustment != null ) {
      logger.debug( String.format( 'Notify Adjustment[Adjust:%d %s  Order:%s  Date:%s', adjustment.id, adjustment.tipo,
          adjustment.idFactura, CustomDateUtils.format( adjustment.fecha ) ) )
      NotaVentaRepository orders = RepositoryFactory.orders
      NotaVenta order = orders.findOne( adjustment.idFactura )
      AcuseRepository acuses = RepositoryFactory.acknowledgements
      Acuse acuse = new Acuse()
      acuse.idTipo = TAG_ACK_ADJUST
      try {
        acuse = acuses.saveAndFlush( acuse )
        logger.debug( String.format( 'Acuse: (%d) %s -> %s', acuse.id, acuse.idTipo, acuse.contenido ) )
      } catch ( Exception e ) {
        logger.error( e.getMessage() )
      }
      acuse.contenido = String.format( 'id_acuse=%s', URLEncoder.encode( String.format( '%d', acuse.id ), 'UTF-8' ) )
      acuse.contenido += String.format( '&id_suc=%s', URLEncoder.encode( String.format( '%d', order.idSucursal ), 'UTF-8' ) )
      acuse.contenido += String.format( '&no_soi=%s', URLEncoder.encode( adjustment.idFactura, 'UTF-8' ) )
      acuse.contenido += String.format( '&id_factura=%s', URLEncoder.encode( order.factura, 'UTF-8' ) )
      acuse.contenido += String.format( '&fecha=%s', URLEncoder.encode( CustomDateUtils.format( adjustment.fecha, 'ddMMyyyy' ), 'UTF-8' ) )
      acuse.fechaCarga = new Date()
      try {
        acuse = acuses.saveAndFlush( acuse )
        logger.debug( String.format( 'Acuse: (%d) %s -> %s', acuse.id, acuse.idTipo, acuse.contenido ) )
      } catch ( Exception e ) {
        logger.error( e.getMessage() )
      }
    }
  }

  void startAsyncNotifyDispatcher( ) {
    AsynchronousNotificationDispatcher dispatcher = AsynchronousNotificationDispatcher.getInstance()
    Thread t = new Thread( dispatcher, dispatcher.name )
    t.setDaemon( true )
    t.start()
    logger.debug( String.format( 'Thread started: %s', t.name ) )
  }

  @Transactional
  void saveAcknowledgement( Acuse pAcknowledgement ) {
    RepositoryFactory.acknowledgements.saveAndFlush( pAcknowledgement )
  }

  void saveAcknowledgementTrans(TransInv pAcknowledgement) {
      RepositoryFactory.inventoryMaster.saveAndFlush( pAcknowledgement )
  }


  void logRemittanceNotification( String idTipoTrans, Integer folio, String codigo ) {
    TransInvRepository transactionRep = RepositoryFactory.inventoryMaster
    TipoTransInvRepository tipoTransactionRep = RepositoryFactory.trTypes
    QTipoTransInv tipoTrans = QTipoTransInv.tipoTransInv
    TipoTransInv tipoTransInv = tipoTransactionRep.findOne( idTipoTrans )
    QTransInv trans = QTransInv.transInv
    TransInv transInv = transactionRep.findOne(trans.idTipoTrans.eq(idTipoTrans).and(trans.folio.eq(tipoTransInv.ultimoFolio)))
    if ( transInv != null ) {
      AcuseRepository acuses = RepositoryFactory.acknowledgements
      Acuse acuse = new Acuse()
      acuse.idTipo = TAG_ACK_REMITTANCES
      try {
        acuse = acuses.saveAndFlush( acuse )
        logger.debug( String.format( 'Acuse: (%d) %s -> %s', acuse.id, acuse.idTipo, acuse.contenido ) )
      } catch ( Exception e ) {
        logger.error( e.getMessage() )
      }
      String referencia = transInv.referencia.substring(0,6)
      String tipo = transInv.referencia.substring( transInv.referencia.length()-1 )
      println referencia
      //acuse.contenido = String.format( 'ImporteVal=%s|', URLEncoder.encode( String.format( '%.2f', order.ventaNeta ), 'UTF-8' ) )
      String sistema = ''
      TransInvDetalleRepository transInvDetalleRepository = RepositoryFactory.inventoryDetail
      List<TransInvDetalle> detalles = transInvDetalleRepository.findByIdTipoTransAndFolio(StringUtils.trimToEmpty(transInv.idTipoTrans), transInv.folio) as List<TransInvDetalle>
      for( TransInvDetalle det : detalles ){
        ArticuloRepository articuloRepository = RepositoryFactory.partMaster
        Articulo articulo = articuloRepository.findOne(det.sku)
        if( articulo != null ){
          sistema = StringUtils.trimToEmpty(articulo.idGenerico)
        }
      }
      String letra = sistema.equalsIgnoreCase("A") ? "I" : "RAC"
      acuse.contenido = String.format( 'sistemaVal=%s|', tipo.trim() )
      acuse.contenido += String.format( 'id_sucVal=%s|', transInv.sucursal.toString().trim() )
      acuse.contenido += String.format( 'horaVal=%s|', CustomDateUtils.format(transInv.fechaMod, 'HH:mm') )
      acuse.contenido += String.format( 'doctoVal=%s|', String.format( '%s', letra+StringUtils.trimToEmpty(transInv.referencia).substring(0,6)) )
      acuse.contenido += String.format( 'id_acuseVal=%s|', String.format( '%d', acuse.id ) )
      acuse.contenido += String.format( 'transaVal=%s|', String.format( '%s', StringUtils.trimToEmpty(transInv.referencia).substring(0,6)) )
      try {
        acuse = acuses.saveAndFlush( acuse )
        logger.debug( String.format( 'Acuse: (%d) %s -> %s', acuse.id, acuse.idTipo, acuse.contenido ) )
      } catch ( Exception e ) {
        logger.error( e.getMessage() )
      }

      Integer idSuc = Registry.currentSite
      ParametroRepository repoParam = RepositoryFactory.registry
      String rutaPorEnviar = Registry.archivePath.trim()
      File file = new File( "${rutaPorEnviar}/4.${idSuc}.REM.${transInv.referencia}.ACU" )
      PrintStream strOut = new PrintStream( file )
      StringBuffer sb = new StringBuffer()
      sb.append("${idSuc}|REM|${StringUtils.trimToEmpty(transInv.referencia).substring(0,6)}|")
      sb.append( "\n" )
      sb.append("${transInv.fecha.format('dd/MM/yyyy')}|${new Date().format('HH:mm')}|${letra}${StringUtils.trimToEmpty(transInv.referencia).substring(0,6)}|${sistema.trim()}|")
      strOut.println sb.toString()
      strOut.close()
      logger.debug(file.absolutePath)
    }
  }



  Remesas updateRemesa( String idTipoTrans ){
    Remesas remesa = new Remesas()
    TransInvRepository transactionRep = RepositoryFactory.inventoryMaster
    TipoTransInvRepository tipoTransactionRep = RepositoryFactory.trTypes
    QTipoTransInv tipoTrans = QTipoTransInv.tipoTransInv
    TipoTransInv tipoTransInv = tipoTransactionRep.findOne( idTipoTrans )
    QTransInv trans = QTransInv.transInv
    TransInv transInv = transactionRep.findOne(trans.idTipoTrans.eq(idTipoTrans).and(trans.folio.eq(tipoTransInv.ultimoFolio)))
    if ( transInv != null ) {
      RemesasRepository repo = RepositoryFactory.remittanceRepository
      QRemesas rem = QRemesas.remesas
      remesa = repo.findOne( rem.clave.eq(transInv.referencia.trim()) )
      if(remesa != null){
        remesa.estado = TAG_ESTADO_REM_CARGADA
        remesa.fechaCarga = new Date()
        remesa = repo.save( remesa )
        repo.flush()
      } else {
        String sistema = ''
        TransInvDetalleRepository transInvDetalleRepository = RepositoryFactory.inventoryDetail
        List<TransInvDetalle> detalles = transInvDetalleRepository.findByIdTipoTransAndFolio(StringUtils.trimToEmpty(transInv.idTipoTrans), transInv.folio) as List<TransInvDetalle>
        for( TransInvDetalle det : detalles ){
          ArticuloRepository articuloRepository = RepositoryFactory.partMaster
          Articulo articulo = articuloRepository.findOne(det.sku)
          if( articulo != null ){
            sistema = StringUtils.trimToEmpty(articulo.idGenerico)
          }
        }
        String letra = sistema.equalsIgnoreCase("A") ? "I" : "RAC"
        remesa = new Remesas()
        remesa.idTipoDocto = "RN"
        remesa.idDocto = letra+StringUtils.trimToEmpty(transInv.referencia).substring(0,6)
        remesa.docto = StringUtils.trimToEmpty(transInv.referencia).substring(0,6)
        remesa.clave = StringUtils.trimToEmpty(transInv.referencia)
        remesa.letra = letra
        remesa.archivo = "4."+StringUtils.trimToEmpty(Registry.currentSite.toString())+".REM."+StringUtils.trimToEmpty(transInv.referencia)
        remesa.articulos = detalles.size()
        remesa.sistema = sistema
        remesa.fechaRecibido = new Date()
        remesa.estado = TAG_ESTADO_REM_CARGADA
        remesa.fechaCarga = new Date()
        remesa = repo.save( remesa )
        repo.flush()
      }
    }
    return remesa
  }


}
