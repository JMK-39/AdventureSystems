package dev.xyat.adventuresystems.curios.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.HashMap;
import java.util.Map;

public final class ColorText {
    private static final Map<String, ChatFormatting[][]> ARG_STYLES = new HashMap<>();

    static {
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.exchange_ratio", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN}, new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.GREEN}, new ChatFormatting[]{ChatFormatting.GOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.exchange_to", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.hud_amount", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_buy_source_backpack", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_buy_source_inventory", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_buy_source_rs", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_buy_source_total", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_buy_source_wallet", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_cell_duration_days_plus", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_cell_duration_hours_plus", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_cell_duration_minutes_plus", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_cell_duration_seconds", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_cell_limit_left", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_cell_quest_need", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_cell_timed_wait", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_choice_line", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_command_count", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_command_manage_hint", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_editor_button_currency", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_editor_button_item", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_editor_button_payment", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_editor_choice_reward_count", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_editor_gacha_reward_count", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_editor_quest_count", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_editor_quest_rule", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_editor_sell_choice_count", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_locked_need_task_name", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.RED}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_need_complete_task", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_need_complete_task_first", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_page_active_prefix", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_price", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_quest_done_line", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_quest_id_scaled", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.AQUA}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_quest_more_line", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_quest_need_line", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_quest_progress", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_reward_probability_compact", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW}, new ChatFormatting[]{ChatFormatting.LIGHT_PURPLE}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_reward_probability_line", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW}, new ChatFormatting[]{ChatFormatting.LIGHT_PURPLE}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_reward_tooltip_chance", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_reward_tooltip_count", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_reward_tooltip_weight", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_sell_backpack_count", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_sell_choice_line", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_sell_choice_tooltip_count", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_sell_fail_preview", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_sell_income_single_icon", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_sell_inventory_count", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_sell_item_count", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_sell_progress_detail", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_sell_rs_count", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_sell_total_income", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_single_count", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_single_price_icon", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_task_button", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.AQUA}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_task_fallback_name", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_task_multiple", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_task_requirement_need", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_timed_limit_ready", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_timed_limit_wait", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.YELLOW}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_timed_wait", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_tooltip_currency_balance", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_tooltip_currency_value", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_total_limit_status", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_trade_cost_preview", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_trade_preview_choice_gain", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_trade_preview_cost", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_trade_preview_currency_gain", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_trade_preview_gacha_gain", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_trade_preview_item_gain", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_trade_preview_material_cost", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.AQUA}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_trade_preview_result", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.shop_trade_preview_times", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.tooltip_convert", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.GREEN}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.tooltip_exact", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("gui.adventuresystems.curios.wallet.tooltip_value", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("msg.adventuresystems.curios.bound_to_other", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}});
        ARG_STYLES.put("msg.adventuresystems.curios.equip_conflict", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}});
        ARG_STYLES.put("msg.adventuresystems.curios.force_unequip", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}});
        ARG_STYLES.put("msg.adventuresystems.curios.paradise_lost.boost", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.RED}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("msg.adventuresystems.curios.wallet.merchant_manual_exchange_required", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}});
        ARG_STYLES.put("msg.adventuresystems.curios.wallet.rs_bind_success", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.AQUA}});
        ARG_STYLES.put("msg.adventuresystems.curios.wallet.shop_buy_fail_money_detail", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}});
        ARG_STYLES.put("msg.adventuresystems.curios.wallet.shop_buy_fail_summary_gacha", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("msg.adventuresystems.curios.wallet.shop_buy_fail_summary_item", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.AQUA}});
        ARG_STYLES.put("msg.adventuresystems.curios.wallet.shop_buy_fail_timed", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}});
        ARG_STYLES.put("msg.adventuresystems.curios.wallet.shop_buy_partial_gacha", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("msg.adventuresystems.curios.wallet.shop_buy_partial_item", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.AQUA}});
        ARG_STYLES.put("msg.adventuresystems.curios.wallet.shop_buy_success_gacha", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW}, new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("msg.adventuresystems.curios.wallet.shop_buy_success_item", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW}, new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.AQUA}});
        ARG_STYLES.put("msg.adventuresystems.curios.wallet.shop_sell_fail_missing_detail", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.YELLOW}, new ChatFormatting[]{ChatFormatting.GREEN}});
        ARG_STYLES.put("msg.adventuresystems.curios.wallet.shop_sell_fail_missing_sources", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.YELLOW}, new ChatFormatting[]{ChatFormatting.GREEN}});
        ARG_STYLES.put("msg.adventuresystems.curios.wallet.shop_sell_fail_missing_summary", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW}, new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.YELLOW}, new ChatFormatting[]{ChatFormatting.AQUA}, new ChatFormatting[]{ChatFormatting.GREEN}, new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.AQUA}, new ChatFormatting[]{ChatFormatting.GREEN}, new ChatFormatting[]{ChatFormatting.AQUA}, new ChatFormatting[]{ChatFormatting.AQUA}});
        ARG_STYLES.put("msg.adventuresystems.curios.wallet.shop_sell_partial_summary", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.YELLOW}, new ChatFormatting[]{ChatFormatting.AQUA}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.AQUA}, new ChatFormatting[]{ChatFormatting.YELLOW}, new ChatFormatting[]{ChatFormatting.GREEN}});
        ARG_STYLES.put("msg.adventuresystems.curios.wallet.shop_sell_progress_summary", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW}, new ChatFormatting[]{ChatFormatting.AQUA}, new ChatFormatting[]{ChatFormatting.YELLOW}, new ChatFormatting[]{ChatFormatting.GREEN}, new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.AQUA}});
        ARG_STYLES.put("msg.adventuresystems.curios.wallet.shop_sell_success_summary", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.AQUA}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GOLD}});
        ARG_STYLES.put("tip.adventuresystems.curios.global.bound_to", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}});
        ARG_STYLES.put("tip.adventuresystems.curios.heart_of_steel.cooldown", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}});
        ARG_STYLES.put("tip.adventuresystems.curios.heart_of_steel.current_damage_bonus", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.AQUA}, new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}});
        ARG_STYLES.put("tip.adventuresystems.curios.heart_of_steel.growth_interval", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.BLUE, ChatFormatting.BOLD}});
        ARG_STYLES.put("tip.adventuresystems.curios.heart_of_steel.healing_bonus", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("tip.adventuresystems.curios.heart_of_steel.health_bonus", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.AQUA}, new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("tip.adventuresystems.curios.heart_of_steel.health_growth_amount", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("tip.adventuresystems.curios.heart_of_steel.scaling", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}});
        ARG_STYLES.put("tip.adventuresystems.curios.heart_of_steel.stacks", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}});
        ARG_STYLES.put("tip.adventuresystems.curios.paradise_lost.damage_bonus_neg", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}});
        ARG_STYLES.put("tip.adventuresystems.curios.paradise_lost.damage_bonus_pos", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("tip.adventuresystems.curios.paradise_lost.next_stage", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("tip.adventuresystems.curios.paradise_lost.score", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.BOLD}, new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}});
        ARG_STYLES.put("tip.adventuresystems.curios.wallet.amount_line", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD}, new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}});
        ARG_STYLES.put("tip.adventuresystems.curios.wallet.desc1", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}});
        ARG_STYLES.put("tip.adventuresystems.curios.wallet.desc3", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.BOLD}});
        ARG_STYLES.put("tip.adventuresystems.curios.wallet.rs_bound", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.AQUA}, new ChatFormatting[]{ChatFormatting.AQUA}, new ChatFormatting[]{ChatFormatting.AQUA}});
        ARG_STYLES.put("tip.adventuresystems.curios.wallet.rs_missing", new ChatFormatting[][]{new ChatFormatting[]{ChatFormatting.RED}, new ChatFormatting[]{ChatFormatting.RED}, new ChatFormatting[]{ChatFormatting.RED}});
    }

    private ColorText() {
    }

    public static MutableComponent translatable(String key, Object... args) {
        ChatFormatting[][] styles = ARG_STYLES.get(key);
        if (styles == null || args.length == 0) {
            return Component.translatable(key, args);
        }
        Object[] styledArgs = args.clone();
        int count = Math.min(styles.length, styledArgs.length);
        for (int i = 0; i < count; i++) {
            ChatFormatting[] formats = styles[i];
            if (formats == null || formats.length == 0) continue;
            Object value = styledArgs[i];
            boolean preserveColor = value instanceof Component existing && existing.getStyle().getColor() != null;
            MutableComponent component = value instanceof Component existing
                    ? existing.copy()
                    : Component.literal(String.valueOf(value));
            if (preserveColor) {
                for (int j = 1; j < formats.length; j++) {
                    component.withStyle(formats[j]);
                }
            } else {
                component.withStyle(formats);
            }
            styledArgs[i] = component;
        }
        return Component.translatable(key, styledArgs);
    }
}
