// ============================================================================
// BOMB TEAM — SHARED CONFIGURATION
// ============================================================================
// Load this FIRST on every scripted block and player script.
// Uses world.getStoreddata() for cross-block state sharing.
// ============================================================================

var SYS = Java.type("java.lang.System")

// ============================================================================
// COIN CURRENCY
// ============================================================================
var COIN_STONE      = "coins:stone_coin"
var COIN_COAL       = "coins:coal_coin"
var COIN_EMERALD    = "coins:emerald_coin"
var STONE_TO_COAL   = 100
var COAL_TO_EMERALD = 100

// ============================================================================
// LOBBY SPAWN
// ============================================================================
var LOBBY_SPAWN = { x: 9, y: -60, z: -6 }

// ============================================================================
// BOMB ITEM
// ============================================================================
var BOMB_ITEM_ID = "minecraft:tnt"

// ============================================================================
// GAME TIMERS (in seconds)
// ============================================================================
var BOMB_TIMER_SECONDS     = 45
var DEFUSE_TIME_WITH_KIT   = 5
var DEFUSE_TIME_NO_KIT     = 10
var ROUND_END_DELAY        = 5
var BOMB_REFRESH_MINUTES   = 5  // How often bomb refreshes (clears and re-enables pickup)

// ============================================================================
// FUND MINIMUM
// ============================================================================
var MIN_FUND_AMOUNT = 100  // in cents: $1.00 default

// ============================================================================
// RESPAWN POINT — Set via command on team join
// ============================================================================
var POLICE_RESPAWN   = { x: 9, y: -60, z: -6 }
var CRIMINAL_RESPAWN = { x: 9, y: -60, z: -6 }

// ============================================================================
// TEAM CONSTANTS
// ============================================================================
var TEAM_NONE     = 0
var TEAM_POLICE   = 1
var TEAM_CRIMINAL = 2

var PHASE_IDLE         = "idle"
var PHASE_ACTIVE       = "active"
var PHASE_BOMB_PLANTED = "bomb_planted"
var PHASE_ROUND_END    = "round_end"

// ============================================================================
// GUI IDs
// ============================================================================
var GUI_LOBBY   = 7600
var GUI_TEAMPC  = 7601
var GUI_BOMB    = 7602
var GUI_FUND    = 7603
var OVERLAY_HUD = 1

var openPlayers = {}

// ============================================================================
// GAME STATE
// ============================================================================
// game = {
//     phase, policeFund, criminalFund, bombPlanted,
//     bombSiteWorld, bombSiteX, bombSiteY, bombSiteZ,
//     bombPlantTime, defusingPlayers, players,
//     bombRefreshTime,        // timestamp of last bomb refresh
//     activeStartTime,         // when PHASE_ACTIVE started
// }

function getGameWorld() {
    var uuids = Object.keys(openPlayers)
    for (var i = 0; i < uuids.length; i++) {
        var entry = openPlayers[uuids[i]]
        if (entry && entry.player) return entry.player.getWorld()
    }
    return null
}

function getGame() {
    var world = getGameWorld()
    if (!world) return null
    var sd = world.getStoreddata()
    if (!sd.has("teampc")) {
        sd.put("teampc", JSON.stringify({
            phase: PHASE_IDLE, policeFund: 0, criminalFund: 0,
            bombPlanted: false, bombSiteWorld: "", bombSiteX: 0, bombSiteY: 0, bombSiteZ: 0,
            bombPlantTime: 0, defusingPlayers: "[]", players: "[]",
            bombRefreshTime: 0, activeStartTime: 0,
            balances: "{}",     // offline winnings: { uuid: amount }
            playerNames: "{}", // last known names: { uuid: name }
        }))
    }
    try { return JSON.parse(sd.get("teampc")) } catch(e) { return null }
}

function saveGame(g) {
    var world = getGameWorld()
    if (!world) return
    world.getStoreddata().put("teampc", JSON.stringify(g))
}

// ============================================================================
// BALANCES (offline winnings) and player name registry
// ============================================================================
function getBalances(g) {
    if (!g) return {}
    if (!g.balances) return {}
    try { return JSON.parse(g.balances) } catch(e) { return {} }
}

function setBalances(g, obj) {
    g.balances = JSON.stringify(obj || {})
}

