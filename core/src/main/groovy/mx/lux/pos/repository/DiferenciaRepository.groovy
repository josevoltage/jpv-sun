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
    @Query( value = "INSERT INTO diferencias (id_articulo,cantidad_soi) (SELECT id_articulo,existencia FROM articulos WHERE id_generico = 'A')", nativeQuery = true)
    void inicializarInventario()

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
    @Query( value = "UPDATE diferencias set diferencias = ?1 WHERE id_articulo = ?2", nativeQuery = true )
    void insertaDiferencias( Integer diferencias, Integer pIdArticulo )

    @Query( value = "SELECT * FROM diferencias WHERE cantidad_fisico IS NULL", nativeQuery = true)
    List<Diferencia> obtenerArtPend( )
}

