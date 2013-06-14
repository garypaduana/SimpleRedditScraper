package com.reddit;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.reddit.domain.CommentThread;
import com.reddit.util.ApiUtil;

public class RedditScraperBot {

	public static final int MINIMUM_TIME_BETWEEN_REQUESTS_IN_MS = 3000;
	public static final boolean DO_NOT_RECURSE_COMMENT_TREE = false;
	public static final boolean RETRIEVE_ALL_COMMENTS = true;
	
	public static void main(String[] args) throws IOException, JSONException, InterruptedException {

		ApiUtil apiUtil = new ApiUtil(MINIMUM_TIME_BETWEEN_REQUESTS_IN_MS);
		
		List<String> subreddits = Arrays.asList("pics", "AskReddit", "funny");
		
		for(String subreddit : subreddits){
			System.out.println("########################################");
			System.out.println("#######  Begin parsing for: " + subreddit);
			System.out.println("########################################");
			
			// Return 50 threads from the front page of this subreddit
			JSONObject redditObject = new JSONObject(apiUtil.getPage("http://www.reddit.com/r/" + subreddit + ".json?limit=50"));
			
			JSONArray children = redditObject.getJSONObject("data").getJSONArray("children");
			for(int i = 0; i < children.length(); i++){
				try{
					String threadId = children.getJSONObject(i).getJSONObject("data").getString("id");

					// Request up to 500 comments for this thread
					JSONArray jsonArray = new JSONArray(apiUtil.getPage("http://www.reddit.com/comments/" + threadId + ".json?limit=500"));
					
					// Change 2nd argument to RETRIEVE_ALL_COMMENTS if you want to fully construct this
					// commentThread object.  Warning: it can take some time since we respect the 3 seconds
					// between requests API rule and a large thread requires many requests.
					CommentThread commentThread = apiUtil.parseJSON(jsonArray, DO_NOT_RECURSE_COMMENT_TREE);
					
					// Do something with the comment thread object
					// Displaying meta data for lack of a better objective 
					System.out.println("Author: " + commentThread.getAuthor() + ", Title: " + commentThread.getTitle() +
						", Upvotes: " + commentThread.getUpvotes() + ", Downvotes: " + commentThread.getDownvotes() + 
						", comments retrieved: " + commentThread.getComments().size());
				}
				catch(IOException ioException){
					ioException.printStackTrace();
				}
				catch(JSONException jsonException){
					jsonException.printStackTrace();
				}
			}
		}
		apiUtil.getTimer().cancel();
	}

}
