package mx.lux.pos.repository

import mx.lux.pos.model.Acuse
import mx.lux.pos.model.Diferencia
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.querydsl.QueryDslPredicateExecutor
import org.springframework.transaction.annotation.Transactional

interface DiferenciaRepository extends JpaRepository<Diferencia, Integer>, QueryDslPredicateExecutor<Diferencia> {

    @Modifying
    @Transactional
    @Query( value = "INSERT INTO diferencias (id_articulo,cantidad_soi,cantidad_fisico) (SELECT id_articulo,COALESCE(max(existencia),0),0 FROM articulos WHERE id_generico = 'A' GROUP BY id_articulo)", nativeQuery = true)
    void inicializarInventario()

    @Modifying
    @Transactional
    @Query( value = "UPDATE diferencias SET cantidad_soi = 0 WHERE cantidad_soi is null", nativeQuery = true)
    void inicializarInventarioExistNull()

    @Modifying
    @Transactional
    @Query( value = "DELETE FROM diferencias", nativeQuery = true )
    void limpiarTabla()

    @Modifying
    @Transactional
    @Query( value = "UPDATE diferencias set cantidad_fisico = ?1 WHERE id_articulo = ?2", nativeQuery = true )
    void actualizaCantFisico( Integer cantidadFisico, Integer pIdArticulo )


    @Modifying
    @Transactional
    @Query( value = "UPDATE diferencias set cantidad_fisico = ?1 WHERE cantidad_fisico IS NULL", nativeQuery = true )
    void actualizaCantFisicoPen( Integer cantidadFisico )

    @Modifying
    @Transactional
    @Query( value = "UPDATE diferencias set diferencias = ?1 WHERE id_articulo = ?2", nativeQuery = true )
    void insertaDiferencias( Integer diferencias, Integer pIdArticulo )

    @Query( value = "SELECT * FROM diferencias WHERE cantidad_fisico IS NULL", nativeQuery = true)
    List<Diferencia> obtenerArtPend( )

    @Query( value = "SELECT * FROM diferencias WHERE diferencias IS NULL", nativeQuery = true)
    List<Diferencia> obtenerDiferenciasPend( )

    @Modifying
    @Transactional
    @Query( value = "UPDATE diferencias SET diferencias = (SELECT cantidad_soi-cantidad_fisico FROM diferencias WHERE id_articulo = ?1) WHERE id_articulo = ?1", nativeQuery = true)
    void calcularDiferencias( Integer idArticulo )

    @Modifying
    @Transactional
    @Query( value = "UPDATE diferencias SET diferencias = 0 WHERE cantidad_soi = cantidad_fisico", nativeQuery = true)
    void insertaDiferenciasCero( )

    @Transactional
    @Query( value = "SELECT * FROM diferencias WHERE diferencias IS NOT NULL AND diferencias > 0 OR diferencias < 0", nativeQuery = true )
    List<Diferencia> obtenerDiferencias( )
}

