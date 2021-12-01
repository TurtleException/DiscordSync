package de.eldritch.spigot.DiscordSync.module;

import de.eldritch.spigot.DiscordSync.DiscordSync;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;

public class ModuleManager {
    private final HashSet<PluginModule> modules = new HashSet<>();


    @SafeVarargs
    public ModuleManager(Map.Entry<Class<? extends PluginModule>, Object[]>... modules) {
        this.registerModules(new HashMap<>(Map.ofEntries(modules)));
    }


    /**
     * Registers modules by instantiating them one at a time.
     */
    public void registerModules(HashMap<Class<? extends PluginModule>, Object[]> modClasses) {
        modClasses.forEach((clazz, params) -> {
            Class<?>[] paramTypes = new Class<?>[params.length];
            for (int i = 0; i < params.length; i++) {
                paramTypes[i] = params[i].getClass();
            }

            PluginModule obj;
            try {
                // instantiate
                obj = clazz.getConstructor(paramTypes).newInstance(params);

                // add to set
                if (!modules.add(obj)) // stop if another object of this module has already been registered
                    throw new PluginModuleEnableException(clazz.getSimpleName() + " already exists.");
                DiscordSync.singleton.getLogger().info("Registered " + clazz.getSimpleName() + ".");
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException | PluginModuleEnableException e) {
                DiscordSync.singleton.getLogger().log(Level.WARNING, "Unable to instantiate " + clazz.getSimpleName() + ". It will be ignored.", e);
            }
        });
    }

    /**
     * @return Set of all currently registered modules.
     */
    public HashSet<PluginModule> getRegisteredModules() {
        return this.modules;
    }

    public boolean unregister(PluginModule module) {
        if (modules.contains(module)) {
            module.onDisable();
            return modules.remove(module);
        }
        return false;
    }
}