package eu.macarropueyo.terapweb.Repository;

import eu.macarropueyo.terapweb.Model.*;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoolRepository extends JpaRepository<Pool, Long>
{
    //Optional<Pool> findByVdisk(Vdisk vdisk);
    Optional<Pool> findByVm(VM vm);
}
