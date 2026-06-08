// ============================================================================
// KILL EFFECT SYSTEM - Manual cosmetic kill effects
// ============================================================================
// Script Type: PlayerEvent
// Target: CustomNPCs 1.20.1
// Style: ES5 | No Semicolons
// Commands:
// !killeffadmin - Admin GUI for granting/revoking online players
// !killeff      - Player GUI for selecting an unlocked kill effect
// !killeff debug - Shows your active/unlocked data and previews the active effect
// !killeff debug on/off - Toggles kill-event debug messages
// Rarities:
// Common - white name
// Rare - aqua name
// Epic - purple name
// Legendary - gold name
// ============================================================================

var API = Java.type("noppes.npcs.api.NpcAPI").Instance()
var Thread = Java.type("java.lang.Thread")

var KILL_EFFECT_KEY = "kill_effects_v1"
var KILL_EFFECT_DEBUG_KEY = "kill_effects_debug_v1"
var KILL_EFFECT_ACTIVE_THREADS = 0
var KILL_EFFECT_MAX_THREADS = 8

var KILL_EFFECT_ADMIN_GUI = 8600
var KILL_EFFECT_PLAYER_GUI = 8601
var KILL_EFFECT_ADMIN_WIDTH = 540
var KILL_EFFECT_ADMIN_HEIGHT = 248
var KILL_EFFECT_PLAYER_WIDTH = 320
var KILL_EFFECT_PLAYER_HEIGHT = 246
var KILL_EFFECT_MAIN_MENU_GUI = 9002
var KILL_EFFECT_QUEST_MENU_GUI = 9003
var KILL_EFFECT_MAIN_MENU_BTN_QUESTS = 201
var KILL_EFFECT_MAIN_MENU_BTN_KILL_EFFECTS = 202
var KILL_EFFECT_MAIN_MENU_BTN_CLOSE = 203
var KILL_EFFECT_QUEST_MENU_BTN_CLOSE = 304
var KILL_EFFECT_MAIN_MENU_BTN_RANKS = 205
var KILL_EFFECT_MAIN_MENU_BTN_GUILDS = 206
var KILL_EFFECT_MAIN_MENU_BTN_COSMETICS = 207
var CYBER_QUEST_SCROLL = 301
var CYBER_QUEST_DETAILS = 302
var CYBER_QUEST_BTN_BACK = 303

var KILL_EFFECT_IDS = {
    ADMIN_SCROLL_PLAYERS: 10,
    ADMIN_SCROLL_EFFECTS: 11,
    ADMIN_BTN_GRANT: 20,
    ADMIN_BTN_REVOKE: 21,
    ADMIN_BTN_SET_ACTIVE: 22,
    ADMIN_BTN_CLEAR_ACTIVE: 23,
    ADMIN_BTN_PREVIEW: 24,
    ADMIN_BTN_REFRESH: 25,
    ADMIN_BTN_CLOSE: 26,

    PLAYER_SCROLL_EFFECTS: 40,
    PLAYER_BTN_SET_ACTIVE: 50,
    PLAYER_BTN_CLEAR_ACTIVE: 51,
    PLAYER_BTN_PREVIEW: 52,
    PLAYER_BTN_CLOSE: 53
}

var KILL_EFFECT_UI = {}

// Add new effects here. Keep ids lowercase and stable because player data stores them.
var KILL_EFFECTS = [
    {
        id: "ember_ring",
        name: "Ember Ring",
        rarity: "common",
        color: "\u00A7f",
        description: "A quick flame ring around the kill",
        sound: "minecraft:block.fire.extinguish",
        particle: "minecraft:flame",
        pattern: "ring",
        radius: 1.55,
        points: 30,
        yOffset: 0.18,
        speed: 0.01,
        count: 1,
        volume: 0.7,
        pitch: 1.25
    },
    {
        id: "crit_spark",
        name: "Crit Spark",
        rarity: "common",
        color: "\u00A7f",
        description: "A clean critical spark pop",
        sound: "minecraft:entity.player.attack.crit",
        particle: "minecraft:crit",
        pattern: "burst",
        radius: 0.95,
        points: 18,
        yOffset: 0.7,
        speed: 0.08,
        count: 1,
        volume: 0.65,
        pitch: 1.35
    },
    {
        id: "smoke_puff",
        name: "Smoke Puff",
        rarity: "common",
        color: "\u00A7f",
        description: "A soft smoke puff",
        sound: "minecraft:block.campfire.crackle",
        particle: "minecraft:smoke",
        pattern: "pillar",
        radius: 0.55,
        points: 18,
        yOffset: 0.2,
        height: 1.5,
        speed: 0.02,
        count: 2,
        volume: 0.55,
        pitch: 1.2
    },
    {
        id: "heart_pop",
        name: "Heart Pop",
        rarity: "common",
        color: "\u00A7f",
        description: "A lighthearted heart pop",
        sound: "minecraft:entity.experience_orb.pickup",
        particle: "minecraft:heart",
        pattern: "bloom",
        radius: 0.9,
        points: 14,
        yOffset: 0.85,
        speed: 0.01,
        count: 1,
        volume: 0.55,
        pitch: 1.75
    },
    {
        id: "villager_cheer",
        name: "Villager Cheer",
        rarity: "common",
        color: "\u00A7f",
        description: "Small happy sparks",
        sound: "minecraft:entity.villager.yes",
        particle: "minecraft:happy_villager",
        pattern: "bloom",
        radius: 1.05,
        points: 18,
        yOffset: 0.45,
        speed: 0.02,
        count: 1,
        volume: 0.6,
        pitch: 1.45
    },
    {
        id: "cloud_step",
        name: "Cloud Step",
        rarity: "common",
        color: "\u00A7f",
        description: "A low cloud ring",
        sound: "minecraft:block.wool.step",
        particle: "minecraft:cloud",
        pattern: "ring",
        radius: 1.35,
        points: 24,
        yOffset: 0.1,
        speed: 0.02,
        count: 2,
        volume: 0.5,
        pitch: 1.1
    },
    {
        id: "soul_spiral",
        name: "Soul Spiral",
        rarity: "rare",
        color: "\u00A7b",
        description: "A rising soul spiral",
        sound: "minecraft:entity.warden.sonic_boom",
        particle: "minecraft:soul_fire_flame",
        pattern: "spiral",
        radius: 1.15,
        points: 34,
        yOffset: 0.1,
        height: 2.25,
        speed: 0.01,
        count: 1,
        volume: 0.55,
        pitch: 1.6
    },
    {
        id: "aqua_bloom",
        name: "Aqua Bloom",
        rarity: "rare",
        color: "\u00A7b",
        description: "A blue dust flower",
        sound: "minecraft:block.amethyst_block.hit",
        particle: "dust",
        dustRed: 0.1,
        dustGreen: 0.75,
        dustBlue: 1,
        dustScale: 1.25,
        pattern: "bloom",
        radius: 1.45,
        points: 26,
        yOffset: 0.55,
        speed: 0,
        count: 1,
        volume: 0.7,
        pitch: 1.4
    },
    {
        id: "frost_shatter",
        name: "Frost Shatter",
        rarity: "rare",
        color: "\u00A7b",
        description: "A cold shatter burst",
        sound: "minecraft:block.glass.break",
        particle: "minecraft:snowflake",
        pattern: "nova",
        radius: 1.65,
        points: 32,
        yOffset: 0.55,
        speed: 0.03,
        count: 1,
        volume: 0.65,
        pitch: 1.35
    },
    {
        id: "emerald_runeburst",
        name: "Emerald Runeburst",
        rarity: "rare",
        color: "\u00A7b",
        description: "Green runes flash under the target",
        sound: "minecraft:block.enchantment_table.use",
        particle: "dust",
        dustRed: 0.1,
        dustGreen: 1,
        dustBlue: 0.35,
        dustScale: 1.1,
        pattern: "rune",
        radius: 1.4,
        points: 36,
        yOffset: 0.12,
        speed: 0,
        count: 1,
        volume: 0.65,
        pitch: 1.2
    },
    {
        id: "spark_column",
        name: "Spark Column",
        rarity: "rare",
        color: "\u00A7b",
        description: "A bright vertical spark column",
        sound: "minecraft:block.beacon.power_select",
        particle: "minecraft:electric_spark",
        pattern: "pillar",
        radius: 0.75,
        points: 28,
        yOffset: 0.15,
        height: 2.8,
        speed: 0.04,
        count: 1,
        volume: 0.7,
        pitch: 1.35
    },
    {
        id: "nautilus_wave",
        name: "Nautilus Wave",
        rarity: "rare",
        color: "\u00A7b",
        description: "A small nautilus wave",
        sound: "minecraft:item.trident.return",
        particle: "minecraft:nautilus",
        pattern: "helix",
        radius: 1.15,
        points: 30,
        yOffset: 0.15,
        height: 1.9,
        speed: 0.01,
        count: 1,
        volume: 0.65,
        pitch: 1.25
    },
    {
        id: "gold_burst",
        name: "Gold Burst",
        rarity: "epic",
        color: "\u00A7d",
        description: "A cosmetic gold dust burst",
        sound: "minecraft:block.amethyst_block.chime",
        particle: "dust",
        dustRed: 1,
        dustGreen: 0.72,
        dustBlue: 0.18,
        dustScale: 1.3,
        pattern: "burst",
        radius: 1.35,
        points: 28,
        yOffset: 0.75,
        speed: 0,
        count: 1,
        volume: 0.85,
        pitch: 1.1
    },
    {
        id: "arcane_vortex",
        name: "Arcane Vortex",
        rarity: "epic",
        color: "\u00A7d",
        description: "Purple magic twists inward",
        sound: "minecraft:block.portal.ambient",
        particle: "minecraft:portal",
        pattern: "vortex",
        radius: 2,
        points: 42,
        yOffset: 0.25,
        height: 2.2,
        speed: 0.04,
        count: 1,
        volume: 0.75,
        pitch: 1.4
    },
    {
        id: "dragon_whisper",
        name: "Dragon Whisper",
        rarity: "epic",
        color: "\u00A7d",
        description: "Dragon breath coils upward",
        sound: "minecraft:entity.ender_dragon.flap",
        particle: "minecraft:dragon_breath",
        pattern: "helix",
        radius: 1.55,
        points: 42,
        yOffset: 0.15,
        height: 2.6,
        speed: 0.02,
        count: 1,
        volume: 0.75,
        pitch: 1.3
    },
    {
        id: "witch_hex",
        name: "Witch Hex",
        rarity: "epic",
        color: "\u00A7d",
        description: "A crooked witch sigil",
        sound: "minecraft:entity.witch.celebrate",
        particle: "minecraft:witch",
        pattern: "rune",
        radius: 1.75,
        points: 42,
        yOffset: 0.22,
        speed: 0.04,
        count: 1,
        volume: 0.7,
        pitch: 1.15
    },
    {
        id: "end_rod_crown",
        name: "End Rod Crown",
        rarity: "epic",
        color: "\u00A7d",
        description: "A crown of pale end sparks",
        sound: "minecraft:block.end_portal_frame.fill",
        particle: "minecraft:end_rod",
        pattern: "crown",
        radius: 1.45,
        points: 34,
        yOffset: 1.55,
        height: 0.9,
        speed: 0.02,
        count: 1,
        volume: 0.85,
        pitch: 1.25
    },
    {
        id: "totem_echo",
        name: "Totem Echo",
        rarity: "epic",
        color: "\u00A7d",
        description: "A golden-green totem echo",
        sound: "minecraft:item.totem.use",
        particle: "minecraft:totem_of_undying",
        pattern: "bloom",
        radius: 1.75,
        points: 36,
        yOffset: 0.45,
        speed: 0.08,
        count: 2,
        volume: 0.7,
        pitch: 1.65
    },
    {
        id: "meteor_fall",
        name: "Meteor Fall",
        rarity: "legendary",
        color: "\u00A76",
        description: "A meteor drops onto the killed entity",
        sound: "minecraft:entity.generic.explode",
        particle: "minecraft:flame",
        pattern: "meteor",
        radius: 2.25,
        points: 28,
        yOffset: 0.15,
        height: 7,
        speed: 0.02,
        count: 2,
        volume: 1,
        pitch: 0.8
    },
    {
        id: "void_rift",
        name: "Void Rift",
        rarity: "legendary",
        color: "\u00A76",
        description: "A lingering void tear opens and collapses",
        sound: "minecraft:entity.enderman.teleport",
        particle: "minecraft:reverse_portal",
        pattern: "rift",
        radius: 2.2,
        points: 58,
        yOffset: 0.4,
        height: 3,
        speed: 0.08,
        count: 2,
        volume: 1,
        pitch: 0.75
    },
    {
        id: "celestial_judgement",
        name: "Celestial Judgement",
        rarity: "legendary",
        color: "\u00A76",
        description: "A beam descends into a radiant sigil",
        sound: "minecraft:item.trident.thunder",
        particle: "minecraft:end_rod",
        pattern: "judgement",
        radius: 2.4,
        points: 64,
        yOffset: 0.1,
        height: 6,
        speed: 0.02,
        count: 2,
        volume: 1,
        pitch: 0.9
    },
    {
        id: "phoenix_ascension",
        name: "Phoenix Ascension",
        rarity: "legendary",
        color: "\u00A76",
        description: "Fire wings rise from the kill",
        sound: "minecraft:entity.blaze.shoot",
        particle: "minecraft:flame",
        pattern: "phoenix",
        radius: 2.2,
        points: 62,
        yOffset: 0.25,
        height: 2.8,
        speed: 0.03,
        count: 2,
        volume: 1,
        pitch: 0.8
    },
    {
        id: "galaxy_collapse",
        name: "Galaxy Collapse",
        rarity: "legendary",
        color: "\u00A76",
        description: "A tiny galaxy spins then collapses",
        sound: "minecraft:block.respawn_anchor.deplete",
        particle: "minecraft:enchant",
        pattern: "galaxy",
        radius: 2.45,
        points: 72,
        yOffset: 0.8,
        height: 1.8,
        speed: 0.04,
        count: 2,
        volume: 1,
        pitch: 0.85
    },
    {
        id: "soul_reaper",
        name: "Soul Reaper",
        rarity: "legendary",
        color: "\u00A76",
        description: "Souls orbit, rise, and vanish",
        sound: "minecraft:entity.warden.heartbeat",
        particle: "minecraft:soul",
        pattern: "reaper",
        radius: 2.05,
        points: 64,
        yOffset: 0.35,
        height: 3.4,
        speed: 0.015,
        count: 2,
        volume: 1,
        pitch: 0.7
    },
    {
        id: "soul_harvest",
        name: "Soul Harvest",
        rarity: "legendary",
        color: "\u00A76",
        description: "A harvested soul flies from the kill into the player",
        sound: "minecraft:block.soul_sand.break",
        particle: "minecraft:soul",
        pattern: "soul_harvest",
        radius: 1.35,
        points: 26,
        yOffset: 1,
        height: 2.6,
        speed: 0.01,
        count: 2,
        volume: 1,
        pitch: 0.85
    }
]

