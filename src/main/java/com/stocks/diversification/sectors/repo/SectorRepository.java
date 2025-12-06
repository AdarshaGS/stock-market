package com.stocks.diversification.sectors.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stocks.diversification.sectors.data.Sector;

@Repository
public interface SectorRepository extends JpaRepository<Sector, Long> {

    @Query(value = "SELECT s.id FROM sectors s WHERE s.name = :sectorName", nativeQuery = true)
    Long findIdByName(@Param("sectorName") String sectorName);

}
