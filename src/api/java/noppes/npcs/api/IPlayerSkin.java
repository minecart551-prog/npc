package noppes.npcs.api;

import java.util.List;

public interface IPlayerSkin {
    boolean isMale();

    IPlayerSkin setMale(boolean male);

    int getBodyType();

    IPlayerSkin setBodyType(int type);

    int getBodyColor();

    IPlayerSkin setBodyColor(int bodyColor);

    int getHairType();

    IPlayerSkin setHairType(int type);

    int getHairColor();

    IPlayerSkin setHairColor(int hairColor);

    int getFaceType();

    IPlayerSkin setFaceType(int type);

    int getEyesColor();

    IPlayerSkin setEyesColor(int eyesColor);

    int getPantsType();

    IPlayerSkin setPantsType(int type);

    int getJacketType();

    IPlayerSkin setJacketType(int type);

    int getShoesType();

    IPlayerSkin setShoesType(int type);

    List<Integer> getPeculiarities();

    IPlayerSkin setPeculiarities(List<Integer> peculiarities);
}