function killEffectTrim(value) {
    return String(value || "").replace(/^\s+|\s+$/g, "")
}

function killEffectLower(value) {
    return killEffectTrim(value).toLowerCase()
}

function killEffectMsg(player, message) {
    player.message("\u00A78[\u00A76KillFX\u00A78]\u00A7r " + message)
}

function killEffectAdminMsg(player, message) {
    player.message("\u00A78[\u00A7cKillFX Admin\u00A78]\u00A7r " + message)
}

function killEffectRunPlayerCommand(player, command) {
    API.executeCommand(player.getWorld(), command)
}

function killEffectShowMainActionbar(player) {
    killEffectRunPlayerCommand(player, "title " + player.getName() + " actionbar {\"text\":\"\",\"extra\":[{\"text\":\"!showmenu\",\"color\":\"aqua\",\"bold\":true},{\"text\":\" - \",\"color\":\"dark_gray\"},{\"text\":\"to see \",\"color\":\"gray\"},{\"text\":\"main menu\",\"color\":\"gold\",\"bold\":true}]}")
}

function killEffectBuildMainMenu(player) {
    var gui = API.createCustomGui(KILL_EFFECT_MAIN_MENU_GUI, 322, 206, false, player)

    killEffectBuildMainMenuFrame(gui)

    gui.addLabel(100, "\u00A76CyberCraft 2069", 104, 13, 140, 14, 0xFFAA00)
    gui.addLabel(101, "\u00A78Network Menu", 120, 29, 96, 12, 0xAAAAAA)
    gui.addLabel(102, "\u00A7bProgression", 32, 54, 92, 12, 0x55FFFF)
    gui.addLabel(103, "\u00A77Quests, guilds, server goals", 32, 68, 120, 12, 0xAAAAAA)
    gui.addLabel(104, "\u00A7dAccount & Style", 178, 54, 110, 12, 0xFF55FF)
    gui.addLabel(105, "\u00A77Ranks, effects, cosmetics", 178, 68, 120, 12, 0xAAAAAA)
    gui.addLabel(106, "\u00A78Choose a category", 112, 181, 104, 12, 0xAAAAAA)

    gui.addButton(KILL_EFFECT_MAIN_MENU_BTN_CLOSE, "X", 290, 12, 20, 20)
    gui.addButton(KILL_EFFECT_MAIN_MENU_BTN_QUESTS, "\u00A7bQuests", 34, 94, 112, 22)
    gui.addButton(KILL_EFFECT_MAIN_MENU_BTN_GUILDS, "\u00A73Guilds", 34, 124, 112, 22)
    gui.addButton(KILL_EFFECT_MAIN_MENU_BTN_RANKS, "\u00A76Ranks", 178, 94, 112, 22)
    gui.addButton(KILL_EFFECT_MAIN_MENU_BTN_KILL_EFFECTS, "\u00A7dKill Effects", 178, 124, 112, 22)
    gui.addButton(KILL_EFFECT_MAIN_MENU_BTN_COSMETICS, "\u00A75Cosmetics", 178, 154, 112, 22)

    player.showCustomGui(gui)
}

function killEffectBuildMainMenuFrame(gui) {
    gui.addTexturedRect(10, "minecraft:textures/block/black_concrete.png", 0, 0, 322, 206)
    gui.addTexturedRect(11, "minecraft:textures/block/cyan_concrete.png", 2, 2, 318, 202)
    gui.addTexturedRect(12, "minecraft:textures/block/gray_concrete.png", 5, 5, 312, 196)
    gui.addTexturedRect(13, "minecraft:textures/block/black_concrete.png", 9, 9, 304, 188)
    gui.addTexturedRect(14, "minecraft:textures/block/blue_terracotta.png", 12, 12, 298, 182)
    gui.addTexturedRect(15, "minecraft:textures/block/black_concrete.png", 18, 44, 286, 1)
    gui.addTexturedRect(16, "minecraft:textures/block/black_concrete.png", 160, 48, 1, 126)
    gui.addTexturedRect(17, "minecraft:textures/block/light_blue_concrete.png", 24, 48, 130, 130)
    gui.addTexturedRect(18, "minecraft:textures/block/gray_concrete.png", 28, 52, 122, 122)
    gui.addTexturedRect(19, "minecraft:textures/block/purple_concrete.png", 168, 48, 130, 130)
    gui.addTexturedRect(20, "minecraft:textures/block/gray_concrete.png", 172, 52, 122, 122)
    gui.addTexturedRect(21, "minecraft:textures/block/black_concrete.png", 28, 86, 122, 1)
    gui.addTexturedRect(22, "minecraft:textures/block/black_concrete.png", 172, 86, 122, 1)
    gui.addTexturedRect(23, "minecraft:textures/block/stone_bricks.png", 26, 182, 270, 12)
}

