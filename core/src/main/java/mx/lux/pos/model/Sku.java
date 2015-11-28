package mx.lux.pos.model;

import java.util.ArrayList;
import java.util.List;

public class Sku {

   public Sku( Integer skuTmp ){
     this.setSku( skuTmp );
   }

    private Integer sku;



    public Integer getSku() {
        return sku;
    }

    public void setSku(Integer sku) {
        this.sku = sku;
    }
}
