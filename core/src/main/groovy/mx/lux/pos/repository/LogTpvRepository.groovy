package mx.lux.pos.repository

import mx.lux.pos.model.LogTpv
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.querydsl.QueryDslPredicateExecutor

interface LogTpvRepository extends JpaRepository<LogTpv, Integer>, QueryDslPredicateExecutor<LogTpv> {

  @Query( value = "select id from log_tpv where id = (select max(id) from log_tpv)", nativeQuery = true )
  Integer getLogTpvSequence( )

}

