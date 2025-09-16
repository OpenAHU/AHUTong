-keepclasseswithmembernames class * {
    native <methods>;
}
 -keepclassmembers class * extends java.lang.Enum {
     public static **[] values();
     public static ** valueOf(java.lang.String);
 }
 -keepclassmembers class * implements java.io.Serializable {
     static final long serialVersionUID;
     static final java.io.ObjectStreamField[] serialPersistentFields;
     private void writeObject(java.io.ObjectOutputStream);
     private void readObject(java.io.ObjectInputStream);
     java.lang.Object writeReplace();
     java.lang.Object readResolve();
 }
 -keepclasseswithmembernames class * {
      native <methods>;
  }
# Bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

#Jsoup
-keeppackagenames org.jsoup.nodes
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.examples.android.model.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}
#不混淆某个包下的类
-keep class com.ahu.plugin.** {*;}

-dontwarn java.lang.instrument.ClassFileTransformer
-dontwarn sun.misc.SignalHandler
-dontwarn java.lang.instrument.Instrumentation
-dontwarn sun.misc.Signal
-keep class com.ahu.ahutong.data.AHUResponse{
    private *;
}
-keep class org.jsoup.Connection{*;}
-keep class com.ahu.ahutong.ui.widget.schedule.bean.**{*;}
-keepclassmembers class com.ahu.ahutong.data.model.* {
    private *;
}

-keepclassmembers class com.ahu.ahutong.data.crawler.model.** {
     *;
}


-renamesourcefileattribute AHUTong
#开启深度重载
# -overloadaggressively
# 把重命名之后的类名放到根目录
-repackageclasses

-printmapping map.txt

 #使用GSON、fastjson等框架时，所写的JSON对象类不混淆，否则无法将JSON解析成对应的对象
-keepclassmembers class * {
    public <init>(org.json.JSONObject);
}

-assumenosideeffects class java.io.PrintStream {
      public *** println(...);
      public *** print(...);
  }

-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int i(...);
    public static int d(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}
