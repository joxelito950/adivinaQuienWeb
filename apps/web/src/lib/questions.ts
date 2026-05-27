import { QuestionKey } from '@/lib/protocol'

export const QUESTION_LABELS: Record<QuestionKey, string> = {
  [QuestionKey.USES_GLASSES]: '¿Usa lentes?',
  [QuestionKey.HAS_BEARD]: '¿Tiene barba?',
  [QuestionKey.HAS_HAT]: '¿Usa sombrero?',
  [QuestionKey.HAS_BLONDE_HAIR]: '¿Tiene pelo rubio?',
  [QuestionKey.HAS_LONG_HAIR]: '¿Tiene cabello largo?',
  [QuestionKey.HAS_SHORT_HAIR]: '¿Tiene cabello corto?',
  [QuestionKey.HAS_STRAIGHT_HAIR]: '¿Tiene cabello liso?',
  [QuestionKey.HAS_CURLY_HAIR]: '¿Tiene cabello crespo?',
  [QuestionKey.HAS_EARRINGS]: '¿Tiene aretes?',
  [QuestionKey.IS_MALE]: '¿Es hombre?',
  [QuestionKey.IS_FEMALE]: '¿Es mujer?',
  [QuestionKey.IS_BALD]: '¿Es calvo?',
  [QuestionKey.HAS_FAIR_SKIN]: '¿Es de tez clara?',
  [QuestionKey.HAS_DARK_SKIN]: '¿Es de tez oscura?',
}

// Fase 3: lista activa completa para usar todo el catalogo de preguntas.
export const ACTIVE_QUESTION_KEYS: QuestionKey[] = [
  QuestionKey.USES_GLASSES,
  QuestionKey.HAS_BEARD,
  QuestionKey.HAS_HAT,
  QuestionKey.HAS_LONG_HAIR,
  QuestionKey.HAS_SHORT_HAIR,
  QuestionKey.HAS_STRAIGHT_HAIR,
  QuestionKey.HAS_CURLY_HAIR,
  QuestionKey.HAS_EARRINGS,
  QuestionKey.IS_MALE,
  QuestionKey.IS_FEMALE,
  QuestionKey.IS_BALD,
]
