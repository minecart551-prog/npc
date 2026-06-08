// ============================================================================
// BOMB DEFUSE — LOBBY BLOCK
// ============================================================================
// Place this script on a Scripted Block in the lobby room.
// Uses world.getStoreddata() for state (shared across all blocks).
// Requires bomb_defuse_config.js loaded FIRST.
// ============================================================================

var L = {
    LBL_TITLE:        1,
    LBL_CT_COUNT:     2,
    LBL_T_COUNT:      3,
    LBL_YOUR_TEAM:    4,
    LBL_MONEY:        5,
    LBL_STATUS:       6,
    BTN_JOIN_CT:      10,
    BTN_JOIN_T:       11,
    BTN_SPECTATE:     12,
    BTN_READY:        13,
    BTN_START:        14,
    BTN_CLOSE_GUI:    15,
    BTN_LEAVE_GAME:   16,
    LBL_PLAYER_LIST:  20,
}

function interact(event) {
    var player = event.player
    var uuid   = player.getUUID()
    openPlayers[uuid] = { player: player, API: event.API }

    var sd = getGameFromEvent(event)
    rejoinPlayer(uuid, player)
    openLobbyGui(event)
}

function openLobbyGui(event) {
    var player = event.player
    var uuid   = player.getUUID()
    var api    = event.API
    openPlayers[uuid] = { player: player, API: api }

    var g = getGame()
    if (!g) { player.message("\u00a7cError: Game not initialized!"); return }
    var players = getPlayers(g)
    var pIdx = -1
    for (var i = 0; i < players.length; i++)
        if (players[i].uuid === uuid) { pIdx = i; break }

    var gui = api.createCustomGui(GUI_LOBBY, 300, 220, false, player)
    gui.addLabel(L.LBL_TITLE, "\u00a76\u00a7lBOMB DEFUSE", 100, 5, 150, 16)
    gui.addButton(L.BTN_JOIN_CT,  "\u00a7bJoin CT",  30,  30, 100, 20)
    gui.addButton(L.BTN_JOIN_T,   "\u00a7cJoin T",   170, 30, 100, 20)
    gui.addButton(L.BTN_SPECTATE, "\u00a77Spectate", 110, 55, 80, 16)
    gui.addButton(L.BTN_READY,    "\u00a7aReady",    110, 80, 80, 16)
    gui.addButton(L.BTN_START,    "\u00a7eStart Game",100, 110, 100, 18)
    gui.addButton(L.BTN_CLOSE_GUI, "\u00a77Close GUI", 250, 5, 55, 12)
    gui.addButton(L.BTN_LEAVE_GAME, "\u00a7cLeave Match", 240, 195, 55, 12)

    var ctCount = countTeam(TEAM_CT)
    var tCount  = countTeam(TEAM_T)
    gui.addLabel(L.LBL_CT_COUNT, "\u00a7bCT: \u00a7f" + ctCount, 30, 170, 80, 10)
    gui.addLabel(L.LBL_T_COUNT,  "\u00a7cT: \u00a7f" + tCount,  170, 170, 80, 10)

    if (pIdx >= 0) {
        var p = players[pIdx]
        var teamStr = p.team === TEAM_CT ? "\u00a7bCT" : (p.team === TEAM_T ? "\u00a7cT" : "\u00a77Spectator")
        gui.addLabel(L.LBL_YOUR_TEAM, "\u00a77Team: " + teamStr + "  \u00a77Ready: " + (p.ready ? "\u00a7a\u2713" : "\u00a7c\u2717"), 60, 145, 200, 10)
        gui.addLabel(L.LBL_MONEY, "\u00a77Money: " + fmt(p.money), 60, 155, 200, 10)
    }

    var playerListText = ""
    for (var i = 0; i < players.length && i < 10; i++) {
        var p2 = players[i]
        var tc = teamColor(p2.team)
        var readyStr = p2.ready ? "\u00a7a\u2713" : "\u00a77O"
        playerListText += readyStr + " " + tc + p2.name + "\u00a7r\n"
    }
    if (playerListText.length > 0) {
        gui.addLabel(L.LBL_PLAYER_LIST, playerListText, 30, 190, 240, 80)
    }
    gui.addLabel(L.LBL_STATUS, "\u00a77" + g.phase, 110, 130, 100, 10)
    player.showCustomGui(gui)
}

