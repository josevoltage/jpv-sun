package mx.lux.pos.service.business

import mx.lux.pos.model.Articulo
import mx.lux.pos.model.ArticuloSunglass
import mx.lux.pos.model.Generico
import mx.lux.pos.model.Sucursal
import mx.lux.pos.repository.impl.RepositoryFactory
import mx.lux.pos.service.impl.ServiceFactory
import mx.lux.pos.service.io.PartFileSunglass
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ArticuloSunglassImportTask {

  private static final String MSG_PARSE_SKU_NBR = "ERROR! No se puede identificar el SKU#"
  private static final String MSG_GENRE_UNDEFINED = "ERROR! Generico no registrado"

  private String filename
  private PartFileSunglass file
  private PrintStream errorFile
  private Integer nRead, nUpdated, nError
  private Integer site
  private Logger logger

  // Internal Methods
  protected debug( String pMessage ) {
    this.getLogger().debug( pMessage )
  }

  protected info( String pMessage ) {
    this.getLogger().info( pMessage )
  }

  protected Articulo findOrCreate( Integer pSku ) {
    Articulo part = RepositoryFactory.partMaster.findOne( pSku )
    if ( part == null ) {
      part = new Articulo()
      part.id = pSku
      part.idSucursal = this.getSite( )
    }
    return part
  }

  protected PrintStream getErrorFile() {
    if ( this.errorFile == null ) {
      this.errorFile = new PrintStream( this.filename + ".err" )
    }
    return this.errorFile
  }

  protected PartFileSunglass getFile( ) {
    if ( this.hasFilename() && ( this.file == null ) ) {
      this.file = new PartFileSunglass( this.filename )
    }
    return this.file
  }

  protected String getFilename( ) {
    return this.filename
  }

  protected getLogger( ) {
    if ( this.logger == null ) {
      this.logger = LoggerFactory.getLogger( this.getClass() )
    }
    return this.logger
  }

  protected Integer getSite( ) {
    if ( this.site == null ) {
      this.site = ServiceFactory.sites.obtenSucursalActual().id
    }
    return this.site
  }

  protected Boolean hasFilename( ) {
    return ( this.filename != null )
  }

  protected Boolean isValid( ArticuloSunglass pSunglassPart ) {
    Boolean valid = true

    if ( pSunglassPart.sku == null ) {
      this.reportError( pSunglassPart, MSG_PARSE_SKU_NBR )
      valid = false
    }

    if ( pSunglassPart.genre != null ) {
      Generico genre = null
      if ( pSunglassPart.genre != null ) {
        genre = RepositoryFactory.genres.findOne( pSunglassPart.genre )
      }
      if ( genre == null ) {
        this.reportError( pSunglassPart, MSG_GENRE_UNDEFINED )
        valid = false
      }
    }

    if ( !valid ) {
      this.nError++
    }
    return valid
  }

  protected void reportError( ArticuloSunglass pSunglassPart, String pErrorMessage ) {
    this.info( String.format( "[%,d] %s", this.nRead, pErrorMessage ) )
    this.getErrorFile().println( String.format( "[%,d] %s", this.nRead, pErrorMessage ) )
    this.info( String.format( "[%,d] %s", this.nRead, pSunglassPart.toString() ) )
    this.getErrorFile().println( String.format( "[%,d] %s", this.nRead, pSunglassPart.toString() ) )
  }

  protected void updateArticulo( Articulo pPart, ArticuloSunglass pSunglass ) {
    if ( pSunglass.partNbr != null )
      pPart.articulo = pSunglass.partNbr
    if ( pSunglass.colorCode != null )
      pPart.codigoColor = pSunglass.colorCode
    if ( pSunglass.description != null )
      pPart.descripcion = pSunglass.description
    if ( pSunglass.colorDesc != null )
      pPart.descripcionColor = pSunglass.colorDesc

    if ( pSunglass.genre != null )
      pPart.idGenerico = pSunglass.genre
    if ( pSunglass.revDate != null )
      pPart.fechaMod = pSunglass.revDate
    if ( pSunglass.revUserid != null )
      pPart.idMod = pSunglass.revUserid
    if ( pSunglass.price != null )
      pPart.precio = BigDecimal.valueOf( pSunglass.price )
    if ( pSunglass.redPrice != null )
      pPart.precioO = BigDecimal.valueOf( pSunglass.redPrice )

    if ( pSunglass.supplier != null ) {
      pPart.proveedor = pSunglass.supplier
    }

    if ( pSunglass.type != null ) {
      pPart.tipo = pSunglass.type
    }
    if ( pSunglass.subtype != null ) {
      pPart.subtipo = pSunglass.subtype
    }
    if ( pSunglass.brand != null ) {
      pPart.marca = pSunglass.brand
    }

    ServiceFactory.partMaster.registrarArticulo( pPart )
  }

  // Public Methods
  Integer getErrorCount( ) {
    return this.nError
  }

  Integer getUpdatedCount( ) {
    return this.nUpdated
  }

  Integer getRecordCount( ) {
    return this.nRead
  }


  void run( ) {
    this.debug( String.format( "[STARTED] Import Articulo Sunglass (%s)", this.filename ) )
    this.nError = 0
    this.nUpdated = 0
    this.nRead = 0
    if ( this.getFile() != null ) {
      ArticuloSunglass partSunglass = this.getFile().read()
      while ( partSunglass != null ) {
        this.nRead++
        if ( isValid( partSunglass ) ) {
          Articulo part = findOrCreate( partSunglass.sku )
          updateArticulo( part, partSunglass )
          nUpdated++
        }
        partSunglass = this.file.read()
      }
      this.getFile().close()
    }
    if ( this.nUpdated > 0 ) {
      RepositoryFactory.partMaster.flush( )
    }
    if ( this.errorFile != null ) {
      this.getErrorFile().close()
    }
    this.debug( String.format( "[FINISHED] Import Articulo Sunglass  Updated:%,d/%,d  Error:%,d",
        this.nUpdated, this.nRead, this.nError
    ) )
  }

  void setFilename( String pFilename ) {
    this.file = null
    this.filename = StringUtils.trimToNull( pFilename )
    if ( ( this.filename != null ) && ( new File( this.filename ).exists() ) ) {
      this.filename = pFilename
    } else {
      this.filename = null
      throw new FileNotFoundException( pFilename )
    }
  }

}
