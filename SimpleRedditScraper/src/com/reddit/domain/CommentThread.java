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

package com.reddit.domain;

import java.util.ArrayList;
import java.util.List;

public class CommentThread {
	
	private List<Comment> comments = new ArrayList<Comment>();
	private String subreddit;
	private long upvotes;
	private long downvotes;
	private long timestamp;
	private String selftext;
	private String author;
	private String url;
	private boolean over18;
	private String title;
	private String nameHash;
	private String id;
	
	public CommentThread(){
		
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public String getSubreddit() {
		return subreddit;
	}

	public void setSubreddit(String subreddit) {
		this.subreddit = subreddit;
	}

	public long getUpvotes() {
		return upvotes;
	}

	public void setUpvotes(long upvotes) {
		this.upvotes = upvotes;
	}

	public long getDownvotes() {
		return downvotes;
	}

	public void setDownvotes(long downvotes) {
		this.downvotes = downvotes;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getSelftext() {
		return selftext;
	}

	public void setSelftext(String selftext) {
		this.selftext = selftext;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isOver18() {
		return over18;
	}

	public void setOver18(boolean over18) {
		this.over18 = over18;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getNameHash() {
		return nameHash;
	}

	public void setNameHash(String nameHash) {
		this.nameHash = nameHash;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
