package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.curios.wallet.data.CurrencyType;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.data.StackCodec;
import dev.xyat.adventuresystems.curios.wallet.shop.Shop;
import dev.xyat.adventuresystems.ftb.util.BridgeFTB;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

final class ShopGuiSupport {
    static ItemStack stack(String id) {
        ItemStack stack = StackCodec.fromConfigString(id);
        return stack.isEmpty() ? new ItemStack(net.minecraft.world.item.Items.BARRIER) : stack;
    }




    static boolean isConfiguredCurrency(String id) {
        return id != null && !id.isBlank() && Data.currencyMap().containsKey(id);
    }

    static String stackName(String id) {
        return stackName(stack(id));
    }

    static String stackName(ItemStack stack) {
        return stackNameComponent(stack).getString();
    }

    static Component stackNameComponent(String id) {
        return stackNameComponent(stack(id));
    }

    static Component stackNameComponent(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Component.empty();
        return stack.getHoverName();
    }

    static String stackNameWithCount(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";
        int count = Math.max(1, stack.getCount());
        return stackName(stack) + " x" + count;
    }

    static String selectedStackSpec(ItemStack stack) {
        return StackCodec.toConfigString(stack, stack == null || stack.isEmpty() ? 1 : stack.getCount());
    }

    static String stackSpecWithCount(String itemId, int count) {
        return StackCodec.withCount(itemId, count);
    }

    static String formatExact(long value) {
        return NumberFormat.getIntegerInstance(Locale.ROOT).format(Math.max(0L, value));
    }

    static String percent(double value) {
        return String.format(Locale.ROOT, "%.2f%%", Math.max(0.0D, value));
    }

    static void drawScaledString(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        if (text == null || text.isBlank()) return;
        graphics.drawString(font, text, x, y, color, true);
    }


    static Map<Long, QuestDisplay> loadQuestDisplays() {
        Map<Long, QuestDisplay> result = new LinkedHashMap<>();
        try {
            for (var ref : BridgeFTB.getAllQuestRefs()) {
                if (ref == null || ref.id() == 0L) continue;
                QuestDisplay display = new QuestDisplay(ref.id(), safeString(ref.code()), safeString(ref.title()), safeString(ref.chapter()));
                result.put(display.id(), display);
            }
        } catch (Throwable ignored) {
        }
        return result;
    }

    private static String safeString(String value) {
        return value == null ? "" : value;
    }

    record QuestDisplay(long id, String code, String title, String chapter) {
        String displayTitle() {
            String value = title == null || title.isBlank() ? code : title;
            return value == null || value.isBlank() ? String.valueOf(id) : value;
        }

        String displayMeta() {
            String c = chapter == null ? "" : chapter;
            String k = code == null ? "" : code;
            if (!c.isBlank() && !k.isBlank()) return c + "  " + k;
            if (!c.isBlank()) return c;
            if (!k.isBlank()) return k;
            return String.valueOf(id);
        }
    }

    static class EditorDraft {
        Shop.Mode mode;
        int index;
        String itemId = "";
        String currencyId = "";
        long price = 1;
        int count = 1;
        int dailyLimit = 0;
        int totalLimit = 0;
        String pageName = "";
        String displayName = "";
        String description = "";
        String commandText = "";
        int requiredQuestCount = 0;
        List<Long> questIds = new ArrayList<>();
        boolean gacha = false;
        boolean selectable = false;
        List<RewardDraft> rewards = new ArrayList<>();
        List<CommandDraft> commands = new ArrayList<>();

        EditorDraft(Shop.Mode mode) {
            this.mode = mode;
            this.index = -1;
            List<CurrencyType> currencies = Data.currencies();
            if (!currencies.isEmpty()) this.currencyId = currencies.get(0).itemId();
        }

