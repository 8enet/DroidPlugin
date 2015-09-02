package com.morgoo.droidplugin.hook.proxy;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.morgoo.droidplugin.hook.BaseHookHandle;
import com.morgoo.droidplugin.hook.handle.HttpHookHandle;
import com.morgoo.droidplugin.reflect.FieldUtils;
import com.morgoo.droidplugin.reflect.MethodUtils;
import com.morgoo.droidplugin.reflect.Utils;
import com.morgoo.helper.MyProxy;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
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
        install("http");
        //install("https");
    }

    private void install(String type) throws Exception{
        Object streamHandlers = FieldUtils.readStaticField(URL.class, "streamHandlers");
        MyURLStreamHandler streamHandler = new MyURLStreamHandler(type);
        setOldObj(streamHandler.mInterface);
        List<Class<?>> interfaces = Utils.getAllInterfaces(mOldObj.getClass());
        Class<?>[] ifs = interfaces != null && interfaces.size() > 0 ? interfaces.toArray(new Class[interfaces.size()]) : new Class[0];
        HttpHooKInterface proxyObj = (HttpHooKInterface) MyProxy.newProxyInstance(HttpHooKInterface.class.getClassLoader(), ifs, this);
        streamHandler.setInterface(proxyObj);
        MethodUtils.invokeMethod(streamHandlers, "put", type, streamHandler);

        Log.e(TAG, "onInstall -->>Http success ___    " + type + "      " + proxyObj);
    }

    private static class MyURLStreamHandler extends URLStreamHandler{
        private int port;
        HttpHooKInterface mInterface;
        MyURLStreamHandler(String type) throws Exception{
            if("http".equals(type)){
                port=80;
                mInterface=new InternalHttpHooKInterface(getHandlerClassName(true));
            }else if("https".equals(type)){
                port=443;
                mInterface=new InternalHttpHooKInterface(getHandlerClassName(false));
            }
        }

        private String getHandlerClassName(boolean http){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                return http?"com.android.okhttp.HttpHandler":"com.android.okhttp.HttpsHandler";
            }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
                return http?"libcore.net.http.HttpHandler":"libcore.net.http.HttpsHandler";
            }else {
                throw new UnsupportedClassVersionError("don't support sdk < 2.3");
            }
        }

        public void setInterface(HttpHooKInterface _interface) {
            this.mInterface = _interface;
        }

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return mInterface.openConnection(u);
        }

        @Override
        protected URLConnection openConnection(URL u, Proxy proxy) throws IOException {
            return mInterface.openConnection(u, proxy);
        }

        @Override
        protected int getDefaultPort() {
            return port;
        }

    }

    private interface HttpHooKInterface{
        URLConnection openConnection(URL u);
        URLConnection openConnection(URL u, Proxy proxy);
    }

    private static class InternalHttpHooKInterface implements HttpHooKInterface{
        private Method openConnectionMethod;
        private Method OpenConnectionMethod2;
        private Object mHttpHandler;

        InternalHttpHooKInterface(String clsName) throws Exception {
            Class cls = Class.forName(clsName);
            mHttpHandler = cls.newInstance();
            openConnectionMethod = cls.getDeclaredMethod("openConnection", URL.class);
            OpenConnectionMethod2 = cls.getDeclaredMethod("openConnection", URL.class, Proxy.class);
            openConnectionMethod.setAccessible(true);
            OpenConnectionMethod2.setAccessible(true);
        }

        @Override
        public URLConnection openConnection(URL u) {
            try{
               return (URLConnection) openConnectionMethod.invoke(mHttpHandler,u);
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public URLConnection openConnection(URL u, Proxy proxy) {
            try{
               return (URLConnection) OpenConnectionMethod2.invoke(mHttpHandler,u,proxy);
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }
}
