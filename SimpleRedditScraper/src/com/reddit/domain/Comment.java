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

public class Comment {

	private String contents;
	private long timestamp;
	private long upvotes;
	private long downvotes;
	private String author;
	private CommentThread commentThread;
	private String id;
	
	
	public Comment(){
		
	}

	public Comment(String contents, long timestamp, long upvotes, long downvotes,
			String author, CommentThread parent){
		this.contents = contents;
		this.timestamp = timestamp;
		this.upvotes = upvotes;
		this.downvotes = downvotes;
		this.author = author;
		this.commentThread = parent;
	}

	public String getContents() {
		return contents;
	}


	public void setContents(String contents) {
		this.contents = contents;
	}


	public long getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
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


	public String getAuthor() {
		return author;
	}


	public void setAuthor(String author) {
		this.author = author;
	}


	public CommentThread getCommentThread() {
		return commentThread;
	}


	public void setCommentThread(CommentThread commentThread) {
		this.commentThread = commentThread;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public String toString(){
		return author + ": " + contents;
	}
}
