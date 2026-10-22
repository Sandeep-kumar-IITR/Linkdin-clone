package com.linkedin.postservice.service;

import com.linkedin.postservice.entity.Comment;
import com.linkedin.postservice.entity.Like;
import com.linkedin.postservice.entity.Post;
import com.linkedin.postservice.repository.CommentRepository;
import com.linkedin.postservice.repository.LikeRepository;
import com.linkedin.postservice.repository.PostRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor

public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final S3Service s3Service;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String POST_CREATED_TOPIC = "post.created";

    private static final String POST_LIKED_TOPIC = "post.liked";

    private static final String POST_COMMENT_TOPIC = "post.commented";

    /**
     * create post
     * optionally upload image to s3
     * public post.created event to kafka
     * Feed Service and search Service will consume this
     * @return
     */

    public Post createPost(String authorId, String content , MultipartFile image) {
            log.info("Created post for user: {}",authorId);
            Post post = new Post();
            post.setAuthorId(authorId);
            post.setContent(content);

            if(image != null&&!image.isEmpty()) {

                String imageUrl = s3Service.uploadFile(
                        image , "post/"+authorId);
                post.setImageUrl(imageUrl);
            }
            Post savedPost = postRepository.save(post);
            log.info("Saved post for user: {}",savedPost.getId());

            //publish to kafka - Feed service and search service
        Map<String,Object> postCreatedEvent = new HashMap<>();
        postCreatedEvent.put("postId",savedPost.getId());
        postCreatedEvent.put("authorId",savedPost.getAuthorId());
        postCreatedEvent.put("content",savedPost.getContent());
        postCreatedEvent.put("imageUrl",savedPost.getImageUrl());
        postCreatedEvent.put("createdAt",savedPost.getCreatedAt()).toString();
        kafkaTemplate.send(POST_CREATED_TOPIC,savedPost.getId(),postCreatedEvent);

        log.info("Post created event published: {}",savedPost.getId());

        return savedPost;
    }
    public Post getPost(String postId) {
        return postRepository.findById(postId).orElseThrow(()-> new RuntimeException("post not found"));
    }

    public List<Post> getUserPosts(String userId) {
        return postRepository.findByAuthorIdOrderAtCreatedAtDesc(userId);
    }

    public String likePost(String postId,String userId) {

        Post post  = getPost(postId);

        if(likeRepository.existsByPostIdAndUserId(postId,userId))
        {
            likeRepository.findByPostIdAndUserId(postId,userId).ifPresent(likeRepository::delete);
            post.setLikeCount(post.getLikeCount()-1);
            postRepository.save(post);

            return "Post unliked";
        }


        Like like = new Like();
        like.setPostId(postId);
        like.setUserId(userId);
         likeRepository.save(like);
         post.setLikeCount(post.getLikeCount()+1);
          postRepository.save(post);

          // publish post.liked event
        Map<String,Object> postLikedEvent = new HashMap<>();
        postLikedEvent.put("postId",post.getId());
        postLikedEvent.put("userId", userId);
        postLikedEvent.put("authorId",post.getAuthorId());

        kafkaTemplate.send(POST_LIKED_TOPIC,postId,postLikedEvent);

        return "Post liked";

    }

    /**
     * Add Comment to post
     */

    public Comment addComment(String postId, String authorId , String content) {

        Post post = getPost(postId);
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(authorId);
        comment.setContent(content);
        Comment savedComment =  commentRepository.save(comment);
        post.setCommentCount(post.getCommentCount()+1);
        postRepository.save(post);

        // publish post commented

        Map<String,Object> commentCreatedEvent = new HashMap<>();
        commentCreatedEvent.put("postId",savedComment.getPostId());
        commentCreatedEvent.put("authorId",savedComment.getAuthorId());
        commentCreatedEvent.put("commentId",savedComment.getId());
        commentCreatedEvent.put("postAuthorId",post.getAuthorId());

        kafkaTemplate.send(POST_COMMENT_TOPIC,postId,commentCreatedEvent);

        return savedComment;


    }

    /**
     * get cmment list for a post
     * @param postId
     * @return
     */

    public List<Comment> getComments(String postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId)
    }

    public String deletePost(String postId , String userId)
    {
        Post post = getPost(postId);

        if(!post.getAuthorId().equals(userId))
        {
            throw new RuntimeException("Not authorize to delete this post");

        }
        postRepository.delete(post);
        log.info("Post deleted: {}",post.getId());
        return "Post deleted";


    }







}
