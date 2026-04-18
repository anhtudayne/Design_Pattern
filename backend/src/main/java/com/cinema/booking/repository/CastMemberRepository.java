package com.cinema.booking.repository;

import com.cinema.booking.entity.CastMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CastMemberRepository extends JpaRepository<CastMember, Integer> {
}

