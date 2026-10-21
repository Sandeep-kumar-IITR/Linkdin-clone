package com.linkedin.userservice.service;

import com.linkedin.userservice.dto.UserResponse;
import com.linkedin.userservice.entity.Connection;
import com.linkedin.userservice.entity.ConnectionStatus;
import com.linkedin.userservice.entity.User;
import com.linkedin.userservice.repository.ConnectionRepositiry;
import com.linkedin.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final ConnectionRepositiry connectionRepositiry;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String CONNECTION_REQUESTED_TOPIC = "connection.requested";
    private static final String CONNECTION_ACCEPTED_TOPIC = "connection.accepted";

    private static final String USER_UPDATED_TOPIC = "user.updated";

    public String sendConnectionRequest(String recieverId , String requesterId){

        if(connectionRepositiry.existByRequesterIdAndRecieverId(requesterId,recieverId))
        {
            throw new RuntimeException("connection request already send");


        }
        Connection  connection = new Connection();
        connection.setRequesterId(requesterId);
        connection.setReceiverId(recieverId);
        connection.setStatus(ConnectionStatus.PENDING);

        connectionRepositiry.save(connection);


        // publish connection.requested event
        Map<String,Object> connectionRequestEvent = new HashMap<>();
        connectionRequestEvent.put("requesterId",requesterId);
        connectionRequestEvent.put("recieverId",recieverId);
        kafkaTemplate.send(CONNECTION_REQUESTED_TOPIC,requesterId,connectionRequestEvent);

        log.info("connection request sent to requesterId={}",requesterId);

        return "Connection request sent";


    }

    public String acceptConnectionRequest(String connectionId){
        Connection connection = connectionRepositiry.findById(connectionId)
                .orElseThrow(()->new RuntimeException("connection not found"));
        connection.setStatus(ConnectionStatus.CONNECTED);
        connectionRepositiry.save(connection);

        //publish connection.accepted event

        Map<String,Object> connectionAcceptEvent = new HashMap<>();
        connectionAcceptEvent.put("requesterId",connection.getRequesterId());
        connectionAcceptEvent.put("recieverId",connection.getReceiverId());
        kafkaTemplate.send(CONNECTION_ACCEPTED_TOPIC,connection.getRequesterId(),connectionAcceptEvent);

        log.info("connection accepted to connectionId={}",connectionId);
        return "Connection accepted";


    }

    public List<UserResponse> getConnections(String userId){
       List<Connection> connections =  connectionRepositiry.findByRequesterIdAndStatus(userId , ConnectionStatus.CONNECTED);

       return connections.stream().map(c->getUserProfile(c.getReceiverId())).collect(Collectors.toList());
    }

    public UserResponse getUserProfile(String userId){

        User user = userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("user not found"));

        return mapToResponse(user);


    }


    public UserResponse uploadProfilePhoto(String userId , MultipartFile file ){

        User user = userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("user not found"));
        String photourl  = S3Service.uploadFile(
                file , "profiles/"+userId+"/avatar");
        user.setProfilePhotoUrl(photourl);
        User savedUser = userRepository.save(user);

        log.info("user profile photo uploaded successfully");
        return mapToResponse(savedUser);
    }

    private UserResponse mapToResponse(User user){
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setHeadline(user.getHeadline());
        userResponse.setAbout(user.getAbout());
        userResponse.setLocation(user.getLocation());
        userResponse.setProfilePhotoUrl(user.getProfilePhotoUrl());
        userResponse.setCoverPhotoUrl(user.getCoverPhotoUrl());
        userResponse.setRole(user.getRole());
        userResponse.setSkills(user.getSkills());
        userResponse.setCreatedAt(user.getCreatedAt());

        return userResponse;

    }

    public UserResponse updateProfile(String userId ,UserResponse request){
        User user = userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("user not found"));

        user.setHeadline(request.getHeadline());
        user.setAbout(request.getAbout());
        user.setLocation(request.getLocation());
        user.setSkills(request.getSkills());

        User savedUser = userRepository.save(user);

        // publish user.updated event

        Map<String, Object> userUpdatedEvent = new HashMap<>();
        userUpdatedEvent.put("userId", savedUser.getId());
        userUpdatedEvent.put("firstName", savedUser.getFirstName());
        userUpdatedEvent.put("LastName", savedUser.getLastName());
        userUpdatedEvent.put("headline", savedUser.getHeadline());
        userUpdatedEvent.put("location", savedUser.getLocation());
        userUpdatedEvent.put("skills", savedUser.getSkills());

        kafkaTemplate.send(USER_UPDATED_TOPIC,savedUser.getId(),userUpdatedEvent);

        log.info("user update event publish userId={}",userId);

        return mapToResponse(savedUser);
    }




}
