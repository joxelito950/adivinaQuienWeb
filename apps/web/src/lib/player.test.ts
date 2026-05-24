import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getPlayerId } from '@/lib/player'

describe('player identity', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('creates and persists a new player id if it does not exist', () => {
    const randomUUID = vi.fn(() => 'uuid-1')
    Object.defineProperty(globalThis, 'crypto', {
      value: { randomUUID },
      configurable: true,
    })

    const first = getPlayerId()
    const second = getPlayerId()

    expect(first).toBe('uuid-1')
    expect(second).toBe('uuid-1')
    expect(localStorage.getItem('adivinaquien.playerId')).toBe('uuid-1')
    expect(randomUUID).toHaveBeenCalledTimes(1)
  })

  it('uses existing player id from localStorage', () => {
    localStorage.setItem('adivinaquien.playerId', 'player-existing')

    const value = getPlayerId()

    expect(value).toBe('player-existing')
  })
})
