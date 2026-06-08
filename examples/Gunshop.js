// ============================================================
//  SHOP CONFIGURATION — Edit everything here
// ============================================================

var CONFIG_MAX_PAGES = 12;
var SELL_LOSS_PERCENTAGE = 0.3;
var CONFIG_TAB_ICONS = [
    { id: "tacz:modern_kinetic_gun", nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 17, "GunFireMode": "SEMI", "GunId": "cyber_armorer:unity_cheetah"} },
    { id: "tacz:modern_kinetic_gun", nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 17, "GunFireMode": "SEMI", "GunId": "cyber_armorer:saratoga_problem_solver"} },
    { id: "tacz:modern_kinetic_gun", nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 17, "GunFireMode": "SEMI", "GunId": "cyber_armorer:ajax"} },
    { id: "tacz:modern_kinetic_gun", nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 17, "GunFireMode": "SEMI", "GunId": "cyber_armorer:carnage_guts"} },
    { id: "tacz:modern_kinetic_gun", nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 17, "GunFireMode": "SEMI", "GunId": "cyber_armorer:grad"} },
    { id: "tacz:modern_kinetic_gun", nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 17, "GunFireMode": "SEMI", "GunId": "cyber_armorer:mantis_blade_maxtac"} },
    { id: "tacz:ammo", nbt: {"AmmoId": "tacz:9mm"} },
    { id: "minecraft:leather_helmet", nbt: {"Damage": 0} },
    { id: "tacz:attachment", nbt: {"AttachmentId": "tacz:sight_sro_dot"} },
    { id: "tacz:attachment", nbt: {"AttachmentId": "tacz:light_extended_mag_1"} },

];

var CONFIG_TAB_NAMES = [
    "Pistols",
    "SMG",
    "Rifle",
    "Shotgun",
    "Sniper",
    "Something",
    "Ammo",
    "Armour",
    "Scope",
    "Mag",
    "Tab 10",
    "Tab 11",
];

var CONFIG_TAB_ROWS = [
    6,
    6,
    6,
    6,
    6,
    6,
    6,
    6,
    6,
    6,
    6,
    6,
];

var CONFIG_SHOP_ITEMS = [
    // Tab 0 — Guns
[

{ id: "tacz:modern_kinetic_gun", count: 1, price: 15, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 17, "GunFireMode": "SEMI", "GunId": "tacz:glock_17"}, lore: [] },
{ id: "tacz:modern_kinetic_gun", count: 1, price: 100, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 16, "GunFireMode": "AUTO", "GunId": "tacz:cz75"}, lore: [] },
{ id: "tacz:modern_kinetic_gun", count: 1, price: 4500, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 11, "GunFireMode": "SEMI", "GunId": "cyber_armorer:overture_archangel"}, lore: [] },
{ id: "tacz:modern_kinetic_gun", count: 1, price: 4500, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 11, "GunFireMode": "SEMI", "GunId": "cyber_armorer:overture_crash"}, lore: [] },
{ id: "tacz:modern_kinetic_gun", count: 1, price: 5000, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 11, "GunFireMode": "SEMI", "GunId": "cyber_armorer:overture"}, lore: [] },
{ id: "tacz:modern_kinetic_gun", count: 1, price: 5500, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 11, "GunFireMode": "SEMI", "GunId": "cyber_armorer:overture_rosco"}, lore: [] },
{ id: "tacz:modern_kinetic_gun", count: 1, price: 5500, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 11, "GunFireMode": "SEMI", "GunId": "cyber_armorer:overture_reliable"}, lore: [] },
{ id: "tacz:modern_kinetic_gun", count: 1, price: 6500, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 11, "GunFireMode": "SEMI", "GunId": "cyber_armorer:unity_her_majesty"}, lore: [] },

{ id: "tacz:modern_kinetic_gun", count: 1, price: 8000, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 11, "GunFireMode": "SEMI", "GunId": "cyber_armorer:unity"}, lore: [] },


