package mx.lux.pos.model;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table( name = "remesas", schema = "public" )
public class Remesas implements Serializable {


    private static final long serialVersionUID = -2467918185056256140L;


    @Id
    @GeneratedValue( strategy = GenerationType.AUTO, generator = "remesas_id_remesa_seq" )
    @SequenceGenerator( name = "remesas_id_remesa_seq", sequenceName = "remesas_id_remesa_seq" )
    @Column( name = "id_remesa" )
    private Integer idRemesa;

    @Column( name = "id_tipo_docto" )
    private String idTipoDocto;

    @Column( name = "id_docto" )
    private String idDocto;

    @Column( name = "docto" )
    private String docto;

    @Column( name = "clave" )
    private String clave;

    @Column( name = "letra" )
    private String letra;

    @Column( name = "archivo" )
    private String archivo;

    @Column( name = "articulos" )
    private Integer articulos;

    @Column( name = "estado" )
    private String estado;

    @Column( name = "sistema" )
    private String sistema;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "fecha_mod" )
    private Date fechaMod;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "fecha_recibido" )
    private Date fechaRecibido;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "fecha_carga" )
    private Date fechaCarga;



    public Integer getIdRemesa() {
        return idRemesa;
    }

    public void setIdRemesa(Integer idRemesa) {
        this.idRemesa = idRemesa;
    }

    public String getIdTipoDocto() {
        return idTipoDocto;
    }

    public void setIdTipoDocto(String idTipoDocto) {
        this.idTipoDocto = idTipoDocto;
    }

    public String getIdDocto() {
        return idDocto;
    }

    public void setIdDocto(String idDocto) {
        this.idDocto = idDocto;
    }

    public String getDocto() {
        return docto;
    }

    public void setDocto(String docto) {
        this.docto = docto;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getLetra() {
        return letra;
    }

    public void setLetra(String letra) {
        this.letra = letra;
    }

    public String getArchivo() {
        return archivo;
    }

    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }

    public Integer getArticulos() {
        return articulos;
    }

    public void setArticulos(Integer articulos) {
        this.articulos = articulos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getSistema() {
        return sistema;
    }

    public void setSistema(String sistema) {
        this.sistema = sistema;
    }

    public Date getFechaMod() {
        return fechaMod;
    }

    public void setFechaMod(Date fechaMod) {
        this.fechaMod = fechaMod;
    }

    public Date getFechaRecibido() {
        return fechaRecibido;
    }

    public void setFechaRecibido(Date fechaRecibido) {
        this.fechaRecibido = fechaRecibido;
    }

    public Date getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(Date fechaCarga) {
        this.fechaCarga = fechaCarga;
    }
}
