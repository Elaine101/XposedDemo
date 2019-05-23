# XposedDemo
### 以米家APK为研究对象学习研发Xposed插件，实现了对多dex app、动态加载的apk的Hook。
## 本程序仅供学习交流，如作他用所承受的法律责任一概与作者无关，转载请注明出处

该Demo主要包括以下几个问题：
   
   1、若要hook的方法的参数是自定义怎么办？
```
//使用方法XposedHelpers.findClass来获取参数类型的class对象
//具体的实现可见代码的HookNorNormal类
 XposedHelpers.findClass("xxx.你要hook的class.class", loadPackageParam.classLoader）
```
  2、如何hook内部类？
  ```
  //使用美元符号$来连接内部类，其中Builder是NetRequest的内部类
  //具体的实现可见代码的HookNorNormal类
hookclass = cl.loadClass("com.xiaomi.smarthome.core.entity.net.NetRequest$Builder");
  ```
  3、如何hook multidex分包的多dex的app？
  ```
     //先hook Application类，在afterHookedMethod里在hook你想要的类
    //被HOOK的程序的包名和类名
    //具体的实现可见代码的HookNorNormal类
    String packName = "xxx.你要hook的package";
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals(packName)){
            XposedBridge.log("xpose Loaded app: " + loadPackageParam.packageName);
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ClassLoader cl = ((Context)param.args[0]).getClassLoader();
                    Class<?> hookclass = cl.loadClass("xxx.你要hook的类.class");
                    //执行hook要hook的类及方法
                }
            });
        }
    }
  ```
  
  4、如何hook动态加载的dex及apk？
  ```
          //具体代码见HookDynamicDex类
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
  ```
