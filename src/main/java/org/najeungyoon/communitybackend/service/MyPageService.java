package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.domain.app.user.User;
import org.najeungyoon.communitybackend.model.comment.MyPageCommentResponseDTO;
import org.najeungyoon.communitybackend.model.post.PostResponseDTO;
import org.najeungyoon.communitybackend.model.user.UserPublicProfileResponseDTO;
import org.najeungyoon.communitybackend.repository.CommentRepository;
import org.najeungyoon.communitybackend.repository.PostRepository;
import org.najeungyoon.communitybackend.repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;


    private User checkUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 유저입니다."));
        return user;
    }


    /* 유저가 작성한 post들 불러오기
       마이페이지 외에도 유저페이지에서 사용하기 좋게 메서드명 일반화 */
    @Transactional(readOnly = true)
    public List<PostResponseDTO> getPostsByUserId(Long userId) {
        User user = checkUser(userId);

        return postRepository.findAllByUser(user)
                .stream()
                .map(PostResponseDTO::from)
                .toList();
    }


    /* 유저가 작성한 댓글 가져오기 */
    @Transactional(readOnly = true)
    public List<MyPageCommentResponseDTO> getCommentsByUserId(Long userId) {
        checkUser(userId);

        return commentRepository.findAllByUserIdWithPostAndBoard(userId)
                .stream()
                .map(MyPageCommentResponseDTO::from)
                .toList();
    }

    /* 다른 유저 프로필 가져오기 */
    @Transactional(readOnly = true)
    public UserPublicProfileResponseDTO getPublicProfile(Long userId) {
        User user = checkUser(userId);
        return UserPublicProfileResponseDTO.from(user);
    }

}
