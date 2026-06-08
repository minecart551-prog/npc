package noppes.npcs.client.gui.player;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.TextBlockClient;
import noppes.npcs.client.gui.player.tabs.InventoryTabFactions;
import noppes.npcs.client.gui.player.tabs.InventoryTabQuests;
import noppes.npcs.client.gui.player.tabs.InventoryTabVanilla;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.shared.client.gui.components.GuiButtonNextPage;
import noppes.npcs.shared.client.gui.components.GuiCustomScrollNop;
import noppes.npcs.shared.client.gui.components.GuiMenuSideButton;
import noppes.npcs.shared.client.gui.listeners.ICustomScrollListener;
import noppes.npcs.shared.client.gui.listeners.ITopButtonListener;
import noppes.npcs.shared.common.util.NaturalOrderComparator;

import java.util.*;


public class GuiQuestLog extends GuiNPCInterface implements ITopButtonListener, ICustomScrollListener {

	private final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/standardbg.png");

	public HashMap<String, List<Quest>> activeQuests = new HashMap<String, List<Quest>>();
	private HashMap<String, Quest> categoryQuests = new HashMap<String, Quest>();
	public Quest selectedQuest = null;
	public Component selectedCategory = Component.empty();
    private Player player;
    private GuiCustomScrollNop scroll;
	private HashMap<Integer, GuiMenuSideButton> sideButtons = new HashMap<Integer,GuiMenuSideButton>();
	private boolean noQuests = false;
	
	private final int maxLines = 10;
	private int currentPage = 0;
	private int maxPages = 1;

	TextBlockClient textblock = null;
	
	private Minecraft mc = Minecraft.getInstance();
	
	public GuiQuestLog(Player player) {
		super();
		this.player = player;
        imageWidth = 280;
        imageHeight = 180;
        drawDefaultBackground = false;
	}
	
	@Override
    public void init(){
        super.init();
        for(Quest quest : PlayerQuestController.getActiveQuests(player)){
    		String category = quest.category.title;
    		if(!activeQuests.containsKey(category))
    			activeQuests.put(category, new ArrayList<Quest>());
    		List<Quest> list = activeQuests.get(category);
    		list.add(quest);
        }
        
    	sideButtons.clear();
        guiTop +=10;


		addRenderableWidget(new InventoryTabVanilla().init(this));
		addRenderableWidget(new InventoryTabFactions().init(this));
		addRenderableWidget(new InventoryTabQuests().init(this));
        
        noQuests = false;

        if(activeQuests.isEmpty()){
        	noQuests = true;
        	return;
        }
        List<String> categories = new ArrayList<String>();
        categories.addAll(activeQuests.keySet());
        Collections.sort(categories, new NaturalOrderComparator());
        int i = 0;
        for(String category : categories){
        	if(Objects.equals(selectedCategory, Component.empty()))
        		selectedCategory = Component.translatable(category);
        	sideButtons.put(i, new GuiMenuSideButton(this, i,guiLeft - 69, this.guiTop +2 + i*21, 70,22, category));
        	i++;
        }
        sideButtons.get(categories.indexOf(selectedCategory.getString())).active = true;
        
        if(scroll == null)
        	scroll = new GuiCustomScrollNop(this,0);

    	HashMap<String, Quest> categoryQuests = new HashMap<String, Quest>();
        for(Quest q : activeQuests.get(selectedCategory.getString())){
        	categoryQuests.put(q.title, q);
        }
        this.categoryQuests = categoryQuests;
        
        scroll.setList(new ArrayList<String>(categoryQuests.keySet()));
        scroll.setSize(134, 174);
        scroll.guiLeft = guiLeft + 5;
        scroll.guiTop = guiTop + 15;
        addScroll(scroll);

        addButton(new GuiButtonNextPage(this, 1, guiLeft + 286, guiTop + 114, true, (b) ->{
			currentPage++;
			init();
		}));
        addButton(new GuiButtonNextPage(this, 2, guiLeft + 144, guiTop + 114, false, (b) -> {
			currentPage--;
			init();
		}));

        getButton(1).visible = selectedQuest != null && currentPage < (maxPages - 1);
        getButton(2).visible = selectedQuest != null && currentPage > 0;
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    	if(scroll != null)
    		scroll.visible = !noQuests;
		PoseStack matrixStack = graphics.pose();
    	renderBackground(graphics);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, resource);
		graphics.blit(resource, guiLeft, guiTop, 0, 0, 252, 195);
		graphics.blit(resource,guiLeft + 252, guiTop, 188, 0, 67, 195);
        super.render(graphics, mouseX, mouseY, partialTicks);
        
