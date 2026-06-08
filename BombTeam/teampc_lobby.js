// ============================================================================
// BOMB TEAM — LOBBY BLOCK
// ============================================================================
// Place on a Scripted Block. Players join teams and submit fund amounts here.
// Requires bombteam_config.js loaded FIRST.
// ============================================================================

var L = {
    LBL_TITLE: 1, LBL_POLICE_FUND: 2, LBL_CRIMINAL_FUND: 3,
    LBL_YOUR_TEAM: 4, LBL_FUND_INPUT: 5, LBL_PLAYER_LIST: 6, LBL_STATUS: 7,
    BTN_JOIN_POLICE: 10, BTN_JOIN_CRIMINAL: 11, BTN_SPECTATE: 12,
    BTN_FUND_ADD: 13, BTN_LEAVE: 15, BTN_GET_BOMB: 16,
    TF_FUND_AMOUNT: 20,
}

function interact(event) {
    var player = event.player; var uuid = player.getUUID()
    openPlayers[uuid] = { player: player, API: event.API }
    getGameFromEvent(event)

    var g = getGame()
    if (g) {
        var players = getPlayers(g)
        var pIdx = -1
        for (var i = 0; i < players.length; i++) if (players[i].uuid === uuid) { pIdx = i; break }
        // If criminal player right-clicks and bomb is ready, give it directly
        if (pIdx >= 0 && players[pIdx].team === TEAM_CRIMINAL && checkBombRefresh()) {
            doBombRefresh()
            g.bombRefreshTime = SYS.currentTimeMillis() // Start countdown
            setPlayers(g, players); saveGame(g)
            try { player.giveItem(player.getWorld().createItem(BOMB_ITEM_ID, 1)) } catch(e) {}
            player.message("\u00a7cYou received the bomb! Plant it at a bomb site!")
        }
    }
    openLobbyGui(event)
}

