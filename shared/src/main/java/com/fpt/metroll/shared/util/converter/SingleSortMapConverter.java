package com.fpt.metroll.shared.util.converter;

import com.fpt.metroll.shared.util.SortDirection;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SingleSortMapConverter implements Converter<String, Map<String, SortDirection>> {
    private final MultiSortMapConverter multiSortMapConverter;

    public SingleSortMapConverter(MultiSortMapConverter multiSortMapConverter) {
        this.multiSortMapConverter = multiSortMapConverter;
    }

    @Override
    public Map<String, SortDirection> convert(String source) {
        return multiSortMapConverter.convert(new String[]{source});
    }
}
