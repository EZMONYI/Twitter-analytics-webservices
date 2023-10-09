package com.clouddeadline.twitter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TwitterController {
	@Autowired
	RecommendationService service;

	@GetMapping("/twitter")
	public String recommend(@RequestParam String type, @RequestParam String user_id, @RequestParam String phrase, @RequestParam String hashtag) {
		if(type.equals("reply") || type.equals("retweet") || type.equals("both")){
			return service.recommend(type, user_id, phrase, hashtag);
		}
		return "CloudDeadline,576651666672\nINVALID";
	}
}
