package noppes.npcs.client.gui.player;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.data.RecipeCarpentry;
import noppes.npcs.shared.client.gui.components.GuiButtonNextPage;
import noppes.npcs.shared.client.gui.components.GuiButtonNop;
import noppes.npcs.shared.client.gui.components.GuiLabel;

import java.util.ArrayList;
import java.util.List;



@Environment(EnvType.CLIENT)
public class GuiRecipes extends GuiNPCInterface
{
	private static final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/slot.png");
    private int page = 0;
    private boolean npcRecipes = true;
    private GuiLabel label;
    private GuiButtonNop left, right;
    private List<Recipe> recipes = new ArrayList<Recipe>();

    public GuiRecipes(){
        this.imageHeight = 182;
        this.imageWidth = 256;
        setBackground("recipes.png");
		recipes.addAll(RecipeController.instance.anvilRecipes.values());
    }
    @Override
    public void init(){
    	super.init();

    	addLabel(new GuiLabel(0, "Recipe List", guiLeft + 5, guiTop + 5));
    	addLabel(label = new GuiLabel(1, "", guiLeft + 5, guiTop + 168));

        addButton(this.left = new GuiButtonNextPage(this, 1, guiLeft + 150, guiTop + 164, true, (b) -> {
			page++;
			updateButton();
		}));
        addButton(this.right = new GuiButtonNextPage(this, 2, guiLeft + 80, guiTop + 164, false, (b) -> {
			page--;
			updateButton();
		}));
        
        updateButton();
    }
    private void updateButton(){
    	right.visible = right.active = page > 0;
    	left.visible = left.active = page + 1 < Mth.ceil(recipes.size() / 4f);
    }

    @Override
    public void render(GuiGraphics graphics, int xMouse, int yMouse, float f){
    	super.render(graphics, xMouse, yMouse, f);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, resource);
		
		label.setMessage(Component.literal(page + 1 + "/" + Mth.ceil(recipes.size() / 4f)));
		label.setX(guiLeft + (256 - Minecraft.getInstance().font.width(label.getMessage())) / 2);
		for(int i = 0; i < 4; i++){
			int index = i + page * 4;
			if(index >= recipes.size())
				break;
			Recipe irecipe = recipes.get(index);
			if(irecipe.getResultItem(player.level().registryAccess()).isEmpty())
				continue;
			int x = guiLeft + 5 + i / 2 * 126;
			int y = guiTop + 15 + i % 2 * 76;
			drawItem(graphics, irecipe.getResultItem(player.level().registryAccess()), x + 98, y + 28, xMouse, yMouse);
			if(irecipe instanceof RecipeCarpentry){
				RecipeCarpentry recipe = (RecipeCarpentry) irecipe;
				x += (72 - recipe.getWidth() * 18) / 2;
				y += (72 - recipe.getHeight() * 18) / 2;
				for(int j = 0; j < recipe.getWidth(); j++){
					for(int k = 0; k < recipe.getHeight(); k++){
						RenderSystem.setShader(GameRenderer::getPositionTexShader);
						RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
						RenderSystem.setShaderTexture(0, resource);
						graphics.blit(resource, x + j * 18, y + k * 18, 0, 0, 18, 18);
				        ItemStack item = recipe.getCraftingItem(j + k * recipe.getWidth());
				        if(item.isEmpty())
				        	continue;
				        drawItem(graphics, item, x + j * 18 + 1, y + k * 18 + 1, xMouse, yMouse);
					}
				}
			}
		}
		for(int i = 0; i < 4; i++){
			int index = i + page * 4;
			if(index >= recipes.size())
				break;
			Recipe irecipe = recipes.get(index);
			if(irecipe instanceof RecipeCarpentry){
				RecipeCarpentry recipe = (RecipeCarpentry) irecipe;
				if(recipe.getResultItem(player.level().registryAccess()).isEmpty())
					continue;
				int x = guiLeft + 5 + i / 2 * 126;
				int y = guiTop + 15 + i % 2 * 76;
				drawOverlay(graphics, recipe.getResultItem(player.level().registryAccess()), x + 98, y + 22, xMouse, yMouse);
				x += (72 - recipe.getWidth() * 18) / 2;
				y += (72 - recipe.getHeight() * 18) / 2;
				for(int j = 0; j < recipe.getWidth(); j++){
					for(int k = 0; k < recipe.getHeight(); k++){
				        ItemStack item = recipe.getCraftingItem(j + k * recipe.getWidth());
				        if(item.isEmpty())
				        	continue;
				        drawOverlay(graphics, item, x + j * 18 + 1, y + k * 18 + 1, xMouse, yMouse);
					}
				}
			}
		}
    }

    private void drawItem(GuiGraphics graphics, ItemStack item, int x, int y, int xMouse, int yMouse) {

		//RenderHelper.enableGUIStandardItemLighting();
		graphics.pose().pushPose();
		graphics.pose().translate(0,0,100.0);
		graphics.renderItem(item, x, y);
		graphics.renderItemDecorations(font, item, x, y);
		graphics.pose().popPose();
		//RenderHelper.disableStandardItemLighting();
	}
    
    private void drawOverlay(GuiGraphics graphics, ItemStack item, int x, int y, int xMouse, int yMouse){
        if (this.func_146978_c(x - guiLeft, y - guiTop, 16, 16, xMouse, yMouse)){

			graphics.renderTooltip(font, item, xMouse, yMouse);
        }
    }
    protected boolean func_146978_c(int p_146978_1_, int p_146978_2_, int p_146978_3_, int p_146978_4_, int p_146978_5_, int p_146978_6_)
    {
        int k1 = this.guiLeft;
        int l1 = this.guiTop;
        p_146978_5_ -= k1;
        p_146978_6_ -= l1;
        return p_146978_5_ >= p_146978_1_ - 1 && p_146978_5_ < p_146978_1_ + p_146978_3_ + 1 && p_146978_6_ >= p_146978_2_ - 1 && p_146978_6_ < p_146978_2_ + p_146978_4_ + 1;
    }

	@Override
	public void save() {
	}
}