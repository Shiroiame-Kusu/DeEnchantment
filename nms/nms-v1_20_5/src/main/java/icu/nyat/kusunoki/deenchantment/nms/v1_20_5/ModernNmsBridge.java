package icu.nyat.kusunoki.deenchantment.nms.v1_20_5;

import icu.nyat.kusunoki.deenchantment.nms.NmsBridge;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.logging.Level;

/**
 * Bridge for 1.20.5-1.20.6 servers (Mojang mappings via paperweight).
 * Uses reflection to interact with NMS registries due to API changes in this version.
 */
final class ModernNmsBridge implements NmsBridge {

    private final Map<NamespacedKey, Enchantment> registered = new ConcurrentHashMap<>();
    private final Object nmsRegistry;
    private final Object bukkitRegistry;
    private final Class<?> bukkitRegistryClass;
    private final Object registryKey;  // ResourceKey for the enchantment registry
    
    // Reflected fields and methods
    private final Field frozenField;
    private final Field intrusiveHolderField;
    private final Field allTagsField;
    private final Field minecraftToBukkitField;
    private final Field cacheField;
    private final Method registerMethod;
    private final int registerMethodParamCount;  // 2, 3, or 4 params
    private final Object registrationInfo;  // RegistrationInfo.BUILT_IN or Lifecycle.STABLE
    private final Method containsKeyMethod;
    private final Method createIntrusiveHolderMethod;
    private final Method freezeMethod;
    private final Method getNextIdMethod;  // For getting next available registry ID
    
    private final BiFunction<NamespacedKey, Object, Enchantment> originalMapper;
    private volatile boolean registryRemapped;

    @SuppressWarnings("unchecked")
    ModernNmsBridge() {
        try {
            // Get Bukkit registry
            this.bukkitRegistry = org.bukkit.Registry.ENCHANTMENT;
            if (this.bukkitRegistry == null) {
                throw new IllegalStateException("Bukkit enchantment registry is null");
            }
            this.bukkitRegistryClass = this.bukkitRegistry.getClass();
            
            // Get NMS registry from CraftRegistry
            final Field minecraftRegistryField = findField(bukkitRegistryClass, "minecraftRegistry");
            this.nmsRegistry = minecraftRegistryField.get(bukkitRegistry);
            if (this.nmsRegistry == null) {
                throw new IllegalStateException("NMS enchantment registry is null");
            }
            final Class<?> nmsRegistryClass = this.nmsRegistry.getClass();
            
            // Get the registry's ResourceKey (needed for creating element ResourceKeys)
            this.registryKey = getRegistryKey(nmsRegistry);
            
            // Locate CraftRegistry fields
            this.minecraftToBukkitField = findField(bukkitRegistryClass, "minecraftToBukkit");
            this.cacheField = findField(bukkitRegistryClass, "cache");
            this.originalMapper = (BiFunction<NamespacedKey, Object, Enchantment>) minecraftToBukkitField.get(bukkitRegistry);
            
            // Locate NMS registry fields - try Mojang names first, then obfuscated
            this.frozenField = findFieldByType(nmsRegistryClass, boolean.class);
            this.intrusiveHolderField = findNullableMapField(nmsRegistryClass);
            this.allTagsField = findTagSetField(nmsRegistryClass);
            
            // Locate NMS registry methods
            this.registerMethod = findRegisterMethod(nmsRegistryClass);
            this.registerMethodParamCount = this.registerMethod.getParameterCount();
            this.registrationInfo = getRegistrationInfo(this.registerMethod);
            this.getNextIdMethod = (registerMethodParamCount == 4) ? findSizeMethod(nmsRegistryClass) : null;
            this.containsKeyMethod = findContainsKeyMethod(nmsRegistryClass);
            this.createIntrusiveHolderMethod = findSingleArgMethod(nmsRegistryClass, "createIntrusiveHolder", "f");
            this.freezeMethod = findNoArgMethod(nmsRegistryClass, "freeze", "l");
            
        } catch (final ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to initialize 1.20.5/1.20.6 bridge", exception);
        }
    }
    
