package eu.macarropueyo.terapweb.Repository;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.macarropueyo.terapweb.Model.*;

public interface LogHostRepository extends JpaRepository<LogHost, Long>
{
    List<LogHost> findByVhostOrderByDateDesc(Vhost vhost); //DESC or ASC
    List<LogHost> findAllByOrderByDateAsc();
    List<LogHost> findByDate(Date date);
}
