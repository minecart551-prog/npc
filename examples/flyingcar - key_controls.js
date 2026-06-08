var FLIGHT_CONTROL_NAME = "Flight Control";
var CAR_BASE_NAME = "FC";

// ========== Key Pressed ==========

function keyPressed(e) {
    var p = e.player;
    var n = p.getMount();

    // Only handle keys when mounted on an NPC whose name starts with FC
    if (!n || n.getType() !== 2) return;

    var npcName = n.getDisplay().getName();
    if (npcName.indexOf(CAR_BASE_NAME) !== 0 && npcName.indexOf(CAR_BASE_NAME.toLowerCase()) !== 0) return;

    // Store pressed state in the NPC's storeddata
    n.getStoreddata().put("key_" + e.key, "true");
}

// ========== Key Released ==========

function keyReleased(e) {
    var p = e.player;
    var n = p.getMount();

    if (!n || n.getType() !== 2) return;

    var npcName = n.getDisplay().getName();
    if (npcName.indexOf(CAR_BASE_NAME) !== 0 && npcName.indexOf(CAR_BASE_NAME.toLowerCase()) !== 0) return;

    // Mark released state in the NPC's storeddata
    n.getStoreddata().put("key_" + e.key, "false");
}