function openLobbyGui(event) {
    var player = event.player; var uuid = player.getUUID(); var api = event.API
    openPlayers[uuid] = { player: player, API: api }
    var g = getGame(); if (!g) return
    var players = getPlayers(g)
    var pIdx = -1
    for (var i = 0; i < players.length; i++) if (players[i].uuid === uuid) { pIdx = i; break }

    var gui = api.createCustomGui(GUI_LOBBY, 420, 280, false, player)
    gui.addLabel(L.LBL_TITLE, "\u00a76\u00a7lTEAM INFO", 192, 15, 120, 16)

    // Fund displays at top (police x=60, criminal x=280)
    gui.addLabel(L.LBL_POLICE_FUND, "\u00a7bPolice Fund: " + fmt(g.policeFund), 60, 35, 170, 10)
    gui.addLabel(L.LBL_CRIMINAL_FUND, "\u00a7cCriminal Fund: " + fmt(g.criminalFund), 280, 35, 170, 10)

    // Join buttons (police x=60, criminal x=280)
    gui.addButton(L.BTN_JOIN_POLICE, "\u00a7bJoin Police", 60, 50, 90, 14)
    gui.addButton(L.BTN_JOIN_CRIMINAL, "\u00a7cJoin Criminal", 280, 50, 90, 14)

    var policeTotal = 0
    var criminalTotal = 0
    for (var i = 0; i < players.length; i++) {
        var p = players[i]
        if (p.team === TEAM_POLICE) policeTotal += p.contributed
        else if (p.team === TEAM_CRIMINAL) criminalTotal += p.contributed
    }

    // Get online player UUIDs once
    var online = getOnlinePlayerUUIDs()

    // Police player list (left column) - ONLY online players
    var policeLines = ""
    for (var i = 0; i < players.length; i++) {
        var p = players[i]
        if (p.team === TEAM_POLICE && online[p.uuid]) {
            var pct = policeTotal > 0 ? Math.round((p.contributed / policeTotal) * 100) : 0
            policeLines += "\u00a7b" + p.name + " \u00a77- " + fmt(p.contributed) + " (" + pct + "%)\n"
        }
    }
    if (policeLines === "") policeLines = "\u00a77No players"
    gui.addLabel(L.LBL_PLAYER_LIST, policeLines, 60, 70, 170, 120)

    // Criminal player list (right column, +10px right) - ONLY online players
    var criminalLines = ""
    for (var i = 0; i < players.length; i++) {
        var p = players[i]
        if (p.team === TEAM_CRIMINAL && online[p.uuid]) {
            var pct = criminalTotal > 0 ? Math.round((p.contributed / criminalTotal) * 100) : 0
            criminalLines += "\u00a7c" + p.name + " \u00a77- " + fmt(p.contributed) + " (" + pct + "%)\n"
        }
    }
    if (criminalLines === "") criminalLines = "\u00a77No players"
    gui.addLabel(50, criminalLines, 280, 70, 170, 120)

    // Team status and fund input (both at x=60)
    if (pIdx >= 0) {
        var p = players[pIdx]
        gui.addLabel(8, p.team === TEAM_POLICE ? "\u00a7bPolice \u00a77| Contributed: " + fmt(p.contributed) : "\u00a7cCriminal \u00a77| Contributed: " + fmt(p.contributed), 60, 200, 200, 10)
        gui.addLabel(L.LBL_FUND_INPUT, "\u00a77Add funds (min " + fmt(MIN_FUND_AMOUNT) + "):", 60, 215, 170, 10)
        gui.addTextField(L.TF_FUND_AMOUNT, 60, 225, 90, 14).setText("")
        gui.addButton(L.BTN_FUND_ADD, "§aAdd", 155, 225, 50, 14)
    }

    // Balance & Wallet (always shown, not just when in a team)
    var balance = getPlayerBalance(g, uuid)
    var wallet = countCoins(player)
    gui.addLabel(60, "§6Balance: §e" + fmt(balance), 60, 245, 200, 10)
    gui.addLabel(61, "§6Wallet: §e" + fmt(wallet), 280, 245, 130, 10)
    if (balance > 0) {
        gui.addButton(62, "§aReceive (§e" + fmt(balance) + "§a)", 60, 258, 200, 14)
    }


    // Leave button (same width as Join Criminal = 90, x=280)
    gui.addButton(L.BTN_LEAVE, "\u00a77Leave", 280, 225, 90, 14)

    gui.addLabel(L.LBL_STATUS, "\u00a78Phase: " + g.phase, 180, 275, 100, 10)
    player.showCustomGui(gui)
}


