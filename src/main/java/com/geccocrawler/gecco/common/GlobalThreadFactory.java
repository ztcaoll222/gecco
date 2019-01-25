package com.geccocrawler.gecco.common;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import javax.annotation.Nonnull;
import java.util.concurrent.*;

/**
 * @author ztcaoll222
 * Create time: 2019/1/25 9:38
 */
public enum GlobalThreadFactory {
    /**
     * 实例
     */
    INSTANCE;

    private ExecutorService singleThreadPool;

    GlobalThreadFactory() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("global-thread-pool-%d").build();
        singleThreadPool = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors() + 1,
                Runtime.getRuntime().availableProcessors() * 2 + 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy()
        );
    }

    public void execute(@Nonnull Runnable command) {
        singleThreadPool.execute(command);
    }

    public void shutdown() {
        singleThreadPool.shutdown();
    }
}
