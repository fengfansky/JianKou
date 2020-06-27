package com.arrownock.social;

import java.io.InputStream;

public class AnSocialFile {
    private String fileName;
    private InputStream inputStream;
    private byte[] data;

    public AnSocialFile(String fileName, InputStream inputStream) {
        this.fileName = fileName;
        this.inputStream = inputStream;
    }

    public AnSocialFile(String fileName, byte[] data) {
        this.fileName = fileName;
        this.data = data;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
