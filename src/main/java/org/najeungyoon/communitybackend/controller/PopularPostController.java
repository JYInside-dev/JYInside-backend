package org.najeungyoon.communitybackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.model.PostResponseDTO;
import org.najeungyoon.communitybackend.repository.PostRepository;
import org.najeungyoon.communitybackend.service.PostLikeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/community/board/popular")
@RequiredArgsConstructor
@Slf4j
public class PopularPostController {

    private final PostLikeService postLikeService;


    @GetMapping("/")
    public List<PostResponseDTO> getAllPopularPost(){
        return postLikeService.getPopularPost();
    }

    @GetMapping("/top5")
    public List<PostResponseDTO> getTop5PopularPost(){
        return postLikeService.getTop5PopularPost();
    }


}
