package mx.lux.pos.repository

import mx.lux.pos.model.Incidencia
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QueryDslPredicateExecutor

interface IncidenciaRepository extends JpaRepository<Incidencia, Integer>, QueryDslPredicateExecutor<Incidencia> {

}

