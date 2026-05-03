package org.najeungyoon.communitybackend.repository;

import org.najeungyoon.communitybackend.domain.app.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board,Long> {

    Optional<Board> findByUrlPath(String urlPath);

    boolean existsByUrlPath(String urlPath);
}
