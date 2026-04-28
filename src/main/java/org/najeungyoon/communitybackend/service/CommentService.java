package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.najeungyoon.communitybackend.domain.Comment;
import org.najeungyoon.communitybackend.domain.Post;
import org.najeungyoon.communitybackend.domain.User;
import org.najeungyoon.communitybackend.model.comment.CommentCreateRequestDTO;
import org.najeungyoon.communitybackend.model.comment.CommentResponseDTO;
import org.najeungyoon.communitybackend.repository.CommentRepository;
import org.najeungyoon.communitybackend.repository.PostRepository;
import org.najeungyoon.communitybackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /* Post가 정말 유효한 경로에서 요청이 들어온건지 보안 검증 확인 후에 반환 */
    private Post getValidatedPost(String urlPath, Long postId){
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 글입니다."));

        if (!post.getBoard().getUrlPath().equals(urlPath)) {
            throw new IllegalStateException("잘못된 접근입니다. 게시글과 게시판 정보가 일치하지 않습니다.");
        }

        return post;
    }


    /*
        게시글의 id에 종속된 루트 댓글들 조회
        루트댓글들만 호출되면 어짜피 자식댓글들 알아서 조회됨.
    */
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByPostId(String urlPath,Long postId){

        getValidatedPost(urlPath, postId);

        List<Comment> rootComments = commentRepository.findByPostIdAndParentIsNull(postId);

        return rootComments.stream().map(
                rootComment -> {
                    return CommentResponseDTO.from(rootComment);
                }
        ).toList();
    };



    /* 댓글 생성하는 API */
    @Transactional
    public void createComment(String urlPath, Long postId, CommentCreateRequestDTO dto,Long userId){

        /* 인증 검사 */
        Post post = getValidatedPost(urlPath, postId);

        String hashedPassword = BCrypt.hashpw(dto.password(),BCrypt.gensalt());
        Comment parent = null;
        String mentionedName = null;


        /* 루트 댓글이 아니고 대댓글인 경우 */
        if(dto.parentId() != null){
            /* 루트댓글 검사 */
            Comment targetComment = commentRepository.findById(dto.parentId())
                    .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 루트댓글 입니다."));

            // 루트댓글에 대댓긇을 단 경우
            if(targetComment.getParent()==null){
                parent=targetComment;
            }else{
                /* 대댓글에 댓글을 단 경우 부모를 루트 댓글로 수정 */
                parent = targetComment.getParent();
                mentionedName = targetComment.getAuthorName();
            }
        }
        /* 로그인 회원시 */
        if(userId!=null){
            User user = userRepository.findById(userId)
                    .orElseThrow(()->new IllegalArgumentException("유저를 찾을 수 없습니다."));

            /* 회원용 댓글 생성 */
            Comment comment = new Comment(
                    dto.content(),
                    user,
                    post,
                    mentionedName,
                    parent
            );
            commentRepository.save(comment);

        }else{
            /* entity 조립 */
            Comment comment = new Comment(
                    dto.content(),
                    dto.authorName(),
                    hashedPassword,
                    post,
                    mentionedName,
                    parent);
            commentRepository.save(comment);
        }
    }
}
