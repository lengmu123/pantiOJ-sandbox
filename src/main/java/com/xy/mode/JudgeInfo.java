package com.xy.mode;

import lombok.Data;

import java.util.List;

/**
 * 判题信息
 */
@Data
public class JudgeInfo {
    /**
     * 程序执行信息
     */
    private String  message;

    /**
     * 消耗内存(KB)
     */
    private Long memory;
    /**
     * 消耗时间(KB)
     */
    private Long time;
//    private List<Long> memoryList;


}
