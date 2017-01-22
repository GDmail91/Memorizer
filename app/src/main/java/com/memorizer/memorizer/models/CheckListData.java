package com.memorizer.memorizer.models;

import java.io.Serializable;

/**
 * Created by YS on 2017-01-23.
 */

public class CheckListData implements Serializable {
    boolean isCheck = false;
    String checkMessage = "";

    public CheckListData() {}

    public CheckListData(boolean isCheck, String checkMessage) {
        this.isCheck = isCheck;
        this.checkMessage = checkMessage;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public String getCheckMessage() {
        return checkMessage;
    }

    public void setCheckMessage(String checkMessage) {
        this.checkMessage = checkMessage;
    }

    public String toString() {
        return checkMessage + " : "+isCheck;
    }
}
