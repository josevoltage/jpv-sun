package mx.lux.pos.repository

import mx.lux.pos.model.Remesas
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.querydsl.QueryDslPredicateExecutor

interface RemesasRepository extends JpaRepository<Remesas, Integer>, QueryDslPredicateExecutor<Remesas> {
}

