// ============================================================================
// BOMB DEFUSE — BUY BLOCK
// ============================================================================
// Place on Scripted Blocks at CT and T spawn. Uses world.getStoreddata().
// Requires bomb_defuse_config.js loaded FIRST.
// ============================================================================

var TEAM_BOTH = 0
var TEAM_CT_ONLY = 1
var TEAM_T_ONLY = 2

var BUY_ITEMS = {
    knives: [
        { id: "minecraft:iron_sword", count: 1, price: 0,      name: "Default Knife",       team: TEAM_BOTH },
        { id: "minecraft:wooden_sword", count: 1, price: 0,    name: "Default Knife (T)",    team: TEAM_T_ONLY },
        { id: "minecraft:golden_sword", count: 1, price: 0,    name: "Default Knife (CT)",   team: TEAM_CT_ONLY },
    ],
    pistols: [
        { id: "minecraft:bow", count: 1, price: 0,             name: "Default Pistol",       team: TEAM_BOTH },
        { id: "minecraft:crossbow", count: 1, price: 70000,    name: "Deagle",               team: TEAM_BOTH },
    ],
    rifles: [
        { id: "minecraft:crossbow", count: 1, price: 310000,   name: "M4A4",                 team: TEAM_CT_ONLY },
        { id: "minecraft:crossbow", count: 1, price: 270000,   name: "AK-47",                team: TEAM_T_ONLY },
        { id: "minecraft:crossbow", count: 1, price: 225000,   name: "FAMAS",                team: TEAM_CT_ONLY },
        { id: "minecraft:crossbow", count: 1, price: 200000,   name: "Galil AR",             team: TEAM_T_ONLY },
    ],
    snipers: [
        { id: "minecraft:crossbow", count: 1, price: 475000,   name: "AWP",                  team: TEAM_BOTH },
    ],
    smgs: [
        { id: "minecraft:bow", count: 1, price: 105000,        name: "MAC-10",               team: TEAM_T_ONLY },
        { id: "minecraft:bow", count: 1, price: 120000,        name: "MP9",                  team: TEAM_CT_ONLY },
    ],
    equipment: [
        { id: "minecraft:leather_helmet", count: 1, price: 65000,  name: "Kevlar",            team: TEAM_BOTH },
        { id: "minecraft:chainmail_helmet", count: 1, price: 100000, name: "Kevlar+Helmet",   team: TEAM_BOTH },
        { id: "minecraft:shears", count: 1, price: 40000,       name: "Defuse Kit",           team: TEAM_CT_ONLY },
        { id: "minecraft:flint_and_steel", count: 1, price: 20000, name: "Zeus x27",          team: TEAM_BOTH },
        { id: "minecraft:splash_potion", count: 1, price: 20000,   name: "Flashbang",         team: TEAM_BOTH },
        { id: "minecraft:firework_rocket", count: 1, price: 30000, name: "Smoke",             team: TEAM_BOTH },
        { id: "minecraft:lingering_potion", count: 1, price: 30000, name: "HE Grenade",       team: TEAM_BOTH },
        { id: "minecraft:fire_charge", count: 1, price: 50000,     name: "Molotov",           team: TEAM_BOTH },
    ],
}

var B = {
    LBL_TITLE:        1,
    LBL_MONEY:        2,
    LBL_STATUS:       3,
    LBL_TIME:         4,
    BTN_TAB_PISTOLS:  10,
    BTN_TAB_RIFLES:   11,
    BTN_TAB_SMGS:     12,
    BTN_TAB_SNIPERS:  13,
    BTN_TAB_EQUIP:    14,
    BTN_ITEM_BASE:    20,
    BTN_CLOSE:        99,
    BTN_LEAVE_GAME:   100,
}

var BUY_TABS = [
    { key: "pistols",   name: "Pistols",  btn: B.BTN_TAB_PISTOLS },
    { key: "rifles",    name: "Rifles",   btn: B.BTN_TAB_RIFLES },
    { key: "smgs",      name: "SMGs",     btn: B.BTN_TAB_SMGS },
    { key: "snipers",   name: "Snipers",  btn: B.BTN_TAB_SNIPERS },
    { key: "equipment", name: "Equip",    btn: B.BTN_TAB_EQUIP },
]

var currentBuyTab = 0

function interact(event) {
    openPlayers[event.player.getUUID()] = { player: event.player, API: event.API }
    getGameFromEvent(event)

    var g = getGame()
    if (!g) { event.player.message("\u00a7cGame not started yet!"); return }
    if (g.phase !== PHASE_FREEZETIME) { event.player.message("\u00a7cYou can only buy during freeze time!"); return }
    openBuyGui(event)
}

