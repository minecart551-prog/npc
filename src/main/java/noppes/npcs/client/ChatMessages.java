package noppes.npcs.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import noppes.npcs.CustomNpcs;
import noppes.npcs.IChatMessages;
import noppes.npcs.entity.EntityNPCInterface;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ChatMessages implements IChatMessages {
    private static Map<String,ChatMessages> users = new Hashtable<String,ChatMessages>();

    protected static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    private static final RenderStateShard.ShaderStateShard sharder = new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorLightmapShader);
    protected static final RenderType type = RenderType.create("chatbubble", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, false,RenderType.CompositeState.builder().setCullState(new RenderStateShard.CullStateShard(true)).setLightmapState(new RenderStateShard.LightmapStateShard(true)).setShaderState(sharder).createCompositeState(true));
    protected static final RenderType typeDepth = RenderType.create("chatbubbledepth", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder().setCullState(new RenderStateShard.CullStateShard(true)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setShaderState(sharder).setLightmapState(new RenderStateShard.LightmapStateShard(true)).setDepthTestState(new RenderStateShard.DepthTestStateShard("always", 519)).createCompositeState(false));


    private Map<Long,TextBlockClient> messages = new TreeMap<Long,TextBlockClient>();

    private int boxLength = 46;
    private float scale = 0.5f;

    private String lastMessage = "";
    private long lastMessageTime = 0;

    public void addMessage(String message, EntityNPCInterface npc){
        if(!CustomNpcs.EnableChatBubbles)
            return;
        long time = System.currentTimeMillis();
        if(message.equals(lastMessage) && lastMessageTime + 1000 > time){
            return;
        }
        Map<Long,TextBlockClient> messages = new TreeMap<>(this.messages);
        messages.put(time, new TextBlockClient(message, (int) (boxLength * 4), true, Minecraft.getInstance().player, npc));

        if(messages.size() > 3){
            messages.remove(messages.keySet().iterator().next());
        }
        this.messages = messages;
        lastMessage = message;
        lastMessageTime = time;
    }

    public void renderMessages(PoseStack PoseStack, MultiBufferSource typeBuffer, float textscale, boolean inRange, int lightmapUV){
        Map<Long,TextBlockClient> messages = getMessages();
        if(messages.isEmpty())
            return;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionColorLightmapShader);
        if(inRange)
            render(PoseStack, typeBuffer, typeBuffer.getBuffer(typeDepth), textscale,false, lightmapUV);
        render(PoseStack, typeBuffer, typeBuffer.getBuffer(type), textscale, true, lightmapUV);
    }

    public void render(PoseStack poseStack, MultiBufferSource typeBuffer, VertexConsumer ivertex, float textScale, boolean depth, int lightmapUV) {
        Font font = Minecraft.getInstance().font;
        float var14 = 0.016666668F * 1.6F;
        int size = 0;
        for(TextBlockClient block : messages.values())
            size += block.lines.size();
        Minecraft mc = Minecraft.getInstance();
        int textYSize = (int) (size * font.lineHeight * scale);

        poseStack.pushPose();
        poseStack.translate(0, textYSize * var14, 0);
        poseStack.scale(textScale, textScale, textScale);

        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-var14, -var14, var14);
        //RenderSystem.rotatef(-mc.getEntityRenderDispatcher().cameraOrientation().j(), 0.0F, 1F, 0.0F);
        //RenderSystem.rotatef(mc.getEntityRenderDispatcher().cameraOrientation().i(), 1F, 0.0F, 0.0F);
        //RenderSystem.scalef(-var14, -var14, var14);
        //RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

