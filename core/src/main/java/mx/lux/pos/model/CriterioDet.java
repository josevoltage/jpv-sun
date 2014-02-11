package mx.lux.pos.model;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table( name = "criterio_det", schema = "public" )
public class CriterioDet implements Serializable {


    private static final long serialVersionUID = 213753303785706769L;

    @Id
    @Column( name = "id" )
    private Integer id;

    @Column( name = "id_grupo" )
    private Integer idGrupo;

    @Column( name = "id_criterio" )
    private String idCriterio;

    @Column( name = "descripcion" )
    private String descripcion;



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(Integer idGrupo) {
        this.idGrupo = idGrupo;
    }

    public String getIdCriterio() {
        return idCriterio;
    }

    public void setIdCriterio(String idCriterio) {
        this.idCriterio = idCriterio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
