package com.linkedin.feedservice.service;

import com.linkedin.feedservice.config.RedisConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedService {

    private final RedisTemplate<String , Object> redisTemplate;
    private static final String FEED_KEY_PREFIX = "feed:";

    public List<String> getFeed(String userId,int page ,int size)
    {
        log.info("getting feed for user: {}", userId);

        String feedkey =FEED_KEY_PREFIX+userId;
       //  pagination post range
        int start = page*size;
        int end = start+size-1;

        List<Object>  postIds = redisTemplate.opsForList().range(feedkey,start,end);

        if(postIds==null || postIds.isEmpty())
        {
            log.info("Feed cache empty for user: {}" , userId);
            return new ArrayList<>();
        }
        List<String> result = postIds.stream().map(Object::toString).toList();

        log.info("I am returning feed for user: {} ", userId);
        return result;
    }

    /**
     * clear feed cache for a user
     * @param userId
     */
    public void clearFeed(String userId)
    {
        String feedkey = FEED_KEY_PREFIX + userId;
        redisTemplate.delete(feedkey);
        log.info("feed cache cleared for user: {}", userId);
    }

}
