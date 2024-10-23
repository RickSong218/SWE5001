package com.SSMS.company.service;

import org.springframework.stereotype.Service;
import com.SSMS.company.dto.TimeZoneList;

import java.util.TimeZone;

@Service
public class TimeZoneService {
    // 列出所有可用的时区ID
    public TimeZoneList listTimeZones() {
        // 创建一个TimeZoneList对象，使用其builder模式进行构建
        TimeZoneList timeZoneList = TimeZoneList.builder().build();
        // 遍历所有可用的时区ID（通过TimeZone类的getAvailableIDs方法获取）
        for(String id : TimeZone.getAvailableIDs()) {
            // 将每个时区ID添加到timeZoneList对象的timezones列表中
            timeZoneList.getTimezones().add(id);
        }
        return timeZoneList; // 返回包含所有时区ID的timeZoneList对象
    }
}
