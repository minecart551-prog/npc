// ============================================================================
// BOMB TEAM — PLAYER SCRIPT
// ============================================================================
// Attach to all players. Handles HUD, chat command !teampc, bomb enforcement,
// and game round management.
// Requires bombteam_config.js loaded FIRST.
// ============================================================================

var playerTimerId = 69421

function init(event) {
    var player = event.player
    openPlayers[player.getUUID()] = { player: player, API: event.API }
    player.getTimers().forceStart(playerTimerId, 5, true)
    removeBombIfNotCriminal(player)
}

function removeBombIfNotCriminal(player) {
    try {
        var g = getGame()
        if (!g) return
        var players = getPlayers(g)
        var uuid = player.getUUID()
        var pIdx = -1
        for (var i = 0; i < players.length; i++) if (players[i].uuid === uuid) { pIdx = i; break }
        if (pIdx < 0) return
        var p = players[pIdx]
        if (p.team !== TEAM_CRIMINAL) {
            var inv = player.getInventory()
            for (var si = 0; si < inv.getSize(); si++) {
                var stack = inv.getSlot(si)
                if (stack && !stack.isEmpty() && stack.getName() === BOMB_ITEM_ID) {
                    inv.setSlot(si, null)
                }
            }
            player.updatePlayerInventory()
        }
    } catch(e) {}
}

function timer(event) {
    if (event.id !== playerTimerId) return
    var player = event.player; var api = event.API; var uuid = player.getUUID()
    openPlayers[uuid] = { player: player, API: api }

    var g = getGame()
    if (!g) { try { player.hideOverlay(OVERLAY_HUD) } catch(e) {} return }

    var players = getPlayers(g)
    var pIdx = -1
    for (var i = 0; i < players.length; i++) if (players[i].uuid === uuid) { pIdx = i; break }
    if (pIdx < 0) { try { player.hideOverlay(OVERLAY_HUD) } catch(e) {} return }

    var p = players[pIdx]

    if (g.phase === PHASE_ACTIVE && g.bombRefreshTime > 0 && getBombRefreshRemaining(g) <= 0) {
        g.bombRefreshTime = -1
        saveGame(g)
        doBombRefresh()
    }

    var overlay = api.createOverlay(OVERLAY_HUD)
    var teamStr = p.team === TEAM_POLICE ? "§bPolice" : (p.team === TEAM_CRIMINAL ? "§cCriminal" : "§7Spectator")
    overlay.addLabel(1, teamStr, -120, -25)

    var bombDeadline = -1
    if (g.phase === PHASE_ACTIVE) {
        var deadlineSeconds = BOMB_REFRESH_MINUTES * 60
        var startTime = g.bombRefreshTime
        if (startTime <= 0) startTime = SYS.currentTimeMillis()
        var elapsed = Math.floor((SYS.currentTimeMillis() - startTime) / 1000)
        bombDeadline = Math.max(0, deadlineSeconds - elapsed)
    }

    var timerStr = ""
    var phaseMsg = ""
    if (g.phase === PHASE_ACTIVE) {
        timerStr = "§aRound Active"
        if (p.team === TEAM_CRIMINAL) {
            if (checkBombRefresh()) {
                phaseMsg = "§cReturn to lobby to get bomb!"
            } else if (bombDeadline >= 0) {
                phaseMsg = "§cPlant bomb! §e(refresh: " + bombDeadline + "s)"
            } else {
                phaseMsg = "§cPlant the bomb!"
            }
        } else if (p.team === TEAM_POLICE) phaseMsg = "§bProtect bomb sites!"
        else phaseMsg = "§7Game in progress"
    } else if (g.phase === PHASE_BOMB_PLANTED) {
        var e = (SYS.currentTimeMillis() - g.bombPlantTime) / 1000
        var r = Math.max(0, BOMB_TIMER_SECONDS - Math.floor(e))
        timerStr = "§c§lBOMB: " + r + "s"
        phaseMsg = "§7Site: §e" + g.bombSiteX + ", " + g.bombSiteY + ", " + g.bombSiteZ
    } else if (g.phase === PHASE_ROUND_END) {
        timerStr = "§7Round Over"
        phaseMsg = "§7Joining locked till reset"
    } else {
        timerStr = "§7Idle - Join via lobby!"
        phaseMsg = "§7Use !teampc for info"
    }
    overlay.addLabel(2, timerStr, -120, -15)
    overlay.addLabel(5, phaseMsg, -120, -5)
    overlay.addLabel(7, "§7Use !teampc to view team info", -120, 5)

    // Show defuse countdown on HUD only for POLICE team
    if (g.phase === PHASE_BOMB_PLANTED && p.team === TEAM_POLICE) {
        var defusingHUD = getDefusing(g)
        for (var dhi = 0; dhi < defusingHUD.length; dhi++) {
            var dInfo = defusingHUD[dhi]
            var dElapsed = (SYS.currentTimeMillis() - dInfo.startTime) / 1000
            var dRemaining = Math.max(0, Math.ceil((dInfo.defuseTime / 1000) - dElapsed))
            overlay.addLabel(20, "§a§l" + dInfo.name + " §7defusing: §e" + dRemaining + "s left", -120, 20)
            break  // only show first defuser
        }
    }


    player.showOverlay(overlay)
}


