package ru.practicum.server.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.stereotype.Component;
import ru.practicum.server.model.EndpointHit;
import ru.practicum.server.model.QEndpointHit;
import ru.practicum.server.model.Stats;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CustomEndpointHitRepositoryImpl implements CustomEndpointHitRepository {

    private final JPAQueryFactory queryFactory;

    @Autowired
    public CustomEndpointHitRepositoryImpl(JpaContext context) {
        EntityManager entityManager = context.getEntityManagerByManagedType(EndpointHit.class);
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<Stats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {

        log.info("CustomEndpointHitRepositoryImpl getStats by start={}, end={}, uris={}, unique={}", start, end, uris, unique);

        QEndpointHit endpointHit = QEndpointHit.endpointHit;
        List<BooleanExpression> conditions = new ArrayList<>();

        conditions.add(endpointHit.timestamp.goe(start));
        conditions.add(endpointHit.timestamp.loe(end));
        if (uris != null && !uris.isEmpty()) {
            conditions.add(endpointHit.uri.in(uris));
        }
        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .get();
        NumberExpression<Long> uniqueIp = unique ? endpointHit.ip.countDistinct() : endpointHit.ip.count();
        NumberPath<Long> hits = Expressions.numberPath(Long.class, "hits");

        JPAQuery<Stats> query = queryFactory
                .select(Projections.bean(Stats.class,
                        endpointHit.app, endpointHit.uri, uniqueIp.as(hits)))
                .from(endpointHit)
                .where(finalCondition)
                .groupBy(endpointHit.app, endpointHit.uri)
                .orderBy(hits.desc());
        return query.fetch();

    }
}