function openBuyGui(event) {
    var player = event.player
    var uuid   = player.getUUID()
    var api    = event.API

    var mp = getMyPlayer(uuid)
    if (!mp) { player.message("\u00a7cYou're not in the game!"); return }
    if (mp.player.team <= 0) { player.message("\u00a7cSpectators can't buy!"); return }

    var gui = api.createCustomGui(GUI_BUY, 420, 240, false, player)

    gui.addLabel(B.LBL_TITLE, "\u00a76\u00a7lBUY MENU", 180, 5, 100, 16)
    gui.addLabel(B.LBL_MONEY, "\u00a77Money: " + fmt(mp.player.money), 10, 5, 150, 12)

    var remaining = FREEZE_TIME_SECONDS
    if (mp.game.phase === PHASE_FREEZETIME) {
        var elapsed = (SYS.currentTimeMillis() - mp.game.roundStartTime) / 1000
        remaining = Math.max(0, FREEZE_TIME_SECONDS - Math.floor(elapsed))
    }
    gui.addLabel(B.LBL_TIME, "\u00a77Time: \u00a7e" + remaining + "s", 320, 5, 80, 12)

    for (var t = 0; t < BUY_TABS.length; t++) {
        var tab = BUY_TABS[t]
        var x = 10 + t * 80
        var highlight = (t === currentBuyTab) ? "\u00a7l> " : "  "
        gui.addButton(tab.btn, highlight + tab.name, x, 25, 75, 16)
    }

    var items = BUY_ITEMS[BUY_TABS[currentBuyTab].key] || []
    var itemY = 50
    var shown = 0
    for (var i = 0; i < items.length; i++) {
        var item = items[i]
        if (item.team !== TEAM_BOTH && item.team !== mp.player.team) continue
        var canAfford = mp.player.money >= item.price
        var priceStr = canAfford ? "\u00a7a" + fmt(item.price) : "\u00a7c" + fmt(item.price)
        var btnId = B.BTN_ITEM_BASE + i
        gui.addButton(btnId, item.name + " (" + priceStr + ")", 20, itemY, 380, 16)
        shown++
        itemY += 18
    }

    if (shown === 0) gui.addLabel(B.LBL_STATUS, "\u00a77No items available", 20, 50, 200, 12)
    gui.addButton(B.BTN_CLOSE, "\u00a77Close", 370, 220, 45, 14)
    gui.addButton(B.BTN_LEAVE_GAME, "\u00a7cLeave Match", 10, 220, 70, 14)
    player.showCustomGui(gui)
}

function customGuiButton(event) {
    if (event.gui.getID() !== GUI_BUY) return
    var player = event.player
    var uuid   = player.getUUID()
    var bid    = event.buttonId

    for (var t = 0; t < BUY_TABS.length; t++) {
        if (bid === BUY_TABS[t].btn) { currentBuyTab = t; openBuyGui(event); return }
    }

    if (bid === B.BTN_CLOSE) { player.closeGui(); return }
    if (bid === B.BTN_LEAVE_GAME) { player.closeGui(); removePlayerFromGame(uuid); return }

    var items = BUY_ITEMS[BUY_TABS[currentBuyTab].key] || []
    var itemIdx = bid - B.BTN_ITEM_BASE
    if (itemIdx < 0 || itemIdx >= items.length) return

    var item = items[itemIdx]
    var mp = getMyPlayer(uuid)
    if (!mp) return

    if (item.team !== TEAM_BOTH && item.team !== mp.player.team) {
        player.message("\u00a7cThis item is not available for your team!"); return
    }
    if (mp.player.money < item.price) {
        player.message("\u00a7cNot enough money! Need " + fmt(item.price) + ", have " + fmt(mp.player.money)); return
    }

    if (item.id === "minecraft:shears" && mp.player.team === TEAM_CT) {
        if (mp.player.hasDefuseKit) { player.message("\u00a7cYou already have a defuse kit!"); return }
        mp.player.hasDefuseKit = true
        mp.player.money -= item.price
        saveMyPlayer(mp)
        player.message("\u00a7aPurchased Defuse Kit for " + fmt(item.price))
        openBuyGui(event); return
    }

    mp.player.money -= item.price
    saveMyPlayer(mp)
    try {
        var mcItem = player.getWorld().createItem(item.id, item.count)
        if (mcItem) {
            mcItem.setCustomName(item.name)
            player.giveItem(mcItem)
            player.message("\u00a7aPurchased " + item.name + " for " + fmt(item.price))
        }
    } catch(e) {
        player.message("\u00a7cError purchasing item!")
        mp.player.money += item.price
        saveMyPlayer(mp)
    }
    openBuyGui(event)
}

function customGuiClosed(event) {}