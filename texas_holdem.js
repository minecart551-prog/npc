// === Texas Hold'em Poker (6 Players) ===
// Uses same coin system as blackjack.js
// stone_coin = 1¢   coal_coin = $1 (100¢)   emerald_coin = $100 (10,000¢)
// ============================================================================

var SYS = Java.type("java.lang.System")

// ============================================================================
// CONFIG
// ============================================================================
var MAX_PLAYERS          = 6
var SMALL_BLIND          = 50    // in cents: 50¢
var BIG_BLIND            = 100   // in cents: $1.00
var MIN_RAISE            = 100   // minimum raise amount
var TURN_TIMEOUT_MS      = 30000
var CHALLENGE_TIMEOUT_MS = 60000

var STONE_TO_COAL   = 100
var COAL_TO_EMERALD = 100
var COIN_STONE      = "coins:stone_coin"
var COIN_COAL       = "coins:coal_coin"
var COIN_EMERALD    = "coins:emerald_coin"

var SUITS = ["♠", "♥", "♦", "♣"]
var RANKS = ["2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"]

var TEX_CARD_BACK   = "minecraft:textures/block/black_wool.png"
var TEX_CARD_RED    = "minecraft:textures/block/white_wool.png"

// ============================================================================
// LAYOUT — Edit these to reposition elements
// ============================================================================
var L = {
    // Player seats — 6 positions in oval (3 left, 3 right)
    P0: { x: 48,  y: 30  },
    P1: { x: -25,  y: 125 },
    P2: { x: 48,  y: 230 },
    P3: { x: 395, y: 30  },
    P4: { x: 450, y: 125 },
    P5: { x: 395, y: 230 },

    // Dealer / Community cards area
    DEALER_LABEL_Y: 0,
    COMM_Y: 15,
    COMM_SPACING: 24,

    // Pot & Bet labels
    POT_Y: 302,
    BET_Y: 64,

    // Status bar
    STATUS_Y: 118,

    // Controls (gameplay buttons)
    CONTROLS_Y: 135,

    // Chips display at bottom
    CHIPS_X: 210,
    CHIPS_Y: 290
}

// ============================================================================
// GUI
// ============================================================================
var GUI_MAIN = 7400
var GUI_W    = 480
var GUI_H    = 340

var cid = 9000
var maxCid = 9000
function nextCid() { return cid++ }
function resetCid() { cid = 9000 }

var C = {
    LBL_POT:      1,
    LBL_CURRENT_BET: 2,
    LBL_STATUS:   3,
    LBL_DEALER:   4,
    LBL_YOUR_HAND: 5,
    BTN_CHECK:    20,
    BTN_CALL:     21,
    BTN_RAISE:    22,
    BTN_FOLD:     23,
    BTN_ALLIN:    24,
    BTN_BET_50:   25,
    BTN_BET_100:  26,
    BTN_BET_200:  27,
    BTN_CUSTOM_BET: 28,
    BTN_DEAL:     29,
    BTN_NEW_HAND: 30,
    BTN_LEAVE:    31,
    BTN_CLOSE:    32,
    TF_CUSTOM_BET: 38
}

// ============================================================================
// STATE
// ============================================================================
var npcUuid = null
var game = null

var openPlayers = {}

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
    var suitColor = (card.suit === 0 || card.suit === 3) ? "§0" : "§c"
    return suitColor + RANKS[card.rank] + SUITS[card.suit] + "§r"
}

var HAND_RANK = {
    ROYAL_FLUSH: 9, STRAIGHT_FLUSH: 8, FOUR_OF_A_KIND: 7,
    FULL_HOUSE: 6, FLUSH: 5, STRAIGHT: 4, THREE_OF_A_KIND: 3,
    TWO_PAIR: 2, ONE_PAIR: 1, HIGH_CARD: 0
}

function evaluateHand(cards) {
    var best = { rank: -1, score: [], name: "" }
    for (var a = 0; a < cards.length - 4; a++)
        for (var b = a + 1; b < cards.length - 3; b++)
            for (var c = b + 1; c < cards.length - 2; c++)
                for (var d = c + 1; d < cards.length - 1; d++)
                    for (var e = d + 1; e < cards.length; e++) {
                        var hand = [cards[a], cards[b], cards[c], cards[d], cards[e]]
                        var result = evaluate5(hand)
                        if (compareHands(result, best) > 0) best = result
                    }
    return best
}

