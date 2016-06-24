/*
    Simple Reddit Scraper
    Copyright (C) 2012-2013, Gary Paduana, gary.paduana@gmail.com
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.reddit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.reddit.domain.Comment;
import com.reddit.domain.CommentThread;
import com.reddit.util.ApiUtil;

public class RedditScraperBot {

	public static final int MINIMUM_TIME_BETWEEN_REQUESTS_IN_MS = 3000;
	public static final boolean DO_NOT_RECURSE_COMMENT_TREE = false;
	public static final boolean RETRIEVE_ALL_COMMENTS = true;
	
	public static void main(String[] args) throws IOException, JSONException, InterruptedException {

		ApiUtil apiUtil = new ApiUtil(MINIMUM_TIME_BETWEEN_REQUESTS_IN_MS);
		
		List<String> subreddits = Arrays.asList("AskReddit");
		
		for(String subreddit : subreddits){
			System.out.println("########################################");
			System.out.println("#######  Begin parsing for: " + subreddit);
			System.out.println("########################################");
			
			// Return 50 threads from the front page of this subreddit
			JSONObject redditObject = new JSONObject(apiUtil.getPage("http://www.reddit.com/r/" + subreddit + ".json?limit=10"));
			List<String> comments = new ArrayList<String>();
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
//					System.out.println("Author: " + commentThread.getAuthor() + ", Title: " + commentThread.getTitle() +
//						", Upvotes: " + commentThread.getUpvotes() + ", Downvotes: " + commentThread.getDownvotes() + 
//						", comments retrieved: " + commentThread.getComments().size());
					
					comments.addAll(commentThread.getComments().stream().map((c) -> c.getContents()).collect(Collectors.toList()));
				}
				catch(IOException ioException){
					ioException.printStackTrace();
				}
				catch(JSONException jsonException){
					jsonException.printStackTrace();
				}
			}
			
			JSONObject output = new JSONObject();
			output.put("comments", comments);
			
			File outFile = new File("/Users/garypaduana/Documents/GitHub/helen/selanimate/src/main/resources/comments.json");
			
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))){
			    bw.write(output.toString(4));
			}
			
			System.out.println(comments.size());
			
		}
		apiUtil.getTimer().cancel();
	}

}
