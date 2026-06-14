package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.domain.app.*;
import org.najeungyoon.communitybackend.domain.app.user.User;
import org.najeungyoon.communitybackend.event.NotificationEventPublisher;
import org.najeungyoon.communitybackend.model.post.PageResponseDTO;
import org.najeungyoon.communitybackend.model.post.PostResponseDTO;
import org.najeungyoon.communitybackend.repository.PostLikeRepository;
import org.najeungyoon.communitybackend.repository.PostRepository;
import org.najeungyoon.communitybackend.repository.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final NotificationEventPublisher notificationEventPublisher;



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


        /* 알림 이벤트 생성 */
        String message = user.getNickname() + "님이 게시글을 추천했습니다.";
        String url = "/board/" + post.getBoard().getUrlPath() + "/post/" + post.getId();

        notificationEventPublisher.publishEvent(
                post.getUser(), // 게시글 작성자 (수신자)
                user,           // 추천 누른 사람 (발신자)
                NotificationType.LIKE,
                message,
                url
        );
    }


    /* 인기게시글 출력 */
    @Transactional(readOnly = true)
    public PageResponseDTO<PostResponseDTO> getPopularPost(String urlPath, Pageable pageable){
        Page<Post> popularPosts = postRepository.findByBoard_UrlPathAndLikeCountGreaterThanEqualOrderByCreateAtDesc(
                urlPath,
                POPULAR_THRESHOLD,
                pageable
        );

        Page<PostResponseDTO> postResponseDTOS=popularPosts.map(PostResponseDTO::from);

        return PageResponseDTO.from(postResponseDTOS);
    }





//    /* 인기게시글 출력 상위 5개만! */
//    @Transactional(readOnly = true)
//    public List<PostResponseDTO> getTop5PopularPost(){
//        List<Post> popularPosts = postRepository.findTop5ByLikeCountGreaterThanEqualOrderByCreateAtDesc(POPULAR_THRESHOLD);
//        return popularPosts.stream().map(
//                post -> {
//                    return PostResponseDTO.from(post);
//                }
//        ).toList();
//    }


}
