package com.fpt.metroll.shared.util.converter;

import com.fpt.metroll.shared.util.SortDirection;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class MultiSortMapConverter implements Converter<String[], Map<String, SortDirection>> {

    @Override
    public Map<String, SortDirection> convert(String[] source) {
        Map<String, SortDirection> sortMap = new LinkedHashMap<>();
        for (String sortParam : source) {
            if (sortParam != null && !sortParam.isBlank()) {
                String[] parts = sortParam.split(",");
                if (parts.length == 2) {
                    String field = parts[0];
                    SortDirection direction = SortDirection.valueOf(parts[1].toUpperCase());
                    sortMap.put(field, direction);
                }
            }
        }
        return sortMap;
    }
}
