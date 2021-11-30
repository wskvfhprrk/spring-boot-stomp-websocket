package com.hejz.springbootstomp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String content;
}
