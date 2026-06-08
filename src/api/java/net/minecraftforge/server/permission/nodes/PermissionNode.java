/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.server.permission.nodes;

import com.google.common.base.Preconditions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public final class PermissionNode<T>
{
    private final String nodeName;
    private final PermissionType<T> type;
    private final PermissionResolver<T> defaultResolver;

    @Nullable
    private Component readableName;
    @Nullable
    private Component description;

    public PermissionNode(ResourceLocation nodeName, PermissionType<T> type, PermissionResolver<T> defaultResolver)
    {
        this(nodeName.getNamespace(), nodeName.getPath(), type, defaultResolver);
    }

    public PermissionNode(String modID, String nodeName, PermissionType<T> type, PermissionResolver<T> defaultResolver)
    {
        this(modID + "." + nodeName, type, defaultResolver);
    }

    /**
     * @param nodeName        The identifier of a node, recommended identifier structure: "modid.path.for.node"
     * @param type            type object for the PermissionNode, only use types in {@link PermissionTypes}
     * @param defaultResolver Default resolver for the permission, can but doesn't have to be used to by PermissionHandlers
     */
    private PermissionNode(String nodeName, PermissionType<T> type, PermissionResolver<T> defaultResolver)
    {
        this.nodeName = nodeName;
        this.type = type;
        this.defaultResolver = defaultResolver;
    }

    /**
     * Allows you to set a human-readable name and description for your Permission.
     *
     * <p>Note: Even though not used by Default, PermissionHandlers may display this information in game,
     * or provide it to the user by other means.<br>
     * You may use {@link net.minecraft.network.chat.Component#translatable(String) translatable components}, but you'll
     * need 2 language files. One inside the data directory for the server and one inside assets for the client.</p>
     *
     * @param readableName an easier to read name for the PermissionNode,
     *                     when using TranslatableComponent, key should be of format {@code "permission.name.<nodename>"}
     * @param description  description for the PermissionNode
     *                     when using TranslatableComponent, key should be of format {@code "permission.desc.<nodename>"}
     * @return itself with the new information set.
     */
    public PermissionNode setInformation(@NotNull Component readableName, @NotNull Component description)
    {
        Preconditions.checkNotNull(readableName, "Readable name for PermissionNodes must not be null %s", this.nodeName);
        Preconditions.checkNotNull(description, "Description for PermissionNodes must not be null %s", this.nodeName);

        this.readableName = readableName;
        this.description = description;

        return this;
    }

    public String getNodeName()
    {
        return nodeName;
    }

    public PermissionType<T> getType()
    {
        return type;
    }

    public PermissionResolver<T> getDefaultResolver()
    {
        return defaultResolver;
    }

    @Nullable
    public Component getReadableName()
    {
        return readableName;
    }

    @Nullable
    public Component getDescription()
    {
        return description;
    }

    /**
     * Utility Interface used for resolving the default value of PermissionNodes
     *
     * @param <T> generic value of the PermissionType of a PermissionNode
     */
    @FunctionalInterface
    public interface PermissionResolver<T>
    {
        /**
         * @param player     an online player
         * @param playerUUID if the player is null, this UUID belongs to an offline player,
         *                   otherwise it must match the UUID of the passed in player.
         * @param context    may contain DynamicContext if it was provided
         * @return according Permission Value
         */
        T resolve(@Nullable ServerPlayer player, UUID playerUUID, Object... context);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof PermissionNode otherNode)) return false;
        return nodeName.equals(otherNode.nodeName) && type.equals(otherNode.type);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(nodeName, type);
    }
}
