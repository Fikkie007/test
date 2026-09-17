package com.riverside.permits.repository;

import com.riverside.permits.domain.Hall;
import java.util.List;
import java.util.Optional;

public interface HallRepository {
    Optional<Hall> findById(String id);
    List<Hall> findAll();
    void save(Hall hall);
}
