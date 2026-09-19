package com.rotfix.probe;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class RotFixProbeModule extends XposedModule {
    private static final float END_SCALE = 0.8f;
    private static final String LOG = "/storage/emulated/0/Download/RotFixProbe.log";
    private static final float ROT_DEG = -180.0f;
    private static final float ROT_SPEED = 2.0f;
    private static final float ROT_START = 0.0f;
    private static final long TURN_MS = 420;
    private static final long TURN_RESET_MS = 1500;
    private volatile boolean installed = false;
    private static final long T0 = System.currentTimeMillis();
    private static final Object LOCK = new Object();
    private static int cAll = 0;
    private static int cRot = 0;
    private static int cDiag = 0;
    private static int cDiag2 = 0;
    private static int cAs = 0;
    private static int cPO = 0;
    private static int cPO2 = 0;
    private static int cPO3 = 0;
    private static int cDiag3 = 0;
    private static int cDiag4 = 0;
    private static int cAll5 = 0;
    private static int thumbs = 0;
    private static volatile long sTurnStart = 0;
    private static int sTurnTaskId = Integer.MIN_VALUE;
    private static long sTurnTaskTime = 0;
    private static volatile boolean sReleased = false;
    private static volatile long sReleaseTime = 0;
    private static long burstLast = 0;
    private static int burstScene = -1;
    private static int burstCount = 0;
    private static volatile float sNativeProg = -1.0f;
    private static volatile long sNativeProgTime = 0;
    private static volatile boolean sNativeAnim = false;
    private static final StringBuilder SBUF = new StringBuilder();
    private static volatile boolean sFlushStarted = false;

    static  int access$1108() {
        int i = cAll;
        cAll = i + 1;
        return i;
    }

    static  int access$1208() {
        int i = cRot;
        cRot = i + 1;
        return i;
    }

    static  int access$1508() {
        int i = thumbs;
        thumbs = i + 1;
        return i;
    }

    static  int access$1608() {
        int i = cDiag;
        cDiag = i + 1;
        return i;
    }

    static  int access$1708() {
        int i = cAll5;
        cAll5 = i + 1;
        return i;
    }

    static  int access$1908() {
        int i = cDiag3;
        cDiag3 = i + 1;
        return i;
    }

    static  int access$2008() {
        int i = cDiag4;
        cDiag4 = i + 1;
        return i;
    }

    static  int access$208() {
        int i = cAs;
        cAs = i + 1;
        return i;
    }

    static  int access$2308() {
        int i = burstCount;
        burstCount = i + 1;
        return i;
    }



    private static void startFlusher() {
        if (sFlushStarted) {
            return;
        }
        sFlushStarted = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000L);
                    } catch (Throwable th) {
                    }
                    RotFixProbeModule.flushLog();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static void flushLog() {
        synchronized (SBUF) {
            if (SBUF.length() == 0) {
                return;
            }
            String string = SBUF.toString();
            SBUF.setLength(0);
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(new File(LOG), true), "UTF-8");
                outputStreamWriter.write(string);
                outputStreamWriter.flush();
                outputStreamWriter.close();
            } catch (Throwable th) {
            }
        }
    }

    public static void log(String str, String str2) {
        if (true) {
            return;
        }
        if (!sFlushStarted) {
            startFlusher();
        }
        synchronized (SBUF) {
            if (SBUF.length() > 262144) {
                return;
            }
            SBUF.append("P|").append(str).append("|t=").append(System.currentTimeMillis() - T0).append("|").append(str2).append("\n");
        }
    }

    public static Object readAny(Object obj, String str) {
        if (obj == null) {
            return null;
        }
        for (Class<?> superclass = obj.getClass(); superclass != null; superclass = superclass.getSuperclass()) {
            try {
                Field declaredField = superclass.getDeclaredField(str);
                declaredField.setAccessible(true);
                return declaredField.get(obj);
            } catch (Throwable th) {
            }
        }
        return null;
    }

    public static Object call0(Object obj, String str) {
        if (obj == null) {
            return null;
        }
        try {
            Method method = obj.getClass().getMethod(str, new Class[0]);
            method.setAccessible(true);
            return method.invoke(obj, new Object[0]);
        } catch (Throwable th) {
            return null;
        }
    }

    public static float halfW(Object obj) {
        Object any;
        Object objCall0;
        try {
            any = readAny(obj, "mDp");
        } catch (Throwable th) {
        }
        if (any == null || (objCall0 = call0(any, "config")) == null) {
            return ROT_START;
        }
        Object objCall02 = call0(objCall0, "getWidthPx");
        if (objCall02 instanceof Number) {
            return ((Number) objCall02).floatValue() / ROT_SPEED;
        }
        return ROT_START;
    }

    public static float halfH(Object obj) {
        Object any;
        Object objCall0;
        try {
            any = readAny(obj, "mDp");
        } catch (Throwable th) {
        }
        if (any == null || (objCall0 = call0(any, "config")) == null) {
            return ROT_START;
        }
        Object objCall02 = call0(objCall0, "getHeightPx");
        if (objCall02 instanceof Number) {
            return ((Number) objCall02).floatValue() / ROT_SPEED;
        }
        return ROT_START;
    }

    public static boolean isLandscape(Object obj) {
        try {
            Object any = readAny(readAny(obj, "mThumbnailData"), "rotation");
            if (any instanceof Number) {
                return ((Number) any).intValue() != 0;
            }
        } catch (Throwable th) {
        }
        return false;
    }


    public static int classifyScene() {
        try {
            StackTraceElement[] stackTrace = new Throwable().getStackTrace();
            boolean z = false;
            for (int i = 0; i < stackTrace.length; i++) {
                String className = stackTrace[i].getClassName();
                String methodName = stackTrace[i].getMethodName();
                if (!className.startsWith("com.rotfix.probe")) {
                    if (methodName.equals("startAppLaunchWindowAnim") || methodName.equals("createBreakAppOpenAnim") || methodName.equals("preStartResult") || className.contains("OplusLauncherAppTransitionHelper")) {
                        return 0;
                    }
                    if (methodName.equals("createAppToHomeAnimation")) {
                        z = true;
                    }
                    if (methodName.equals("createAnimateToHome")) {
                        z = true;
                    }
                    if (methodName.equals("applyScrollAndTransform")) {
                        z = true;
                    }
                    if (methodName.equals("onScrollXSpringUpdate")) {
                        z = true;
                    }
                    if (methodName.equals("onScrollYSpringUpdate")) {
                        z = true;
                    }
                }
            }
            return z ? 1 : -1;
        } catch (Throwable th) {
            return -1;
        }
    }

    private static void setAccessible(Executable executable) {
        try {
            executable.setAccessible(true);
        } catch (Throwable th) {
        }
    }

    public static float easeRot(float f) {
        float f2 = ROT_START;
        if (f <= ROT_START) {
            return ROT_START;
        }
        float f3 = (f - ROT_START) / 1.0f;
        if (f3 >= ROT_START) {
            f2 = f3;
        }
        return (float) Math.pow(f2 <= 1.0f ? f2 : 1.0f, 2.0d);
    }



    public void applyOne(Object obj, Object obj2, Object obj3, float f, boolean z) {
        int iIntValue;
        int i;
        int iIntValue2;
        Object any;
        if (obj == null || obj2 == null || obj3 == null) {
            return;
        }
        try {
            Object objCall0 = call0(obj, "isTaskView");
            if ((objCall0 instanceof Boolean) && ((Boolean) objCall0).booleanValue()) {
                return;
            }
            Object any2 = readAny(obj2, "screenSpaceBounds");
            if (any2 == null) {
                iIntValue = 0;
                i = 0;
            } else {
                Object any3 = readAny(any2, "right");
                Object any4 = readAny(any2, "bottom");
                int iIntValue3 = any3 instanceof Number ? ((Number) any3).intValue() : 0;
                if (any4 instanceof Number) {
                    i = iIntValue3;
                    iIntValue = ((Number) any4).intValue();
                } else {
                    i = iIntValue3;
                    iIntValue = 0;
                }
            }
            if (i < 1000 || iIntValue < 1000) {
                return;
            }
            try {
                Object any5 = readAny(obj2, "rotationChange");
                iIntValue2 = any5 instanceof Number ? ((Number) any5).intValue() : 0;
            } catch (Throwable th) {
                iIntValue2 = 0;
            }
            if (cPO <= 40) {
                cPO++;
                log("PO", "size=" + i + "x" + iIntValue + " rotChange=" + iIntValue2 + " leash=" + readAny(obj2, "leash"));
            }
            if (cPO3 <= 60) {
                cPO3++;
                log("ID", "size=" + i + "x" + iIntValue + " taskId=" + readAny(obj2, "taskId") + " activityType=" + readAny(obj2, "activityType") + " mode=" + readAny(obj2, "mode") + " windowType=" + readAny(obj2, "windowType") + " transId=" + readAny(obj2, "transitionId") + " leash=" + readAny(obj2, "leash"));
            }
            boolean z2 = i > iIntValue;
            if (cPO2 <= 40) {
                cPO2++;
                log("PX", "landscape=" + z2 + " sz=" + i + "x" + iIntValue + " leash=" + readAny(obj2, "leash"));
            }
            if (z2 && (any = readAny(obj2, "leash")) != null) {
                Object objCall02 = call0(any, "isValid");
                if (!(objCall02 instanceof Boolean) || ((Boolean) objCall02).booleanValue()) {
                    Method method = obj3.getClass().getMethod("forSurface", any.getClass());
                    method.setAccessible(true);
                    Object objInvoke = method.invoke(obj3, any);
                    if (objInvoke == null) {
                        return;
                    }
                    Object objCall03 = call0(obj, "getScale");
                    float f2 = 1.0f;
                    float fFloatValue = objCall03 instanceof Number ? ((Number) objCall03).floatValue() : 1.0f;
                    Object objCall04 = call0(obj, "getX");
                    float fFloatValue2 = objCall04 instanceof Number ? ((Number) objCall04).floatValue() : 0.0f;
                    Object objCall05 = call0(obj, "getY");
                    float fFloatValue3 = objCall05 instanceof Number ? ((Number) objCall05).floatValue() : 0.0f;
                    Object any6 = readAny(readAny(obj2, "clipRect"), "left");
                    int iIntValue4 = any6 instanceof Number ? ((Number) any6).intValue() : 0;
                    float f3 = f < ROT_START ? 0.0f : f;
                    if (f3 <= 1.0f) {
                        f2 = f3;
                    }
                    float f4 = ROT_DEG * f2;
                    float f5 = fFloatValue2 - (iIntValue4 * fFloatValue);
                    float f6 = i / ROT_SPEED;
                    float f7 = iIntValue / ROT_SPEED;
                    if (cPO <= 40) {
                        log("PV", "pivot=(" + f6 + "," + f7 + ") size=" + i + "x" + iIntValue + " land=(" + fFloatValue2 + "," + fFloatValue3 + ") sc=" + fFloatValue);
                    }
                    Class<?> cls = Class.forName("android.graphics.Matrix", false, objInvoke.getClass().getClassLoader());
                    Object objNewInstance = cls.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                    Method method2 = cls.getMethod("setScale", Float.TYPE, Float.TYPE);
                    method2.setAccessible(true);
                    method2.invoke(objNewInstance, Float.valueOf(fFloatValue), Float.valueOf(fFloatValue));
                    int i2 = iIntValue;
                    Method method3 = cls.getMethod("postTranslate", Float.TYPE, Float.TYPE);
                    method3.setAccessible(true);
                    method3.invoke(objNewInstance, Float.valueOf(f5), Float.valueOf(fFloatValue3));
                    if (f4 != ROT_START) {
                        Method method4 = cls.getMethod("postRotate", Float.TYPE, Float.TYPE, Float.TYPE);
                        method4.setAccessible(true);
                        method4.invoke(objNewInstance, Float.valueOf(f4), Float.valueOf(f6), Float.valueOf(f7));
                    }
                    Method method5 = objInvoke.getClass().getMethod("setMatrix", objNewInstance.getClass());
                    method5.setAccessible(true);
                    method5.invoke(objInvoke, objNewInstance);
                    cRot++;
                    if (cRot <= 120) {
                        log("RT", "hit#" + cRot + " tt=" + f2 + " deg=" + f4 + " sc=" + fFloatValue + " tx=" + f5 + " ty=" + fFloatValue3 + " size=" + i + "x" + i2);
                    }
                }
            }
        } catch (Throwable th2) {
            if (cAs <= 4) {
                log("ERR", "one " + th2);
            }
        }
    }

    public void onModuleLoaded(XposedModuleInterface.ModuleLoadedParam moduleLoadedParam) {
        try {
            new File(LOG).delete();
        } catch (Throwable th) {
        }
        log("INSTALL", "=== RotFixProbe loaded ===");
    }

    public void onPackageReady(XposedModuleInterface.PackageReadyParam packageReadyParam) {
        ClassLoader classLoader;
        if (packageReadyParam == null) {
            return;
        }
        try {
            String packageName = packageReadyParam.getPackageName();
            if (("com.android.launcher".equals(packageName) || "com.oplus.launcher".equals(packageName) || "com.coloros.launcher".equals(packageName)) && (classLoader = packageReadyParam.getClassLoader()) != null) {
                synchronized (this) {
                    if (this.installed) {
                        return;
                    }
                    this.installed = true;
                    log("INSTALL", "=== RotFixProbe v16 install start ===");
                    install(classLoader);
                    log("INSTALL", "pkg=" + packageName + " installed=" + this.installed);
                }
            }
        } catch (Throwable th) {
            log("INSTALL", "onPackageReady failed: " + th);
        }
    }

    private void install(ClassLoader classLoader) {
        log("INSTALL", "=== v64 total hooks = " + (hookRectLambda(classLoader) + 0 + hookThumbLambda(classLoader)) + " ===");
    }



    private int hookThumbLambda(ClassLoader classLoader) {
        try {
            Method method = null;
            for (Class<?> cls = Class.forName("com.android.quickstep.util.animation.RectTransformHelper", false, classLoader); cls != null; cls = cls.getSuperclass()) {
                Method[] declaredMethods = cls.getDeclaredMethods();
                int length = declaredMethods.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    Method method2 = declaredMethods[i];
                    Class<?>[] parameterTypes = method2.getParameterTypes();
                    if (parameterTypes.length == 4 && parameterTypes[1].getName().endsWith("RectTransformHelper$TransformParams") && parameterTypes[3].getName().equals("com.android.quickstep.util.SurfaceTransaction") && method2.getName().equals("applyThumbSurfaceParams$lambda$9")) {
                        method = method2;
                        break;
                    }
                    i++;
                }
                if (method != null) {
                    break;
                }
            }
            if (method == null) {
                log("INSTALL", "thumbLambda NOT FOUND");
                return 0;
            }
            return installThumbRot(method, "rotfix.thumbimpl");
        } catch (Throwable th) {
            log("INSTALL", "hook thumbLambda failed: " + th);
            return 0;
        }
    }

    private int installThumbRot(Method method, String str) {
        try {
            setAccessible(method);
            hook(method).setId(str).setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE).intercept(new XposedInterface.Hooker() {
                public Object intercept(XposedInterface.Chain chain) throws Throwable {
                    Object arg;
                    Object objProceed = chain.proceed();
                    try {
                        arg = chain.getArg(1);
                    } catch (Throwable th) {
                        if (RotFixProbeModule.cAll <= 6) {
                            RotFixProbeModule.log("ERR", "thumb " + th);
                        }
                    }
                    if (arg == null) {
                        return objProceed;
                    }
                    Object objCall0 = RotFixProbeModule.call0(arg, "getProgress");
                    float fFloatValue = objCall0 instanceof Number ? ((Number) objCall0).floatValue() : 0.0f;
                    if (fFloatValue > RotFixProbeModule.ROT_START && fFloatValue <= 1.0f && RotFixProbeModule.thumbs <= 120) {
                        RotFixProbeModule.access$1508();
                        RotFixProbeModule.log("TH", "hit#" + RotFixProbeModule.thumbs + " p=" + fFloatValue + " sc=" + RotFixProbeModule.call0(arg, "getScale") + " x=" + RotFixProbeModule.call0(arg, "getX") + " y=" + RotFixProbeModule.call0(arg, "getY"));
                    }
                    return objProceed;
                }
            });
            log("INSTALL", "hooked " + str + " ok");
            return 1;
        } catch (Throwable th) {
            log("INSTALL", "hook " + str + " failed: " + th);
            return 0;
        }
    }

    private int hookRectLambda(ClassLoader classLoader) {
        int iInstallRectRot;
        try {
            Method method = null;
            Method method2 = null;
            for (Class<?> cls = Class.forName("com.android.quickstep.util.animation.RectTransformHelper", false, classLoader); cls != null; cls = cls.getSuperclass()) {
                for (Method method3 : cls.getDeclaredMethods()) {
                    Class<?>[] parameterTypes = method3.getParameterTypes();
                    if (parameterTypes.length == 8 && parameterTypes[2].getName().equals("com.android.quickstep.util.SurfaceTransaction") && parameterTypes[3].getName().endsWith("RectTransformHelper$TransformParams")) {
                        if (method3.getName().equals("applySurfaceParams$lambda$7$lambda$5")) {
                            method = method3;
                        } else if (method3.isSynthetic()) {
                            method2 = method3;
                        }
                    }
                }
            }
            if (method != null) {
                iInstallRectRot = installRectRot(method, "rotfix.rectimpl") + 0;
            } else {
                iInstallRectRot = method2 != null ? installRectRot(method2, "rotfix.rotbridge") + 0 : 0;
            }
            if (method == null && method2 == null) {
                log("INSTALL", "rectLambda NOT FOUND");
                return 0;
            }
            return iInstallRectRot;
        } catch (Throwable th) {
            log("INSTALL", "hook rectLambda failed: " + th);
            return 0;
        }
    }


    private int installRectRot(Method method, String str) {
        try {
            setAccessible(method);
            hook(method).setId(str).setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE).intercept(new XposedInterface.Hooker() {
                public Object intercept(XposedInterface.Chain chain) throws Throwable {
                    Object objProceed = chain.proceed();
                    try {
                        Object arg = chain.getArg(3);
                        if (arg == null) {
                            return objProceed;
                        }
                        boolean z = (call0(arg, "isTaskView") instanceof Boolean) && ((Boolean) call0(arg, "isTaskView")).booleanValue();
                        Object objProgress = call0(arg, "getProgress");
                        float p = objProgress instanceof Number ? ((Number) objProgress).floatValue() : ROT_START;
                        if (z || p <= ROT_START || p > 1.0f) {
                            return objProceed;
                        }
                        Object trans = chain.getArg(2);
                        Object target = chain.getArg(0);
                        if (trans == null || target == null) {
                            return objProceed;
                        }
                        Object leash = readAny(target, "leash");
                        if (leash == null) {
                            return objProceed;
                        }
                        Object objValid = call0(leash, "isValid");
                        if ((objValid instanceof Boolean) && !((Boolean) objValid).booleanValue()) {
                            return objProceed;
                        }
                        Method forSurface = trans.getClass().getMethod("forSurface", leash.getClass());
                        forSurface.setAccessible(true);
                        Object surface = forSurface.invoke(trans, leash);
                        if (surface == null) {
                            return objProceed;
                        }
                        float scale = numberOr(call0(arg, "getScale"), 1.0f);
                        float landX = numberOr(call0(arg, "getX"), ROT_START);
                        float landY = numberOr(call0(arg, "getY"), ROT_START);
                        Object clipLeft = readAny(readAny(target, "clipRect"), "left");
                        int clip = clipLeft instanceof Number ? ((Number) clipLeft).intValue() : 0;
                        Object ssb = readAny(target, "screenSpaceBounds");
                        float pw = numberOr(readAny(ssb, "right"), 1272.0f);
                        float ph = numberOr(readAny(ssb, "bottom"), 2800.0f);

                        float deg = ROT_DEG * easeRot(p);
                        float tx = landX - (clip * scale);
                        float pivotX = ((pw / ROT_SPEED) * scale) + tx;
                        float pivotY = ((ph / ROT_SPEED) * scale) + landY;

                        Class<?> matrixCls = Class.forName("android.graphics.Matrix", false, surface.getClass().getClassLoader());
                        Object matrix = matrixCls.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                        Method setScale = matrixCls.getMethod("setScale", Float.TYPE, Float.TYPE);
                        setScale.setAccessible(true);
                        setScale.invoke(matrix, Float.valueOf(scale), Float.valueOf(scale));
                        Method postTranslate = matrixCls.getMethod("postTranslate", Float.TYPE, Float.TYPE);
                        postTranslate.setAccessible(true);
                        postTranslate.invoke(matrix, Float.valueOf(tx), Float.valueOf(landY));
                        if (deg != ROT_START) {
                            Method postRotate = matrixCls.getMethod("postRotate", Float.TYPE, Float.TYPE, Float.TYPE);
                            postRotate.setAccessible(true);
                            postRotate.invoke(matrix, Float.valueOf(deg), Float.valueOf(pivotX), Float.valueOf(pivotY));
                        }
                        Method setMatrix = surface.getClass().getMethod("setMatrix", matrix.getClass());
                        setMatrix.setAccessible(true);
                        setMatrix.invoke(surface, matrix);
                        cRot++;
                        if (cRot <= 200) {
                            log("RT", "hit#" + cRot + " p=" + p + " deg=" + deg + " sc=" + scale + " tx=" + tx + " ty=" + landY);
                        }
                    } catch (Throwable th) {
                        if (cAll <= 6) {
                            log("ERR", "rot " + th);
                        }
                    }
                    return objProceed;
                }
            });
            log("INSTALL", "hooked " + str + " ok");
            return 1;
        } catch (Throwable th) {
            log("INSTALL", "hook " + str + " failed: " + th);
            return 0;
        }
    }

    private static float numberOr(Object obj, float f) {
        return obj instanceof Number ? ((Number) obj).floatValue() : f;
    }

}