        EditorDraft(Shop.Entry entry) {
            this.mode = entry.mode();
            this.index = entry.index();
            this.itemId = StackCodec.toConfigString(entry.stack(), entry.stack().getCount());
            this.currencyId = entry.currencyId();
            this.price = entry.price();
            this.count = entry.stack().getCount();
            this.dailyLimit = entry.dailyLimit();
            this.totalLimit = entry.totalLimit();
            this.pageName = entry.pageName();
            this.displayName = entry.displayName();
            this.description = entry.description();
            this.commandText = entry.command();
            if (this.commandText != null && !this.commandText.isBlank()) {
                this.commands.add(new CommandDraft(this.itemId, this.displayName, this.commandText));
            }
            this.requiredQuestCount = entry.requiredQuestCount();
            if (entry.requiredQuestIds() != null) this.questIds = new ArrayList<>(entry.requiredQuestIds());
            this.gacha = entry.gacha();
            this.selectable = entry.selectable();
            if (entry.rewards() != null) {
                for (Shop.Reward r : entry.rewards()) {
                    String rewardSpec = r.empty() ? "" : StackCodec.toConfigString(r.stack(), r.stack().getCount());
                    this.rewards.add(new RewardDraft(rewardSpec, r.empty() ? 0 : r.stack().getCount(), r.weight(), r.empty(), r.chance(), r.displayName(), r.command()));
                    if (r.command() != null && !r.command().isBlank()) {
                        this.commands.add(new CommandDraft(rewardSpec, r.displayName(), r.command()));
                    }
                }
            }
        }

        void syncDirectCommandFromList() {
            commandText = "";
            if (gacha || selectable || commands.isEmpty()) return;
            CommandDraft command = commands.get(0);
            if (command == null || command.command() == null || command.command().isBlank()) return;
            commandText = command.command();
            if ((displayName == null || displayName.isBlank()) && command.displayName() != null && !command.displayName().isBlank()) displayName = command.displayName();
            if ((itemId == null || itemId.isBlank()) && command.iconId() != null && !command.iconId().isBlank()) itemId = command.iconId();
        }

        String buildQuestText() {
            StringBuilder b = new StringBuilder();
            for (Long q : questIds) {
                if (!b.isEmpty()) b.append(",");
                b.append(q);
            }
            if (requiredQuestCount > 0) {
                if (!b.isEmpty()) b.append(";");
                b.append("questNeed=").append(requiredQuestCount);
            }
            if (pageName != null && !pageName.isBlank()) {
                if (!b.isEmpty()) b.append(";");
                b.append("page=").append(pageName.replace("|", "/").replace(";", "/"));
            }
            if (displayName != null && !displayName.isBlank()) {
                if (!b.isEmpty()) b.append(";");
                b.append("name=").append(displayName.replace("|", "/").replace(";", "/"));
            }
            if (description != null && !description.isBlank()) {
                if (!b.isEmpty()) b.append(";");
                b.append("description=").append(description.replace("|", "/").replace(";", "/").replace('\n', ' ').replace('\r', ' '));
            }
            if (commandText != null && !commandText.isBlank()) {
                if (!b.isEmpty()) b.append(";");
                String command = commandText.trim();
                if (command.startsWith("/")) command = command.substring(1);
                b.append("command=").append(command.replace("|", " ").replace(";", " "));
            }
            if (selectable) {
                if (!b.isEmpty()) b.append(";");
                b.append("choice=true");
            }
            return b.toString();
        }

        String buildRewardsText() {
            StringBuilder b = new StringBuilder();
            for (RewardDraft r : rewards) {
                if (!b.isEmpty()) b.append(",");
                if (r.empty()) b.append("empty");
                else b.append(stackSpecWithCount(r.itemId(), r.count()));
                b.append("@").append(r.weight());
                if (r.displayName() != null && !r.displayName().isBlank()) {
                    b.append("~name=").append(encodeRewardOption(r.displayName()));
                }
                if (r.command() != null && !r.command().isBlank()) {
                    String command = r.command().trim();
                    if (command.startsWith("/")) command = command.substring(1);
                    b.append("~command=").append(encodeRewardOption(command));
                }
            }
            return b.toString();
        }

        private String encodeRewardOption(String value) {
            if (value == null) return "";
            return value.replace("|", " ").replace(";", " ").replace(",", " ").replace("~", " ").trim();
        }
    }

    record CommandDraft(String iconId, String displayName, String command) {
        CommandDraft {
            iconId = iconId == null ? "" : iconId;
            displayName = displayName == null ? "" : displayName;
            command = command == null ? "" : command.trim();
        }

        RewardDraft toRewardDraft(int weight) {
            return new RewardDraft(iconId, Math.max(1, stack(iconId).getCount()), Math.max(1, weight), false, 0.0D, displayName, command);
        }
    }

    record RewardDraft(String itemId, int count, int weight, boolean empty, double chance, String displayName, String command) {
        RewardDraft {
            displayName = displayName == null ? "" : displayName;
            command = command == null ? "" : command;
        }
    }
}