function customGuiButton(event) {
    if (event.gui.getID() !== GUI_LOBBY) return
    var player = event.player; var uuid = player.getUUID(); var bid = event.buttonId
    openPlayers[uuid] = { player: player, API: event.API }
    getGameFromEvent(event)
    var g = getGame(); if (!g) return
    var players = getPlayers(g)
    var pIdx = -1; for (var i = 0; i < players.length; i++) if (players[i].uuid === uuid) { pIdx = i; break }

    if (bid === L.BTN_JOIN_POLICE) {
        if (g.phase === PHASE_BOMB_PLANTED || g.phase === PHASE_ROUND_END) { player.message("\u00a7cRound in progress!"); return }
        if (pIdx >= 0) { player.message("\u00a7cAlready in a team!"); return }
        // Check coins AND deduct them immediately
        if (!removeCoins(player, MIN_FUND_AMOUNT)) {
            player.message("\u00a7cYou need at least " + fmt(MIN_FUND_AMOUNT) + " to join!")
            return
        }
        players.push({ uuid: uuid, name: player.getName(), team: TEAM_POLICE, contributed: MIN_FUND_AMOUNT })
        g.policeFund += MIN_FUND_AMOUNT
        setPlayers(g, players); saveGame(g)
        player.message("\u00a7b[Team] Joined Police! " + fmt(MIN_FUND_AMOUNT) + " deducted as your contribution.")
        try { player.setSpawnpoint(POLICE_RESPAWN.x, POLICE_RESPAWN.y, POLICE_RESPAWN.z) } catch(e) {}
        openLobbyGui(event); return
    }

    if (bid === L.BTN_JOIN_CRIMINAL) {
        if (g.phase === PHASE_BOMB_PLANTED || g.phase === PHASE_ROUND_END) { player.message("\u00a7cRound in progress!"); return }
        if (pIdx >= 0) { player.message("\u00a7cAlready in a team!"); return }
        // Check coins AND deduct them immediately
        if (!removeCoins(player, MIN_FUND_AMOUNT)) {
            player.message("\u00a7cYou need at least " + fmt(MIN_FUND_AMOUNT) + " to join!")
            return
        }
        var firstCriminal = (countTeam(TEAM_CRIMINAL) === 0)
        if (firstCriminal && g.phase === PHASE_IDLE) {
            g.phase = PHASE_ACTIVE
            teamBroadcast("\u00a7c[Bomb] First criminal joined! Receive the bomb and plant it!")
        }
        players.push({ uuid: uuid, name: player.getName(), team: TEAM_CRIMINAL, contributed: MIN_FUND_AMOUNT })
        g.criminalFund += MIN_FUND_AMOUNT
        if (g.phase === PHASE_IDLE || !g.activeStartTime || g.activeStartTime === 0) {
            g.activeStartTime = SYS.currentTimeMillis()
            g.bombRefreshTime = SYS.currentTimeMillis()
        }
        setPlayers(g, players); saveGame(g)
        player.message("\u00a7c[Team] Joined Criminal! " + fmt(MIN_FUND_AMOUNT) + " deducted as your contribution.")
        try { player.setSpawnpoint(CRIMINAL_RESPAWN.x, CRIMINAL_RESPAWN.y, CRIMINAL_RESPAWN.z) } catch(e) {}
        if (firstCriminal) {
            try { player.giveItem(player.getWorld().createItem(BOMB_ITEM_ID, 1)) } catch(e) {}
            player.message("\u00a7cYou received the bomb! Plant it at a bomb site!")
        }
        openLobbyGui(event); return
    }

    // Spectate option removed

    if (bid === L.BTN_FUND_ADD) {
        if (pIdx < 0) { player.message("\u00a7cJoin a team first!"); return }
        var p = players[pIdx]
        try {
            var tf = event.gui.getComponent(L.TF_FUND_AMOUNT)
            if (!tf) { player.message("\u00a7cCould not read field!"); return }
            var raw = tf.getText()
            if (!raw || raw === "") { player.message("\u00a7cEnter an amount!"); return }
            var cleaned = raw.replace("$", "").trim()
            var parsed = parseFloat(cleaned)
            if (isNaN(parsed) || parsed <= 0) { player.message("\u00a7cInvalid amount!"); return }
            var cents = Math.round(parsed * 100)
            if (cents < MIN_FUND_AMOUNT) { player.message("\u00a7cMin contribution is " + fmt(MIN_FUND_AMOUNT)); return }
            if (!removeCoins(player, cents)) { player.message("\u00a7cNot enough coins!"); return }
            p.contributed += cents
            if (p.team === TEAM_POLICE) g.policeFund += cents
            else if (p.team === TEAM_CRIMINAL) g.criminalFund += cents
            setPlayers(g, players); saveGame(g)
            player.message("\u00a7aAdded " + fmt(cents) + " to your team fund!")
            openLobbyGui(event)
        } catch(e) { player.message("\u00a7cError: " + e) }
        return
    }

    // BTN_GET_BOMB is handled in interact() on right-click
    // This handler is left as a fallback
    if (bid === L.BTN_GET_BOMB) {
        // Bomb is now given on right-click interact(). Just reopen GUI.
        openLobbyGui(event); return
    }

    if (bid === L.BTN_LEAVE) {
        player.closeGui(); removePlayerFromGame(uuid); return
    }

    // Receive button (62) - claim balance
    if (bid === 62) {
        var bal = getPlayerBalance(g, uuid)
        if (bal <= 0) { player.message("§cNo balance to receive!"); return }
        clearBalance(g, uuid)
        giveCoins(player, bal)
        player.message("§aReceived " + fmt(bal) + " into your wallet!")
        openLobbyGui(event)
        return
    }
}


function customGuiClosed(event) {}