function killEffectSendRanksLink(player) {
    killEffectRunPlayerCommand(player, "tellraw " + player.getName() + " {\"text\":\"\",\"extra\":[{\"text\":\"Ranks: \",\"color\":\"gold\",\"bold\":true},{\"text\":\"Click here to view CyberCraft 2069 ranks\",\"color\":\"aqua\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://www.cybercraft2069.world/#ranks\"}}]}")
}

function killEffectSendComingSoon(player, feature) {
    player.message("\u00A78[\u00A76CyberCraft\u00A78]\u00A7r \u00A7e" + feature + "\u00A77 is coming soon.")
}

function killEffectBuildQuestMenu(player) {
    var ui = killEffectGetUi(player.getName())
    var questData = cyberQuestGetList(player)
    var selectedQuest
    var details
    var detailArea
    var gui = API.createCustomGui(KILL_EFFECT_QUEST_MENU_GUI, 420, 228, false, player)

    killEffectBuildQuestMenuFrame(gui)

    ui.questIds = questData.ids
    if (ui.questIndex < 0) ui.questIndex = 0
    if (ui.questIndex >= ui.questIds.length) ui.questIndex = ui.questIds.length - 1
    if (ui.questIndex < 0) ui.questIndex = 0

    selectedQuest = ui.questIds.length > 0 ? cyberQuestGetById(ui.questIds[ui.questIndex]) : null
    details = cyberQuestBuildDetails(player, selectedQuest)

    gui.addLabel(100, "\u00A7eQuest Journal", 24, 16, 110, 14, 0xFFFF55)
    gui.addLabel(101, "\u00A78Actual CustomNPCs quests", 24, 32, 140, 12, 0xAAAAAA)
    gui.addLabel(102, "\u00A76Quest Details", 180, 16, 110, 14, 0xFFAA00)
    gui.addLabel(103, "\u00A78Selected quest progress and notes", 180, 32, 180, 12, 0xAAAAAA)

    gui.addScroll(CYBER_QUEST_SCROLL, 24, 54, 138, 126, questData.labels.length > 0 ? questData.labels : ["\u00A78No quests found"]).setDefaultSelection(ui.questIndex)
    detailArea = gui.addTextArea(CYBER_QUEST_DETAILS, 180, 54, 210, 126)
    detailArea.setText(details)
    detailArea.setEnabled(false)

    gui.addButton(CYBER_QUEST_BTN_BACK, "Back", 24, 190, 64, 20)
    gui.addButton(KILL_EFFECT_QUEST_MENU_BTN_CLOSE, "X", 388, 12, 20, 20)

    player.showCustomGui(gui)
}

function killEffectBuildQuestMenuFrame(gui) {
    killEffectAddInsideOutline(gui, 10, 8, 8, 404, 212, "minecraft:textures/block/yellow_concrete.png")
    killEffectAddInsideOutline(gui, 20, 18, 48, 150, 136, "minecraft:textures/block/orange_terracotta.png")
    killEffectAddInsideOutline(gui, 30, 174, 48, 222, 136, "minecraft:textures/block/orange_terracotta.png")
    gui.addTexturedRect(40, "minecraft:textures/block/black_concrete.png", 18, 46, 384, 1)
    gui.addTexturedRect(41, "minecraft:textures/block/black_concrete.png", 170, 48, 1, 136)
    gui.addTexturedRect(42, "minecraft:textures/block/black_concrete.png", 20, 188, 374, 1)
}

function cyberQuestGetList(player) {
    var labels = []
    var ids = []
    var seen = {}
    var activeQuests
    var i

    try {
        activeQuests = player.getActiveQuests()
        for (i = 0; i < activeQuests.length; i++) {
            cyberQuestPushListQuest(player, activeQuests[i], labels, ids, seen)
        }
    } catch (err) {
    }

    return { ids: ids, labels: labels }
}

function cyberQuestPushListQuest(player, quest, labels, ids, seen) {
    var id

    if (!quest) return
    id = "" + quest.getId()
    if (seen[id]) return
    seen[id] = true
    ids.push(id)
    labels.push(cyberQuestBuildListLabel(player, quest))
}

function cyberQuestGetById(id) {
    try {
        return API.getQuests().get(parseInt(id, 10))
    } catch (err) {
        return null
    }
}

function cyberQuestBuildListLabel(player, quest) {
    var status = cyberQuestStatus(player, quest.getId())
    var prefix = "\u00A77"

    if (status === "Active") prefix = "\u00A7a"
    if (status === "Available") prefix = "\u00A7e"
    if (status === "Completed") prefix = "\u00A7b"

    return prefix + cyberQuestShortText(quest.getName(), 22) + " \u00A78#" + quest.getId()
}

function cyberQuestBuildDetails(player, quest) {
    var text
    var nextQuest
    var objectives

    if (!quest) {
        return "No active quests.\n\nAccept a quest from an NPC and it will appear here."
    }

    text = "Name: " + quest.getName() + "\n"
    text = text + "Quest ID: " + quest.getId() + "\n"
    text = text + "Status: " + cyberQuestStatus(player, quest.getId()) + "\n"
    text = text + "Type: " + quest.getType() + "\n"
    text = text + "Repeatable: " + (quest.getIsRepeatable() ? "Yes" : "No") + "\n"

    try {
        nextQuest = quest.getNextQuest()
        if (nextQuest) text = text + "Next: " + nextQuest.getName() + " (#" + nextQuest.getId() + ")\n"
    } catch (err) {
    }

    objectives = cyberQuestObjectiveText(player, quest)
    text = text + "\nProgress:\n" + (objectives ? objectives : "No active objective data for this player") + "\n"
    text = text + "\nDescription:\n" + cyberQuestCleanText(quest.getLogText(), "No quest log text") + "\n"
    text = text + "\nCompletion:\n" + cyberQuestCleanText(quest.getCompleteText(), "No completion text")

    return text
}

function cyberQuestStatus(player, questId) {
    try {
        if (player.hasActiveQuest(questId)) return "Active"
    } catch (err) {
    }

    try {
        if (player.hasFinishedQuest(questId)) return "Completed"
    } catch (err2) {
    }

    try {
        if (player.canQuestBeAccepted(questId)) return "Available"
    } catch (err3) {
    }

    return "Locked"
}

function cyberQuestObjectiveText(player, quest) {
    var objectives

    try {
        objectives = quest.getObjectives(player)
    } catch (err) {
        return ""
    }

    if (!objectives || ("" + objectives) === "null") return ""
    return cyberQuestCleanText("" + objectives, "")
}

function cyberQuestCleanText(value, fallback) {
    var text = killEffectTrim(value)

    if (!text || text === "null") return fallback
    return text.replace(/\r/g, "").replace(/\n\n\n+/g, "\n\n")
}

function cyberQuestShortText(value, maxLength) {
    var text = cyberQuestCleanText(value, "Unnamed Quest")

    if (text.length <= maxLength) return text
    return text.substring(0, maxLength - 3) + "..."
}

function killEffectDelay(ms) {
    Thread.sleep(ms)
}

function killEffectRunAsync(fn) {
    if (KILL_EFFECT_ACTIVE_THREADS >= KILL_EFFECT_MAX_THREADS) return false
    KILL_EFFECT_ACTIVE_THREADS++
    new (Java.extend(Thread, {
        run: function() {
            try {
                fn()
            } catch (err) {
            } finally {
                KILL_EFFECT_ACTIVE_THREADS--
                if (KILL_EFFECT_ACTIVE_THREADS < 0) KILL_EFFECT_ACTIVE_THREADS = 0
            }
        }
    }))().start()
    return true
}

function killEffectIsAdmin(player) {
    try {
        return player.getGamemode() === 1
    } catch (err) {
        return false
    }
}

function killEffectGetUi(playerName) {
    if (!KILL_EFFECT_UI[playerName]) {
        KILL_EFFECT_UI[playerName] = {
            playerIndex: 0,
            effectIndex: 0,
            playerNames: [],
            effectIds: [],
            ownEffectIndex: 0,
            ownEffectIds: [],
            questIndex: 0,
            questIds: []
        }
    }
    return KILL_EFFECT_UI[playerName]
}

function killEffectLoadData(player) {
    var raw = "" + player.getStoreddata().get(KILL_EFFECT_KEY)
    var data

    if (!raw || raw === "null") {
        return { unlocked: [], active: "" }
    }

    try {
        data = JSON.parse(raw)
    } catch (err) {
        data = { unlocked: [], active: "" }
    }

    return killEffectSanitizeData(data)
}

function killEffectSaveData(player, data) {
    player.getStoreddata().put(KILL_EFFECT_KEY, JSON.stringify(killEffectSanitizeData(data)))
}

