package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.domain.app.LikeType;
import org.najeungyoon.communitybackend.domain.app.Post;
import org.najeungyoon.communitybackend.domain.app.PostLike;
import org.najeungyoon.communitybackend.domain.app.User;
import org.najeungyoon.communitybackend.model.post.PostResponseDTO;
import org.najeungyoon.communitybackend.repository.PostLikeRepository;
import org.najeungyoon.communitybackend.repository.PostRepository;
import org.najeungyoon.communitybackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostLikeService {

    private static final int POPULAR_THRESHOLD =10;


    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;


    /* 추천수 증가 API*/
    @Transactional
    public void addLike(Long postId,Long userId){

        /* 추천을 누르려는 게시글이 존재하는지 여부 확인 */
        Post post = postRepository.findById(postId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 게시글 입니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 유저입니다."));


        if(postLikeRepository.existsByPostAndUser(post,user)){
            throw new IllegalArgumentException("이미 추천한 게시글입니다.");
        }

        PostLike postLike = new PostLike(
                post,
                LikeType.LIKE,
                user
        );

        postLikeRepository.save(postLike);
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
