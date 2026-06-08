package noppes.npcs.client.gui.player;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.containers.ContainerMail;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketPlayerMailDelete;
import noppes.npcs.packets.server.SPacketPlayerMailSend;
import noppes.npcs.shared.client.gui.components.GuiButtonNextPage;
import noppes.npcs.shared.client.gui.components.GuiButtonNop;
import noppes.npcs.shared.client.gui.components.GuiLabel;
import noppes.npcs.shared.client.gui.components.GuiTextFieldNop;
import noppes.npcs.shared.client.gui.listeners.IGuiClose;
import noppes.npcs.shared.client.gui.listeners.IGuiError;
import noppes.npcs.shared.client.gui.listeners.ITextfieldListener;

@Environment(EnvType.CLIENT)
public class GuiMailmanWrite extends GuiContainerNPCInterface<ContainerMail> implements ITextfieldListener, IGuiError, IGuiClose
{
    private static final ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
    private static final ResourceLocation bookWidgets = new ResourceLocation("textures/gui/widgets.png");
    private static final ResourceLocation bookInventory = new ResourceLocation("textures/gui/container/inventory.png");

    private final TextFieldHelper pageEdit = new TextFieldHelper(this::getText, this::setText, this::getClipboard, this::setClipboard, (p_238774_1_) -> {
        return p_238774_1_.length() < 1024 && this.font.wordWrapHeight(p_238774_1_, 114) <= 128;
    });

    /** Update ticks since the gui was opened */
    private int updateCount;
    private int bookImageWidth = 192;
    private int bookImageHeight = 192;
    private int bookTotalPages = 1;
    private int currPage;
    private ListTag bookPages;
    private GuiButtonNextPage buttonNextPage;
    private GuiButtonNextPage buttonPreviousPage;
    
    private final boolean canEdit;
    private final boolean canSend;
    private boolean hasSend = false;

    /** The GuiButton to sign this book. */
    
    public static Screen parent;
	public static PlayerMail mail = new PlayerMail();
    
    private Minecraft mc = Minecraft.getInstance();

	private String username = "";
	private GuiLabel error;

    public GuiMailmanWrite(ContainerMail container, Inventory inv, Component titleIn) {
        super(null, container, inv, titleIn);
    	title = "";
        this.canEdit = container.canEdit;
        this.canSend = container.canSend;

        if(mail.message.contains("pages"))
        	this.bookPages = mail.message.getList("pages", 8);

        if (this.bookPages != null)
        {
            this.bookPages = (ListTag)this.bookPages.copy();
            this.bookTotalPages = this.bookPages.size();

            if (this.bookTotalPages < 1)
            {
                this.bookTotalPages = 1;
            }
        }
        else
        {
            this.bookPages = new ListTag();
            this.bookPages.add(StringTag.valueOf(""));
            this.bookTotalPages = 1;
        }
        imageWidth = 360;
        imageHeight = 260;
        drawDefaultBackground = false;
    }

    @Override
    public void containerTick() {
        ++this.updateCount;
    }

    @Override
    public void init() {
    	super.init();
        this.renderables.clear();

		if(canEdit && !canSend)
			addLabel(new GuiLabel(0, "mailbox.sender", guiLeft + 170, guiTop + 32, 0));
		else
			addLabel(new GuiLabel(0, "mailbox.username", guiLeft + 170, guiTop + 32, 0));
		
		if(canEdit && !canSend)
			addTextField(new GuiTextFieldNop(2, this,  guiLeft + 170, guiTop + 42, 114, 20, mail.sender));
		else if(canEdit)
			addTextField(new GuiTextFieldNop(0, this,  guiLeft + 170, guiTop + 42, 114, 20, username));
		else
			addLabel(new GuiLabel(10, mail.sender, guiLeft + 170, guiTop + 42, 0));

		addLabel(new GuiLabel(1, "mailbox.subject", guiLeft + 170, guiTop + 72, 0));
		if(canEdit)
			addTextField(new GuiTextFieldNop(1, this,  guiLeft + 170, guiTop + 82, 114, 20, mail.subject));
		else
			addLabel(new GuiLabel(11, mail.subject, guiLeft + 170, guiTop + 82, 0));

		addLabel(error = new GuiLabel(2, "", guiLeft + 170, guiTop + 114, 0xFF0000));
		if(canEdit && !canSend)
			addButton(new GuiButtonNop(this, 0, this.guiLeft + 200, guiTop + 171, 60, 20, "gui.done"));
		else if(canEdit)
			addButton(new GuiButtonNop(this, 0, this.guiLeft + 200, guiTop + 171, 60, 20, "mailbox.send"));
		
		if(!canEdit && !canSend)
			addButton(new GuiButtonNop(this, 4, this.guiLeft + 200, guiTop + 171, 60, 20, "selectServer.delete"));
		if(!canEdit || canSend)
			addButton(new GuiButtonNop(this, 3, this.guiLeft + 200, guiTop + 194, 60, 20, "gui.cancel"));

        this.addButton(this.buttonNextPage = new GuiButtonNextPage(this, 1, guiLeft + 120, guiTop + 156, true, (b) ->{
            if (this.currPage < this.bookTotalPages - 1)
            {
                ++this.currPage;
            }
            else if (this.canEdit)
            {
                this.addNewPage();

                if (this.currPage < this.bookTotalPages - 1)
                {
                    ++this.currPage;
                }
            }
            this.updateButtons();
        }));
        this.addButton(this.buttonPreviousPage = new GuiButtonNextPage(this, 2, guiLeft + 38, guiTop + 156, false, (b) -> {
            if (this.currPage > 0)
            {
                --this.currPage;
            }
            this.updateButtons();
        }));
        this.updateButtons();
    }

