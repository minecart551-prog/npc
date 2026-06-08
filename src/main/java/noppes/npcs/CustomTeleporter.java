package noppes.npcs;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalForcer;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class CustomTeleporter extends PortalForcer {

	private float yRot;
	private float xRot;
	private Vec3 pos;

	public CustomTeleporter(ServerLevel par1ServerLevel, Vec3 pos, float yRot, float xRot) {
		super(par1ServerLevel);
		this.pos = pos;
		this.yRot = yRot;
		this.xRot = xRot;
	}

	@Override
	public Optional<BlockUtil.FoundRectangle> findPortalAround(BlockPos pos, boolean isNether, WorldBorder border) {
		return Optional.empty();
	}

//	@Override
//	public PortalInfo getPortalInfo(Entity entity, ServerLevel destLevel, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
//		return new PortalInfo(pos, Vec3.ZERO, yRot, xRot);
//	}
}
