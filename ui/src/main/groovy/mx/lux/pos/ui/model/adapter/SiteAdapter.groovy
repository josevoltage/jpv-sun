package mx.lux.pos.ui.model.adapter

import mx.lux.pos.model.Sucursal
import org.apache.commons.lang.StringUtils

class SiteAdapter extends StringAdapter<Sucursal> {
  
  public String getText( Sucursal pSucursal ) {
    String suc = ""
    if( pSucursal == null ){
      suc = ""
    } else if( pSucursal.id == null ){
      if( StringUtils.trimToEmpty(pSucursal.nombre).length() > 0 ){
        suc = pSucursal.nombre
      } else {
        suc = ""
      }
    } else {
      suc = String.format( "[%d] %s", pSucursal.id, pSucursal.nombre)
    }
    return suc;
  }
  
}
