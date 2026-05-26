import { QuestionKey } from '@/lib/protocol'

export const QUESTION_LABELS: Record<QuestionKey, string> = {
  [QuestionKey.USES_GLASSES]: '¿Usa lentes?',
  [QuestionKey.HAS_BEARD]: '¿Tiene barba?',
  [QuestionKey.HAS_HAT]: '¿Usa sombrero?',
  [QuestionKey.HAS_BLONDE_HAIR]: '¿Tiene pelo rubio?',
  [QuestionKey.HAS_BLUE_EYES]: '¿Tiene ojos azules?',
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
  QuestionKey.HAS_BLONDE_HAIR,
  QuestionKey.HAS_BLUE_EYES,
  QuestionKey.HAS_EARRINGS,
  QuestionKey.IS_MALE,
  QuestionKey.IS_FEMALE,
  QuestionKey.IS_BALD,
  QuestionKey.HAS_FAIR_SKIN,
  QuestionKey.HAS_DARK_SKIN,
]
