package mx.lux.pos.model;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table( name = "remesas", schema = "public" )
public class Remesas implements Serializable {


    private static final long serialVersionUID = -7198826207467733267L;

    @Id
    @GeneratedValue( strategy = GenerationType.SEQUENCE, generator = "remesas_id_remesa_seq" )
    @SequenceGenerator( schema = "public", sequenceName = "remesas_id_remesa_seq", name = "remesas_id_remesa_seq", allocationSize = 1 )
    @Column( name = "id_remesa" )
    private Integer id;

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
    private Date fecha_mod;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "fecha_recibido" )
    private Date fecha_recibido;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "fecha_carga" )
    private Date fecha_carga;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Date getFecha_mod() {
        return fecha_mod;
    }

    public void setFecha_mod(Date fecha_mod) {
        this.fecha_mod = fecha_mod;
    }

    public Date getFecha_recibido() {
        return fecha_recibido;
    }

    public void setFecha_recibido(Date fecha_recibido) {
        this.fecha_recibido = fecha_recibido;
    }

    public Date getFecha_carga() {
        return fecha_carga;
    }

    public void setFecha_carga(Date fecha_carga) {
        this.fecha_carga = fecha_carga;
    }
}
