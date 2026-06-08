// ── GUI window size ──────────────────────────────────────────
var GUI_WIDTH  = 176;
var GUI_HEIGHT = 166;

// ── Reel (item slot) settings ────────────────────────────────
var REEL_CENTER_X  = 87;
var REEL_Y         = -35;
var REEL_SLOT_SIZE = 18;
var REEL_GAP       = 10;

// ── Gold frame around the reels ──────────────────────────────
var FRAME_PADDING   = 5;
var FRAME_COLOR     = 0xFFD700;
var FRAME_THICKNESS = 2;

// ── Title label ──────────────────────────────────────────────
var TITLE_X     = 24;
var TITLE_Y     = -75;
var TITLE_SCALE = 1.0;

// ── Coin counter label ───────────────────────────────────────
var COINS_X     = -40;
var COINS_Y     = -90;
var COINS_SCALE = 0.8;

// ── Status / result message label ───────────────────────────
var MSG_X     = 33;
var MSG_Y     = -10;
var MSG_SCALE = 0.85;

// ── Payout hint label (bottom of GUI) ───────────────────────
var HINT_X     = -45;
var HINT_Y     = 80;
var HINT_SCALE = 0.65;

// ── Bet buttons ──────────────────────────────────────────────
var BTN_CENTER_X = 85;
var BTN_Y        = 18;
var BTN_WIDTH    = 48;
var BTN_HEIGHT   = 20;
var BTN_GAP      = 8;

// ╔══════════════════════════════════════════════════════════╗
// ║                  ★  GAME CONFIG  ★                      ║
// ╚══════════════════════════════════════════════════════════╝

var SYMBOLS = [
    "minecraft:diamond",
    "minecraft:gold_ingot",
    "minecraft:emerald",
    "minecraft:iron_ingot",
    "minecraft:redstone",
    "minecraft:coal",
    "minecraft:bone",
];

var SYMBOL_WEIGHTS = [1, 3, 4, 6, 7, 9, 10];

var PAYOUTS = {
    "0,0,0": 50,
    "1,1,1": 20,
    "2,2,2": 12,
    "3,3,3": 5,
    "4,4,4": 4,
    "5,5,5": 2,
    "6,6,6": 2,
    "0,1,2": 7,
    "1,2,3": 1
};

var BET_OPTIONS = [10, 50, 200];

var PHASE_FAST_INTERVAL   = 1;
var PHASE_FAST_DURATION   = 4;

var PHASE_MEDIUM_INTERVAL = 3;
var PHASE_MEDIUM_DURATION = 4;

var PHASE_SLOW_INTERVAL   = 7;
var PHASE_SLOW_DURATION   = 3;

var SPIN_STAGGER          = 23;

var STONE_TO_COAL   = 100;
var COAL_TO_EMERALD = 100;

// ╔══════════════════════════════════════════════════════════╗
// ║              INTERNAL — do not edit below               ║
// ╚══════════════════════════════════════════════════════════╝

var ID_BET_0    = 10;
var ID_BET_1    = 11;
var ID_BET_2    = 12;
var ID_TITLE    = 20;
var ID_MSG      = 21;
var ID_COINS    = 22;
var ID_SPIN_LBL = 23;

// ── The player who is currently spinning (locked in at bet-time) ──
var spinningPlayer = null;
var spinningGui    = null;
var spinningBet    = 0;
var spinningReels  = [];

// ── Globals for whoever currently has the GUI open ────────────────
var guiRef     = null;
var lastBlock  = null;
var lastPlayer = null;
var lastApi    = null;
var reelSlots  = [];

var spinning       = false;
var spinTick       = 0;
var currentBet     = 0;
var finalSymbols   = [-1, -1, -1];
var displaySymbols = [0, 0, 0];

var reelLocked     = [false, false, false];
var reelPhase      = [0, 0, 0];
var reelPhaseStart = [0, 0, 0];
var reelNextFlip   = [0, 0, 0];
var reelStart      = [0, 0, 0];

// ── Safe GUI update helper ───────────────────────────────────
// All .update() calls go through here so the ClassCastException
// is caught in exactly one place instead of scattered everywhere.
function safeUpdate(gui) {
    if (!gui) return;
    try { gui.update(); } catch(e) {}
}

