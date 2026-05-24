import { describe, expect, it } from 'vitest'
import {
  Difficulty,
  fromWireDifficulty,
  parseServerMessage,
  toWireDifficulty,
} from '@/lib/protocol'

describe('protocol helpers', () => {
  it('maps enum difficulty to wire format', () => {
    expect(toWireDifficulty(Difficulty.SMALL)).toBe('small')
    expect(toWireDifficulty(Difficulty.MEDIUM)).toBe('medium')
    expect(toWireDifficulty(Difficulty.LARGE)).toBe('large')
  })

  it('maps wire format to enum difficulty', () => {
    expect(fromWireDifficulty('small')).toBe(Difficulty.SMALL)
    expect(fromWireDifficulty('MEDIUM')).toBe(Difficulty.MEDIUM)
    expect(fromWireDifficulty('large')).toBe(Difficulty.LARGE)
  })

  it('falls back to SMALL for invalid difficulty strings', () => {
    expect(fromWireDifficulty('unknown')).toBe(Difficulty.SMALL)
  })

  it('parses valid server message envelope', () => {
    const raw = JSON.stringify({
      type: 'queue_waiting',
      correlationId: 'abc-123',
      payload: { timeoutSeconds: 60 },
    })

    const parsed = parseServerMessage(raw)
    expect(parsed).not.toBeNull()
    expect(parsed?.type).toBe('queue_waiting')
    expect(parsed?.correlationId).toBe('abc-123')
    expect(parsed?.payload).toEqual({ timeoutSeconds: 60 })
  })

  it('returns null for invalid json', () => {
    expect(parseServerMessage('{bad json')).toBeNull()
  })

  it('returns null when type is missing', () => {
    const parsed = parseServerMessage(JSON.stringify({ payload: {} }))
    expect(parsed).toBeNull()
  })

  it('normalizes non-object payload to empty object', () => {
    const parsed = parseServerMessage(JSON.stringify({ type: 'pong', payload: 'text' }))
    expect(parsed?.payload).toEqual({})
  })
})
