package dev.xyat.adventuresystems.tips;

import dev.xyat.kineticcore.api.KTNetworkProtocol;
import dev.xyat.kineticcore.api.NetworkCompressUtil;
import dev.xyat.adventuresystems.tips.api.HelpTip;
import dev.xyat.adventuresystems.tips.client.TipCache;
import dev.xyat.adventuresystems.tips.config.ConfigLoader;
import dev.xyat.adventuresystems.tips.util.TipsStructureUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class TipsNetwork {
    private static final String PROTOCOL_VERSION = "2";
    private static final int MAX_COMPRESSED_BYTES = 2 * 1024 * 1024;
    private static final int MAX_DECOMPRESSED_BYTES = 8 * 1024 * 1024;
    private static final Map<UUID, String> CLIENT_LANGUAGES = new ConcurrentHashMap<>();
    private static int packetId;

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(TipsModule.MODID, "tips_system"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(KTNetworkProtocol::acceptsAnyVersion)
            .serverAcceptedVersions(KTNetworkProtocol::acceptsAnyVersion)
            .simpleChannel();

    private TipsNetwork() {
    }

    private static int id() {
        return packetId++;
    }

    public static void register() {
        CHANNEL.messageBuilder(RequestStructure.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RequestStructure::new).encoder(RequestStructure::toBytes)
                .consumerMainThread(RequestStructure::handle).add();
        CHANNEL.messageBuilder(UpdateStructure.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(UpdateStructure::new).encoder(UpdateStructure::toBytes)
                .consumerMainThread(UpdateStructure::handle).add();
        CHANNEL.messageBuilder(OpenEditor.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(OpenEditor::new).encoder(OpenEditor::toBytes)
                .consumerMainThread(OpenEditor::handle).add();
        CHANNEL.messageBuilder(RequestOpenEditor.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RequestOpenEditor::new).encoder(RequestOpenEditor::toBytes)
                .consumerMainThread(RequestOpenEditor::handle).add();
        CHANNEL.messageBuilder(EditorDenied.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(EditorDenied::new).encoder(EditorDenied::toBytes)
                .consumerMainThread(EditorDenied::handle).add();
        CHANNEL.messageBuilder(SaveEditor.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SaveEditor::new).encoder(SaveEditor::toBytes)
                .consumerMainThread(SaveEditor::handle).add();
        CHANNEL.messageBuilder(EditorSaveResult.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(EditorSaveResult::new).encoder(EditorSaveResult::toBytes)
                .consumerMainThread(EditorSaveResult::handle).add();
        CHANNEL.messageBuilder(RequestRuntimeTips.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RequestRuntimeTips::new).encoder(RequestRuntimeTips::toBytes)
                .consumerMainThread(RequestRuntimeTips::handle).add();
        CHANNEL.messageBuilder(SyncRuntimeTips.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncRuntimeTips::new).encoder(SyncRuntimeTips::toBytes)
                .consumerMainThread(SyncRuntimeTips::handle).add();
    }

    public static void requestEditor() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                sendToServer(new RequestOpenEditor(ClientProxy.selectedLanguage()))
        );
    }

    public static void requestRuntimeTips() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                sendToServer(new RequestRuntimeTips(ClientProxy.selectedLanguage()))
        );
    }

    public static void saveEditor(String languageCode, List<HelpTip.JsonModel.Entry> entries) {
        String json;
        try {
            json = ConfigLoader.toJson(entries);
        } catch (RuntimeException exception) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientProxy.handleEditorSaveResult(false));
            return;
        }
        sendToServer(new SaveEditor(languageCode, json));
    }

    public static void sendToServer(Object message) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), message);
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    private static String readCompressedJson(FriendlyByteBuf buffer) {
        byte[] compressed = buffer.readByteArray(MAX_COMPRESSED_BYTES);
        return new String(
                NetworkCompressUtil.decompressBytes(compressed, MAX_DECOMPRESSED_BYTES),
                StandardCharsets.UTF_8
        );
    }

    private static void writeCompressedJson(FriendlyByteBuf buffer, String json) {
        byte[] compressed = NetworkCompressUtil.compress(json == null ? "" : json);
        if (compressed.length > MAX_COMPRESSED_BYTES) {
            throw new IllegalArgumentException("Tips payload exceeds compressed limit");
        }
        buffer.writeByteArray(compressed);
    }

    private static String snapshotJson(String languageCode) {
        return ConfigLoader.toJson(ConfigLoader.getRawEntriesForLanguage(languageCode));
    }

    private static void sendRuntimeSnapshot(ServerPlayer player, String languageCode) {
        String language = ConfigLoader.normalizeLanguageCode(languageCode);
        CLIENT_LANGUAGES.put(player.getUUID(), language);
        sendToPlayer(new SyncRuntimeTips(language, snapshotJson(language)), player);
    }

    private static void broadcastRuntimeSnapshot(MinecraftServer server, String languageCode) {
        if (server == null) return;
        String language = ConfigLoader.normalizeLanguageCode(languageCode);
        String json = snapshotJson(language);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (language.equals(CLIENT_LANGUAGES.get(player.getUUID()))) {
                sendToPlayer(new SyncRuntimeTips(language, json), player);
            }
        }
    }

    public static final class RequestStructure {
        private final boolean requestAll;

        public RequestStructure(boolean requestAll) {
            this.requestAll = requestAll;
        }

        public RequestStructure(FriendlyByteBuf buffer) {
            this.requestAll = buffer.readBoolean();
        }

        public void toBytes(FriendlyByteBuf buffer) {
            buffer.writeBoolean(requestAll);
        }

        public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player != null && player.level() instanceof ServerLevel level) {
                    if (requestAll) {
                        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
                        sendToPlayer(new UpdateStructure(new ArrayList<>(registry.keySet())), player);
                    } else {
                        List<String> structures = TipsStructureUtil.getStructuresAt(level, player.blockPosition());
                        sendToPlayer(new UpdateStructure(structures.isEmpty() ? "" : structures.get(0)), player);
                    }
                }
            });
            context.setPacketHandled(true);
        }
    }

    public static final class UpdateStructure {
        private final int type;
        private final String singleStructure;
        private final List<ResourceLocation> structureList;

        public UpdateStructure(String singleId) {
            this.type = 0;
            this.singleStructure = singleId;
            this.structureList = new ArrayList<>();
        }

        public UpdateStructure(List<ResourceLocation> list) {
            this.type = 1;
            this.singleStructure = "";
            this.structureList = list;
        }

        public UpdateStructure(FriendlyByteBuf buffer) {
            this.type = buffer.readByte();
            if (type == 0) {
                this.singleStructure = buffer.readUtf();
                this.structureList = new ArrayList<>();
            } else {
                this.singleStructure = "";
                this.structureList = buffer.readCollection(ArrayList::new, FriendlyByteBuf::readResourceLocation);
            }
        }

        public void toBytes(FriendlyByteBuf buffer) {
            buffer.writeByte(type);
            if (type == 0) buffer.writeUtf(singleStructure);
            else buffer.writeCollection(structureList, FriendlyByteBuf::writeResourceLocation);
        }

        public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientProxy.handleUpdateStructure(type, singleStructure, structureList)
            ));
            context.setPacketHandled(true);
        }
    }

    public static final class OpenEditor {
        private final String languageCode;
        private final String json;

        public OpenEditor(String languageCode, String json) {
            this.languageCode = ConfigLoader.normalizeLanguageCode(languageCode);
            this.json = json;
        }

        public OpenEditor(FriendlyByteBuf buffer) {
            this.languageCode = buffer.readUtf(32);
            this.json = readCompressedJson(buffer);
        }

        public void toBytes(FriendlyByteBuf buffer) {
            buffer.writeUtf(languageCode, 32);
            writeCompressedJson(buffer, json);
        }

        public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientProxy.handleOpenEditor(languageCode, json)
            ));
            context.setPacketHandled(true);
        }
    }

    public static final class RequestOpenEditor {
        private final String languageCode;

        public RequestOpenEditor(String languageCode) {
            this.languageCode = ConfigLoader.normalizeLanguageCode(languageCode);
        }

        public RequestOpenEditor(FriendlyByteBuf buffer) {
            this.languageCode = ConfigLoader.normalizeLanguageCode(buffer.readUtf(32));
        }

        public void toBytes(FriendlyByteBuf buffer) {
            buffer.writeUtf(languageCode, 32);
        }

        public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) return;
                if (!player.hasPermissions(2)) {
                    sendToPlayer(new EditorDenied(), player);
                    return;
                }
                CLIENT_LANGUAGES.put(player.getUUID(), languageCode);
                sendToPlayer(new OpenEditor(languageCode, snapshotJson(languageCode)), player);
            });
            context.setPacketHandled(true);
        }
    }

    public static final class EditorDenied {
        public EditorDenied() {
        }

        public EditorDenied(FriendlyByteBuf buffer) {
        }

        public void toBytes(FriendlyByteBuf buffer) {
        }

        public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientProxy::handleEditorDenied));
            context.setPacketHandled(true);
        }
    }

    public static final class SaveEditor {
        private final String languageCode;
        private final String json;

        public SaveEditor(String languageCode, String json) {
            this.languageCode = ConfigLoader.normalizeLanguageCode(languageCode);
            this.json = json;
        }

        public SaveEditor(FriendlyByteBuf buffer) {
            this.languageCode = ConfigLoader.normalizeLanguageCode(buffer.readUtf(32));
            this.json = readCompressedJson(buffer);
        }

        public void toBytes(FriendlyByteBuf buffer) {
            buffer.writeUtf(languageCode, 32);
            writeCompressedJson(buffer, json);
        }

        public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) return;
                boolean success = false;
                if (player.hasPermissions(2)) {
                    List<HelpTip.JsonModel.Entry> entries = ConfigLoader.fromJson(json);
                    success = entries != null && ConfigLoader.saveRawEntriesForLanguage(languageCode, entries);
                    if (success) {
                        CLIENT_LANGUAGES.put(player.getUUID(), languageCode);
                        broadcastRuntimeSnapshot(player.server, languageCode);
                    }
                }
                sendToPlayer(new EditorSaveResult(success), player);
            });
            context.setPacketHandled(true);
        }
    }

    public static final class EditorSaveResult {
        private final boolean success;

        public EditorSaveResult(boolean success) {
            this.success = success;
        }

        public EditorSaveResult(FriendlyByteBuf buffer) {
            this.success = buffer.readBoolean();
        }

        public void toBytes(FriendlyByteBuf buffer) {
            buffer.writeBoolean(success);
        }

        public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientProxy.handleEditorSaveResult(success)
            ));
            context.setPacketHandled(true);
        }
    }

    public static final class RequestRuntimeTips {
        private final String languageCode;

        public RequestRuntimeTips(String languageCode) {
            this.languageCode = ConfigLoader.normalizeLanguageCode(languageCode);
        }

        public RequestRuntimeTips(FriendlyByteBuf buffer) {
            this.languageCode = ConfigLoader.normalizeLanguageCode(buffer.readUtf(32));
        }

        public void toBytes(FriendlyByteBuf buffer) {
            buffer.writeUtf(languageCode, 32);
        }

        public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player != null) sendRuntimeSnapshot(player, languageCode);
            });
            context.setPacketHandled(true);
        }
    }

    public static final class SyncRuntimeTips {
        private final String languageCode;
        private final String json;

        public SyncRuntimeTips(String languageCode, String json) {
            this.languageCode = ConfigLoader.normalizeLanguageCode(languageCode);
            this.json = json;
        }

        public SyncRuntimeTips(FriendlyByteBuf buffer) {
            this.languageCode = ConfigLoader.normalizeLanguageCode(buffer.readUtf(32));
            this.json = readCompressedJson(buffer);
        }

        public void toBytes(FriendlyByteBuf buffer) {
            buffer.writeUtf(languageCode, 32);
            writeCompressedJson(buffer, json);
        }

        public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientProxy.handleRuntimeSync(languageCode, json)
            ));
            context.setPacketHandled(true);
        }
    }

    public static final class ClientProxy {
        private ClientProxy() {
        }

        public static String selectedLanguage() {
            return ConfigLoader.normalizeLanguageCode(
                    net.minecraft.client.Minecraft.getInstance().getLanguageManager().getSelected()
            );
        }

        public static void handleUpdateStructure(
                int type,
                String singleStructure,
                List<ResourceLocation> structureList
        ) {
            if (type == 0) {
                TipCache.currentStructure = singleStructure;
                net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
                if (minecraft.screen instanceof net.minecraft.client.gui.screens.PauseScreen) {
                    dev.xyat.adventuresystems.tips.client.TipRenderer.refresh(minecraft.screen);
                }
            } else {
                TipCache.ALL_STRUCTURES.clear();
                TipCache.ALL_STRUCTURES.addAll(structureList);
            }
        }

        public static void handleOpenEditor(String languageCode, String json) {
            List<HelpTip.JsonModel.Entry> entries = ConfigLoader.fromJson(json);
            if (entries == null) {
                dev.xyat.kineticcore.api.client.overlay.GuiOverlay.toast(
                        net.minecraft.network.chat.Component.translatable("msg.adventuresystems.tips.tips.save_failed")
                );
                return;
            }
            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            minecraft.setScreen(new dev.xyat.adventuresystems.tips.client.gui.editor.TipEditorScreen(
                    minecraft.screen,
                    languageCode,
                    entries
            ));
        }

        public static void handleEditorDenied() {
            dev.xyat.kineticcore.api.client.overlay.GuiOverlay.toast(
                    net.minecraft.network.chat.Component.translatable("msg.adventuresystems.tips.tips.permission_denied")
            );
        }

        public static void handleEditorSaveResult(boolean success) {
            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.screen instanceof dev.xyat.adventuresystems.tips.client.gui.editor.TipEditorScreen editor) {
                editor.handleSaveResult(success);
                return;
            }
            if (success) {
                dev.xyat.kineticcore.config.client.KTConfigApi.notifySaved(
                        dev.xyat.adventuresystems.tips.config.TipsConfigGui.EDITOR_PAGE_ID
                );
            } else {
                dev.xyat.kineticcore.api.client.overlay.GuiOverlay.toast(
                        net.minecraft.network.chat.Component.translatable("msg.adventuresystems.tips.tips.save_failed")
                );
            }
        }

        public static void handleRuntimeSync(String languageCode, String json) {
            if (!selectedLanguage().equals(ConfigLoader.normalizeLanguageCode(languageCode))) return;
            List<HelpTip.JsonModel.Entry> entries = ConfigLoader.fromJson(json);
            if (entries != null) TipCache.TIP_MANAGER.replaceServerEntries(entries);
        }
    }
}
