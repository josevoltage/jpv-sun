package mx.lux.pos.service.io

import mx.lux.pos.repository.SucursalRepository
import mx.lux.pos.repository.impl.RepositoryFactory
import mx.lux.pos.service.business.Registry
import mx.lux.pos.service.business.ResourceManager
import mx.lux.pos.service.impl.ServiceFactory
import mx.lux.pos.util.CustomDateUtils
import mx.lux.pos.util.StringList
import org.apache.commons.lang3.StringUtils
import mx.lux.pos.model.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class InvTrFile {

  private static enum DetFld {
    Sku, Article, Color, ModSend, Qty, Remarks, MovType
  }
  private static enum HdrFld {
    TrNbr, LineNum
  }
  private static final String DELIMITER = "|"
  private static final String FMT_TR_DATE = "dd/MM/yyyy"

  private Logger logger = LoggerFactory.getLogger( this.getClass() )
  private String delimiter

  // Internal Methods
  protected Boolean canFindSku( Integer pSku ) {
    Articulo part = ServiceFactory.partMaster.obtenerArticulo( pSku, false )
    return ( part != null )
  }

  protected Boolean canFindTrType( String pTrType ) {
    TipoTransInv trType = ServiceFactory.inventory.obtenerTipoTransaccion( pTrType )
    return ( trType != null )
  }

  String format( TransInv pInvTr ) {
    StringBuffer sb = new StringBuffer()
    sb.append( this.formatHeader( pInvTr ) )
    int iLine = 0
    for ( TransInvDetalle det : pInvTr.trDet ) {
      iLine++
      sb.append( "\n" )
      sb.append( this.formatDetail( pInvTr, iLine, det ) )
    }
    return sb.toString()
  }

  protected String formatDetail( TransInv pInvTr, Integer pLineNum, TransInvDetalle pTrDet ) {
    StringList det = new StringList()
    Articulo articulo = RepositoryFactory.partMaster.findOne( pTrDet.sku )
    for ( DetFld fld : DetFld.values() ) {
      switch ( fld ) {
        case DetFld.Article: det.add( articulo != null ? StringUtils.trimToEmpty(articulo.articulo) : "" ); break
        case DetFld.Color: det.add( articulo != null ? StringUtils.trimToEmpty(articulo.codigoColor) : "" ); break
        case DetFld.ModSend: det.add( "ENTERO" ); break
        case DetFld.Qty: det.addInteger( pTrDet.cantidad ); break
        case DetFld.Sku: det.addInteger( pTrDet.sku ); break
        case DetFld.MovType: det.add( pTrDet.tipoMov ); break
        case DetFld.Remarks: det.add( StringUtils.trimToEmpty(pInvTr.observaciones) ); break
      }
    }
    det.add( "" )
    return det.toString( this.getDelimiter() )
  }

  protected String formatHeader( TransInv pInvTr ) {
    StringList hdr = new StringList()
    for ( HdrFld fld : HdrFld.values() ) {
      switch ( fld ) {
        case HdrFld.TrNbr: hdr.addInteger( pInvTr.folio, "%06d" ); break
        case HdrFld.LineNum: hdr.addInteger( pInvTr.trDet.size() ); break
      }
    }
    hdr.add( "" )
    return hdr.toString( this.getDelimiter() )
  }

  protected String getDelimiter( ) {
    if ( this.delimiter == null ) {
      this.delimiter = DELIMITER
    }
    return this.delimiter
  }

  protected File getInvTrFile( TransInv pInvTr ) {
    SucursalRepository sucRep = RepositoryFactory.siteRepository
    Sucursal sucursal = sucRep.findOne( Registry.currentSite )
    String centroCostos = sucursal != null ? StringUtils.trimToEmpty(sucursal.centroCostos) : StringUtils.trimToEmpty(Registry.currentSite.toString())
    String filename = String.format( "2.%s.%s.DA.%06d", centroCostos, CustomDateUtils.format( pInvTr.fecha, "dd-MM-yyyy" ),
            pInvTr.folio
         )
    String absolutePath = String.format( "%s%s%s", this.location, File.separator, filename )
    return new File( absolutePath )
  }

  protected String getLocation( ) {
    return ResourceManager.getLocation( TipoParametro.RUTA_POR_ENVIAR ).absolutePath
  }

  TransInv parse( List<String> pInputLines ) {
    TransInv tr = null
    int iLine = 0
    for ( String line : pInputLines ) {
      iLine++
      if ( iLine == 1 ) {
        tr = parseHeader( line )
      } else {
        if ( tr != null ) {
          TransInvDetalle trDet = parseDetail( line )
          if ( trDet != null ) {
            tr.add( trDet )
          }
        }
      }
    }
    return tr
  }

  TransInvDetalle parseDetail( String pDetailLine ) {
    TransInvDetalle parsed = null
    StringList list = new StringList( pDetailLine, this.getDelimiter() )
    if ( list.size >= DetFld.values().size() && canFindSku( list.asInteger( DetFld.Sku.ordinal() ) ) ) {
      parsed = new TransInvDetalle()
      parsed.idTipoTrans = list.entry( DetFld.TrType.ordinal() )
      parsed.folio = list.asInteger( DetFld.TrNbr.ordinal() )
      parsed.linea = list.asInteger( DetFld.LineNum.ordinal() )
      parsed.sku = list.asInteger( DetFld.Sku.ordinal() )
      parsed.tipoMov = list.entry( DetFld.MovType.ordinal() )
      parsed.cantidad = list.asInteger( DetFld.Qty.ordinal() )
    }
    return parsed
  }

  TransInv parseHeader( String pHeaderLine ) {
    TransInv parsed = null
    StringList list = new StringList( pHeaderLine, this.getDelimiter() )
    if ( list.size >= HdrFld.values().size() && canFindTrType( list.entry( HdrFld.TrType.ordinal() ) ) ) {
      parsed = new TransInv()
      parsed.idTipoTrans = list.entry( HdrFld.TrType.ordinal() )
      parsed.folio = list.asInteger( HdrFld.TrNbr.ordinal() )
      parsed.sucursal = list.asInteger( HdrFld.Site.ordinal() )
      parsed.fecha = list.asDate( HdrFld.TrDate.ordinal(), FMT_TR_DATE )
      parsed.observaciones = list.entry( HdrFld.Remarks.ordinal() )
      parsed.referencia = list.entry( HdrFld.Ref.ordinal() )
      if ( StringUtils.trimToNull( list.entry( HdrFld.SiteTo.ordinal() ) ) != null ) {
        parsed.sucursalDestino = list.asInteger( HdrFld.SiteTo.ordinal() )
      }
    }
    return parsed
  }

  protected void setDelimiter( String pDelimiter ) {
    this.delimiter = pDelimiter
  }

  // Public Methods
  Boolean identify( String pHeaderLine ) {
    return ( this.parseHeader( pHeaderLine ) != null )
  }

  InvTrFile read( String pFilename ) {
    InvTrFile tr = null
    return tr
  }

  String write( TransInv pInvTr ) {
    File file = this.getInvTrFile( pInvTr )
    PrintStream strOut = new PrintStream( file )
    strOut.println this.format( pInvTr )
    strOut.close()
    logger.debug( String.format( 'Transaction written to: %s', file.absolutePath ) )
    return file.absolutePath
  }
}
