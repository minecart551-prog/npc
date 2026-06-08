package noppes.npcs.shared.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import noppes.npcs.shared.common.util.NaturalOrderComparator;

import java.util.*;

public class GuiStringSlotNop<E extends GuiStringSlotNop.ListEntry> extends ObjectSelectionList {

    public HashSet<String> selectedList;
    private boolean multiSelect;
    private GuiBasic parent;
    public GuiStringSlotNop(Collection<String> list, GuiBasic parent, boolean multiSelect){
        super(Minecraft.getInstance(), parent.width, parent.height, 32, parent.height - 64, parent.getFontRenderer().lineHeight + 3);
        selectedList = new HashSet<String>();
        this.parent = parent;
        this.multiSelect = multiSelect;
        if(list != null){
            setList(list);
        }
    }

    public void setList(Collection<String> l){
        clearEntries();
        List<String> list = new ArrayList<>(l);
        Collections.sort(list, new NaturalOrderComparator());
        for(String s : list){
            this.addEntry(new ListEntry(s));
        }
        setSelected((ListEntry)null);
    }

    public void setColoredList(Map<String, Integer> m){
        clearEntries();
        List<String> list = new ArrayList<>(m.keySet());
        Collections.sort(list, new NaturalOrderComparator());
        for(String s : list){
            this.addEntry(new ListEntry(s, m.get(s)));
        }
        setSelected((ListEntry)null);
    }

    public void setSelected(String s){
        if(s == null){
            setSelected((ListEntry)null);
        }
        else{
            for(Object e : children()){
                if(((ListEntry)e).data.equals(s)){
                    setSelected((ListEntry)e);
                }
            }
        }
    }

    public String getSelectedString(){
        if(getSelected() == null){
            return null;
        }
        return ((ListEntry)getSelected()).data;
    }

    @Override
    protected boolean isSelectedItem(int i) {
    	if(!multiSelect){
	        return super.isSelectedItem(i);
    	}
        return selectedList.contains(((ListEntry)getEntry(i)).data);
    }

//    protected int getItemCount() {
//        return list.size();
//    }

//    @Override
//    protected int getContentHeight()
//    {
//        return list.size() * size;
//    }


    @Override
    protected void renderBackground(GuiGraphics graphics) {
        parent.renderBackground(graphics);
    }

//    @Override
//	protected void drawSlot(int i, int j, int k, int l, int var6, int var7, float partialTick) {
//    	String s = list.get(i);
//    	//if(!parent.drawSlot(i, j, k, l, tessellator, s))
//    	parent.draw(parent.getFontRenderer(), s, j + 50, k + 3, 0xFFFFFF);
//    }

	public void clear() {
		clearEntries();
	}

    public class ListEntry extends ObjectSelectionList.Entry {
        public final String data;
        public final int color;
        public ListEntry(String data){
            this.data = data;
            this.color = 0xFFFFFF;
        }
        public ListEntry(String data, int color){
            this.data = data;
            this.color = color;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int rowTop, int rowBottom, int width, int height, int mouseX, int mouseY, boolean mouseOver, float partialTicks) {
            //GuiComponent.drawString(mStack, parent.getFontRenderer(), this.data, rowBottom, rowTop, 10526880);
            graphics.drawString(parent.getFontRenderer(), data, rowBottom, rowTop, color);
        }


        private long prevTime = 0;
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
//        GuiSelectLevel.onElementSelected(parentLevelGui, i);
//        boolean flag1 = GuiSelectLevel.getSelectedLevel(parentLevelGui) >= 0 && GuiSelectLevel.getSelectedLevel(parentLevelGui) < getSize();
//        GuiSelectLevel.getSelectButton(parentLevelGui).enabled = flag1;
//        GuiSelectLevel.getRenameButton(parentLevelGui).enabled = flag1;
//        GuiSelectLevel.getDeleteButton(parentLevelGui).enabled = flag1;
//        if(flag && flag1)
//        {
//            parentLevelGui.selectLevel(i);
//        }
            long time = System.currentTimeMillis();
            ListEntry s = (ListEntry) GuiStringSlotNop.this.getSelected();
            if(s == this && time - prevTime < 400 ) {
                parent.doubleClicked();
            }
            prevTime = time;

            GuiStringSlotNop.this.setSelected(this);
            if(selectedList.contains(data))
                selectedList.remove(data);
            else
                selectedList.add(data);
            parent.elementClicked();
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.literal(data);
        }
    }
}
