package tek.bwi.hackathon.emotisync.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tek.bwi.hackathon.emotisync.models.RequestAnalysis;

import java.util.Optional;

@Repository
public interface RequestAnalysisRepository extends MongoRepository<RequestAnalysis, String>{
    Optional<RequestAnalysis> findByRequestId(String requestId);
}
