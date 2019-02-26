package com.geccocrawler.gecco.exception;

/**
 * @author ztcaoll222
 * Create time: 2019/2/19 14:37
 */
public class AfterDownloadException extends DownloadException {
    private static final long serialVersionUID = 5853859421810292667L;

    public AfterDownloadException(Throwable cause) {
        super(cause);
    }

    public AfterDownloadException(String message) {
        super(message);
    }
}