{ id: "tacz:modern_kinetic_gun", count: 1, price: 9000, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 11, "GunFireMode": "SEMI", "GunId": "cyber_armorer:3516"}, lore: [] },
{ id: "tacz:modern_kinetic_gun", count: 1, price: 10000, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 11, "GunFireMode": "SEMI", "GunId": "cyber_armorer:overture_amnesty"}, lore: [] },
{ id: "tacz:modern_kinetic_gun", count: 1, price: 10000, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 11, "GunFireMode": "SEMI", "GunId": "cyber_armorer:yukimura"}, lore: [] },
{ id: "tacz:modern_kinetic_gun", count: 1, price: 10000, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 11, "GunFireMode": "SEMI", "GunId": "cyber_armorer:unity_cheetah"}, lore: [] },
{ id: "tacz:modern_kinetic_gun", count: 1, price: 13000, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 11, "GunFireMode": "SEMI", "GunId": "cyber_armorer:yukimura_skippy"}, lore: [] },
{ id: "tacz:modern_kinetic_gun", count: 1, price: 14000, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 11, "GunFireMode": "SEMI", "GunId": "cyber_armorer:yukimura_genjiroh"}, lore: [] },
],
//SMG
[
{ id: "tacz:modern_kinetic_gun", count: 1, price: 13000, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 40, "GunFireMode": "AUTO", "GunId": "cyber_armorer:saratoga"}, lore: [] },
{ id: "tacz:modern_kinetic_gun", count: 1, price: 14000, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 40, "GunFireMode": "AUTO", "GunId": "cyber_armorer:saratoga_fenrir"}, lore: [] },
{ id: "tacz:modern_kinetic_gun", count: 1, price: 14000, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 40, "GunFireMode": "AUTO", "GunId": "cyber_armorer:g58_dian"}, lore: [] },
{ id: "tacz:modern_kinetic_gun", count: 1, price: 17000, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 40, "GunFireMode": "AUTO", "GunId": "cyber_armorer:saratoga_problem_solver"}, lore: [] },
{ id: "tacz:modern_kinetic_gun", count: 1, price: 17500, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 40, "GunFireMode": "AUTO", "GunId": "cyber_armorer:g58_dian_yinglong"}, lore: [] },
],
//rifle
[
{ id: "tacz:modern_kinetic_gun", count: 1, price: 500, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 20, "GunFireMode": "AUTO", "GunId": "tacz:scar_h"}, lore: [] },
],
//Shotgun
[
{ id: "tacz:modern_kinetic_gun", count: 1, price: 1000, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 5, "GunFireMode": "SEMI", "GunId": "tacz:spas_12"}, lore: [] },
],
//Snipers
[
{ id: "tacz:modern_kinetic_gun", count: 1, price: 2000, nbt: {"HasBulletInBarrel": 1, "GunCurrentAmmoCount": 6, "GunFireMode": "SEMI", "GunId": "tacz:m700"}, lore: [] },
],
//Something
[],
    // Tab 6 — Ammo
[

{ id: "tacz:ammo", count: 10, price: 1, nbt: {"AmmoId": "tacz:9mm"}, lore: [] },
{ id: "tacz:ammo", count: 10, price: 10, nbt: {"AmmoId": "tacz:308"}, lore: [] },
{ id: "tacz:ammo", count: 10, price: 10, nbt: {"AmmoId": "tacz:12g"}, lore: [] },
{ id: "tacz:ammo", count: 10, price: 100, nbt: {"AmmoId": "tacz:30_06"}, lore: [] },
    null,null,null,null,null,
{ id: "tacz:ammo", count: 10, price: 50, nbt: {"AmmoId": "cyber_armorer:bullet_pistol"}, lore: [] },
{ id: "tacz:ammo", count: 10, price: 100, nbt: {"AmmoId": "cyber_armorer:bullet_pistol_smart"}, lore: [] },
],
    // Tab 2 — Armour
