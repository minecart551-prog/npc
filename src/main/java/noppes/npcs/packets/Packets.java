package noppes.npcs.packets;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import noppes.npcs.CustomNpcs;
import noppes.npcs.packets.client.*;
import noppes.npcs.packets.server.*;
import noppes.npcs.shared.common.PacketBasic;
import noppes.npcs.util.CustomNPCsScheduler;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Packets {
	public static int index = 0;

	public static HashMap<Class, Integer> indexes = new HashMap<>();
	public static HashMap<Class, BiConsumer> encoders = new HashMap<>();
	
	public static void register() {

		index = 0;
		registerPacket(index++, PacketAchievement.class, PacketAchievement::encode, PacketAchievement::decode, PacketAchievement::handle);
		registerPacket(index++, PacketChat.class, PacketChat::encode, PacketChat::decode, PacketChat::handle);
		registerPacket(index++, PacketChatBubble.class, PacketChatBubble::encode, PacketChatBubble::decode, PacketChatBubble::handle);
		registerPacket(index++, PacketConfigFont.class, PacketConfigFont::encode, PacketConfigFont::decode, PacketConfigFont::handle);
		registerPacket(index++, PacketDialog.class, PacketDialog::encode, PacketDialog::decode, PacketDialog::handle);
		registerPacket(index++, PacketDialogDummy.class, PacketDialogDummy::encode, PacketDialogDummy::decode, PacketDialogDummy::handle);
		registerPacket(index++, PacketEyeBlink.class, PacketEyeBlink::encode, PacketEyeBlink::decode, PacketEyeBlink::handle);
		registerPacket(index++, PacketGuiCloneOpen.class, PacketGuiCloneOpen::encode, PacketGuiCloneOpen::decode, PacketGuiCloneOpen::handle);
		registerPacket(index++, PacketGuiClose.class, PacketGuiClose::encode, PacketGuiClose::decode, PacketGuiClose::handle);
		registerPacket(index++, PacketGuiData.class, PacketGuiData::encode, PacketGuiData::decode, PacketGuiData::handle);
		registerPacket(index++, PacketGuiComponentUpdate.class, PacketGuiComponentUpdate::encode, PacketGuiComponentUpdate::decode, PacketGuiComponentUpdate::handle);
		registerPacket(index++, PacketGuiError.class, PacketGuiError::encode, PacketGuiError::decode, PacketGuiError::handle);
		registerPacket(index++, PacketGuiOpen.class, PacketGuiOpen::encode, PacketGuiOpen::decode, PacketGuiOpen::handle);
		registerPacket(index++, PacketGuiScrollData.class, PacketGuiScrollData::encode, PacketGuiScrollData::decode, PacketGuiScrollData::handle);
		registerPacket(index++, PacketGuiScrollList.class, PacketGuiScrollList::encode, PacketGuiScrollList::decode, PacketGuiScrollList::handle);
		registerPacket(index++, PacketGuiScrollSelected.class, PacketGuiScrollSelected::encode, PacketGuiScrollSelected::decode, PacketGuiScrollSelected::handle);
		registerPacket(index++, PacketGuiUpdate.class, PacketGuiUpdate::encode, PacketGuiUpdate::decode, PacketGuiUpdate::handle);
		registerPacket(index++, PacketItemUpdate.class, PacketItemUpdate::encode, PacketItemUpdate::decode, PacketItemUpdate::handle);
		registerPacket(index++, PacketMarkData.class, PacketMarkData::encode, PacketMarkData::decode, PacketMarkData::handle);
		registerPacket(index++, PacketNpcDelete.class, PacketNpcDelete::encode, PacketNpcDelete::decode, PacketNpcDelete::handle);
		registerPacket(index++, PacketNpcEdit.class, PacketNpcEdit::encode, PacketNpcEdit::decode, PacketNpcEdit::handle);
		registerPacket(index++, PacketNpcRole.class, PacketNpcRole::encode, PacketNpcRole::decode, PacketNpcRole::handle);
		registerPacket(index++, PacketNpcUpdate.class, PacketNpcUpdate::encode, PacketNpcUpdate::decode, PacketNpcUpdate::handle);
		registerPacket(index++, PacketParticle.class, PacketParticle::encode, PacketParticle::decode, PacketParticle::handle);
		registerPacket(index++, PacketPlayMusic.class, PacketPlayMusic::encode, PacketPlayMusic::decode, PacketPlayMusic::handle);
		registerPacket(index++, PacketPlaySound.class, PacketPlaySound::encode, PacketPlaySound::decode, PacketPlaySound::handle);
		registerPacket(index++, PacketQuestCompletion.class, PacketQuestCompletion::encode, PacketQuestCompletion::decode, PacketQuestCompletion::handle);
		registerPacket(index++, PacketSync.class, PacketSync::encode, PacketSync::decode, PacketSync::handle);
		registerPacket(index++, PacketSyncRemove.class, PacketSyncRemove::encode, PacketSyncRemove::decode, PacketSyncRemove::handle);
		registerPacket(index++, PacketSyncUpdate.class, PacketSyncUpdate::encode, PacketSyncUpdate::decode, PacketSyncUpdate::handle);
		registerPacket(index++, PacketNpcVisibleFalse.class, PacketNpcVisibleFalse::encode, PacketNpcVisibleFalse::decode, PacketNpcVisibleFalse::handle);
		registerPacket(index++, PacketNpcVisibleTrue.class, PacketNpcVisibleTrue::encode, PacketNpcVisibleTrue::decode, PacketNpcVisibleTrue::handle);
		registerPacket(index++, PacketUpdatePhysics.class, PacketUpdatePhysics::encode, PacketUpdatePhysics::decode, PacketUpdatePhysics::handle);
		registerPacket(index++, PacketGuiParts.class, PacketGuiParts::encode, PacketGuiParts::decode, PacketGuiParts::handle);
		registerPacket(index++, PacketOverlayShow.class, PacketOverlayShow::encode, PacketOverlayShow::decode, PacketBasic::handle);
		registerPacket(index++, PacketOverlayHide.class, PacketOverlayHide::encode, PacketOverlayHide::decode, PacketBasic::handle);
		registerPacket(index++, PacketHideAllOverlays.class, PacketHideAllOverlays::encode, PacketHideAllOverlays::decode, PacketBasic::handle);
		registerPacket(index++, PacketSoundGUIOpen.class, PacketSoundGUIOpen::encode, PacketSoundGUIOpen::decode, PacketBasic::handle);
		registerPacket(index++, PacketNpcRotationUpdate.class, PacketNpcRotationUpdate::encode, PacketNpcRotationUpdate::decode, PacketNpcRotationUpdate::handle);
		registerPacket(index++, PacketSyncSkin.class, PacketSyncSkin::encode, PacketSyncSkin::decode, PacketSyncSkin::handle);

		registerPacket(index++, SPacketBankGet.class, SPacketBankGet::encode, SPacketBankGet::decode, SPacketBankGet::handle);
		registerPacket(index++, SPacketBankRemove.class, SPacketBankRemove::encode, SPacketBankRemove::decode, SPacketBankRemove::handle);
		registerPacket(index++, SPacketBankSave.class, SPacketBankSave::encode, SPacketBankSave::decode, SPacketBankSave::handle);
		registerPacket(index++, SPacketBanksGet.class, SPacketBanksGet::encode, SPacketBanksGet::decode, SPacketBanksGet::handle);
		registerPacket(index++, SPacketBanksSlotOpen.class, SPacketBanksSlotOpen::encode, SPacketBanksSlotOpen::decode, SPacketBanksSlotOpen::handle);
		registerPacket(index++, SPacketBankUnlock.class, SPacketBankUnlock::encode, SPacketBankUnlock::decode, SPacketBankUnlock::handle);
		registerPacket(index++, SPacketBankUpgrade.class, SPacketBankUpgrade::encode, SPacketBankUpgrade::decode, SPacketBankUpgrade::handle);
		registerPacket(index++, SPacketCloneList.class, SPacketCloneList::encode, SPacketCloneList::decode, SPacketCloneList::handle);
		registerPacket(index++, SPacketCloneNameCheck.class, SPacketCloneNameCheck::encode, SPacketCloneNameCheck::decode, SPacketCloneNameCheck::handle);
		registerPacket(index++, SPacketCloneRemove.class, SPacketCloneRemove::encode, SPacketCloneRemove::decode, SPacketCloneRemove::handle);
		registerPacket(index++, SPacketCloneSave.class, SPacketCloneSave::encode, SPacketCloneSave::decode, SPacketCloneSave::handle);
		registerPacket(index++, SPacketCompanionOpenInv.class, SPacketCompanionOpenInv::encode, SPacketCompanionOpenInv::decode, SPacketCompanionOpenInv::handle);
		registerPacket(index++, SPacketCompanionTalentExp.class, SPacketCompanionTalentExp::encode, SPacketCompanionTalentExp::decode, SPacketCompanionTalentExp::handle);
		registerPacket(index++, SPacketDialogCategoryRemove.class, SPacketDialogCategoryRemove::encode, SPacketDialogCategoryRemove::decode, SPacketDialogCategoryRemove::handle);
		registerPacket(index++, SPacketDialogRemove.class, SPacketDialogRemove::encode, SPacketDialogRemove::decode, SPacketDialogRemove::handle);
		registerPacket(index++, SPacketDialogSelected.class, SPacketDialogSelected::encode, SPacketDialogSelected::decode, SPacketDialogSelected::handle);
		registerPacket(index++, SPacketDimensionsGet.class, SPacketDimensionsGet::encode, SPacketDimensionsGet::decode, SPacketDimensionsGet::handle);
		registerPacket(index++, SPacketDimensionTeleport.class, SPacketDimensionTeleport::encode, SPacketDimensionTeleport::decode, SPacketDimensionTeleport::handle);
		registerPacket(index++, SPacketFactionGet.class, SPacketFactionGet::encode, SPacketFactionGet::decode, SPacketFactionGet::handle);
		registerPacket(index++, SPacketFactionRemove.class, SPacketFactionRemove::encode, SPacketFactionRemove::decode, SPacketFactionRemove::handle);
		registerPacket(index++, SPacketFactionSave.class, SPacketFactionSave::encode, SPacketFactionSave::decode, SPacketFactionSave::handle);
		registerPacket(index++, SPacketFactionsGet.class, SPacketFactionsGet::encode, SPacketFactionsGet::decode, SPacketFactionsGet::handle);
		registerPacket(index++, SPacketFollowerExtend.class, SPacketFollowerExtend::encode, SPacketFollowerExtend::decode, SPacketFollowerExtend::handle);
		registerPacket(index++, SPacketFollowerHire.class, SPacketFollowerHire::encode, SPacketFollowerHire::decode, SPacketFollowerHire::handle);
		registerPacket(index++, SPacketFollowerState.class, SPacketFollowerState::encode, SPacketFollowerState::decode, SPacketFollowerState::handle);
		registerPacket(index++, SPacketGuiOpen.class, SPacketGuiOpen::encode, SPacketGuiOpen::decode, SPacketGuiOpen::handle);
		registerPacket(index++, SPacketLinkedAdd.class, SPacketLinkedAdd::encode, SPacketLinkedAdd::decode, SPacketLinkedAdd::handle);
		registerPacket(index++, SPacketLinkedGet.class, SPacketLinkedGet::encode, SPacketLinkedGet::decode, SPacketLinkedGet::handle);
		registerPacket(index++, SPacketLinkedRemove.class, SPacketLinkedRemove::encode, SPacketLinkedRemove::decode, SPacketLinkedRemove::handle);
		registerPacket(index++, SPacketLinkedSet.class, SPacketLinkedSet::encode, SPacketLinkedSet::decode, SPacketLinkedSet::handle);
		registerPacket(index++, SPacketMailSetup.class, SPacketMailSetup::encode, SPacketMailSetup::decode, SPacketMailSetup::handle);
		registerPacket(index++, SPacketMenuClose.class, SPacketMenuClose::encode, SPacketMenuClose::decode, SPacketMenuClose::handle);
		registerPacket(index++, SPacketMenuGet.class, SPacketMenuGet::encode, SPacketMenuGet::decode, SPacketMenuGet::handle);
		registerPacket(index++, SPacketMenuSave.class, SPacketMenuSave::encode, SPacketMenuSave::decode, SPacketMenuSave::handle);
		registerPacket(index++, SPacketNaturalSpawnGet.class, SPacketNaturalSpawnGet::encode, SPacketNaturalSpawnGet::decode, SPacketNaturalSpawnGet::handle);
		registerPacket(index++, SPacketNaturalSpawnGetAll.class, SPacketNaturalSpawnGetAll::encode, SPacketNaturalSpawnGetAll::decode, SPacketNaturalSpawnGetAll::handle);
		registerPacket(index++, SPacketNaturalSpawnRemove.class, SPacketNaturalSpawnRemove::encode, SPacketNaturalSpawnRemove::decode, SPacketNaturalSpawnRemove::handle);
		registerPacket(index++, SPacketNaturalSpawnSave.class, SPacketNaturalSpawnSave::encode, SPacketNaturalSpawnSave::decode, SPacketNaturalSpawnSave::handle);
		registerPacket(index++, SPacketNbtBookBlockSave.class, SPacketNbtBookBlockSave::encode, SPacketNbtBookBlockSave::decode, SPacketNbtBookBlockSave::handle);
		registerPacket(index++, SPacketNbtBookEntitySave.class, SPacketNbtBookEntitySave::encode, SPacketNbtBookEntitySave::decode, SPacketNbtBookEntitySave::handle);
		registerPacket(index++, SPacketNpcDelete.class, SPacketNpcDelete::encode, SPacketNpcDelete::decode, SPacketNpcDelete::handle);
		registerPacket(index++, SPacketNpcDialogRemove.class, SPacketNpcDialogRemove::encode, SPacketNpcDialogRemove::decode, SPacketNpcDialogRemove::handle);
		registerPacket(index++, SPacketNpcDialogSet.class, SPacketNpcDialogSet::encode, SPacketNpcDialogSet::decode, SPacketNpcDialogSet::handle);
		registerPacket(index++, SPacketNpcDialogsGet.class, SPacketNpcDialogsGet::encode, SPacketNpcDialogsGet::decode, SPacketNpcDialogsGet::handle);
		registerPacket(index++, SPacketNpcFactionSet.class, SPacketNpcFactionSet::encode, SPacketNpcFactionSet::decode, SPacketNpcFactionSet::handle);
		registerPacket(index++, SPacketNpcJobGet.class, SPacketNpcJobGet::encode, SPacketNpcJobGet::decode, SPacketNpcJobGet::handle);
		registerPacket(index++, SPacketNpcJobSave.class, SPacketNpcJobSave::encode, SPacketNpcJobSave::decode, SPacketNpcJobSave::handle);
		registerPacket(index++, SPacketNpcJobSpawnerSet.class, SPacketNpcJobSpawnerSet::encode, SPacketNpcJobSpawnerSet::decode, SPacketNpcJobSpawnerSet::handle);
		registerPacket(index++, SPacketNpcMarketSet.class, SPacketNpcMarketSet::encode, SPacketNpcMarketSet::decode, SPacketNpcMarketSet::handle);
		registerPacket(index++, SPacketNpcRoleCompanionUpdate.class, SPacketNpcRoleCompanionUpdate::encode, SPacketNpcRoleCompanionUpdate::decode, SPacketNpcRoleCompanionUpdate::handle);
		registerPacket(index++, SPacketNpcRoleGet.class, SPacketNpcRoleGet::encode, SPacketNpcRoleGet::decode, SPacketNpcRoleGet::handle);
		registerPacket(index++, SPacketNpcRoleSave.class, SPacketNpcRoleSave::encode, SPacketNpcRoleSave::decode, SPacketNpcRoleSave::handle);
		registerPacket(index++, SPacketNpcTransform.class, SPacketNpcTransform::encode, SPacketNpcTransform::decode, SPacketNpcTransform::handle);
		registerPacket(index++, SPacketNpcTransportGet.class, SPacketNpcTransportGet::encode, SPacketNpcTransportGet::decode, SPacketNpcTransportGet::handle);
		registerPacket(index++, SPacketPlayerCloseContainer.class, SPacketPlayerCloseContainer::encode, SPacketPlayerCloseContainer::decode, SPacketPlayerCloseContainer::handle);
		registerPacket(index++, SPacketPlayerDataGet.class, SPacketPlayerDataGet::encode, SPacketPlayerDataGet::decode, SPacketPlayerDataGet::handle);
		registerPacket(index++, SPacketPlayerDataRemove.class, SPacketPlayerDataRemove::encode, SPacketPlayerDataRemove::decode, SPacketPlayerDataRemove::handle);
		registerPacket(index++, SPacketPlayerKeyPressed.class, SPacketPlayerKeyPressed::encode, SPacketPlayerKeyPressed::decode, SPacketPlayerKeyPressed::handle);
		registerPacket(index++, SPacketPlayerLeftClicked.class, SPacketPlayerLeftClicked::encode, SPacketPlayerLeftClicked::decode, SPacketPlayerLeftClicked::handle);
		registerPacket(index++, SPacketPlayerMailDelete.class, SPacketPlayerMailDelete::encode, SPacketPlayerMailDelete::decode, SPacketPlayerMailDelete::handle);
		registerPacket(index++, SPacketPlayerMailGet.class, SPacketPlayerMailGet::encode, SPacketPlayerMailGet::decode, SPacketPlayerMailGet::handle);
		registerPacket(index++, SPacketPlayerMailOpen.class, SPacketPlayerMailOpen::encode, SPacketPlayerMailOpen::decode, SPacketPlayerMailOpen::handle);
		registerPacket(index++, SPacketPlayerMailRead.class, SPacketPlayerMailRead::encode, SPacketPlayerMailRead::decode, SPacketPlayerMailRead::handle);
		registerPacket(index++, SPacketPlayerMailSend.class, SPacketPlayerMailSend::encode, SPacketPlayerMailSend::decode, SPacketPlayerMailSend::handle);
		registerPacket(index++, SPacketPlayerTransport.class, SPacketPlayerTransport::encode, SPacketPlayerTransport::decode, SPacketPlayerTransport::handle);
		registerPacket(index++, SPacketQuestCategoryRemove.class, SPacketQuestCategoryRemove::encode, SPacketQuestCategoryRemove::decode, SPacketQuestCategoryRemove::handle);
		registerPacket(index++, SPacketQuestCompletionCheck.class, SPacketQuestCompletionCheck::encode, SPacketQuestCompletionCheck::decode, SPacketQuestCompletionCheck::handle);
		registerPacket(index++, SPacketQuestCompletionCheckAll.class, SPacketQuestCompletionCheckAll::encode, SPacketQuestCompletionCheckAll::decode, SPacketQuestCompletionCheckAll::handle);
		registerPacket(index++, SPacketQuestDialogTitles.class, SPacketQuestDialogTitles::encode, SPacketQuestDialogTitles::decode, SPacketQuestDialogTitles::handle);
		registerPacket(index++, SPacketQuestOpen.class, SPacketQuestOpen::encode, SPacketQuestOpen::decode, SPacketQuestOpen::handle);
		registerPacket(index++, SPacketQuestRemove.class, SPacketQuestRemove::encode, SPacketQuestRemove::decode, SPacketQuestRemove::handle);
		registerPacket(index++, SPacketRecipeGet.class, SPacketRecipeGet::encode, SPacketRecipeGet::decode, SPacketRecipeGet::handle);
		registerPacket(index++, SPacketRecipeRemove.class, SPacketRecipeRemove::encode, SPacketRecipeRemove::decode, SPacketRecipeRemove::handle);
		registerPacket(index++, SPacketRecipeSave.class, SPacketRecipeSave::encode, SPacketRecipeSave::decode, SPacketRecipeSave::handle);
		registerPacket(index++, SPacketRecipesGet.class, SPacketRecipesGet::encode, SPacketRecipesGet::decode, SPacketRecipesGet::handle);
		registerPacket(index++, SPacketRemoteFreeze.class, SPacketRemoteFreeze::encode, SPacketRemoteFreeze::decode, SPacketRemoteFreeze::handle);
		registerPacket(index++, SPacketRemoteMenuOpen.class, SPacketRemoteMenuOpen::encode, SPacketRemoteMenuOpen::decode, SPacketRemoteMenuOpen::handle);
		registerPacket(index++, SPacketRemoteNpcDelete.class, SPacketRemoteNpcDelete::encode, SPacketRemoteNpcDelete::decode, SPacketRemoteNpcDelete::handle);
		registerPacket(index++, SPacketRemoteNpcReset.class, SPacketRemoteNpcReset::encode, SPacketRemoteNpcReset::decode, SPacketRemoteNpcReset::handle);
		registerPacket(index++, SPacketRemoteNpcsGet.class, SPacketRemoteNpcsGet::encode, SPacketRemoteNpcsGet::decode, SPacketRemoteNpcsGet::handle);
		registerPacket(index++, SPacketRemoteNpcTp.class, SPacketRemoteNpcTp::encode, SPacketRemoteNpcTp::decode, SPacketRemoteNpcTp::handle);
		registerPacket(index++, SPacketSceneReset.class, SPacketSceneReset::encode, SPacketSceneReset::decode, SPacketSceneReset::handle);
		registerPacket(index++, SPacketSceneStart.class, SPacketSceneStart::encode, SPacketSceneStart::decode, SPacketSceneStart::handle);
		registerPacket(index++, SPacketSchematicsStore.class, SPacketSchematicsStore::encode, SPacketSchematicsStore::decode, SPacketSchematicsStore::handle);
		registerPacket(index++, SPacketSchematicsTileBuild.class, SPacketSchematicsTileBuild::encode, SPacketSchematicsTileBuild::decode, SPacketSchematicsTileBuild::handle);
		registerPacket(index++, SPacketSchematicsTileGet.class, SPacketSchematicsTileGet::encode, SPacketSchematicsTileGet::decode, SPacketSchematicsTileGet::handle);
		registerPacket(index++, SPacketSchematicsTileSave.class, SPacketSchematicsTileSave::encode, SPacketSchematicsTileSave::decode, SPacketSchematicsTileSave::handle);
		registerPacket(index++, SPacketSchematicsTileSet.class, SPacketSchematicsTileSet::encode, SPacketSchematicsTileSet::decode, SPacketSchematicsTileSet::handle);
		registerPacket(index++, SPacketScriptGet.class, SPacketScriptGet::encode, SPacketScriptGet::decode, SPacketScriptGet::handle);
		registerPacket(index++, SPacketTileEntityGet.class, SPacketTileEntityGet::encode, SPacketTileEntityGet::decode, SPacketTileEntityGet::handle);
		registerPacket(index++, SPacketTileEntitySave.class, SPacketTileEntitySave::encode, SPacketTileEntitySave::decode, SPacketTileEntitySave::handle);
		registerPacket(index++, SPacketToolMounter.class, SPacketToolMounter::encode, SPacketToolMounter::decode, SPacketToolMounter::handle);
		registerPacket(index++, SPacketTransportCategoriesGet.class, SPacketTransportCategoriesGet::encode, SPacketTransportCategoriesGet::decode, SPacketTransportCategoriesGet::handle);
		registerPacket(index++, SPacketTransportCategoryRemove.class, SPacketTransportCategoryRemove::encode, SPacketTransportCategoryRemove::decode, SPacketTransportCategoryRemove::handle);
		registerPacket(index++, SPacketTransportCategorySave.class, SPacketTransportCategorySave::encode, SPacketTransportCategorySave::decode, SPacketTransportCategorySave::handle);
		registerPacket(index++, SPacketTransportGet.class, SPacketTransportGet::encode, SPacketTransportGet::decode, SPacketTransportGet::handle);
		registerPacket(index++, SPacketTransportRemove.class, SPacketTransportRemove::encode, SPacketTransportRemove::decode, SPacketTransportRemove::handle);
		registerPacket(index++, SPacketTransportSave.class, SPacketTransportSave::encode, SPacketTransportSave::decode, SPacketTransportSave::handle);
		registerPacket(index++, SPacketCustomGuiButton.class, SPacketCustomGuiButton::encode, SPacketCustomGuiButton::decode, SPacketCustomGuiButton::handle);
		registerPacket(index++, SPacketCustomGuiButtonList.class, SPacketCustomGuiButtonList::encode, SPacketCustomGuiButtonList::decode, SPacketCustomGuiButtonList::handle);
		registerPacket(index++, SPacketCustomGuiTextUpdate.class, SPacketCustomGuiTextUpdate::encode, SPacketCustomGuiTextUpdate::decode, SPacketCustomGuiTextUpdate::handle);
		registerPacket(index++, SPacketCustomGuiSliderUpdate.class, SPacketCustomGuiSliderUpdate::encode, SPacketCustomGuiSliderUpdate::decode, SPacketCustomGuiSliderUpdate::handle);
		registerPacket(index++, SPacketCustomGuiFocusUpdate.class, SPacketCustomGuiFocusUpdate::encode, SPacketCustomGuiFocusUpdate::decode, SPacketCustomGuiFocusUpdate::handle);
		registerPacket(index++, SPacketCustomGuiScrollClick.class, SPacketCustomGuiScrollClick::encode, SPacketCustomGuiScrollClick::decode, SPacketCustomGuiScrollClick::handle);
		registerPacket(index++, SPacketCustomGuiSubGuiClosed.class, SPacketCustomGuiSubGuiClosed::encode, SPacketCustomGuiSubGuiClosed::decode, SPacketCustomGuiSubGuiClosed::handle);
		registerPacket(index++, SPacketCustomGuiParts.class, SPacketCustomGuiParts::encode, SPacketCustomGuiParts::decode, SPacketCustomGuiParts::handle);
		registerPacket(index++, SPacketNpRandomNameSet.class, SPacketNpRandomNameSet::encode, SPacketNpRandomNameSet::decode, SPacketNpRandomNameSet::handle);
		registerPacket(index++, SPacketPlayerSoundPlays.class, SPacketPlayerSoundPlays::encode, SPacketPlayerSoundPlays::decode, SPacketPlayerSoundPlays::handle);

		registerPacket(index++, SPacketScriptSave.class, SPacketScriptSave::encode, SPacketScriptSave::decode, SPacketScriptSave::handle);
		registerPacket(index++, SPacketToolMobSpawner.class, SPacketToolMobSpawner::encode, SPacketToolMobSpawner::decode, SPacketToolMobSpawner::handle);
		registerPacket(index++, SPacketQuestSave.class, SPacketQuestSave::encode, SPacketQuestSave::decode, SPacketQuestSave::handle);
		registerPacket(index++, SPacketQuestCategorySave.class, SPacketQuestCategorySave::encode, SPacketQuestCategorySave::decode, SPacketQuestCategorySave::handle);
		registerPacket(index++, SPacketDialogSave.class, SPacketDialogSave::encode, SPacketDialogSave::decode, SPacketDialogSave::handle);
		registerPacket(index++, SPacketDialogCategorySave.class, SPacketDialogCategorySave::encode, SPacketDialogCategorySave::decode, SPacketDialogCategorySave::handle);
		registerPacket(index++, SPacketOpenParts.class, SPacketOpenParts::encode, SPacketOpenParts::decode, SPacketOpenParts::handle);

//		CNpcsNetworkHelper.addPacket(SPacketScriptSave.class, SPacketScriptSave::new);
//		CNpcsNetworkHelper.addPacket(SPacketToolMobSpawner.class, SPacketToolMobSpawner::new);
//		CNpcsNetworkHelper.addPacket(SPacketQuestSave.class, SPacketQuestSave::new);
//		CNpcsNetworkHelper.addPacket(SPacketQuestCategorySave.class, SPacketQuestCategorySave::new);
//		CNpcsNetworkHelper.addPacket(SPacketDialogSave.class, SPacketDialogSave::new);
//		CNpcsNetworkHelper.addPacket(SPacketDialogCategorySave.class, SPacketDialogCategorySave::new);
	}

	public static <MSG> void registerPacket(int index, Class<MSG> messageType, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, TriConsumer<MSG, MinecraftServer, ServerPlayer> handle) {
		indexes.put(messageType, index);
		encoders.put(messageType, encoder);
		ServerPlayNetworking.registerGlobalReceiver(new ResourceLocation("customnpcs", ""+index), (server, player, _handler, buf, _responseSender) -> handle.accept(decoder.apply(buf), server, player));
	}
	public static <MSG> void registerPacket(int index, Class<MSG> messageType, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, Consumer<MSG> handle) {
		indexes.put(messageType, index);
		encoders.put(messageType, encoder);
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			ClientPlayNetworking.registerGlobalReceiver(new ResourceLocation("customnpcs", "" + index), (client, _handler, buf, _responseSender) -> handle.accept(decoder.apply(buf)));
		}
	}

