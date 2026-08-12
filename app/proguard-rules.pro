# Firestore deserializes these data transfer objects through reflection.
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.finlux.app.data.remote.dto.** { *; }
