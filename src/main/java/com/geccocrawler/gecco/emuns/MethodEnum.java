package com.geccocrawler.gecco.emuns;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 请求方法枚举类
 *
 * @author yanglx
 * @CreateDate: 2018/10/12 15:06
 */
@Getter
public enum MethodEnum {
    /**
     * get 请求
     */
    GET(1),

    /**
     * post 请求
     */
    POST(2),

    /**
     * 其他
     */
    OTHER(0);

    private final int code;

    MethodEnum(int code) {
        this.code = code;
    }

    public static MethodEnum fromCode(Integer code) {
        return Arrays.stream(MethodEnum.values()).filter(it -> Objects.equals(code, it.getCode()))
                .findFirst()
                .orElse(OTHER);
    }
}
