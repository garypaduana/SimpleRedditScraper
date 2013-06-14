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
