package com.linkedin.searchservice.service;


import com.linkedin.searchservice.model.PostDocument;
import com.linkedin.searchservice.model.UserDocument;
import com.linkedin.searchservice.repository.PostSearchRepository;
import com.linkedin.searchservice.repository.UserSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {
    private final UserSearchRepository userSearchRepository;
    private final PostSearchRepository postSearchRepository;

    public List<UserDocument> searchUser(String query)
    {
        log.info("searching users: {}" , query);
        return userSearchRepository.searchUsers(query);

    }

    public List<UserDocument> searchBySkill(String skill)
    {
        log.info("searching users by skill : {}" , skill);
        return userSearchRepository.findBySkillContaining(skill);

    }

    public List<PostDocument> searchPosts(String query)
    {
        log.info("searching post by content : {}" , query);
        return postSearchRepository.searchPosts(query);

    }



}
