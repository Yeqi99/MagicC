package cn.originmc.server.magic.function.world;

import cn.origincraft.magic.expression.functions.FunctionResult;
import cn.origincraft.magic.function.ArgsFunction;
import cn.origincraft.magic.function.ArgsSetting;
import cn.origincraft.magic.function.results.BooleanResult;
import cn.origincraft.magic.function.results.NullResult;
import cn.origincraft.magic.function.results.SpellResult;
import cn.origincraft.magic.function.results.StringResult;
import cn.origincraft.magic.object.Spell;
import cn.origincraft.magic.object.SpellContext;
import cn.originmc.server.Main;
import cn.originmc.server.magic.result.WorldResult;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.generator.Generator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldFunction extends ArgsFunction {
    @Override
    public FunctionResult whenFunctionCalled(SpellContext spellContext, List<FunctionResult> list, ArgsSetting argsSetting) {
        String id = argsSetting.getId();
        switch (id){
            case "A":{
                StringResult path = (StringResult) list.get(0);
                StringResult name = (StringResult) list.get(1);
                // 检查下指定路径的文件是否存在的语句
                if (new File(path.toString()).exists()){
                    AnvilLoader anvilLoader=new AnvilLoader(path.toString());
                    InstanceManager instanceManager= Main.instanceManager;
                    InstanceContainer instanceContainer=instanceManager.createInstanceContainer(anvilLoader);
                    Main.publicContextMap.putVariable("world."+name.toString(),new WorldResult(instanceContainer));
                    return new WorldResult(instanceContainer);
                }else {
                    InstanceManager instanceManager= Main.instanceManager;
                    InstanceContainer instanceContainer=instanceManager.createInstanceContainer();
                    Main.publicContextMap.putVariable("world."+name.toString(),new WorldResult(instanceContainer));
                    instanceContainer.saveChunksToStorage();
                    return new WorldResult(instanceContainer);
                }
            }
            case "B":{
                WorldResult worldResult = (WorldResult) list.get(0);
                worldResult.getWorld().saveInstance();
                return new NullResult();
            }
            case "C":{
                StringResult name = (StringResult) list.get(0);
                if (Main.publicContextMap.hasVariable("world."+name.toString())){
                    WorldResult worldResult= (WorldResult) Main.publicContextMap.getVariable("world."+name);
                    return new WorldResult(worldResult.getWorld());
                }
                return new NullResult();
            }
            case "D":{
                WorldResult worldResult= (WorldResult) list.get(0);
                SpellResult spellResult= (SpellResult) list.get(1);
                Spell spell= spellResult.getSpell();
                spell.execute(Main.publicContextMap);
                return new NullResult();
            }
        }
        return new NullResult();
    }

    @Override
    public List<ArgsSetting> getArgsSetting() {
        List<ArgsSetting> argsSettings=new ArrayList<>();
        argsSettings.add(new ArgsSetting("A")
                .addArgType("String").addArgType("String")
                .addInfo("路径 世界名")
                .addInfo("按文件的路径将世界加载并命名")
                .setResultType("World")
        );
        argsSettings.add(new ArgsSetting("B")
                .addArgType("World")
                .addInfo("世界")
                .addInfo("保存一次世界")
                .setResultType("Null")
        );
        argsSettings.add(new ArgsSetting("C")
                .addArgType("String")
                .addInfo("世界名")
                .addInfo("获取指定名称的世界")
                .setResultType("World")
        );
        argsSettings.add(new ArgsSetting("D")
                .addArgType("World").addArgType("Spell")
                .addInfo("世界 生成逻辑")
                .addInfo("给某个世界设置生成器")
                .setResultType("Null")
        );
        return argsSettings;
    }

    @Override
    public String getName() {
        return "world";
    }

    @Override
    public String getType() {
        return "WORLD";
    }
}
