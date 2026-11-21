package noppes.npcs.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Reflection bridge to the Factions mod (mod id "factions", ickerio/factions).
 *
 * <p>CustomNPCs must keep working when the factions mod is not installed, so
 * nothing here is compiled against it: every target class and member is looked
 * up lazily through reflection. Only JDK types appear in the reflected
 * signatures, so the lookups are not affected by intermediary remapping.
 *
 * <p>Everything is safe to call from block scripts, either directly
 * ({@code Java.type("noppes.npcs.util.FactionsInterop")}) or through the
 * helpers on {@link noppes.npcs.api.block.IBlock} and
 * {@link noppes.npcs.api.entity.IPlayer}. When the mod is missing every method
 * simply returns {@code null}/{@code false} so scripts can fall back to their
 * default behaviour.
 */
public class FactionsInterop {
	private static final Logger logger = LoggerFactory.getLogger("customnpcs");

	private static final String CLAIM = "io.icker.factions.api.persistents.Claim";
	private static final String USER = "io.icker.factions.api.persistents.User";
	private static final String FACTION = "io.icker.factions.api.persistents.Faction";
	private static final String RELATIONSHIP = "io.icker.factions.api.persistents.Relationship";

	/** Lazily resolved, false until the first call (or first failed probe). */
	private static volatile boolean probed = false;
	private static volatile boolean available = false;
	private static Throwable probeFailure;

	private static Method claimGet, claimGetFaction;
	private static Method userGet, userGetFaction;
	private static Method factionGet, factionGetByName, factionGetName, factionGetID, factionIsMutualAllies, factionGetRelationship;
	private static Field relationshipStatus;

	/** @return true when the factions mod is present on this instance */
	public static boolean isAvailable() {
		if (!probed)
			probe();
		return available;
	}

	private static synchronized void probe() {
		if (probed)
			return;
		probed = true;
		try {
			ClassLoader loader = FactionsInterop.class.getClassLoader();
			// initialize=false: only check the class is present, defer its
			// static setup until the first actual server side call
			Class<?> claim = Class.forName(CLAIM, false, loader);
			Class<?> user = Class.forName(USER, false, loader);
			Class<?> faction = Class.forName(FACTION, false, loader);
			Class<?> relationship = Class.forName(RELATIONSHIP, false, loader);

			claimGet = claim.getMethod("get", int.class, int.class, String.class);
			claimGetFaction = claim.getMethod("getFaction");

			userGet = user.getMethod("get", java.util.UUID.class);
			userGetFaction = user.getMethod("getFaction");

			factionGet = faction.getMethod("get", java.util.UUID.class);
			factionGetByName = faction.getMethod("getByName", String.class);
			factionGetName = faction.getMethod("getName");
			factionGetID = faction.getMethod("getID");
			factionIsMutualAllies = faction.getMethod("isMutualAllies", java.util.UUID.class);
			factionGetRelationship = faction.getMethod("getRelationship", java.util.UUID.class);

			// fail early if the relationship status field was renamed
			relationshipStatus = relationship.getField("status");

			available = true;
		} catch (Throwable t) {
			probeFailure = t;
			available = false;
		}
	}

	/** Clears the cached probe result, used by tests/debugging */
	public static synchronized void reset() {
		probed = false;
		available = false;
		probeFailure = null;
	}

	/**
	 * @return the name of the factions mod faction this player belongs to,
	 *         or null when the mod is missing or the player is factionless
	 */
	public static String getPlayerFaction(ServerPlayer player) {
		try {
			if (player == null || !isAvailable())
				return null;
			Object user = userGet.invoke(null, player.getUUID());
			if (user == null)
				return null;
			Object faction = userGetFaction.invoke(user);
			return faction == null ? null : (String) factionGetName.invoke(faction);
		} catch (Throwable t) {
			logOnce(t);
			return null;
		}
	}

	/**
	 * @return the name of the faction claiming the chunk the given position is
	 *         in, or null when the mod is missing or the chunk is wilderness
	 */
	public static String getClaimingFaction(Level level, BlockPos pos) {
		try {
			if (level == null || pos == null || level.isClientSide || !isAvailable())
				return null;
			String dimension = level.dimension().location().toString();
			Object claim = claimGet.invoke(null, pos.getX() >> 4, pos.getZ() >> 4, dimension);
			if (claim == null)
				return null;
			Object faction = claimGetFaction.invoke(claim);
			return faction == null ? null : (String) factionGetName.invoke(faction);
		} catch (Throwable t) {
			logOnce(t);
			return null;
		}
	}

	/** @return true when both factions are mutual allies */
	public static boolean areAllied(String factionA, String factionB) {
		try {
			Object a = resolve(factionA);
			Object b = resolve(factionB);
			if (a == null || b == null)
				return false;
			return (Boolean) factionIsMutualAllies.invoke(a, factionGetID.invoke(b));
		} catch (Throwable t) {
			logOnce(t);
			return false;
		}
	}

	/**
	 * @return the relationship status factionA has towards factionB:
	 *         "ALLY", "FRIENDLY", "NEUTRAL" or "ENEMY", or null when unknown
	 */
	public static String getRelationship(String factionA, String factionB) {
		try {
			Object a = resolve(factionA);
			Object b = resolve(factionB);
			if (a == null || b == null)
				return null;
			Object rel = factionGetRelationship.invoke(a, factionGetID.invoke(b));
			if (rel == null)
				return null;
			Object status = relationshipStatus.get(rel);
			return status == null ? null : ((Enum<?>) status).name();
		} catch (Throwable t) {
			logOnce(t);
			return null;
		}
	}

	private static Object resolve(String name) throws Exception {
		return name == null ? null : factionGetByName.invoke(null, name);
	}

	private static boolean logged;
	private static synchronized void logOnce(Throwable t) {
		if (logged)
			return;
		logged = true;
		logger.warn("Factions interop call failed, disabling further detail", t);
	}
}
