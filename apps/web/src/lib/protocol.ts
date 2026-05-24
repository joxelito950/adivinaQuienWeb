// ─── Enums ────────────────────────────────────────────────────────────────────
// Valores de wire deben coincidir exactamente con los del backend Java.

export enum Difficulty {
  SMALL  = 'SMALL',
  MEDIUM = 'MEDIUM',
  LARGE  = 'LARGE',
}

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
  attributes:   QuestionKey[]
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

export type JoinQueueCommand      = WireMessage<'join_queue',      { difficulty: string }>
export type LeaveQueueCommand     = WireMessage<'leave_queue',     Record<string, never>>
export type AskQuestionCommand    = WireMessage<'ask_question',    { questionKey: string }>
export type GuessCharacterCommand = WireMessage<'guess_character', { characterId: string }>
export type ReconnectGameCommand  = WireMessage<'reconnect_game',  { gameId: string }>
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
  playerId:   string
  difficulty: string
}>

export type QueueWaitingEvent = WireMessage<'queue_waiting', {
  playerId:   string
  difficulty: string
}>

export type GameStartedEvent = WireMessage<'game_started', {
  gameId:              string
  difficulty:          Difficulty
  board:               Board
  secretCharacterId:   string
  currentTurnPlayerId: string
  yourPlayerId:        string
}>

export type TurnChangedEvent = WireMessage<'turn_changed', {
  currentTurnPlayerId: string
}>

export type QuestionAnsweredEvent = WireMessage<'question_answered', {
  questionKey:     string
  answer:          boolean
  askedByPlayerId: string
}>

export type GuessResultEvent = WireMessage<'guess_result', {
  characterId:       string
  correct:           boolean
  guessedByPlayerId: string
}>

export type InvalidActionEvent = WireMessage<'invalid_action', {
  reason: string
}>

export type PlayerDisconnectedEvent = WireMessage<'player_disconnected', {
  playerId:               string
  reconnectWindowSeconds: number
}>

export type GameFinishedEvent = WireMessage<'game_finished', {
  winnerId: string
  reason:   string
}>

export type ReconnectedEvent = WireMessage<'reconnected', {
  gameId:              string
  board:               Board
  secretCharacterId:   string
  currentTurnPlayerId: string
}>

export type ErrorEvent = WireMessage<'error', {
  message: string
  code?:   string
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
  | ErrorEvent