// ── Derived layout helpers ───────────────────────────────────
function getReelBaseX() {
    return REEL_CENTER_X - (3 * REEL_SLOT_SIZE + 2 * REEL_GAP) / 2;
}

function getBtnStartX() {
    return BTN_CENTER_X - (3 * BTN_WIDTH + 2 * BTN_GAP) / 2;
}

function intervalForPhase(phase) {
    if (phase === 0) return PHASE_FAST_INTERVAL;
    if (phase === 1) return PHASE_MEDIUM_INTERVAL;
    return PHASE_SLOW_INTERVAL;
}

function durationForPhase(phase, reel) {
    if (phase === 0) return PHASE_FAST_DURATION;
    if (phase === 1) return PHASE_MEDIUM_DURATION + reel * SPIN_STAGGER;
    return PHASE_SLOW_DURATION;
}

// ── Block init ───────────────────────────────────────────────
function init(event) {
    event.block.setModel("decocraft:slot_machine_blue");
    event.block.setRotation(0, 0, 0);
}

// ── Open GUI ─────────────────────────────────────────────────
function interact(event) {
    var player = event.player;

    if (spinning) {
        player.message("§cThe machine is currently spinning!");
        return;
    }

    var api    = event.API;
    lastBlock  = event.block;
    lastPlayer = player;
    lastApi    = api;

    spinTick       = 0;
    displaySymbols = [0, 0, 0];
    finalSymbols   = [-1, -1, -1];
    currentBet     = 0;

    // Close any container the player currently has open before creating
    // a new custom GUI. Without this, the server tries to cast the active
    // container (class_1723 / ScreenHandler) to ContainerCustomGui and
    // throws a ClassCastException.
    try { player.closeScreen(); } catch(e) {}

    guiRef = api.createCustomGui(GUI_WIDTH, GUI_HEIGHT, 0, true, player);

    if (!guiRef) {
        player.message("§cFailed to open the slot machine GUI. Try again.");
        return;
    }

    guiRef.addLabel(ID_TITLE, "§6§lCASINO SLOT MACHINE", TITLE_X, TITLE_Y, TITLE_SCALE, TITLE_SCALE);

    reelSlots = [];
    var reelBaseX = getReelBaseX();
    for (var r = 0; r < 3; r++) {
        var rx   = reelBaseX + r * (REEL_SLOT_SIZE + REEL_GAP);
        var slot = guiRef.addItemSlot(rx, REEL_Y);
        reelSlots.push(slot);
        try {
            var initItem = player.world.createItem(SYMBOLS[displaySymbols[r]], 1);
            slot.setStack(initItem);
        } catch(e) {}
    }

    var frameLeft   = reelBaseX - FRAME_PADDING;
    var frameRight  = reelBaseX + 3 * (REEL_SLOT_SIZE + REEL_GAP) - REEL_GAP + FRAME_PADDING;
    var frameTop    = REEL_Y    - FRAME_PADDING;
    var frameBottom = REEL_Y    + REEL_SLOT_SIZE + FRAME_PADDING;
    guiRef.addColoredLine(30, frameLeft,  frameTop,    frameRight, frameTop,    FRAME_COLOR, FRAME_THICKNESS);
    guiRef.addColoredLine(31, frameLeft,  frameBottom, frameRight, frameBottom, FRAME_COLOR, FRAME_THICKNESS);
    guiRef.addColoredLine(32, frameLeft,  frameTop,    frameLeft,  frameBottom, FRAME_COLOR, FRAME_THICKNESS);
    guiRef.addColoredLine(33, frameRight, frameTop,    frameRight, frameBottom, FRAME_COLOR, FRAME_THICKNESS);

    var btnLabels = [
        "BET §a" + BET_OPTIONS[0] + "¢",
        "BET §e" + BET_OPTIONS[1] + "¢",
        "BET §c" + BET_OPTIONS[2] + "¢"
    ];
    var btnIds    = [ID_BET_0, ID_BET_1, ID_BET_2];
    var btnStartX = getBtnStartX();
    for (var b = 0; b < 3; b++) {
        guiRef.addButton(btnIds[b], btnLabels[b], btnStartX + b * (BTN_WIDTH + BTN_GAP), BTN_Y, BTN_WIDTH, BTN_HEIGHT);
    }

    guiRef.addLabel(ID_MSG,      "§7Place your bet to spin!", MSG_X,   MSG_Y,   MSG_SCALE,   MSG_SCALE);
    guiRef.addLabel(ID_COINS,    "§7Coins: §f" + countPlayerCoins(player) + "¢", COINS_X, COINS_Y, COINS_SCALE, COINS_SCALE);
    guiRef.addLabel(ID_SPIN_LBL, "§7" + getPayoutHint(), HINT_X, HINT_Y, HINT_SCALE, HINT_SCALE);

    // showCustomGui sends the full GUI to the client on its own.
    // Do NOT call guiRef.update() immediately after on the same tick —
    // the server-side container hasn't finished switching yet, which is
    // what causes the ClassCastException (class_1723 vs ContainerCustomGui).
    try {
        player.showCustomGui(guiRef);
    } catch(e) {
        player.message("§cCould not open GUI. Please try again.");
        guiRef = null;
    }
}

