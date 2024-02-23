package eu.macarropueyo.terapweb.Repository;

import eu.macarropueyo.terapweb.Model.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VdiskRepository extends JpaRepository<Vdisk, Long>
{
    List<Vdisk> findByPool(Pool pol);
    List<Vdisk> findByStorage(Storage stg);
}