function customGuiButton(event) {
    if (event.gui.getID() !== GUI_LOBBY) return
    var player = event.player
    var uuid   = player.getUUID()
    var bid    = event.buttonId
    openPlayers[uuid] = { player: player, API: event.API }
    getGameFromEvent(event)

    var g = getGame()
    if (!g) return
    var players = getPlayers(g)
    var pIdx = -1
    for (var i = 0; i < players.length; i++)
        if (players[i].uuid === uuid) { pIdx = i; break }

    if (bid === L.BTN_JOIN_CT) {
        if (countTeam(TEAM_CT) >= 5) { player.message("\u00a7cCT team is full!"); return }
        if (pIdx >= 0) {
            players[pIdx].team = TEAM_CT; players[pIdx].ready = false
            if (players[pIdx].money <= 0) { players[pIdx].money = STARTING_MONEY; giveCoins(player, STARTING_MONEY) }
            player.message("\u00a77[Team] You joined \u00a7bCounter-Terrorists\u00a77!")
        } else {
            players.push({ uuid: uuid, name: player.getName(), team: TEAM_CT,
                money: STARTING_MONEY, kills: 0, deaths: 0, assists: 0,
                ready: false, hasDefuseKit: false, hasBomb: false, isDead: false })
            giveCoins(player, STARTING_MONEY)
            player.message("\u00a77[Team] You joined \u00a7bCounter-Terrorists\u00a77!")
        }
        setPlayers(g, players); saveGame(g); openLobbyGui(event); return
    }

    if (bid === L.BTN_JOIN_T) {
        if (countTeam(TEAM_T) >= 5) { player.message("\u00a7cT team is full!"); return }
        if (pIdx >= 0) {
            players[pIdx].team = TEAM_T; players[pIdx].ready = false
            if (players[pIdx].money <= 0) { players[pIdx].money = STARTING_MONEY; giveCoins(player, STARTING_MONEY) }
            player.message("\u00a77[Team] You joined \u00a7cTerrorists\u00a77!")
        } else {
            players.push({ uuid: uuid, name: player.getName(), team: TEAM_T,
                money: STARTING_MONEY, kills: 0, deaths: 0, assists: 0,
                ready: false, hasDefuseKit: false, hasBomb: false, isDead: false })
            giveCoins(player, STARTING_MONEY)
            player.message("\u00a77[Team] You joined \u00a7cTerrorists\u00a77!")
        }
        setPlayers(g, players); saveGame(g); openLobbyGui(event); return
    }

    if (bid === L.BTN_SPECTATE) {
        if (pIdx >= 0) { players[pIdx].team = TEAM_NONE; players[pIdx].ready = false
            setPlayers(g, players); saveGame(g)
            player.message("\u00a77[Team] You are now spectating.") }
        openLobbyGui(event); return
    }

    if (bid === L.BTN_READY) {
        if (pIdx < 0) { player.message("\u00a7cJoin a team first!"); return }
        if (players[pIdx].team === TEAM_NONE) { player.message("\u00a7cJoin a team first!"); return }
        players[pIdx].ready = !players[pIdx].ready
        player.message("\u00a77You are " + (players[pIdx].ready ? "\u00a7aready" : "\u00a7cnot ready") + "\u00a77.")
        setPlayers(g, players); saveGame(g); openLobbyGui(event); return
    }

    if (bid === L.BTN_START) {
        if (players.length < 2) { player.message("\u00a7cNeed at least 2 players!"); return }
        var readyCount = 0
        for (var i = 0; i < players.length; i++)
            if (players[i].ready && players[i].team !== TEAM_NONE) readyCount++
        if (readyCount < 2) { player.message("\u00a7cAt least 2 players must be ready!"); return }
        if (countTeam(TEAM_CT) < 1 || countTeam(TEAM_T) < 1) { player.message("\u00a7cBoth teams need at least 1 player!"); return }
        startGame(g, players, player); return
    }

    if (bid === L.BTN_CLOSE_GUI) { player.closeGui(); return }
    if (bid === L.BTN_LEAVE_GAME) { player.closeGui(); removePlayerFromGame(uuid); return }
}