function addBalance(g, uuid, amount) {
    if (!g || amount <= 0) return
    var b = getBalances(g)
    b[uuid] = (b[uuid] || 0) + amount
    setBalances(g, b)
    saveGame(g)
}

function getPlayerBalance(g, uuid) {
    if (!g) return 0
    var b = getBalances(g)
    return b[uuid] || 0
}

function clearBalance(g, uuid) {
    if (!g) return 0
    var b = getBalances(g)
    var v = b[uuid] || 0
    if (v > 0) { delete b[uuid]; setBalances(g, b); saveGame(g) }
    return v
}

function getPlayerNames(g) {
    if (!g || !g.playerNames) return {}
    try { return JSON.parse(g.playerNames) } catch(e) { return {} }
}

function setPlayerName(g, uuid, name) {
    if (!g) return
    var n = getPlayerNames(g)
    n[uuid] = name
    g.playerNames = JSON.stringify(n)
}

function getPlayers(g) {
    if (!g || !g.players) return []
    try { return JSON.parse(g.players) } catch(e) { return [] }
}

function setPlayers(g, arr) {
    g.players = JSON.stringify(arr)
}

function getDefusing(g) {
    if (!g || !g.defusingPlayers) return []
    try { return JSON.parse(g.defusingPlayers) } catch(e) { return [] }
}

function setDefusing(g, arr) {
    g.defusingPlayers = JSON.stringify(arr)
}

function getMyPlayer(uuid) {
    var g = getGame()
    if (!g) return null
    var players = getPlayers(g)
    var idx = -1
    for (var i = 0; i < players.length; i++)
        if (players[i].uuid === uuid) { idx = i; break }
    if (idx < 0) return null
    return { player: players[idx], index: idx, game: g, players: players }
}

function saveMyPlayer(mp) {
    if (!mp) return
    mp.players[mp.index] = mp.player
    setPlayers(mp.game, mp.players)
    saveGame(mp.game)
}

// ============================================================================
// ONLINE PLAYER CHECK
// ============================================================================
/** Get the set of UUIDs of players currently online in the world */
function getOnlinePlayerUUIDs() {
    var world = getGameWorld()
    var online = {}
    if (world) {
        try {
            var allPlayers = world.getAllPlayers()
            for (var i = 0; i < allPlayers.length; i++) {
                try { online[allPlayers[i].getUUID()] = true } catch(e) {}
            }
        } catch(e) {}
    }
    return online
}

/** Returns true if the given UUID is currently online */
function isPlayerOnline(uuid) {
    var online = getOnlinePlayerUUIDs()
    return online[uuid] === true
}

/** Get count of ONLINE police/criminal team members */
function countOnlineTeam(team) {
    var g = getGame(); if (!g) return 0
    var players = getPlayers(g)
    var online = getOnlinePlayerUUIDs()
    var count = 0
    for (var i = 0; i < players.length; i++) {
        if (players[i].team === team && online[players[i].uuid]) count++
    }
    return count
}

// ============================================================================
// BOMB REFRESH
// ============================================================================
function getBombRefreshRemaining(g) {
    if (!g || g.bombRefreshTime <= 0) return 0  // 0 = already available
    var elapsed = Math.floor((SYS.currentTimeMillis() - g.bombRefreshTime) / 1000)
    return Math.max(0, (BOMB_REFRESH_MINUTES * 60) - elapsed)
}

function checkBombRefresh() {
    var g = getGame()
    if (!g) return false
    return getBombRefreshRemaining(g) <= 0
}

function doBombRefresh() {
    var g = getGame()
    if (!g) return
    // Do NOT reset bombRefreshTime here - timer stays at 0 so criminals can get the bomb
    var players = getPlayers(g)
    // Remove bomb items from ALL criminal players in the game
    var world = getGameWorld()
    if (world) {
        try {
            for (var i = 0; i < players.length; i++) {
                if (players[i].team === TEAM_CRIMINAL) {
                    // Get the actual player/entity object by UUID
                    var ap = world.getEntity(players[i].uuid)
                    if (ap && ap.getInventory) {
                        try {
                            var inv = ap.getInventory()
                            for (var si = 0; si < inv.getSize(); si++) {
                                var stack = inv.getSlot(si)
                                if (stack && !stack.isEmpty() && stack.getName() === BOMB_ITEM_ID) {
                                    inv.setSlot(si, null)
                                }
                            }
                            ap.updatePlayerInventory()
                        } catch(e) {}
                    }
                }
            }
        } catch(e) {}
    }
    setPlayers(g, players)
    saveGame(g)
    teamBroadcast("\u00a7c[Bomb Refresh] Return to lobby to get bomb!")
}

