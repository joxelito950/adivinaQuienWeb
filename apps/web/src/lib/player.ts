const PLAYER_ID_KEY = 'adivinaquien.playerId'

function generateFallbackId(): string {
  return `player-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 10)}`
}

export function getPlayerId(): string {
  if (typeof window === 'undefined') return 'server-player'

  const existing = window.localStorage.getItem(PLAYER_ID_KEY)
  if (existing && existing.trim().length > 0) {
    return existing
  }

  const nextId = typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function'
    ? crypto.randomUUID()
    : generateFallbackId()

  window.localStorage.setItem(PLAYER_ID_KEY, nextId)
  return nextId
}
