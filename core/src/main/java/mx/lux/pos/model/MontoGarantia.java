package mx.lux.pos.model;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table( name = "monto_garantia", schema = "public" )
public class MontoGarantia implements Serializable {

    private static final long serialVersionUID = 2682597108089643661L;


    @Id
    @Column( name = "id" )
    private Integer id;

    @Type( type = "mx.lux.pos.model.MoneyAdapter" )
    @Column( name = "monto_garantia" )
    private BigDecimal montoGarantia;

    @Type( type = "mx.lux.pos.model.MoneyAdapter" )
    @Column( name = "monto_minimo" )
    private BigDecimal montoMinimo;

    @Type( type = "mx.lux.pos.model.MoneyAdapter" )
    @Column( name = "monto_maximo" )
    private BigDecimal montoMaximo;



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getMontoGarantia() {
        return montoGarantia;
    }

    public void setMontoGarantia(BigDecimal montoGarantia) {
        this.montoGarantia = montoGarantia;
    }

    public BigDecimal getMontoMinimo() {
        return montoMinimo;
    }

    public void setMontoMinimo(BigDecimal montoMinimo) {
        this.montoMinimo = montoMinimo;
    }

    public BigDecimal getMontoMaximo() {
        return montoMaximo;
    }

    public void setMontoMaximo(BigDecimal montoMaximo) {
        this.montoMaximo = montoMaximo;
    }
}
