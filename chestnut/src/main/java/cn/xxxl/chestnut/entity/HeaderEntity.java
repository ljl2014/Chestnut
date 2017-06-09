package cn.xxxl.chestnut.entity;

import xiaofei.library.datastorage.annotation.ClassId;
import xiaofei.library.datastorage.annotation.ObjectId;

/**
 * @Description
 * @Author LeonUp
 * @Date 17-5-16.
 */
@ClassId("$CH")
public class HeaderEntity {

    @ObjectId
    private String key;
    private String value;

    public HeaderEntity(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
