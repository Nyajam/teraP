package eu.macarropueyo.terapweb.Repository;

import eu.macarropueyo.terapweb.Model.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long>
{
    Optional<Storage> findByIp(String ip);
    List<Storage> findBySpace(int space);
    List<Storage> findByStatus(int status);
}
