package dev.xyat.adventuresystems.tips;

import dev.xyat.adventuresystems.tips.api.HelpTip;
import dev.xyat.adventuresystems.tips.client.TipCache;
import dev.xyat.adventuresystems.tips.client.TipRenderer;
import dev.xyat.adventuresystems.tips.client.gui.editor.TipEditorScreen;
import dev.xyat.adventuresystems.tips.config.ConfigLoader;
import dev.xyat.adventuresystems.tips.config.TipsConfigGui;
import dev.xyat.adventuresystems.tips.util.TipsStructureUtil;
import dev.xyat.kineticcore.api.client.overlay.KineticOverlays;
import dev.xyat.kineticcore.api.config.client.KTConfigApi;
import dev.xyat.kineticcore.api.network.KineticCompression;
import dev.xyat.kineticcore.api.network.NetworkBuffer;
import dev.xyat.kineticcore.api.network.NetworkCodec;
import dev.xyat.kineticcore.api.network.NetworkVersionPolicy;
import dev.xyat.kineticcore.api.network.PacketChannel;
import dev.xyat.kineticcore.api.network.ServerPacketContext;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.kineticcore.api.runtime.KineticPlatform;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TipsNetwork {
    private static final int MAX_COMPRESSED_BYTES = 2 * 1024 * 1024;
    private static final int MAX_DECOMPRESSED_BYTES = 8 * 1024 * 1024;
    private static final Map<UUID, String> CLIENT_LANGUAGES = new ConcurrentHashMap<>();
    private static final PacketChannel CHANNEL = PacketChannel.create(
            KineticResourceIds.of(TipsModule.MODID, "tips_system"),
            "2",
            NetworkVersionPolicy.ANY
    );
    private static boolean registered;

    private TipsNetwork() {
    }

    public static void register() {
        if (registered) return;
        registered = true;

        CHANNEL.registerServerbound(0, RequestStructure.class,
                NetworkCodec.of((buffer, packet) -> buffer.writeBoolean(packet.requestAll()),
                        buffer -> new RequestStructure(buffer.readBoolean())),
                TipsNetwork::handleRequestStructure);

        CHANNEL.registerClientbound(1, UpdateStructure.class,
                NetworkCodec.of(TipsNetwork::writeUpdateStructure, TipsNetwork::readUpdateStructure),
                packet -> ClientProxy.handleUpdateStructure(packet.type(), packet.singleStructure(), packet.structureList()));

        CHANNEL.registerClientbound(2, OpenEditor.class,
                NetworkCodec.of(TipsNetwork::writeOpenEditor, TipsNetwork::readOpenEditor),
                packet -> ClientProxy.handleOpenEditor(packet.languageCode(), packet.json()));

        CHANNEL.registerServerbound(3, RequestOpenEditor.class,
                NetworkCodec.of((buffer, packet) -> buffer.writeUtf(packet.languageCode(), 32),
                        buffer -> new RequestOpenEditor(buffer.readUtf(32))),
                TipsNetwork::handleRequestOpenEditor);

        CHANNEL.registerClientbound(4, EditorDenied.class,
                NetworkCodec.of((buffer, packet) -> { }, buffer -> new EditorDenied()),
                packet -> ClientProxy.handleEditorDenied());

        CHANNEL.registerServerbound(5, SaveEditor.class,
                NetworkCodec.of(TipsNetwork::writeSaveEditor, TipsNetwork::readSaveEditor),
                TipsNetwork::handleSaveEditor);

        CHANNEL.registerClientbound(6, EditorSaveResult.class,
                NetworkCodec.of((buffer, packet) -> buffer.writeBoolean(packet.success()),
                        buffer -> new EditorSaveResult(buffer.readBoolean())),
                packet -> ClientProxy.handleEditorSaveResult(packet.success()));

        CHANNEL.registerServerbound(7, RequestRuntimeTips.class,
                NetworkCodec.of((buffer, packet) -> buffer.writeUtf(packet.languageCode(), 32),
                        buffer -> new RequestRuntimeTips(buffer.readUtf(32))),
                TipsNetwork::handleRequestRuntimeTips);

        CHANNEL.registerClientbound(8, SyncRuntimeTips.class,
                NetworkCodec.of(TipsNetwork::writeSyncRuntimeTips, TipsNetwork::readSyncRuntimeTips),
                packet -> ClientProxy.handleRuntimeSync(packet.languageCode(), packet.json()));
    }

    public static void requestEditor() {
        KineticPlatform.runOnClient(() -> () -> sendToServer(new RequestOpenEditor(ClientProxy.selectedLanguage())));
    }

    public static void requestRuntimeTips() {
        KineticPlatform.runOnClient(() -> () -> sendToServer(new RequestRuntimeTips(ClientProxy.selectedLanguage())));
    }

    public static void saveEditor(String languageCode, List<HelpTip.JsonModel.Entry> entries) {
        String json;
        try {
            json = ConfigLoader.toJson(entries);
        } catch (RuntimeException exception) {
            KineticPlatform.runOnClient(() -> () -> ClientProxy.handleEditorSaveResult(false));
            return;
        }
        sendToServer(new SaveEditor(languageCode, json));
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        CHANNEL.sendToPlayer(player, message);
    }

    private static String readCompressedJson(NetworkBuffer buffer) {
        return KineticCompression.decompressUtf8(
                buffer.readByteArray(MAX_COMPRESSED_BYTES),
                MAX_DECOMPRESSED_BYTES
        );
    }

    private static void writeCompressedJson(NetworkBuffer buffer, String json) {
        byte[] compressed = KineticCompression.compressUtf8(
                json == null ? "" : json,
                MAX_COMPRESSED_BYTES,
                MAX_DECOMPRESSED_BYTES
        );
        buffer.writeByteArray(compressed, MAX_COMPRESSED_BYTES);
    }

    private static void writeUpdateStructure(NetworkBuffer buffer, UpdateStructure packet) {
        buffer.writeByte(packet.type());
        if (packet.type() == 0) {
            buffer.writeUtf(packet.singleStructure());
        } else {
            buffer.writeList(packet.structureList(), NetworkBuffer::writeResourceLocation);
        }
    }

    private static UpdateStructure readUpdateStructure(NetworkBuffer buffer) {
        int type = buffer.readByte();
        if (type == 0) return new UpdateStructure(buffer.readUtf());
        return new UpdateStructure(buffer.readList(NetworkBuffer::readResourceLocation));
    }

    private static void writeOpenEditor(NetworkBuffer buffer, OpenEditor packet) {
        buffer.writeUtf(packet.languageCode(), 32);
        writeCompressedJson(buffer, packet.json());
    }

    private static OpenEditor readOpenEditor(NetworkBuffer buffer) {
        return new OpenEditor(buffer.readUtf(32), readCompressedJson(buffer));
    }

    private static void writeSaveEditor(NetworkBuffer buffer, SaveEditor packet) {
        buffer.writeUtf(packet.languageCode(), 32);
        writeCompressedJson(buffer, packet.json());
    }

    private static SaveEditor readSaveEditor(NetworkBuffer buffer) {
        return new SaveEditor(buffer.readUtf(32), readCompressedJson(buffer));
    }

    private static void writeSyncRuntimeTips(NetworkBuffer buffer, SyncRuntimeTips packet) {
        buffer.writeUtf(packet.languageCode(), 32);
        writeCompressedJson(buffer, packet.json());
    }

    private static SyncRuntimeTips readSyncRuntimeTips(NetworkBuffer buffer) {
        return new SyncRuntimeTips(buffer.readUtf(32), readCompressedJson(buffer));
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

    private static void handleRequestStructure(RequestStructure packet, ServerPacketContext context) {
        ServerPlayer player = context.sender();
        if (!(player.level() instanceof ServerLevel level)) return;
        if (packet.requestAll()) {
            var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            sendToPlayer(new UpdateStructure(new ArrayList<>(registry.keySet())), player);
        } else {
            List<String> structures = TipsStructureUtil.getStructuresAt(level, player.blockPosition());
            sendToPlayer(new UpdateStructure(structures.isEmpty() ? "" : structures.get(0)), player);
        }
    }

    private static void handleRequestOpenEditor(RequestOpenEditor packet, ServerPacketContext context) {
        ServerPlayer player = context.sender();
        if (!player.hasPermissions(2)) {
            sendToPlayer(new EditorDenied(), player);
            return;
        }
        CLIENT_LANGUAGES.put(player.getUUID(), packet.languageCode());
        sendToPlayer(new OpenEditor(packet.languageCode(), snapshotJson(packet.languageCode())), player);
    }

    private static void handleSaveEditor(SaveEditor packet, ServerPacketContext context) {
        ServerPlayer player = context.sender();
        boolean success = false;
        if (player.hasPermissions(2)) {
            List<HelpTip.JsonModel.Entry> entries = ConfigLoader.fromJson(packet.json());
            success = entries != null && ConfigLoader.saveRawEntriesForLanguage(packet.languageCode(), entries);
            if (success) {
                CLIENT_LANGUAGES.put(player.getUUID(), packet.languageCode());
                broadcastRuntimeSnapshot(player.server, packet.languageCode());
            }
        }
        sendToPlayer(new EditorSaveResult(success), player);
    }

    private static void handleRequestRuntimeTips(RequestRuntimeTips packet, ServerPacketContext context) {
        sendRuntimeSnapshot(context.sender(), packet.languageCode());
    }

    public record RequestStructure(boolean requestAll) {
    }

    public static final class UpdateStructure {
        private final int type;
        private final String singleStructure;
        private final List<ResourceLocation> structureList;

        public UpdateStructure(String singleId) {
            this.type = 0;
            this.singleStructure = singleId == null ? "" : singleId;
            this.structureList = List.of();
        }

        public UpdateStructure(List<ResourceLocation> list) {
            this.type = 1;
            this.singleStructure = "";
            this.structureList = list == null ? List.of() : List.copyOf(list);
        }

        public int type() {
            return type;
        }

        public String singleStructure() {
            return singleStructure;
        }

        public List<ResourceLocation> structureList() {
            return structureList;
        }
    }

    public record OpenEditor(String languageCode, String json) {
        public OpenEditor {
            languageCode = ConfigLoader.normalizeLanguageCode(languageCode);
            json = json == null ? "" : json;
        }
    }

    public record RequestOpenEditor(String languageCode) {
        public RequestOpenEditor {
            languageCode = ConfigLoader.normalizeLanguageCode(languageCode);
        }
    }

    public record EditorDenied() {
    }

    public record SaveEditor(String languageCode, String json) {
        public SaveEditor {
            languageCode = ConfigLoader.normalizeLanguageCode(languageCode);
            json = json == null ? "" : json;
        }
    }

    public record EditorSaveResult(boolean success) {
    }

    public record RequestRuntimeTips(String languageCode) {
        public RequestRuntimeTips {
            languageCode = ConfigLoader.normalizeLanguageCode(languageCode);
        }
    }

    public record SyncRuntimeTips(String languageCode, String json) {
        public SyncRuntimeTips {
            languageCode = ConfigLoader.normalizeLanguageCode(languageCode);
            json = json == null ? "" : json;
        }
    }

    public static final class ClientProxy {
        private ClientProxy() {
        }

        public static String selectedLanguage() {
            return ConfigLoader.normalizeLanguageCode(KineticClientRuntime.selectedLanguage());
        }

        public static void handleUpdateStructure(int type, String singleStructure, List<ResourceLocation> structureList) {
            if (type == 0) {
                TipCache.currentStructure = singleStructure;
                if (KineticClientRuntime.currentScreen() instanceof PauseScreen) {
                    TipRenderer.refresh(KineticClientRuntime.currentScreen());
                }
            } else {
                TipCache.ALL_STRUCTURES.clear();
                TipCache.ALL_STRUCTURES.addAll(structureList);
            }
        }

        public static void handleOpenEditor(String languageCode, String json) {
            List<HelpTip.JsonModel.Entry> entries = ConfigLoader.fromJson(json);
            if (entries == null) {
                KineticOverlays.toast(KineticI18n.translatable("msg.adventuresystems.tips.tips.save_failed"));
                return;
            }
            KineticClientRuntime.openScreen(new TipEditorScreen(
                    KineticClientRuntime.currentScreen(),
                    languageCode,
                    entries
            ));
        }

        public static void handleEditorDenied() {
            KineticOverlays.toast(KineticI18n.translatable("msg.adventuresystems.tips.tips.permission_denied"));
        }

        public static void handleEditorSaveResult(boolean success) {
            if (KineticClientRuntime.currentScreen() instanceof TipEditorScreen editor) {
                editor.handleSaveResult(success);
                return;
            }
            if (success) {
                KTConfigApi.notifySaved(TipsConfigGui.EDITOR_PAGE_ID);
            } else {
                KineticOverlays.toast(KineticI18n.translatable("msg.adventuresystems.tips.tips.save_failed"));
            }
        }

        public static void handleRuntimeSync(String languageCode, String json) {
            if (!selectedLanguage().equals(ConfigLoader.normalizeLanguageCode(languageCode))) return;
            List<HelpTip.JsonModel.Entry> entries = ConfigLoader.fromJson(json);
            if (entries != null) TipCache.TIP_MANAGER.replaceServerEntries(entries);
        }
    }
}
