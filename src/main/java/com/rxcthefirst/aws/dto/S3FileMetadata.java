package com.rxcthefirst.aws.dto;

public class S3FileMetadata {
    private String key;
    private String author;
    private String sharedWith;

    // Constructors
    public S3FileMetadata() {}

    public S3FileMetadata(String key, String author, String sharedWith) {
        this.key = key;
        this.author = author;
        this.sharedWith = sharedWith;
    }

    // Getters & Setters
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getSharedWith() { return sharedWith; }
    public void setSharedWith(String sharedWith) { this.sharedWith = sharedWith; }
}