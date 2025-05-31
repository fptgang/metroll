package com.fpt.metroll.shared.domain.mapper;

import com.fpt.metroll.shared.domain.dto.PageDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface PageMapper {

    PageMapper INSTANCE = Mappers.getMapper(PageMapper.class);

    default <T> PageDto<T> toPageDTO(Page<T> page) {
        return PageDto.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
