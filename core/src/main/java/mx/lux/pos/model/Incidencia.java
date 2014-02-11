package mx.lux.pos.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table( name = "incidencia", schema = "public" )
public class Incidencia implements Serializable {

    private static final long serialVersionUID = 5661261405849277927L;

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO, generator = "incidencia_folio_soi_seq" )
    @SequenceGenerator( name = "incidencia_folio_soi_seq", sequenceName = "incidencia_folio_soi_seq" )
    @Column( name = "folio_soi" )
    private Integer folioSoi;

    @Column( name = "folio" )
    private Integer folio;

    @Column( name = "id_empresa", length = 18 )
    private String idEmpresa;

    @Column( name = "id_empleado", length = 18 )
    private String idEmpleado;

    @Column( name = "nombre", length = 40 )
    private String nombre;

    @Column( name = "id_grupo", length = 8 )
    private String idGrupo;

    @Column( name = "id_criterio", length = 8 )
    private String idCriterio;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "fecha ", nullable = false )
    private Date fecha ;

    @Column( name = "id_empresa_cap", length = 8 )
    private String idEmpresaCap;

    @Column( name = "id_empleado_cap", length = 8 )
    private String idEmpleadoCap;

    @Column( name = "nombre_cap", length = 40 )
    private String nombreCap;

    @Column( name = "valor", length = 8 )
    private String valor;

    @Column( name = "observacion", length = 40 )
    private String observacion;

    @Column( name = "descripcion", length = 40 )
    private String descripcion;



    public Integer getFolioSoi() {
        return folioSoi;
    }

    public void setFolioSoi(Integer folioSoi) {
        this.folioSoi = folioSoi;
    }

    public Integer getFolio() {
        return folio;
    }

    public void setFolio(Integer folio) {
        this.folio = folio;
    }

    public String getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(String idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(String idGrupo) {
        this.idGrupo = idGrupo;
    }

    public String getIdCriterio() {
        return idCriterio;
    }

    public void setIdCriterio(String idCriterio) {
        this.idCriterio = idCriterio;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getIdEmpresaCap() {
        return idEmpresaCap;
    }

    public void setIdEmpresaCap(String idEmpresaCap) {
        this.idEmpresaCap = idEmpresaCap;
    }

    public String getIdEmpleadoCap() {
        return idEmpleadoCap;
    }

    public void setIdEmpleadoCap(String idEmpleadoCap) {
        this.idEmpleadoCap = idEmpleadoCap;
    }

    public String getNombreCap() {
        return nombreCap;
    }

    public void setNombreCap(String nombreCap) {
        this.nombreCap = nombreCap;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