function chat(event) {
    var player = event.player; var message = event.message
    if (message && message.startsWith("!teampc")) {
        event.setCanceled(true)
        openTeamPCGui(event)
        return
    }
}

function openTeamPCGui(event) {
    var player = event.player; var api = event.API; var uuid = player.getUUID()
    var g = getGame()

    var gui = api.createCustomGui(GUI_TEAMPC, 420, 280, false, player)
    gui.addLabel(1, "§6§lTEAM INFO", 192, 15, 120, 16)

    if (!g) {
        gui.addLabel(2, "§cNo active game.", 160, 60, 120, 12)
        player.showCustomGui(gui); return
    }

    var players = getPlayers(g)
    var pIdx = -1
    for (var i = 0; i < players.length; i++) if (players[i].uuid === uuid) { pIdx = i; break }

    // Fund displays at top (police x=60, criminal x=280)
    gui.addLabel(3, "§bPolice Fund: " + fmt(g.policeFund), 60, 35, 170, 10)
    gui.addLabel(4, "§cCriminal Fund: " + fmt(g.criminalFund), 280, 35, 170, 10)

    // Join buttons (police x=60, criminal x=280)
    gui.addButton(10, "§bJoin Police", 60, 50, 90, 14)
    gui.addButton(11, "§cJoin Criminal", 280, 50, 90, 14)
    gui.addButton(12, "§7Leave", 280, 225, 90, 14)

    var policeTotal = 0
    var criminalTotal = 0
    for (var i = 0; i < players.length; i++) {
        var p = players[i]
        if (p.team === TEAM_POLICE) policeTotal += p.contributed
        else if (p.team === TEAM_CRIMINAL) criminalTotal += p.contributed
    }

    var online = getOnlinePlayerUUIDs()

    // Police player list (left column) - ONLY online players
    var policeLines = ""
    for (var i = 0; i < players.length; i++) {
        var p = players[i]
        if (p.team === TEAM_POLICE && online[p.uuid]) {
            var pct = policeTotal > 0 ? Math.round((p.contributed / policeTotal) * 100) : 0
            policeLines += "§b" + p.name + " §7- " + fmt(p.contributed) + " (" + pct + "%)\n"
        }
    }
    if (policeLines === "") policeLines = "§7No players"
    gui.addLabel(5, policeLines, 60, 70, 170, 120)

    // Criminal player list (right column, +10px right) - ONLY online players
    var criminalLines = ""
    for (var i = 0; i < players.length; i++) {
        var p = players[i]
        if (p.team === TEAM_CRIMINAL && online[p.uuid]) {
            var pct = criminalTotal > 0 ? Math.round((p.contributed / criminalTotal) * 100) : 0
            criminalLines += "§c" + p.name + " §7- " + fmt(p.contributed) + " (" + pct + "%)\n"
        }
    }
    if (criminalLines === "") criminalLines = "§7No players"
    gui.addLabel(50, criminalLines, 280, 70, 170, 120)

    // Team status and fund input (only when in a team)
    if (pIdx >= 0) {
        var p = players[pIdx]
        gui.addLabel(8, p.team === TEAM_POLICE ? "§bPolice §7| Contributed: " + fmt(p.contributed) : "§cCriminal §7| Contributed: " + fmt(p.contributed), 60, 200, 200, 10)
        gui.addLabel(51, "§7Add funds (min " + fmt(MIN_FUND_AMOUNT) + "):", 60, 215, 170, 10)

        gui.addTextField(20, 60, 225, 90, 14).setText("")
        gui.addButton(13, "§aAdd", 155, 225, 50, 14)
    }

    // Balance & Wallet (always shown)
    var balance = getPlayerBalance(g, uuid)
    var wallet = countCoins(player)
    gui.addLabel(60, "§6Balance: §e" + fmt(balance), 60, 245, 200, 10)
    gui.addLabel(61, "§6Wallet: §e" + fmt(wallet), 280, 245, 130, 10)
    if (balance > 0) {
        gui.addButton(62, "§aReceive (§e" + fmt(balance) + "§a)", 60, 258, 200, 14)
    }


    gui.addLabel(7, "§8Phase: " + g.phase, 180, 275, 100, 10)
    player.showCustomGui(gui)
}


