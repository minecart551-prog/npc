package noppes.npcs.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketNbtBookBlockSave;
import noppes.npcs.packets.server.SPacketNbtBookEntitySave;
import noppes.npcs.shared.client.gui.GuiTextAreaScreen;
import noppes.npcs.shared.client.gui.components.GuiButtonNop;
import noppes.npcs.shared.client.gui.components.GuiLabel;
import noppes.npcs.shared.client.gui.listeners.IGuiData;

public class GuiNbtBook extends GuiNPCInterface implements IGuiData {
	
	private BlockPos pos;
	private BlockEntity tile;
	private BlockState state;
	private ItemStack blockStack;
	
	private int entityId;
	private Entity entity;

	private CompoundTag originalCompound;
	private CompoundTag compound;

	private String faultyText = null;
	private String errorMessage = null;
	
    public GuiNbtBook(BlockPos pos) {
		this.pos = pos;
		setBackground("menubg.png");
		imageWidth = 256;
		imageHeight = 216;
	}

	@Override
	public void init(){
    	super.init();
    	int y = guiTop + 40;
    	if(state != null) {
    		addLabel(new GuiLabel(11, "x: " + pos.getX() + ", y: " + pos.getY() + ", z: " + pos.getZ(), guiLeft + 60, guiTop + 6));
    		addLabel(new GuiLabel(12, "id: " + BuiltInRegistries.BLOCK.getKey(state.getBlock()), guiLeft + 60, guiTop + 16));
    	}
    	if(entity != null) {
    		addLabel(new GuiLabel(12, "id: " + entity.getType().getDescriptionId(), guiLeft + 60, guiTop + 6));
    	}

        addButton(new GuiButtonNop(this, 0, guiLeft + 38, guiTop + 144, 180,20, "nbt.edit"));
        getButton(0).active = compound != null && !compound.isEmpty();

        addLabel(new GuiLabel(0, "", guiLeft + 4, guiTop + 167));
        addLabel(new GuiLabel(1, "", guiLeft + 4, guiTop + 177));

        addButton(new GuiButtonNop(this, 66, guiLeft + 128, guiTop + 190,120,20, "gui.close"));
        addButton(new GuiButtonNop(this, 67, guiLeft + 4, guiTop + 190,120,20, "gui.save"));

        if(errorMessage != null) {
        	getButton(67).active = false;
    		int i = errorMessage.indexOf(" at: ");
    		if(i > 0) {
                getLabel(0).setMessage(Component.translatable(errorMessage.substring(0, i)));
                getLabel(1).setMessage(Component.translatable(errorMessage.substring(i)));
    		}
    		else {
                getLabel(0).setMessage(Component.translatable(errorMessage));
    		}
        }
        if(getButton(67).active && originalCompound != null) {
        	getButton(67).active = !originalCompound.equals(compound);
        }
    }

	@Override
	public void buttonEvent(GuiButtonNop guibutton) {
		int id = guibutton.id;
		if(id == 0) {
			if(faultyText != null) {
				setSubGui(new GuiTextAreaScreen(compound.toString(), faultyText).enableHighlighting());
			}
			else {
				setSubGui(new GuiTextAreaScreen(compound.toString()).enableHighlighting());
			}
		}
		if(id == 67) {
            getLabel(0).setMessage(Component.translatable("Saved"));
			if(compound.equals(originalCompound))
				return;
			if(tile == null) {
				Packets.sendServer(new SPacketNbtBookEntitySave(entityId, compound));
				return;
			}
			else {
				Packets.sendServer(new SPacketNbtBookBlockSave(pos, compound));
			}
			originalCompound = compound.copy();
        	getButton(67).active = false;
		}
		if(id == 66) {
			close();
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.render(graphics, mouseX, mouseY, partialTicks);
		if(hasSubGui())
			return;
		PoseStack matrixStack = graphics.pose();
		if(state != null) {
			matrixStack.pushPose();;
			matrixStack.translate(guiLeft + 4, guiTop + 4, 0);
			matrixStack.scale(3, 3, 3);
			//RenderHelper.enableGUIStandardItemLighting();
			graphics.renderItem(blockStack, 0, 0);
			graphics.renderItemDecorations(font, blockStack, 0, 0);
			//RenderHelper.disableStandardItemLighting();
			matrixStack.popPose();
		}

		if(entity instanceof LivingEntity) {
			drawNpc(graphics, (LivingEntity)entity, 20, 80, 1, 0);
		}
	}

	@Override
	public void subGuiClosed(Screen gui) {
		if(gui instanceof GuiTextAreaScreen){
			try {
				compound = TagParser.parseTag(((GuiTextAreaScreen)gui).text);
				errorMessage = faultyText = null;
			} catch (CommandSyntaxException e) {
				errorMessage = e.getLocalizedMessage();
				faultyText = ((GuiTextAreaScreen)gui).text;
			}
	        init();
		}
	}
	
	@Override
	public void save() {
	}

	@Override
	public void setGuiData(CompoundTag compound) {
		if(compound.contains("EntityId")) {
			entityId = compound.getInt("EntityId");
			entity = player.level().getEntity(entityId);
		}
		else {
			tile = player.level().getBlockEntity(pos);
			state = player.level().getBlockState(pos);
			blockStack = state.getBlock().getCloneItemStack(player.level(), pos, state);
		}
		
		originalCompound = compound.getCompound("Data");
		this.compound = originalCompound.copy();
		init();
	}
}
