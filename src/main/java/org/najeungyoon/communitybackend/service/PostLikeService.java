package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.domain.LikeType;
import org.najeungyoon.communitybackend.domain.Post;
import org.najeungyoon.communitybackend.domain.PostLike;
import org.najeungyoon.communitybackend.model.PostResponseDTO;
import org.najeungyoon.communitybackend.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostLikeService {

    private final PostRepository postRepository;
    private static final int POPULAR_THRESHOLD =10;


    /* 추천수 증가 API*/
    @Transactional
    public void addLike(Long postId){

        /* 추천을 누르려는 게시글이 존재하는지 여부 확인 */
        Post post = postRepository.findById(postId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 게시글 입니다."));

        /* 추천 주작 검증
        *  TODO
        *   ip를 통해서 추천주작을 막으려고 했는데 그냥 로그인 기능 완료시에
        *   구현하려고 함 */
//        if(postLikeRepository.existsByPostIdAndClientIp(postId,clientIp)){
//            throw new IllegalStateException("이미 추천을 누르셨습니다. ");
//        }

        PostLike postLike = new PostLike(
                post,
                LikeType.LIKE
        );
        postRepository.incrementLikeCount(postId);
    }


    /* 인기게시글 출력 */
    @Transactional(readOnly = true)
    public List<PostResponseDTO> getPopularPost(){
        List<Post> popularPosts = postRepository.findByLikeCountGreaterThanEqualOrderByCreateAtDesc(POPULAR_THRESHOLD);
        return popularPosts.stream().map(
                post -> {
                    return PostResponseDTO.from(post);
                }
        ).toList();
    }

    /* 인기게시글 출력 상위 5개만! */
    @Transactional(readOnly = true)
    public List<PostResponseDTO> getTop5PopularPost(){
        List<Post> popularPosts = postRepository.findTop5ByLikeCountGreaterThanEqualOrderByCreateAtDesc(POPULAR_THRESHOLD);
        return popularPosts.stream().map(
                post -> {
                    return PostResponseDTO.from(post);
                }
        ).toList();
    }


}