function killEffectSanitizeData(data) {
    var clean = { unlocked: [], active: "" }
    var seen = {}
    var i
    var id

    if (!data || typeof data !== "object") return clean
    if (data.unlocked && data.unlocked.length !== undefined) {
        for (i = 0; i < data.unlocked.length; i++) {
            id = killEffectTrim(data.unlocked[i])
            if (!id || seen[id] || !killEffectById(id)) continue
            seen[id] = true
            clean.unlocked.push(id)
        }
    }

    if (data.active && killEffectContains(clean.unlocked, data.active)) {
        clean.active = "" + data.active
    }

    return clean
}

function killEffectContains(list, value) {
    var i

    if (!list || list.length === undefined) return false
    for (i = 0; i < list.length; i++) {
        if (String(list[i]) === String(value)) return true
    }
    return false
}

function killEffectById(id) {
    var i

    for (i = 0; i < KILL_EFFECTS.length; i++) {
        if (KILL_EFFECTS[i].id === id) return KILL_EFFECTS[i]
    }
    return null
}

function killEffectColor(effect) {
    if (effect && effect.color) return effect.color
    if (effect && effect.rarity === "rare") return "\u00A7b"
    if (effect && effect.rarity === "epic") return "\u00A7d"
    if (effect && effect.rarity === "legendary") return "\u00A76"
    return "\u00A7f"
}

function killEffectColoredName(effect) {
    if (!effect) return "\u00A7fUnknown"
    return killEffectColor(effect) + effect.name
}

function killEffectRarityLabel(effect) {
    if (!effect || !effect.rarity) return "\u00A7fCommon"
    if (effect.rarity === "rare") return "\u00A7bRare"
    if (effect.rarity === "epic") return "\u00A7dEpic"
    if (effect.rarity === "legendary") return "\u00A76Legendary"
    return "\u00A7fCommon"
}

function killEffectGetOnlinePlayers(world) {
    var rawPlayers = world.getAllPlayers()
    var players = []
    var i

    for (i = 0; i < rawPlayers.length; i++) {
        players.push(rawPlayers[i])
    }

    players.sort(function(a, b) {
        return killEffectLower(a.getName()) > killEffectLower(b.getName()) ? 1 : -1
    })

    return players
}

function killEffectFindOnlinePlayer(world, name) {
    var players = killEffectGetOnlinePlayers(world)
    var lowerName = killEffectLower(name)
    var i

    for (i = 0; i < players.length; i++) {
        if (killEffectLower(players[i].getName()) === lowerName) return players[i]
    }

    return null
}

function killEffectGrant(player, effectId) {
    var data = killEffectLoadData(player)

    if (!killEffectById(effectId)) return false
    if (!killEffectContains(data.unlocked, effectId)) {
        data.unlocked.push(effectId)
    }
    if (!data.active) data.active = effectId

    killEffectSaveData(player, data)
    return true
}

function killEffectRevoke(player, effectId) {
    var data = killEffectLoadData(player)
    var next = []
    var changed = false
    var i

    for (i = 0; i < data.unlocked.length; i++) {
        if (data.unlocked[i] === effectId) {
            changed = true
            continue
        }
        next.push(data.unlocked[i])
    }

    if (!changed) return false
    data.unlocked = next
    if (data.active === effectId) data.active = data.unlocked.length > 0 ? data.unlocked[0] : ""
    killEffectSaveData(player, data)
    return true
}

function killEffectSetActive(player, effectId) {
    var data = killEffectLoadData(player)

    if (!killEffectContains(data.unlocked, effectId)) return false
    data.active = effectId
    killEffectSaveData(player, data)
    return true
}

function killEffectClearActive(player) {
    var data = killEffectLoadData(player)

    data.active = ""
    killEffectSaveData(player, data)
}

function killEffectDescribeData(player) {
    var data = killEffectLoadData(player)
    var active = data.active ? data.active : "none"

    killEffectMsg(player, "\u00A77Active: \u00A7e" + active + "\u00A77 | Unlocked: \u00A7f" + data.unlocked.length)
    if (data.active && !killEffectById(data.active)) {
        killEffectMsg(player, "\u00A7cActive effect id is no longer registered")
    }
}

function killEffectIsDebug(player) {
    return "" + player.getStoreddata().get(KILL_EFFECT_DEBUG_KEY) === "1"
}

function killEffectSetDebug(player, enabled) {
    player.getStoreddata().put(KILL_EFFECT_DEBUG_KEY, enabled ? "1" : "0")
}

function killEffectDebug(player, message) {
    if (killEffectIsDebug(player)) killEffectMsg(player, "\u00A78debug: \u00A77" + message)
}

function killEffectSelectedTarget(admin) {
    var ui = killEffectGetUi(admin.getName())
    var targetName = ui.playerNames[ui.playerIndex]

    if (!targetName) return null
    return killEffectFindOnlinePlayer(admin.getWorld(), targetName)
}

function killEffectSelectedEffect(admin) {
    var ui = killEffectGetUi(admin.getName())
    var effectId = ui.effectIds[ui.effectIndex]

    if (!effectId) return null
    return killEffectById(effectId)
}

function killEffectBuildAdminGui(admin, note) {
    var ui = killEffectGetUi(admin.getName())
    var world = admin.getWorld()
    var players = killEffectGetOnlinePlayers(world)
    var target
    var targetData
    var playerLabels = []
    var effectLabels = []
    var gui = API.createCustomGui(KILL_EFFECT_ADMIN_GUI, KILL_EFFECT_ADMIN_WIDTH, KILL_EFFECT_ADMIN_HEIGHT, false, admin)
    var i
    var effect
    var state

    ui.playerNames = []
    ui.effectIds = []

    for (i = 0; i < players.length; i++) {
        ui.playerNames.push(players[i].getName())
        playerLabels.push("\u00A7f" + players[i].getName())
    }

    if (ui.playerIndex < 0) ui.playerIndex = 0
    if (ui.playerIndex >= ui.playerNames.length) ui.playerIndex = ui.playerNames.length - 1
    if (ui.playerIndex < 0) ui.playerIndex = 0

    target = killEffectSelectedTarget(admin)
    targetData = target ? killEffectLoadData(target) : { unlocked: [], active: "" }

    for (i = 0; i < KILL_EFFECTS.length; i++) {
        effect = KILL_EFFECTS[i]
        ui.effectIds.push(effect.id)
        state = "\u00A78locked"
        if (killEffectContains(targetData.unlocked, effect.id)) state = "\u00A7aunlocked"
        if (targetData.active === effect.id) state = "\u00A7eactive"
        effectLabels.push(killEffectColoredName(effect) + " \u00A78(" + killEffectRarityLabel(effect) + "\u00A78) - " + state)
    }

    if (ui.effectIndex < 0) ui.effectIndex = 0
    if (ui.effectIndex >= ui.effectIds.length) ui.effectIndex = ui.effectIds.length - 1
    if (ui.effectIndex < 0) ui.effectIndex = 0

    gui.addLabel(100, "\u00A76Kill Effect Admin", 12, 8, 180, 14)
    gui.addLabel(101, "\u00A77Online players", 12, 28, 130, 12)
    gui.addLabel(102, "\u00A77Effects", 154, 28, 160, 12)
    gui.addLabel(103, target ? "\u00A7fTarget: \u00A7e" + target.getName() : "\u00A7cNo online players", 154, 144, 340, 12)
    gui.addLabel(104, note ? "\u00A7e" + note : "\u00A78Select player and effect", 12, 226, 440, 12)

    gui.addScroll(KILL_EFFECT_IDS.ADMIN_SCROLL_PLAYERS, 12, 42, 130, 98, playerLabels.length > 0 ? playerLabels : ["\u00A78No players online"]).setDefaultSelection(ui.playerIndex)
    gui.addScroll(KILL_EFFECT_IDS.ADMIN_SCROLL_EFFECTS, 154, 42, 350, 98, effectLabels.length > 0 ? effectLabels : ["\u00A78No effects registered"]).setDefaultSelection(ui.effectIndex)

    gui.addButton(KILL_EFFECT_IDS.ADMIN_BTN_GRANT, "Grant", 154, 164, 78, 22)
    gui.addButton(KILL_EFFECT_IDS.ADMIN_BTN_REVOKE, "Revoke", 238, 164, 78, 22)
    gui.addButton(KILL_EFFECT_IDS.ADMIN_BTN_SET_ACTIVE, "Set Active", 322, 164, 86, 22)
    gui.addButton(KILL_EFFECT_IDS.ADMIN_BTN_CLEAR_ACTIVE, "Clear", 154, 194, 78, 22)
    gui.addButton(KILL_EFFECT_IDS.ADMIN_BTN_PREVIEW, "Preview", 238, 194, 78, 22)
    gui.addButton(KILL_EFFECT_IDS.ADMIN_BTN_REFRESH, "Refresh", 322, 194, 86, 22)
    gui.addButton(KILL_EFFECT_IDS.ADMIN_BTN_CLOSE, "Close", 474, 8, 54, 20)

    admin.showCustomGui(gui)
}