function getPayoutHint() {
    return "3xDiamond=50x  3xGold=20x  3xEmerald=12x  Pair=2x";
}

// ── Button clicked ───────────────────────────────────────────
function customGuiButton(event) {
    var player = event.player;
    var api    = event.API;
    var btnId  = event.buttonId;

    if (spinning) {
        player.message("§cAlready spinning!");
        return;
    }

    var betIndex = -1;
    if      (btnId === ID_BET_0) betIndex = 0;
    else if (btnId === ID_BET_1) betIndex = 1;
    else if (btnId === ID_BET_2) betIndex = 2;
    if (betIndex === -1) return;

    var betAmount   = BET_OPTIONS[betIndex];
    var playerCoins = countPlayerCoins(player);
    if (playerCoins < betAmount) {
        player.message("§cNot enough coins! Need §e" + betAmount + "¢§c, have §e" + playerCoins + "¢");
        updateMessage("§cNot enough coins!");
        safeUpdate(guiRef);
        return;
    }

    removeCoins(player, betAmount);
    currentBet   = betAmount;
    finalSymbols = [weightedRandom(), weightedRandom(), weightedRandom()];

    spinning       = true;
    spinTick       = 0;
    displaySymbols = [
        Math.floor(Math.random() * SYMBOLS.length),
        Math.floor(Math.random() * SYMBOLS.length),
        Math.floor(Math.random() * SYMBOLS.length)
    ];

    for (var r = 0; r < 3; r++) {
        reelLocked[r]     = false;
        reelPhase[r]      = 0;
        reelPhaseStart[r] = 0;
        reelNextFlip[r]   = 0;
    }

    // ── Lock in who is spinning ──
    spinningPlayer = player;
    spinningGui    = guiRef;
    spinningBet    = betAmount;
    spinningReels  = reelSlots.slice();

    updateMessage("§e§lSPINNING...");
    updateCoinsLabel(player);
    safeUpdate(guiRef);

    lastPlayer = player;
    lastApi    = api;
    if (!lastBlock) {
        player.message("§cError: block reference lost. Please close and reopen the machine.");
        spinning       = false;
        spinningPlayer = null;
        spinningGui    = null;
        spinningBet    = 0;
        spinningReels  = [];
        return;
    }
    lastBlock.timers.forceStart(1, 1, true);
}

