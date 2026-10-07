package com.example.urlshortener.repository;

import com.example.urlshortener.dto.CountStat;
import com.example.urlshortener.model.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    long countByShortCode(String shortCode);

    @Query("""
            select new com.example.urlshortener.dto.CountStat(c.country, count(c))
            from ClickEvent c
            where c.shortCode = :shortCode
            group by c.country
            order by count(c) desc
            """)
    List<CountStat> countByCountry(@Param("shortCode") String shortCode);

    @Query("""
            select new com.example.urlshortener.dto.CountStat(cast(c.deviceType as string), count(c))
            from ClickEvent c
            where c.shortCode = :shortCode
            group by c.deviceType
            order by count(c) desc
            """)
    List<CountStat> countByDevice(@Param("shortCode") String shortCode);

    @Query("""
            select new com.example.urlshortener.dto.CountStat(format(c.clickedAt as 'yyyy-MM-dd'), count(c))
            from ClickEvent c
            where c.shortCode = :shortCode
            group by format(c.clickedAt as 'yyyy-MM-dd')
            order by format(c.clickedAt as 'yyyy-MM-dd')
            """)
    List<CountStat> countByDay(@Param("shortCode") String shortCode);
}