function startGame(g, players, starter) {
    broadcast("\u00a76[BombDefuse] \u00a7aGAME STARTED!")
    broadcast("\u00a76" + countTeam(TEAM_CT) + " CT vs " + countTeam(TEAM_T) + " T")

    var tPlayers = []
    for (var i = 0; i < players.length; i++)
        if (players[i].team === TEAM_T) tPlayers.push(players[i])
    if (tPlayers.length > 0) {
        var chosen = tPlayers[Math.floor(Math.random() * tPlayers.length)]
        chosen.hasBomb = true
        var entry = openPlayers[chosen.uuid]
        if (entry && entry.player) {
            entry.player.giveItem(entry.player.getWorld().createItem(BOMB_ITEM_ID, 1))
            broadcast("\u00a77[Bomb] " + chosen.name + " has the bomb!")
        }
    }

    setPlayers(g, players)
    g.roundNum = 0; g.ctScore = 0; g.tScore = 0
    var losses = {}; losses[TEAM_CT] = 0; losses[TEAM_T] = 0
    setLosses(g, losses)
    saveGame(g)
    startNewRound()
}

function startNewRound() {
    var g = getGame()
    if (!g) return
    var players = getPlayers(g)

    g.roundNum++
    g.phase = PHASE_FREEZETIME
    g.roundStartTime = SYS.currentTimeMillis()
    g.bombPlanted = false; g.bombSite = ""; g.bombPlantTime = 0
    setDefusing(g, []); g.tickCounter = 0
    saveGame(g)

    broadcast("\u00a76=== Round " + g.roundNum + " ===")

    var ctIdx = 0; var tIdx = 0
    for (var i = 0; i < players.length; i++) {
        var p = players[i]
        players[i].isDead = false // Reset death flag on new round
        var entry = openPlayers[p.uuid]
        if (!entry || !entry.player) continue
        clearInventory(entry.player)

        if (p.team === TEAM_CT && ctIdx < CT_SPAWNS.length) {
            var spawn = CT_SPAWNS[ctIdx++]
            entry.player.setPosition(spawn.x + 0.5, spawn.y, spawn.z + 0.5)
            entry.player.giveItem(entry.player.getWorld().createItem("minecraft:leather_helmet", 1))
            entry.player.giveItem(entry.player.getWorld().createItem("minecraft:leather_chestplate", 1))
            try { entry.player.setSpawnpoint(RESPAWN_POINT.x, RESPAWN_POINT.y, RESPAWN_POINT.z) } catch(e) {}
            playerMsg(p.uuid, "\u00a7b[CT] Defuse the bomb, protect the sites!")
        } else if (p.team === TEAM_T && tIdx < T_SPAWNS.length) {
            var spawn = T_SPAWNS[tIdx++]
            entry.player.setPosition(spawn.x + 0.5, spawn.y, spawn.z + 0.5)
            entry.player.giveItem(entry.player.getWorld().createItem("minecraft:leather_helmet", 1))
            entry.player.giveItem(entry.player.getWorld().createItem("minecraft:leather_chestplate", 1))
            try { entry.player.setSpawnpoint(RESPAWN_POINT.x, RESPAWN_POINT.y, RESPAWN_POINT.z) } catch(e) {}
            playerMsg(p.uuid, "\u00a7c[T] Plant the bomb at A or B!")
        } else if (p.team === TEAM_NONE) {
            entry.player.setPosition(LOBBY_SPAWN.x, LOBBY_SPAWN.y, LOBBY_SPAWN.z)
        }
    }

    giveBombToRandomT()
    saveGame(g)
    broadcast("\u00a7e[Round] Freeze time! " + FREEZE_TIME_SECONDS + " seconds until round starts.")
}

