package org.schema.schine.common.util;

/**
 * Created by sms3h on 3/26/2017.
 */
public class ErrorMessage extends Object {

    private String msg;

    public ErrorMessage(String message) {
        this.msg = message;
    }

    @Override
    public String toString() {
        return this.msg;
    }
}
