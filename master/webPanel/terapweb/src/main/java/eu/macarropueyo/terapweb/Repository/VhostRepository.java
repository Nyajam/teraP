package eu.macarropueyo.terapweb.Repository;

import eu.macarropueyo.terapweb.Model.*;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VhostRepository extends JpaRepository<Vhost, Long>
{
    Optional<Vhost> findById(long id);
    Optional<Vhost> findByIp(String ip);
}