function evaluate5(cards) {
    var ranks = cards.map(function(c) { return c.rank }).sort(function(a,b) { return b - a })
    var suits = cards.map(function(c) { return c.suit })
    var isFlush = suits[0] === suits[1] && suits[1] === suits[2] && suits[2] === suits[3] && suits[3] === suits[4]
    var isStraight = false, highCard = 0
    if (ranks[0] - ranks[1] === 1 && ranks[1] - ranks[2] === 1 && ranks[2] - ranks[3] === 1 && ranks[3] - ranks[4] === 1) { isStraight = true; highCard = ranks[0] }
    if (ranks[0] === 12 && ranks[1] === 3 && ranks[2] === 2 && ranks[3] === 1 && ranks[4] === 0) { isStraight = true; highCard = 3 }
    var rankCounts = {}
    for (var i = 0; i < ranks.length; i++) rankCounts[ranks[i]] = (rankCounts[ranks[i]] || 0) + 1
    var groups = []
    for (var r in rankCounts) groups.push({ rank: parseInt(r), count: rankCounts[r] })
    groups.sort(function(a,b) { return b.count - a.count || b.rank - a.rank })
    var result = { rank: 0, score: [], name: "" }
    if (isFlush && isStraight && highCard === 12) { result.rank = HAND_RANK.ROYAL_FLUSH; result.score = [12]; result.name = "Royal Flush"; return result }
    if (isFlush && isStraight) { result.rank = HAND_RANK.STRAIGHT_FLUSH; result.score = [highCard]; result.name = "Straight Flush (" + RANKS[highCard] + " high)"; return result }
    if (groups[0].count === 4) { result.rank = HAND_RANK.FOUR_OF_A_KIND; result.score = [groups[0].rank, groups[1].rank]; result.name = "Four of a Kind (" + RANKS[groups[0].rank] + ")"; return result }
    if (groups[0].count === 3 && groups[1].count === 2) { result.rank = HAND_RANK.FULL_HOUSE; result.score = [groups[0].rank, groups[1].rank]; result.name = "Full House (" + RANKS[groups[0].rank] + " over " + RANKS[groups[1].rank] + ")"; return result }
    if (isFlush) { result.rank = HAND_RANK.FLUSH; result.score = ranks; result.name = "Flush (" + RANKS[ranks[0]] + " high)"; return result }
    if (isStraight) { result.rank = HAND_RANK.STRAIGHT; result.score = [highCard]; result.name = "Straight (" + RANKS[highCard] + " high)"; return result }
    if (groups[0].count === 3) { result.rank = HAND_RANK.THREE_OF_A_KIND; result.score = [groups[0].rank, groups[1].rank, groups[2].rank]; result.name = "Three of a Kind (" + RANKS[groups[0].rank] + ")"; return result }
    if (groups[0].count === 2 && groups[1].count === 2) { result.rank = HAND_RANK.TWO_PAIR; result.score = [groups[0].rank, groups[1].rank, groups[2].rank]; result.name = "Two Pair (" + RANKS[groups[0].rank] + " and " + RANKS[groups[1].rank] + ")"; return result }
    if (groups[0].count === 2) {
        var kickers = []
        for (var i = 0; i < groups.length; i++) if (groups[i].count === 1) kickers.push(groups[i].rank)
        result.rank = HAND_RANK.ONE_PAIR; result.score = [groups[0].rank].concat(kickers); result.name = "Pair of " + RANKS[groups[0].rank]; return result
    }
    result.rank = HAND_RANK.HIGH_CARD; result.score = ranks; result.name = RANKS[ranks[0]] + " High"; return result
}

function compareHands(a, b) {
    if (a.rank !== b.rank) return a.rank - b.rank
    for (var i = 0; i < a.score.length && i < b.score.length; i++)
        if (a.score[i] !== b.score[i]) return a.score[i] - b.score[i]
    return 0
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
    if (!game || !game.deck || game.deck.length === 0) { if (game) game.deck = createDeck(); else return null }
    return game.deck.pop()
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
    return game && game.currentPlayerIdx >= 0 && game.players[game.currentPlayerIdx].uuid === uuid
}

