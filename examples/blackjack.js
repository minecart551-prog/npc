// COIN CURRENCY:
//   stone_coin = 1¢   coal_coin = $1 (100¢)   emerald_coin = $100 (10,000¢)
// ============================================================================

var SYS = Java.type("java.lang.System")

// ============================================================================
// CONFIG
// ============================================================================
var BET_OPTIONS          = [10, 50, 200]   // in cents: 10¢, 50¢, $2.00
var MAX_PLAYERS          = 5
var CHALLENGE_TIMEOUT_MS = 30000
var TURN_TIMEOUT_MS      = 15000

var STONE_TO_COAL   = 100
var COAL_TO_EMERALD = 100
var COIN_STONE      = "coins:stone_coin"
var COIN_COAL       = "coins:coal_coin"
var COIN_EMERALD    = "coins:emerald_coin"

var SUITS = ["♠", "♥", "♦", "♣"]
var RANKS = ["A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"]

var TEX_CARD_BACK   = "minecraft:textures/block/black_wool.png"
var TEX_CARD_BLACK  = "minecraft:textures/block/white_wool.png"
var TEX_CARD_RED    = "minecraft:textures/block/white_wool.png"

// ============================================================================
// ANIMATION — Adjust these to change card dealing animation
// ============================================================================
var ANIM_SOUND          = "minecraft:item.bundle.remove_one"  // sound played per card dealt
var ANIM_TIMER_INTERVAL = 10                          // game ticks between cards (20 = 1 sec)

// ============================================================================
// GUI
// ============================================================================
var GUI_MAIN    = 7300
var GUI_W       = 420
var GUI_H       = 330

var cid = 9000
var maxCid = 9000
function nextCid() { return cid++ }
function resetCid() { cid = 9000 }

var C = {
    LBL_DEALER_TITLE: 1,
    LBL_DEALER_HAND:  2,
    LBL_STATUS:       3,
    LBL_POT:          4,
    LBL_YOUR_HAND:    5,
    BTN_10:        20,
    BTN_50:        21,
    BTN_200:       22,
    BTN_HIT:       30,
    BTN_STAND:     31,
    BTN_DOUBLE:    32,
    BTN_START:     33,
    BTN_CLOSE:     34,
    BTN_NEW_ROUND: 35,
    BTN_LEAVE:     36,
    BTN_CUSTOM_BET: 37,
    TF_CUSTOM_BET:  38
}

var npcUuid = null
var game = null
var openPlayers = {}

// Animation state
var animating = false
var animTick = 0
var animNpc = null

// ============================================================================
// HELPERS
// ============================================================================
function getNpc(world) {
    if (!npcUuid) return null
    return world.getEntity(npcUuid)
}

// ============================================================================
// CARD FUNCTIONS
// ============================================================================
function cardString(card) {
    if (card.hidden) return "§8[?]"
    var suitColor = (card.suit === 0 || card.suit === 3) ? "§0" : "§c"
    return suitColor + RANKS[card.rank] + SUITS[card.suit] + "§r"
}

function cardValue(rank) {
    if (rank === 0) return 11
    if (rank >= 10) return 10
    return rank + 1
}

function handValue(hand) {
    var total = 0
    var aces = 0
    for (var i = 0; i < hand.length; i++) {
        if (hand[i].hidden) continue
        var v = cardValue(hand[i].rank)
        total += v
        if (hand[i].rank === 0) aces++
    }
    while (total > 21 && aces > 0) { total -= 10; aces-- }
    return total
}

function createDeck() {
    var deck = []
    for (var s = 0; s < 4; s++)
        for (var r = 0; r < 13; r++)
            deck.push({ rank: r, suit: s, hidden: false })
    for (var i = deck.length - 1; i > 0; i--) {
        var j = Math.floor(Math.random() * (i + 1))
        var tmp = deck[i]; deck[i] = deck[j]; deck[j] = tmp
    }
    return deck
}

function drawCard() {
    if (!game || !game.deck || game.deck.length === 0) {
        if (game) game.deck = createDeck()
        else return null
    }
    return game.deck.pop()
}

function isBlackjack(hand) {
    return hand.length === 2 && handValue(hand) === 21 && !hand[0].hidden && !hand[1].hidden
}

