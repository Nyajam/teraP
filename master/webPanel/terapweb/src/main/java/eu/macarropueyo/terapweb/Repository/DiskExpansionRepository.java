package eu.macarropueyo.terapweb.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.macarropueyo.terapweb.Model.*;

public interface DiskExpansionRepository extends JpaRepository<DiskExpansion, Long>
{
    Optional<DiskExpansion> findByVm(VM vm);
}
