package com.hapla.admin.model.vo;

import java.util.Date;
import lombok.Data;

@Data
public class AccessStats {
    private Long id;
    private Long userId;
    private String visitorId;
    private String pageId;
    private String deviceType;
    private Date accessTime;
    private Integer sessionDuration;
    private String ipAddress;
    private String userAgent;
    private String referrer;
}