package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.najeungyoon.communitybackend.domain.app.Board;
import org.najeungyoon.communitybackend.domain.app.Post;
import org.najeungyoon.communitybackend.domain.app.user.User;
import org.najeungyoon.communitybackend.domain.app.user.UserRole;
import org.najeungyoon.communitybackend.domain.app.*;
import org.najeungyoon.communitybackend.model.PasswordVerifyRequestDTO;
import org.najeungyoon.communitybackend.model.post.*;
import org.najeungyoon.communitybackend.repository.BoardAdminRepository;
import org.najeungyoon.communitybackend.repository.BoardRepository;
import org.najeungyoon.communitybackend.repository.PostRepository;
import org.najeungyoon.communitybackend.repository.user.UserRepository;
import org.najeungyoon.communitybackend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardAdminRepository boardAdminRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final CommentRepository commentRepository;
    private final PostRedisService postRedisService;


    /* url을 이용해서 post가 종속될 커뮤니티 Board 호출*/
    private Board getBoardByUrlPath(String urlPath){
        return boardRepository.findByUrlPath(urlPath)
                .orElseThrow(() -> new IllegalArgumentException(urlPath+"는 존재하지 않는 커뮤니티입니다."));
    }


    /* 페이지로 나눠서 글 조회 */
    @Transactional(readOnly = true)
    public PageResponseDTO<PostResponseDTO> getPosts(
            String urlPath,
            Long categoryId,
            String sort,
            int page,
            int size
    ) {
        Sort postSort = switch (sort == null ? "latest" : sort) {
            case "view" -> Sort.by(
                    Sort.Order.desc("viewCount"),
                    Sort.Order.desc("createAt")
            );
            case "latest" -> Sort.by(
                    Sort.Order.desc("createAt")
            );
            default -> throw new IllegalArgumentException("지원하지 않는 정렬 방식입니다.");
        };

        Pageable pageable = PageRequest.of(page, size, postSort);

        Page<Post> postPage;

        if (categoryId == null) {
            postPage = postRepository.findByBoard_UrlPath(urlPath, pageable);
        } else {
            postPage = postRepository.findByBoard_UrlPathAndCategory_Id(urlPath, categoryId, pageable);
        }



        /* 게시글 조회시 n+1 문제 해결을 위해 게시글id, 게시글 댓글 수를 한꺼번에 모아 쿼리로 날림.*/
        List<Long> postIds = postPage.getContent().stream()
                .map(Post::getId)
                .toList();

        Map<Long, Integer> commentCountByPostId = postIds.isEmpty()
                ? Map.of()
                : commentRepository.countByPostIds(postIds).stream()
                .collect(Collectors.toMap(
                        PostCommentCountDTO::postId,
                        result -> Math.toIntExact(result.commentCount())
                ));

        Page<PostResponseDTO> dtoPage = postPage.map(post ->
                PostResponseDTO.from(
                        post,
                        commentCountByPostId.getOrDefault(post.getId(), 0)
                )
        );

        return PageResponseDTO.from(dtoPage);
    }


    /* 글 생성 API*/
    @Transactional
    public void createPost(
            String urlPath,
            PostCreateRequestDTO dto,
            Long userId){
        log.info("{} 갤러리에 게시글 저장 시작",urlPath);

        Board board = getBoardByUrlPath(urlPath);

        Category category = null;

        if (dto.categoryId() != null) {
            category = categoryRepository.findByIdAndBoard(dto.categoryId(), board)
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));

            if (!category.isActive()) {
                throw new IllegalArgumentException("비활성화된 카테고리에는 글을 작성할 수 없습니다.");
            }
        }

        /* 로그인 회원시 */
        if(userId != null){
            User user = userRepository.findById(userId)
                    .orElseThrow(()->new IllegalArgumentException("ID와 일치하는 유저를 찾을 수 없습니다."));
            Post post = dto.toEntityUser(board,user, category);
            postRepository.save(post);
        }

        /* 비 로그인 회원시 */
        else {

            /* DTO에서 검증안한 authorName,password 빈칸 검증 */
            if(!StringUtils.hasText(dto.authorName())){
                throw new IllegalArgumentException("작성자 명을 입력해 주세요");
            }
            if(!StringUtils.hasText(dto.password())){
                throw new IllegalArgumentException("비밀번호를 입력해 주세요");
            }


            // 평문비밀번호 해싱
            String hashedPassword = BCrypt.hashpw(dto.password(),BCrypt.gensalt());

            // 해싱된 비밀번호로 Entity 만들어서 board랑 같이 인자로 주입
            // DB에 insert
            Post post = dto.toEntityAnonymous(hashedPassword, board, category);
            postRepository.save(post);
        }

        log.info("갤러리에 게시글 저장 완료");
    }


    /* 글 삭제 APi */
    @Transactional
    public void deletePost(
            String urlPath,
            Long id,
            PostDeleteRequestDTO dto,
            Long userId
      ){

        log.info("controller에서 쏜 {}커뮤니티의 {} 글 삭제요청 service에서 받음.",urlPath,id);

        Post post = postRepository.findById(id)
                .orElseThrow( ()-> new IllegalArgumentException("삭제하려는 글은 존재하지 않는 글입니다."));

        /* 해당 갤러리에서 요청한게 맞는지 확인 */
        if(!post.getBoard().getUrlPath().equals(urlPath)){
            throw new IllegalArgumentException("해당 갤러리에 속한 글이 아닙니다.");
        }

        boolean isAdmin = false;
        if(userId!=null){
            User requestUser = userRepository.findById(userId)
                    .orElseThrow(()->new IllegalArgumentException("로그인 된 유저를 찾을 수 없습니다. "));

            /* SUPER_ADMIN은 바로 Pass */
            if(requestUser.getRole() == UserRole.SUPER_ADMIN){
                isAdmin=true;
            }

            /* 커뮤니티 관리자면 해당 글이 써진 커뮤니티의 관리자인지 검증 */
            if(requestUser.getRole()==UserRole.COMMUNITY_ADMIN){
                isAdmin = boardAdminRepository.existsByUserAndBoard(requestUser,post.getBoard());
            }
        }

        if(!isAdmin){
            /* 로그인 회원 글일시 userId 대조 후 삭제.*/
            if(post.getUser() != null){
                /* 비로그인회원이거나 다른 회원이 지우려고 할 떄 */
                if(userId == null || !post.getUser().getId().equals(userId)){
                    throw new IllegalArgumentException("본인이 작성한 글만 삭제할 수 있습니다.");
                }
            }
        }

        postRepository.delete(post);
        log.info("게시판 성공적으로 삭제됨.");

    }


    /* 글 수정 API */
    @Transactional
    public PostResponseDTO updatePost(String urlPath, Long id, PostUpdateRequestDTO dto, Long userId){

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당id글은 없습니다."));

        /* 존재하는 게시판인지 검증 */
        getBoardByUrlPath(urlPath);

        /* 해당 갤러리에서 요청한게 맞는지 확인 */
        if(!post.getBoard().getUrlPath().equals(urlPath)){
            throw new IllegalArgumentException("해당 갤러리에 속한 글이 아닙니다.");
        }

        if(post.getUser() != null){
            /* 비로그인회원이거나 다른 회원이 지우려고 할 떄 */
            if(userId == null || !post.getUser().getId().equals(userId)){
                throw new IllegalArgumentException("본인이 작성한 글만 수정할 수 있습니다.");
            }
        }

        post.update(dto.title(),dto.content());
        return PostResponseDTO.from(post);
    }

    /* 비밀번호 체크 */
    @Transactional(readOnly = true)
    public void verifyPassword(PasswordVerifyRequestDTO dto,Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당id글은 없습니다."));
        if (!passwordEncoder.matches(dto.password(), post.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }
    }

    /* 해당 ID 의 글 불러오기 */
    @Transactional(readOnly = true) // 클래스 전체에 적용
    public PostResponseDTO getPostById(String urlPath, Long id) {
        log.info("{}커뮤니티의 {} 번호 글 상세보기 요청 들어옴.",urlPath,id);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당id글은 없습니다."));

        /* 해당 갤러리에서 요청한게 맞는지 확인 */
        if(!post.getBoard().getUrlPath().equals(urlPath)){
            throw new IllegalArgumentException("해당 갤러리에 속한 글이 아닙니다.");
        }


        /* 수정할 조회수 로직 */
//        postRepository.increaseViewCount(id);

        PostResponseDTO response = PostResponseDTO.from(post);
        long currentViewCount = postRedisService.incrementAndGet(
                post.getId(),
                post.getViewCount()
        );

        return  response.withViewCount(currentViewCount);
    }


    /* 검색어로 글 검색 */
    @Transactional(readOnly = true)
    public List<PostResponseDTO> searchPosts(String urlPath, String type, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }

        Board board = boardRepository.findByUrlPath(urlPath)
                .orElseThrow(() -> new IllegalArgumentException("게시판을 찾을 수 없습니다."));

        String searchType = type == null ? "all" : type.toLowerCase();

        List<Post> posts = switch (searchType) {
            case "title" -> postRepository.findByBoardAndTitleContainingIgnoreCase(board, keyword);
            case "content" -> postRepository.findByBoardAndContentContainingIgnoreCase(board, keyword);
            case "author" -> postRepository.findByBoardAndAuthorNameContainingIgnoreCase(board, keyword);
            case "all" -> postRepository.searchAllByBoard(board, keyword );
            default -> throw new IllegalArgumentException("지원하지 않는 검색 옵션입니다.");
        };

        return posts.stream()
                .map(PostResponseDTO::from)
                .toList();
    }


    /* 고정 글 목록 조회 */
    @Transactional(readOnly = true)
    public List<PostResponseDTO> getPinnedPosts(String urlPath) {
        Board board = boardRepository.findByUrlPath(urlPath)
                .orElseThrow(() -> new IllegalArgumentException("게시판을 찾을 수 없습니다."));

        return postRepository.findByBoardAndPinnedTrueOrderByCreateAtDesc(board)
                .stream()
                .map(PostResponseDTO::from)
                .toList();
    }


    /* 글 고정/해제 토글 기능 */
    @Transactional
    public PostResponseDTO togglePinned(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        post.togglePinned();

        return PostResponseDTO.from(post);
    }

}
