# Flutter Laranjinha Payment Plugin
-keep class com.flutter_laranjinha_payment.** { *; }
-keepclassmembers class com.flutter_laranjinha_payment.** { *; }

# Rede SmartRede SDK
-keep class rede.smartrede.** { *; }
-keepclassmembers class rede.smartrede.** { *; }
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep interfaces
-keep interface rede.smartrede.** { *; }
-keep interface com.flutter_laranjinha_payment.** { *; }

# Kotlin Parcelize
-dontwarn kotlinx.parcelize.Parcelize
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keep @kotlinx.parcelize.Parcelize class * { *; }
-keepnames class kotlinx.parcelize.Parcelize

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class **$DefaultImpls {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

# Keep Activity and callbacks
-keepclassmembers class * implements io.flutter.plugin.common.MethodChannel.MethodCallHandler {
    public void onMethodCall(io.flutter.plugin.common.MethodCall, io.flutter.plugin.common.MethodChannel.Result);
}

-keepclassmembers class * implements io.flutter.embedding.engine.plugins.activity.ActivityAware {
    public void onAttachedToActivity(io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding);
    public void onDetachedFromActivityForConfigChanges();
    public void onReattachedToActivityForConfigChanges(io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding);
    public void onDetachedFromActivity();
}

# Keep printer callback
-keep class * implements rede.smartrede.commons.callback.IPrinterCallback { *; }
-keepclassmembers class * implements rede.smartrede.commons.callback.IPrinterCallback {
    public void onError(java.lang.String);
    public void onCompleted();
}
