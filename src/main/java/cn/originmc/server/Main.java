package cn.originmc.server;

import cn.origincraft.magic.MagicManager;
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
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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
        // 加载配置文件包
        MagicPackage configPackage=getMagicPackage("magic");
        // 从配置文件包获取默认配置脚
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

        var chunks = new ArrayList<CompletableFuture<Chunk>>();
        ChunkUtils.forChunksInRange(0, 0, 32, (x, z) -> chunks.add(instanceContainer.loadChunk(x, z)));

        CompletableFuture.runAsync(() -> {
            CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).join();
            System.out.println("load end");
            LightingChunk.relight(instanceContainer, instanceContainer.getChunks());
            System.out.println("light end");
        });

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

    public static MagicPackage getMagicPackage(String dirPath) {
        File configDir = new File(dirPath);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        MagicPackage magicPackage = new MagicPackage(dirPath);

        // 默认的配置文件名列表
        List<String> defaultConfigs = Arrays.asList("server.m");

        for (String configName : defaultConfigs) {
            File configFile = new File(configDir, configName);
            if (!configFile.exists()) {
                // 尝试从类路径加载资源并复制到指定目录
                copyResourceToFile("magic/" + configName, configFile);
            }
        }

        // 加载目录中的所有文件
        magicPackage.loadFiles(configDir.getAbsolutePath());
        return magicPackage;
    }

    private static void copyResourceToFile(String resourcePath, File targetFile) {
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                // 为了调试，如果找不到资源，输出错误消息和路径
                System.err.println("资源文件未找到: " + resourcePath);
                return;
            }

            try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                System.out.println("文件成功复制至：" + targetFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}