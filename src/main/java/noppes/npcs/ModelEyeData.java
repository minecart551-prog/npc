package noppes.npcs;

import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import noppes.npcs.client.parts.MpmPartData;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketEyeBlink;
import noppes.npcs.shared.SharedReferences;
import noppes.npcs.shared.common.util.ColorUtil;
import noppes.npcs.shared.common.util.NopVector2i;
import noppes.npcs.shared.common.util.NopVector3f;

import java.util.Random;


public class ModelEyeData extends MpmPartData {
	public static final ResourceLocation RESOURCE = new ResourceLocation("moreplayermodels", "eyes");
	public static final ResourceLocation RESOURCE_LEFT = new ResourceLocation("moreplayermodels", "eyes_left");
	public static final ResourceLocation RESOURCE_RIGHT = new ResourceLocation("moreplayermodels", "eyes_right");
	private final Random r = new Random();
	public boolean glint = true;

	public NopVector3f browThickness = new NopVector3f(1, 4 / 10f, 1);
	public NopVector2i eyePos = NopVector2i.ZERO;
	public boolean mirror = false;
	public int eyeSize = 0;
	public int skinType = 0; //0:Player, 1:Normal, 2:Custom texture

	public boolean useLidTexture = false;
	//public ResourceLocation lidTexture = new ResourceLocation(MorePlayerModels.MODID, "textures/parts/eyes/lids.png");
	public NopVector3f lidColor = ColorUtil.colorToRgb(0xB4846D);

	public NopVector3f browColor = ColorUtil.colorToRgb(0x5b4934);

	public long blinkStart = 0;
	public boolean disableBlink = false;

	public ModelEyeData(){
		this.color = (new NopVector3f[]{ColorUtil.colorToRgb(8368696), ColorUtil.colorToRgb(16247203), ColorUtil.colorToRgb(10526975),
				ColorUtil.colorToRgb(10987431), ColorUtil.colorToRgb(10791096), ColorUtil.colorToRgb(4210943), ColorUtil.colorToRgb(14188339),
				ColorUtil.colorToRgb(11685080), ColorUtil.colorToRgb(6724056), ColorUtil.colorToRgb(15066419), ColorUtil.colorToRgb(55610),
				ColorUtil.colorToRgb(8375321), ColorUtil.colorToRgb(15892389), ColorUtil.colorToRgb(10066329), ColorUtil.colorToRgb(5013401),
				ColorUtil.colorToRgb(8339378), ColorUtil.colorToRgb(3361970), ColorUtil.colorToRgb(6704179), ColorUtil.colorToRgb(6717235),
				ColorUtil.colorToRgb(10040115), ColorUtil.colorToRgb(16445005), ColorUtil.colorToRgb(6085589), ColorUtil.colorToRgb(4882687)})[r.nextInt(23)];
	}

	@Override
	public CompoundTag getNbt(){
		CompoundTag compound = super.getNbt();
		compound.putBoolean("Glint", glint);
		compound.putBoolean("UseLidTexture", useLidTexture);
		compound.putBoolean("Mirror", mirror);
		compound.putBoolean("DisableBlink", disableBlink);
		compound.putInt("SkinType", skinType);
		compound.putInt("EyeSize", eyeSize);

		compound.putInt("SkinColor", ColorUtil.rgbToColor(lidColor));
		compound.putInt("BrowColor", ColorUtil.rgbToColor(browColor));

		compound.putInt("PositionX", eyePos.x);
		compound.putInt("PositionY", eyePos.y);
		compound.putInt("BrowThickness", (int) (browThickness.y * 10f));
		return compound;
	}

	public void setNbt(CompoundTag compound){
		super.setNbt(compound);
		glint = compound.getBoolean("Glint");
		useLidTexture = compound.getBoolean("UseLidTexture");
		mirror = compound.getBoolean("Mirror");
		disableBlink = compound.getBoolean("DisableBlink");
		skinType = compound.getInt("SkinType");
		eyeSize = compound.getInt("EyeSize");

		lidColor = ColorUtil.colorToRgb(compound.getInt("SkinColor"));
		browColor = ColorUtil.colorToRgb(compound.getInt("BrowColor"));

		eyePos = new NopVector2i(compound.getInt("PositionX"), compound.getInt("PositionY"));
		browThickness = new NopVector3f(1, compound.getInt("BrowThickness") / 10f, 1);
	}

	public void update(LivingEntity player){
		if(!player.isAlive() || disableBlink) {
			return;
		}
		if(blinkStart < 0){
			blinkStart++;
		}
		else if(blinkStart == 0){
			if(r.nextInt(140) == 1){
				blinkStart = System.currentTimeMillis();
				if(!player.level().isClientSide){
					Packets.sendNearby(player, new PacketEyeBlink(player.getId()));
				}
			}
		}
		else if(System.currentTimeMillis() - blinkStart > 300){
			blinkStart = -20;
		}
	}

	@Override
	public ResourceLocation getUrlTexture() {
		ResourceLocation url = super.getUrlTexture();
		if(url == null){
			return MissingTextureAtlasSprite.getLocation();
		}
		return url;
	}
}