// ── Timer tick (fires every 1 game-tick) ─────────────────────
function timer(event) {
    if (event.id !== 1) return;

    if (!spinning || !lastBlock) {
        if (lastBlock) lastBlock.timers.stop(1);
        return;
    }

    var block = lastBlock;

    spinTick++;

    var anyFlip = false;

    for (var r = 0; r < 3; r++) {
        if (reelLocked[r]) continue;

        var phase    = reelPhase[r];
        var phaseAge = spinTick - reelPhaseStart[r];

        if (phaseAge >= durationForPhase(phase, r)) {
            if (phase < 2) {
                reelPhase[r]      = phase + 1;
                reelPhaseStart[r] = spinTick;
                reelNextFlip[r]   = spinTick;
            } else {
                displaySymbols[r] = finalSymbols[r];
                reelLocked[r]     = true;
                anyFlip           = true;
                continue;
            }
        }

        if (spinTick >= reelNextFlip[r]) {
            displaySymbols[r] = Math.floor(Math.random() * SYMBOLS.length);
            anyFlip           = true;
            reelNextFlip[r]   = spinTick + intervalForPhase(reelPhase[r]);
        }
    }

    if (anyFlip) {
        updateReelDisplay(lastApi);
    }

    if (reelLocked[0] && reelLocked[1] && reelLocked[2]) {
        block.timers.stop(1);
        spinning = false;
        spinTick = 0;
        resolveResult();
    }
}

// ── Refresh reel item stacks ──────────────────────────────────
function updateReelDisplay(api) {
    if (!spinningGui || !spinningPlayer) return;
    for (var r = 0; r < 3; r++) {
        if (!spinningReels[r]) continue;
        try {
            var item = spinningPlayer.world.createItem(SYMBOLS[displaySymbols[r]], 1);
            spinningReels[r].setStack(item);
        } catch(e) {}
    }
    safeUpdate(spinningGui);
}

// ── Resolve win / loss ───────────────────────────────────────
function resolveResult() {
    if (!spinningPlayer) return;

    var player = spinningPlayer;
    var bet    = spinningBet;
    var gui    = spinningGui;

    var s          = finalSymbols;
    var sortedKey  = s.slice().sort(function(a, b) { return a - b; }).join(",");
    var multiplier = 0;
    var resultMsg  = "";

    if (PAYOUTS[sortedKey] !== undefined) {
        multiplier = PAYOUTS[sortedKey];
        resultMsg  = (s[0] === s[1] && s[1] === s[2])
                   ? "§6§l★ " + multiplier + "× WIN! ★"
                   : "§b§lCOMBO WIN! §e" + multiplier + "×";
    } else if (s[0] === s[1] || s[1] === s[2] || s[0] === s[2]) {
        multiplier = 2;
        resultMsg  = "§a§lPAIR! §e2× win";
    } else {
        resultMsg  = "§c§lNo match. Better luck!";
    }

    if (multiplier > 0) {
        var payout = Math.floor(bet * multiplier);
        giveCoins(player, payout);
        player.message("§aYou won §e" + payout + "¢§a! (" + multiplier + "× your §e" + bet + "¢§a bet)");
        updateMessageOnGui(gui, resultMsg + " §e+" + payout + "¢");
    } else {
        player.message("§cYou lost §e" + bet + "¢§c. Spin again!");
        updateMessageOnGui(gui, resultMsg);
    }

    updateCoinsLabelOnGui(gui, player);
    safeUpdate(gui);

    // Release spin lock
    spinningPlayer = null;
    spinningGui    = null;
    spinningBet    = 0;
    spinningReels  = [];
}

// ── Label update helpers ─────────────────────────────────────
function updateMessage(text) {
    updateMessageOnGui(guiRef, text);
}

function updateMessageOnGui(gui, text) {
    if (!gui) return;
    try { gui.removeComponent(ID_MSG); } catch(e) {}
    try { gui.addLabel(ID_MSG, text, MSG_X, MSG_Y, MSG_SCALE, MSG_SCALE); } catch(e) {}
}

function updateCoinsLabel(player) {
    updateCoinsLabelOnGui(guiRef, player);
}

function updateCoinsLabelOnGui(gui, player) {
    if (!gui || !player) return;
    try { gui.removeComponent(ID_COINS); } catch(e) {}
    try { gui.addLabel(ID_COINS, "§7Coins: §f" + countPlayerCoins(player) + "¢", COINS_X, COINS_Y, COINS_SCALE, COINS_SCALE); } catch(e) {}
}

// ── Slot clicked (reels are display-only) ────────────────────
function customGuiSlotClicked(event) {
    if (!guiRef) return;
    safeUpdate(guiRef);
}

// ── GUI closed ────────────────────────────────────────────────
function customGuiClosed(event) {
    guiRef = null;
    // spinningGui is intentionally NOT cleared here —
    // only resolveResult() clears it after the spin finishes.
}

