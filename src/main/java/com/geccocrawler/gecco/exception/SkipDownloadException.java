package com.geccocrawler.gecco.exception;

/**
 * @author ztcaoll222
 * Create time: 2019/2/22 9:50
 */
public class SkipDownloadException extends DownloadException {
    private static final long serialVersionUID = -1918322266206245528L;

    public SkipDownloadException(Throwable cause) {
        super(cause);
    }

    public SkipDownloadException(String message) {
        super(message);
    }
}
