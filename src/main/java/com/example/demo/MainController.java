package com.example.demo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class MainController {

    @Data
    class Post {

        @JsonProperty("post_id")
        Integer postId;

        @JsonProperty("post_title")
        String postTitle;

        @JsonProperty("post_body")
        String postBody;

        @JsonProperty("total_number_of_comments")
        Integer totalNumberOfComments = 0;

        Post(Integer postId, String postTitle, String postBody) {
            this.postId = postId;
            this.postTitle = postTitle;
            this.postBody = postBody;
        }
    }

    @Data
    @AllArgsConstructor
    class Comment {

        @JsonProperty("id")
        Integer id;

        @JsonProperty("post_id")
        Integer postId;

        @JsonProperty("name")
        String name;

        @JsonProperty("email")
        String email;

        @JsonProperty("body")
        String body;

    }

    @GetMapping("/getTopPosts")
    public List<Post> getTopPosts() {

        ResponseEntity<String> response = new RestTemplate().getForEntity("https://jsonplaceholder.typicode.com/posts", String.class);

        List<Post> list = new ArrayList<>();
        JSONArray body = new JSONArray(response.getBody());
        for (int i = 0; i < body.length(); i++) {
            JSONObject obj = body.getJSONObject(i);

            Integer post_id = Integer.valueOf(obj.getString("id"));
            String post_title = obj.getString("title");
            String post_body = obj.getString("body");

            list.add(new Post(post_id, post_title, post_body));
        }

        response = new RestTemplate().getForEntity("https://jsonplaceholder.typicode.com/comments", String.class);
        body = new JSONArray(response.getBody());
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < body.length(); i++) {
            JSONObject obj = body.getJSONObject(i);

            Integer post_id = Integer.valueOf(obj.getString("postId"));

            Integer value = map.get(post_id);
            if (value == null) {
                map.put(post_id, 1);
            } else {
                map.replace(post_id, value + 1);
            }
        }

        for (Post post : list) {
            Integer value = map.get(post.getPostId());
            if (value == null) {
                post.setTotalNumberOfComments(0);
            } else {
                post.setTotalNumberOfComments(value);
            }
        }

        list.sort(Comparator.comparing(Post::getTotalNumberOfComments).reversed());

        return list;

    }

    @PostMapping("/search")
    public List<Comment> search(
            @RequestParam(value = "postIds", required = false) List<Integer> postIds,
            @RequestParam(value = "ids", required = false) List<Integer> ids,
            @RequestParam(value = "nameSearchText", required = false) String nameSearchText,
            @RequestParam(value = "emailSearchText", required = false) String emailSearchText,
            @RequestParam(value = "bodySearchText", required = false) String bodySearchText
    ) {
        ResponseEntity<String> response = new RestTemplate().getForEntity("https://jsonplaceholder.typicode.com/comments", String.class);

        List<Comment> list = new ArrayList<>();
        JSONArray responseBody = new JSONArray(response.getBody());
        for (int i = 0; i < responseBody.length(); i++) {
            JSONObject obj = responseBody.getJSONObject(i);

            Integer postId = Integer.valueOf(obj.getString("postId"));
            Integer id = obj.getInt("id");
            String name = obj.getString("name");
            String email = obj.getString("email");
            String body = obj.getString("body");

            Boolean passFilter = true;

            if (postIds != null && !postIds.contains(postId)) {
                passFilter = false;
            }

            if (ids != null && !ids.contains(id)) {
                passFilter = false;
            }

            if (nameSearchText != null && !name.contains(nameSearchText)) {
                passFilter = false;
            }

            if (emailSearchText != null && !email.contains(emailSearchText)) {
                passFilter = false;
            }

            if (bodySearchText != null && !body.contains(bodySearchText)) {
                passFilter = false;
            }

            if (passFilter) {
                list.add(new Comment(id, postId, name, email, body));
            }
        }

        return list;

    }

}