// ── Weighted random symbol ────────────────────────────────────
function weightedRandom() {
    var total = 0;
    for (var i = 0; i < SYMBOL_WEIGHTS.length; i++) total += SYMBOL_WEIGHTS[i];
    var roll       = Math.floor(Math.random() * total);
    var cumulative = 0;
    for (var i = 0; i < SYMBOL_WEIGHTS.length; i++) {
        cumulative += SYMBOL_WEIGHTS[i];
        if (roll < cumulative) return i;
    }
    return SYMBOL_WEIGHTS.length - 1;
}

// ── Currency helpers ─────────────────────────────────────────
function countPlayerCoins(player) {
    var stone = 0, coal = 0, emerald = 0;
    var inv   = player.getInventory();
    for (var i = 0; i < inv.getSize(); i++) {
        var s = inv.getSlot(i);
        if (s && !s.isEmpty()) {
            var n = s.getName();
            if      (n === "coins:stone_coin")   stone   += s.getStackSize();
            else if (n === "coins:coal_coin")    coal    += s.getStackSize();
            else if (n === "coins:emerald_coin") emerald += s.getStackSize();
        }
    }
    return stone + coal * STONE_TO_COAL + emerald * STONE_TO_COAL * COAL_TO_EMERALD;
}

function removeCoins(player, amount) {
    var remaining = amount;
    var inv = player.getInventory();
    for (var i = 0; i < inv.getSize() && remaining > 0; i++) {
        var s = inv.getSlot(i);
        if (s && !s.isEmpty() && s.getName() === "coins:stone_coin") {
            var qty = s.getStackSize();
            if (qty <= remaining) { inv.setSlot(i, null); remaining -= qty; }
            else { s.setStackSize(qty - remaining); remaining = 0; }
        }
    }
    for (var i = 0; i < inv.getSize() && remaining > 0; i++) {
        var s = inv.getSlot(i);
        if (s && !s.isEmpty() && s.getName() === "coins:coal_coin") {
            var qty = s.getStackSize();
            var val = qty * STONE_TO_COAL;
            if (val <= remaining) { inv.setSlot(i, null); remaining -= val; }
            else {
                var needed   = Math.ceil(remaining / STONE_TO_COAL);
                var overpaid = needed * STONE_TO_COAL - remaining;
                s.setStackSize(qty - needed);
                remaining = 0;
                if (overpaid > 0) player.giveItem(player.world.createItem("coins:stone_coin", overpaid));
            }
        }
    }
    for (var i = 0; i < inv.getSize() && remaining > 0; i++) {
        var s = inv.getSlot(i);
        if (s && !s.isEmpty() && s.getName() === "coins:emerald_coin") {
            var qty  = s.getStackSize();
            var unit = STONE_TO_COAL * COAL_TO_EMERALD;
            var val  = qty * unit;
            if (val <= remaining) { inv.setSlot(i, null); remaining -= val; }
            else {
                var needed   = Math.ceil(remaining / unit);
                var overpaid = needed * unit - remaining;
                s.setStackSize(qty - needed);
                remaining = 0;
                var changeCoal  = Math.floor(overpaid / STONE_TO_COAL);
                var changeStone = overpaid % STONE_TO_COAL;
                if (changeCoal  > 0) player.giveItem(player.world.createItem("coins:coal_coin",  changeCoal));
                if (changeStone > 0) player.giveItem(player.world.createItem("coins:stone_coin", changeStone));
            }
        }
    }
}

function giveCoins(player, amount) {
    var unit     = STONE_TO_COAL * COAL_TO_EMERALD;
    var emeralds = Math.floor(amount / unit);          amount -= emeralds * unit;
    var coals    = Math.floor(amount / STONE_TO_COAL); amount -= coals * STONE_TO_COAL;
    var stones   = amount;
    if (emeralds > 0) player.giveItem(player.world.createItem("coins:emerald_coin", emeralds));
    if (coals    > 0) player.giveItem(player.world.createItem("coins:coal_coin",    coals));
    if (stones   > 0) player.giveItem(player.world.createItem("coins:stone_coin",   stones));
}