        if(noQuests){
			graphics.drawString(mc.font, Component.translatable("quest.noquests"),guiLeft + 84,guiTop + 80, CustomNpcResourceListener.DefaultTextColor, false);
        	return;
        }
        for(GuiMenuSideButton button: sideButtons.values().toArray(new GuiMenuSideButton[sideButtons.size()])){
        	button.render(graphics, mouseX, mouseY, partialTicks);
        }
		graphics.drawString(mc.font, selectedCategory, guiLeft + 5,guiTop + 5, CustomNpcResourceListener.DefaultTextColor, false);

        if(selectedQuest == null)
        	return;

    	drawProgress(graphics);

    	drawQuestText(graphics);

		matrixStack.pushPose();
		matrixStack.translate(guiLeft + 148, guiTop, 0);
		matrixStack.scale(1.24f, 1.24f, 1.24f);
		Component title = Component.translatable(selectedQuest.title);
		graphics.drawString(mc.font, title, (130 - font.width(title)) / 2, 4, CustomNpcResourceListener.DefaultTextColor, false);
		matrixStack.popPose();
        graphics.hLine(guiLeft + 142, guiLeft + 312, guiTop + 17,  + 0xFF000000 + CustomNpcResourceListener.DefaultTextColor);
    }
    
    private void drawQuestText(GuiGraphics graphics){
    	if(textblock == null)
    		return;
        int yoffset = guiTop + 5; 
    	for(int i = 0; i < maxLines; i++){
    		int index = i + currentPage * maxLines;
    		if(index >= textblock.lines.size())
    			continue;
			Component text = textblock.lines.get(index);
			graphics.drawString(font, text, guiLeft + 142, guiTop + 20 + (i * font.lineHeight), CustomNpcResourceListener.DefaultTextColor, false);
    	}
    }
    
    private void drawProgress(GuiGraphics graphics) {
		Component title = Component.translatable("quest.objectives").append(":");
		graphics.drawString(mc.font, title, guiLeft + 142, guiTop + 130, CustomNpcResourceListener.DefaultTextColor, false);
		graphics.hLine( guiLeft + 142, guiLeft + 312, guiTop + 140,  + 0xFF000000 + CustomNpcResourceListener.DefaultTextColor);
    	
    	int yoffset = guiTop + 144;
        for(IQuestObjective objective : selectedQuest.questInterface.getObjectives(player)){
			graphics.drawString(mc.font, Component.literal("- ").append(objective.getMCText()), guiLeft + 142, yoffset , CustomNpcResourceListener.DefaultTextColor, false);
	        yoffset += 10;
        }

		graphics.hLine( guiLeft + 142, guiLeft + 312, guiTop + 178,  + 0xFF000000 + CustomNpcResourceListener.DefaultTextColor);
        String complete = selectedQuest.getNpcName();
        if(complete != null && !complete.isEmpty()) {
			graphics.drawString(mc.font, Component.translatable("quest.completewith", complete), guiLeft + 142, guiTop + 182, CustomNpcResourceListener.DefaultTextColor, false);
        }
	}

    @Override
    public boolean mouseClicked(double i, double j, int k){
    	super.mouseClicked(i, j, k);
        if (k == 0){
        	if(scroll != null)
        		scroll.mouseClicked(i, j, k);
            for (GuiMenuSideButton button : new ArrayList<GuiMenuSideButton>(sideButtons.values())){
                if (button.mouseClicked(i, j, k)){
                	sideButtonPressed(button);
                	return true;
                }
            }
        }
        return false;
    }
    
    private void sideButtonPressed(GuiMenuSideButton button) {
    	if(button.active)
    		return;
    	NoppesUtil.clickSound();
        selectedCategory = button.getMessage();
        selectedQuest = null;
        this.init();
    }
    
	@Override
	public void scrollClicked(double i, double j, int k, GuiCustomScrollNop scroll) {
		if(!scroll.hasSelected())
			return;
		selectedQuest = categoryQuests.get(scroll.getSelected());
		textblock = new TextBlockClient(selectedQuest.getLogText(), 172, true, player);
    	if(textblock.lines.size() > maxLines) {
    		maxPages = Mth.ceil(1f * textblock.lines.size() / maxLines);
    	}
		currentPage = 0;
		init();
	}

    @Override
    public boolean isPauseScreen(){
        return false;
    }
    
	@Override
	public void save() {
		
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScrollNop scroll) {}
	
}
