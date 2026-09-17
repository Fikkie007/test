package com.riverside.permits.controller;

import com.riverside.permits.domain.PermitStatus;
import com.riverside.permits.domain.Purpose;
import com.riverside.permits.repository.HallRepository;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReferenceDataController {
    private final HallRepository halls;

    public ReferenceDataController(HallRepository halls) {
        this.halls = halls;
    }

    @GetMapping("/api/reference-data")
    public ReferenceData referenceData() {
        List<HallOption> hallOptions = halls.findAll().stream()
                .filter(hall -> hall.active())
                .sorted(Comparator.comparing(hall -> hall.name()))
                .map(hall -> new HallOption(hall.id(), hall.name(), hall.district()))
                .toList();
        return new ReferenceData(
                hallOptions,
                Arrays.stream(Purpose.values()).map(purpose -> new Option(purpose.name(), purpose.displayName())).toList(),
                Arrays.stream(PermitStatus.values()).map(status -> new Option(status.name(), displayName(status.name()))).toList());
    }

    private static String displayName(String value) {
        String sentence = value.replace('_', ' ').toLowerCase(Locale.ROOT);
        return sentence.substring(0, 1).toUpperCase() + sentence.substring(1);
    }

    public record ReferenceData(List<HallOption> halls, List<Option> purposes, List<Option> statuses) { }
    public record HallOption(String id, String name, String district) { }
    public record Option(String value, String name) { }
}