function giveBombToRandomT() {
    var g = getGame()
    if (!g) return
    var players = getPlayers(g)
    for (var i = 0; i < players.length; i++) players[i].hasBomb = false
    var tPlayers = []
    for (var i = 0; i < players.length; i++)
        if (players[i].team === TEAM_T) tPlayers.push(i)
    if (tPlayers.length === 0) return
    var chosenIdx = tPlayers[Math.floor(Math.random() * tPlayers.length)]
    players[chosenIdx].hasBomb = true
    var entry = openPlayers[players[chosenIdx].uuid]
    if (entry && entry.player) {
        entry.player.giveItem(entry.player.getWorld().createItem(BOMB_ITEM_ID, 1))
        broadcast("\u00a77[Bomb] " + players[chosenIdx].name + " has the bomb!")
    }
    setPlayers(g, players); saveGame(g)
}

function tick(event) {
    var g = getGame()
    if (!g || g.phase === PHASE_LOBBY) return
    g.tickCounter++

    if (g.phase === PHASE_FREEZETIME) {
        var e = (SYS.currentTimeMillis() - g.roundStartTime) / 1000
        if (FREEZE_TIME_SECONDS - Math.floor(e) <= 0) {
            g.phase = PHASE_LIVE; g.roundStartTime = SYS.currentTimeMillis()
            saveGame(g)
            broadcast("\u00a7e[Round] \u00a7aROUND START! \u00a77Go go go!")
        }
        return
    }

    if (g.phase === PHASE_LIVE) {
        var e = (SYS.currentTimeMillis() - g.roundStartTime) / 1000
        var remaining = ROUND_TIME_SECONDS - Math.floor(e)
        if (remaining <= 0) { endRound(TEAM_T, "Time ran out!"); return }
        if (g.tickCounter % 20 === 0) {
            var winner = checkTeamWinCondition()
            if (winner !== null) {
                endRound(winner, winner === TEAM_T ? "\u00a7cAll CT eliminated" : "\u00a7bAll T eliminated")
                return
            }
        }
        return
    }

    if (g.phase === PHASE_BOMB_PLANTED) {
        var e = (SYS.currentTimeMillis() - g.bombPlantTime) / 1000
        if (BOMB_TIMER_SECONDS - Math.floor(e) <= 0) { bombExplode(); return }

        // Check defuse players (lobby block tick handles this)
        var defusing = getDefusing(g)
        for (var di = defusing.length - 1; di >= 0; di--) {
            var def = defusing[di]
            if (SYS.currentTimeMillis() - def.startTime >= def.defuseTime) {
                var entry = openPlayers[def.uuid]
                if (entry && entry.player) {
                    entry.player.message("\u00a7a\u00a7lBOMB DEFUSED!")
                    entry.player.playSound("minecraft:block.note_block.chime", 1.0, 1.0)
                }
                g.bombPlanted = false; defusing = []
                broadcast("\u00a7a\u00a7l[BOMB] Counter-Terrorists defused the bomb!")
                saveGame(g)
                endRound(TEAM_CT, "Bomb defused")
                return
            }
        }
        return
    }

    if (g.phase === PHASE_ROUND_END) {
        var e = (SYS.currentTimeMillis() - g.roundStartTime) / 1000
        if (e > 5) startNewRound()
        return
    }
}

function customGuiClosed(event) {
    // Player closed the GUI - that's fine, they can reopen
}