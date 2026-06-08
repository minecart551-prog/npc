package noppes.npcs.shared.common.util;

public class ColorUtil {

    public static NopVector3f colorToRgb(int color){
        return new NopVector3f(new float[]{(color >> 16 & 255) / 255f, (color >> 8  & 255) / 255f, (color & 255) / 255f});
    }

    public static int rgbToColor(NopVector3f color){
        int r = (int)(color.x * 255) << 16;
        int g = (int)(color.y * 255) << 8;
        int b = (int)(color.z * 255);
        return r + g + b;
    }

    public static String colorToHex(int color) {
        String str = Integer.toHexString(color);
        while(str.length() < 6)
            str = "0" + str;

        return str;
    }

    public static int hexToColor(String hex){
        try{
            return Integer.parseInt(hex,16);
        }
        catch(NumberFormatException e){
        }
        return 0xFFFFFF;
    }
}
