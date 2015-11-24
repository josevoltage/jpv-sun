package mx.lux.pos.model;

import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Skus {

    public Skus( String marca ){
      this.setMarca( marca );
      lstSku = new ArrayList<Sku>();
      sku = "";
    }

    private String marca;
    private String sku;
    private List<Sku> lstSku;

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public List<Sku> getLstSku() {
        return lstSku;
    }

    public void setLstSku(List<Sku> lstSku) {
        this.lstSku = lstSku;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void acumulaSkusPorMarca(Integer sku) {
      //Sku skuTmp = new Sku( sku );
      //lstSku.add(skuTmp);
      this.sku = this.sku+", "+ StringUtils.trimToEmpty(sku.toString());
    }


}
