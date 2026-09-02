package com.OdysseyAi.demo.travel.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DestinationData {

    private String name;

    private String formattedAddress;

    private Double latitude;

    private Double longitude;

    private String placeId;
}