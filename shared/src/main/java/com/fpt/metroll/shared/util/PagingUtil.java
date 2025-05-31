package com.fpt.metroll.shared.util;

import com.fpt.metroll.shared.domain.dto.PageableDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;

public class PagingUtil {

    public static Pageable toPageable(PageableDto pageableDto) {
        Sort sort = Sort.unsorted();
        if (pageableDto.getSort() != null && !pageableDto.getSort().isEmpty()) {
            List<Sort.Order> orders = pageableDto.getSort().entrySet().stream()
                    .map(entry -> {
                        Sort.Direction direction = entry.getValue() == SortDirection.ASC ?
                                Sort.Direction.ASC :
                                Sort.Direction.DESC;
                        return new Sort.Order(direction, entry.getKey());
                    })
                    .collect(Collectors.toList());
            sort = Sort.by(orders);
        }
        return PageRequest.of(pageableDto.getPage(), pageableDto.getSize(), sort);
    }
}