	@Override
    public void onClose()
    {

    }

    private void updateButtons()
    {
        this.buttonNextPage.visible = this.currPage < this.bookTotalPages - 1 || this.canEdit;
        this.buttonPreviousPage.visible = this.currPage > 0;
    }

    @Override
    public void buttonEvent(GuiButtonNop par1GuiButton) {
        if (par1GuiButton.active) {
        	int id = par1GuiButton.id;
    		if(id == 0){
    			mail.message.put("pages", bookPages);
    	    	if(canSend) {
    	    		if(!hasSend) {
    	    			hasSend = true;
        	    		Packets.sendServer(new SPacketPlayerMailSend(this.username, mail.writeNBT()));
    	    		}
    	    	}
    	    	else {
    	    		close();
    	    	}
    		}
            if (id == 3)
            {
                close();
            }
            if (id == 4)
            {
                ConfirmScreen guiyesno = new ConfirmScreen((flag) -> {
                    if(flag){
                        Packets.sendServer(new SPacketPlayerMailDelete(mail.time, mail.sender));
                        close();
                    }
                    else
                        NoppesUtil.openGUI(player, this); }, Component.literal(""), Component.translatable(I18n.get("gui.deleteMessage")));
                setScreen(guiyesno);
            }

            this.updateButtons();
        }
    }

    private void addNewPage()
    {
        if (this.bookPages != null && this.bookPages.size() < 50)
        {
            this.bookPages.add(StringTag.valueOf(""));
            ++this.bookTotalPages;
        }
    }

    @Override
    public boolean charTyped(char par1, int limbSwingAmount) {
    	if(!GuiTextFieldNop.isAnyActive() && canEdit) {
            if (SharedConstants.isAllowedChatCharacter(par1)) {
                this.pageEdit.insertText(Character.toString(par1));
                return true;
            }
        }
    	else
    		super.charTyped(par1, limbSwingAmount);
    	return true;
    }

