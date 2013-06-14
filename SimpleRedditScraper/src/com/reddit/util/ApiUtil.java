package com.reddit.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.reddit.RedditScraperBot;
import com.reddit.domain.Comment;
import com.reddit.domain.CommentThread;

public class ApiUtil {
	
	private Timer timer = new Timer();
	private boolean delayedSufficiently = false;
	private long totalRequests = 0;
	private long start;
	Set<String> processedCommentIds = new HashSet<String>();
	private String modHash = null;
	private String cookie = null;
	
	/**
	 * 
	 * @param delay - time in milliseconds to wait between requests
	 */
	public ApiUtil(long delay){
		timer.schedule(new TimerTask(){

			@Override
			public void run() {
				delayedSufficiently = true;
			}
		
		}, new Date(), delay);
		start = System.currentTimeMillis();
	}
	
	/**
	 * Blocks the caller until it is acceptable to make a request based on the initial
	 * construction of this class.
	 * 
	 * @param urlString
	 * @throws InterruptedException
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws JSONException
	 */
	private void possibleDelay(String urlString) throws InterruptedException, MalformedURLException, IOException, JSONException{
		while(delayedSufficiently == false){
			// Requests to reddit do not happen exactly every 3000ms.  
			// Keep trying a new request every 1 second until acceptable.
			Thread.sleep(1000);
		}
		totalRequests++;	
		System.out.println("\r\n## Request: " + totalRequests + ", Elapsed time: " + displayTime(System.currentTimeMillis() - start) + ", Currently fetching " + urlString);
		delayedSufficiently = false;
	}
	
	/**
	 * Prints milliseconds as hours:minutes:seconds.milliseconds
	 * @param ms
	 * @return
	 */
	public static String displayTime(long ms){
	    long hr = ms / (3600 * 1000);
	    ms = ms - (hr * 3600 * 1000);
	    
	    long min = ms / (60 * 1000);
	    ms = ms - (min * 60 * 1000);
	    
	    long sec = ms / 1000;
	    ms = ms - (sec * 1000);
	    
	    String mss = Long.toString(ms);
	    while(mss.length() < 3){
	        mss = "0" + mss;
	    }
	    
	    return (hr < 10 ? ("0" + Long.toString(hr)) : hr) +
	           ":" + 
	           (min < 10 ? ("0" + Long.toString(min)) : min) +
	           ":" +
	           (sec < 10 ? ("0" + Long.toString(sec)) : sec) +
	           "." + mss;          
	}
	
	/**
	 * Retrieves the raw output from a url.
	 * 
	 * @param urlString
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public String getPage(String urlString) throws JSONException, IOException, InterruptedException{
		possibleDelay(urlString);
		
		URL url = new URL(urlString);
		InputStream is = null;
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		
		try{
		    is = url.openStream();
		    br = new BufferedReader(new InputStreamReader(is));
		    String line = null;
		    while ((line = br.readLine()) != null){
		        sb.append(line + "\r\n");
		    }
		}
		finally{
			try{
				if(is != null){
					is.close();
				}
		    } 
			catch (IOException ioe) {
		    
			}
		}
		return sb.toString();
	}
	
	/**
	 * Creates a CommentThread object from a properly constructed JSONArray derived from a reddit response.
	 * 
	 * @param threadJSON
	 * @param retrieveMoreComments if false, comments will not be recursively requested.
	 *  this is ideal to gain thread metrics. if true, all comments will be recursively
	 *  requested until the thread is completely consumed.  This takes a lot of time and
	 *  requests for large threads.
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public CommentThread parseJSON(JSONArray threadJSON, boolean retrieveMoreComments) throws JSONException, IOException, InterruptedException{
		CommentThread commentThread = new CommentThread();
		
		JSONObject listing = threadJSON.getJSONObject(0).getJSONObject("data").getJSONArray("children").getJSONObject(0).getJSONObject("data");
		commentThread.setDownvotes(listing.getLong("downs"));
		commentThread.setUpvotes(listing.getLong("ups"));
		commentThread.setSelftext(listing.getString("selftext"));
		commentThread.setSubreddit(listing.getString("subreddit"));
		commentThread.setTimestamp(listing.getLong("created_utc"));
		commentThread.setAuthor(listing.getString("author"));
		commentThread.setUrl(listing.getString("url"));
		commentThread.setOver18(listing.getBoolean("over_18"));
		commentThread.setTitle(listing.getString("title"));
		commentThread.setNameHash(listing.getString("name"));
		commentThread.setId(listing.getString("id"));
		
		JSONArray comments = threadJSON.getJSONObject(1).getJSONObject("data").getJSONArray("children");
		
		parseComments(comments, commentThread, retrieveMoreComments);
		
		return commentThread;
	}
	
	/**
	 * Digs through a JSONArray of comments and extracts relevant information in order to construct
	 * a CommentThread object.  There is a lot of assumed information in here based on the reddit api.
	 * 
	 * The comments are flattened and added to a List for this thread.  The tree is disregarded.  You 
	 * may need to provide your own implementation if a tree is desired.
	 * @param jsonComments
	 * @param commentThread
	 * @param retrieveMoreComments - whether or not to request more comments
	 * @throws JSONException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void parseComments(JSONArray jsonComments, CommentThread commentThread, boolean retrieveMoreComments)
			throws JSONException, IOException, InterruptedException{
		
		for(int i = 0; i < jsonComments.length(); i++){
			JSONObject data = jsonComments.getJSONObject(i).getJSONObject("data");
			String kind = jsonComments.getJSONObject(i).getString("kind");
			
			if(!kind.equals("more")){
				if(processedCommentIds.contains(data.getString("id"))){
					return;
				}
				
				parseComment(data, commentThread);
								
				if(data.optJSONObject("replies") != null){
					JSONArray root = data.getJSONObject("replies").getJSONObject("data").getJSONArray("children");
					for(int childrenIndex = 0; childrenIndex < root.length(); childrenIndex++){
						JSONObject childObject = root.getJSONObject(childrenIndex);
					
						if(childObject.getString("kind").equals("more")){
							JSONArray replies = childObject.getJSONObject("data").getJSONArray("children");
							for(int replyIndex = 0; replyIndex < replies.length(); replyIndex++){
								if(retrieveMoreComments){
									fetchMoreComments(commentThread.getId(), replies.getString(replyIndex), commentThread);
								}
							}
						}
						// "kind": "t1"
						else if(childObject.getJSONObject("data").optJSONObject("replies") != null){
							parseComment(childObject.getJSONObject("data"), commentThread);
							parseComments(childObject.getJSONObject("data").getJSONObject("replies").getJSONObject("data").getJSONArray("children"), commentThread, retrieveMoreComments);
						}
						// this IS the child comment; there are no replies
						else{
							parseComment(childObject.getJSONObject("data"), commentThread);
						}
					}
				}
			}
			else{
				JSONArray children = data.getJSONArray("children");
				for(int childIndex = 0; childIndex < children.length(); childIndex++){
					if(processedCommentIds.contains(children.getString(childIndex))){
						return;
					}
					if(retrieveMoreComments){
						fetchMoreComments(commentThread.getId(), children.getString(childIndex), commentThread);
					}
				}
			}
		}
	}
	
	/**
	 * Extracts a single comment from JSON and creates a Comment object.
	 * @param comment
	 * @param commentThread
	 * @throws JSONException
	 */
	public void parseComment(JSONObject comment, CommentThread commentThread) throws JSONException{
		Comment c = new Comment();
		c.setCommentThread(commentThread);
		c.setContents(comment.getString("body"));
		c.setDownvotes(comment.getLong("downs"));
		c.setUpvotes(comment.getLong("ups"));
		c.setTimestamp(comment.getLong("created_utc"));
		c.setAuthor(comment.getString("author"));
		c.setId(comment.getString("id"));
		
		processedCommentIds.add(c.getId());
		commentThread.getComments().add(c);
	}
	
