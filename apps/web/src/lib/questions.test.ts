import { describe, expect, it } from 'vitest'
import { QuestionKey } from '@/lib/protocol'
import { ACTIVE_QUESTION_KEYS, QUESTION_LABELS } from '@/lib/questions'

describe('question policy in frontend', () => {
  it('exposes full question subset in phase 3', () => {
    expect(ACTIVE_QUESTION_KEYS).toEqual([
      QuestionKey.USES_GLASSES,
      QuestionKey.HAS_BEARD,
      QuestionKey.HAS_HAT,
      QuestionKey.HAS_BLONDE_HAIR,
      QuestionKey.HAS_LONG_HAIR,
      QuestionKey.HAS_SHORT_HAIR,
      QuestionKey.HAS_STRAIGHT_HAIR,
      QuestionKey.HAS_CURLY_HAIR,
      QuestionKey.HAS_EARRINGS,
      QuestionKey.IS_MALE,
      QuestionKey.IS_FEMALE,
      QuestionKey.IS_BALD,
      QuestionKey.HAS_FAIR_SKIN,
      QuestionKey.HAS_DARK_SKIN,
    ])
  })

  it('includes every key in QuestionKey enum', () => {
    expect(new Set(ACTIVE_QUESTION_KEYS)).toEqual(new Set(Object.values(QuestionKey)))
  })

  it('keeps labels available for every QuestionKey', () => {
    for (const key of Object.values(QuestionKey)) {
      expect(QUESTION_LABELS[key]).toBeTruthy()
    }
  })
})
