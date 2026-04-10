package com.cinema.booking.repositories;

import com.cinema.booking.entities.CastMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CastMemberRepository extends JpaRepository<CastMember, Integer> {
}