[

{ id: "minecraft:leather_helmet", count: 1, price: 15, nbt: {"Damage": 0, "AttributeModifiers": [{"Amount": 2.0, "Operation": 0, "Slot": "head", "UUID": "[I;1276935719,2101628065,-2062805011,-1388635]", "AttributeName": "generic.armor", "Name": "generic.armor"}]}, lore: [] },
{ id: "minecraft:chainmail_helmet", count: 1, price: 50, nbt: {"Damage": 0, "AttributeModifiers": [{"Amount": 3.0, "Operation": 0, "Name": "generic.armor", "UUID": "[I;1962758238,-402895587,-1157109447,1225991700]", "AttributeName": "generic.armor", "Slot": "head"}, {"Amount": 3.0, "Operation": 0, "Name": "generic.armor_toughness", "UUID": "[I;-1942079108,914771463,-1608237363,1151659171]", "AttributeName": "generic.armor_toughness", "Slot": "head"}]}, lore: [] },
{ id: "minecraft:iron_helmet", count: 1, price: 800, nbt: {"Damage": 0, "AttributeModifiers": [{"Amount": 11.0, "Operation": 0, "Name": "generic.armor", "UUID": "[I;819273645,-564738291,271828182,-314159265]", "AttributeName": "generic.armor", "Slot": "head"}]}, lore: [] },
{ id: "minecraft:diamond_helmet", count: 1, price: 4000, nbt: {"Damage": 0, "AttributeModifiers": [{"Amount": 18.0, "Operation": 0, "Name": "generic.armor", "UUID": "[I;472918365,-183756592,938475610,-561029384]", "AttributeName": "generic.armor", "Slot": "head"}]}, lore: [] },
    null,null,null,null,null,
{ id: "minecraft:leather_chestplate", count: 1, price: 50, nbt: {"Damage": 0, "AttributeModifiers": [{"Amount": 6.0, "Operation": 0, "Slot": "chest", "UUID": "[I;66453,79497593,-201178,-16957605]", "AttributeName": "generic.armor", "Name": "generic.armor"}]}, lore: [] },
{ id: "minecraft:chainmail_chestplate", count: 1, price: 100, nbt: {"Damage": 0, "AttributeModifiers": [{"Amount": 8.0, "Operation": 0, "Name": "generic.armor", "UUID": "[I;2082959146,1448298836,-2088010816,-866068823]", "AttributeName": "generic.armor", "Slot": "chest"}, {"Amount": 3.0, "Operation": 0, "Name": "generic.armor_toughness", "UUID": "[I;-734355839,1568492182,-1279907194,-1996913419]", "AttributeName": "generic.armor_toughness", "Slot": "chest"}]}, lore: [] },
{ id: "minecraft:iron_chestplate", count: 1, price: 2100, nbt: {"Damage": 0, "AttributeModifiers": [{"Amount": 24.0, "Operation": 0, "Name": "generic.armor", "UUID": "[I;-192837465,918273645,-135792468,246813579]", "AttributeName": "generic.armor", "Slot": "chest"}]}, lore: [] },
{ id: "minecraft:diamond_chestplate", count: 1, price: 8000, nbt: {"Damage": 0, "AttributeModifiers": [{"Amount": 39.0, "Operation": 0, "Name": "generic.armor", "UUID": "[I;-609243745,817364920,-274918365,193746582]", "AttributeName": "generic.armor", "Slot": "chest"}]}, lore: [] },
    null,null,null,null,null,
{ id: "minecraft:leather_leggings", count: 1, price: 40, nbt: {"Damage": 0, "AttributeModifiers": [{"Amount": 5.0, "Operation": 0, "Slot": "legs", "UUID": "[I;1393173916,1759135223,-1879342790,-508144820]", "AttributeName": "generic.armor", "Name": "generic.armor"}]}, lore: [] },
{ id: "minecraft:chainmail_leggings", count: 1, price: 90, nbt: {"Damage": 0, "AttributeModifiers": [{"Amount": 6.0, "Operation": 0, "Slot": "legs", "UUID": "[I;650941737,-1901506567,-1582766196,1857334701]", "AttributeName": "generic.armor", "Name": "generic.armor"}, {"Amount": 3.0, "Operation": 0, "Slot": "legs", "UUID": "[I;-865580501,1016744579,-1206154126,-1400343339]", "AttributeName": "generic.armor_toughness", "Name": "generic.armor_toughness"}]}, lore: [] },
{ id: "minecraft:iron_leggings", count: 1, price: 1300, nbt: {"Damage": 0, "AttributeModifiers": [{"Amount": 15.0, "Operation": 0, "Name": "generic.armor", "UUID": "[I;135791113,-975318642,864209753,-509182736]", "AttributeName": "generic.armor", "Slot": "legs"}]}, lore: [] },
{ id: "minecraft:diamond_leggings", count: 1, price: 4000, nbt: {"Damage": 0, "AttributeModifiers": [{"Amount": 25.0, "Operation": 0, "Name": "generic.armor", "UUID": "[I;358474619,-920183746,647382915,-183746590]", "AttributeName": "generic.armor", "Slot": "legs"}]}, lore: [] },
    null,null,null,null,null,
{ id: "minecraft:leather_boots", count: 1, price: 15, nbt: {"Damage": 0, "AttributeModifiers": [{"Amount": 2.0, "Operation": 0, "Slot": "feet", "UUID": "[I;-1305545332,996295294,-1733047031,531723401]", "AttributeName": "generic.armor", "Name": "generic.armor"}]}, lore: [] },
{ id: "minecraft:chainmail_boots", count: 1, price: 50, nbt: {"Damage": 0, "AttributeModifiers": [{"Amount": 3.0, "Operation": 0, "Slot": "feet", "UUID": "[I;1624854239,-1370865197,-1979221776,1862783941]", "AttributeName": "generic.armor", "Name": "generic.armor"}, {"Amount": 3.0, "Operation": 0, "Slot": "feet", "UUID": "[I;996174618,-1532542096,-1324170079,1457612765]", "AttributeName": "generic.armor_toughness", "Name": "generic.armor_toughness"}]}, lore: [] },
{ id: "minecraft:iron_boots", count: 1, price: 800, nbt: {"Damage": 0, "AttributeModifiers": [{"Amount": 11.0, "Operation": 0, "Name": "generic.armor", "UUID": "[I;-741852963,159357258,-258147963,753951456]", "AttributeName": "generic.armor", "Slot": "feet"}]}, lore: [] },
{ id: "minecraft:diamond_boots", count: 1, price: 3000, nbt: {"Damage": 0, "AttributeModifiers": [{"Amount": 18.0, "Operation": 0, "Name": "generic.armor", "UUID": "[I;-847362915,244918375,-519283746,736491820]", "AttributeName": "generic.armor", "Slot": "feet"}]}, lore: [] },
],
    // Tab 3 — Scope
[

{ id: "tacz:attachment", count: 1, price: 20, nbt: {"AttachmentId": "tacz:sight_sro_dot"}, lore: [] },
{ id: "tacz:attachment", count: 1, price: 40, nbt: {"AttachmentId": "tacz:sight_coyote", "ZoomNumber": 1}, lore: [] },
{ id: "tacz:attachment", count: 1, price: 40, nbt: {"AttachmentId": "tacz:sight_t2"}, lore: [] },
{ id: "tacz:attachment", count: 1, price: 60, nbt: {"AttachmentId": "tacz:scope_acog_ta31"}, lore: [] },
{ id: "tacz:attachment", count: 1, price: 600, nbt: {"AttachmentId": "cyber_armorer:sight_04"}, lore: [] },
{ id: "tacz:attachment", count: 1, price: 300, nbt: {"AttachmentId": "cyber_armorer:sight_01"}, lore: [] },
{ id: "tacz:attachment", count: 1, price: 400, nbt: {"AttachmentId": "cyber_armorer:sight_05"}, lore: [] },
{ id: "tacz:attachment", count: 1, price: 500, nbt: {"AttachmentId": "cyber_armorer:sight_02"}, lore: [] },
{ id: "tacz:attachment", count: 1, price: 400, nbt: {"AttachmentId": "cyber_armorer:sight_03"}, lore: [] },
],
    // Tab 4 — Mag
