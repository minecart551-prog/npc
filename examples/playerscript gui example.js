var lastSeen = {}
function init(event){
    event.player.getTimers().forceStart(69420,0,true)
}
function timer(event) {
    if (event.id == 69420) {
        var player = event.player
        var api = event.API
        var ray = player.rayTraceEntities(16, false, false)
        var entity = null
        if (ray && ray.length && ray.length > 0) {
            entity = ray[0]
            lastSeen[player.getName()] = Date.now()
        } else {
            var last = lastSeen[player.getName()]
            if (!last || (Date.now() - last) > 2000) {
                player.hideOverlay(1)
                return
            }
            entity = null
        }
        if (!entity) return
        var health = Number(entity.getHealth()) || 0
        var maxHealth = Number(entity.getMaxHealth()) || 0
        var pct = maxHealth > 0 ? (health / maxHealth) : 0
        var name = String(entity.getName())
        var color = '§a'
        if (pct <= 0.3) {
            color = '§c'
        } else if (pct <= 0.5) {
            color = '§e'
        }
        var labelText = color + Math.round(health) + "/" + Math.round(maxHealth)
        var barWidth = 20
        var pctClamped = Math.max(0, Math.min(1, pct))
        var filledCount = Math.round(pctClamped * barWidth)
        function repeatChar(ch, n) {
            ch = String(ch || '')
            n = Math.max(0, Math.floor(Number(n) || 0))
            if (n <= 0) return ''
            return new Array(n + 1).join(ch)
        }
        var filledGlyph = '█'
        var emptyGlyph = '█'
        var filledPart = repeatChar(filledGlyph, filledCount)
        var emptyPart = repeatChar(emptyGlyph, barWidth - filledCount)
        var barText = '§7[' + color + filledPart + '§7' + '§7' + emptyPart + '§7]'
        var overlay = api.createOverlay(1) // overlay id 1
        overlay.addLabel(3, name, -7, 50)
        overlay.addLabel(1, labelText, -7, 55)
        overlay.addLabel(2, barText, -45, 60)
        player.showOverlay(overlay)
    }
}