function customGuiButton(event) {
    if (event.gui.getID() !== GUI_TEAMPC) return
    var player = event.player; var uuid = player.getUUID()
    var g = getGame(); if (!g) return
    var players = getPlayers(g)
    var pIdx = -1
    for (var i = 0; i < players.length; i++) if (players[i].uuid === uuid) { pIdx = i; break }

    // Receive button (62) - claim balance
    if (event.buttonId === 62) {
        var bal = getPlayerBalance(g, uuid)
        if (bal <= 0) { player.message("§cNo balance to receive!"); return }
        clearBalance(g, uuid)
        giveCoins(player, bal)
        player.message("§aReceived " + fmt(bal) + " into your wallet!")
        openTeamPCGui(event)
        return
    }


    // Join Police button (10)
    if (event.buttonId === 10) {
        if (g.phase === PHASE_BOMB_PLANTED || g.phase === PHASE_ROUND_END) { player.message("§cRound in progress!"); return }
        if (pIdx >= 0) { player.message("§cAlready in a team!"); return }
        if (!removeCoins(player, MIN_FUND_AMOUNT)) { player.message("§cNeed at least " + fmt(MIN_FUND_AMOUNT) + " to join!"); return }
        players.push({ uuid: uuid, name: player.getName(), team: TEAM_POLICE, contributed: MIN_FUND_AMOUNT })
        g.policeFund += MIN_FUND_AMOUNT
        setPlayers(g, players); saveGame(g)
        player.message("§bJoined Police! " + fmt(MIN_FUND_AMOUNT) + " deducted.")
        try { player.setSpawnpoint(POLICE_RESPAWN.x, POLICE_RESPAWN.y, POLICE_RESPAWN.z) } catch(e) {}
        openTeamPCGui(event)
        return
    }

    // Join Criminal button (11)
    if (event.buttonId === 11) {
        if (g.phase === PHASE_BOMB_PLANTED || g.phase === PHASE_ROUND_END) { player.message("§cRound in progress!"); return }
        if (pIdx >= 0) { player.message("§cAlready in a team!"); return }
        if (!removeCoins(player, MIN_FUND_AMOUNT)) { player.message("§cNeed at least " + fmt(MIN_FUND_AMOUNT) + " to join!"); return }
        var firstCriminal = (countTeam(TEAM_CRIMINAL) === 0)
        if (firstCriminal && g.phase === PHASE_IDLE) {
            g.phase = PHASE_ACTIVE
            teamBroadcast("§c[Bomb] First criminal joined! Get bomb from lobby block!")
        }
        players.push({ uuid: uuid, name: player.getName(), team: TEAM_CRIMINAL, contributed: MIN_FUND_AMOUNT })
        g.criminalFund += MIN_FUND_AMOUNT
        if (!g.activeStartTime || g.activeStartTime === 0) {
            g.activeStartTime = SYS.currentTimeMillis()
            g.bombRefreshTime = SYS.currentTimeMillis()
        }
        setPlayers(g, players); saveGame(g)
        player.message("§cJoined Criminal! " + fmt(MIN_FUND_AMOUNT) + " deducted. Get bomb from lobby block!")
        try { player.setSpawnpoint(CRIMINAL_RESPAWN.x, CRIMINAL_RESPAWN.y, CRIMINAL_RESPAWN.z) } catch(e) {}
        openTeamPCGui(event)
        return
    }

    // Leave button (12)
    if (event.buttonId === 12) {
        if (pIdx >= 0) {
            players.splice(pIdx, 1)
            setPlayers(g, players); saveGame(g)
            player.message("§7Left your team.")
        }
        openTeamPCGui(event)
        return
    }

    // Add funds button (13)
    if (event.buttonId === 13) {
        if (pIdx < 0) { player.message("§cJoin a team first!"); return }
        var p = players[pIdx]
        try {
            var tf = event.gui.getComponent(20)
            if (!tf) { player.message("§cCould not read field!"); return }

            var raw = tf.getText()
            if (!raw || raw === "") { player.message("§cEnter an amount!"); return }
            var cleaned = raw.replace("$", "").trim()
            var parsed = parseFloat(cleaned)
            if (isNaN(parsed) || parsed <= 0) { player.message("§cInvalid amount!"); return }
            var cents = Math.round(parsed * 100)
            if (cents < MIN_FUND_AMOUNT) { player.message("§cMin contribution is " + fmt(MIN_FUND_AMOUNT)); return }
            if (!removeCoins(player, cents)) { player.message("§cNot enough coins!"); return }
            p.contributed += cents
            if (p.team === TEAM_POLICE) g.policeFund += cents
            else if (p.team === TEAM_CRIMINAL) g.criminalFund += cents
            setPlayers(g, players); saveGame(g)
            player.message("§aAdded " + fmt(cents) + " to your team fund!")
            openTeamPCGui(event)
        } catch(e) { player.message("§cError: " + e) }
        return
    }
}

