package com.riverside.permits.repository;

import com.riverside.permits.domain.PermitHistory;
import java.util.List;

public interface PermitHistoryRepository {
    List<PermitHistory> findByPermitNumber(String permitNumber);
    void save(PermitHistory history);
    void delete(String id);
}
