package com.hapla.admin.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyStats {
    private String date;
    private int visitors;
    private int pageViews;
    private String avgDuration;
    private double bounceRate;
}

