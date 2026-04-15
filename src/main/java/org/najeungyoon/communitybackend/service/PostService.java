package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.najeungyoon.communitybackend.domain.Post;
import org.najeungyoon.communitybackend.model.PostCreateRequestDTO;
import org.najeungyoon.communitybackend.model.PostDTO;
import org.najeungyoon.communitybackend.model.PostDeleteRequestDTO;
import org.najeungyoon.communitybackend.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /* 글 생성 API*/
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


    /* 글 삭제 APi */
    @Transactional
    public boolean deletePost(
            Long id,
            PostDeleteRequestDTO dto){

        log.info("controller에서 쏜 "+id +"글 삭제요청 service에서 받음.");
        Post post = postRepository.findById(id)
                .orElseThrow( ()-> new IllegalArgumentException("삭제하려는 글은 존재하지 않는 글입니다."));

        if(BCrypt.checkpw(dto.password(),post.getPassword())){
            postRepository.delete(post);
            log.info("성공적으로 삭제됨.");
            return true;
        }
        return false;
    }


    /* 해당 ID 의 글 불러오기 */
    @Transactional(readOnly = true) // 클래스 전체에 적용
    public PostDTO getPostById(Long id) {
        log.info(id+"번호 글 상세보기 요청 들어옴.");

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당id글은 없습니다."));
        return PostDTO.from(post);
    }




}
