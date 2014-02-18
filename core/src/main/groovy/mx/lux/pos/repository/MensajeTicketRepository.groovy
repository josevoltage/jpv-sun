package mx.lux.pos.repository

import mx.lux.pos.model.MensajeTicket
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.querydsl.QueryDslPredicateExecutor

interface MensajeTicketRepository extends JpaRepository<MensajeTicket, Integer>, QueryDslPredicateExecutor<MensajeTicket> {
}

