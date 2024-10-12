package cn.originmc.server.magic.function.world;

import cn.origincraft.magic.expression.functions.FunctionResult;
import cn.origincraft.magic.function.ArgsFunction;
import cn.origincraft.magic.function.ArgsSetting;
import cn.origincraft.magic.function.results.BooleanResult;
import cn.origincraft.magic.function.results.NullResult;
import cn.origincraft.magic.function.results.NumberResult;
import cn.origincraft.magic.function.results.StringResult;
import cn.origincraft.magic.object.SpellContext;
import cn.originmc.server.Main;
import cn.originmc.server.magic.result.WorldResult;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.List;

public class FillHeightFunction extends ArgsFunction {
    @Override
    public FunctionResult whenFunctionCalled(SpellContext spellContext, List<FunctionResult> list, ArgsSetting argsSetting) {
        String id = argsSetting.getId();
        switch (id){
            case "A":{
                WorldResult worldResult = (WorldResult) list.get(0);
                NumberResult startHeight = (NumberResult) list.get(1);
                NumberResult endHeight = (NumberResult) list.get(2);
                StringResult blockName = (StringResult) list.get(3);
                Block block=Block.fromNamespaceId(blockName.toString());
                if (block==null){
                    return new BooleanResult(false);
                }else {
                    worldResult.getWorld().setGenerator(unit -> unit.modifier().fillHeight(startHeight.toInteger(),
                            endHeight.toInteger(), block));
                    return new BooleanResult(true);
                }

            }
        }
        return new BooleanResult(false);
    }

    @Override
    public List<ArgsSetting> getArgsSetting() {
        List<ArgsSetting> argsSettings=new ArrayList<>();
        argsSettings.add(
                new ArgsSetting("A")
                        .addArgType("World").addArgType("Number").addArgType("Number").addArgType("String")
                        .addInfo( "世界 开始高度 结束高度 方块名")
                         .addInfo("将世界中指定高度的方块填充为指定方块")
                        .setResultType("Boolean")
        );
        return argsSettings;
    }

    @Override
    public String getName() {
        return "fillHeight";
    }

    @Override
    public String getType() {
        return "WORLD";
    }
}
