package com.riverside.permits.repository;

import com.riverside.permits.domain.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
class InMemoryPermitRepository implements PermitRepository {
    private final Map<String, Permit> data = new ConcurrentHashMap<>();
    public Optional<Permit> findByNumber(String number) { return Optional.ofNullable(data.get(number)); }
    public List<Permit> findAll() { return new ArrayList<>(data.values()); }
    public void save(Permit permit) { data.put(permit.permitNumber(), permit); }
}

@Repository
class InMemoryHallRepository implements HallRepository {
    private final Map<String, Hall> data = new ConcurrentHashMap<>();
    public Optional<Hall> findById(String id) { return Optional.ofNullable(data.get(id)); }
    public List<Hall> findAll() { return new ArrayList<>(data.values()); }
    public void save(Hall hall) { data.put(hall.id(), hall); }
}

@Repository
class InMemoryRenewalRepository implements RenewalRepository {
    private final List<Renewal> data = Collections.synchronizedList(new ArrayList<>());
    public List<Renewal> findByPermitNumber(String number) { synchronized (data) { return data.stream().filter(r -> r.permitNumber().equals(number)).toList(); } }
    public void save(Renewal renewal) { data.add(renewal); }
    public void delete(String id) { data.removeIf(r -> r.id().equals(id)); }
}

@Repository
class InMemoryPermitHistoryRepository implements PermitHistoryRepository {
    private final List<PermitHistory> data = Collections.synchronizedList(new ArrayList<>());
    public List<PermitHistory> findByPermitNumber(String number) { synchronized (data) { return data.stream().filter(h -> h.permitNumber().equals(number)).toList(); } }
    public void save(PermitHistory history) { data.add(history); }
    public void delete(String id) { data.removeIf(h -> h.id().equals(id)); }
}