function killEffectBuildPlayerGui(player, note) {
    var ui = killEffectGetUi(player.getName())
    var data = killEffectLoadData(player)
    var labels = []
    var gui = API.createCustomGui(KILL_EFFECT_PLAYER_GUI, KILL_EFFECT_PLAYER_WIDTH, KILL_EFFECT_PLAYER_HEIGHT, false, player)
    var i
    var effect
    var selectedEffect
    var suffix

    ui.ownEffectIds = []

    for (i = 0; i < data.unlocked.length; i++) {
        effect = killEffectById(data.unlocked[i])
        if (!effect) continue
        suffix = data.active === effect.id ? " \u00A7eactive" : ""
        ui.ownEffectIds.push(effect.id)
        labels.push(killEffectColoredName(effect) + " \u00A78(" + killEffectRarityLabel(effect) + "\u00A78)" + suffix)
    }

    if (ui.ownEffectIndex < 0) ui.ownEffectIndex = 0
    if (ui.ownEffectIndex >= ui.ownEffectIds.length) ui.ownEffectIndex = ui.ownEffectIds.length - 1
    if (ui.ownEffectIndex < 0) ui.ownEffectIndex = 0

    selectedEffect = ui.ownEffectIds.length > 0 ? killEffectById(ui.ownEffectIds[ui.ownEffectIndex]) : null

    killEffectBuildPlayerGuiFrame(gui)

    gui.addLabel(100, "\u00A7dKill Effects", 24, 14, 120, 14, 0xFF55FF)
    gui.addLabel(101, "\u00A78Unlocked effects", 24, 30, 110, 12, 0xAAAAAA)
    gui.addLabel(102, "\u00A76Selected Effect", 172, 54, 120, 12, 0xFFAA00)
    gui.addLabel(103, killEffectSelectedEffectName(selectedEffect), 172, 72, 124, 12, 0xFFFFFF)
    gui.addLabel(104, killEffectSelectedEffectRarity(selectedEffect), 172, 88, 124, 12, 0xAAAAAA)
    gui.addLabel(105, killEffectSelectedEffectStatus(data, selectedEffect), 172, 104, 124, 12, 0xAAAAAA)
    gui.addLabel(106, killEffectSelectedEffectDescription(selectedEffect), 172, 126, 124, 34, 0xAAAAAA)
    gui.addLabel(107, note ? "\u00A7e" + note : "\u00A78Choose one effect and set it active", 24, 220, 220, 12, 0xAAAAAA)

    gui.addScroll(KILL_EFFECT_IDS.PLAYER_SCROLL_EFFECTS, 24, 50, 132, 132, labels.length > 0 ? labels : ["\u00A78No kill effects unlocked"]).setDefaultSelection(ui.ownEffectIndex)
    gui.addButton(KILL_EFFECT_IDS.PLAYER_BTN_SET_ACTIVE, "\u00A7aSet Active", 24, 190, 82, 20)
    gui.addButton(KILL_EFFECT_IDS.PLAYER_BTN_PREVIEW, "\u00A7ePreview", 112, 190, 70, 20)
    gui.addButton(KILL_EFFECT_IDS.PLAYER_BTN_CLEAR_ACTIVE, "\u00A7cClear", 188, 190, 54, 20)
    gui.addButton(KILL_EFFECT_IDS.PLAYER_BTN_CLOSE, "X", 288, 12, 20, 20)

    player.showCustomGui(gui)
}

function killEffectBuildPlayerGuiFrame(gui) {
    killEffectAddInsideOutline(gui, 10, 8, 8, KILL_EFFECT_PLAYER_WIDTH - 16, KILL_EFFECT_PLAYER_HEIGHT - 16, "minecraft:textures/block/purple_concrete.png")
    killEffectAddInsideOutline(gui, 20, 20, 48, 140, 138, "minecraft:textures/block/purple_terracotta.png")
    killEffectAddInsideOutline(gui, 30, 168, 48, 128, 138, "minecraft:textures/block/purple_terracotta.png")
    gui.addTexturedRect(80, "minecraft:textures/block/black_concrete.png", 18, 44, KILL_EFFECT_PLAYER_WIDTH - 36, 1)
    gui.addTexturedRect(81, "minecraft:textures/block/black_concrete.png", 164, 48, 1, 134)
    gui.addTexturedRect(82, "minecraft:textures/block/black_concrete.png", 172, 118, 120, 1)
    gui.addTexturedRect(83, "minecraft:textures/block/black_concrete.png", 20, 216, 260, 1)
}

function killEffectAddInsideOutline(gui, idStart, x, y, width, height, texture) {
    gui.addTexturedRect(idStart, texture, x, y, width, 2)
    gui.addTexturedRect(idStart + 1, texture, x, y + height - 2, width, 2)
    gui.addTexturedRect(idStart + 2, texture, x, y, 2, height)
    gui.addTexturedRect(idStart + 3, texture, x + width - 2, y, 2, height)
}

function killEffectSelectedEffectName(effect) {
    if (!effect) return "\u00A78None selected"
    return killEffectColoredName(effect)
}

function killEffectSelectedEffectRarity(effect) {
    if (!effect) return "\u00A78Rarity: none"
    return "\u00A77Rarity: " + killEffectRarityLabel(effect)
}

function killEffectSelectedEffectStatus(data, effect) {
    if (!effect) return "\u00A78Status: locked"
    if (data.active === effect.id) return "\u00A7aStatus: active"
    return "\u00A77Status: unlocked"
}

function killEffectSelectedEffectDescription(effect) {
    if (!effect) return "\u00A78Unlock effects to view them here."
    return "\u00A77" + killEffectShortText(effect.description || "No description", 54)
}

function killEffectShortText(value, maxLength) {
    var text = killEffectTrim(value)

    if (text.length <= maxLength) return text
    return text.substring(0, maxLength - 3) + "..."
}

function killEffectParticleName(effect) {
    if (effect.particle === "dust") {
        return "dust " + effect.dustRed + " " + effect.dustGreen + " " + effect.dustBlue + " " + effect.dustScale
    }
    return effect.particle
}

function killEffectDirectParticleName(effect) {
    if (effect.particle.indexOf("minecraft:") === 0) {
        return effect.particle.substring("minecraft:".length)
    }
    return effect.particle
}

function killEffectNumber(value) {
    var num = parseFloat(value)

    if (isNaN(num)) num = 0
    return num.toFixed(3)
}

function killEffectCommandParticle(world, effect, x, y, z) {
    var cmd = "/particle " + killEffectParticleName(effect) + " " +
        killEffectNumber(x) + " " + killEffectNumber(y) + " " + killEffectNumber(z) + " " +
        "0 0 0 " + (effect.speed || 0) + " " + (effect.count || 1) + " force"

    return API.executeCommand(world, cmd)
}

function killEffectParticleStub(particle, speed, count) {
    return {
        particle: particle,
        speed: speed || 0,
        count: count || 1
    }
}

function killEffectCommandSound(world, effect, x, y, z) {
    var cmd = "/execute positioned " + killEffectNumber(x) + " " + killEffectNumber(y) + " " + killEffectNumber(z) +
        " run playsound " + effect.sound + " master @a[distance=..32] " +
        killEffectNumber(x) + " " + killEffectNumber(y) + " " + killEffectNumber(z) + " " +
        (effect.volume || 0.8) + " " + (effect.pitch || 1)

    return API.executeCommand(world, cmd)
}

function killEffectPlaySound(world, effect, x, y, z) {
    try {
        world.playSoundAt(API.getIPos(x, y, z), effect.sound, effect.volume || 0.8, effect.pitch || 1)
        return "direct"
    } catch (err) {
        return "" + killEffectCommandSound(world, effect, x, y, z)
    }
}

function killEffectSpawnParticle(world, effect, x, y, z) {
    if (effect.particle !== "dust") {
        try {
            world.spawnParticle(killEffectDirectParticleName(effect), x, y, z, 0, 0, 0, effect.speed || 0, effect.count || 1)
            return "direct"
        } catch (err) {
            try {
                return "direct failed: " + err + " | cmd: " + killEffectCommandParticle(world, effect, x, y, z)
            } catch (cmdErr) {
                return "direct failed: " + err + " | cmd failed: " + cmdErr
            }
        }
    }

    try {
        return "" + killEffectCommandParticle(world, effect, x, y, z)
    } catch (cmdErr) {
        return "cmd failed: " + cmdErr
    }
}

function killEffectSafeX(entity) {
    try { return entity.getX() } catch (err) { return 0 }
}

function killEffectSafeY(entity) {
    try { return entity.getY() } catch (err) { return 0 }
}

function killEffectSafeZ(entity) {
    try { return entity.getZ() } catch (err) { return 0 }
}

function killEffectSafeRotation(entity) {
    try { return entity.getRotation() } catch (err) { return 0 }
}

function killEffectSafePitch(entity) {
    try { return entity.getPitch() } catch (err) { return 0 }
}

function KillEffectFrontVectors(entity, dr, dp, distance, mode) {
    var angle
    var pitch

    if (!mode) mode = 0
    if (mode === 1) {
        angle = dr + killEffectSafeRotation(entity)
        pitch = (-killEffectSafePitch(entity) + dp) * Math.PI / 180
    } else {
        angle = dr
        pitch = dp * Math.PI / 180
    }

    return [
        -Math.sin(angle * Math.PI / 180) * (distance * Math.cos(pitch)),
        Math.sin(pitch) * distance,
        Math.cos(angle * Math.PI / 180) * (distance * Math.cos(pitch))
    ]
}

function KillEffectCross(a, b) {
    return [
        a[1] * b[2] - a[2] * b[1],
        a[2] * b[0] - a[0] * b[2],
        a[0] * b[1] - a[1] * b[0]
    ]
}

function KillEffectMakeNewAxis(entity, rotation, pitch) {
    var a = KillEffectFrontVectors(entity, rotation, pitch, 1, 0)
    var b = KillEffectFrontVectors(entity, rotation, pitch - 90, 1, 0)

    return [b, a, KillEffectCross(b, a)]
}

