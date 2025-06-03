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
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("stations")
public class Station {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    private String name;
    private String address;
    private Coordinates location;
    private StationStatus status;
    private String description;

    private List<String> lineIds;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Coordinates {
        private double lat;
        private double lng;
    }

    public enum StationStatus {
        OPERATIONAL, UNDER_MAINTENANCE, CLOSED
    }
}
