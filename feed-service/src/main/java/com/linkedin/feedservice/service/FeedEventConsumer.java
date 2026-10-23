package com.linkedin.feedservice.service;


import com.linkedin.feedservice.client.UserServiceClient;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor

public class FeedEventConsumer {

    private final UserServiceClient userServiceClient;
    private static final String FEED_KEY_PREFIX = "feed:";
    private final RedisTemplate<String , Object> redisTemplate;

    @Value("${feed.max-size}")
    private int maxFeedSize;
    /**
     * consume post.created event
     * when a user create a post -immediatily push that post
     * to all their connection feed
     * @param payload
     */
    @KafkaListener(topics = "post.created")
    public void consumePostCreated(
            @Payload Map<String ,Object> payload
            )
    {
        try {
            String postId = (String)payload.get("postId");
            String authorId  = (String)payload.get("authorId");

            // get all connection of post author
            List<Map<String,Object>> connections =userServiceClient.getConnections(authorId);


            //push post to each connection's feed
            for (Map<String, Object> connection: connections)
            {
                String connectionId  = (String) connection.get("Id");
                String feedkey = FEED_KEY_PREFIX+connectionId;

                //add post to feed( as sorted by time stamp)
                redisTemplate.opsForList().leftPush(feedkey , postId);

                redisTemplate.opsForList().trim(feedkey,0,maxFeedSize-1);

                log.info("post {} pushed to feed of user: {}", postId , connectionId);


            }
            String authorFeedKey = FEED_KEY_PREFIX+authorId;
            redisTemplate.opsForList().leftPush(authorFeedKey,postId);

            redisTemplate.opsForList().trim(authorFeedKey,0,maxFeedSize-1);


        }
        catch (Exception e)
        {
            log.error("error in pushing feed: {}", e.getMessage());
        }

    }



}
