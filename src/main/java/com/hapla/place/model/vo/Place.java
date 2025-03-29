package com.hapla.place.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Place {
    private int placeNo;
    private int userNo;
    private String type;
    private String apiId;
}
