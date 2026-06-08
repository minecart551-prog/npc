// ============================================================================
// BOMB DEFUSE — PLAYER SCRIPT
// ============================================================================
// Attach to all players via CustomNPCs player scripts.
// Uses world.getStoreddata() for game state.
// Requires bomb_defuse_config.js loaded FIRST.
// ============================================================================

var playerTimerId = 69420

function init(event) {
    var player = event.player
    openPlayers[player.getUUID()] = { player: player, API: event.API }
    player.getTimers().forceStart(playerTimerId, 5, true)
}

function timer(event) {
    if (event.id !== playerTimerId) return
    var player = event.player
    var api    = event.API
    var uuid   = player.getUUID()

    openPlayers[uuid] = { player: player, API: api }

    var g = getGame()
    if (!g) {
        try { player.hideOverlay(OVERLAY_HUD) } catch(e) {}
        return
    }

    var players = getPlayers(g)
    var pIdx = -1
    for (var i = 0; i < players.length; i++)
        if (players[i].uuid === uuid) { pIdx = i; break }
    if (pIdx < 0) {
        try { player.hideOverlay(OVERLAY_HUD) } catch(e) {}
        return
    }
    var p = players[pIdx]

    var overlay = api.createOverlay(OVERLAY_HUD)

    // Line 1: Team + Money
    var isDead = p.isDead === true
    var teamStr = isDead ? "\u00a77DEAD" : (p.team === TEAM_CT ? "\u00a7bCT" : (p.team === TEAM_T ? "\u00a7cT" : "\u00a77SPEC"))
    var moneyStr = "\u00a7e" + fmt(p.money)
    overlay.addLabel(1, teamStr + " \u00a77| " + moneyStr, -120, 5)

    // Line 2: Round timer + phase
    var timerStr = ""
    if (isDead) {
        timerStr = "\u00a77You died. Wait for next round..."
    } else if (g.phase === PHASE_FREEZETIME) {
        var e = (SYS.currentTimeMillis() - g.roundStartTime) / 1000
        var r = Math.max(0, FREEZE_TIME_SECONDS - Math.floor(e))
        timerStr = "\u00a7eFreeze: " + r + "s"
    } else if (g.phase === PHASE_LIVE) {
        var e = (SYS.currentTimeMillis() - g.roundStartTime) / 1000
        var r = Math.max(0, ROUND_TIME_SECONDS - Math.floor(e))
        timerStr = "\u00a7aRound: " + r + "s"
    } else if (g.phase === PHASE_BOMB_PLANTED) {
        var e = (SYS.currentTimeMillis() - g.bombPlantTime) / 1000
        var r = Math.max(0, BOMB_TIMER_SECONDS - Math.floor(e))
        timerStr = "\u00a7c\u00a7lBOMB: " + r + "s"
    } else if (g.phase === PHASE_ROUND_END) {
        timerStr = "\u00a77Round Over"
    } else {
        timerStr = "\u00a77Lobby - " + countTeam(TEAM_CT) + "v" + countTeam(TEAM_T)
    }
    overlay.addLabel(2, timerStr, -120, 15)

    // Line 3: K/D/A
    overlay.addLabel(3, "\u00a77K:\u00a7f" + p.kills + " \u00a77D:\u00a7f" + p.deaths + " \u00a77A:\u00a7f" + p.assists, -120, 25)

    // Line 4: Round / Score
    overlay.addLabel(4, "\u00a77Rd " + g.roundNum + " \u00a7bCT " + g.ctScore + " \u00a77- \u00a7c" + g.tScore + " T", -120, 35)

    // Line 5: Bomb / Defuse Kit indicators
    if (!isDead) {
        var indicators = ""
        if (p.hasBomb) indicators += "\u00a7c\u00a7l[\uD83D\uDCA3] "
        if (p.hasDefuseKit) indicators += "\u00a7b[Kit] "
        if (indicators.length > 0) overlay.addLabel(5, indicators, -120, 45)
    }

    // Line 6: Dead indicator
    if (isDead) {
        overlay.addLabel(6, "\u00a7cYou are dead. \u00a77Spectating...", -120, 55)
    }

    player.showOverlay(overlay)
}

function death(event) {
    var player = event.player
    var uuid   = player.getUUID()
    var g = getGame()
    if (!g) return

    var players = getPlayers(g)
    var pIdx = -1
    for (var i = 0; i < players.length; i++)
        if (players[i].uuid === uuid) { pIdx = i; break }
    if (pIdx < 0) return

    players[pIdx].deaths++
    players[pIdx].isDead = true  // Use boolean flag, keep team value

    // Kill reward
    var source = event.source
    if (source && source.getType() === 1) {
        try {
            var killerUUID = source.getUUID()
            for (var k = 0; k < players.length; k++) {
                if (players[k].uuid === killerUUID) {
                    players[k].kills++
                    players[k].money += KILL_REWARD
                    if (players[k].money > MAX_MONEY) players[k].money = MAX_MONEY
                    var entry = openPlayers[killerUUID]
                    if (entry && entry.player) {
                        giveCoins(entry.player, KILL_REWARD)
                        entry.player.message("\u00a7a+ " + fmt(KILL_REWARD) + " (kill)")
                    }
                    break
                }
            }
        } catch(e) {}
    }

    setPlayers(g, players)
    saveGame(g)
    clearInventory(player)

    // Teleport to spectator spawn
    player.setPosition(SPECTATOR_SPAWN.x, SPECTATOR_SPAWN.y, SPECTATOR_SPAWN.z)
    player.message("\u00a7cYou died! \u00a77Teleported to spectator area. Wait for next round.")

    // Check if this death ends the round
    var winner = checkTeamWinCondition()
    if (winner !== null) {
        var reason = winner === TEAM_T ? "\u00a7cAll CT eliminated" : "\u00a7bAll T eliminated"
        endRound(winner, reason)
    }
}

function respawn(event) {
    var player = event.player
    var uuid   = player.getUUID()
    var g = getGame()
    if (!g) return

    var players = getPlayers(g)
    var pIdx = -1
    for (var i = 0; i < players.length; i++)
        if (players[i].uuid === uuid) { pIdx = i; break }
    if (pIdx < 0) return
    var p = players[pIdx]

    // If dead, teleport to spectator area
    if (p.isDead) {
        player.setPosition(SPECTATOR_SPAWN.x, SPECTATOR_SPAWN.y, SPECTATOR_SPAWN.z)
        return
    }

    if (p.team === TEAM_CT) {
        var spawn = CT_SPAWNS[Math.floor(Math.random() * CT_SPAWNS.length)]
        player.setPosition(spawn.x + 0.5, spawn.y, spawn.z + 0.5)
    } else if (p.team === TEAM_T) {
        var spawn = T_SPAWNS[Math.floor(Math.random() * T_SPAWNS.length)]
        player.setPosition(spawn.x + 0.5, spawn.y, spawn.z + 0.5)
    } else {
        player.setPosition(LOBBY_SPAWN.x, LOBBY_SPAWN.y, LOBBY_SPAWN.z)
    }
}