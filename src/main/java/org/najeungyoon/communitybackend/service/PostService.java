package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.najeungyoon.communitybackend.domain.Board;
import org.najeungyoon.communitybackend.domain.Post;
import org.najeungyoon.communitybackend.model.PostCreateRequestDTO;
import org.najeungyoon.communitybackend.model.PostDTO;
import org.najeungyoon.communitybackend.model.PostDeleteRequestDTO;
import org.najeungyoon.communitybackend.repository.BoardRepository;
import org.najeungyoon.communitybackend.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;


    /* url을 이용해서 post가 종속될 커뮤니티 Board 호출*/
    private Board getBoardByUrlPath(String urlPath){
        return boardRepository.findByUrlPath(urlPath)
                .orElseThrow(() -> new IllegalArgumentException(urlPath+"는 존재하지 않는 커뮤니티입니다."));
    }


    /* 특정 커뮤니티의 모든글을 조회하는 API  */
    @Transactional(readOnly = true)
    public List<PostDTO> getAllPostsByBoard(String urlPath){

        List<Post> allPosts = postRepository.findByBoard_UrlPath(urlPath);
        log.info("{} 에서 조회한 게시글 수: {}",urlPath, allPosts.size());
        return allPosts.stream().map(
                post -> {
                    return PostDTO.from(post);
                }
        ).collect(Collectors.toList());
    }


    /* 글 생성 API*/
    @Transactional
    public void createPost(String urlPath,PostCreateRequestDTO dto){
        log.info("{} 갤러리에 게시글 저장 시작",urlPath);


        Board board = getBoardByUrlPath(urlPath);


        // 평문비밀번호 해싱
        String hashedPassword = BCrypt.hashpw(dto.password(),BCrypt.gensalt());

        // 해싱된 비밀번호로 Entity 만들어서 board랑 같이 인자로 주입
        // DB에 insert
        Post post = dto.toEntity(hashedPassword,board);
        postRepository.save(post);

        log.info("갤러리에 게시글 저장 완료");
    }


    /* 글 삭제 APi */
    @Transactional
    public boolean deletePost(
            String urlPath,
            Long id,
            PostDeleteRequestDTO dto){

        log.info("controller에서 쏜 {}커뮤니티의 {} 글 삭제요청 service에서 받음.",urlPath,id);

        Post post = postRepository.findById(id)
                .orElseThrow( ()-> new IllegalArgumentException("삭제하려는 글은 존재하지 않는 글입니다."));

        /* 해당 갤러리에서 요청한게 맞는지 확인 */
        if(!post.getBoard().getUrlPath().equals(urlPath)){
            throw new IllegalArgumentException("해당 갤러리에 속한 글이 아닙니다.");
        }

        if(BCrypt.checkpw(dto.password(),post.getPassword())){
            postRepository.delete(post);
            log.info("성공적으로 삭제됨.");
            return true;
        }
        return false;
    }


    /* 해당 ID 의 글 불러오기 */
    @Transactional(readOnly = true) // 클래스 전체에 적용
    public PostDTO getPostById(String urlPath,Long id) {
        log.info("{}커뮤니티의 {} 번호 글 상세보기 요청 들어옴.",urlPath,id);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당id글은 없습니다."));


        /* 해당 갤러리에서 요청한게 맞는지 확인 */
        if(!post.getBoard().getUrlPath().equals(urlPath)){
            throw new IllegalArgumentException("해당 갤러리에 속한 글이 아닙니다.");
        }

        return PostDTO.from(post);
    }




}
