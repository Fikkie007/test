package com.riverside.permits.repository;

import com.riverside.permits.domain.Permit;
import java.util.List;
import java.util.Optional;

public interface PermitRepository {
    Optional<Permit> findByNumber(String permitNumber);
    List<Permit> findAll();
    void save(Permit permit);
}
