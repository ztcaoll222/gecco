package com.geccocrawler.gecco.downloader;

import com.geccocrawler.gecco.exception.BeforeDownloadException;
import com.geccocrawler.gecco.exception.SkipDownloadException;
import com.geccocrawler.gecco.request.HttpRequest;

public interface BeforeDownload {
	
	public void process(HttpRequest request) throws BeforeDownloadException, SkipDownloadException;

}
