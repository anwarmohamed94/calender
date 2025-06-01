package com.stc.calender.repo;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stc.calender.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b " +
            "WHERE b.property.id = :propertyId " +
            "AND b.startDate < :end " +
            "AND b.endDate > :start")
    List<Booking> findBookingsInRange(
            @Param("propertyId") Long propertyId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("SELECT b FROM Booking b JOIN b.user u WHERE u.keycloakId = :keycloakId")
    Page<Booking> findByUserKeycloakId(@Param("keycloakId") String keycloakId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.property.id = :propertyId " +
            "AND b.startDate < :endDate " +
            "AND b.endDate > :startDate")
     List<Booking> findOverlappingBookings(
         @Param("propertyId") Long propertyId,
         @Param("startDate") Instant startDate,
         @Param("endDate") Instant endDate
     );
}