package mx.lux.pos.model;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table( name = "mensaje_ticket", schema = "public" )
public class MensajeTicket implements Serializable {

    private static final long serialVersionUID = 3993958734222687983L;

    @Id
    @Column( name = "folio" )
    private Integer folio;

    @Column( name = "descripcion" )
    private String descripcion;

    @Column( name = "fecha_inicio" )
    private Date fechaInicio;

    @Column( name = "fecha_final" )
    private Date fechaFinal;

    @Column( name = "id_linea" )
    private String idLinea;

    @Column( name = "lista_articulo" )
    private String listaArticulo;

    @Column( name = "mensaje" )
    private String mensaje;



    public Integer getFolio() {
        return folio;
    }

    public void setFolio(Integer folio) {
        this.folio = folio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(Date fechaFinal) {
        this.fechaFinal = fechaFinal;
    }

    public String getIdLinea() {
        return idLinea;
    }

    public void setIdLinea(String idLinea) {
        this.idLinea = idLinea;
    }

    public String getListaArticulo() {
        return listaArticulo;
    }

    public void setListaArticulo(String listaArticulo) {
        this.listaArticulo = listaArticulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
