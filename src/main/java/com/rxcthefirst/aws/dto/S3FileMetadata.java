package com.rxcthefirst.aws.dto;

public class S3FileMetadata {
    private String key;
    private String author;
    private String sharedWith;
    private long size;
    private String lastModified;
    private String eTag;

    // Constructors
    public S3FileMetadata() {}

    public S3FileMetadata(String key, String author, String sharedWith, long size, String lastModified, String eTag) {
        this.key = key;
        this.author = author;
        this.sharedWith = sharedWith;
        this.size = size;
        this.lastModified = lastModified;
        this.eTag = eTag;
    }

    // Getters & Setters
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getSharedWith() { return sharedWith; }
    public void setSharedWith(String sharedWith) { this.sharedWith = sharedWith; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getLastModified() { return lastModified; }
    public void setLastModified(String lastModified) { this.lastModified = lastModified; }

    public String getETag() { return eTag; }
    public void setETag(String eTag) { this.eTag = eTag; }
}