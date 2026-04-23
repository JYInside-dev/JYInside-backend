package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.domain.LikeType;
import org.najeungyoon.communitybackend.domain.Post;
import org.najeungyoon.communitybackend.domain.PostLike;
import org.najeungyoon.communitybackend.repository.PostLikeRepository;
import org.najeungyoon.communitybackend.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostLikeService {

    private final PostRepository postRepository;

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




}