	/**
	 * The JSON response indicated that more comments exist but they were not returned in this
	 * result.  Send another request for additional comments. There is potentially a lot of
	 * recursion going on here as we start fresh by requesting a new page, which will have
	 * comments, which may have additional comments that need to be fetched as well in a new request.
	 * 
	 * @param parentId
	 * @param commentId
	 * @param commentThread
	 * @throws JSONException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void fetchMoreComments(String parentId, String commentId, CommentThread commentThread)
			throws JSONException, IOException, InterruptedException{
		JSONArray commentJSON = null;
		boolean complete = false;
		while(!complete){
			try{
				commentJSON = new JSONArray(getPage("http://www.reddit.com/comments/" + parentId + "/_/" + commentId + ".json?limit=500"));
				complete = true;
			}
			// I found that this will often fail as reddit can be overloaded and may time out.
			// this will continue attempts until a result is achieved.
			catch(IOException e){
				Thread.sleep(RedditScraperBot.MINIMUM_TIME_BETWEEN_REQUESTS_IN_MS);
			}
		}
		parseComments(commentJSON.getJSONObject(1).getJSONObject("data").getJSONArray("children"), commentThread, true);
	}
		
	/**
	 * Experimental right now.  I messed around with this but never really used it for anything.
	 * 
	 * @param url should be new URL("https://ssl.reddit.com/api/login/myusername");
	 * @param user
	 * @param pw
	 * @throws IOException
	 * @throws JSONException
	 */
	public void login(URL url, String user, String pw) throws IOException, JSONException{

        String data = "api_type=json&user=" + user + "&passwd=" + pw;
        HttpURLConnection httpUrlConn = null;
        httpUrlConn = (HttpURLConnection) url.openConnection();
        httpUrlConn.setRequestMethod("POST");
        httpUrlConn.setDoOutput(true);
        httpUrlConn.setUseCaches(false);
        httpUrlConn.setRequestProperty("Content-Type",
            "application/x-www-form-urlencoded; charset=UTF-8");
        httpUrlConn.setRequestProperty("Content-Length", String.valueOf(data.length()));

        DataOutputStream dataOutputStream = new DataOutputStream(httpUrlConn.getOutputStream());
        dataOutputStream.writeBytes(data);
        dataOutputStream.flush();
        dataOutputStream.close();
        InputStream is = httpUrlConn.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuffer response = new StringBuffer();
        while ((line = bufferedReader.readLine()) != null){
            response.append(line);
            response.append('\r');
        }
        for(Entry<String, List<String>> r : httpUrlConn.getHeaderFields().entrySet()){
            System.out.println(r.getKey() + ": " + r.getValue());
        }
        bufferedReader.close();
        System.out.println("Response: " + response.toString());
        this.setModHash(new JSONObject(response.toString()).getJSONObject("json").getJSONObject("data").getString("modhash"));
        this.setCookie(new JSONObject(response.toString()).getJSONObject("json").getJSONObject("data").getString("cookie"));
        
    }

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public String getModHash() {
		return modHash;
	}

	public void setModHash(String modHash) {
		this.modHash = modHash;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}
}
