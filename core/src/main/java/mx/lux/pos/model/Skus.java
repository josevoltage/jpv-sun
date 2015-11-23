package mx.lux.pos.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Skus {

    public Skus( String marca ){
      this.setMarca( marca );
      lstSku = new ArrayList<Sku>();
    }

    private String marca;
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

    public void acumulaSkusPorMarca(Integer sku) {
      Sku skuTmp = new Sku( sku );
      lstSku.add(skuTmp);
    }


}
