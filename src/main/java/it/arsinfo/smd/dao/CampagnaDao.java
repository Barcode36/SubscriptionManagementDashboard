package it.arsinfo.smd.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import it.arsinfo.smd.data.Anno;
import it.arsinfo.smd.entity.Campagna;

public interface CampagnaDao extends JpaRepository<Campagna, Long> {

	Campagna findByAnno(Anno anno);

}