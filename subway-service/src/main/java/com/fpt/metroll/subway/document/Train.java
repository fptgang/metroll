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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("trains")
public class Train {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    private String trainNumber;
    private Integer capacity;
    private TrainStatus status;
    private String assignedLineId;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public enum TrainStatus {
        OPERATIONAL, UNDER_MAINTENANCE, RETIRED
    }
}
