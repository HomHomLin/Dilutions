package com.linhonghong.dilutions;

/**
 * Created by Linhh on 16/12/1.
 */

public interface DilutionsManager<T> {

    public void apply() throws Exception;

    public DilutionsBuilder getDilutionsBuilder();

    public ProtocolClazzORMethod getProtocolClazzORMethod();

    public int getProtocolType();
}