// ============================================================================
// PLAYER MANAGEMENT
// ============================================================================
function removePlayerFromGame(uuid) {
    var g = getGame()
    if (!g) return
    var players = getPlayers(g)
    var idx = -1
    for (var i = 0; i < players.length; i++)
        if (players[i].uuid === uuid) { idx = i; break }
    if (idx < 0) return

    var entry = openPlayers[uuid]
    if (entry && entry.player) {   
        entry.player.message("\u00a77You left the game.")
        try { entry.player.hideOverlay(OVERLAY_HUD) } catch(e) {}
    }

    players.splice(idx, 1)
    setPlayers(g, players)
    saveGame(g)
}

// ============================================================================
// BROADCAST — Send message only to players in the game (both teams)
// Uses world.getAllPlayers() to reach all players, then filters by game membership
// ============================================================================
function teamBroadcast(msg) {
    var g = getGame()
    if (!g) return
    var players = getPlayers(g)
    var world = getGameWorld()
    if (world) {
        try {
            var allPlayers = world.getAllPlayers()
            for (var pi = 0; pi < allPlayers.length; pi++) {
                var ap = allPlayers[pi]
                var auuid = ap.getUUID()
                // Check if this player is in the game (on any team)
                for (var i = 0; i < players.length; i++) {
                    if (players[i].uuid === auuid) {
                        ap.message(msg)
                        break
                    }
                }
            }
        } catch(e) {}
    }
}

// Send a message only to POLICE team members in the game
function policeBroadcast(msg) {
    var g = getGame()
    if (!g) return
    var players = getPlayers(g)
    var world = getGameWorld()
    if (world) {
        try {
            var allPlayers = world.getAllPlayers()
            for (var pi = 0; pi < allPlayers.length; pi++) {
                var ap = allPlayers[pi]
                var auuid = ap.getUUID()
                // Check if this player is on the police team
                for (var i = 0; i < players.length; i++) {
                    if (players[i].uuid === auuid && players[i].team === TEAM_POLICE) {
                        ap.message(msg)
                        break
                    }
                }
            }
        } catch(e) {}
    }
}


// ============================================================================
// COIN HELPERS
// ============================================================================
function countCoins(player) {
    var total = 0
    var inv = player.getInventory()
    for (var i = 0; i < inv.getSize(); i++) {
        var s = inv.getSlot(i)
        if (s && !s.isEmpty()) {
            var n = s.getName()
            if      (n === COIN_STONE)   total += s.getStackSize()
            else if (n === COIN_COAL)    total += s.getStackSize() * STONE_TO_COAL
            else if (n === COIN_EMERALD) total += s.getStackSize() * STONE_TO_COAL * COAL_TO_EMERALD
        }
    }
    return total
}

function removeCoins(player, amount) {
    var rem = amount
    var inv = player.getInventory()
    var world = player.getWorld()
    for (var i = 0; i < inv.getSize() && rem > 0; i++) {
        var s = inv.getSlot(i)
        if (s && !s.isEmpty() && s.getName() === COIN_STONE) {
            var qty = s.getStackSize()
            if (qty <= rem) { inv.setSlot(i, null); rem -= qty }
            else { s.setStackSize(qty - rem); rem = 0 }
        }
    }
    for (var i = 0; i < inv.getSize() && rem > 0; i++) {
        var s = inv.getSlot(i)
        if (s && !s.isEmpty() && s.getName() === COIN_COAL) {
            var qty = s.getStackSize()
            var val = qty * STONE_TO_COAL
            if (val <= rem) { inv.setSlot(i, null); rem -= val }
            else {
                var need = Math.ceil(rem / STONE_TO_COAL)
                var over = need * STONE_TO_COAL - rem
                s.setStackSize(qty - need); rem = 0
                if (over > 0) player.giveItem(world.createItem(COIN_STONE, over))
            }
        }
    }
    for (var i = 0; i < inv.getSize() && rem > 0; i++) {
        var s = inv.getSlot(i)
        if (s && !s.isEmpty() && s.getName() === COIN_EMERALD) {
            var qty = s.getStackSize()
            var unit = STONE_TO_COAL * COAL_TO_EMERALD
            var val = qty * unit
            if (val <= rem) { inv.setSlot(i, null); rem -= val }
            else {
                var need = Math.ceil(rem / unit)
                var over = need * unit - rem
                s.setStackSize(qty - need); rem = 0
                var gc = Math.floor(over / STONE_TO_COAL)
                var gs = over % STONE_TO_COAL
                if (gc > 0) player.giveItem(world.createItem(COIN_COAL, gc))
                if (gs > 0) player.giveItem(world.createItem(COIN_STONE, gs))
            }
        }
    }
    player.updatePlayerInventory()
    return rem <= 0
}

