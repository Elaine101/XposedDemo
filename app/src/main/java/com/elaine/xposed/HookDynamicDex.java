package com.elaine.xposed;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * 通过修改assest中的xpose_init为com.elaine.xposed.HookDynamicDex来使其成为程序入口
 * Author: elaine
 * Date: 2019/5/23
 * Tips:Hook动态加载的dex的方法就是  1.先Hook Activity类（android.app.Activity）
 *                                  2.在afterHookedMethod中获得该activity对象
 *                                  3.判断该对象是否时动态加载进来的dex中自定义的Activity,一般hook MainActivty即可
 *                                  4.通过调用getClass().getClassLoader()方法获得classLoader对象
 *                                  5.之后就可以用该classloader获取我们要hook的class啦
 * Description:Hook动态加载进来的Dex或apk，这里以米家apk中动态加载loock智能锁com.loock.classic_android包时的调用
 */


public class HookDynamicDex implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        //Hook Activity类（android.app.Activity）
        Class activityCls = XposedHelpers.findClass("android.app.Activity",lpparam.classLoader);
        XposedBridge.hookAllConstructors(activityCls, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //在afterHookedMethod中获得该activity对象
                final Object activityCls = param.thisObject;
                //打印所有的activity
                String name = activityCls.getClass().getName();
                XposedBridge.log("xpose_name:"+name);
                //判断该activity是否时我们要hook的activity
                if("com.loock.classic_android.MainActivity".equals(name)){
                    //通过调用getClass().getClassLoader()方法获得classLoader对象
                    ClassLoader classLoader =activityCls.getClass().getClassLoader();
                    Class<?> hookclass = null;
                    //hook我们实际要hook的类及其操作
                    try {
                        hookclass = classLoader.loadClass("com.loock.classic_android.customview.MyLogger");
                    } catch (Exception e) {
                        Log.e("xpose", "com.loock.classic_android.customview.MyLogger报错", e);
                        return;
                    }
                    Log.i("xpose", "寻找com.loock.classic_android.customview.MyLogger成功");
                    XposedHelpers.findAndHookMethod(hookclass,"d", Object.class,new XC_MethodHook(){
                        //进行hook操作
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Log.d("xpose_BleMyLog_d", param.args[0].toString());
                        }
                    });
                }

            }
        });


    }

    public static byte[] shortBytes(int arg3) {
        return new byte[]{((byte)(arg3 >> 8)), ((byte)arg3)};
    }
    private static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        if (bytes==null){
            return "null";
        }
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}

