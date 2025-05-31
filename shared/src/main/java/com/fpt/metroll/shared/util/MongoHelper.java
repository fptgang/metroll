package com.fpt.metroll.shared.util;

import com.fpt.metroll.shared.domain.dto.PageableDto;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class MongoHelper {

    private final MongoTemplate mongoTemplate;

    public MongoHelper(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public <T> Page<T> find(Function<Query, Query> queryBuilder,
                            PageableDto pageableDto,
                            Class<T> entityClass) {
        Pageable pageable = PagingUtil.toPageable(pageableDto);

        Query baseQuery = new Query();
        Query finalQuery = queryBuilder.apply(baseQuery).with(pageable);

        List<T> content = mongoTemplate.find(finalQuery, entityClass);
        long total = mongoTemplate.count(Query.of(finalQuery).limit(-1).skip(-1), entityClass);

        return new PageImpl<>(content, pageable, total);
    }
}