function giveCoins(player, amount) {
    var rem = amount
    if (rem <= 0) return
    var world = player.getWorld()
    var unit = STONE_TO_COAL * COAL_TO_EMERALD
    if (rem >= unit) {
        var em = Math.floor(rem / unit)
        while (em > 0) { var g = Math.min(em, 64); player.giveItem(world.createItem(COIN_EMERALD, g)); em -= g }
        rem %= unit
    }
    if (rem >= STONE_TO_COAL) {
        var co = Math.floor(rem / STONE_TO_COAL)
        while (co > 0) { var g = Math.min(co, 64); player.giveItem(world.createItem(COIN_COAL, g)); co -= g }
        rem %= STONE_TO_COAL
    }
    while (rem > 0) {
        var g = Math.min(rem, 64)
        player.giveItem(world.createItem(COIN_STONE, g))
        rem -= g
    }
    player.updatePlayerInventory()
}

function clearInventory(player) {
    var inv = player.getInventory()
    for (var i = 0; i < inv.getSize(); i++) {
        try { inv.setSlot(i, null) } catch(e) {}
    }
    try { player.getInventory().setArmorSlot(0, null) } catch(e) {}
    try { player.getInventory().setArmorSlot(1, null) } catch(e) {}
    try { player.getInventory().setArmorSlot(2, null) } catch(e) {}
    try { player.getInventory().setArmorSlot(3, null) } catch(e) {}
    player.updatePlayerInventory()
}

function fmt(amount) {
    if (amount < STONE_TO_COAL) return "\u00a7e" + amount + "\u00a2"
    var dollars = Math.floor(amount / STONE_TO_COAL)
    var cents = amount % STONE_TO_COAL
    if (cents === 0) return "\u00a7e$" + dollars + ".00"
    if (cents < 10) return "\u00a7e$" + dollars + ".0" + cents
    return "\u00a7e$" + dollars + "." + cents
}

function teamName(team) {
    if (team === TEAM_POLICE) return "\u00a7bPolice"
    if (team === TEAM_CRIMINAL) return "\u00a7cCriminal"
    return "\u00a77Spectator"
}

function teamColor(team) {
    if (team === TEAM_POLICE) return "\u00a7b"
    if (team === TEAM_CRIMINAL) return "\u00a7c"
    return "\u00a77"
}

function countTeam(team) {
    var g = getGame()
    if (!g) return 0
    var players = getPlayers(g)
    var count = 0
    for (var i = 0; i < players.length; i++)
        if (players[i].team === team) count++
    return count
}

function playerMsg(uuid, msg) {
    var entry = openPlayers[uuid]
    if (entry && entry.player) entry.player.message(msg)
}

function getGameFromEvent(event) {
    if (event && event.player) {
        var world = event.player.getWorld()
        var sd = world.getStoreddata()
        if (!sd.has("teampc")) {
            sd.put("teampc", JSON.stringify({
                phase: PHASE_IDLE, policeFund: 0, criminalFund: 0,
                bombPlanted: false, bombSiteWorld: "", bombSiteX: 0, bombSiteY: 0, bombSiteZ: 0,
                bombPlantTime: 0, defusingPlayers: "[]", players: "[]",
                bombRefreshTime: 0, activeStartTime: 0,
            }))
        }
        return sd
    }
    return null
}
