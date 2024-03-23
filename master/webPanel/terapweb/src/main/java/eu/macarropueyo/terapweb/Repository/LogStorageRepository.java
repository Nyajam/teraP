package eu.macarropueyo.terapweb.Repository;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.macarropueyo.terapweb.Model.*;

public interface LogStorageRepository extends JpaRepository<LogStorage, Long>
{
    List<LogStorage> findByStorageOrderByDateDesc(Storage storage); //DESC or ASC
    List<LogStorage> findAllByOrderByDateAsc();
    List<LogStorage> findByDate(Date date);
    void deleteByStorage(Storage storage);
}