    /**
     * Gets the registration info object based on the register method's parameter type.
     * In 1.20.5+, this is RegistrationInfo.BUILT_IN; in older versions it's Lifecycle.stable().
     */
    private static Object getRegistrationInfo(final Method registerMethod) throws ReflectiveOperationException {
        if (registerMethod.getParameterCount() < 3) {
            return null;  // 2-param method doesn't need this
        }
        
        final Class<?> thirdParam = registerMethod.getParameterTypes()[2];
        final String paramName = thirdParam.getSimpleName();
        
        // Try RegistrationInfo first (1.20.5+)
        if (paramName.contains("RegistrationInfo")) {
            // Get RegistrationInfo.BUILT_IN static field
            for (final Field field : thirdParam.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) &&
                    field.getType() == thirdParam &&
                    (field.getName().equals("BUILT_IN") || field.getName().equals("a"))) {
                    field.setAccessible(true);
                    return field.get(null);
                }
            }
            // Try to find any static field of the same type
            for (final Field field : thirdParam.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) &&
                    field.getType() == thirdParam) {
                    field.setAccessible(true);
                    return field.get(null);
                }
            }
            throw new NoSuchFieldException("Cannot find RegistrationInfo.BUILT_IN");
        }
        
        // Fall back to Lifecycle.stable() for older versions
        return getLifecycleStable();
    }
    
    /**
     * Gets the Lifecycle.stable() constant via reflection.
     */
    private static Object getLifecycleStable() throws ReflectiveOperationException {
        // Try to find the Lifecycle class
        Class<?> lifecycleClass = null;
        final String[] possibleNames = {
            "com.mojang.serialization.Lifecycle",
            "net.minecraft.util.Lifecycle"
        };
        
        for (final String className : possibleNames) {
            try {
                lifecycleClass = Class.forName(className);
                break;
            } catch (final ClassNotFoundException ignored) {
            }
        }
        
        if (lifecycleClass == null) {
            throw new ClassNotFoundException("Cannot find Lifecycle class");
        }
        
        // Try to get the stable() method or STABLE field
        try {
            final Method stableMethod = lifecycleClass.getMethod("stable");
            return stableMethod.invoke(null);
        } catch (final NoSuchMethodException e) {
            // Try STABLE field
            try {
                final Field stableField = lifecycleClass.getField("STABLE");
                return stableField.get(null);
            } catch (final NoSuchFieldException e2) {
                // Try to find any static method/field that returns Lifecycle
                for (final Method method : lifecycleClass.getDeclaredMethods()) {
                    if (java.lang.reflect.Modifier.isStatic(method.getModifiers()) &&
                        method.getParameterCount() == 0 &&
                        method.getReturnType() == lifecycleClass &&
                        (method.getName().equals("a") || method.getName().equals("stable"))) {
                        method.setAccessible(true);
                        return method.invoke(null);
                    }
                }
                for (final Field field : lifecycleClass.getDeclaredFields()) {
                    if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) &&
                        field.getType() == lifecycleClass) {
                        field.setAccessible(true);
                        return field.get(null);
                    }
                }
                throw new NoSuchMethodException("Cannot find Lifecycle.stable() or STABLE field");
            }
        }
    }

    @Override
    public void prepareRegistration() {
        ensureRegistryMapper();
        clearBukkitCache();
        unfreezeRegistry();
    }

    @Override
    public boolean register(final Enchantment enchantment) {
        if (enchantment == null) {
            return false;
        }

        final NamespacedKey key = enchantment.getKey();
        registered.put(key, enchantment);

        try {
            // Convert to NMS ResourceLocation/MinecraftKey via reflection
            final Object minecraftKey = createMinecraftKey(key);
            
            // Check if already registered
            if ((boolean) containsKeyMethod.invoke(nmsRegistry, minecraftKey)) {
                return true;
            }

            // Create a ResourceKey for this enchantment
            final Object resourceKey = createResourceKey(registryKey, minecraftKey);
            
            // Create a placeholder NMS enchantment - we just need something in the registry
            // The actual enchantment behavior comes from our registered Bukkit Enchantment
            final Object vanillaLike = createPlaceholderEnchantment();
            
            // Create intrusive holder and register
            createIntrusiveHolderMethod.invoke(nmsRegistry, vanillaLike);
            
            // Register based on method signature:
            // - 2-param: (ResourceKey, T) - newer versions without Lifecycle
            // - 3-param: (ResourceKey, T, RegistrationInfo/Lifecycle) - 1.20.5+ or older
            // - 4-param: (int, ResourceKey, T, Lifecycle) - with explicit ID
            switch (registerMethodParamCount) {
                case 2:
                    registerMethod.invoke(nmsRegistry, resourceKey, vanillaLike);
                    break;
                case 3:
                    registerMethod.invoke(nmsRegistry, resourceKey, vanillaLike, registrationInfo);
                    break;
                case 4:
                    final int nextId = getNextId();
                    registerMethod.invoke(nmsRegistry, nextId, resourceKey, vanillaLike, registrationInfo);
                    break;
                default:
                    throw new IllegalStateException("Unexpected register method param count: " + registerMethodParamCount);
            }
            
            return true;
        } catch (final Throwable error) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to register enchantment " + key + " via 1.20.5/1.20.6 bridge", error);
            registered.remove(key);
            return false;
        }
    }
    
    /**
     * Gets the next available ID for registry entries.
     */
    private int getNextId() throws ReflectiveOperationException {
        if (getNextIdMethod != null) {
            return (int) getNextIdMethod.invoke(nmsRegistry);
        }
        return 0;  // Fallback
    }

    @Override
    public void freezeRegistration() {
        try {
            freezeMethod.invoke(nmsRegistry);
        } catch (final ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to freeze registry", exception);
        }
    }

    @Override
    public void unregisterAll() {
        registered.clear();
        clearBukkitCache();
    }

    @Override
    public boolean supportsHardReset() {
        return false;
    }

    private void ensureRegistryMapper() {
        if (registryRemapped) {
            return;
        }
        synchronized (this) {
            if (registryRemapped) {
                return;
            }
            try {
                minecraftToBukkitField.set(bukkitRegistry, createMapperFunction());
                registryRemapped = true;
            } catch (final IllegalAccessException exception) {
                throw new IllegalStateException("Unable to override CraftRegistry mapper", exception);
            }
        }
    }

    private BiFunction<NamespacedKey, Object, Enchantment> createMapperFunction() {
        return (key, value) -> {
            final Enchantment custom = registered.get(key);
            if (custom != null) {
                return custom;
            }
            if (originalMapper != null) {
                return originalMapper.apply(key, value);
            }
            return null;
        };
    }

    private void clearBukkitCache() {
        try {
            @SuppressWarnings("unchecked")
            final Map<NamespacedKey, Enchantment> cache = (Map<NamespacedKey, Enchantment>) cacheField.get(bukkitRegistry);
            if (cache != null) {
                cache.clear();
            }
        } catch (final IllegalAccessException exception) {
            throw new IllegalStateException("Unable to clear CraftRegistry cache", exception);
        }
    }

    private void unfreezeRegistry() {
        try {
            frozenField.setBoolean(nmsRegistry, false);
            
            // Set a NEW IdentityHashMap to enable intrusive holder creation
            // Just clearing isn't enough - the registry checks if the map is non-null
            if (intrusiveHolderField != null) {
                intrusiveHolderField.set(nmsRegistry, new java.util.IdentityHashMap<>());
            }
            
            // Reset the tag set to unbound state
            if (allTagsField != null) {
                allTagsField.set(nmsRegistry, createUnboundTagSet());
            }
        } catch (final IllegalAccessException exception) {
            throw new IllegalStateException("Unable to unfreeze enchantment registry", exception);
        }
    }
    
    /**
     * Creates an unbound tag set for the registry.
     * This allows the registry to accept new tags after unfreezing.
     */
    private Object createUnboundTagSet() {
        try {
            final Class<?> registryClass = nmsRegistry.getClass();
            // Look for inner classes that have TagSet in their name
            for (final Class<?> innerClass : registryClass.getDeclaredClasses()) {
                for (final Method method : innerClass.getDeclaredMethods()) {
                    if (java.lang.reflect.Modifier.isStatic(method.getModifiers()) &&
                        method.getParameterCount() == 0) {
                        method.setAccessible(true);
                        try {
                            return method.invoke(null);
                        } catch (final Exception ignored) {
                            // Try next method
                        }
                    }
                }
            }
            // Fallback: return null if we can't create it
            return null;
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Creates a placeholder NMS enchantment. Since the actual behavior comes from
     * our custom Bukkit Enchantment via the mapper, we just need a valid NMS object.
     */
    private Object createPlaceholderEnchantment() throws ReflectiveOperationException {
        // Get an existing enchantment to use as template
        final Object protection = org.bukkit.Registry.ENCHANTMENT.get(NamespacedKey.minecraft("protection"));
        if (protection == null) {
            throw new IllegalStateException("Cannot find protection enchantment as template");
        }
        
        // Get the underlying NMS enchantment from CraftEnchantment
        final Class<?> craftEnchantmentClass = Class.forName("org.bukkit.craftbukkit.enchantments.CraftEnchantment");
        final Method getHandleMethod = craftEnchantmentClass.getMethod("getHandle");
        return getHandleMethod.invoke(protection);
    }

    /**
     * Creates a MinecraftKey/ResourceLocation from a NamespacedKey using reflection.
     * This handles both Mojang-mapped (ResourceLocation) and obfuscated (MinecraftKey) class names.
     */
    private static Object createMinecraftKey(final NamespacedKey key) throws ReflectiveOperationException {
        // Try Mojang-mapped name first
        Class<?> keyClass;
        try {
            keyClass = Class.forName("net.minecraft.resources.ResourceLocation");
        } catch (final ClassNotFoundException e) {
            // Try obfuscated name
            keyClass = Class.forName("net.minecraft.resources.MinecraftKey");
        }
        
        // Try constructor with two strings (namespace, path)
        try {
            return keyClass.getConstructor(String.class, String.class)
                    .newInstance(key.getNamespace(), key.getKey());
        } catch (final NoSuchMethodException e) {
            // Try single string constructor with colon separator
            return keyClass.getConstructor(String.class)
                    .newInstance(key.getNamespace() + ":" + key.getKey());
        }
    }
    
    /**
     * Gets the ResourceKey of the registry itself (e.g., Registry.ENCHANTMENT).
     */
    private static Object getRegistryKey(final Object registry) throws ReflectiveOperationException {
        // Try to find the key() method that returns ResourceKey
        // Check all methods including inherited ones
        for (Class<?> c = registry.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (final Method method : c.getDeclaredMethods()) {
                if (method.getParameterCount() == 0 &&
                    method.getReturnType().getSimpleName().contains("ResourceKey")) {
                    method.setAccessible(true);
                    try {
                        final Object result = method.invoke(registry);
                        if (result != null) {
                            return result;
                        }
                    } catch (final Exception ignored) {
                        // Try next method
                    }
                }
            }
        }
        
        // Also try fields that might hold the registry key
        for (Class<?> c = registry.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (final Field field : c.getDeclaredFields()) {
                if (field.getType().getSimpleName().contains("ResourceKey")) {
                    field.setAccessible(true);
                    final Object result = field.get(registry);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        
        throw new NoSuchMethodException("Cannot find registry key method or field");
    }
    
    /**
     * Creates a ResourceKey for an element in the registry.
     * ResourceKey.create(registryKey, resourceLocation)
     */
    private static Object createResourceKey(final Object registryKey, final Object resourceLocation) throws ReflectiveOperationException {
        final Class<?> resourceKeyClass = registryKey.getClass();
        
        // Find the static create method: ResourceKey.create(ResourceKey<? extends Registry<T>>, ResourceLocation)
        for (final Method method : resourceKeyClass.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isStatic(method.getModifiers()) &&
                method.getParameterCount() == 2 &&
                (method.getName().equals("create") || method.getName().equals("a"))) {
                method.setAccessible(true);
                return method.invoke(null, registryKey, resourceLocation);
            }
        }
        throw new NoSuchMethodException("Cannot find ResourceKey.create method");
    }

    // ==================== Reflection Utilities ====================

    private static Field findField(final Class<?> clazz, final String name) throws NoSuchFieldException {
        try {
            final Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (final NoSuchFieldException e) {
            // Try superclass
            final Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                return findField(superClass, name);
            }
            throw e;
        }
    }

    private static Field findFieldByType(final Class<?> clazz, final Class<?> type) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (final Field field : c.getDeclaredFields()) {
                if (field.getType() == type) {
                    field.setAccessible(true);
                    return field;
                }
            }
        }
        throw new IllegalStateException("Cannot find field of type " + type + " in " + clazz);
    }

    private static Field findNullableMapField(final Class<?> clazz) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (final Field field : c.getDeclaredFields()) {
                if (Map.class.isAssignableFrom(field.getType())) {
                    // Check for @Nullable annotation or if it's the unregisteredIntrusiveHolders pattern
                    if (field.getAnnotations().length > 0 || 
                        field.getName().contains("unregistered") ||
                        field.getName().equals("m")) {  // Obfuscated name
                        field.setAccessible(true);
                        return field;
                    }
                }
            }
        }
        // Return null if not found - not all versions have this field
        return null;
    }

    private static Field findTagSetField(final Class<?> clazz) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (final Field field : c.getDeclaredFields()) {
                // Look for TagSet field by type name
                if (field.getType().getName().contains("TagSet")) {
                    field.setAccessible(true);
                    return field;
                }
            }
        }
        // Return null if not found - not all versions have this field
        return null;
    }

    private static Method findRegisterMethod(final Class<?> registryClass) throws NoSuchMethodException {
        // In obfuscated builds, method names can be anything
        // Looking for patterns:
        // - (ResourceKey, Object, RegistrationInfo) -> 3 params (1.20.5+)
        // - (ResourceKey, Object, Lifecycle) -> 3 params (older)
        // - (int, ResourceKey, Object, Lifecycle) -> 4 params
        
        // First priority: 3-param method (ResourceKey, Object, RegistrationInfo) - 1.20.5+
        for (Class<?> c = registryClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (final Method method : c.getDeclaredMethods()) {
                if (!java.lang.reflect.Modifier.isStatic(method.getModifiers()) &&
                    method.getParameterCount() == 3) {
                    final Class<?>[] params = method.getParameterTypes();
                    // Check for (ResourceKey, Object, RegistrationInfo) pattern
                    if (params[0].getSimpleName().contains("ResourceKey") &&
                        params[2].getSimpleName().contains("RegistrationInfo")) {
                        method.setAccessible(true);
                        Bukkit.getLogger().info("[DeEnchantment] Found 3-param register method with RegistrationInfo: " + method.getName());
                        return method;
                    }
                }
            }
        }
        
        // Second priority: 3-param method (ResourceKey, Object, Lifecycle)
        for (Class<?> c = registryClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (final Method method : c.getDeclaredMethods()) {
                if (!java.lang.reflect.Modifier.isStatic(method.getModifiers()) &&
                    method.getParameterCount() == 3) {
                    final Class<?>[] params = method.getParameterTypes();
                    // Check for (ResourceKey, Object, Lifecycle) pattern
                    if (params[0].getSimpleName().contains("ResourceKey") &&
                        params[2].getSimpleName().contains("Lifecycle")) {
                        method.setAccessible(true);
                        Bukkit.getLogger().info("[DeEnchantment] Found 3-param register method with Lifecycle: " + method.getName());
                        return method;
                    }
                }
            }
        }
        
        // Third priority: 4-param method (int, ResourceKey, Object, Lifecycle)
        for (Class<?> c = registryClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (final Method method : c.getDeclaredMethods()) {
                if (!java.lang.reflect.Modifier.isStatic(method.getModifiers()) &&
                    method.getParameterCount() == 4) {
                    final Class<?>[] params = method.getParameterTypes();
                    // Check for (int, ResourceKey, Object, Lifecycle) pattern
                    if ((params[0] == int.class || params[0] == Integer.class) &&
                        params[1].getSimpleName().contains("ResourceKey") &&
                        params[3].getSimpleName().contains("Lifecycle")) {
                        method.setAccessible(true);
                        Bukkit.getLogger().info("[DeEnchantment] Found 4-param register method: " + method.getName());
                        return method;
                    }
                }
            }
        }
        
        // Fallback: 2-param method (ResourceKey, T) - for newer versions without Lifecycle
        for (Class<?> c = registryClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (final Method method : c.getDeclaredMethods()) {
                if (!java.lang.reflect.Modifier.isStatic(method.getModifiers()) &&
                    method.getParameterCount() == 2) {
                    final Class<?>[] params = method.getParameterTypes();
                    if (params[0].getSimpleName().contains("ResourceKey")) {
                        method.setAccessible(true);
                        Bukkit.getLogger().info("[DeEnchantment] Found 2-param register method: " + method.getName());
                        return method;
                    }
                }
            }
        }
        
        // Debug: Log all methods to help diagnose
        Bukkit.getLogger().warning("[DeEnchantment] Could not find register method. Available methods in " + registryClass.getName() + ":");
        for (Class<?> c = registryClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (final Method method : c.getDeclaredMethods()) {
                if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("  ").append(method.getName()).append("(");
                    final Class<?>[] params = method.getParameterTypes();
                    for (int i = 0; i < params.length; i++) {
                        if (i > 0) sb.append(", ");
                        sb.append(params[i].getSimpleName());
                    }
                    sb.append(") -> ").append(method.getReturnType().getSimpleName());
                    Bukkit.getLogger().warning(sb.toString());
                }
            }
        }
        
        throw new NoSuchMethodException("Cannot find registry.register method");
    }
    
    private static Method findSizeMethod(final Class<?> registryClass) {
        // Find size() method to get next available ID
        for (Class<?> c = registryClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (final Method method : c.getDeclaredMethods()) {
                if (method.getParameterCount() == 0 &&
                    method.getReturnType() == int.class &&
                    (method.getName().equals("size") || method.getName().equals("a") ||
                     method.getName().equals("b") || method.getName().equals("c"))) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        return null;  // Not found, will use fallback
    }

    private static Method findContainsKeyMethod(final Class<?> registryClass) {
        // Try containsKey first (Mojang mapped), then 'b' or 'c' (obfuscated)
        for (Class<?> c = registryClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (final Method method : c.getDeclaredMethods()) {
                if (method.getParameterCount() == 1 && 
                    method.getReturnType() == boolean.class &&
                    (method.getName().equals("containsKey") || 
                     method.getName().equals("b") ||
                     method.getName().equals("c"))) {
                    // Check parameter is ResourceLocation/MinecraftKey type
                    final Class<?> paramType = method.getParameterTypes()[0];
                    if (paramType.getSimpleName().contains("ResourceLocation") ||
                        paramType.getSimpleName().contains("MinecraftKey")) {
                        method.setAccessible(true);
                        return method;
                    }
                }
            }
        }
        throw new IllegalStateException("Cannot find containsKey method in " + registryClass);
    }

    private static Method findSingleArgMethod(final Class<?> clazz, final String mojangName, final String obfName) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (final Method method : c.getDeclaredMethods()) {
                if (method.getParameterCount() == 1 &&
                    (method.getName().equals(mojangName) || method.getName().equals(obfName))) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        throw new IllegalStateException("Cannot find method " + mojangName + "/" + obfName + " in " + clazz);
    }

    private static Method findNoArgMethod(final Class<?> clazz, final String mojangName, final String obfName) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (final Method method : c.getDeclaredMethods()) {
                if (method.getParameterCount() == 0 &&
                    (method.getName().equals(mojangName) || method.getName().equals(obfName))) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        throw new IllegalStateException("Cannot find method " + mojangName + "/" + obfName + " in " + clazz);
    }
}
