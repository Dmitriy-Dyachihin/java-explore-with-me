package ru.practicum.server.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Component;
import ru.practicum.server.model.QEndpointHit;
import ru.practicum.server.model.Stats;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class CustomEndpointHitRepositoryImpl implements CustomEndpointHitRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Stats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {

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
        NumberPath<Long> asHits = Expressions.numberPath(Long.class, "hits");
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        JPAQuery<Stats> query = queryFactory
                .select(Projections.bean(Stats.class,
                        endpointHit.app, endpointHit.uri, uniqueIp.as(asHits)))
                .from(endpointHit)
                .where(finalCondition)
                .groupBy(endpointHit.app, endpointHit.uri)
                .orderBy(asHits.desc());
        return query.fetch();
    }
}
