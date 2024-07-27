package de.turtleboi.spigot.dsync.api;

import de.turtleboi.spigot.dsync.api.entity.dao.UserDAO;
import org.jetbrains.annotations.NotNull;

public interface ObeliskAPI {
    static void register(@NotNull ObeliskAPI api) throws IllegalStateException {
        Bootstrap.register(api);
    }

    static @NotNull ObeliskAPI getInstance() throws IllegalStateException {
        return Bootstrap.get();
    }

    /* - - - */

    @NotNull UserDAO getUserDAO();
}
