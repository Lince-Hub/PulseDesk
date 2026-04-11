package lt.linas_puplauskas.repository;

import lt.linas_puplauskas.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
