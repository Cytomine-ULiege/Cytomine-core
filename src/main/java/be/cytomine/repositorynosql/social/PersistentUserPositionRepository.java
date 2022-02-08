package be.cytomine.repositorynosql.social;


import be.cytomine.domain.social.PersistentImageConsultation;
import be.cytomine.domain.social.PersistentUserPosition;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


@Repository
public interface PersistentUserPositionRepository extends MongoRepository<PersistentUserPosition, Long> {


    @Aggregation(pipeline = {"{$match: {project: ?0, user: ?1, image: ?2, $and : [{created: {$gte: ?4}},{created: {$lte: ?3}}]}},{$sort: {created: 1}},{$project: {dateInMillis: {$subtract: {'$created', ?5}}}}"})
    AggregationResults retrieve(Long project, Long user, Long image, Date before, Date after, Date firstDate);

    void deleteAllByImage(Long id);
}