    @Override
    public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
        if (super.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_)) {
            return true;
        } else {
            boolean flag = this.bookKeyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
            if (flag) {
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean bookKeyPressed(int p_214230_1_, int p_214230_2_, int p_214230_3_) {
        if (Screen.isSelectAll(p_214230_1_)) {
            this.pageEdit.selectAll();
            return true;
        } else if (Screen.isCopy(p_214230_1_)) {
            this.pageEdit.copy();
            return true;
        } else if (Screen.isPaste(p_214230_1_)) {
            this.pageEdit.paste();
            return true;
        } else if (Screen.isCut(p_214230_1_)) {
            this.pageEdit.cut();
            return true;
        } else {
            switch(p_214230_1_) {
                case 257:
                case 335:
                    this.pageEdit.insertText("\n");
                    return true;
                case 259:
                    this.pageEdit.removeCharsFromCursor(-1);
                    return true;
                case 261:
                    this.pageEdit.removeCharsFromCursor(1);
                    return true;
                case 262:
                    this.pageEdit.moveByChars(1, Screen.hasShiftDown());
                    return true;
                case 263:
                    this.pageEdit.moveByChars(-1, Screen.hasShiftDown());
                    return true;
                case 266:
                    this.buttonPreviousPage.onPress();
                    return true;
                case 267:
                    this.buttonNextPage.onPress();
                    return true;
                default:
                    return false;
            }
        }
    }

    private String getText() {
        if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.size()) {
            return this.bookPages.getString(this.currPage);
        }
        else {
            return "";
        }
    }

    private void setText(String par1Str) {
        if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.size()) {
        	bookPages.set(currPage, StringTag.valueOf(par1Str));
        }
    }

    private void setClipboard(String p_238760_1_) {
        if (this.minecraft != null) {
            TextFieldHelper.setClipboardContents(this.minecraft, p_238760_1_);
        }

    }

    private String getClipboard() {
        return this.minecraft != null ? TextFieldHelper.getClipboardContents(this.minecraft) : "";
    }

    private void append(String par1Str) {
        String s1 = this.getText();
        String s2 = s1 + par1Str;
        int i = mc.font.wordWrapHeight(s2 + "" + ChatFormatting.BLACK + "_", 118);

        if (i <= 118 && s2.length() < 256)
        {
            this.setText(s2);
        }
    }

	@Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float par3) {
        super.renderBackground(graphics);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, bookGuiTextures);
        graphics.blit(bookGuiTextures, guiLeft + 130, guiTop + 22, 0, 0, this.bookImageWidth, this.bookImageHeight / 3);
        graphics.blit(bookGuiTextures,guiLeft + 130, guiTop + 22 + bookImageHeight/3, 0, bookImageHeight / 2, this.bookImageWidth, this.bookImageHeight / 2);

        graphics.blit(bookGuiTextures,guiLeft, guiTop + 2, 0, 0, this.bookImageWidth, this.bookImageHeight);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, bookInventory);
        graphics.blit(bookInventory,guiLeft + 20, guiTop + 173, 0, 82, 180, 55);
        graphics.blit(bookInventory,guiLeft + 20, guiTop + 228, 0, 140, 180, 28);
        String s;
        String s1;
        int l;

        s = I18n.get("book.pageIndicator", new Object[] {Integer.valueOf(this.currPage + 1), Integer.valueOf(this.bookTotalPages)});
        s1 = "";

        if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.size())
        {
            s1 = this.bookPages.getString(this.currPage);
        }
        
        if(canEdit){
	        if (this.updateCount / 6 % 2 == 0)
	        {
	            s1 = s1 + "_";
	        }
	        else if (this.updateCount / 6 % 2 == 0)
	        {
	            s1 = s1 + "" + ChatFormatting.BLACK + "_";
	        }
	        else
	        {
	            s1 = s1 + "" + ChatFormatting.GRAY + "_";
	        }
        }

        l = mc.font.width(s);
        graphics.drawString(mc.font, s, guiLeft - l + this.bookImageWidth - 44, guiTop + 18, 0);
        graphics.drawWordWrap(mc.font, Component.translatable(s1), guiLeft + 36, guiTop + 18 + 16, 116, 0);

        graphics.fillGradient(guiLeft + 175, guiTop + 136, guiLeft + 269, guiTop + 154, -1072689136, -804253680);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, bookWidgets);
        for(int i = 0; i < 4; i++)
            graphics.blit(bookWidgets,guiLeft + 175 + i * 24, guiTop + 134, 0, 22, 24, 24);
        
        super.render(graphics, mouseX, mouseY, par3);
        
    }

    public void close(){
    	mc.setScreen(parent);
    	parent = null;
    	mail = new PlayerMail();
    }

	@Override
	public void unFocused(GuiTextFieldNop textfield) {
		if(textfield.id == 0)
			username = textfield.getValue();
		if(textfield.id == 1)
			mail.subject = textfield.getValue();
		if(textfield.id == 2)
			mail.sender = textfield.getValue();
	}

	@Override
	public void setError(int i, CompoundTag data) {
		if(i == 0)
			error.setMessage(Component.translatable("mailbox.errorUsername"));
		if(i == 1)
			error.setMessage(Component.translatable("mailbox.errorSubject"));
		hasSend = false;
	}

	@Override
	public void setClose(CompoundTag data) {
		player.sendSystemMessage(Component.translatable("mailbox.succes", data.getString("username")));
	}

	@Override
	public void save() {
		
	}
}
