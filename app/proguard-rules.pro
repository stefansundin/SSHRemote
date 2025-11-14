#================================================================================
# ProGuard rules for SSH Remote
#================================================================================

# --- General Android & Debugging Rules ---
# Keep line numbers for better stack traces in release builds.
-keepattributes SourceFile,LineNumberTable

# If you use WebView with a JavaScript interface, you would configure it here.
# -keepclassmembers class fqcn.of.javascript.interface.for.webview { public *; }


#================================================================================
# Library: JSch (com.jcraft.jsch)
#================================================================================

# Keep the main library and its JCE crypto implementations.
# JSch uses reflection to load crypto classes, so we must keep them explicitly.
-keep class com.jcraft.jsch.** { *; }

# Suppress warnings for JSch's optional, non-Android dependencies.
# JSch was designed for desktop Java and includes optional support for features
# that are not available on the Android platform.

# Java Native Access (JNA) - used for native OS integration.
-dontwarn com.sun.jna.**
# Java Naming and Directory Interface (JNDI) - for enterprise resource lookup.
-dontwarn javax.naming.**
# Generic Security Service API (GSSAPI) - for Kerberos authentication.
-dontwarn org.ietf.jgss.**
# Optional logging frameworks.
-dontwarn org.apache.logging.log4j.**
-dontwarn org.slf4j.**
# Optional Unix socket support.
-dontwarn org.newsclub.net.unix.**


#================================================================================
# Library: Bouncy Castle (org.bouncycastle)
#================================================================================

# Keep the Bouncy Castle provider and its crypto implementations.
# It is used as a security provider by JSch.
-keep class org.bouncycastle.** { *; }

# Keep the constructors of all security providers.
-keepclassmembers class * extends java.security.Provider {
  public <init>();
  public <init>(java.lang.String);
}
