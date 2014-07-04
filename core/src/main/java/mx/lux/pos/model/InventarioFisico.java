package mx.lux.pos.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class InventarioFisico {

    private Integer idArticulo ;
    private Integer cantidadFisico;

    public Integer getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(Integer idArticulo) {
        this.idArticulo = idArticulo;
    }

    public Integer getCantidadFisico() {
        return cantidadFisico;
    }

    public void setCantidadFisico(Integer cantidadFisico) {
        this.cantidadFisico = cantidadFisico;
    }
}
