package tek.bwi.hackathon.emotisync.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tek.bwi.hackathon.emotisync.entities.UserInfo;

import java.util.List;


@Repository
public interface UserRepository extends MongoRepository<UserInfo, String> {
    List<UserInfo> findByRole(String role);
}