function KillEffectTransformVector(axis, vector) {
    return [
        axis[0][0] * -vector[0] + axis[1][0] * vector[1] + axis[2][0] * vector[2],
        axis[0][1] * -vector[0] + axis[1][1] * vector[1] + axis[2][1] * vector[2],
        axis[0][2] * -vector[0] + axis[1][2] * vector[1] + axis[2][2] * vector[2]
    ]
}

function KillEffectFrontVectorsInPlane(newYRot, newYPitch, entity, dr, dp, distance) {
    var axis = KillEffectMakeNewAxis(entity, newYRot, newYPitch)
    var vector = KillEffectFrontVectors(entity, dr, dp, distance, 0)

    return KillEffectTransformVector(axis, vector)
}

function killEffectPlayPattern(world, entity, effect) {
    var baseX = killEffectSafeX(entity)
    var baseY = killEffectSafeY(entity) + (effect.yOffset || 0)
    var baseZ = killEffectSafeZ(entity)
    var points = effect.points || 24
    var radius = effect.radius || 1
    var height = effect.height || 1.8
    var i
    var angle
    var vector
    var y
    var firstResult = ""
    var t
    var flameEffect
    var smokeEffect
    var impactEffect
    var secondaryEffect
    var goldEffect
    var portalEffect
    var soulEffect
    var sparkEffect
    var trailVector
    var layer
    var wing
    var side

    if (effect.pattern === "spiral") {
        for (i = 0; i < points; i++) {
            angle = i * 42
            y = baseY + height * (i / points)
            vector = KillEffectFrontVectorsInPlane(0, 90, entity, angle, 0, radius * (1 - (i / points) * 0.35))
            if (!firstResult) firstResult = killEffectSpawnParticle(world, effect, baseX + vector[0], y + vector[1], baseZ + vector[2])
            else killEffectSpawnParticle(world, effect, baseX + vector[0], y + vector[1], baseZ + vector[2])
        }
        return firstResult
    }

    if (effect.pattern === "pillar") {
        for (i = 0; i < points; i++) {
            t = points <= 1 ? 1 : i / (points - 1)
            angle = i * 137
            vector = KillEffectFrontVectorsInPlane(0, 90, entity, angle, 0, radius * (0.35 + (i % 4) * 0.18))
            y = baseY + height * t
            if (!firstResult) firstResult = killEffectSpawnParticle(world, effect, baseX + vector[0], y, baseZ + vector[2])
            else killEffectSpawnParticle(world, effect, baseX + vector[0], y, baseZ + vector[2])
        }
        return firstResult
    }

    if (effect.pattern === "bloom") {
        for (i = 0; i < points; i++) {
            angle = i * (360 / points)
            vector = KillEffectFrontVectorsInPlane(0, 90, entity, angle, 0, radius * (0.4 + (i % 3) * 0.28))
            y = baseY + Math.sin(i * 0.7) * 0.25
            if (!firstResult) firstResult = killEffectSpawnParticle(world, effect, baseX + vector[0], y, baseZ + vector[2])
            else killEffectSpawnParticle(world, effect, baseX + vector[0], y, baseZ + vector[2])
        }
        return firstResult
    }

    if (effect.pattern === "nova") {
        for (i = 0; i < points; i++) {
            angle = i * (360 / points)
            vector = KillEffectFrontVectorsInPlane(killEffectSafeRotation(entity), -35 + (i % 7) * 12, entity, angle, 0, radius)
            if (!firstResult) firstResult = killEffectSpawnParticle(world, effect, baseX + vector[0], baseY + 0.45 + vector[1], baseZ + vector[2])
            else killEffectSpawnParticle(world, effect, baseX + vector[0], baseY + 0.45 + vector[1], baseZ + vector[2])
        }
        return firstResult
    }

    if (effect.pattern === "rune") {
        for (i = 0; i < points; i++) {
            angle = i * (360 / points)
            radius = i % 2 === 0 ? effect.radius || 1.4 : (effect.radius || 1.4) * 0.55
            vector = KillEffectFrontVectorsInPlane(0, 90, entity, angle, 0, radius)
            if (!firstResult) firstResult = killEffectSpawnParticle(world, effect, baseX + vector[0], baseY, baseZ + vector[2])
            else killEffectSpawnParticle(world, effect, baseX + vector[0], baseY, baseZ + vector[2])
        }
        return firstResult
    }

    if (effect.pattern === "helix") {
        for (i = 0; i < points; i++) {
            t = points <= 1 ? 1 : i / (points - 1)
            angle = i * 38
            y = baseY + height * t
            vector = KillEffectFrontVectorsInPlane(0, 90, entity, angle, 0, radius)
            if (!firstResult) firstResult = killEffectSpawnParticle(world, effect, baseX + vector[0], y, baseZ + vector[2])
            else killEffectSpawnParticle(world, effect, baseX + vector[0], y, baseZ + vector[2])
            vector = KillEffectFrontVectorsInPlane(0, 90, entity, angle + 180, 0, radius * 0.82)
            killEffectSpawnParticle(world, effect, baseX + vector[0], y + 0.06, baseZ + vector[2])
        }
        return firstResult
    }

    if (effect.pattern === "vortex") {
        for (i = 0; i < points; i++) {
            t = points <= 1 ? 1 : i / (points - 1)
            angle = i * 32
            vector = KillEffectFrontVectorsInPlane(0, 90, entity, angle, 0, radius * (1 - t * 0.75))
            y = baseY + height * t
            if (!firstResult) firstResult = killEffectSpawnParticle(world, effect, baseX + vector[0], y, baseZ + vector[2])
            else killEffectSpawnParticle(world, effect, baseX + vector[0], y, baseZ + vector[2])
        }
        return firstResult
    }

    if (effect.pattern === "crown") {
        for (i = 0; i < points; i++) {
            angle = i * (360 / points)
            vector = KillEffectFrontVectorsInPlane(0, 90, entity, angle, 0, radius)
            y = baseY + ((i % 4 === 0) ? height : 0)
            if (!firstResult) firstResult = killEffectSpawnParticle(world, effect, baseX + vector[0], y, baseZ + vector[2])
            else killEffectSpawnParticle(world, effect, baseX + vector[0], y, baseZ + vector[2])
        }
        return firstResult
    }

    if (effect.pattern === "meteor") {
        flameEffect = killEffectParticleStub("minecraft:flame", 0.02, 3)
        smokeEffect = killEffectParticleStub("minecraft:smoke", 0.02, 2)
        impactEffect = killEffectParticleStub("minecraft:lava", 0.12, 2)

        for (i = 0; i < points; i++) {
            t = points <= 1 ? 1 : i / (points - 1)
            trailVector = KillEffectFrontVectorsInPlane(killEffectSafeRotation(entity) + 30, 62, entity, 0, 0, radius * (1 - t))
            y = baseY + height * (1 - t)
            if (!firstResult) firstResult = killEffectSpawnParticle(world, flameEffect, baseX + trailVector[0], y, baseZ + trailVector[2])
            else killEffectSpawnParticle(world, flameEffect, baseX + trailVector[0], y, baseZ + trailVector[2])
            if (i % 2 === 0) killEffectSpawnParticle(world, smokeEffect, baseX + trailVector[0] * 0.85, y + 0.08, baseZ + trailVector[2] * 0.85)
        }

        for (i = 0; i < points; i++) {
            angle = i * (360 / points)
            vector = KillEffectFrontVectorsInPlane(0, 90, entity, angle, 0, radius * 0.45)
            killEffectSpawnParticle(world, impactEffect, baseX + vector[0], baseY + 0.15, baseZ + vector[2])
            if (i % 3 === 0) killEffectSpawnParticle(world, smokeEffect, baseX + vector[0] * 0.7, baseY + 0.35, baseZ + vector[2] * 0.7)
        }

        return firstResult
    }

    if (effect.pattern === "rift") {
        portalEffect = killEffectParticleStub("minecraft:reverse_portal", 0.08, 2)
        soulEffect = killEffectParticleStub("minecraft:dragon_breath", 0.02, 1)
        for (i = 0; i < points; i++) {
            t = points <= 1 ? 1 : i / (points - 1)
            angle = i * 31
            vector = KillEffectFrontVectorsInPlane(killEffectSafeRotation(entity), 8 + Math.sin(i) * 20, entity, angle, 0, radius * (0.3 + Math.sin(t * Math.PI) * 0.7))
            y = baseY + height * t
            if (!firstResult) firstResult = killEffectSpawnParticle(world, portalEffect, baseX + vector[0] * 0.45, y, baseZ + vector[2] * 0.45)
            else killEffectSpawnParticle(world, portalEffect, baseX + vector[0] * 0.45, y, baseZ + vector[2] * 0.45)
            if (i % 3 === 0) killEffectSpawnParticle(world, soulEffect, baseX + vector[0], y, baseZ + vector[2])
        }
        return firstResult
    }

    if (effect.pattern === "judgement") {
        goldEffect = killEffectParticleStub("minecraft:end_rod", 0.01, 2)
        sparkEffect = killEffectParticleStub("minecraft:electric_spark", 0.06, 1)
        for (i = 0; i < points; i++) {
            t = points <= 1 ? 1 : i / (points - 1)
            y = baseY + height * t
            if (!firstResult) firstResult = killEffectSpawnParticle(world, goldEffect, baseX, y, baseZ)
            else killEffectSpawnParticle(world, goldEffect, baseX, y, baseZ)
            if (i % 4 === 0) {
                angle = i * 47
                vector = KillEffectFrontVectorsInPlane(0, 90, entity, angle, 0, radius * (1 - t * 0.45))
                killEffectSpawnParticle(world, sparkEffect, baseX + vector[0], baseY + 0.2, baseZ + vector[2])
            }
        }
        for (i = 0; i < points; i++) {
            angle = i * (360 / points)
            vector = KillEffectFrontVectorsInPlane(0, 90, entity, angle, 0, radius)
            killEffectSpawnParticle(world, goldEffect, baseX + vector[0], baseY + 0.1, baseZ + vector[2])
        }
        return firstResult
    }

    if (effect.pattern === "phoenix") {
        flameEffect = killEffectParticleStub("minecraft:flame", 0.035, 2)
        smokeEffect = killEffectParticleStub("minecraft:lava", 0.08, 1)
        for (i = 0; i < points; i++) {
            t = points <= 1 ? 1 : i / (points - 1)
            side = i % 2 === 0 ? 1 : -1
            wing = Math.sin(t * Math.PI)
            vector = KillEffectFrontVectorsInPlane(killEffectSafeRotation(entity), 20 + side * 32, entity, side * 65, 0, radius * wing)
            y = baseY + height * t
            if (!firstResult) firstResult = killEffectSpawnParticle(world, flameEffect, baseX + vector[0], y, baseZ + vector[2])
            else killEffectSpawnParticle(world, flameEffect, baseX + vector[0], y, baseZ + vector[2])
            if (i % 5 === 0) killEffectSpawnParticle(world, smokeEffect, baseX + vector[0] * 0.45, y - 0.2, baseZ + vector[2] * 0.45)
        }
        return firstResult
    }

    if (effect.pattern === "galaxy") {
        portalEffect = killEffectParticleStub("minecraft:portal", 0.06, 1)
        goldEffect = killEffectParticleStub("minecraft:end_rod", 0.02, 1)
        for (i = 0; i < points; i++) {
            t = points <= 1 ? 1 : i / (points - 1)
            layer = i % 3
            angle = i * 29 + layer * 120
            vector = KillEffectFrontVectorsInPlane(0, 90, entity, angle, 0, radius * (1 - t * 0.55))
            y = baseY + Math.sin(t * Math.PI * 2) * height * 0.35 + layer * 0.18
            if (!firstResult) firstResult = killEffectSpawnParticle(world, portalEffect, baseX + vector[0], y, baseZ + vector[2])
            else killEffectSpawnParticle(world, portalEffect, baseX + vector[0], y, baseZ + vector[2])
            if (i % 6 === 0) killEffectSpawnParticle(world, goldEffect, baseX + vector[0] * 0.45, y, baseZ + vector[2] * 0.45)
        }
        return firstResult
    }

    if (effect.pattern === "reaper") {
        soulEffect = killEffectParticleStub("minecraft:soul", 0.015, 2)
        smokeEffect = killEffectParticleStub("minecraft:soul_fire_flame", 0.02, 1)
        for (i = 0; i < points; i++) {
            t = points <= 1 ? 1 : i / (points - 1)
            angle = i * 41
            vector = KillEffectFrontVectorsInPlane(0, 90, entity, angle, 0, radius * (1 - t * 0.25))
            y = baseY + height * t
            if (!firstResult) firstResult = killEffectSpawnParticle(world, soulEffect, baseX + vector[0], y, baseZ + vector[2])
            else killEffectSpawnParticle(world, soulEffect, baseX + vector[0], y, baseZ + vector[2])
            if (i % 4 === 0) killEffectSpawnParticle(world, smokeEffect, baseX + vector[0] * 0.65, y - 0.2, baseZ + vector[2] * 0.65)
        }
        return firstResult
    }

    if (effect.pattern === "burst") {
        for (i = 0; i < points; i++) {
            angle = i * (360 / points)
            vector = KillEffectFrontVectorsInPlane(killEffectSafeRotation(entity), 30 + (i % 5) * 14, entity, angle, 0, radius)
            if (!firstResult) firstResult = killEffectSpawnParticle(world, effect, baseX + vector[0], baseY + vector[1], baseZ + vector[2])
            else killEffectSpawnParticle(world, effect, baseX + vector[0], baseY + vector[1], baseZ + vector[2])
        }
        return firstResult
    }

    for (i = 0; i < points; i++) {
        angle = i * (360 / points)
        vector = KillEffectFrontVectorsInPlane(0, 90, entity, angle, 0, radius)
        if (!firstResult) firstResult = killEffectSpawnParticle(world, effect, baseX + vector[0], baseY + vector[1], baseZ + vector[2])
        else killEffectSpawnParticle(world, effect, baseX + vector[0], baseY + vector[1], baseZ + vector[2])
    }

    return firstResult
}

