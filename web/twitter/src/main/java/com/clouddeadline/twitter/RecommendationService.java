package com.clouddeadline.twitter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

@Service
public class RecommendationService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public String recommend(String type, String id, String phrase, String hashtag) {
		try {
			BigInteger user = new BigInteger(id);
			hashtag = hashtag.toLowerCase(Locale.ENGLISH);
			List<Map<String, Object>> posts = jdbcTemplate.queryForList("SELECT hashtags, created_at, id, type, text, user_id, interactor_id, score1, score2, user_description, user_screen_name, interactor_description, interactor_screen_name FROM posts WHERE user_id=? OR interactor_id=?", user, user);
			if (posts.size() == 0) {
				return "CloudDeadline,576651666672\nINVALID";
			}
			HashMap<BigInteger, String> screenName = new HashMap<>();
			HashMap<BigInteger, String> description = new HashMap<>();
			HashMap<BigInteger, String> lastPost = new HashMap<>();
			HashMap<BigInteger, Timestamp> lastTime = new HashMap<>();
			HashMap<BigInteger, BigInteger> lastPostId = new HashMap<>();
			HashMap<BigInteger, Double> score1 = new HashMap<>();
			HashMap<BigInteger, Double> score2 = new HashMap<>();
			HashMap<BigInteger, Double> score3 = new HashMap<>();
			for (Map<String, Object> row : posts) {
				if (!type.equals("both") && !row.get("type").toString().equals(type)) {
					continue;
				}
				BigInteger userId = new BigInteger(row.get("user_id").toString());
				BigInteger interactorId = new BigInteger(row.get("interactor_id").toString());
				BigInteger postId = new BigInteger(row.get("id").toString());
				String hashtags = row.get("hashtags").toString();
				String text = "";
				if (row.get("text") != null) {
					text = row.get("text").toString();
				}
				Timestamp time = Timestamp.valueOf(row.get("created_at").toString());
				if (userId.equals(user)) {
					// Update score 1 and score 2.
					score1.put(interactorId, (Double) row.get("score1"));
					if (interactorId.equals(user)) {
						score2.put(interactorId, 1.0);
					} else {
						score2.put(interactorId, (Double) row.get("score2"));
					}
					// Update screen name and description.
					String interactorName = "";
					String interactorDescription = "";
					if (row.get("interactor_screen_name") != null) {
						interactorName = row.get("interactor_screen_name").toString();
					}
					if (row.get("interactor_description") != null) {
						interactorDescription = row.get("interactor_description").toString();
					}
					screenName.put(interactorId, interactorName);
					description.put(interactorId, interactorDescription);
					// Update last post.
					if (lastPost.containsKey(interactorId)) {
						int comp = time.compareTo(lastTime.get(interactorId));
						if (comp > 0) {
							// time is greater.
							lastPost.put(interactorId, text);
							lastTime.put(interactorId, time);
							lastPostId.put(interactorId, postId);
						} else if (comp == 0) {
							if (postId.compareTo(lastPostId.get(interactorId)) > 0) {
								lastPost.put(interactorId, text);
								lastTime.put(interactorId, time);
								lastPostId.put(interactorId, postId);
							}
						}
					} else {
						lastPost.put(interactorId, text);
						lastTime.put(interactorId, time);
						lastPostId.put(interactorId, postId);
					}
					// update score 3.
					score3.put(interactorId, score3.getOrDefault(interactorId, 0.0) + countHashTag(hashtags, hashtag) + countKeywords(text, phrase));
				} else {
					// Update score 1 and score 2.
					score1.put(userId, (Double) row.get("score1"));
					if (userId.equals(user)) {
						score2.put(userId, 1.0);
					} else {
						score2.put(userId, (Double) row.get("score2"));
					}
					// Update screen name and description.
					String userDescription = "";
					String userName = "";
					if (row.get("user_screen_name") != null) {
						userName = row.get("user_screen_name").toString();
					}
					if (row.get("user_description") != null) {
						userDescription = row.get("user_description").toString();
					}
					screenName.put(userId, userName);
					description.put(userId, userDescription);
					// Update last post.
					if (lastPost.containsKey(userId)) {
						int comp = time.compareTo(lastTime.get(userId));
						if (comp > 0) {
							// time is greater.
							lastPost.put(userId, text);
							lastTime.put(userId, time);
							lastPostId.put(userId, postId);
						} else if (comp == 0) {
							if (postId.compareTo(lastPostId.get(userId)) > 0) {
								lastPost.put(userId, text);
								lastTime.put(userId, time);
								lastPostId.put(userId, postId);
							}
						}
					} else {
						lastPost.put(userId, text);
						lastTime.put(userId, time);
						lastPostId.put(userId, postId);
					}
					// update score 3.
					score3.put(userId, score3.getOrDefault(userId, 0.0) + countHashTag(hashtags, hashtag) + countKeywords(text, phrase));
				}
			}

			// Apply logarithm to score 3.
			for (BigInteger key : score3.keySet()) {
				score3.put(key, 1 + Math.log(1 + score3.get(key)));
			}

			// Calculate total score.
			HashMap<BigInteger, Double> totalScore = new HashMap<>();
			for (BigInteger key : score3.keySet()) {
				totalScore.put(key, score1.get(key) * score2.get(key) * score3.get(key));
			}

			// Generate response.
			// <TEAMID>,<TEAM_AWS_ACCOUNT_ID>\n
			//	user_id_1\tscreen_name_1\tdescription_1\tcontact_tweet_text\n
			//	user_id_2\tscreen_name_2\tdescription_2\tcontact_tweet_text
			List<BigInteger> responseUsers = totalScore.keySet().stream().sorted(new Comparator<BigInteger>() {
				@Override
				public int compare(BigInteger o1, BigInteger o2) {
					if (totalScore.get(o1).equals(totalScore.get(o2))) {
						return o2.compareTo(o1);
					} else {
						return totalScore.get(o2).compareTo(totalScore.get(o1));
					}
				}
			}).toList();
			StringBuilder sb = new StringBuilder();
			sb.append("CloudDeadline,576651666672");
			for(int i = 0; i < responseUsers.size(); i++) {
				sb.append("\n");
				sb.append(responseUsers.get(i));
				sb.append("\t");
				sb.append(screenName.getOrDefault(responseUsers.get(i), ""));
				sb.append("\t");
				sb.append(description.getOrDefault(responseUsers.get(i), ""));
				sb.append("\t");
				// append last post.
				sb.append(lastPost.get(responseUsers.get(i)));
			}

			return sb.toString();
		} catch(Exception e) {
			System.out.println(e);
			return "CloudDeadline,576651666672\nINVALID";
		}
	}

	private int countHashTag(String hashtags, String matchTag) {
		int count = 0;
		String[] hashtagsArray = hashtags.split(",", 0);
		for (String hashtag : hashtagsArray) {
			if (hashtag.toLowerCase().equals(matchTag)) {
				count += 1;
			}
		}
		return count;
	}

	private int countKeywords(String text, String phrase) {
		return StringUtils.countMatches(text, phrase);
	}
}