//        if(depth){
//            RenderSystem.enableDepthTest();
//        }
//        else{
//            RenderSystem.disableDepthTest();
//        }

        int black = depth?0xFF000000:0xFF000000;
        int white = depth?0xBBFFFFFF:0x44FFFFFF;
        //RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        PoseStack.Pose entry = poseStack.last();
        Matrix4f matrix = entry.pose();
        drawRect(ivertex, matrix, lightmapUV, -boxLength - 2, -2, boxLength + 2, textYSize + 1, white, 0.11f);

        drawRect(ivertex, matrix, lightmapUV, -boxLength - 1, -3, boxLength + 1, -2, black, 0.1f); //top
        drawRect(ivertex, matrix, lightmapUV, -boxLength - 1, textYSize + 2, -1, textYSize + 1, black, 0.1f); //bottom1
        drawRect(ivertex, matrix, lightmapUV, 3, textYSize + 2, boxLength + 1, textYSize + 1, black, 0.1f); //bottom2
        drawRect(ivertex, matrix, lightmapUV, -boxLength - 3, -1, -boxLength - 2, textYSize, black, 0.1f); //left
        drawRect(ivertex, matrix, lightmapUV, boxLength + 3, -1, boxLength + 2, textYSize, black, 0.1f); //right

        drawRect(ivertex, matrix, lightmapUV, -boxLength - 2, -2, -boxLength - 1, -1, black, 0.1f);
        drawRect(ivertex, matrix, lightmapUV, boxLength + 2, -2, boxLength + 1, -1, black, 0.1f);
        drawRect(ivertex, matrix, lightmapUV, -boxLength - 2, textYSize + 1, -boxLength - 1, textYSize, black, 0.1f);
        drawRect(ivertex, matrix, lightmapUV, boxLength + 2, textYSize + 1, boxLength + 1, textYSize, black, 0.1f);

        drawRect(ivertex, matrix, lightmapUV, 0, textYSize + 1, 3, textYSize + 4, white, 0.11f);
        drawRect(ivertex, matrix, lightmapUV, -1, textYSize + 4, 1, textYSize + 5, white, 0.11f);

        drawRect(ivertex, matrix, lightmapUV, -1, textYSize + 1, 0, textYSize + 4, black, 0.1f);
        drawRect(ivertex, matrix, lightmapUV, 3, textYSize + 1, 4, textYSize + 3, black, 0.1f);
        drawRect(ivertex, matrix, lightmapUV, 2, textYSize + 3, 3, textYSize + 4, black, 0.1f);
        drawRect(ivertex, matrix, lightmapUV, 1, textYSize + 4, 2, textYSize + 5, black, 0.1f);
        drawRect(ivertex, matrix, lightmapUV, -2, textYSize + 4, -1, textYSize + 5, black, 0.1f);

        drawRect(ivertex, matrix, lightmapUV, -2, textYSize + 5, 1, textYSize + 6, black, 0.1f);

        poseStack.scale(scale, scale, scale);
        int index = 0;
        for(TextBlockClient block : messages.values()){
            for(Component chat : block.lines){
                font.drawInBatch(chat, -font.width(chat) / 2, index * font.lineHeight, black, false, matrix, typeBuffer, !depth? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, 0, lightmapUV);
                //font.draw(PoseStack, message, -font.width(message) / 2, index * font.lineHeight, black);
                index++;
            }
        }
        poseStack.popPose();
    }

    public void drawRect(VertexConsumer ivertex, Matrix4f matrix, int lightmapUV, float x, float y, float x2, float y2, int color, float z)
    {
        float j1;

        if (x < x2)
        {
            j1 = x;
            x = x2;
            x2 = j1;
        }

        if (y < y2)
        {
            j1 = y;
            y = y2;
            y2 = j1;
        }

        float f1 = (float)(color >> 16 & 255) / 255.0F;
        float f2 = (float)(color >> 8 & 255) / 255.0F;
        float f3 = (float)(color & 255) / 255.0F;
        //RenderSystem.color4f(1, 1, 1, 1);
        draw(ivertex, matrix, lightmapUV, x, y, z,f1, f2, f3);
        draw(ivertex, matrix, lightmapUV, x, y2, z,f1, f2, f3);
        draw(ivertex, matrix, lightmapUV, x2, y2, z,f1, f2, f3);
        draw(ivertex, matrix, lightmapUV, x2, y, z,f1, f2, f3);
    }

    private void draw(VertexConsumer ivertex, Matrix4f matrix, int lightmapUV, float x, float y, float z, float red, float green, float blue){
        Vector4f v = new Vector4f(x, y, z, 1.0F);
        v.mul(matrix);
        ivertex.vertex(v.x(), v.y(), v.z()).color(red, green, blue, 1).uv2(lightmapUV).endVertex();
    }

    public static ChatMessages getChatMessages(String username){
        if(users.containsKey(username))
            return users.get(username);

        ChatMessages chat = new ChatMessages();
        users.put(username, chat);
        return chat;
    }

