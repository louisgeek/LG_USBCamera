package com.shine.usbcameralib.serialdog.serialutil;


import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Rx通信类
 *
 * @author GP
 * @date 2016/9/9.
 */
public enum RxBus {

    INSTANCE;

    private final Subject<Object> bus = PublishSubject.create().toSerialized();

    // 发送一个新的事件
    public void post (Object o) {
        if (o != null){
            bus.onNext(o);
        }
    }

    // 根据传递的 eventType 类型返回特定类型(eventType)的 被观察者
    /*public <T> Observable<T> toObservable (Class<T> eventType) {
        return bus.ofType(eventType);
    }*/
}
