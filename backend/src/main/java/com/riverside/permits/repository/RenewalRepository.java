package com.riverside.permits.repository;

import com.riverside.permits.domain.Renewal;
import java.util.List;

public interface RenewalRepository {
    List<Renewal> findByPermitNumber(String permitNumber);
    void save(Renewal renewal);
    void delete(String id);
}