[

{ id: "tacz:attachment", count: 1, price: 20, nbt: {"AttachmentId": "tacz:light_extended_mag_1"}, lore: [] },
{ id: "tacz:attachment", count: 1, price: 100, nbt: {"AttachmentId": "tacz:shotgun_extended_mag_1"}, lore: [] },
],


];


// ============================================================
 
// Selling: fraction of buy price lost when selling e.g. 0.3 = player gets 70% of buy price at full durability
var SELL_LOSS_PERCENTAGE = 0.3;
 
var guiRef;
var mySlots = [];
var tabSlots = [];
var highlightLineIds = [];
var lastNpc = null;
var storedSlotItems = {};
var currentPage = 0;
var maxPages = CONFIG_MAX_PAGES;
var isSellMode = false;
 
// Viewport system
var viewportRow = 0;
var viewportRows = 6;
var totalRows = CONFIG_TAB_ROWS[0];
var numCols = 9;
 
// Currency conversion rates
var STONE_TO_COAL = 100;
var COAL_TO_EMERALD = 100;
 
// Component IDs
var ID_TAB_BASE    = 50;
var ID_SCROLL_UP   = 111;
var ID_SCROLL_DOWN = 112;
var ID_MODE_TOGGLE = 113;
 
// ========== Layout ==========
var slotPositions = [];
var startX = 0;
var startY = -50;
var rowSpacing = 18;
var colSpacing = 18;
for (var row = 0; row < viewportRows; row++) {
    var y = startY + row * rowSpacing;
    for (var col = 0; col < numCols; col++) {
        var x = startX + col * colSpacing;
        slotPositions.push({x: x, y: y});
    }
}
 
function viewportToGlobal(slotIndex) {
    var localRow = Math.floor(slotIndex / numCols);
    var localCol = slotIndex % numCols;
    var globalRow = viewportRow + localRow;
    return globalRow * numCols + localCol;
}
 
function makeNullArray(n) {
    var a = new Array(n);
    for (var i = 0; i < n; i++) { a[i] = null; }
    return a;
}
 
