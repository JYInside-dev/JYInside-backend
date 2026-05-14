package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.domain.app.Board;
import org.najeungyoon.communitybackend.model.post.CategoryResponseDTO;
import org.najeungyoon.communitybackend.repository.BoardRepository;
import org.najeungyoon.communitybackend.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final BoardRepository boardRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getActiveCategories(String urlPath) {
        Board board = boardRepository.findByUrlPath(urlPath)
                .orElseThrow(() -> new IllegalArgumentException(urlPath + "커뮤니티는 없습니다."));

        return categoryRepository.findByBoardAndActiveTrueOrderByDisplayOrderAsc(board)
                .stream()
                .map(CategoryResponseDTO::from)
                .toList();
    }
}
