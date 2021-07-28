package com.example.orangepi.me;

import java.io.Serializable;

public class Mail implements Serializable,Cloneable {
    public Object msg;
    public TYPE type;
    public enum TYPE{
        MESSAGE,BYE,USER,STR
    }
    private static final long serialVersionUID=10000L;

    public Mail(TYPE type, Object msg) {
        this.msg = msg;
        this.type = type;
    }
}
