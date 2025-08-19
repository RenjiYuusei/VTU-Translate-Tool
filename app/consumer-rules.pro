# Consumer rules for better R8 optimization

# Aggressive class merging
-allowaccessmodification
-repackageclasses ''
-flattenpackagehierarchy ''

# Remove unused parameters and return values
-optimizations method/removal/parameter,method/propagation/returnvalue

# Enable more aggressive shrinking
-dontshrink !com.vtu.translate.**

# Optimize string concatenation
-optimizations code/simplification/string

# Remove unused exception handlers
-optimizations code/removal/exception

# Optimize field access
-optimizations field/removal/writeonly,field/propagation/value

# Additional enum optimization
-assumevalues public class * extends java.lang.Enum {
    public static **[] values() return ..;
    public static ** valueOf(java.lang.String) return _;
}
