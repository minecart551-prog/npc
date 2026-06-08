// ============================================================================
// BOMB TEAM — BOMB SITE BLOCK
// ============================================================================
// Place on Scripted Blocks around the city. Players click while holding
// kubejs:bomb to plant. Police click to defuse.
// Requires bombteam_config.js loaded FIRST.
// ============================================================================

var BM = { LBL_TITLE: 1, LBL_STATUS: 2, LBL_TIMER: 3, BTN_PLANT: 10, BTN_DEFUSE: 11, BTN_CLOSE: 12 }

function interact(event) {
    openPlayers[event.player.getUUID()] = { player: event.player, API: event.API }
    getGameFromEvent(event)
    var g = getGame()
    if (!g || g.phase === PHASE_IDLE) { event.player.message("\u00a7cGame not active! Join via lobby block."); return }
    if (g.phase === PHASE_ROUND_END) { event.player.message("\u00a7cRound is over!"); return }
    openBombGui(event)
}

function openBombGui(event) {
    var player = event.player; var uuid = player.getUUID(); var api = event.API
    var mp = getMyPlayer(uuid)
    if (!mp) { player.message("\u00a7cYou're not in the game!"); return }

    var blockPos = event.block ? event.block.getPos() : null
    var gui = api.createCustomGui(GUI_BOMB, 300, 160, false, player)
    gui.addLabel(BM.LBL_TITLE, "\u00a76\u00a7lBOMB SITE", 110, 5, 120, 16)

    if (mp.game.bombPlanted) {
        var elapsed = (SYS.currentTimeMillis() - mp.game.bombPlantTime) / 1000
        var remaining = Math.max(0, BOMB_TIMER_SECONDS - Math.floor(elapsed))
        gui.addLabel(BM.LBL_TIMER, "§c§lBOMB ACTIVE §7- §e" + remaining + "s", 80, 40, 180, 16)
        // Check if this player is currently defusing
        var defusing = getDefusing(mp.game)
        var myDefuse = null
        for (var di = 0; di < defusing.length; di++) {
            if (defusing[di].uuid === uuid) { myDefuse = defusing[di]; break }
        }
        if (mp.player.team === TEAM_POLICE) {
            if (myDefuse) {
                // Show defuse progress in the GUI
                var defuseElapsed = (SYS.currentTimeMillis() - myDefuse.startTime) / 1000
                var defuseRemaining = Math.max(0, Math.ceil((myDefuse.defuseTime / 1000) - defuseElapsed))
                gui.addLabel(BM.LBL_STATUS, "§a§lDefusing... §7" + defuseRemaining + "s left", 60, 60, 220, 12)
                gui.addLabel(4, "§c§l⚠ STAY ON THIS GUI! ⚠", 60, 80, 220, 14)
                gui.addLabel(5, "§c§lClosing will cancel defuse!", 50, 98, 220, 12)
            } else {
                gui.addLabel(BM.LBL_STATUS, "§7Click to defuse!", 60, 60, 120, 12)
                gui.addButton(BM.BTN_DEFUSE, "§a§lDEFUSE", 100, 75, 100, 20)
                gui.addLabel(4, "§c§l⚠ STAY ON GUI AFTER CLICK! ⚠", 50, 100, 240, 12)
                gui.addLabel(5, "§c§lClosing cancels the defuse!", 50, 115, 240, 12)
            }


        } else {
            if (defusing.length > 0) {
                // Don't show defuse info to criminals - they can guard normally
                gui.addLabel(BM.LBL_STATUS, "§7Bomb is planted! Defend it!", 60, 70, 180, 12)
            } else {
                gui.addLabel(BM.LBL_STATUS, "§7Bomb is planted! Defend it!", 60, 70, 180, 12)
            }
        }
    } else {


        if (mp.player.team === TEAM_CRIMINAL) {
            gui.addLabel(BM.LBL_STATUS, "\u00a77Hold bomb item and click Plant!", 40, 50, 220, 12)
            gui.addButton(BM.BTN_PLANT, "\u00a7c\u00a7lPLANT BOMB", 100, 80, 100, 20)
        } else {
            gui.addLabel(BM.LBL_STATUS, "\u00a77Protect this area!", 90, 60, 120, 12)
        }
    }
    gui.addButton(BM.BTN_CLOSE, "\u00a77Close", 250, 140, 45, 12)
    player.showCustomGui(gui)
}