function customGuiClosed(event) {}

function tick(event) {
    var g = getGame()
    if (!g) return
    var player = event.player; var uuid = player.getUUID()

    if (g.phase === PHASE_BOMB_PLANTED) {
        var elapsed = (SYS.currentTimeMillis() - g.bombPlantTime) / 1000
        if (BOMB_TIMER_SECONDS - Math.floor(elapsed) <= 0) {
            teamBroadcast("§c§l💥 BOMB EXPLODED! §cCriminals win!")

            try {
                var uuids = Object.keys(openPlayers)
                for (var u = 0; u < uuids.length; u++) {
                    var entry = openPlayers[uuids[u]]
                    if (entry && entry.player) {
                        entry.player.playSound("minecraft:entity.generic.explode", 1.0, 1.0)
                    }
                }
            } catch(e) {}
            endBombTeamRound(TEAM_CRIMINAL, "Bomb detonated!")
            return
        }

        var defusing = getDefusing(g)
        // Cancel defuse if the defuser closed their GUI (police only notification)
        for (var di = 0; di < defusing.length; di++) {
            var def = defusing[di]
            if (def.guiOpen === false) {
                defusing.splice(di, 1)
                setDefusing(g, defusing); saveGame(g)
                policeBroadcast("§7§l[Defuse] " + (def.name || "Police") + " cancelled defusing!")
                continue
            }
            // The defuse countdown is shown on the player's HUD overlay (timer() function)
        }




        // Check for completion
        for (var di = defusing.length - 1; di >= 0; di--) {
            var def = defusing[di]
            if (SYS.currentTimeMillis() - def.startTime >= def.defuseTime) {
                var entry = openPlayers[def.uuid]
                var defuserName = entry && entry.player ? entry.player.getName() : "Police"
                if (entry && entry.player) {
                    entry.player.message("§a§lBOMB DEFUSED!")
                    entry.player.playSound("minecraft:block.note_block.chime", 1.0, 1.0)
                    try { entry.player.closeGui() } catch(e) {}
                }
                g.bombPlanted = false; defusing = []
                teamBroadcast("§a§l[BOMB] " + defuserName + " defused the bomb!")
                setDefusing(g, defusing)
                saveGame(g)
                endBombTeamRound(TEAM_POLICE, "Bomb defused")

                return
            }
        }
    }
}


function endBombTeamRound(winner, reason) {
    var g = getGame()
    if (!g) return
    g.phase = PHASE_ROUND_END
    g.roundStartTime = SYS.currentTimeMillis()

    var players = getPlayers(g)
    var winnerName = winner === TEAM_POLICE ? "§bPolice" : "§cCriminals"
    var loserTeam = winner === TEAM_POLICE ? TEAM_CRIMINAL : TEAM_POLICE
    var loserFund = winner === TEAM_POLICE ? g.criminalFund : g.policeFund
    var winnerFund = winner === TEAM_POLICE ? g.policeFund : g.criminalFund

    teamBroadcast("§6§l" + winnerName + " §6win! (" + reason + "§6)")
    teamBroadcast("§7Winner takes §e" + fmt(loserFund) + " §7from the losing team!")
    teamBroadcast("§eAll players removed from teams. Rejoin via !teampc or lobby block!")

    var totalContributed = 0
    for (var i = 0; i < players.length; i++) {
        if (players[i].team === winner) totalContributed += players[i].contributed
    }

    // Payout winners individually
    for (var i = 0; i < players.length; i++) {
        var p = players[i]
        if (p.team === winner) {
            var pct = totalContributed > 0 ? p.contributed / totalContributed : 0
            var reward = Math.floor(loserFund * pct) + p.contributed
            var winnerEntry = openPlayers[p.uuid]
            if (winnerEntry && winnerEntry.player) {
                // Player is online - pay directly
                giveCoins(winnerEntry.player, reward)
                winnerEntry.player.message("§a+ " + fmt(reward) + " (win payout: " + Math.round(pct * 100) + "% share + your contribution)")
            } else {
                // Player is offline - store in balance for later pickup
                addBalance(g, p.uuid, reward)
            }
        }
    }

    saveGame(g)

    // Reset game state (clear players AFTER messages sent)
    g.phase = PHASE_IDLE
    g.policeFund = 0
    g.criminalFund = 0
    g.bombPlanted = false
    g.bombSiteWorld = ""
    g.bombSiteX = 0
    g.bombSiteY = 0
    g.bombSiteZ = 0
    g.bombPlantTime = 0
    setDefusing(g, [])
    setPlayers(g, [])
    saveGame(g)
}


