package com.linkedin.notification_service.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class NotificationService {

    /**
     * consume user.created event
     * send welcome notification
     */
    @KafkaListener(topics  = "user.created" )
    public void consumeUserCreated(
            @Payload Map<String, Object> payload
            )
    {
        try {
            String userId = (String)payload.get("userId");
            String firstName = (String)payload.get("firstName");

            sendNotification(userId,"Welcome to Linkdin!",
                    String.format("Welcome %s Your account has created"+
                            "Start connecting with Professionals.",
                            firstName));
        }
        catch (Exception e)
        {
            log.error("Error sending welcome notification");

        }
    }

    /**
     * consume connectin.request event
     * notify reciever about connection request.
     */
    @KafkaListener(topics = "connection.request")
    public void consumeConnectionRequest(
            @Payload Map<String,Object> payload
    )
    {
        try {
            String receiverId = (String)payload.get("receiverId");
            String requesterId = (String)payload.get("requesterId");

            sendNotification(receiverId , "New Connection Request" , String.format(
                    "User %s want to connect with you",requesterId
            ));

        }
        catch (Exception e){
            log.error("Error sending connection notification");



        }
    }

    /**
     * consume connectin.accepted event
     * notify sender about connection accepted.
     */

    @KafkaListener(topics = "connection.accepted")
    public void consumeConnectionAccepted(
            @Payload Map<String,Object> payload
    )
    {
        try {
            String receiverId = (String)payload.get("receiverId");
            String requesterId = (String)payload.get("requesterId");

            sendNotification(receiverId , "Connection Accepted" , String.format(
                    "User %s accepted your connection request",receiverId
            ));

        }
        catch (Exception e){
            log.error("Error sending connection accepted notification");



        }
    }
    @KafkaListener(topics = "posts.liked")
    public void consumePostLiked(
            @Payload Map<String,Object> payload
    )
    {
        try{
            String authorId = (String)payload.get("authorId");
            String userId = (String)payload.get("userId");
            String postId = (String)payload.get("postId");

            sendNotification(authorId , "someone liked your post",
                    String.format("user %s liked your post %s", userId , postId));





        }
        catch(Exception e)
        {
            log.error("Error sending like notification");


        }

    }

    @KafkaListener(topics = "posts.commented")
    public void consumePostCommented(
            @Payload Map<String,Object> payload
    )
    {
        try{
            String postAuthorId = (String)payload.get("postAuthorId");
            String authorId= (String)payload.get("authorId");
            String postId = (String)payload.get("postId");

            sendNotification(postAuthorId , "someone commented your post",
                    String.format("user %s commented your post %s",authorId , postId));





        }
        catch(Exception e)
        {
            log.error("Error sending comment notification");


        }

    }




    private void sendNotification(String userId , String title , String message)
    {
        log.info("-------------------");
        log.info("Notification Send service is done ");

    }


}
