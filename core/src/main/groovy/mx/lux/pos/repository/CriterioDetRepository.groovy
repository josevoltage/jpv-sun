package mx.lux.pos.repository

import mx.lux.pos.model.CriterioDet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.querydsl.QueryDslPredicateExecutor

interface CriterioDetRepository extends JpaRepository<CriterioDet, Integer>, QueryDslPredicateExecutor<CriterioDet> {

}

