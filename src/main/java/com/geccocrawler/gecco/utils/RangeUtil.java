package com.geccocrawler.gecco.utils;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author ztcaoll222
 * Create time: 2018/12/25 9:20
 */
public class RangeUtil {
    private static final boolean DEFAULT_SYNC = true;

    private static final int DEFAULT_STEP = 1;

    /**
     * the {@code Supplier} of generated elements
     *
     * @param start 计数从 start 开始
     * @param end   计数到 stop 结束
     * @param step  步长
     */
    public static Supplier<Integer> generateSeed(final Integer start, final Integer end, final Integer step) {
        return new Supplier<Integer>() {
            private Integer next = start;

            @Override
            public Integer get() {
                Integer old = this.next;
                Integer nw = old + step;
                if (nw > end) {
                    next = end;
                } else {
                    next = nw;
                }
                return old;
            }
        };
    }

    public static Stream<Integer> rangeInt(Supplier<Integer> seed, Integer length, boolean sync) {
        return sync ? Stream.generate(seed).limit(length) : Stream.generate(seed).parallel().limit(length);
    }

    /**
     * 生成某个范围内的数组列表
     *
     * @param start 计数从 start 开始
     * @param end   计数到 stop 结束
     * @param step  步长
     * @param sync  是否为同步
     *              <p>
     *              {@<code>
     *              >> rangeInt(0, 35, 20, false).forEach(System.out : : println)
     *              <p>
     *              >> 0, 20
     *              </code>}
     */
    public static Stream<Integer> rangeInt(final Integer start, final Integer end, final Integer step, boolean sync) {
        return rangeInt(generateSeed(start, end, step), (int) Math.ceil((end - start) * 1.0 / step), sync);
    }

    /**
     * 生成同步的某个范围内的数组列表
     *
     * @param start 计数从 start 开始
     * @param end   计数到 stop 结束
     * @param step  步长
     *              <p>
     *              {@<code>
     *              >> rangeInt(0, 35, 20).forEach(System.out : : println)
     *              <p>
     *              >> 0, 20
     *              </code>}
     */
    public static Stream<Integer> rangeInt(final Integer start, final Integer end, final Integer step) {
        return rangeInt(generateSeed(start, end, step), (int) Math.ceil((end - start) * 1.0 / step), DEFAULT_SYNC);
    }

    /**
     * 生成同步的步长为 1 的某个范围内的数组列表
     *
     * @param start 计数从 start 开始
     * @param end   计数到 stop 结束
     *              <p>
     *              {@<code>
     *              >> rangeInt(0, 35).forEach(System.out : : println)
     *              <p>
     *              >> 0, 1, 2....
     *              </code>}
     */
    public static Stream<Integer> rangeInt(final Integer start, final Integer end) {
        return rangeInt(start, end, DEFAULT_STEP, DEFAULT_SYNC);
    }
}
