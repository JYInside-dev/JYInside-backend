package org.najeungyoon.communitybackend.repository;

import org.najeungyoon.communitybackend.domain.app.Board;
import org.najeungyoon.communitybackend.domain.app.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByBoardAndActiveTrueOrderByDisplayOrderAsc(Board board);

    List<Category> findByBoardOrderByDisplayOrderAsc(Board board);

    Optional<Category> findByIdAndBoard(Long id, Board board);
}
