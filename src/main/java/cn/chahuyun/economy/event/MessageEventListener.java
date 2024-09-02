package cn.chahuyun.economy.event;

import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.config.EconomyConfig;
import cn.chahuyun.economy.manager.*;
import cn.chahuyun.economy.plugin.PluginManager;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.EventCancelledException;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :消息检测
 * @Date 2022/7/9 18:11
 */
//@MessageComponent
public class MessageEventListener extends SimpleListenerHost {


    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        if (exception instanceof EventCancelledException) {
            Log.error("发送消息被取消:", exception);
        } else if (exception instanceof BotIsBeingMutedException) {
            Log.error("你的机器人被禁言:", exception);
        } else if (exception instanceof MessageTooLargeException) {
            Log.error("发送消息过长:", exception);
        } else if (exception instanceof IllegalArgumentException) {
            Log.error("发送消息为空:", exception);
        }

        // 处理事件处理时抛出的异常
        Log.error(exception.getCause());
    }

    /**
     * 消息入口
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/11/14 12:34
     */
    @EventHandler()
    public void onMessage(@NotNull MessageEvent event) {
        EconomyConfig config = HuYanEconomy.config;
        User sender = event.getSender();
        //主人
        boolean owner = config.getOwner().contains(sender.getId());
        Contact subject = event.getSubject();
        Group group = null;
        if (subject instanceof Group) {
            group = (Group) subject;
        }

        String code = event.getMessage().serializeToMiraiCode();
        PropsManager propsManager = PluginManager.getPropsManager();
        if (!config.getPrefix().isBlank()) {
            if (!code.startsWith(config.getPrefix())) {
                return;
            }
            code = code.substring(1);
        }
        switch (code) {
            case "背包":
            case "backpack":
                Log.info("背包指令");
                propsManager.viewUserBackpack(event);
                return;
            case "道具商店":
            case "shops":
                Log.info("道具商店指令");
                propsManager.propStore(event);
                return;
            case "开启 猜签":
                if (owner) {
                    Log.info("管理指令");
                    if (group != null && !config.getLotteryGroup().contains(group.getId())) {
                        EconomyConfig.INSTANCE.getLotteryGroup().add(group.getId());
                    }
                    subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "本群的猜签功能已开启!"));
                }
                return;
            case "关闭 猜签":
                if (owner) {
                    Log.info("管理指令");
                    if (group != null && config.getLotteryGroup().contains(group.getId())) {
                        EconomyConfig.INSTANCE.getLotteryGroup().remove(group.getId());
                    }
                    subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "本群的猜签功能已关闭!"));
                }
                return;
            case "开启 钓鱼":
                if (owner || sender == Objects.requireNonNull(group).getOwner()) {
                    Log.info("管理指令");
                    if (group != null && !config.getFishGroup().contains(group.getId())) {
                        EconomyConfig.INSTANCE.getFishGroup().add(group.getId());
                    }
                    subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "本群的钓鱼功能已开启!"));
                }
                return;
            case "关闭 钓鱼":
                if (owner || sender == Objects.requireNonNull(group).getOwner()) {
                    Log.info("管理指令");
                    if (group != null && config.getFishGroup().contains(group.getId())) {
                        EconomyConfig.INSTANCE.getFishGroup().remove(group.getId());
                    }
                    subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "本群的钓鱼功能已关闭!"));
                }
                return;
            case "开启 抢劫":
                if (owner || sender == Objects.requireNonNull(group).getOwner()) {
                    Log.info("管理指令");
                    if (group != null && !config.getRobGroup().contains(group.getId())) {
                        EconomyConfig.INSTANCE.getRobGroup().add(group.getId());
                    }
                    subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "本群的抢劫功能已开启!"));
                }
                return;
            case "关闭 抢劫":
                if (owner || sender == Objects.requireNonNull(group).getOwner()) {
                    Log.info("管理指令");
                    if (group != null && config.getRobGroup().contains(group.getId())) {
                        EconomyConfig.INSTANCE.getRobGroup().remove(group.getId());
                    }
                    subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "本群的抢劫功能已关闭!"));
                }
                return;
            default:
        }

        String buyPropRegex = "购买 (\\S+)( \\S+)?|buy (\\S+)( \\S+)?";
        if (Pattern.matches(buyPropRegex, code)) {
            Log.info("购买指令");
            propsManager.buyPropFromStore(event);
            return;
        }

        String userPropRegex = "使用 (\\S+)( \\S+)?|use (\\S+)( \\S+)?";
        if (Pattern.matches(userPropRegex, code)) {
            Log.info("使用指令");
            propsManager.userProp(event);
            return;
        }

    }

}