function customGuiButton(event) {
    if (event.gui.getID() !== GUI_BOMB) return
    var player = event.player; var uuid = player.getUUID(); var bid = event.buttonId
    var mp = getMyPlayer(uuid); if (!mp) return

    if (bid === BM.BTN_CLOSE) { player.closeGui(); return }

    if (bid === BM.BTN_PLANT) {
        if (mp.game.bombPlanted) { player.message("\u00a7cBomb already planted!"); return }
        if (mp.player.team !== TEAM_CRIMINAL) { player.message("\u00a7cOnly criminals can plant!"); return }
        // Check that at least 1 police player is currently online
        if (countOnlineTeam(TEAM_POLICE) < 1) {
            player.message("\u00a7cNeed at least 1 Police player online to plant!")
            return
        }

        // Check if player is holding the bomb item (using main hand)
        var held = player.getMainhandItem()
        if (!held || held.isEmpty() || held.getName() !== BOMB_ITEM_ID) {
            player.message("\u00a7cYou must hold " + BOMB_ITEM_ID + " in your hand to plant!")
            return
        }

        // Store bomb site position from player's position
        var pos = player.getPos()
        var bx = Math.floor(pos.getX())
        var by = Math.floor(pos.getY())
        var bz = Math.floor(pos.getZ())
        mp.game.bombSiteWorld = event.player.getWorld().getName()
        mp.game.bombSiteX = bx
        mp.game.bombSiteY = by
        mp.game.bombSiteZ = bz
        mp.game.bombPlanted = true
        mp.game.bombPlantTime = SYS.currentTimeMillis()
        mp.game.phase = PHASE_BOMB_PLANTED
        // Remove one bomb from the main hand
        try {
            var heldStack = player.getMainhandItem()
            if (heldStack && !heldStack.isEmpty()) {
                var qty = heldStack.getStackSize()
                if (qty > 1) {
                    heldStack.setStackSize(qty - 1)
                } else {
                    heldStack.setStackSize(0)
                }
                player.updatePlayerInventory()
            }
        } catch(e) {}
        saveGame(mp.game)

        teamBroadcast("\u00a7c\u00a7l[BOMB] " + mp.player.name + " planted a bomb at " + bx + ", " + by + ", " + bz + "!")
        teamBroadcast("\u00a7ePolice defuse or explode in " + BOMB_TIMER_SECONDS + " seconds!")
        player.closeGui()
        return
    }

    if (bid === BM.BTN_DEFUSE) {
        if (!mp.game.bombPlanted) { player.message("§cNo bomb to defuse!"); return }
        if (mp.player.team !== TEAM_POLICE) { player.message("§cOnly police can defuse!"); return }
        // Check if player is within 10 blocks of the bomb site
        var defPos = player.getPos()
        var defX = defPos.getX()
        var defY = defPos.getY()
        var defZ = defPos.getZ()
        var dx = defX - mp.game.bombSiteX
        var dy = defY - mp.game.bombSiteY
        var dz = defZ - mp.game.bombSiteZ
        if (Math.abs(dx) > 10 || Math.abs(dy) > 10 || Math.abs(dz) > 10) {
            player.message("§cYou must be within 10 blocks of the bomb site! (§e" + mp.game.bombSiteX + ", " + mp.game.bombSiteY + ", " + mp.game.bombSiteZ + "§c)")
            return
        }

        // Check if already defusing
        var dList = getDefusing(mp.game)
        for (var di = 0; di < dList.length; di++) {
            if (dList[di].uuid === uuid) { player.message("§cAlready defusing!"); return }
        }

        var defuseTime = DEFUSE_TIME_NO_KIT * 1000
        dList.push({ uuid: uuid, name: player.getName(), startTime: SYS.currentTimeMillis(), defuseTime: defuseTime, lastTick: -1, totalSec: Math.floor(defuseTime/1000), guiOpen: true })
        setDefusing(mp.game, dList)
        saveGame(mp.game)
        // Announce to police only - hide from criminals
        policeBroadcast("§e§l[BOMB] " + player.getName() + " is defusing the bomb!")
        // DON'T refresh GUI - that triggers a false close event
        return
    }




}

// Init starts a timer so the bomb block can refresh the defuse GUI every second
var bombBlockTimerId = 7602
function init(event) {
    try { event.block.getTimers().forceStart(bombBlockTimerId, 20, true) } catch(e) {}
}

function tick(event) {
    if (event.id !== bombBlockTimerId) return
    // No periodic refresh needed - GUI updates are handled in the player script
}

function customGuiClosed(event) {
    if (event.gui.getID() !== GUI_BOMB) return
    var player = event.player; var uuid = player.getUUID()
    var g = getGame(); if (!g) return
    // Mark the defuse as guiOpen = false so the player tick can cancel it
    var dList = getDefusing(g)
    for (var di = 0; di < dList.length; di++) {
        if (dList[di].uuid === uuid) {
            dList[di].guiOpen = false
            setDefusing(g, dList); saveGame(g)
            break
        }
    }
}




