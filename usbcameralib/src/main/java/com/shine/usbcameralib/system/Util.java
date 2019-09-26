package com.shine.usbcameralib.system;


import com.shine.usbcameralib.serialdog.serialutil.LogPlus;
import com.shine.utilitylib.A64Utility;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * 工具类使用总类
 *
 * @author GP
 * @date 2016/8/18.
 */
public enum Util {

    // 建一个实例
    INSTANCE;

    public int lightSleep = 30;
    /**线程池*/
    private LinkedBlockingDeque<Runnable> runNAbles = new LinkedBlockingDeque<>(20);
    public ExecutorService threadPool = new ThreadPoolExecutor(2, 5, 10,
            TimeUnit.SECONDS,
            runNAbles,
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    LogPlus.i("线程池添加失败:CorePoolSize:" + executor.getCorePoolSize()
                            + "|PoolSize" + executor.getPoolSize());
                    LogPlus.i("=======runNAbles:" + runNAbles.size() + "=======");
                    new Thread(r).start();
                }
            }
    );

    public void openLight() {
        Flowable.just("A64openLight")
                .observeOn(Schedulers.newThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        LogPlus.i(s + "---" + Thread.currentThread().getId());
                        A64Utility mA64Utility = new  A64Utility();
                        mA64Utility.SetLameValue(1, 0);
                        mA64Utility.SetLameValue(2, 0);
                        mA64Utility.SetLameValue(3, 0);
                        mA64Utility.SetLameValue(4, 0);
                    }
                });
    }

    public void closeLight() {
        Flowable.just("A64closeLight")
                .observeOn(Schedulers.newThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        LogPlus.i(s + "---" + Thread.currentThread().getId());
                         A64Utility mA64Utility = new  A64Utility();
                        mA64Utility.SetLameValue(1, 1);
                        mA64Utility.SetLameValue(2, 1);
                        mA64Utility.SetLameValue(3, 1);
                        mA64Utility.SetLameValue(4, 1);
                    }
                });
    }
}
