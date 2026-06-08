// ============================================================================
// BOMB DEFUSE — BOMB SITE BLOCK
// ============================================================================
// Place on Scripted Blocks at bomb sites A and B. Uses world.getStoreddata().
// Requires bomb_defuse_config.js loaded FIRST.
// ============================================================================

var BM = {
    LBL_TITLE:        1,
    LBL_STATUS:       2,
    LBL_TIMER:        3,
    LBL_SITE:         4,
    BTN_PLANT:        10,
    BTN_DEFUSE:       11,
    BTN_CLOSE:        12,
}

function interact(event) {
    openPlayers[event.player.getUUID()] = { player: event.player, API: event.API }
    getGameFromEvent(event)

    var g = getGame()
    if (!g) { event.player.message("\u00a7cGame not started yet!"); return }
    if (g.phase === PHASE_LOBBY || g.phase === PHASE_FREEZETIME) {
        event.player.message("\u00a7cRound hasn't started yet!"); return
    }
    if (g.phase === PHASE_ROUND_END) {
        event.player.message("\u00a7cRound is over!"); return
    }
    openBombGui(event)
}

function openBombGui(event) {
    var player = event.player
    var uuid   = player.getUUID()
    var api    = event.API

    var mp = getMyPlayer(uuid)
    if (!mp) { player.message("\u00a7cYou're not in the game!"); return }

    var blockPos = event.block ? event.block.getPos() : null
    var siteName = "?"
    if (blockPos) {
        var bx = Math.floor(blockPos.getX())
        var bz = Math.floor(blockPos.getZ())
        if (Math.abs(bx - BOMB_SITE_A_POS.x) <= 3 && Math.abs(bz - BOMB_SITE_A_POS.z) <= 3) siteName = "A"
        else if (Math.abs(bx - BOMB_SITE_B_POS.x) <= 3 && Math.abs(bz - BOMB_SITE_B_POS.z) <= 3) siteName = "B"
    }

    var gui = api.createCustomGui(GUI_BOMB, 300, 180, false, player)
    gui.addLabel(BM.LBL_TITLE, "\u00a76\u00a7lBOMB SITE " + siteName, 100, 5, 150, 16)
    gui.addLabel(BM.LBL_SITE, "\u00a77Site: \u00a7e" + siteName, 10, 25, 100, 12)

    if (mp.game.bombPlanted) {
        var elapsed = (SYS.currentTimeMillis() - mp.game.bombPlantTime) / 1000
        var remaining = Math.max(0, BOMB_TIMER_SECONDS - Math.floor(elapsed))
        gui.addLabel(BM.LBL_TIMER, "\u00a7c\u00a7lBOMB ACTIVE \u00a77- \u00a7e" + remaining + "s", 80, 50, 180, 16)

        if (mp.player.team === TEAM_CT) {
            var defuseTime = mp.player.hasDefuseKit ? DEFUSE_TIME_WITH_KIT : DEFUSE_TIME_NO_KIT
            gui.addLabel(BM.LBL_STATUS, "\u00a77Defuse time: \u00a7e" + defuseTime + "s" + (mp.player.hasDefuseKit ? " \u00a7b(Kit)" : ""), 50, 80, 200, 12)
            gui.addButton(BM.BTN_DEFUSE, "\u00a7a\u00a7lDEFUSE", 100, 110, 100, 20)
        } else if (mp.player.team === TEAM_T) {
            gui.addLabel(BM.LBL_STATUS, "\u00a77Bomb is planted! Protect it!", 50, 80, 200, 12)
        } else {
            gui.addLabel(BM.LBL_STATUS, "\u00a77Spectating...", 100, 80, 100, 12)
        }
    } else {
        if (mp.player.team === TEAM_T && mp.player.hasBomb) {
            gui.addLabel(BM.LBL_STATUS, "\u00a77Hold bomb item and click Plant!", 40, 60, 220, 12)
            gui.addButton(BM.BTN_PLANT, "\u00a7c\u00a7lPLANT BOMB", 100, 90, 100, 20)
        } else if (mp.player.team === TEAM_T && !mp.player.hasBomb) {
            gui.addLabel(BM.LBL_STATUS, "\u00a77You don't have the bomb!", 70, 70, 160, 12)
        } else if (mp.player.team === TEAM_CT) {
            gui.addLabel(BM.LBL_STATUS, "\u00a77Protect this site!", 100, 70, 100, 12)
        } else {
            gui.addLabel(BM.LBL_STATUS, "\u00a77Spectating...", 100, 70, 100, 12)
        }
    }

    gui.addButton(BM.BTN_CLOSE, "\u00a77Close", 250, 155, 45, 14)
    player.showCustomGui(gui)
}

function customGuiButton(event) {
    if (event.gui.getID() !== GUI_BOMB) return
    var player = event.player
    var uuid   = player.getUUID()
    var bid    = event.buttonId

    var mp = getMyPlayer(uuid)
    if (!mp) return

    if (bid === BM.BTN_CLOSE) { player.closeGui(); return }

    if (bid === BM.BTN_PLANT) {
        if (mp.game.bombPlanted) { player.message("\u00a7cBomb is already planted!"); return }
        if (mp.player.team !== TEAM_T) { player.message("\u00a7cOnly Terrorists can plant!"); return }
        if (!mp.player.hasBomb) { player.message("\u00a7cYou don't have the bomb!"); return }

        mp.game.bombPlanted = true
        mp.game.bombPlantTime = SYS.currentTimeMillis()
        mp.game.bombSite = (event.block && Math.abs(Math.floor(event.block.getPos().getZ()) - BOMB_SITE_A_POS.z) <= 3) ? "A" : "B"
        mp.player.hasBomb = false
        mp.player.money += BOMB_PLANT_REWARD
        saveMyPlayer(mp)

        // Remove one bomb item from inventory if they have it
        try { player.removeItem(BOMB_ITEM_ID, 1) } catch(e) {}
        giveCoins(player, BOMB_PLANT_REWARD)

        broadcast("\u00a7c\u00a7l[BOMB] " + mp.player.name + " planted the bomb at site " + mp.game.bombSite + "!")
        broadcast("\u00a7eDefuse or explode in " + BOMB_TIMER_SECONDS + " seconds!")
        mp.game.phase = PHASE_BOMB_PLANTED
        saveGame(mp.game)
        player.closeGui()
        return
    }

    if (bid === BM.BTN_DEFUSE) {
        if (!mp.game.bombPlanted) { player.message("\u00a7cNo bomb to defuse!"); return }
        if (mp.player.team !== TEAM_CT) { player.message("\u00a7cOnly CT can defuse!"); return }

        var defuseTime = mp.player.hasDefuseKit ? DEFUSE_TIME_WITH_KIT * 1000 : DEFUSE_TIME_NO_KIT * 1000

        var defusing = getDefusing(mp.game)
        defusing.push({ uuid: uuid, startTime: SYS.currentTimeMillis(), defuseTime: defuseTime, hasKit: mp.player.hasDefuseKit })
        setDefusing(mp.game, defusing)
        saveGame(mp.game)

        player.message("\u00a7aDefusing... stand still! (" + Math.floor(defuseTime/1000) + "s)")
        player.closeGui()
        return
    }
}

function customGuiClosed(event) {}