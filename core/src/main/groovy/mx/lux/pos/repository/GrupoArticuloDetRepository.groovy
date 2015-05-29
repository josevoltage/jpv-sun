package mx.lux.pos.repository

import mx.lux.pos.model.GrupoArticuloDet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.querydsl.QueryDslPredicateExecutor
import org.springframework.transaction.annotation.Transactional

interface GrupoArticuloDetRepository
extends JpaRepository<GrupoArticuloDet, GrupoArticuloDet>,
    QueryDslPredicateExecutor<GrupoArticuloDet> {

  List<GrupoArticuloDet> findByArticulo( String pArticulo )

  List<GrupoArticuloDet> findByIdGrupo( Integer pIdGrupo )

  @Modifying
  @Transactional
  @Query( value = "DELETE FROM grupo_articulo_det WHERE id_grupo = ?1", nativeQuery = true )
  void deleteByIdGrupo( Integer pIdGrupo )

}
