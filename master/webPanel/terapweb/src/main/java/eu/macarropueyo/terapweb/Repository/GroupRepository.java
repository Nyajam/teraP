package eu.macarropueyo.terapweb.Repository;

import eu.macarropueyo.terapweb.Model.*;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Groupp, Long>
{
	Optional<Groupp> findById(long id);
	Optional<Groupp> findByName(String name);
}