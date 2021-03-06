package com.rtcvoice;

import com.rtcvoice.RTMStruct.RTMAnswer;

public class UserInterface {

    //返回RTMAnswer的回调接口
    public interface IRTMEmptyCallback {
        void onResult(RTMAnswer answer);
    }

    //泛型接口 带有一个返回值的回调函数 (请优先判断answer的错误码 泛型值有可能为null)
    public interface IRTMCallback<T> {
        void onResult(T t, RTMAnswer answer);
    }

    //泛型接口 带有两个返回值的回调函数 (请优先判断answer的错误码, 泛型值有可能为null)
    public interface IRTMDoubleValueCallback<T,V> {
        void onResult(T t, V v, RTMAnswer answer);
    }
}
