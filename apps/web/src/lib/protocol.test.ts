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

  it('parses question_asked payloads', () => {
    const raw = JSON.stringify({
      type: 'question_asked',
      payload: {
        gameId: 'g-1',
        playerId: 'p2',
        questionKey: 'USES_GLASSES',
      },
    })

    const parsed = parseServerMessage(raw)
    const payload = parsed?.payload as { questionKey?: string; playerId?: string }
    expect(parsed?.type).toBe('question_asked')
    expect(payload.playerId).toBe('p2')
    expect(payload.questionKey).toBe('USES_GLASSES')
  })

  it('parses question_pending payloads', () => {
    const raw = JSON.stringify({
      type: 'question_pending',
      payload: {
        gameId: 'g-1',
        askerPlayerId: 'p1',
        defenderPlayerId: 'p2',
        questionKey: 'HAS_BEARD',
        timeoutSeconds: 15,
      },
    })

    const parsed = parseServerMessage(raw)
    const payload = parsed?.payload as { timeoutSeconds?: number; defenderPlayerId?: string }
    expect(parsed?.type).toBe('question_pending')
    expect(payload.defenderPlayerId).toBe('p2')
    expect(payload.timeoutSeconds).toBe(15)
  })

  it('keeps imageUrl in game_started board characters', () => {
    const raw = JSON.stringify({
      type: 'game_started',
      payload: {
        gameId: 'g-1',
        difficulty: 'small',
        opponentType: 'human',
        board: {
          rows: 3,
          cols: 4,
          characters: [
            {
              characterId: 'char-1',
              displayName: 'Character 1',
              imageUrl: '/characters/png/char-01.png',
            },
          ],
        },
        yourSecretCharacterId: 'char-1',
        firstTurnPlayerId: 'p1',
        activeQuestionKeys: ['USES_GLASSES', 'HAS_BEARD'],
      },
    })

    const parsed = parseServerMessage(raw)
    const payload = parsed?.payload as { board?: { characters?: Array<{ imageUrl?: string }> } }
    expect(payload.board?.characters?.[0]?.imageUrl).toBe('/characters/png/char-01.png')
  })

  it('parses auto_action_triggered payloads', () => {
    const raw = JSON.stringify({
      type: 'auto_action_triggered',
      payload: {
        gameId: 'g-1',
        playerId: 'p1',
        action: 'guess_character',
        characterId: 'char-9',
        candidateCount: 3,
        correct: false,
      },
    })

    const parsed = parseServerMessage(raw)
    const payload = parsed?.payload as { action?: string; candidateCount?: number; characterId?: string }
    expect(parsed?.type).toBe('auto_action_triggered')
    expect(payload.action).toBe('guess_character')
    expect(payload.candidateCount).toBe(3)
    expect(payload.characterId).toBe('char-9')
  })

  it('parses candidates_updated payloads', () => {
    const raw = JSON.stringify({
      type: 'candidates_updated',
      payload: {
        gameId: 'g-1',
        playerId: 'p1',
        eliminatedCharacterIds: ['char-2', 'char-7'],
        candidateCount: 9,
      },
    })

    const parsed = parseServerMessage(raw)
    const payload = parsed?.payload as { eliminatedCharacterIds?: string[]; candidateCount?: number }
    expect(parsed?.type).toBe('candidates_updated')
    expect(payload.eliminatedCharacterIds).toEqual(['char-2', 'char-7'])
    expect(payload.candidateCount).toBe(9)
  })
})
