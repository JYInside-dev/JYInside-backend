package org.najeungyoon.communitybackend.controller;

import lombok.RequiredArgsConstructor;
import org.najeungyoon.communitybackend.model.post.CategoryResponseDTO;
import org.najeungyoon.communitybackend.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/board/{urlPath}/categories")
    public ResponseEntity<List<CategoryResponseDTO>> getCategories(
            @PathVariable String urlPath
    ) {
        return ResponseEntity.ok(categoryService.getActiveCategories(urlPath));
    }
}
