package cn.originmc.server;

import cn.origincraft.magic.MagicManager;
import cn.origincraft.magic.expression.functions.FunctionResult;
import cn.origincraft.magic.function.results.NumberResult;
import cn.origincraft.magic.function.results.StringResult;
import cn.origincraft.magic.manager.MagicInstance;
import cn.origincraft.magic.manager.MagicPackage;
import cn.origincraft.magic.object.ContextMap;
import cn.origincraft.magic.object.NormalContext;
import cn.origincraft.magic.object.Spell;
import cn.origincraft.magic.object.SpellContext;
import cn.origincraft.magic.utils.ErrorUtils;
import cn.originmc.server.magic.FunctionRegister;
import cn.originmc.server.magic.result.WorldResult;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;

import java.io.File;
import java.net.URL;
import java.util.*;

public class Main {
    public static MagicManager magicManager=new MagicManager();
    public static ContextMap publicContextMap=new NormalContext();
    public static MinecraftServer minecraftServer;
    public static InstanceManager instanceManager;
    public static void main(String[] args) {
        // 初始化MS服务器实例
        minecraftServer = MinecraftServer.init();
        instanceManager = MinecraftServer.getInstanceManager();
        magicManager.registerDefaultFunction();
        FunctionRegister.register();
        MagicPackage configPackage=getMagicPackage("config");
        MagicInstance serverConfig=configPackage.getMagicInstance("server");
        Spell spell= serverConfig.getSpell(magicManager);
        SpellContext spellContext= spell.execute(publicContextMap);
        if (spellContext.hasExecuteError()){
            System.out.println(ErrorUtils.stringError(spellContext));
        }
        StringResult ipResult= (StringResult) publicContextMap.getVariable("ip");
        NumberResult portResult= (NumberResult) publicContextMap.getVariable("port");
        NumberResult respawnPointX= (NumberResult) publicContextMap.getVariable("respawnPointX");
        NumberResult respawnPointY= (NumberResult) publicContextMap.getVariable("respawnPointY");
        NumberResult respawnPointZ= (NumberResult) publicContextMap.getVariable("respawnPointZ");

        InstanceContainer instanceContainer;
        if (publicContextMap.hasVariable("world.main")){
                instanceContainer =((WorldResult) publicContextMap.getVariable("world.main")).getWorld();
        }else {
            instanceContainer = instanceManager.createInstanceContainer();
            instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
        }

        // 添加事件回调以指定生成实例（和生成位置）
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(respawnPointX.toInteger(),respawnPointY.toInteger(),respawnPointZ.toInteger()));
        });

        // 服务器按照配置文件监听
        minecraftServer.start(ipResult.toString(),portResult.toInteger());

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("输入多行文本 (输入 'END' 表示结束，直接输入 'STOP' 可以退出程序):");
            StringBuilder inputText = new StringBuilder();

            while (true) {
                String line = scanner.nextLine();
                if (line.equalsIgnoreCase("STOP")) {
                    // 关闭控制台窗口并退出程序
                    System.exit(0);
                }
                if (line.equalsIgnoreCase("END")) {
                    break;
                }
                inputText.append(line).append(System.lineSeparator());
            }

            String finalInputText = inputText.toString().trim();
            if (finalInputText.equalsIgnoreCase("stop")) {
                break;
            } else {
                String[] linesArray = finalInputText.split("\n");
                List<String> magicStrings = new ArrayList<>(Arrays.asList(linesArray));
                Spell inputSpell = new Spell(magicStrings, magicManager);
                SpellContext inputSpellContext = inputSpell.execute(publicContextMap);
                if (inputSpellContext.hasExecuteError()) {
                    System.out.println(ErrorUtils.stringError(inputSpellContext));
                }
            }
        }
        scanner.close();

    }
    public static MagicPackage getMagicPackage(String path){
        // 创建一个MagicPackage实例，假设这个类是用来处理配置文件的
        MagicPackage magicPackage = new MagicPackage(path);

        // 尝试在当前工作目录下找到名为"config"的文件或目录
        File config = new File(path);

        // 检查该路径是否存在
        if (config.exists()) {
            // 如果存在，使用该路径的绝对路径加载文件
            magicPackage.loadFiles(config.getAbsolutePath());
        } else {
            // 如果不存在，尝试从类路径（即Jar包内部）获取资源
            URL url = Main.class.getClassLoader().getResource(path);

            // 检查资源是否存在
            if (url != null) {
                // 如果资源存在，从URL获取路径并加载文件
                magicPackage.loadFiles(url.getPath());
            }
        }
        return magicPackage;
    }
}