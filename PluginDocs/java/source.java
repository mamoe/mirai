package net.mamoe.n;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.mamoe.mirai.console.command.*;
import net.mamoe.mirai.console.plugin.Config;
import net.mamoe.mirai.console.plugin.ConfigSection;
import net.mamoe.mirai.console.plugin.ConfigSectionFactory;
import net.mamoe.mirai.console.plugin.PluginBase;
import net.mamoe.mirai.console.util.Utils;
import net.mamoe.mirai.message.GroupMessage;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;

class PingMyMCServerMain extends PluginBase {

    private String defaultServerName;
    private ConfigSection serverMap;
    private Config setting;
    private String API;
    private String responseTemplate;

    public void onLoad(){
        super.onLoad();

        this.setting = this.loadConfig("setting.yml");

        this.setting.setIfAbsent("API","https://api-mping.loliboy.com/ping/{address}/{port}");
        this.setting.setIfAbsent("ServerList", ConfigSectionFactory.create());
        this.setting.setIfAbsent("DefaultServerName","");
        this.setting.setIfAbsent("ResponseTemplate","Ping {serverName}: \nGame: {game}, {version}\nName: {fullName}\nPlayer: {currentPlayers}/{maxPlayers}\nConnected: {connected}\nIP: {address}:{port}");


        this.API = this.setting.getString("API");
        this.defaultServerName = this.setting.getString("DefaultServerName");
        this.serverMap = this.setting.getConfigSection("ServerList");
        this.responseTemplate = this.setting.getString("ResponseTemplate");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.setting.set("ServerList",serverMap);
        this.setting.set("DefaultServerName",defaultServerName);
        this.setting.save();
    }

    public void onEnable(){
        this.getEventListener().subscribeAlways(GroupMessage.class, (GroupMessage event) -> {

            String messageInString = event.getMessage().toString();

            if(!messageInString.contains("ping ")) {
                return;
            }

            String serverName = messageInString.replace("ping ", "").toLowerCase().trim();

            if(!this.serverMap.containsKey(serverName)) {
                serverName = this.defaultServerName;
            }

            if(!this.serverMap.containsKey(serverName)){
                event.getSubject().sendMessage("Bot管理员没有设置任何可ping的服务器, 请使用/mcserver 来增加");
                return;
            }

            ConfigSection serverInfo = this.serverMap.getConfigSection(serverName);

            final String serverName_ = serverName;

            getScheduler().async(() -> {
                event.getSubject().sendMessage("正在获取中..");
                try {
                    String response = Utils.tryNTimes(2, () ->
                            Jsoup.connect(API
                                    .replace("{address}", serverInfo.getString("address"))
                                    .replace("{port}", serverInfo.getString("port"))
                            ).ignoreContentType(true).timeout(8000).execute().body()
                    );
                    JsonObject resObj = JsonParser.parseString(response).getAsJsonObject();
                    JsonObject addressObj = resObj.get("rinfo").getAsJsonObject();
                    event.getSubject().sendMessage(this.responseTemplate
                            .replace("{connected}",resObj.get("connected").getAsString())
                            .replace("{currentPlayers}",resObj.get("currentPlayers").getAsString())
                            .replace("{maxPlayers}",resObj.get("maxPlayers").getAsString())
                            .replace("{serverName}",serverName_)
                            .replace("{fullName}",resObj.get("cleanName").getAsString())
                            .replace("{game}",resObj.get("game").getAsString())
                            .replace("{version}",resObj.get("version").getAsString())
                            .replace("{address}",addressObj.get("address").getAsString())
                            .replace("{port}",addressObj.get("port").getAsString())
                    );
                } catch (Exception e) {
                    event.getSubject().sendMessage("获取失败.." + e.getMessage());
                    e.printStackTrace();
                }
            });

        });

        JCommandManager.getInstance().register(this, new BlockingCommand(
                "mcserver", new ArrayList<>(),"管理可以ping的MC服务器","/mcserver add/remove"
        ) {
            @Override
            public boolean onCommandBlocking(@NotNull CommandSender commandSender, @NotNull List<String> list) {
                if(list.size() < 1){
                    return false;
                }
                switch (list.get(0)){
                    case "add":
                        if(list.size() < 4){
                            commandSender.sendMessageBlocking("/mcserver add 服务器名字 IP 端口");
                            return true;
                        }
                        String serverName = list.get(1);

                        String IP = list.get(2);

                        int port = -1;
                        try {
                            port = Integer.parseInt(list.get(3));
                        }catch (Exception e){
                            commandSender.sendMessageBlocking("无法识别端口号");
                            return true;
                        }

                        if(port < 0 || port > 65535){
                            commandSender.sendMessageBlocking("无法识别端口号[0-65535]");
                            return true;
                        }
                        if(IP.contains(":")){
                            commandSender.sendMessageBlocking("IP中不应包含端口");
                            return true;
                        }

                        ConfigSection data = ConfigSectionFactory.create();

                        data.set("address",IP);
                        data.set("port",port);

                        if(serverMap.size() == 0){
                            defaultServerName = serverName;
                        }

                        serverMap.put(serverName.toLowerCase(),data);
                        commandSender.sendMessageBlocking("设置成功, 发送ping " + serverName + " 即可");

                        break;
                    case "remove":
                        if(list.size() < 2){
                            commandSender.sendMessageBlocking("/mcserver remove 服务器名字");
                            return true;
                        }
                        String serverNameToRemove = list.get(1).toLowerCase();
                        if(serverMap.containsKey(serverNameToRemove)){
                            serverMap.remove(serverNameToRemove);
                            commandSender.sendMessageBlocking("移除成功");
                        }else{
                            commandSender.sendMessageBlocking("没有找到" + list.get(1) + "的数据");
                        }
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });

        this.getLogger().info("PingMyMCServer Enabled");
    }

}