package com.geccocrawler.gecco.exception;

/**
 * 网络请求请求方法异常
 *
 * @author ztcaoll222
 */
public class MethodException extends DownloadException {
    private static final long serialVersionUID = -9095236684428625480L;

    public MethodException(String message) {
        super(message);
    }
}
