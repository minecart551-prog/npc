// ============================================================================
// BOMB DEFUSE — SHARED CONFIGURATION
// ============================================================================
// This file is loaded FIRST on every scripted block and player.
// Uses world.getStoreddata() for cross-block state sharing.
// ============================================================================

var SYS = Java.type("java.lang.System")

// ============================================================================
// COIN CURRENCY — Edit item IDs for different coin mods
// ============================================================================
var COIN_STONE      = "coins:stone_coin"
var COIN_COAL       = "coins:coal_coin"
var COIN_EMERALD    = "coins:emerald_coin"
var STONE_TO_COAL   = 100
var COAL_TO_EMERALD = 100

// ============================================================================
// MAP COORDINATES — Set these to your map
// ============================================================================
var LOBBY_SPAWN      = { x: 22, y: -59, z: 7 }   // Where new players appear / spectate
var SPECTATOR_SPAWN  = { x: 43, y: -59, z: -13 }  // Where dead players go to wait
var RESPAWN_POINT    = { x: 44, y: -59, z: -13 }  // Spawnpoint set with /spawnpoint command

var CT_SPAWNS  = [    { x: 7, y: -59, z: -22 }]
var T_SPAWNS   = [    { x: 7, y: -59, z: 4 }]

var BOMB_SITE_A_POS = { x: -4, y: -58, z: -14 }
var BOMB_SITE_B_POS = { x: 8, y: -58, z: -13 }

// ============================================================================
// BOMB ITEM
// ============================================================================
var BOMB_ITEM_ID = "minecraft:tnt"

// ============================================================================
// GAME TIMERS (in seconds)
// ============================================================================
var FREEZE_TIME_SECONDS    = 20
var ROUND_TIME_SECONDS     = 115
var BOMB_TIMER_SECONDS     = 45
var DEFUSE_TIME_WITH_KIT   = 5
var DEFUSE_TIME_NO_KIT     = 10

// ============================================================================
// MONEY SYSTEM (in cents: $1 = 100¢)
// ============================================================================
var STARTING_MONEY    = 80000
var KILL_REWARD       = 30000
var ASSIST_REWARD     = 15000
var KNIFE_KILL_REWARD = 150000
var BOMB_PLANT_REWARD = 30000
var BOMB_DEFUSE_REWARD= 30000
var WIN_REWARD        = 325000
var LOSE_REWARD_BASE  = 140000
var LOSE_REWARD_STEP  = 50000
var LOSE_REWARD_MAX   = 340000
var MAX_MONEY         = 1600000

// ============================================================================
// TEAM & GAME STATE CONSTANTS
// ============================================================================
var TEAM_NONE  = 0
var TEAM_CT    = 1
var TEAM_T     = 2

var PHASE_LOBBY       = "lobby"
var PHASE_FREEZETIME  = "freezetime"
var PHASE_LIVE        = "live"
var PHASE_BOMB_PLANTED= "bomb_planted"
var PHASE_ROUND_END   = "round_end"

// ============================================================================
// GUI IDs
// ============================================================================
var GUI_LOBBY   = 7500
var GUI_BUY     = 7501
var GUI_BOMB    = 7502
var GUI_SCORE   = 7503
var OVERLAY_HUD = 1

var openPlayers = {}

// ============================================================================
// GAME STATE FUNCTIONS
// ============================================================================
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
    if (!sd.has("bd_game")) {
        sd.put("bd_game", JSON.stringify({
            phase: PHASE_LOBBY, players: "[]", roundNum: 0,
            roundStartTime: 0, bombPlanted: false, bombSite: "",
            bombPlantTime: 0, defusingPlayers: "[]",
            ctScore: 0, tScore: 0,
            consecutiveLosses: "{\"1\":0,\"2\":0}", tickCounter: 0,
        }))
    }
    try { return JSON.parse(sd.get("bd_game")) } catch(e) { return null }
}