//    private static Pattern[] patterns = new Pattern[]{
//            Pattern.compile("^<+([a-zA-z0-9_]{2,16})>[:]? (.*)"),
//            Pattern.compile("^\\[.*[\\]]{1,16}[^a-zA-z0-9]?([a-zA-z0-9_]{2,16})[:]? (.*)"),
//            Pattern.compile("^[a-zA-z0-9_]{2,10}[^a-zA-z0-9]([a-zA-z0-9_]{2,16})[:]? (.*)"),
//    };
//
//    public static void parseMessage(String toParse){
//        toParse = toParse.replaceAll("\247.", "");
//        for(Pattern pattern : patterns){
//            Matcher m = pattern.matcher(toParse);
//            if(m.find()){
//                String username = m.group(1);
//                if(!validPlayer(username))
//                    continue;
//                String message = m.group(2);
//                getChatMessages(username).addMessage(message);
//                return;
//            }
//        }
//    }

//    public static void test() {
//        test("<Sirnoppes01> :)","Sirnoppes01: :)");
//        test("<Sirnoppes01> hey","Sirnoppes01: hey");
//        test("<Sir_noppes> hey","Sir_noppes: hey");
//        test("<Sirnoppes>: hey","Sirnoppes: hey");
//        test("[member]Sirnoppes: hey","Sirnoppes: hey");
//        test("[member]Sirnoppes01: hey","Sirnoppes01: hey");
//        test("[member]Sir_noppes: hey","Sir_noppes: hey");
//        test("[member] Sirnoppes: hey","Sirnoppes: hey");
//        test("[g][member]Sirnoppes: hey","Sirnoppes: hey");
//        test("[g] [member]Sirnoppes: hey","Sirnoppes: hey");
//        test("[g] [member]-Sirnoppes: hey","Sirnoppes: hey");
//        test("[Player755: Teleported Player755 to Player885]","");
//        test("member Sirnoppes: hey","Sirnoppes: hey");
//        test("member-Sirnoppes: hey","Sirnoppes: hey");
//        test("member: Sirnoppes: hey","");
//    }
//    private static void test(String toParse, String result) {
//        for(Pattern pattern : patterns){
//            Matcher m = pattern.matcher(toParse);
//            if(m.find()){
//                String username = m.group(1);
//                String message = m.group(2);
//                if(message == null || username == null)
//                    continue;
//                else if(result.isEmpty()){
//                    System.err.println("failed: " + toParse + " - " + username + ": " + message);
//                    return;
//                }
//                if((username +": " + message).equals(result)){
//                    System.out.println("success: " + toParse);
//                    return;
//                }
//            }
//        }
//        if(result.isEmpty())
//            System.out.println("success: " + toParse);
//        else
//            System.err.println("failed: " + toParse);
//    }

    private static boolean validPlayer(String username){
        for (Player player : Minecraft.getInstance().level.players()) {
            if (username.equals(player.getName()) || username.equals(player.getDisplayName().getString())) {
                return true;
            }
        }

        return false;
    }


    private Map<Long, TextBlockClient> getMessages(){
        Map<Long, TextBlockClient> messages = new TreeMap<Long, TextBlockClient>();
        long time = System.currentTimeMillis();
        for(Entry<Long, TextBlockClient> entry : this.messages.entrySet()){
            if(time > entry.getKey() + 10000)
                continue;
            messages.put(entry.getKey(), entry.getValue());
        }
        return this.messages = messages;
    }

    public boolean hasMessage() {
        return !messages.isEmpty();
    }

}
