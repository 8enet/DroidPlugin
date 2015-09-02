package com.morgoo.droidplugin.hook.handle;

import android.content.Context;
import android.util.Log;

import com.morgoo.droidplugin.hook.BaseHookHandle;
import com.morgoo.droidplugin.hook.HookedMethodHandler;
import com.morgoo.droidplugin.hook.proxy.ProxyHook;

import java.lang.reflect.Method;

/**
 * Created by zl on 15/9/2.
 */
public class HttpHookHandle extends BaseHookHandle {
    private static final String TAG = "HttpHookHandle";
    
    public HttpHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("openConnection", new openConnection(mHostContext));
    }

    private static class openConnection extends HookedMethodHandler{

        public openConnection(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            Log.e(TAG, "HttpHookHandle  beforeInvoke -->> " + method.getName() + " -->     <-- args " + ProxyHook.arraysToString(args));
            return super.beforeInvoke(receiver, method, args);
        }
    }
}
