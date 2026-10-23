package com.linkedin.searchservice.controller;


import com.linkedin.searchservice.model.PostDocument;
import com.linkedin.searchservice.model.UserDocument;
import com.linkedin.searchservice.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/api/v1/search")
@Slf4j
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;


    /**
     * search people by  name , health  or locaation
     *
     */
    @GetMapping("/people")
    public ResponseEntity <List<UserDocument>> searchPeople(
            @RequestParam String q
    )
    {
        return ResponseEntity.ok(searchService.searchUser(q));
    }



    @GetMapping("/skills")
    public ResponseEntity <List<UserDocument>> searchBySkill(
            @RequestParam String skill
    )
    {
        return ResponseEntity.ok(searchService.searchBySkill(skill));
    }

    @GetMapping("/posts")
    public ResponseEntity <List<PostDocument>> searchPosts(
            @RequestParam String q
    )
    {
        return ResponseEntity.ok(searchService.searchPosts(q));
    }


}
