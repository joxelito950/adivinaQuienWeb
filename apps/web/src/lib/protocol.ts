// ─── Enums ────────────────────────────────────────────────────────────────────
// Valores de wire deben coincidir exactamente con los del backend Java.

export enum Difficulty {
  SMALL  = 'SMALL',
  MEDIUM = 'MEDIUM',
  LARGE  = 'LARGE',
}

export type DifficultyWire = 'small' | 'medium' | 'large'

export enum QuestionKey {
  USES_GLASSES    = 'USES_GLASSES',
  HAS_BEARD       = 'HAS_BEARD',
  HAS_HAT         = 'HAS_HAT',
  HAS_BLONDE_HAIR = 'HAS_BLONDE_HAIR',
  HAS_BLUE_EYES   = 'HAS_BLUE_EYES',
  HAS_EARRINGS    = 'HAS_EARRINGS',
}

export enum GameStatus {
  WAITING     = 'WAITING',
  IN_PROGRESS = 'IN_PROGRESS',
  FINISHED    = 'FINISHED',
  ABANDONED   = 'ABANDONED',
}

export enum PlayerType {
  HUMAN = 'HUMAN',
  DUMMY = 'DUMMY',
}

// ─── Domain types ─────────────────────────────────────────────────────────────

export interface CharacterCard {
  characterId: string
  displayName:  string
  attributes?:  QuestionKey[]
}

export interface Board {
  rows:       number
  cols:       number
  characters: CharacterCard[]
}

export interface PlayerState {
  playerId:        string
  type:            PlayerType
  connected:       boolean
  disconnectedAt?: string | null
}

// ─── Wire envelope ────────────────────────────────────────────────────────────

export interface WireMessage<T extends string, P> {
  type:           T
  correlationId?: string
  payload:        P
}

// ─── Client → Server commands ─────────────────────────────────────────────────

export type JoinQueueCommand      = WireMessage<'join_queue',      { playerId: string; difficulty: string }>
export type LeaveQueueCommand     = WireMessage<'leave_queue',     { playerId: string }>
export type AskQuestionCommand    = WireMessage<'ask_question',    { gameId: string; playerId: string; questionKey: string }>
export type GuessCharacterCommand = WireMessage<'guess_character', { gameId: string; playerId: string; characterId: string }>
export type ReconnectGameCommand  = WireMessage<'reconnect_game',  { gameId: string; playerId: string }>
export type PingCommand           = WireMessage<'ping',            Record<string, never>>

export type ClientMessage =
  | JoinQueueCommand
  | LeaveQueueCommand
  | AskQuestionCommand
  | GuessCharacterCommand
  | ReconnectGameCommand
  | PingCommand

// ─── Server → Client events ───────────────────────────────────────────────────

export type QueueJoinedEvent = WireMessage<'queue_joined', {
  difficulty: DifficultyWire
}>

export type QueueWaitingEvent = WireMessage<'queue_waiting', {
  timeoutSeconds: number
}>

export type GameStartedEvent = WireMessage<'game_started', {
  gameId:                string
  difficulty:            DifficultyWire
  opponentType:          'human' | 'dummy'
  board:                 Board
  yourSecretCharacterId: string
  firstTurnPlayerId:     string
}>

export type TurnChangedEvent = WireMessage<'turn_changed', {
  gameId: string
  currentTurnPlayerId: string
}>

export type QuestionAnsweredEvent = WireMessage<'question_answered', {
  gameId:           string
  playerId:         string
  questionKey:     string
  answer:          boolean
}>

export type GuessResultEvent = WireMessage<'guess_result', {
  gameId:             string
  playerId:           string
  characterId:       string
  correct:           boolean
}>

export type InvalidActionEvent = WireMessage<'invalid_action', {
  reason: string
}>

export type PlayerDisconnectedEvent = WireMessage<'player_disconnected', {
  playerId: string
  at:       string
}>

export type GameFinishedEvent = WireMessage<'game_finished', {
  gameId:         string
  winnerPlayerId: string
}>

export type ReconnectedEvent = WireMessage<'reconnected', {
  gameId: string
  currentTurnPlayerId: string
  status: 'waiting' | 'in_progress' | 'finished' | 'abandoned'
}>

export type ErrorEvent = WireMessage<'error', {
  reason: string
}>

export type QueueLeftEvent = WireMessage<'queue_left', {
  playerId: string
}>

export type PongEvent = WireMessage<'pong', {
  ts: string
}>

export type ServerMessage =
  | QueueJoinedEvent
  | QueueWaitingEvent
  | GameStartedEvent
  | TurnChangedEvent
  | QuestionAnsweredEvent
  | GuessResultEvent
  | InvalidActionEvent
  | PlayerDisconnectedEvent
  | GameFinishedEvent
  | ReconnectedEvent
  | QueueLeftEvent
  | PongEvent
  | ErrorEvent

export function toWireDifficulty(value: Difficulty): DifficultyWire {
  return value.toLowerCase() as DifficultyWire
}

export function fromWireDifficulty(value: string): Difficulty {
  const upper = value.trim().toUpperCase() as keyof typeof Difficulty
  return Difficulty[upper] ?? Difficulty.SMALL
}

export function parseServerMessage(raw: string): ServerMessage | null {
  try {
    const parsed = JSON.parse(raw) as { type?: unknown; payload?: unknown; correlationId?: unknown }
    if (typeof parsed !== 'object' || parsed === null) return null
    if (typeof parsed.type !== 'string') return null
    const payload = typeof parsed.payload === 'object' && parsed.payload !== null
      ? parsed.payload
      : {}
    return {
      type: parsed.type,
      payload: payload as Record<string, unknown>,
      correlationId: typeof parsed.correlationId === 'string' ? parsed.correlationId : undefined,
    } as ServerMessage
  } catch {
    return null
  }
}
