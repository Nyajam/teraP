package eu.macarropueyo.terapweb.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.macarropueyo.terapweb.Model.HelpRows;

public interface HelpRowsRepository extends JpaRepository<HelpRows, String>{

    Optional<HelpRows> findByName(String name);
    
}
