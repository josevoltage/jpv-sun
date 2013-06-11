package mx.lux.pos.service.io

import mx.lux.pos.model.TransInv
import mx.lux.pos.model.TransInvDetalle
import mx.lux.pos.service.business.Registry
import mx.lux.pos.util.CustomDateUtils
import mx.lux.pos.util.StringList
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ZInFile {

  private static final String DELIMITER = "|"
  private static final String DELIMITER_2 = "!"
  private static final String FMT_TR_DATE = "dd/MM/yyyy"
  private static final String FILENAME = "2.%d.%s.IN"

  private Logger logger = LoggerFactory.getLogger( this.getClass() )
  private String filename
  private Date fileDate
  private List<TransInv> trInvList
  private Integer nTrans

  ZInFile( Date pDate ) {
    this.fileDate = pDate
    filename = String.format( FILENAME, Registry.getCurrentSite(), CustomDateUtils.format( pDate, "dd-MM-yyyy" ) )
  }

  // Private Methods
  protected String formatTrans( TransInv pTrMstr, TransInvDetalle pTrDet ) {
    String idTipoTrans = pTrMstr.idTipoTrans
    if( pTrMstr.idTipoTrans.contains('SALIDA_TIENDA') ){
        idTipoTrans = 'SALIDA'
    } else if( pTrMstr.idTipoTrans.contains('ENTRADA_TIENDA') ){
        idTipoTrans = 'ENTRADA'
    }
    StringList list = new StringList()
    list.addInteger( nTrans++ )
    list.add( idTipoTrans )
    list.addInteger( pTrMstr.folio )
    list.addDate( pTrMstr.fecha )
    list.add( pTrMstr.referencia.replace( DELIMITER, DELIMITER_2 ) )
    list.add( pTrMstr.observaciones )
    list.add( pTrMstr.idEmpleado )
    if ( pTrMstr.sucursalDestino != null ) {
      list.addInteger( pTrMstr.sucursalDestino, "%04d" )
    } else {
      list.add( "" )
    }
    list.addInteger( pTrDet.linea )
    list.addInteger( pTrDet.sku )
    list.add( pTrDet.tipoMov )
    list.addInteger( pTrDet.cantidad )
    list.add( "" )
    return list.toString( DELIMITER )
  }

  protected String formatHeader( ) {
    StringList list = new StringList()
    list.addInteger( Registry.getCurrentSite(), "%04d" )
    list.addDate( this.fileDate, FMT_TR_DATE )
    nTrans = 0
    for ( TransInv tr : this.trInvList ) {
      for ( TransInvDetalle det : tr.trDet ) {
        nTrans++
      }
    }
    list.addInteger( nTrans )
    nTrans = 0
    list.add( "" )
    return list.toString( DELIMITER )
  }

  // Public Methods
  void setInvTrList( List<TransInv> pList ) {
    trInvList = pList
  }

  void write( ) {
    if ( this.trInvList.size() > 0 ) {
      File file = new File( Registry.getDailyClosePath() + File.separator + filename )
      if ( file.exists() ) {
        file.delete()
      }
      PrintStream str = null
      try {
        str = new PrintStream( file )
        str.println( this.formatHeader() )
        for ( TransInv tr : this.trInvList ) {
          for ( TransInvDetalle det : tr.trDet ) {
            str.println( this.formatTrans( tr, det ) )
          }
        }
      } catch ( Exception e ) {
        this.logger.error( e.getMessage(), e )
      } finally {
        if ( str != null ) {
          str.close()
        }
      }
    }
  }
}
