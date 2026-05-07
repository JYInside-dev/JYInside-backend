package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.najeungyoon.communitybackend.domain.app.User;
import org.najeungyoon.communitybackend.model.comment.CommentResponseDTO;
import org.najeungyoon.communitybackend.model.post.PostResponseDTO;
import org.najeungyoon.communitybackend.repository.CommentRepository;
import org.najeungyoon.communitybackend.repository.PostRepository;
import org.najeungyoon.communitybackend.repository.UserRepository;
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


    /* 유저가 작성한 post들 불러오기 */
    @Transactional(readOnly = true)
    public List<PostResponseDTO> getMyPosts(Long userId){
        User user = checkUser(userId);

        return postRepository.findAllByUser(user).stream().map((post)->{
            return PostResponseDTO.from(post);
        }).toList();
    }


    /* 유저가 작성한 댓글 가져오기 */
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getMyComments(Long userId){
        User user = checkUser(userId);

        return commentRepository.findAllByUser(user).stream().map((comment)->{
            return CommentResponseDTO.from(comment);
        }).toList();
    }






}