function killEffectPlaySoulHarvest(player, entity, effect) {
    var world = player.getWorld()
    var startX = killEffectSafeX(entity)
    var startY = killEffectSafeY(entity) + (effect.yOffset || 1)
    var startZ = killEffectSafeZ(entity)
    var endX = killEffectSafeX(player)
    var endY = killEffectSafeY(player) + 1.15
    var endZ = killEffectSafeZ(player)
    var steps = effect.points || 26
    var soulEffect = killEffectParticleStub("minecraft:soul", 0.01, 2)
    var flameEffect = killEffectParticleStub("minecraft:soul_fire_flame", 0.015, 1)
    var smokeEffect = killEffectParticleStub("minecraft:reverse_portal", 0.06, 1)
    var started

    started = killEffectRunAsync(function() {
        var i
        var t
        var arc
        var wobble
        var x
        var y
        var z
        var angle
        var ringX
        var ringZ

        for (i = 0; i <= steps; i++) {
            t = i / steps
            arc = Math.sin(t * Math.PI) * (effect.height || 2.6)
            wobble = Math.sin(t * Math.PI * 6) * (effect.radius || 1.35) * (1 - t) * 0.25
            x = startX + (endX - startX) * t + wobble
            y = startY + (endY - startY) * t + arc
            z = startZ + (endZ - startZ) * t + Math.cos(t * Math.PI * 6) * (effect.radius || 1.35) * (1 - t) * 0.25

            killEffectSpawnParticle(world, soulEffect, x, y, z)
            if (i % 2 === 0) killEffectSpawnParticle(world, flameEffect, x, y - 0.08, z)
            if (i % 4 === 0) killEffectSpawnParticle(world, smokeEffect, x, y + 0.1, z)
            killEffectDelay(25)
        }

        for (i = 0; i < 24; i++) {
            angle = i * 15
            ringX = Math.cos(angle * Math.PI / 180) * 0.85
            ringZ = Math.sin(angle * Math.PI / 180) * 0.85
            killEffectSpawnParticle(world, soulEffect, endX + ringX, endY, endZ + ringZ)
            if (i % 3 === 0) killEffectSpawnParticle(world, flameEffect, endX + ringX * 0.55, endY + 0.25, endZ + ringZ * 0.55)
        }

        killEffectPlaySound(world, {
            sound: "minecraft:entity.experience_orb.pickup",
            volume: 0.8,
            pitch: 0.65
        }, endX, endY, endZ)
    })

    return started ? "threaded soul harvest" : "thread limit reached"
}

function killEffectPlayAtEntity(player, entity, forcedEffectId) {
    var data = killEffectLoadData(player)
    var effectId = forcedEffectId || data.active
    var effect = killEffectById(effectId)
    var world = player.getWorld()
    var x
    var y
    var z

    if (!effect) {
        killEffectDebug(player, "no effect for id " + effectId)
        return false
    }
    if (!forcedEffectId && !killEffectContains(data.unlocked, effectId)) {
        killEffectDebug(player, "effect not unlocked: " + effectId)
        return false
    }

    x = killEffectSafeX(entity)
    y = killEffectSafeY(entity) + 0.6
    z = killEffectSafeZ(entity)

    killEffectDebug(player, "playing " + effect.id + " at " + killEffectNumber(x) + " " + killEffectNumber(y) + " " + killEffectNumber(z))
    killEffectDebug(player, "sound result " + killEffectPlaySound(world, effect, x, y, z))
    if (effect.pattern === "soul_harvest") {
        killEffectDebug(player, "particle result " + killEffectPlaySoulHarvest(player, entity, effect))
        return true
    }
    killEffectDebug(player, "particle result " + killEffectPlayPattern(world, entity, effect))
    return true
}

function killEffectHandleKillEvent(event) {
    if (!event || !event.player) return
    if (!event.entity) {
        killEffectDebug(event.player, "kill event has no event.entity")
        return
    }
    if (!killEffectPlayAtEntity(event.player, event.entity, "")) {
        killEffectDebug(event.player, "kill event fired, playback skipped")
    }
}

function killEffectPreview(player, effectId) {
    if (!effectId) return false
    return killEffectPlayAtEntity(player, player, effectId)
}

