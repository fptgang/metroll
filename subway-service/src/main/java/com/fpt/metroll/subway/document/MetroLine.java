package com.fpt.metroll.subway.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("metro_lines")
public class MetroLine {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    private String name;
    private String color;
    private String operatingHours;
    private LineStatus status;
    private String description;

    @Builder.Default
    private List<Segment> segments = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Segment {
        private Integer sequence;
        private String startStationCode;
        private String endStationCode;
        private Double distance;
        private Integer travelTime;
        private String description;
    }

    public enum LineStatus {
        OPERATIONAL, PLANNED, UNDER_MAINTENANCE, CLOSED
    }
}
