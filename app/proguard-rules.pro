# ProGuard / R8 rules for Cabela's Smoker Companion
# Retain models and serialization structures if needed
-keepclassmembers class * {
    @androidx.annotation.Keep <fields>;
    @androidx.annotation.Keep <methods>;
}
