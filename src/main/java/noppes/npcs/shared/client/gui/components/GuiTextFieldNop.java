package noppes.npcs.shared.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import noppes.npcs.shared.client.gui.listeners.ITextfieldListener;

public class GuiTextFieldNop extends EditBox {
	public boolean enabled = true;
	public boolean inMenu = true;
	public boolean numbersOnly = false;
	public boolean floatsOnly = false;
	public ITextfieldListener listener;
	public int id;
	public int min = 0,max = Integer.MAX_VALUE,def = 0;
	public float minF = 0,maxF = Float.MAX_VALUE,defF = 0;
	private static GuiTextFieldNop activeTextfield = null;
	private String initialValue;
	private final int[] allowedSpecialChars = {14,211,203,205};

	public GuiTextFieldNop(int id, Screen parent, int i, int j, int k, int l, String s) {
		this(id, parent, i, j, k, l, Component.translatable(s != null ? s : ""));
	}
	public GuiTextFieldNop(int id, Screen parent, int i, int j, int k, int l, Component s) {
		super(Minecraft.getInstance().font, i, j, k, l, s);
		setMaxLength(500);
		if(!s.getString().isEmpty()){
			initialValue = s.getString();
			this.setValue(s.getString());
		}
		this.id = id;
		if(parent instanceof ITextfieldListener)
			listener = (ITextfieldListener) parent;
	}

	public static boolean isAnyActive(){
		return activeTextfield != null;
	}

	public static GuiTextFieldNop getActive() {
		return activeTextfield;
	}

	private boolean charAllowed(char c, int i){
		if(!numbersOnly || Character.isDigit(c))
			return true;
		for(int j : allowedSpecialChars)
			if(j == i)
				return true;

		return false;
	}

	@Override
	public boolean charTyped(char c, int i) {
		if(!charAllowed(c,i))
			return false;
		return super.charTyped(c, i);
	}
	public boolean isEmpty(){
		return getValue().trim().length() == 0;
	}

	public int getInteger(){
		return Integer.parseInt(getValue());
	}
	public boolean isInteger(){
		try{
			Integer.parseInt(getValue());
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}

	public boolean isFloat() {
		try {
			Float.parseFloat(getValue());
			return true;
		} catch (NumberFormatException var2) {
			return false;
		}
	}
	public float getFloat() {
		return Float.parseFloat(getValue());
	}

	@Override
	public boolean mouseClicked(double i, double j, int k){
		if(!enabled){
			return false;
		}
		boolean wasFocused = this.isFocused();
		boolean flag = i >= (double)this.getX() && i < (double)(this.getX() + this.width) && j >= (double)this.getY() && j < (double)(this.getY() + this.height);
		this.setFocused(flag);
		boolean clicked = super.mouseClicked(i, j, k);
		if(!wasFocused && isFocused()){
			GuiTextFieldNop.unfocus();
			activeTextfield = this;
		}
		if(wasFocused && !isFocused()){
			unFocused();
		}
		return clicked;
	}

	public void unFocused(){
		if(numbersOnly){
			if(isEmpty() || !isInteger())
				setValue(def + "");
			else if( getInteger() < min)
				setValue(min+"");
			else if(getInteger() > max)
				setValue(max+"");
		}
		if(floatsOnly){
			if(isEmpty() || !isFloat())
				setValue(defF + "");
			else if( getFloat() < minF)
				setValue(minF+"");
			else if(getFloat() > maxF)
				setValue(maxF+"");
		}

		if(listener != null)
			listener.unFocused(this);

		//setFocus(false); TODO
		if(this == activeTextfield)
			activeTextfield = null;
	}

	public int getTextColor(){
		if(numbersOnly || floatsOnly){
			if(numbersOnly && (!isInteger() || getInteger()<min || getInteger()>max)){
				return 0xfc0345;
			}
			if(floatsOnly && (!isFloat() || getFloat()<minF || getFloat()>maxF)){
				return 0xfc0345;
			}
		}
		return 14737632;
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int x, int y, float f) {
		if(enabled) {
			setTextColor(getTextColor());
			super.renderWidget(graphics, y, x, f);
		}
	}
	public GuiTextFieldNop setMinMaxDefault(int i, int j, int k) {
		min = i;
		max = j;
		def = k;
		return this;
	}
	public GuiTextFieldNop setMinMaxDefault(float i, float j, float k) {
		minF = i;
		maxF = j;
		defF = k;
		return this;
	}
	public static void unfocus() {
		GuiTextFieldNop field = activeTextfield;
		activeTextfield = null;//prevent infinite loop, set null before calling unfocused
		if(field != null)
			field.unFocused();
	}


    public GuiTextFieldNop setNumbersOnly() {
		this.numbersOnly = true;
		return this;
    }
	public GuiTextFieldNop setFloatsOnly() {
		this.floatsOnly = true;
		return this;
	}
}