function killEffectHandleAdminButton(event) {
    var admin = event.player
    var target
    var effect

    if (!killEffectIsAdmin(admin)) {
        killEffectAdminMsg(admin, "\u00A7cCreative mode required")
        admin.closeGui()
        return
    }

    if (event.buttonId === KILL_EFFECT_IDS.ADMIN_BTN_CLOSE) {
        admin.closeGui()
        return
    }

    if (event.buttonId === KILL_EFFECT_IDS.ADMIN_BTN_REFRESH) {
        killEffectBuildAdminGui(admin, "Refreshed")
        return
    }

    target = killEffectSelectedTarget(admin)
    effect = killEffectSelectedEffect(admin)

    if (!target || !effect) {
        killEffectBuildAdminGui(admin, "Select an online player and effect")
        return
    }

    if (event.buttonId === KILL_EFFECT_IDS.ADMIN_BTN_GRANT) {
        killEffectGrant(target, effect.id)
        killEffectMsg(target, "\u00A7aUnlocked kill effect: \u00A7e" + effect.name)
        killEffectBuildAdminGui(admin, "Granted " + effect.name + " to " + target.getName())
        return
    }

    if (event.buttonId === KILL_EFFECT_IDS.ADMIN_BTN_REVOKE) {
        killEffectRevoke(target, effect.id)
        killEffectMsg(target, "\u00A7cRemoved kill effect: \u00A7e" + effect.name)
        killEffectBuildAdminGui(admin, "Revoked " + effect.name + " from " + target.getName())
        return
    }

    if (event.buttonId === KILL_EFFECT_IDS.ADMIN_BTN_SET_ACTIVE) {
        if (!killEffectSetActive(target, effect.id)) {
            killEffectBuildAdminGui(admin, "Grant the effect before setting it active")
            return
        }
        killEffectMsg(target, "\u00A7aActive kill effect set to \u00A7e" + effect.name)
        killEffectBuildAdminGui(admin, "Set active for " + target.getName())
        return
    }

    if (event.buttonId === KILL_EFFECT_IDS.ADMIN_BTN_CLEAR_ACTIVE) {
        killEffectClearActive(target)
        killEffectMsg(target, "\u00A77Active kill effect cleared")
        killEffectBuildAdminGui(admin, "Cleared active effect for " + target.getName())
        return
    }

    if (event.buttonId === KILL_EFFECT_IDS.ADMIN_BTN_PREVIEW) {
        killEffectPreview(admin, effect.id)
        killEffectBuildAdminGui(admin, "Previewed " + effect.name)
    }
}

function killEffectHandlePlayerButton(event) {
    var player = event.player
    var ui = killEffectGetUi(player.getName())
    var effectId = ui.ownEffectIds[ui.ownEffectIndex]
    var effect = killEffectById(effectId)

    if (event.buttonId === KILL_EFFECT_IDS.PLAYER_BTN_CLOSE) {
        player.closeGui()
        return
    }

    if (!effect) {
        killEffectBuildPlayerGui(player, "No effect selected")
        return
    }

    if (event.buttonId === KILL_EFFECT_IDS.PLAYER_BTN_SET_ACTIVE) {
        if (!killEffectSetActive(player, effect.id)) {
            killEffectBuildPlayerGui(player, "That effect is not unlocked")
            return
        }
        killEffectBuildPlayerGui(player, "Active: " + effect.name)
        return
    }

    if (event.buttonId === KILL_EFFECT_IDS.PLAYER_BTN_CLEAR_ACTIVE) {
        killEffectClearActive(player)
        killEffectBuildPlayerGui(player, "Active effect cleared")
        return
    }

    if (event.buttonId === KILL_EFFECT_IDS.PLAYER_BTN_PREVIEW) {
        killEffectPreview(player, effect.id)
        killEffectBuildPlayerGui(player, "Previewed " + effect.name)
    }
}

function killEffectIsBoundaryChar(ch) {
    if (!ch) return true
    return " \t\r\n:;,.!?()[]{}<>|\u00A7\u00BB\u203A-".indexOf(ch) !== -1
}

function killEffectIsQuoteChar(ch) {
    return ch === "\"" || ch === "'" || ch === "\u201C" || ch === "\u201D" || ch === "\u2018" || ch === "\u2019"
}

function killEffectMentioned(message, command) {
    var lower = killEffectLower(message)
    var idx = 0
    var before
    var after

    while (idx !== -1) {
        idx = lower.indexOf(command, idx)
        if (idx === -1) return false

        before = idx > 0 ? lower.charAt(idx - 1) : ""
        after = idx + command.length < lower.length ? lower.charAt(idx + command.length) : ""

        if (!killEffectIsQuoteChar(before) && !killEffectIsQuoteChar(after) && killEffectIsBoundaryChar(before) && killEffectIsBoundaryChar(after)) {
            return true
        }

        idx = idx + command.length
    }

    return false
}

/**
 * @param {PlayerEvent.UpdateEvent} event
 */
function tick(event) {
    killEffectShowMainActionbar(event.player)
}

/**
 * @param {PlayerEvent.ChatEvent} event
 */
function chat(event) {
    var player = event.player
    var message = killEffectTrim(event.message)

    if (killEffectMentioned(message, "!showmenu")) {
        event.setCanceled(true)
        killEffectBuildMainMenu(player)
        return
    }

    if (killEffectMentioned(message, "!killeffadmin")) {
        event.setCanceled(true)
        if (!killEffectIsAdmin(player)) {
            killEffectAdminMsg(player, "\u00A7cCreative mode required")
            return
        }
        killEffectBuildAdminGui(player, "")
        return
    }

    if (killEffectMentioned(message, "!killeff debug on")) {
        event.setCanceled(true)
        killEffectSetDebug(player, true)
        killEffectMsg(player, "\u00A7aDebug enabled")
        killEffectDescribeData(player)
        return
    }

    if (killEffectMentioned(message, "!killeff debug off")) {
        event.setCanceled(true)
        killEffectSetDebug(player, false)
        killEffectMsg(player, "\u00A77Debug disabled")
        return
    }

    if (killEffectMentioned(message, "!killeff debug")) {
        event.setCanceled(true)
        killEffectDescribeData(player)
        if (!killEffectPlayAtEntity(player, player, "")) {
            killEffectMsg(player, "\u00A7cNo active unlocked kill effect to preview")
        }
        return
    }

    if (killEffectMentioned(message, "!killeff")) {
        event.setCanceled(true)
        killEffectBuildPlayerGui(player, "")
    }
}

/**
 * @param {PlayerEvent.KilledEntityEvent} event
 */
function kill(event) {
    killEffectHandleKillEvent(event)
}

/**
 * @param {PlayerEvent.KilledEntityEvent} event
 */
function killedEntity(event) {
    killEffectHandleKillEvent(event)
}

/**
 * @param {CustomGuiEvent.ScrollEvent} event
 */
function customGuiScroll(event) {
    var guiId = event.gui.getID()
    var ui = killEffectGetUi(event.player.getName())

    if (guiId === KILL_EFFECT_ADMIN_GUI) {
        if (event.scrollId === KILL_EFFECT_IDS.ADMIN_SCROLL_PLAYERS) {
            ui.playerIndex = event.scrollIndex
            ui.effectIndex = 0
            killEffectBuildAdminGui(event.player, "")
            return
        }

        if (event.scrollId === KILL_EFFECT_IDS.ADMIN_SCROLL_EFFECTS) {
            ui.effectIndex = event.scrollIndex
            return
        }
    }

    if (guiId === KILL_EFFECT_PLAYER_GUI && event.scrollId === KILL_EFFECT_IDS.PLAYER_SCROLL_EFFECTS) {
        ui.ownEffectIndex = event.scrollIndex
        killEffectBuildPlayerGui(event.player, "")
    }

    if (guiId === KILL_EFFECT_QUEST_MENU_GUI && event.scrollId === CYBER_QUEST_SCROLL) {
        ui.questIndex = event.scrollIndex
        killEffectBuildQuestMenu(event.player)
    }
}

/**
 * @param {CustomGuiEvent.ButtonEvent} event
 */
function customGuiButton(event) {
    var guiId = event.gui.getID()

    if (guiId === KILL_EFFECT_MAIN_MENU_GUI) {
        if (event.buttonId === KILL_EFFECT_MAIN_MENU_BTN_CLOSE) {
            event.player.closeGui()
            return
        }

        if (event.buttonId === KILL_EFFECT_MAIN_MENU_BTN_QUESTS) {
            killEffectBuildQuestMenu(event.player)
            return
        }

        if (event.buttonId === KILL_EFFECT_MAIN_MENU_BTN_GUILDS) {
            killEffectSendComingSoon(event.player, "Guilds")
            event.player.closeGui()
            return
        }

        if (event.buttonId === KILL_EFFECT_MAIN_MENU_BTN_KILL_EFFECTS) {
            killEffectBuildPlayerGui(event.player, "")
            return
        }

        if (event.buttonId === KILL_EFFECT_MAIN_MENU_BTN_RANKS) {
            killEffectSendRanksLink(event.player)
            event.player.closeGui()
            return
        }

        if (event.buttonId === KILL_EFFECT_MAIN_MENU_BTN_COSMETICS) {
            killEffectSendComingSoon(event.player, "Cosmetics")
            event.player.closeGui()
        }
        return
    }

    if (guiId === KILL_EFFECT_QUEST_MENU_GUI) {
        if (event.buttonId === CYBER_QUEST_BTN_BACK) {
            killEffectBuildMainMenu(event.player)
            return
        }

        if (event.buttonId === KILL_EFFECT_QUEST_MENU_BTN_CLOSE) {
            event.player.closeGui()
        }
        return
    }

    if (guiId === KILL_EFFECT_ADMIN_GUI) {
        killEffectHandleAdminButton(event)
        return
    }

    if (guiId === KILL_EFFECT_PLAYER_GUI) {
        killEffectHandlePlayerButton(event)
    }
}

/**
 * @param {CustomGuiEvent.CloseEvent} event
 */
function customGuiClosed(event) {
    if (event.gui.getID() === KILL_EFFECT_ADMIN_GUI || event.gui.getID() === KILL_EFFECT_PLAYER_GUI || event.gui.getID() === KILL_EFFECT_MAIN_MENU_GUI || event.gui.getID() === KILL_EFFECT_QUEST_MENU_GUI) {
        killEffectGetUi(event.player.getName()).lastClosedAt = new Date().getTime()
    }
}