//	public static void addPacket(Class c, Supplier sup){
//		ProtocolTypeMixin type = (ProtocolTypeMixin)(Object) ConnectionProtocol.PLAY;
//		ConnectionProtocol.PacketSet list = (ConnectionProtocol.PacketSet) type.getFows().get(PacketFlow.SERVERBOUND);
//		list.addPacket(c, sup);
//
//		type.getProtocols().put(c, ConnectionProtocol.PLAY);
//	}

	public static <MSG> void send(ServerPlayer player, MSG msg) {
		FriendlyByteBuf ret = new FriendlyByteBuf(Unpooled.buffer());
		encoders.get(msg.getClass()).accept(msg, ret);
		ServerPlayNetworking.send(player, new ResourceLocation("customnpcs", ""+indexes.get(msg.getClass())), ret);
	}

	public static <MSG> void sendNearby(Level level, BlockPos pos, int range, MSG msg) {
		for(ServerPlayer player: level.getServer().getPlayerList().getPlayers()){
			if(player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ())<=range && player.level().dimension().equals(level.dimension())){
				send(player, msg);
			}
		}
	}
	
	public static <MSG> void sendNearby(Entity entity, MSG msg) {
		Set<ServerPlayerConnection> connections = ((ServerChunkCache)entity.getCommandSenderWorld().getChunkSource()).chunkMap.entityMap.get(entity.getId()).seenBy;
		for(ServerPlayerConnection conn : connections){
			ServerPlayer player = conn.getPlayer();
			send(player, msg);
		}
	}
	
	public static <MSG> void sendAll(MSG msg) {
		for(ServerPlayer player: CustomNpcs.Server.getPlayerList().getPlayers()) {
			send(player, msg);
		}
	}
	
	public static <MSG> void sendServer(MSG msg){
		if(msg instanceof Packet){
			Minecraft.getInstance().getConnection().getConnection().send((Packet)msg);
		}
		else{
			FriendlyByteBuf ret = new FriendlyByteBuf(Unpooled.buffer());
			encoders.get(msg.getClass()).accept(msg, ret);
			ClientPlayNetworking.send(new ResourceLocation("customnpcs", ""+indexes.get(msg.getClass())), ret);
		}
	}
}
