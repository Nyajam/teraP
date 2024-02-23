package eu.macarropueyo.terapweb.Repository;

import eu.macarropueyo.terapweb.Model.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VmRepository extends JpaRepository<VM, Long>
{
    List<VM> findByHost(Vhost host);
    List<VM> findByGroup(Groupp group);
    Optional<VM> findByUuid(String uuid);
    List<VM> findAllByOrderByDateDesc();
    List<VM> findAllByOrderByDateAsc();
}
