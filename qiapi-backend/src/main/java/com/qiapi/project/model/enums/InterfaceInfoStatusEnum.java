package com.qiapi.project.model.enums;


/**
 * @author zhexueqi
 * @ClassName InterfaceInfoStatusEnum
 * @since 2024/8/5    16:39
 */
public enum InterfaceInfoStatusEnum {
    OFFLINE("下线", 0),
    ONLINE("上线", 1);

    private final String text;

    private final int value;

    InterfaceInfoStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