// ============================================================================
// COINS
// ============================================================================
function countCoins(player) {
    var total = 0
    var inv   = player.getInventory()
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
    var rem   = amount
    var inv   = player.getInventory()
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
            var qty  = s.getStackSize()
            var unit = STONE_TO_COAL * COAL_TO_EMERALD
            var val  = qty * unit
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
    var rem  = amount
    if (rem <= 0) return
    var world = player.getWorld()
    var unit  = STONE_TO_COAL * COAL_TO_EMERALD
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

function fmt(amount) {
    if (amount < STONE_TO_COAL) return "§e" + amount + "¢"
    var dollars = Math.floor(amount / STONE_TO_COAL)
    var cents = amount % STONE_TO_COAL
    if (cents === 0) return "§e$" + dollars + ".00"
    if (cents < 10) return "§e$" + dollars + ".0" + cents
    return "§e$" + dollars + "." + cents
}

function myPlayerIdx(uuid) {
    if (!game) return -1
    for (var i = 0; i < game.players.length; i++)
        if (game.players[i].uuid === uuid) return i
    return -1
}

function isMyTurn(uuid) {
    if (!game || game.phase !== "playing") return false
    return game.currentPlayerIdx >= 0 && game.players[game.currentPlayerIdx].uuid === uuid
}

// ============================================================================
// CARD RENDERER
// ============================================================================
function addCardToGui(gui, x, y, card, faceDown) {
    var cw = 32, ch = 42
    var id = nextCid()
    if (faceDown) { gui.addTexturedRect(id, TEX_CARD_BACK, x, y, cw, ch); return }
    if (!card) return
    var isRed = (card.suit === 1 || card.suit === 2)
    var color = isRed ? "§c" : "§0"
    gui.addTexturedRect(id, TEX_CARD_RED, x, y, cw, ch)
    gui.addColoredLine(nextCid(), x, y, x+cw, y, 0x333333, 1)
    gui.addColoredLine(nextCid(), x, y+ch, x+cw, y+ch, 0x333333, 1)
    gui.addColoredLine(nextCid(), x, y, x, y+ch, 0x333333, 1)
    gui.addColoredLine(nextCid(), x+cw, y, x+cw, y+ch, 0x333333, 1)
    var rankOffTop = (RANKS[card.rank] === "10") ? -1 : 0
    var rankOffBottom = (RANKS[card.rank] === "10") ? -3 : 0
    gui.addLabel(nextCid(), color + RANKS[card.rank], x+1+rankOffTop, y+2, 14, 10)
    gui.addLabel(nextCid(), color + SUITS[card.suit], x+1, y+11, 12, 10)
    gui.addLabel(nextCid(), color + SUITS[card.suit], x+cw-10, y+ch-18, 12, 10)
    gui.addLabel(nextCid(), color + RANKS[card.rank], x+cw-8+rankOffBottom, y+ch-9, 12, 10)
    gui.addLabel(nextCid(), color + SUITS[card.suit] + "§r", x+12.5, y+17, 18, 16)
}

// ============================================================================
// BUILD GUI on any gui object (new or existing)
// ============================================================================
function buildGuiOn(gui, player, api) {
    var uuid   = player.getUUID()
    var bal    = countCoins(player)
    var pIdx   = myPlayerIdx(uuid)
    var playerSeats = [
        { x: -80,  y: 60  }, { x: 30,  y: 160 },
        { x: 177,  y: 190 }, { x: 330, y: 160 },
        { x: 440,  y: 60  }
    ]
    var CARD_GAP = 35, statusY = 110, cy = 130

    gui.addLabel(nextCid(), "§7Balance: " + fmt(bal), 180, 300, 120, 10)
    gui.addLabel(C.LBL_DEALER_TITLE, "§f§lDEALER", 191, 6, 60, 10)

    var dealerX = 209
    if (game && game.dealerHand && game.dealerHand.length > 0) {
        var showAll = (game.phase === "result" || game.phase === "dealer")
        var startX = dealerX - Math.floor((game.dealerHand.length * CARD_GAP) / 2)
        for (var d = 0; d < game.dealerHand.length; d++) {
            var card = game.dealerHand[d]
            if (!card) continue
            var isHidden = card.hidden && !showAll
            addCardToGui(gui, startX + d * CARD_GAP, 16, card, isHidden)
        }
        if (showAll) gui.addLabel(C.LBL_DEALER_HAND, "§7Value: §f" + handValue(game.dealerHand), 191, 62, 120, 10)
    }

    for (var s = 0; s < MAX_PLAYERS; s++) {
        var seat = playerSeats[s]
        gui.addLabel(nextCid(), "§8○", seat.x + 30, seat.y - 8, 12, 12)
    }

    if (game) {
        for (var i = 0; i < game.players.length; i++) {
            var p = game.players[i]
            var seat = playerSeats[i]
            var sx = seat.x - 20, sy = seat.y
            var isActive  = (i === game.currentPlayerIdx && game.phase === "playing")
            var isBust    = (p.state === "bust")
            var isStood   = (p.state === "stood" || p.state === "blackjack")
            var nameColor = isBust ? "§c" : (isStood ? "§a" : (isActive ? "§e" : "§f"))
            gui.addLabel(nextCid(), nameColor + "§l" + p.name, sx, sy, 80, 10)
            if (p.bet > 0) gui.addLabel(nextCid(), "§7Bet: " + fmt(p.bet), sx, sy + 10, 80, 10)
            if (p.hand && p.hand.length > 0) {
                var handStartX = sx, handStartY = sy + 22
                for (var c = 0; c < p.hand.length; c++) {
                    if (p.hand[c]) addCardToGui(gui, handStartX + c * CARD_GAP, handStartY, p.hand[c], false)
                }
                var val = handValue(p.hand)
                gui.addLabel(nextCid(), "§7Value: §f" + val, sx, handStartY + 46, 60, 10)
                if (p.state === "bust") gui.addLabel(nextCid(), "§c§lBUST", sx + 48, handStartY + 46, 40, 10)
                if (p.state === "blackjack") gui.addLabel(nextCid(), "§6§lBJ!", sx + 48, handStartY + 46, 40, 10)
            }
            if (p.result) {
                var isBJ = (p.state === "blackjack" && p.result === "win")
                var resColor = (p.result === "win") ? "§a" : (p.result === "push") ? "§e" : "§c"
                var resText = isBJ ? "BJ 3:2 + " + fmt(Math.floor(p.bet * 1.5))
                             : (p.result === "win") ? "WIN + " + fmt(p.bet)
                             : (p.result === "push") ? "PUSH" : "LOSE - " + fmt(p.bet)
                gui.addLabel(nextCid(), resColor + "§l" + resText, sx, sy + 80, 100, 10)
            }
            if (isActive) gui.addLabel(nextCid(), "§e◄ YOUR TURN", sx, sy + 80, 80, 10)
        }
    }

    if (game) {
        if (game.phase === "betting") gui.addLabel(C.LBL_STATUS, "§6§lBETTING PHASE — Place your bet!", 140, statusY, 260, 12)
        else if (game.phase === "animating") gui.addLabel(C.LBL_STATUS, "§e§lDealing cards...", 174, statusY, 160, 12)
        else if (game.phase === "playing") {
            var cur = game.players[game.currentPlayerIdx]
            var turnText = (cur.uuid === uuid) ? "§e§lYOUR TURN" : "§7" + cur.name + "'s turn..."
            gui.addLabel(C.LBL_STATUS, turnText, 184, statusY + 10, 300, 12)
            gui.addLabel(C.LBL_POT, "§7Pot: §f" + fmt(totalPot()), -50, 266, 100, 12)
        } else if (game.phase === "dealer") gui.addLabel(C.LBL_STATUS, "§c§lDealer's turn...", 120, statusY, 200, 12)
        else if (game.phase === "result") {
            gui.addLabel(C.LBL_STATUS, "§a§lRound Over!", 186, statusY + 13, 120, 12)
            gui.addLabel(C.LBL_POT, "§7Pot: §f" + fmt(totalPot()), -50, 266, 100, 12)
        }
    } else {
        gui.addLabel(C.LBL_STATUS, "§7No active game. Place a bet to start!", 140, statusY, 220, 12)
    }

    // During animation, hide action buttons
    if (!game || game.phase === "idle" || game.phase === "betting") {
        var bw = 58
        gui.addButton(C.BTN_10,  fmt(BET_OPTIONS[0]), 112, cy - 2, bw, 16)
        gui.addButton(C.BTN_50,  fmt(BET_OPTIONS[1]), 182, cy - 2, bw, 16)
        gui.addButton(C.BTN_200, fmt(BET_OPTIONS[2]), 252, cy - 2, bw, 16)
        gui.addLabel(nextCid(), "§7Custom($)", 123, cy + 25, 50, 10)
        gui.addTextField(C.TF_CUSTOM_BET, 170, cy + 23, 78, 14).setText("")
        gui.addButton(C.BTN_CUSTOM_BET, "§aBet", 256, cy + 23, 35, 14)
        if (game && game.players.length > 0) gui.addButton(C.BTN_START, "§a§lDeal", 340, cy, 60, 16)
    } else if (game.phase === "playing") {
        if (isMyTurn(uuid) && pIdx >= 0) {
            var myP = game.players[pIdx]
            gui.addButton(C.BTN_HIT,   "§a§lHIT",   114, cy+10, 58, 18)
            gui.addButton(C.BTN_STAND, "§e§lSTAND", 180, cy+10, 58, 18)
            if (myP.hand.length === 2 && bal >= myP.bet) gui.addButton(C.BTN_DOUBLE, "§6§lDOUBLE", 246, cy+10, 58, 18)
            gui.addLabel(C.LBL_YOUR_HAND, "§7Your hand value: §f" + handValue(myP.hand), -50, 255, 140, 10)
        } else gui.addLabel(C.LBL_YOUR_HAND, "§7Waiting...", 180, cy, 100, 10)
    } else if (game.phase === "dealer") gui.addLabel(C.LBL_YOUR_HAND, "§7Dealer drawing...", 140, cy, 120, 10)
    else if (game.phase === "result") {
        gui.addButton(C.BTN_NEW_ROUND, "§a§lNew Round", 125, cy+10, 80, 18)
        gui.addButton(C.BTN_CLOSE, "§7Close Table", 215, cy+10, 80, 18)
    }
    gui.addButton(C.BTN_LEAVE, "§7Leave", GUI_W - 50, 2, 46, 12)
}

// ============================================================================
// Remove all components that will be rebuilt
// ============================================================================
function removeGuiComponents(gui) {
    var stableIds = [1, 2, 3, 4, 5, 20, 21, 22, 30, 31, 32, 33, 34, 35, 36, 37, 38]
    for (var s = 0; s < stableIds.length; s++) {
        try { gui.removeComponent(stableIds[s]); } catch(e) {}
    }
    // Remove ALL dynamic components with IDs from 9000 to 9999.
    // This covers all card components (textured rects, colored lines, labels, text fields)
    // that were added via nextCid() during any round, regardless of per-player maxCid tracking.
    for (var d = 9000; d < 9999; d++) {
        try { gui.removeComponent(d); } catch(e) {}
    }
}

// ============================================================================
// Safe update helper
// ============================================================================
function safeUpdate(gui) {
    if (!gui) return
    try { gui.update() } catch(e) {}
}

// ============================================================================
// OPEN / REFRESH GUI
// ============================================================================
function openGui(event) {
    var player = event.player
    var uuid   = player.getUUID()
    var api    = event.API

    var gui = api.createCustomGui(GUI_MAIN, GUI_W, GUI_H, false, player)
    resetCid()
    buildGuiOn(gui, player, api)
    maxCid = cid
    player.showCustomGui(gui)
    openPlayers[uuid] = { player: player, API: api, gui: gui }
}

function refreshAllGuis(excludeUuid) {
    var uuids = Object.keys(openPlayers)
    for (var i = 0; i < uuids.length; i++) {
        if (excludeUuid && uuids[i] === excludeUuid) continue
        var entry = openPlayers[uuids[i]]
        if (entry && entry.player && entry.API && entry.gui) {
            try {
                removeGuiComponents(entry.gui)
                resetCid()
                buildGuiOn(entry.gui, entry.player, entry.API)
                maxCid = cid
                entry.gui.update()
            } catch(e) {}
        }
    }
}

function broadcast(msg) {
    var uuids = Object.keys(openPlayers)
    for (var i = 0; i < uuids.length; i++) {
        var entry = openPlayers[uuids[i]]
        if (entry && entry.player) entry.player.message(msg)
    }
}

// ============================================================================
// ANIMATION TIMER
// ============================================================================
function onDealTimer(npc) {
    if (!game || game.phase !== "animating") {
        if (npc) npc.timers.stop(1)
        animating = false
        return
    }

    animTick++

    var animDelay = 8  // ticks between each card
    var totalPlayers = game.players.length

    // Round 1: deal first card to each player, plus dealer's up card = totalPlayers + 1 steps
    // Round 2: deal second card to each player, plus dealer's down card = totalPlayers + 1 steps
    // Total = 2 * (totalPlayers + 1) steps

    var totalSteps = 2 * (totalPlayers + 1)
    var step = animTick - 1

    if (step >= totalSteps) {
        // Animation complete — finalize
        npc.timers.stop(1)
        animating = false
        doStartAfterAnim()
        return
    }

    // Determine which card to deal this tick
    var cardsPerRound = totalPlayers + 1
    var round = Math.floor(step / cardsPerRound)  // 0 or 1
    var idxInRound = step % cardsPerRound          // 0..totalPlayers

    if (round === 0) {
        // First card each
        if (idxInRound < totalPlayers) {
            // Deal to player
            var card = drawCard()
            if (card) game.players[idxInRound].hand.push(card)
            broadcast("§7[Dealer] Dealing to " + game.players[idxInRound].name + "...")
        } else {
            // Dealer's first card (face up)
            var card = drawCard()
            if (card) { card.hidden = false; game.dealerHand[0] = card }
            broadcast("§7[Dealer] Dealer's up card...")
        }
    } else {
        // Second card each
        if (idxInRound < totalPlayers) {
            var card = drawCard()
            if (card) game.players[idxInRound].hand.push(card)
        } else {
            // Dealer's second card (face down)
            var card = drawCard()
            if (card) { card.hidden = true; game.dealerHand[1] = card }
            broadcast("§7[Dealer] Dealer's hole card...")
        }
    }

    // Play card flip sound at the NPC position
    if (npc) {
        try {
            var npcWorld = npc.getWorld()
            var npcPos = npc.getPos()
            npcWorld.playSoundAt(npcPos, ANIM_SOUND, 0.5, 1.0)
        } catch(e) {}
    }

    // Refresh all GUIs to show the new card
    refreshAllGuis()
}

function doStartAfterAnim() {
    if (!game) return

    for (var p = 0; p < game.players.length; p++)
        if (isBlackjack(game.players[p].hand)) game.players[p].state = "blackjack"

    game.dealerBlackjack = isBlackjack(game.dealerHand)
    if (game.dealerBlackjack) {
        game.dealerHand[1].hidden = false
        for (var p = 0; p < game.players.length; p++) {
            if (game.players[p].state === "blackjack") game.players[p].result = "push"
            else { game.players[p].state = "bust"; game.players[p].result = "lose" }
        }
        finishDealerTurn()
        return
    }

    var allDone = true
    for (var p = 0; p < game.players.length; p++)
        if (game.players[p].state === "playing") { allDone = false; break }
    if (allDone) { finishDealerTurn(); return }

    game.phase = "playing"
    game.currentPlayerIdx = 0
    game.turnStartedAt = SYS.currentTimeMillis()
    broadcast("§e[Blackjack] Cards dealt! Good luck!")
    refreshAllGuis()
}

// ============================================================================
// TOTAL POT
// ============================================================================
function totalPot() {
    if (!game) return 0
    var total = 0
    for (var i = 0; i < game.players.length; i++)
        if (game.players[i].bet > 0) total += game.players[i].bet * 2
    return total
}

// ============================================================================
// GAME LOGIC
// ============================================================================
function prepareNewGame() {
    // Called from doStart — sets up deck and hands, starts animation
    if (!game || game.phase !== "betting") return
    if (game.players.length === 0) { game = null; return }

    var activePlayers = []
    for (var i = 0; i < game.players.length && activePlayers.length < MAX_PLAYERS; i++) {
        if (game.players[i].bet > 0) {
            var pEntry = openPlayers[game.players[i].uuid]
            if (!pEntry || !pEntry.player || !removeCoins(pEntry.player, game.players[i].bet)) {
                broadcast("§c[Blackjack] " + game.players[i].name + " no longer has enough coins! Skipped.")
                continue
            }
            if (pEntry && pEntry.player) pEntry.player.message("§c[Blackjack] " + fmt(game.players[i].bet) + " deducted from your coins!")
            game.players[i].hand = []
            game.players[i].state = "playing"
            activePlayers.push(game.players[i])
        }
    }
    if (activePlayers.length === 0) { game = null; return }
    game.players = activePlayers
    game.dealerHand = [null, null]
    game.deck = createDeck()
    game.dealerBlackjack = false

    // Set to animating phase
    game.phase = "animating"
    animTick = 0
    animating = true

    // Try to get NPC from any open player's world
    var uuids = Object.keys(openPlayers)
    for (var u = 0; u < uuids.length; u++) {
        var entry = openPlayers[uuids[u]]
        if (entry && entry.player && entry.API) {
            try {
                var world = entry.player.getWorld()
                var npc = world.getEntity(npcUuid)
                if (npc) {
                    animNpc = npc
                    npc.timers.stop(1)
                    npc.timers.start(1, ANIM_TIMER_INTERVAL, true)
                    break
                }
            } catch(e) {}
        }
    }

    broadcast("§e[Blackjack] Dealing cards...")
    refreshAllGuis()
}

function startNewGame() {
    if (!game || game.phase !== "betting") return
    if (game.players.length === 0) { game = null; return }
    // Check if animation should run (more than 1 player for visual effect)
    prepareNewGame()
}

function doPlaceBet(event, amount) {
    var player = event.player; var uuid = player.getUUID(); var bal = countCoins(player)
    if (game && game.players.length >= MAX_PLAYERS && myPlayerIdx(uuid) < 0) { player.message("§c[Blackjack] Table is full!"); openGui(event); return }
    if (bal < amount) { player.message("§c[Blackjack] Not enough coins! Need " + fmt(amount)); openGui(event); return }
    if (!game) game = { phase: "betting", deck: [], dealerHand: [], players: [], currentPlayerIdx: -1, createdAt: SYS.currentTimeMillis(), turnStartedAt: 0 }
    if (game.phase !== "betting" && game.phase !== "idle") { player.message("§c[Blackjack] Round in progress!"); openGui(event); return }
    if (game.phase === "idle") { game.phase = "betting"; game.players = []; game.dealerHand = []; game.currentPlayerIdx = -1; game.createdAt = SYS.currentTimeMillis() }
    var pIdx = myPlayerIdx(uuid)
    if (pIdx >= 0) {
        var p = game.players[pIdx]
        if (p.bet > 0) { player.message("§c[Blackjack] Already bet " + fmt(p.bet) + "!"); openGui(event); return }
        p.bet = amount; player.message("§a[Blackjack] You pledged " + fmt(amount) + "!"); openGui(event); refreshAllGuis(uuid); return
    }
    game.players.push({ uuid: uuid, name: player.getName(), hand: [], bet: amount, state: "waiting_bet", result: null })
    player.message("§a[Blackjack] You pledged " + fmt(amount) + "!"); openGui(event); refreshAllGuis(uuid)
}

function doCustomBet(event) {
    var player = event.player; var uuid = player.getUUID(); var gui = event.gui
    try {
        var textField = gui.getComponent(C.TF_CUSTOM_BET)
        if (!textField) { player.message("§c[Blackjack] Could not read custom bet field!"); return }
        var raw = textField.getText()
        if (!raw || raw === "") { player.message("§c[Blackjack] Enter a bet amount!"); return }
        var cleaned = raw.replace("$", "").trim()
        var parsed = parseFloat(cleaned)
        if (isNaN(parsed) || parsed <= 0) { player.message("§c[Blackjack] Invalid amount: " + raw); return }
        var cents = Math.round(parsed * 100)
        if (cents < 1) { player.message("§c[Blackjack] Minimum bet is 1¢!"); return }
        if (cents > 10000) { player.message("§c[Blackjack] Max bet is $100.00!"); return }
        var amount = cents; var bal = countCoins(player)
        if (game && game.players.length >= MAX_PLAYERS && myPlayerIdx(uuid) < 0) { player.message("§c[Blackjack] Table is full!"); openGui(event); return }
        if (bal < amount) { player.message("§c[Blackjack] Not enough coins! Need " + fmt(amount)); openGui(event); return }
        if (!game) game = { phase: "betting", deck: [], dealerHand: [], players: [], currentPlayerIdx: -1, createdAt: SYS.currentTimeMillis(), turnStartedAt: 0 }
        if (game.phase !== "betting" && game.phase !== "idle") { player.message("§c[Blackjack] Round in progress!"); openGui(event); return }
        if (game.phase === "idle") { game.phase = "betting"; game.players = []; game.dealerHand = []; game.currentPlayerIdx = -1; game.createdAt = SYS.currentTimeMillis() }
        var pIdx = myPlayerIdx(uuid)
        if (pIdx >= 0) {
            var p = game.players[pIdx]; if (p.bet > 0) { player.message("§c[Blackjack] Already bet " + fmt(p.bet) + "!"); openGui(event); return }
            p.bet = amount; player.message("§a[Blackjack] You pledged " + fmt(amount) + "!"); openGui(event); refreshAllGuis(uuid); return
        }
        game.players.push({ uuid: uuid, name: player.getName(), hand: [], bet: amount, state: "waiting_bet", result: null })
        player.message("§a[Blackjack] You pledged " + fmt(amount) + "!"); openGui(event); refreshAllGuis(uuid)
    } catch (err) { player.message("§c[Blackjack] Error: " + err) }
}

function doStart(event) {
    if (!game) return
    var uuid = event.player.getUUID()
    prepareNewGame()
    openGui(event)
    refreshAllGuis(uuid)
}

function doHit(event) {
    var player = event.player; var uuid = player.getUUID()
    if (!game || game.phase !== "playing") return
    if (!isMyTurn(uuid)) { player.message("§c[Blackjack] Not your turn!"); return }
    var pIdx = myPlayerIdx(uuid); var p = game.players[pIdx]
    var card = drawCard(); p.hand.push(card)
    var val = handValue(p.hand)
    if (val > 21) { p.state = "bust"; player.message("§c[Blackjack] Bust! (" + val + ")"); openGui(event); nextTurn(uuid) }
    else if (val === 21) { player.message("§a[Blackjack] 21!"); openGui(event); nextTurn(uuid) }
    else { player.message("§e[Blackjack] Drew (" + val + ")"); game.turnStartedAt = SYS.currentTimeMillis(); openGui(event); refreshAllGuis(uuid) }
}

function doStand(event) {
    var player = event.player; var uuid = player.getUUID()
    if (!game || game.phase !== "playing") return
    if (!isMyTurn(uuid)) { player.message("§c[Blackjack] Not your turn!"); return }
    var pIdx = myPlayerIdx(uuid)
    game.players[pIdx].state = "stood"
    player.message("§e[Blackjack] Stand at " + handValue(game.players[pIdx].hand))
    nextTurn()
    openGui(event)
}

function doDouble(event) {
    var player = event.player; var uuid = player.getUUID(); var bal = countCoins(player)
    if (!game || game.phase !== "playing") return
    if (!isMyTurn(uuid)) { player.message("§c[Blackjack] Not your turn!"); return }
    var pIdx = myPlayerIdx(uuid); var p = game.players[pIdx]
    if (p.hand.length !== 2) { player.message("§c[Blackjack] Double down requires 2 cards!"); return }
    if (bal < p.bet) { player.message("§c[Blackjack] Not enough coins!"); return }
    if (!removeCoins(player, p.bet)) { player.message("§c[Blackjack] Coin deduction failed!"); return }
    p.bet *= 2; var card = drawCard(); p.hand.push(card)
    var val = handValue(p.hand)
    if (val > 21) { p.state = "bust"; player.message("§c[Blackjack] Double bust! (" + val + ")") }
    else { p.state = "stood"; player.message("§e[Blackjack] Double down! Stand at " + val) }
    nextTurn()
    openGui(event)
}

function nextTurn(excludeUuid) {
    if (!game) return
    for (var i = game.currentPlayerIdx + 1; i < game.players.length; i++) {
        if (game.players[i].state === "playing") {
            game.currentPlayerIdx = i; game.turnStartedAt = SYS.currentTimeMillis()
            refreshAllGuis(excludeUuid)
            return
        }
    }
    finishDealerTurn()
}

function finishDealerTurn() {
    if (!game) return
    game.phase = "dealer"; game.currentPlayerIdx = -1
    if (game.dealerHand && game.dealerHand.length > 1) game.dealerHand[1].hidden = false
    var dVal = handValue(game.dealerHand)
    while (dVal < 17) { var card = drawCard(); game.dealerHand.push(card); dVal = handValue(game.dealerHand) }
    for (var i = 0; i < game.players.length; i++) {
        var p = game.players[i]
        if (p.state === "bust") { p.result = "lose"; continue }
        if (p.state === "blackjack") {
            if (game.dealerBlackjack) { p.result = "push" }
            else {
                p.result = "win"; var payout = p.bet + Math.floor(p.bet * 1.5)
                var entry = openPlayers[p.uuid]
                if (entry && entry.player) { giveCoins(entry.player, payout); entry.player.message("§6[Blackjack] BLACKJACK! Won " + fmt(payout) + "!") }
            }
            continue
        }
        var pVal = handValue(p.hand)
        if (dVal > 21 || pVal > dVal) {
            p.result = "win"; var payout = p.bet * 2
            var entry = openPlayers[p.uuid]
            if (entry && entry.player) { giveCoins(entry.player, payout); entry.player.message("§a[Blackjack] Won " + fmt(payout) + "!") }
        } else if (pVal === dVal) {
            p.result = "push"
            var entry = openPlayers[p.uuid]
            if (entry && entry.player) { giveCoins(entry.player, p.bet); entry.player.message("§e[Blackjack] Push! " + fmt(p.bet) + " returned.") }
        } else {
            p.result = "lose"
            var entry = openPlayers[p.uuid]
            if (entry && entry.player) { entry.player.message("§c[Blackjack] Dealer wins. Lost " + fmt(p.bet) + ".") }
        }
    }
    game.phase = "result"
    refreshAllGuis()
}

function doNewRound(event) {
    if (!game) return
    var uuid = event.player.getUUID()
    for (var i = 0; i < game.players.length; i++) {
        var p = game.players[i]; p.hand = []; p.bet = 0; p.state = "waiting_bet"; p.result = null
    }
    game.phase = "betting"; game.dealerHand = []; game.deck = []; game.currentPlayerIdx = -1
    game.createdAt = SYS.currentTimeMillis()
    openGui(event)
    refreshAllGuis(uuid)
}

function findOnline(world, uuid) {
    try {
        if (!world) {
            var uuids = Object.keys(openPlayers)
            for (var i = 0; i < uuids.length; i++) {
                var entry = openPlayers[uuids[i]]
                if (entry && entry.player) { world = entry.player.getWorld(); break }
            }
        }
        if (!world) return null
        var list = world.getOnlinePlayers()
        for (var i = 0; i < list.length; i++)
            if (list[i].getUUID() === uuid) return list[i]
    } catch(e) {}
    return null
}

// ============================================================================
// EVENTS
// ============================================================================
function init(e) { npcUuid = e.npc.getUUID() }

function timer(e) {
    if (e.id === 1 && animating) {
        onDealTimer(e.npc)
    }
}

function tick(e) {
    if (game && game.phase === "playing" && game.currentPlayerIdx >= 0) {
        var elapsed = SYS.currentTimeMillis() - game.turnStartedAt
        if (elapsed > TURN_TIMEOUT_MS) {
            var p = game.players[game.currentPlayerIdx]; p.state = "stood"; nextTurn()
        }
    }
    if (game && game.phase === "betting") {
        var elapsed = SYS.currentTimeMillis() - game.createdAt
        if (elapsed > CHALLENGE_TIMEOUT_MS) {
            if (game.players.length > 0) startNewGame()
            else { game = null; refreshAllGuis() }
        }
    }
}

function interact(e) {
    npcUuid = e.npc.getUUID()
    e.setCanceled(true)
    openGui(e)
}

function customGuiButton(e) {
    var bid = e.buttonId
    if (e.gui.getID() !== GUI_MAIN) return
    // Ignore buttons during animation
    if (animating) { e.player.message("§eCards are being dealt..."); return }
    if (bid === C.BTN_10)        { doPlaceBet(e, BET_OPTIONS[0]); return }
    if (bid === C.BTN_50)        { doPlaceBet(e, BET_OPTIONS[1]); return }
    if (bid === C.BTN_200)       { doPlaceBet(e, BET_OPTIONS[2]); return }
    if (bid === C.BTN_CUSTOM_BET){ doCustomBet(e);               return }
    if (bid === C.BTN_START)     { doStart(e);                   return }
    if (bid === C.BTN_HIT)       { doHit(e);                     return }
    if (bid === C.BTN_STAND)     { doStand(e);                   return }
    if (bid === C.BTN_DOUBLE)    { doDouble(e);                  return }
    if (bid === C.BTN_NEW_ROUND) { doNewRound(e);                return }
    if (bid === C.BTN_LEAVE) {
        var uuid = e.player.getUUID()
        delete openPlayers[uuid]
        if (game && game.phase === "betting") {
            var pIdx = myPlayerIdx(uuid)
            if (pIdx >= 0) { game.players.splice(pIdx, 1); if (game.players.length === 0) game = null }
        }
        e.player.closeGui()
        refreshAllGuis()
        return
    }
    if (bid === C.BTN_CLOSE) {
        var uuid = e.player.getUUID()
        delete openPlayers[uuid]
        if (game && game.phase === "betting") {
            var pIdx = myPlayerIdx(uuid)
            if (pIdx >= 0) { game.players.splice(pIdx, 1); if (game.players.length === 0) game = null }
        }
        e.player.closeGui()
        refreshAllGuis()
        return
    }
}

function customGuiScroll(e) {}
function customGuiClosed(e) {}