function nextActivePlayer(fromIdx) {
    if (!game || game.players.length === 0) return -1
    for (var i = 1; i <= game.players.length; i++) {
        var idx = (fromIdx + i) % game.players.length
        if (!game.players[idx].folded) return idx
    }
    return -1
}

// ============================================================================
// CARD RENDERER (same as blackjack.js)
// ============================================================================
function addCardToGui(gui, x, y, card, faceDown) {
    var cw = 32, ch = 42
    var id = nextCid()
    if (faceDown) { gui.addTexturedRect(id, TEX_CARD_BACK, x, y, cw, ch); return }
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
// BUILD GUI — Traditional poker table (dealer top, players left/right)
// ============================================================================
function buildGuiOn(gui, player, api) {
    var uuid   = player.getUUID()
    var bal    = countCoins(player)
    var pIdx   = myPlayerIdx(uuid)
    var pData  = (pIdx >= 0 && game) ? game.players[pIdx] : null

    var CX = 240
    var seats = [L.P0, L.P1, L.P2, L.P3, L.P4, L.P5]
    var CARD_GAP = 14
    var statusY = L.STATUS_Y
    var cy = L.CONTROLS_Y

    // --- Chips display (bottom center) ---
    gui.addLabel(nextCid(), "§7Chips: " + fmt(bal), L.CHIPS_X, L.CHIPS_Y, 140, 10)

    // --- Dealer / Community cards (top center) ---
    gui.addLabel(C.LBL_DEALER, "§f§l♠ ♥ ♦ ♣", 220, L.DEALER_LABEL_Y, 80, 10)
    if (game && game.communityCards && game.communityCards.length > 0) {
        var commX = CX - Math.floor(game.communityCards.length * L.COMM_SPACING / 2)
        for (var c = 0; c < game.communityCards.length; c++)
            addCardToGui(gui, commX + c * L.COMM_SPACING, L.COMM_Y, game.communityCards[c], false)
    }

    // --- Pot (center, below community cards) ---
    if (game) {
        gui.addLabel(C.LBL_POT, "§6Pot: " + fmt(game.pot), 225, L.POT_Y, 100, 12)
        if (game.currentBet > 0)
            gui.addLabel(C.LBL_CURRENT_BET, "§7Bet: " + fmt(game.currentBet), CX - 25, L.BET_Y, 100, 10)
    }

    // --- Players (left and right oval) ---
    if (game) {
        for (var i = 0; i < game.players.length; i++) {
            var p = game.players[i]
            var seat = seats[i]
            var sx = seat.x, sy = seat.y
            var isActiveTurn = (i === game.currentPlayerIdx)
            var isDealer = (i === game.dealerIdx)
            var isSB = (i === game.sbIdx)
            var isBB = (i === game.bbIdx)
            var nameColor = p.folded ? "§7" : (isActiveTurn ? "§e" : "§f")
            var isLeft = i < 3  // first 3 seats on left, last 3 on right

            var badges = ""
            if (isDealer) badges += "§f§lD "
            if (isSB) badges += "§bSB "
            if (isBB) badges += "§cBB "

            // Left side: labels left-aligned, right side: right-aligned
            if (isLeft) {
                gui.addLabel(nextCid(), badges + nameColor + p.name, sx, sy, 90, 8)
                gui.addLabel(nextCid(), "§7" + fmt(p.chips), sx, sy + 9, 70, 8)
                if (p.bet > 0)
                    gui.addLabel(nextCid(), "§7" + fmt(p.bet), sx + 60, sy + 9, 60, 8)
                if (p.hand && p.hand.length === 2) {
                    var handX = sx, handY = sy + 18
                    for (var c = 0; c < 2; c++)
                        addCardToGui(gui, handX + c * CARD_GAP, handY, p.hand[c], game.phase !== "showdown" && uuid !== p.uuid)
                    if (game.phase === "showdown" && !p.folded) {
                        var allCards = p.hand.concat(game.communityCards)
                        var evalResult = evaluateHand(allCards)
                        gui.addLabel(nextCid(), "§7" + evalResult.name, sx, handY + 46, 110, 10)
                    }
                }
                if (p.folded) gui.addLabel(nextCid(), "§7FOLD", sx, sy + 55, 50, 10)
            } else {
                gui.addLabel(nextCid(), nameColor + p.name + " " + badges, sx - 80, sy, 90, 8)
                gui.addLabel(nextCid(), "§7" + fmt(p.chips), sx - 60, sy + 9, 70, 8)
                if (p.bet > 0)
                    gui.addLabel(nextCid(), "§7" + fmt(p.bet), sx - 50, sy + 9, 60, 8)
                if (p.hand && p.hand.length === 2) {
                    var handX = sx - 12, handY = sy + 18
                    for (var c = 0; c < 2; c++)
                        addCardToGui(gui, handX + c * CARD_GAP, handY, p.hand[c], game.phase !== "showdown" && uuid !== p.uuid)
                    if (game.phase === "showdown" && !p.folded) {
                        var allCards = p.hand.concat(game.communityCards)
                        var evalResult = evaluateHand(allCards)
                        gui.addLabel(nextCid(), "§7" + evalResult.name, sx - 30, handY + 46, 110, 10)
                    }
                }
                if (p.folded) gui.addLabel(nextCid(), "§7FOLD", sx - 30, sy + 55, 50, 10)
            }
        }
    }

    // --- Status (between table and controls) ---
    if (game) {
        if (game.phase === "waiting") {
            gui.addLabel(C.LBL_STATUS, "§6§lWaiting for players...", 200, statusY, 200, 12)
        } else if (game.phase === "preflop" || game.phase === "flop" || game.phase === "turn" || game.phase === "river") {
            var phaseNames = { preflop: "Pre-Flop", flop: "Flop", turn: "Turn", river: "River" }
            var cur = game.players[game.currentPlayerIdx]
            var turnText = cur ? ((cur.uuid === uuid) ? "§e§lYOUR TURN" : "§7" + cur.name + "...") : ""
            gui.addLabel(C.LBL_STATUS, "§6§l" + phaseNames[game.phase] + " — " + turnText, 140, statusY, 280, 12)
        } else if (game.phase === "showdown") {
            gui.addLabel(C.LBL_STATUS, "§a§lShowdown!", 215, statusY, 120, 12)
        }
    } else {
        gui.addLabel(C.LBL_STATUS, "§7Buy in to start!", 214, statusY, 150, 12)
    }

    // --- Controls (bottom) ---
    if (!game || game.phase === "waiting") {
        var bw = 42
        gui.addButton(C.BTN_BET_50,  "0.50", 170, cy - 2, bw, 14)
        gui.addButton(C.BTN_BET_100, "1.00", 220, cy - 2, bw, 14)
        gui.addButton(C.BTN_BET_200, "2.00", 270, cy - 2, bw, 14)
        gui.addLabel(nextCid(), "§7Custom", 173, cy + 20, 40, 8)
        gui.addTextField(C.TF_CUSTOM_BET, 208, cy + 18, 65, 12).setText("")
        gui.addButton(C.BTN_CUSTOM_BET, "§aBuy", 279, cy + 17, 30, 14)
        if (game && game.players.length >= 2)
            gui.addButton(C.BTN_DEAL, "§aDeal", 320, cy, 50, 14)
    } else if (game.phase !== "showdown") {
        if (isMyTurn(uuid) && pData && !pData.folded) {
            var bw = 50, toCall = game.currentBet - pData.bet
            if (toCall > 0)
                gui.addButton(C.BTN_CALL, "§aCall " + fmt(toCall), 165, cy, bw, 14)
            else
                gui.addButton(C.BTN_CHECK, "§aCheck", 165, cy, bw, 14)
            gui.addButton(C.BTN_RAISE, "§eRaise", 223, cy, bw, 14)
            gui.addButton(C.BTN_FOLD, "§cFold", 281, cy, bw, 14)
            if (pData.chips > 0)
                gui.addButton(C.BTN_ALLIN, "§5All " + fmt(pData.chips), 339, cy, 65, 14)
        } else {
            gui.addLabel(C.LBL_YOUR_HAND, "§7Waiting...", 200, cy, 80, 10)
        }
    } else if (game.phase === "showdown") {
        if (game.roundComplete) {
            gui.addButton(C.BTN_NEW_HAND, "§aNew Hand", 175, cy, 60, 14)
            gui.addButton(C.BTN_CLOSE, "§7Leave", 245, cy, 60, 14)
        }
    }

    gui.addButton(C.BTN_LEAVE, "§7Leave", GUI_W - 50, 2, 40, 12)
}

// ============================================================================
// REMOVE + REFRESH
// ============================================================================
function removeGuiComponents(gui) {
    var stableIds = [1, 2, 3, 4, 5, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 38]
    for (var s = 0; s < stableIds.length; s++) {
        try { gui.removeComponent(stableIds[s]); } catch(e) {}
    }
    for (var d = 9000; d < maxCid; d++) {
        try { gui.removeComponent(d); } catch(e) {}
    }
}

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
// GAME LOGIC
// ============================================================================
function startNewHand() {
    if (!game) return

    // Remove players with no chips
    game.players = game.players.filter(function(p) { return p.chips > 0 })
    if (game.players.length < 2) { game = null; return }

    // Deduct coins now that a hand is starting
    for (var i = 0; i < game.players.length; i++) {
        var p = game.players[i]
        if (!p.coinsDeducted && p.buyInAmount > 0) {
            var entry = openPlayers[p.uuid]
            if (entry && entry.player && removeCoins(entry.player, p.buyInAmount)) {
                p.coinsDeducted = true
                entry.player.message("§c[Poker] " + fmt(p.buyInAmount) + " deducted from your inventory.")
            } else if (entry && entry.player) {
                entry.player.message("§c[Poker] Not enough coins to buy in! You've been removed.")
                p.chips = 0
            }
        }
    }

    // Remove players who couldn't pay
    game.players = game.players.filter(function(p) { return p.chips > 0 })
    if (game.players.length < 2) { game = null; return }

    // Reset players for new hand
    for (var i = 0; i < game.players.length; i++) {
        var p = game.players[i]
        p.hand = []
        p.bet = 0
        p.totalBet = 0
        p.folded = false
    }

    // Move dealer button
    if (game.dealerIdx < 0) game.dealerIdx = 0
    else game.dealerIdx = nextActivePlayer(game.dealerIdx)
    if (game.dealerIdx < 0) { game = null; return }

    game.sbIdx = nextActivePlayer(game.dealerIdx)
    if (game.sbIdx < 0) { game = null; return }
    game.bbIdx = nextActivePlayer(game.sbIdx)
    if (game.bbIdx < 0) { game = null; return }

    game.deck = createDeck()
    game.communityCards = []
    game.currentBet = BIG_BLIND
    game.pot = 0
    game.minRaise = MIN_RAISE
    game.phase = "preflop"
    game.currentPlayerIdx = -1
    game.roundComplete = false

    for (var i = 0; i < game.players.length; i++)
        game.players[i].hand = [drawCard(), drawCard()]

    var sb = game.players[game.sbIdx]
    var sbAmt = Math.min(SMALL_BLIND, sb.chips)
    sb.chips -= sbAmt; sb.bet = sbAmt; sb.totalBet += sbAmt; game.pot += sbAmt

    var bb = game.players[game.bbIdx]
    var bbAmt = Math.min(BIG_BLIND, bb.chips)
    bb.chips -= bbAmt; bb.bet = bbAmt; bb.totalBet += bbAmt; game.pot += bbAmt

    game.currentPlayerIdx = nextActivePlayer(game.bbIdx)
    if (game.currentPlayerIdx < 0) { game = null; return }
    game.turnStartedAt = SYS.currentTimeMillis()

    broadcast("§e[Poker] New hand! " + game.players.length + " players. SB: " + fmt(SMALL_BLIND) + ", BB: " + fmt(BIG_BLIND))
}

function doBuyIn(event, amount) {
    var player = event.player
    var uuid   = player.getUUID()
    var bal    = countCoins(player)

    if (game && game.players.length >= MAX_PLAYERS && myPlayerIdx(uuid) < 0) {
        player.message("§c[Poker] Table is full (" + MAX_PLAYERS + " max)!")
        openGui(event); return
    }
    if (bal < amount) {
        player.message("§c[Poker] Not enough coins! Need " + fmt(amount))
        openGui(event); return
    }

    if (!game) {
        game = {
            phase: "waiting", deck: [], communityCards: [], players: [],
            currentPlayerIdx: -1, dealerIdx: -1, currentBet: 0, pot: 0,
            minRaise: MIN_RAISE, roundCount: 0, createdAt: SYS.currentTimeMillis(),
            turnStartedAt: 0, sbIdx: -1, bbIdx: -1, roundComplete: false
        }
    }
    if (game.phase !== "waiting" && myPlayerIdx(uuid) < 0) {
        player.message("§c[Poker] A hand is in progress! Wait for it to finish.")
        openGui(event); return
    }

    var pIdx = myPlayerIdx(uuid)
    if (pIdx >= 0) {
        player.message("§c[Poker] You're already in the game!")
        openGui(event); return
    }

    // Pledge only — no coins taken until hand starts
    game.players.push({
        uuid: uuid, name: player.getName(), hand: [], bet: 0,
        totalBet: 0, chips: amount, folded: false, buyInAmount: amount,
        coinsDeducted: false
    })

    player.message("§a[Poker] You pledged " + fmt(amount) + "! Coins taken when deal is clicked.")
    openGui(event)
    refreshAllGuis(uuid)
}

function doCustomBuyIn(event) {
    var player = event.player; var uuid = player.getUUID(); var gui = event.gui
    try {
        var textField = gui.getComponent(C.TF_CUSTOM_BET)
        if (!textField) { player.message("§c[Poker] Could not read field!"); return }
        var raw = textField.getText()
        if (!raw || raw === "") { player.message("§c[Poker] Enter an amount!"); return }
        var cleaned = raw.replace("$", "").trim()
        var parsed = parseFloat(cleaned)
        if (isNaN(parsed) || parsed <= 0) { player.message("§c[Poker] Invalid amount!"); return }
        var cents = Math.round(parsed * 100)
        if (cents < BIG_BLIND) { player.message("§c[Poker] Minimum buy-in is " + fmt(BIG_BLIND) + "!"); return }
        if (cents > 50000) { player.message("§c[Poker] Max buy-in is $500.00!"); return }
        doBuyIn(event, cents)
    } catch (err) { player.message("§c[Poker] Error: " + err) }
}

function doDeal(event) {
    if (!game || game.players.length < 2) {
        event.player.message("§c[Poker] Need at least 2 players!")
        return
    }
    startNewHand()
    openGui(event)
    refreshAllGuis(event.player.getUUID())
}

function doFold(event) {
    var player = event.player; var uuid = player.getUUID()
    if (!game || !isMyTurn(uuid)) { player.message("§c[Poker] Not your turn!"); return }
    var pIdx = myPlayerIdx(uuid)
    game.players[pIdx].folded = true
    player.message("§c[Poker] You folded.")
    advanceTurn()
}

function doCheck(event) {
    var player = event.player; var uuid = player.getUUID()
    if (!game || !isMyTurn(uuid)) { player.message("§c[Poker] Not your turn!"); return }
    var pIdx = myPlayerIdx(uuid)
    if (game.currentBet > game.players[pIdx].bet) { player.message("§c[Poker] Can't check. You need to call or raise!"); return }
    player.message("§e[Poker] Check.")
    advanceTurn()
}

function doCall(event) {
    var player = event.player; var uuid = player.getUUID()
    if (!game || !isMyTurn(uuid)) { player.message("§c[Poker] Not your turn!"); return }
    var pIdx = myPlayerIdx(uuid); var p = game.players[pIdx]
    var toCall = game.currentBet - p.bet
    if (toCall <= 0) { player.message("§c[Poker] Nothing to call!"); return }
    var callAmt = Math.min(toCall, p.chips)
    if (callAmt < toCall) player.message("§c[Poker] Not enough chips to call! All-in for " + fmt(callAmt))
    p.chips -= callAmt; p.bet += callAmt; p.totalBet += callAmt; game.pot += callAmt
    player.message("§e[Poker] Called " + fmt(callAmt) + ".")
    advanceTurn()
}

function doRaise(event) {
    var player = event.player; var uuid = player.getUUID()
    if (!game || !isMyTurn(uuid)) { player.message("§c[Poker] Not your turn!"); return }
    var pIdx = myPlayerIdx(uuid); var p = game.players[pIdx]
    var toCall = game.currentBet - p.bet
    if (p.chips < toCall + game.minRaise) {
        if (p.chips > toCall) {
            var allAmt = p.chips
            game.currentBet = p.bet + allAmt; game.minRaise = allAmt
            p.chips = 0; p.bet += allAmt; p.totalBet += allAmt; game.pot += allAmt
            player.message("§e[Poker] All-in for " + fmt(allAmt) + "!")
        } else {
            var callAmt = p.chips
            p.chips = 0; p.bet += callAmt; p.totalBet += callAmt; game.pot += callAmt
            player.message("§e[Poker] Called " + fmt(callAmt) + ".")
        }
        advanceTurn(); return
    }
    var raiseAmt = game.minRaise
    var totalCost = toCall + raiseAmt
    p.chips -= totalCost; p.bet += totalCost; p.totalBet += totalCost
    game.currentBet = p.bet; game.pot += totalCost; game.minRaise = raiseAmt
    player.message("§e[Poker] Raised to " + fmt(game.currentBet) + ".")
    advanceTurn()
}

function doAllIn(event) {
    var player = event.player; var uuid = player.getUUID()
    if (!game || !isMyTurn(uuid)) { player.message("§c[Poker] Not your turn!"); return }
    var pIdx = myPlayerIdx(uuid); var p = game.players[pIdx]
    if (p.chips <= 0) { player.message("§c[Poker] No chips to go all-in with!"); return }
    if (p.folded) { player.message("§c[Poker] You folded!"); return }
    var allAmt = p.chips
    var totalNewBet = p.bet + allAmt
    if (totalNewBet > game.currentBet) { game.currentBet = totalNewBet; game.minRaise = allAmt }
    p.chips = 0; p.bet += allAmt; p.totalBet += allAmt; game.pot += allAmt
    broadcast("§5[Poker] " + p.name + " is ALL-IN for " + fmt(allAmt) + "!")
    advanceTurn()
}

function advanceTurn() {
    if (!game) return
    var active = 0, lastActive = -1
    for (var i = 0; i < game.players.length; i++) { if (!game.players[i].folded) { active++; lastActive = i } }
    if (active === 1) {
        var winner = game.players[lastActive]
        winner.chips += game.pot
        broadcast("§6[Poker] " + winner.name + " wins " + fmt(game.pot) + "! (All others folded)")
        game.phase = "showdown"; game.currentPlayerIdx = -1; game.roundComplete = true
        refreshAllGuis(); return
    }
    var nextIdx = nextActivePlayer(game.currentPlayerIdx)
    if (nextIdx < 0) { game = null; return }
    game.currentPlayerIdx = nextIdx; game.turnStartedAt = SYS.currentTimeMillis()
    var allEqual = true, firstBet = -1
    for (var i = 0; i < game.players.length; i++) {
        if (!game.players[i].folded) {
            if (firstBet < 0) firstBet = game.players[i].bet
            else if (game.players[i].bet !== firstBet) { allEqual = false; break }
        }
    }
    if (allEqual && nextIdx === getFirstToAct()) { advancePhase(); return }
    refreshAllGuis()
}

function getFirstToAct() {
    if (!game) return -1
    if (game.phase === "preflop") return nextActivePlayer(game.bbIdx)
    return nextActivePlayer(game.dealerIdx)
}

function advancePhase() {
    if (!game) return
    for (var i = 0; i < game.players.length; i++) game.players[i].bet = 0
    game.currentBet = 0; game.minRaise = MIN_RAISE
    if (game.phase === "preflop") {
        game.phase = "flop"
        game.communityCards.push(drawCard()); game.communityCards.push(drawCard()); game.communityCards.push(drawCard())
        broadcast("§e[Poker] Flop: " + cardString(game.communityCards[0]) + " " + cardString(game.communityCards[1]) + " " + cardString(game.communityCards[2]))
    } else if (game.phase === "flop") {
        game.phase = "turn"; game.communityCards.push(drawCard())
        broadcast("§e[Poker] Turn: " + cardString(game.communityCards[3]))
    } else if (game.phase === "turn") {
        game.phase = "river"; game.communityCards.push(drawCard())
        broadcast("§e[Poker] River: " + cardString(game.communityCards[4]))
    } else {
        game.phase = "showdown"; game.currentPlayerIdx = -1; doShowdown(); return
    }
    game.currentPlayerIdx = getFirstToAct()
    if (game.currentPlayerIdx < 0) { game = null; return }
    game.turnStartedAt = SYS.currentTimeMillis()
    refreshAllGuis()
}

function doShowdown() {
    if (!game) return
    var activePlayers = []
    for (var i = 0; i < game.players.length; i++) {
        if (!game.players[i].folded) {
            var allCards = game.players[i].hand.concat(game.communityCards)
            var result = evaluateHand(allCards)
            activePlayers.push({ idx: i, player: game.players[i], result: result })
        }
    }
    activePlayers.sort(function(a, b) { return compareHands(a.result, b.result) }); activePlayers.reverse()
    var bestScore = activePlayers[0].result
    var winners = []
    for (var i = 0; i < activePlayers.length; i++) {
        if (compareHands(activePlayers[i].result, bestScore) === 0) winners.push(activePlayers[i])
    }
    var share = Math.floor(game.pot / winners.length)
    var remainder = game.pot - (share * winners.length)
    for (var i = 0; i < winners.length; i++) {
        var extra = (i === 0) ? remainder : 0
        winners[i].player.chips += share + extra
        giveCoins(openPlayers[winners[i].player.uuid].player, share + extra)
        broadcast("§6[Poker] " + winners[i].player.name + " wins " + fmt(share + extra) + "! (" + winners[i].result.name + ")")
    }
    game.roundComplete = true
    refreshAllGuis()
}

function doNewHand(event) {
    if (!game) return
    startNewHand()
    openGui(event)
    refreshAllGuis(event.player.getUUID())
}

// ============================================================================
// EVENTS
// ============================================================================
function init(e) { npcUuid = e.npc.getUUID() }

function tick(e) {
    if (game && game.phase === "waiting") {
        var elapsed = SYS.currentTimeMillis() - game.createdAt
        if (elapsed > CHALLENGE_TIMEOUT_MS) { game = null; refreshAllGuis() }
    }
    if (game && game.phase !== "waiting" && game.phase !== "showdown" && game.currentPlayerIdx >= 0) {
        var elapsed = SYS.currentTimeMillis() - game.turnStartedAt
        if (elapsed > TURN_TIMEOUT_MS) {
            var p = game.players[game.currentPlayerIdx]
            if (!p.folded) {
                broadcast("§c[Poker] " + p.name + " ran out of time! Auto-fold.")
                p.folded = true; advanceTurn()
            }
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
    if (bid === C.BTN_BET_50)     { doBuyIn(e, SMALL_BLIND); return }
    if (bid === C.BTN_BET_100)    { doBuyIn(e, BIG_BLIND); return }
    if (bid === C.BTN_BET_200)    { doBuyIn(e, BIG_BLIND * 2); return }
    if (bid === C.BTN_CUSTOM_BET) { doCustomBuyIn(e); return }
    if (bid === C.BTN_DEAL)       { doDeal(e); return }
    if (bid === C.BTN_CHECK)      { doCheck(e); return }
    if (bid === C.BTN_CALL)       { doCall(e); return }
    if (bid === C.BTN_RAISE)      { doRaise(e); return }
    if (bid === C.BTN_FOLD)       { doFold(e); return }
    if (bid === C.BTN_ALLIN)      { doAllIn(e); return }
    if (bid === C.BTN_NEW_HAND)   { doNewHand(e); return }
    if (bid === C.BTN_LEAVE) {
        var uuid = e.player.getUUID()
        delete openPlayers[uuid]
        if (game) {
            for (var i = 0; i < game.players.length; i++) {
                if (game.players[i].uuid === uuid) {
                    if (game.phase === "waiting") {
                        game.players.splice(i, 1)
                        if (game.players.length === 0) game = null
                    }
                    break
                }
            }
        }
        e.player.closeGui(); refreshAllGuis(); return
    }
    if (bid === C.BTN_CLOSE) {
        var uuid = e.player.getUUID()
        delete openPlayers[uuid]
        if (game && game.phase === "waiting") {
            for (var i = 0; i < game.players.length; i++) {
                if (game.players[i].uuid === uuid) {
                    game.players.splice(i, 1)
                    if (game.players.length === 0) game = null
                    break
                }
            }
        }
        e.player.closeGui(); refreshAllGuis(); return
    }
}

function customGuiScroll(e) {}
function customGuiClosed(e) {}