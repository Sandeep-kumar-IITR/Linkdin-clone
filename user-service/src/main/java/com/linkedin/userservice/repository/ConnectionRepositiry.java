package com.linkedin.userservice.repository;

import com.linkedin.userservice.entity.Connection;
import com.linkedin.userservice.entity.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConnectionRepositiry extends JpaRepository<Connection, String> {
     boolean existByRequesterIdAndRecieverId(String  requesterId,String recieverId);

     List<Connection> findByRequesterIdAndStatus(String requesterId, ConnectionStatus status);
}
