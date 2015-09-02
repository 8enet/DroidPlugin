package com.morgoo.droidplugin.hook.proxy;

import android.content.Context;
import android.util.Log;

import com.morgoo.droidplugin.hook.BaseHookHandle;
import com.morgoo.droidplugin.hook.handle.HttpHookHandle;
import com.morgoo.droidplugin.reflect.FieldUtils;
import com.morgoo.droidplugin.reflect.MethodUtils;
import com.morgoo.droidplugin.reflect.Utils;
import com.morgoo.helper.MyProxy;

import java.net.URL;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by zl on 15/9/2.
 */
public class HttpHook extends ProxyHook {
    private static final String TAG = "HttpHook";
    
    public HttpHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new HttpHookHandle(mHostContext);
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Throwable {

        Object streamHandlers = FieldUtils.readStaticField(URL.class, "streamHandlers");
        Object streamHandler=null;
        if(streamHandlers instanceof Hashtable){
            streamHandler=((Hashtable)streamHandlers).get("http");
        }
        if(streamHandler != null) {
            setOldObj(streamHandler);
        }else {
            String name = "com.android.okhttp.HttpHandler";
            streamHandler = Class.forName(name).newInstance();
            setOldObj(streamHandler);
        }
        URL u=null;
        u.openConnection();
        URLStreamHandler uh=null;
        List<Class<?>> interfaces = Utils.getAllInterfaces(mOldObj.getClass());
        Class<?>[] ifs = interfaces != null && interfaces.size() > 0 ? interfaces.toArray(new Class[interfaces.size()]) : new Class[0];
        Object proxyObj = MyProxy.newProxyInstance(mOldObj.getClass().getClassLoader(), ifs, this);

        MethodUtils.invokeMethod(streamHandlers, "put", "http", proxyObj);

        Log.e(TAG, "onInstall -->> success ___ "+streamHandlers+"     "+proxyObj+"      "+streamHandler);
    }
}
