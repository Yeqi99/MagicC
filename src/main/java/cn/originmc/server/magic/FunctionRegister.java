package cn.originmc.server.magic;

import cn.originmc.server.Main;
import cn.originmc.server.magic.function.world.FillHeightFunction;
import cn.originmc.server.magic.function.world.WorldFunction;

public class FunctionRegister {
    public static void register() {
        Main.magicManager.registerFunction(new WorldFunction());
        Main.magicManager.registerFunction(new FillHeightFunction(),"fh");
    }
}
