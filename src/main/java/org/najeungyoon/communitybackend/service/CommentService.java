package org.najeungyoon.communitybackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.core.ApplicationContext;
import org.mindrot.jbcrypt.BCrypt;
import org.najeungyoon.communitybackend.domain.app.*;
import org.najeungyoon.communitybackend.event.NotificationEventPublisher;
import org.najeungyoon.communitybackend.model.comment.CommentCreateRequestDTO;
import org.najeungyoon.communitybackend.model.comment.CommentDeleteRequestDTO;
import org.najeungyoon.communitybackend.model.comment.CommentResponseDTO;
import org.najeungyoon.communitybackend.model.notification.NotificationEvent;
import org.najeungyoon.communitybackend.repository.BoardAdminRepository;
import org.najeungyoon.communitybackend.repository.CommentRepository;
import org.najeungyoon.communitybackend.repository.PostRepository;
import org.najeungyoon.communitybackend.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final BoardAdminRepository boardAdminRepository;
    private final NotificationEventPublisher notificationEventPublisher;


    /* 댓글이 생성되면 이벤트 발생 */
    private final ApplicationEventPublisher eventPublisher;


    /* Post가 정말 유효한 경로에서 요청이 들어온건지 보안 검증 확인 후에 반환 */
    private Post getValidatedPost(String urlPath, Long postId){
        Post post = postRepository.findByIdWithBoardAndUser(postId)
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



    /* 댓글 생성하는 API로직 */
    @Transactional
    public void createComment(String urlPath, Long postId, CommentCreateRequestDTO dto,Long senderId){

        /* 인증 검사 및 User 가져옴 */
        Post post = getValidatedPost(urlPath, postId);

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




        User sender = null;
        Comment comment;

        /* 로그인 회원시 */
        if(senderId!=null) {
            sender = userRepository.findById(senderId)
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

            /* 회원용 댓글 생성 */
            comment = new Comment(dto.content(), sender, post, mentionedName, parent);

        }

        /* 비 로그인 회원시 */
        else {
            /* DTO에서 안했던 공백처리 진행 */
            if(!StringUtils.hasText(dto.authorName()))  throw new IllegalArgumentException("댓글 작성자 명을 입력하세요");
            if(!StringUtils.hasText(dto.password()))  throw new IllegalArgumentException("댓글 비밀번호를 입력하세요");

            String hashedPassword = bCryptPasswordEncoder.encode(dto.password());

            /* entity 조립 */
            comment = new Comment(dto.content(), dto.authorName(), hashedPassword, post, mentionedName, parent);
        }

        /* 댓글 저장 */
        commentRepository.save(comment);


        /* ===========================
                Transaction commit 이후
                알림 이벤트 발행 시작
           =========================== */

        User receiver = post.getUser();

        String senderDisplayName = sender != null ? sender.getNickname() : dto.authorName();
        String message = senderDisplayName + "님이 댓글을 남겼습니다.";
        String targetUrl = "/board/" + urlPath + "/post/" + postId;

        /* 알림 발송 */
        notificationEventPublisher.publishEvent(receiver,sender, Notification.NotificationType.COMMENT,
                message,targetUrl);

    }



    /* 댓글 삭제 API */
    @Transactional
    public void deleteComment(Long postId,
                              String urlPath,
                              Long commentId,
                              Long userId,
                              CommentDeleteRequestDTO deleteRequestDTO
    ){

        Post post = getValidatedPost(urlPath,postId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(()->new IllegalArgumentException("해당ID의 댓글을 찾을 수 없습니다."));

        boolean isAdmin = false;

        if(userId!=null){
            User user = userRepository.findById(userId)
                    .orElseThrow(()-> new IllegalArgumentException("로그인된 유저를 찾을 수 없습니다."));

            if(user.getRole()== UserRole.SUPER_ADMIN){
                isAdmin=true;
            }

            if(user.getRole() == UserRole.COMMUNITY_ADMIN){
                isAdmin = boardAdminRepository.existsByUserAndBoard(user,post.getBoard());
            }
        }

        if(!isAdmin){
            /* 댓글을 로그인 유저가 작성했을 때 */
            if(comment.getUser()!=null){
                /* 익명회원, 다른 로그인 유저가 댓글을 지우려 할 때*/
                if(userId==null || !userId.equals(comment.getUser().getId())){
                    throw new IllegalArgumentException("본인이 작성한 댓글만 삭제 가능합니다.");
                }
            }

            /* 익명 댓글일 경우에 */
            else {
                if(!BCrypt.checkpw(deleteRequestDTO.password(),comment.getPassword())){
                    throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
                }
            }
        }

        commentRepository.delete(comment);
        log.info("댓글 성공적으로 삭제됨.");

    }





}