function saveGame(g) {
    var world = getGameWorld()
    if (!world) return
    world.getStoreddata().put("bd_game", JSON.stringify(g))
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

function getLosses(g) {
    if (!g || !g.consecutiveLosses) return {}
    try { return JSON.parse(g.consecutiveLosses) } catch(e) { return {} }
}

function setLosses(g, obj) {
    g.consecutiveLosses = JSON.stringify(obj)
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
// ROUND MANAGEMENT (shared across all blocks)
// ============================================================================
/** Check if a team should win based on alive players. Returns null or winner team number. */
function checkTeamWinCondition() {
    var g = getGame()
    if (!g || g.phase === PHASE_ROUND_END || g.phase === PHASE_LOBBY) return null
    var players = getPlayers(g)

    var aliveCt = 0, aliveT = 0
    for (var i = 0; i < players.length; i++) {
        if (players[i].team === TEAM_CT && !players[i].isDead) aliveCt++
        if (players[i].team === TEAM_T && !players[i].isDead) aliveT++
    }

    if (aliveCt === 0 && aliveT === 0) return null

    if (g.phase === PHASE_BOMB_PLANTED) {
        // Bomb planted: T wins if CT can't defuse (all CT dead)
        if (aliveCt === 0 && aliveT >= 0) return TEAM_T
        // CT needs to defuse the bomb to win (handled by defuse timer)
        return null
    }

    // Bomb not planted: eliminate all enemies to win
    if (aliveCt === 0) return TEAM_T
    if (aliveT === 0) return TEAM_CT

    return null
}

function endRound(winner, reason) {
    var g = getGame()
    if (!g) return
    var players = getPlayers(g)
    g.phase = PHASE_ROUND_END
    g.roundStartTime = SYS.currentTimeMillis()

    var winnerName = winner === TEAM_CT ? "\u00a7bCounter-Terrorists" : "\u00a7cTerrorists"
    broadcast("\u00a76\u00a7l" + winnerName + " \u00a76win! (" + reason + "\u00a76)")

    if (winner === TEAM_CT) g.ctScore++
    else g.tScore++

    var losses = getLosses(g)

    for (var i = 0; i < players.length; i++) {
        var p = players[i]
        var entry = openPlayers[p.uuid]
        if (p.team === winner) {
            p.money += WIN_REWARD
            if (p.money > MAX_MONEY) p.money = MAX_MONEY
            playerMsg(p.uuid, "\u00a7a+ " + fmt(WIN_REWARD) + " (win bonus)")
            losses[p.team] = 0
        } else if (p.team >= TEAM_CT) {
            var lossCount = losses[p.team] || 0
            var lossReward = Math.min(LOSE_REWARD_BASE + (lossCount * LOSE_REWARD_STEP), LOSE_REWARD_MAX)
            p.money += lossReward
            if (p.money > MAX_MONEY) p.money = MAX_MONEY
            losses[p.team] = lossCount + 1
            playerMsg(p.uuid, "\u00a7c+ " + fmt(lossReward) + " (loss bonus)")
        }
        if (entry && entry.player) {
            giveCoins(entry.player, Math.min(p.money, 640000))
        }
    }

    setPlayers(g, players)
    setLosses(g, losses)
    saveGame(g)

    broadcast("\u00a77Score: \u00a7bCT " + g.ctScore + " \u00a77- \u00a7cT " + g.tScore)
    broadcast("\u00a7eNext round in 5 seconds...")
}

function bombExplode() {
    var g = getGame()
    if (!g) return
    broadcast("\u00a7c\u00a7l\uD83D\uDCA5 BOMB EXPLODED! \u00a7cTerrorists win!")
    try {
        var uuids = Object.keys(openPlayers)
        for (var u = 0; u < uuids.length; u++) {
            var entry = openPlayers[uuids[u]]
            if (entry && entry.player) {
                entry.player.playSound("minecraft:entity.generic.explode", 1.0, 1.0)
            }
        }
    } catch(e) {}
    endRound(TEAM_T, "Bomb detonated!")
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
    if (idx < 0) {
        var anyEntry = openPlayers[uuid]
        if (anyEntry && anyEntry.player) anyEntry.player.setPosition(LOBBY_SPAWN.x, LOBBY_SPAWN.y, LOBBY_SPAWN.z)
        return
    }

    if (players[idx].hasBomb) {
        players[idx].hasBomb = false
        var tPlayers = []
        for (var j = 0; j < players.length; j++)
            if (players[j].team === TEAM_T && j !== idx) tPlayers.push(j)
        if (tPlayers.length > 0) {
            var newBomb = tPlayers[Math.floor(Math.random() * tPlayers.length)]
            players[newBomb].hasBomb = true
            var bombEntry = openPlayers[players[newBomb].uuid]
            if (bombEntry && bombEntry.player) {
                bombEntry.player.giveItem(bombEntry.player.getWorld().createItem(BOMB_ITEM_ID, 1))
                broadcast("\u00a77[Bomb] " + players[newBomb].name + " now has the bomb (transferred)!")
            }
        }
    }

    if (g.bombPlanted) {
        var defusing = getDefusing(g)
        for (var di = defusing.length - 1; di >= 0; di--) {
            if (defusing[di].uuid === uuid) defusing.splice(di, 1)
        }
        setDefusing(g, defusing)
    }

    var entry = openPlayers[uuid]
    if (entry && entry.player) {
        entry.player.setPosition(LOBBY_SPAWN.x, LOBBY_SPAWN.y, LOBBY_SPAWN.z)
        entry.player.message("\u00a77You left the game.")
        try { entry.player.hideOverlay(OVERLAY_HUD) } catch(e) {}
    }

    players.splice(idx, 1)
    setPlayers(g, players)
    saveGame(g)

    if (g.phase !== PHASE_LOBBY) {
        var winner = checkTeamWinCondition()
        if (winner !== null) {
            var reason = winner === TEAM_T ? "CT team eliminated" : "T team eliminated"
            endRound(winner, reason)
        } else if (players.length === 0) {
            broadcast("\u00a77All players left. Returning to lobby.")
            g.phase = PHASE_LOBBY
            saveGame(g)
        }
    }
}

function rejoinPlayer(uuid, player) {
    var g = getGame()
    if (!g) return false
    var players = getPlayers(g)
    var idx = -1
    for (var i = 0; i < players.length; i++)
        if (players[i].uuid === uuid) { idx = i; break }
    if (idx < 0) return false

    setPlayers(g, players)
    saveGame(g)

    if (g.phase === PHASE_LOBBY) return true

    players[idx].team = TEAM_NONE
    players[idx].ready = false
    setPlayers(g, players)
    saveGame(g)
    player.setPosition(SPECTATOR_SPAWN.x, SPECTATOR_SPAWN.y, SPECTATOR_SPAWN.z)
    clearInventory(player)
    player.message("\u00a77[Rejoin] Round in progress. Spectating until next round.")
    player.message("\u00a77Use the Lobby block to rejoin when the round ends.")
    return true
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

function broadcast(msg) {
    var uuids = Object.keys(openPlayers)
    for (var i = 0; i < uuids.length; i++) {
        var entry = openPlayers[uuids[i]]
        if (entry && entry.player) entry.player.message(msg)
    }
}

function teamName(team) {
    if (team === TEAM_CT) return "\u00a7bCT"
    if (team === TEAM_T) return "\u00a7cT"
    return "\u00a77Spectator"
}

function teamColor(team) {
    if (team === TEAM_CT) return "\u00a7b"
    if (team === TEAM_T) return "\u00a7c"
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

function countAliveTeam(team) {
    var g = getGame()
    if (!g) return 0
    var players = getPlayers(g)
    var count = 0
    for (var i = 0; i < players.length; i++)
        if (players[i].team === team && !players[i].isDead) count++
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
        if (!sd.has("bd_game")) {
            sd.put("bd_game", JSON.stringify({
                phase: PHASE_LOBBY, players: "[]", roundNum: 0,
                roundStartTime: 0, bombPlanted: false, bombSite: "",
                bombPlantTime: 0, defusingPlayers: "[]",
                ctScore: 0, tScore: 0,
                consecutiveLosses: "{\"1\":0,\"2\":0}", tickCounter: 0,
            }))
        }
        return sd
    }
    return null
}