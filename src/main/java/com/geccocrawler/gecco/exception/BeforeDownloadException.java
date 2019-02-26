package com.geccocrawler.gecco.exception;

/**
 * @author ztcaoll222
 * Create time: 2019/2/19 14:33
 */
public class BeforeDownloadException extends DownloadException {
    private static final long serialVersionUID = 1993817792591700427L;

    public BeforeDownloadException(Throwable cause) {
        super(cause);
    }

    public BeforeDownloadException(String message) {
        super(message);
    }
}
