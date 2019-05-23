package com.elaine.xposed;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.jdom.JDOMException;

import java.io.IOException;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 使用前通过修改assest中的xpose_init为com.elaine.xposed.HookNormal来使其成为程序入口
 * Author: elaine
 * Date: 2019/5/23
 * Tips：1、当hook的方法的参数含有自定义类，则通过loadclass的方式先导入
 *       2、若hook的方法的参数的自定义类为内部类，则使用美元符号连接$
 *
 * Description:hook普通的类及方法
 *
 *
 *
 */
public class HookNormal implements IXposedHookLoadPackage {

    //被HOOK的程序的包名和类名
    String packName = "com.xiaomi.smarthome";
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedBridge.log("xposelyl Loaded app: " + loadPackageParam.packageName);
        if (loadPackageParam.packageName.equals(packName)) {
            XposedBridge.log("xposelyl Loaded app: " + loadPackageParam.packageName);
            //hook多dex的apk需要先hook Application类，并调用xpose的attach方法，在afterHookedMethod里再hook你想要的类
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ClassLoader cl = ((Context) param.args[0]).getClassLoader();
                    Class<?> hookclass = null;
                    try {
                        hookclass = cl.loadClass("com.xiaomi.miio.JNIBridge");
                    } catch (Exception e) {
                        Log.e("xpose", "com.xiaomi.miio.JNIBridge报错", e);
                        return;
                    }
                    Log.i("xpose", "寻找com.xiaomi.miio.JNIBridge成功");
                    XposedHelpers.findAndHookMethod(hookclass, "decrypt", byte[].class, byte[].class,new XC_MethodHook() {
                        //进行hook操作

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            byte[] input1 = (byte[]) param.args[0];
                            byte[] input2 = (byte[]) param.args[1];
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Object result = param.getResult();
                            byte[] message = (byte[]) result.getClass().getField("message").get(result);
                            long did = (long) result.getClass().getField("did").get(result);
                            byte[] token = (byte[]) result.getClass().getField("token").get(result);
                            if (message!=null){
                                Log.d("xpose_decrypt_message:",new String(message));
                            }else {
                                Log.d("xpose_decrypt_message:","null");
                            }
                            Log.d("xpose_decrypt_message:",new String(message));
                        }
                    });


                    //当hook的方法的参数含有自定义类，则通过loadclass的方式先导入
                    // 若hook的方法的参数的自定义类为内部类，则使用美元符号连接$
                    final Class<?> miioMsg = cl.loadClass("com.xiaomi.miio.JNIBridge$MiioMsg");
                    XposedHelpers.findAndHookMethod(hookclass, "encrypt", miioMsg, new XC_MethodHook() {
                        //进行hook操作

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("xposelyl_encrypt 开始获取属性：");
                            Object input = param.args[0];
                            byte[] message = (byte[]) input.getClass().getField("message").get(input);
                            long did = (long) input.getClass().getField("did").get(input);
                            byte[] token = (byte[]) input.getClass().getField("token").get(input);
                            if (message != null) {
                                Log.d("xpose_ecrypt_message", new String(message));
                            }
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            byte[] b = (byte[]) param.getResult();
                            Log.d("xpose_hencrypt_output",ByteArrayToHexString(b));
                        }
                    });
                }
            });

        }
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
