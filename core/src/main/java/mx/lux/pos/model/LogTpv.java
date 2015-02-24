package mx.lux.pos.model;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table( name = "log_tpv", schema = "public" )
public class LogTpv implements Serializable {


    private static final long serialVersionUID = 5523889495324782356L;

    @Id
    @Column( name = "id" )
    private Integer id;

    @Column( name = "id_factura" )
    private String idFactura;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "fecha" )
    private Date fecha;

    @Column( name = "pago_seleccionado" )
    private String pagoSeleccionado;

    @Column( name = "pago_recibido" )
    private String pagoRecibido;

    @Column( name = "cadena" )
    private String cadena;

    @Column( name = "tarjeta" )
    private String tarjeta;

    @Column( name = "autorizacion" )
    private String autorizacion;

    @Type( type = "mx.lux.pos.model.MoneyAdapter" )
    @Column( name = "monto" )
    private BigDecimal monto;

    @Column( name = "tipo" )
    private String tipo;

    @Column( name = "empleado" )
    private String empleado;

    @Column( name = "plan" )
    private String plan;



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(String idFactura) {
        this.idFactura = idFactura;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getPagoSeleccionado() {
        return pagoSeleccionado;
    }

    public void setPagoSeleccionado(String pagoSeleccionado) {
        this.pagoSeleccionado = pagoSeleccionado;
    }

    public String getPagoRecibido() {
        return pagoRecibido;
    }

    public void setPagoRecibido(String pagoRecibido) {
        this.pagoRecibido = pagoRecibido;
    }

    public String getCadena() {
        return cadena;
    }

    public void setCadena(String cadena) {
        this.cadena = cadena;
    }

    public String getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(String tarjeta) {
        this.tarjeta = tarjeta;
    }

    public String getAutorizacion() {
        return autorizacion;
    }

    public void setAutorizacion(String autorizacion) {
        this.autorizacion = autorizacion;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEmpleado() {
        return empleado;
    }

    public void setEmpleado(String empleado) {
        this.empleado = empleado;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }
}
