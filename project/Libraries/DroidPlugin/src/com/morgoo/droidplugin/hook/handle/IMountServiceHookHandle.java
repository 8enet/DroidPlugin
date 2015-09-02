/*
**        DroidPlugin Project
**
** Copyright(c) 2015 Andy Zhang <zhangyong232@gmail.com>
**
** This file is part of DroidPlugin.
**
** DroidPlugin is free software: you can redistribute it and/or
** modify it under the terms of the GNU Lesser General Public
** License as published by the Free Software Foundation, either
** version 3 of the License, or (at your option) any later version.
**
** DroidPlugin is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
** Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public
** License along with DroidPlugin.  If not, see <http://www.gnu.org/licenses/lgpl.txt>
**
**/

package com.morgoo.droidplugin.hook.handle;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.text.TextUtils;

import com.morgoo.droidplugin.hook.BaseHookHandle;
import com.morgoo.droidplugin.hook.HookedMethodHandler;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Created by Andy Zhang(zhangyong232@gmail.com) on 2015/3/6.
 */
public class IMountServiceHookHandle extends BaseHookHandle {
    private static final String TAG = "IMountServiceHookHandle";
    private static final String A_DATA_KEY="Android/data/";

    public IMountServiceHookHandle(Context context) {
        super(context);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("mkdirs", new mkdirs(mHostContext));
    }

    private class mkdirs extends HookedMethodHandler {
        public mkdirs(Context context) {
            super(context);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            final int pkgIndex = 0;
            int pathIndex = 1;
            if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
                if (args != null && args.length > pkgIndex && args[pkgIndex] instanceof String) {
                    String oldPkg = (String) args[pkgIndex];
                    if (!TextUtils.equals(oldPkg, mHostContext.getPackageName())) {
                        args[pkgIndex] = mHostContext.getPackageName();
                    }
                }
            } else {
                pathIndex=0;
            }

            if (args != null && args.length > pathIndex && args[pathIndex] instanceof String) {
                String path = (String) args[pathIndex];
                String path1 = new File(Environment.getExternalStorageDirectory(), A_DATA_KEY).getPath();

                //前缀相同的可以直接替换
                if (path.startsWith(path1)) {
                    path = path.replace(A_DATA_KEY, A_DATA_KEY+mHostContext.getPackageName()+"/Plugin/");
                    args[pathIndex] = path;
                }else {
                    //不相同就拼接相对路径
                    //前缀有可能是/storage/sdcard1 、/storage/emulated/0 、/sdcard 但对应的位置确是一样的，
                    //有时候不同方式得到的并不相同，所以截取后面的相对路径
                    String oldRelaPath=findOldPath(path);
                    if(oldRelaPath != null)
                    args[pathIndex]=path1+"/"+mHostContext.getPackageName()+"/Plugin/"+oldRelaPath;
                }
            }
            return super.beforeInvoke(receiver, method, args);
        }

        private String findOldPath(String path){
            int idx=path.indexOf(A_DATA_KEY);
            if(idx != -1){
                return path.substring(idx+A_DATA_KEY.length());
            }
            return null;
        }
    }
}
