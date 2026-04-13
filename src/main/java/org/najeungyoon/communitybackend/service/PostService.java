package org.najeungyoon.communitybackend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.najeungyoon.communitybackend.domain.Post;
import org.najeungyoon.communitybackend.model.PostCreateRequestDTO;
import org.najeungyoon.communitybackend.model.PostDTO;
import org.najeungyoon.communitybackend.repository.PostRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;

    /* 모든 게시글 조회하는 api  */
    public List<PostDTO> getAllPost(){

        List<Post> allPosts = postRepository.findAll();
        log.info("DB에서 조회한 게시글 수: {}", allPosts.size());
        return allPosts.stream().map(
                post -> {
                    return PostDTO.from(post);
                }
        ).collect(Collectors.toList());
    }

    @Transactional
    public void createPost(PostCreateRequestDTO dto){

        log.info("게시글 저장 시작");
        // 평문비밀번호 해싱
        String hashedPassword = BCrypt.hashpw(dto.password(),BCrypt.gensalt());
        // 해싱된 비밀번호로 Entity 만들어서 DB에 insert
        Post post = dto.toEntity(hashedPassword);
        postRepository.save(post);

        log.info("게시글 저장 완료");
    }




}