// ========== SNBT helpers ==========
function snbtValue(v) {
    if (v === null || v === undefined) return "0";
    if (typeof v === "string" && v.charAt(0) === "[") return v;
    if (typeof v === "string") return '"' + v.replace(/\\/g, "\\\\").replace(/"/g, '\\"') + '"';
    if (typeof v === "boolean") return v ? "1b" : "0b";
    if (typeof v === "number") {
        if (v === Math.floor(v)) return String(v);
        return v + "d";
    }
    if (Array.isArray(v)) {
        var parts = [];
        for (var i = 0; i < v.length; i++) parts.push(snbtValue(v[i]));
        return "[" + parts.join(",") + "]";
    }
    if (typeof v === "object") return snbtCompound(v);
    return String(v);
}
 
function snbtCompound(obj) {
    var parts = [];
    for (var key in obj) {
        if (obj.hasOwnProperty(key)) {
            parts.push(key + ":" + snbtValue(obj[key]));
        }
    }
    return "{" + parts.join(",") + "}";
}
 
function buildSnbt(cfg, loreArr) {
    var tagObj = cfg.nbt ? cfg.nbt : {};
    var tag = JSON.parse(JSON.stringify(tagObj));
 
    if (loreArr && loreArr.length > 0) {
        if (!tag.display) tag.display = {};
        tag.display.Lore = loreArr;
    }
 
    var tagParts = [];
    for (var key in tag) {
        if (!tag.hasOwnProperty(key)) continue;
        if (key === "AttributeModifiers") {
            var modParts = [];
            var mods = tag[key];
            for (var m = 0; m < mods.length; m++) {
                modParts.push(snbtCompound(mods[m]));
            }
            tagParts.push("AttributeModifiers:[" + modParts.join(",") + "]");
        } else if (key === "display") {
            var dispParts = [];
            var disp = tag[key];
            for (var dk in disp) {
                if (!disp.hasOwnProperty(dk)) continue;
                if (dk === "Lore") {
                    var loreParts = [];
                    for (var li = 0; li < disp[dk].length; li++) {
                        loreParts.push('"' + String(disp[dk][li]).replace(/\\/g,"\\\\").replace(/"/g,'\\"') + '"');
                    }
                    dispParts.push("Lore:[" + loreParts.join(",") + "]");
                } else {
                    dispParts.push(dk + ":" + snbtValue(disp[dk]));
                }
            }
            tagParts.push("display:{" + dispParts.join(",") + "}");
        } else {
            tagParts.push(key + ":" + snbtValue(tag[key]));
        }
    }
 
    var count = cfg.count || 1;
    return '{id:"' + cfg.id + '",Count:' + count + 'b,tag:{' + tagParts.join(",") + '}}';
}
 
function buildShopDataFromConfig(player, api) {
    var shopData = {};
    for (var t = 0; t < CONFIG_MAX_PAGES; t++) {
        var rows = CONFIG_TAB_ROWS[t] || 5;
        var totalSlots = rows * numCols;
        var arr = makeNullArray(totalSlots);
        var items = CONFIG_SHOP_ITEMS[t] || [];
        for (var idx = 0; idx < items.length && idx < totalSlots; idx++) {
            var cfg = items[idx];
            if (!cfg) { arr[idx] = null; continue; }
            try {
                var loreArr = cfg.lore ? cfg.lore.slice() : [];
                loreArr.push("");
                loreArr.push("\u00a7aPrice: \u00a7e" + (cfg.price || 0) + "\u00a2");
 
                var snbt = buildSnbt(cfg, loreArr);
                var item = player.world.createItemFromNbt(api.stringToNbt(snbt));
                item.setLore(loreArr);
 
                arr[idx] = {
                    displayNbt: item.getItemNbt().toJsonString(),
                    price: cfg.price || 0
                };
            } catch(e) {
                arr[idx] = null;
            }
        }
        shopData[t] = arr;
    }
    return shopData;
}
 
// ========== Open GUI ==========
function interact(event) {
    var player = event.player;
    var api = event.API;
    lastNpc = event.npc;
 
    maxPages = CONFIG_MAX_PAGES;
    totalRows = CONFIG_TAB_ROWS[currentPage] || 5;
 
    storedSlotItems = buildShopDataFromConfig(player, api);
 
    var totalSlots = totalRows * numCols;
    if (!storedSlotItems[currentPage]) {
        storedSlotItems[currentPage] = makeNullArray(totalSlots);
    }
 
    highlightLineIds = [];
 
    if (!guiRef) {
        guiRef = api.createCustomGui(176, 166, 0, true, player);
 
        var tabWidth = 25;
        var tabHeight = 28;
        var tabSpacing = 2;
        var tabStartX = 0;
        tabSlots = [];
        for (var i = 0; i < maxPages; i++) {
            var tabY = (i < 6) ? -80 : 59;
            var tabX = tabStartX + (i % 6) * (tabWidth + tabSpacing) - (i >= 6 ? 1 : 0);
            var tabSlot = guiRef.addItemSlot(tabX + 4, tabY + 5);
            tabSlots.push(tabSlot);
            guiRef.addButton(ID_TAB_BASE + i, "", tabX, tabY, tabWidth, tabHeight);
        }
 
        mySlots = slotPositions.map(function(pos) {
            return guiRef.addItemSlot(pos.x, pos.y);
        });
 
        var scrollX = startX + (numCols * colSpacing) + 2;
        var scrollY = startY;
        guiRef.addButton(ID_SCROLL_UP,   "↑", scrollX, scrollY,      18, 18);
        guiRef.addButton(ID_SCROLL_DOWN, "↓", scrollX, scrollY + 20, 18, 18);
        guiRef.addLabel(10, "", scrollX + 1, scrollY + 42, 0.7, 0.7);

        guiRef.addButton(ID_MODE_TOGGLE, "Buying", scrollX, scrollY + 90, 50, 18);
 
        for (var i = 0; i < tabSlots.length; i++) {
            try {
                var iconName = CONFIG_TAB_NAMES[i] || ("Tab " + (i + 1));
                var iconCfg = CONFIG_TAB_ICONS[i];
                var iconItem;
                if (iconCfg) {
                    var iconSnbt = buildSnbt({ id: iconCfg.id, count: 1, nbt: iconCfg.nbt || {}, lore: [] }, null);
                    iconItem = player.world.createItemFromNbt(api.stringToNbt(iconSnbt));
                } else {
                    iconItem = player.world.createItemFromNbt(api.stringToNbt('{id:"minecraft:barrier",Count:1b,tag:{}}'));
                }
                iconItem.setCustomName(iconName);
                tabSlots[i].setStack(iconItem);
            } catch(e) {}
        }
 
        player.showCustomGui(guiRef);
    } else {
        for (var i = 0; i < tabSlots.length; i++) {
            try {
                var iconName = CONFIG_TAB_NAMES[i] || ("Tab " + (i + 1));
                var iconCfg = CONFIG_TAB_ICONS[i];
                var iconItem;
                if (iconCfg) {
                    var iconSnbt = buildSnbt({ id: iconCfg.id, count: 1, nbt: iconCfg.nbt || {}, lore: [] }, null);
                    iconItem = player.world.createItemFromNbt(api.stringToNbt(iconSnbt));
                } else {
                    iconItem = player.world.createItemFromNbt(api.stringToNbt('{id:"minecraft:barrier",Count:1b,tag:{}}'));
                }
                iconItem.setCustomName(iconName);
                tabSlots[i].setStack(iconItem);
            } catch(e) {}
        }
    }
 
    try {
        guiRef.removeComponent(20);
        guiRef.removeComponent(21);
        guiRef.removeComponent(22);
        guiRef.removeComponent(23);
    } catch(e) {}
    try {
        var tabWidth = 25;
        var tabHeight = 28;
        var tabSpacing = 2;
        var tabStartX = 0;
        var tabY = (currentPage < 6) ? -80 : 59;
        var highlightTabX = tabStartX + (currentPage % 6) * (tabWidth + tabSpacing);
        guiRef.addColoredLine(20, highlightTabX - 1, tabY - 1, highlightTabX + tabWidth + 1, tabY - 1, 0xFFFF00, 2);
        guiRef.addColoredLine(21, highlightTabX - 1, tabY + tabHeight + 1, highlightTabX + tabWidth + 1, tabY + tabHeight + 1, 0xFFFF00, 2);
        guiRef.addColoredLine(22, highlightTabX - 1, tabY - 1, highlightTabX - 1, tabY + tabHeight + 1, 0xFFFF00, 2);
        guiRef.addColoredLine(23, highlightTabX + tabWidth + 1, tabY - 1, highlightTabX + tabWidth + 1, tabY + tabHeight + 1, 0xFFFF00, 2);
    } catch(e) {}
 
    updateVisibleSlots(player, api);
    updateScrollIndicator();
    if (guiRef) {
        guiRef.update();
    }
}
 
function updateScrollIndicator() {
    if (!guiRef) return;
    var maxViewportRow = Math.max(0, totalRows - viewportRows);
    try {
        guiRef.removeComponent(10);
        var scrollX = startX + (numCols * colSpacing) + 2;
        var scrollY = startY;
        guiRef.addLabel(10, "§7" + (viewportRow + 1) + "/" + (maxViewportRow + 1), scrollX + 1, scrollY + 42, 0.7, 0.7);
    } catch(e) {}
}
 
function updateVisibleSlots(player, api) {
    for (var i = 0; i < mySlots.length; i++) {
        mySlots[i].setStack(null);
        var globalIndex = viewportToGlobal(i);
        var pageData = storedSlotItems[currentPage];
        if (pageData && globalIndex < pageData.length && pageData[globalIndex]) {
            try {
                var entry = pageData[globalIndex];
                var item = player.world.createItemFromNbt(api.stringToNbt(entry.displayNbt));
                var loreArr = cfg_getLore(currentPage, globalIndex, isSellMode);
                if (loreArr) {
                    item.setLore(loreArr);
                }
                mySlots[i].setStack(item);
            } catch(e) {}
        }
    }
}
 
function cfg_getLore(tabIndex, globalIndex, sellMode) {
    var items = CONFIG_SHOP_ITEMS[tabIndex] || [];
    var cfg = items[globalIndex];
    if (!cfg) return null;
    var loreArr = cfg.lore ? cfg.lore.slice() : [];
    loreArr.push("");
    if (sellMode) {
        var sellPrice = Math.max(0, Math.floor((cfg.price || 0) * (1 - SELL_LOSS_PERCENTAGE)));
        loreArr.push("\u00a76Sell Price: \u00a7e" + sellPrice + "\u00a2");
        loreArr.push("\u00a77(based on durability)");
    } else {
        loreArr.push("\u00a7aPrice: \u00a7e" + (cfg.price || 0) + "\u00a2");
    }
    return loreArr;
}
 
function calcSellPrice(cfg, playerItem) {
    var buyPrice = cfg.price || 0;
    var durabilityRatio = 1.0;
    try {
        var snbt = playerItem.getItemNbt().toJsonString();
        var damage = 0;
        var maxDurability = 0;
        var dmgMatch = snbt.match(/"Damage":\s*(\d+)/);
        if (dmgMatch) damage = parseInt(dmgMatch[1]);
        try { maxDurability = playerItem.getMaxDamage(); } catch(e) {}
        if (maxDurability > 0 && damage > 0) {
            durabilityRatio = (maxDurability - damage) / maxDurability;
            if (durabilityRatio < 0) durabilityRatio = 0;
        }
    } catch(e) {}
    return Math.max(0, Math.floor(buyPrice * durabilityRatio - buyPrice * SELL_LOSS_PERCENTAGE));
}
 
function customGuiButton(event) {
    var player = event.player;
    var api = event.API;
    var maxViewportRow = Math.max(0, totalRows - viewportRows);
 
    if (event.buttonId === ID_SCROLL_UP) {
        if (viewportRow > 0) {
            viewportRow--;
            updateVisibleSlots(player, api);
            updateScrollIndicator();
            if (guiRef) guiRef.update();
        }
        return;
    }
 
    if (event.buttonId === ID_SCROLL_DOWN) {
        if (viewportRow < maxViewportRow) {
            viewportRow++;
            updateVisibleSlots(player, api);
            updateScrollIndicator();
            if (guiRef) guiRef.update();
        }
        return;
    }
 
    if (event.buttonId === ID_MODE_TOGGLE) {
        isSellMode = !isSellMode;
        try {
            guiRef.removeComponent(ID_MODE_TOGGLE);
        } catch(e) {}
        var scrollX = startX + (numCols * colSpacing) + 2;
        var scrollY = startY;
        guiRef.addButton(ID_MODE_TOGGLE, isSellMode ? "Selling" : "Buying", scrollX, scrollY + 90, 50, 18);
        updateVisibleSlots(player, api);
        if (guiRef) guiRef.update();
        return;
    }
 
    if (event.buttonId >= ID_TAB_BASE && event.buttonId < ID_TAB_BASE + maxPages) {
        var tabIndex = event.buttonId - ID_TAB_BASE;
        if (tabIndex !== currentPage) {
            currentPage = tabIndex;
            viewportRow = 0;
            totalRows = CONFIG_TAB_ROWS[currentPage] || 5;
            storedSlotItems = buildShopDataFromConfig(player, api);
            if (!storedSlotItems[currentPage]) {
                storedSlotItems[currentPage] = makeNullArray(totalRows * numCols);
            }
            try {
                guiRef.removeComponent(20);
                guiRef.removeComponent(21);
                guiRef.removeComponent(22);
                guiRef.removeComponent(23);
            } catch(e) {}
            try {
                var tw = 25, th = 28, ts = 2, tx = 0;
                var ty = (currentPage < 6) ? -80 : 59;
                var hx = tx + (currentPage % 6) * (tw + ts);
                guiRef.addColoredLine(20, hx - 1,      ty - 1,      hx + tw + 1, ty - 1,      0xFFFF00, 2);
                guiRef.addColoredLine(21, hx - 1,      ty + th + 1, hx + tw + 1, ty + th + 1, 0xFFFF00, 2);
                guiRef.addColoredLine(22, hx - 1,      ty - 1,      hx - 1,      ty + th + 1, 0xFFFF00, 2);
                guiRef.addColoredLine(23, hx + tw + 1, ty - 1,      hx + tw + 1, ty + th + 1, 0xFFFF00, 2);
            } catch(e) {}
            updateVisibleSlots(player, api);
            updateScrollIndicator();
            if (guiRef) guiRef.update();
        }
        return;
    }
}
 
function customGuiSlotClicked(event) {
    var clickedSlot = event.slot;
    var player = event.player;
    var api = event.API;
    var slotIndex = mySlots.indexOf(clickedSlot);
 
    if (slotIndex === -1) return;
 
    var globalIndex = viewportToGlobal(slotIndex);
    var pageData = storedSlotItems[currentPage];
    if (!pageData || globalIndex >= pageData.length) return;
 
    var entry = pageData[globalIndex];
    if (!entry) return;
 
    var item = mySlots[slotIndex].getStack();
    if (!item || item.isEmpty()) return;
 
    var cfg = (CONFIG_SHOP_ITEMS[currentPage] || [])[globalIndex];
    if (!cfg) return;

    // How many items make up one "pack" for this shop entry
    var packSize = cfg.count || 1;
 
    if (isSellMode) {
        // ===== SELL MODE =====
        // Find a matching item in player inventory
        var inv = player.getInventory();
        var foundSlot = -1;
        var foundStack = null;
        for (var i = 0; i < inv.getSize(); i++) {
            var stack = inv.getSlot(i);
            if (!stack || stack.isEmpty()) continue;
            if (nbtMatchesIgnoreDamageAndLore(stack, cfg)) {
                foundSlot = i;
                foundStack = stack;
                break;
            }
        }
        if (foundSlot === -1) {
            player.message("§cYou don't have this item to sell!");
            return;
        }

        var stackSize = foundStack.getStackSize();

        // For stackable items (ammo etc.) require a full pack to sell
        if (packSize > 1) {
            if (stackSize < packSize) {
                player.message("§cYou need at least §e" + packSize + "§c of this item to sell (you have §e" + stackSize + "§c)!");
                return;
            }
            // Remove exactly one pack's worth
            if (stackSize <= packSize) {
                inv.setSlot(foundSlot, null);
            } else {
                foundStack.setStackSize(stackSize - packSize);
            }
            var sellPrice = Math.max(0, Math.floor((cfg.price || 0) * (1 - SELL_LOSS_PERCENTAGE)));
            giveCoins(player, sellPrice);
            player.message("§aSold §e" + packSize + "x §afor §e" + sellPrice + "¢!");
        } else {
            // Single-item sell (guns, armor, attachments) — use durability calc
            var sellPrice = calcSellPrice(cfg, foundStack);
            if (sellPrice <= 0) {
                player.message("§cThis item has no sell value!");
                return;
            }
            if (stackSize <= 1) {
                inv.setSlot(foundSlot, null);
            } else {
                foundStack.setStackSize(stackSize - 1);
            }

            // For armor, show durability percentage
            var isArmor = cfg.id.indexOf("_helmet") !== -1 || cfg.id.indexOf("_chestplate") !== -1
                       || cfg.id.indexOf("_leggings") !== -1 || cfg.id.indexOf("_boots") !== -1;
            if (isArmor) {
                var durabilityPct = 100;
                try {
                    var snbt = foundStack.getItemNbt().toJsonString();
                    var dmgMatch = snbt.match(/"Damage":\s*(\d+)/);
                    var damage = dmgMatch ? parseInt(dmgMatch[1]) : 0;
                    var maxDur = 0;
                    try { maxDur = foundStack.getMaxDamage(); } catch(e) {}
                    if (maxDur > 0) durabilityPct = Math.round((maxDur - damage) / maxDur * 100);
                } catch(e) {}
                player.message("§aSold item for §e" + sellPrice + "¢ §7(durability: §f" + durabilityPct + "%§7)!");
            } else {
                player.message("§aSold item for §e" + sellPrice + "¢!");
            }

            giveCoins(player, sellPrice);
        }
 
    } else {
        // ===== BUY MODE =====
        var price = entry.price;
        if (price === null || price === undefined) {
            player.message("§cThis item has no price set!");
            return;
        }
        var playerCoins = countPlayerCoins(player);
        if (playerCoins < price) {
            player.message("§cNot enough coins! Need: §e" + price + "¢ §c, Have: §e" + playerCoins + "¢");
            return;
        }
        // Block purchase if hotbar is full
        var inv2 = player.getInventory();
        var hotbarFull = true;
        for (var h = 0; h < 8; h++) {
            var hs = inv2.getSlot(h);
            if (!hs || hs.isEmpty()) { hotbarFull = false; break; }
        }
        if (hotbarFull) {
            player.message("§cYour hotbar is full! Make some room before purchasing.");
            return;
        }

        removeCoins(player, price);
        try {
            var cleanLore = cfg.lore && cfg.lore.length > 0 ? cfg.lore.slice() : null;
            var snbt = buildSnbt(cfg, cleanLore);
            var purchaseItem = player.world.createItemFromNbt(api.stringToNbt(snbt));
            if (cleanLore) purchaseItem.setLore(cleanLore);
            player.giveItem(purchaseItem);
            player.message("§aPurchased item for §e" + price + "¢!");
        } catch(e) {
            player.message("§cError purchasing item: " + e);
        }
    }
}
 
var VARIANT_KEYS = ["GunId", "AmmoId", "AttachmentId"];
 
function nbtMatchesIgnoreDamageAndLore(playerStack, cfg) {
    try {
        var snbt = playerStack.getItemNbt().toJsonString();
        var cfgNbt = cfg.nbt || {};
 
        if (snbt.indexOf('"id": "' + cfg.id + '"') === -1) return false;
 
        if (cfgNbt.AttributeModifiers && cfgNbt.AttributeModifiers.length > 0) {
            var firstUUID = cfgNbt.AttributeModifiers[0].UUID;
            if (firstUUID && snbt.indexOf(firstUUID) === -1) return false;
            return true;
        }
 
        for (var k = 0; k < VARIANT_KEYS.length; k++) {
            var key = VARIANT_KEYS[k];
            if (!cfgNbt.hasOwnProperty(key)) continue;
            var val = String(cfgNbt[key]);
            if (snbt.indexOf('"' + key + '": "' + val + '"') === -1) return false;
        }
        return true;
    } catch(e) {
        return false;
    }
}
 
function giveCoins(player, amount) {
    var emeralds = Math.floor(amount / (STONE_TO_COAL * COAL_TO_EMERALD));
    amount -= emeralds * STONE_TO_COAL * COAL_TO_EMERALD;
    var coals = Math.floor(amount / STONE_TO_COAL);
    var stones = amount % STONE_TO_COAL;
    if (emeralds > 0) player.giveItem(player.world.createItem("coins:emerald_coin", emeralds));
    if (coals    > 0) player.giveItem(player.world.createItem("coins:coal_coin",    coals));
    if (stones   > 0) player.giveItem(player.world.createItem("coins:stone_coin",   stones));
}
 
function customGuiClosed(event) {
    guiRef = null;
    viewportRow = 0;
    currentPage = 0;
    isSellMode = false;
}
 
function countPlayerCoins(player) {
    var stoneTotal = 0;
    var coalTotal = 0;
    var emeraldTotal = 0;
    var inv = player.getInventory();
    for (var i = 0; i < inv.getSize(); i++) {
        var stack = inv.getSlot(i);
        if (stack && !stack.isEmpty()) {
            var name = stack.getName();
            if      (name === "coins:stone_coin")   stoneTotal   += stack.getStackSize();
            else if (name === "coins:coal_coin")    coalTotal    += stack.getStackSize();
            else if (name === "coins:emerald_coin") emeraldTotal += stack.getStackSize();
        }
    }
    return stoneTotal + (coalTotal * STONE_TO_COAL) + (emeraldTotal * STONE_TO_COAL * COAL_TO_EMERALD);
}
 
function removeCoins(player, amount) {
    var remaining = amount;
    var inv = player.getInventory();
 
    for (var i = 0; i < inv.getSize() && remaining > 0; i++) {
        var stack = inv.getSlot(i);
        if (stack && !stack.isEmpty() && stack.getName() === "coins:stone_coin") {
            var stackAmount = stack.getStackSize();
            if (stackAmount <= remaining) { inv.setSlot(i, null); remaining -= stackAmount; }
            else { stack.setStackSize(stackAmount - remaining); remaining = 0; }
        }
    }
 
    for (var i = 0; i < inv.getSize() && remaining > 0; i++) {
        var stack = inv.getSlot(i);
        if (stack && !stack.isEmpty() && stack.getName() === "coins:coal_coin") {
            var stackAmount = stack.getStackSize();
            var stoneValue = stackAmount * STONE_TO_COAL;
            if (stoneValue <= remaining) { inv.setSlot(i, null); remaining -= stoneValue; }
            else {
                var coalsNeeded = Math.ceil(remaining / STONE_TO_COAL);
                stack.setStackSize(stackAmount - coalsNeeded);
                var overpaid = (coalsNeeded * STONE_TO_COAL) - remaining;
                remaining = 0;
                if (overpaid > 0) player.giveItem(player.world.createItem("coins:stone_coin", overpaid));
            }
        }
    }
 
    for (var i = 0; i < inv.getSize() && remaining > 0; i++) {
        var stack = inv.getSlot(i);
        if (stack && !stack.isEmpty() && stack.getName() === "coins:emerald_coin") {
            var stackAmount = stack.getStackSize();
            var stoneValue = stackAmount * STONE_TO_COAL * COAL_TO_EMERALD;
            if (stoneValue <= remaining) { inv.setSlot(i, null); remaining -= stoneValue; }
            else {
                var emeraldsNeeded = Math.ceil(remaining / (STONE_TO_COAL * COAL_TO_EMERALD));
                stack.setStackSize(stackAmount - emeraldsNeeded);
                var overpaid = (emeraldsNeeded * STONE_TO_COAL * COAL_TO_EMERALD) - remaining;
                remaining = 0;
                var changeCoal  = Math.floor(overpaid / STONE_TO_COAL);
                var changeStone = overpaid % STONE_TO_COAL;
                if (changeCoal  > 0) player.giveItem(player.world.createItem("coins:coal_coin",  changeCoal));
                if (changeStone > 0) player.giveItem(player.world.createItem("coins:stone_coin", changeStone));
            }
        }
    }
}