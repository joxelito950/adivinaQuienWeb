'use client'

import { use, useEffect, useMemo, useRef, useState } from 'react'
import { useRouter } from 'next/navigation'
import { BoardGrid } from '@/components/game/BoardGrid'
import { TurnIndicator } from '@/components/game/TurnIndicator'
import { ActionPanel, ActionTab } from '@/components/game/ActionPanel'
import { GameLog } from '@/components/game/GameLog'
import type { LogEntry } from '@/components/game/GameLog'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { getPlayerId } from '@/lib/player'
import { QUESTION_LABELS } from '@/lib/questions'
import { createWsClient, WsClient } from '@/lib/ws-client'
import {
  Board,
  DifficultyWire,
  fromWireDifficulty,
  parseServerMessage,
  QuestionKey,
  ServerMessage,
} from '@/lib/protocol'

interface PageProps {
  params: Promise<{ gameId: string }>
}

interface StoredGameSnapshot {
  gameId: string
  difficulty: DifficultyWire
  board: Board
  yourSecretCharacterId: string
  firstTurnPlayerId: string
  opponentType: 'human' | 'dummy'
}

interface OpponentQuestionSummary {
  key: QuestionKey
  answer: boolean | null
  timeoutFallback?: boolean
}

interface PendingQuestionState {
  askerPlayerId: string
  defenderPlayerId: string
  key: QuestionKey
  deadlineMs: number
}

function readStoredSnapshot(gameId: string): StoredGameSnapshot | null {
  if (typeof window === 'undefined') return null
  const raw = sessionStorage.getItem(`adivinaquien.game.${gameId}`)
  if (!raw) return null
  try {
    return JSON.parse(raw) as StoredGameSnapshot
  } catch {
    return null
  }
}

export default function GamePage({ params }: PageProps) {
  const { gameId } = use(params)
  const router = useRouter()

  const snapshot = useMemo(() => readStoredSnapshot(gameId), [gameId])
  const playerIdRef = useRef<string>('')
  const wsClientRef = useRef<WsClient | null>(null)

  const [difficulty, setDifficulty] = useState<DifficultyWire>(snapshot?.difficulty ?? 'small')
  const [board, setBoard] = useState<Board | null>(snapshot?.board ?? null)
  const [secretCharacterId, setSecretCharacterId] = useState<string | null>(snapshot?.yourSecretCharacterId ?? null)
  const [currentTurnPlayerId, setCurrentTurnPlayerId] = useState<string>(snapshot?.firstTurnPlayerId ?? '')
  const [gameStatus, setGameStatus] = useState<'in_progress' | 'waiting' | 'finished' | 'abandoned'>('in_progress')
  const [winnerPlayerId, setWinnerPlayerId] = useState<string | null>(null)
  const [connectionStatus, setConnectionStatus] = useState<'connecting' | 'connected' | 'error'>('connecting')
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [logEntries, setLogEntries] = useState<LogEntry[]>([])
  const [latestOpponentQuestion, setLatestOpponentQuestion] = useState<OpponentQuestionSummary | null>(null)
  const [pendingQuestion, setPendingQuestion] = useState<PendingQuestionState | null>(null)
  const [nowMs, setNowMs] = useState<number>(Date.now())
  const [activeTab, setActiveTab] = useState<ActionTab>('question')
  const [selectedGuessCharacterId, setSelectedGuessCharacterId] = useState<string | null>(null)

  const myPlayerId = playerIdRef.current
  const isGameFinished = gameStatus === 'finished'
  const isMyTurn = !isGameFinished && !!myPlayerId && currentTurnPlayerId === myPlayerId

  useEffect(() => {
    const timer = window.setInterval(() => {
      setNowMs(Date.now())
    }, 1000)
    return () => window.clearInterval(timer)
  }, [])

  useEffect(() => {
    playerIdRef.current = getPlayerId()

    const client = createWsClient({
      onOpen: () => {
        setConnectionStatus('connected')
        setErrorMessage(null)
        client?.send({
          type: 'reconnect_game',
          payload: {
            gameId,
            playerId: playerIdRef.current,
          },
        })
      },
      onMessage: (raw) => {
        const parsed = parseServerMessage(raw)
        if (!parsed) return
        handleEvent(parsed)
      },
      onClose: () => {
        setConnectionStatus((prev) => (prev === 'error' ? prev : 'connecting'))
      },
      onError: () => {
        setConnectionStatus('error')
        setErrorMessage('La conexión con el backend falló.')
      },
    })

    wsClientRef.current = client

    function handleEvent(message: ServerMessage) {
      if (message.type === 'turn_changed' && message.payload.gameId === gameId) {
        setCurrentTurnPlayerId(message.payload.currentTurnPlayerId)
        return
      }

      if (message.type === 'question_asked' && message.payload.gameId === gameId) {
        if (message.payload.playerId !== playerIdRef.current) {
          const key = String(message.payload.questionKey).toUpperCase() as QuestionKey
          setLatestOpponentQuestion({ key, answer: null })
        }
        return
      }

      if (message.type === 'question_pending' && message.payload.gameId === gameId) {
        const key = String(message.payload.questionKey).toUpperCase() as QuestionKey
        const timeoutSeconds = Number(message.payload.timeoutSeconds ?? 15)
        setPendingQuestion({
          askerPlayerId: message.payload.askerPlayerId,
          defenderPlayerId: message.payload.defenderPlayerId,
          key,
          deadlineMs: Date.now() + Math.max(0, timeoutSeconds) * 1000,
        })
        return
      }

      if (message.type === 'question_answered' && message.payload.gameId === gameId) {
        const key = String(message.payload.questionKey).toUpperCase() as QuestionKey
        const byMe = message.payload.playerId === playerIdRef.current
        setPendingQuestion(null)
        if (!byMe) {
          setLatestOpponentQuestion({
            key,
            answer: Boolean(message.payload.answer),
            timeoutFallback: Boolean(message.payload.timeoutFallback),
          })
        }
        setLogEntries((prev) => [
          ...prev,
          {
            type: 'question',
            by: byMe ? 'Tú' : 'Oponente',
            key,
            answer: Boolean(message.payload.answer),
          },
        ])
        return
      }

      if (message.type === 'guess_result' && message.payload.gameId === gameId) {
        const byMe = message.payload.playerId === playerIdRef.current
        const characterName = board?.characters.find((c) => c.characterId === message.payload.characterId)?.displayName
          ?? message.payload.characterId

        setLogEntries((prev) => [
          ...prev,
          {
            type: 'guess',
            by: byMe ? 'Tú' : 'Oponente',
            characterName,
            correct: Boolean(message.payload.correct),
          },
        ])

        if (message.payload.correct) {
          setGameStatus('finished')
          setWinnerPlayerId(message.payload.playerId)
          setCurrentTurnPlayerId('')
          setPendingQuestion(null)
          setSelectedGuessCharacterId(message.payload.characterId)
        } else {
          setSelectedGuessCharacterId(null)
          setActiveTab('question')
        }
        return
      }

      if (message.type === 'auto_action_triggered' && message.payload.gameId === gameId) {
        const byMe = message.payload.playerId === playerIdRef.current
        const actionLabel = message.payload.action === 'guess_character' ? 'adivinanza automática' : 'pregunta automática'
        setLogEntries((prev) => [
          ...prev,
          {
            type: 'event',
            message: `${byMe ? 'Tu turno' : 'Turno del oponente'}: ${actionLabel} por 30s de inactividad.`,
          },
        ])
        return
      }

      if (message.type === 'player_disconnected') {
        const byMe = message.payload.playerId === playerIdRef.current
        setLogEntries((prev) => [
          ...prev,
          {
            type: 'event',
            message: byMe
              ? 'Tu conexión se perdió. Intentando reconectar…'
              : 'Tu oponente se desconectó temporalmente.',
          },
        ])
        return
      }

      if (message.type === 'game_finished' && message.payload.gameId === gameId) {
        setGameStatus('finished')
        setWinnerPlayerId(message.payload.winnerPlayerId)
        setCurrentTurnPlayerId('')
        setPendingQuestion(null)
        setLogEntries((prev) => [
          ...prev,
          {
            type: 'event',
            message: message.payload.winnerPlayerId === playerIdRef.current
              ? '¡Ganaste la partida!'
              : 'La partida terminó. Ganó tu oponente.',
          },
        ])
        return
      }

      if (message.type === 'reconnected' && message.payload.gameId === gameId) {
        setCurrentTurnPlayerId(message.payload.currentTurnPlayerId)
        setGameStatus(message.payload.status)
        return
      }

      if (message.type === 'invalid_action') {
        setErrorMessage(message.payload.reason)
        return
      }

      if (message.type === 'error') {
        setErrorMessage(message.payload.reason)
      }
    }

    return () => {
      wsClientRef.current?.close()
      wsClientRef.current = null
    }
  }, [gameId])

  function sendAskQuestion(questionKey: QuestionKey) {
    wsClientRef.current?.send({
      type: 'ask_question',
      payload: {
        gameId,
        playerId: playerIdRef.current,
        questionKey,
      },
    })
  }

  function sendGuessCharacter(characterId: string) {
    wsClientRef.current?.send({
      type: 'guess_character',
      payload: {
        gameId,
        playerId: playerIdRef.current,
        characterId,
      },
    })
  }

  function confirmGuessFromCard(characterId: string) {
    sendGuessCharacter(characterId)
  }

  function sendAnswerQuestion(answer: boolean) {
    wsClientRef.current?.send({
      type: 'answer_question',
      payload: {
        gameId,
        playerId: playerIdRef.current,
        answer,
      },
    })
  }

  if (!board || !secretCharacterId) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center gap-3 text-center">
        <h1 className="text-2xl font-bold text-slate-100">No hay estado de partida cargado</h1>
        <p className="max-w-md text-sm text-slate-400">
          Abre primero la cola para recibir game_started y luego entrar en esta ruta.
        </p>
      </main>
    )
  }

  const pendingSeconds = pendingQuestion
    ? Math.max(0, Math.ceil((pendingQuestion.deadlineMs - nowMs) / 1000))
    : 0
  const isDefenderWaiting = !!pendingQuestion && pendingQuestion.defenderPlayerId === myPlayerId
  const hasPendingQuestion = !!pendingQuestion
  const selectedGuessCharacterName = selectedGuessCharacterId
    ? board.characters.find((character) => character.characterId === selectedGuessCharacterId)?.displayName ?? null
    : null

  const opponentQuestionNote = pendingQuestion
    ? isDefenderWaiting
      ? `Responde: ${QUESTION_LABELS[pendingQuestion.key]} (${pendingSeconds}s)`
      : `Esperando respuesta del oponente: ${QUESTION_LABELS[pendingQuestion.key]} (${pendingSeconds}s)`
    : latestOpponentQuestion
      ? latestOpponentQuestion.answer === null
        ? `Oponente preguntó: ${QUESTION_LABELS[latestOpponentQuestion.key]}`
        : `Oponente preguntó: ${QUESTION_LABELS[latestOpponentQuestion.key]}. ${latestOpponentQuestion.timeoutFallback ? 'Respuesta automática por timeout' : 'Respuesta manual'}: ${latestOpponentQuestion.answer ? 'Sí' : 'No'}.`
      : null

  const turnIndicatorNote = isGameFinished
    ? winnerPlayerId
      ? winnerPlayerId === myPlayerId
        ? 'Puedes revisar el historial para ver la jugada final.'
        : 'La jugada final quedó registrada en el historial.'
      : null
    : opponentQuestionNote

  return (
    <main className="py-6 sm:py-8 lg:py-10">
      <header className="mb-5 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-xl font-bold text-slate-100 sm:text-2xl">Adivina Quién</h1>
          <p className="text-xs text-slate-500">
            Partida:{' '}
            <span className="font-mono text-slate-400">{gameId}</span>
          </p>
          <p className="text-xs text-slate-500">Conexión: {connectionStatus}</p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Badge variant="info">Dificultad: {difficulty}</Badge>
          <Badge variant={gameStatus === 'finished' ? 'warning' : 'success'}>
            {gameStatus === 'finished' ? 'Finalizada' : 'En progreso'}
          </Badge>
          {winnerPlayerId && (
            <Badge variant={winnerPlayerId === myPlayerId ? 'success' : 'danger'}>
              {winnerPlayerId === myPlayerId ? 'Ganaste' : 'Perdiste'}
            </Badge>
          )}
        </div>
      </header>

      {errorMessage && (
        <div className="mb-4 rounded-xl border border-red-700 bg-red-900/20 px-4 py-2 text-sm text-red-300">
          {errorMessage}
        </div>
      )}

      <TurnIndicator
        isMyTurn={isMyTurn}
        isFinished={isGameFinished}
        didIWin={winnerPlayerId === myPlayerId}
        opponentName="Oponente"
        note={turnIndicatorNote}
        className="mb-6"
      />

      {isGameFinished && (
        <section className="mb-6 rounded-2xl border border-brand-700/50 bg-brand-900/20 p-4">
          <h2 className="text-sm font-semibold text-brand-200">Partida finalizada</h2>
          <p className="mt-1 text-sm text-brand-100/90">
            Puedes volver al inicio para comenzar una nueva partida.
          </p>
          <div className="mt-3 flex flex-col gap-2 sm:flex-row sm:items-center">
            <Button onClick={() => router.push('/')}>
              Jugar otra partida
            </Button>
          </div>
        </section>
      )}

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[1fr_320px] lg:items-start">
        <section>
          <h2 className="mb-3 text-xs font-semibold uppercase tracking-wider text-slate-400">
            Tablero de candidatos
          </h2>
          <BoardGrid
            board={board}
            difficulty={fromWireDifficulty(difficulty)}
            secretCharacterId={secretCharacterId}
            eliminatedIds={[]}
            selectable={isMyTurn && gameStatus === 'in_progress' && !hasPendingQuestion && activeTab === 'guess'}
            selectedId={selectedGuessCharacterId ?? undefined}
            onSelect={setSelectedGuessCharacterId}
            onConfirmSelected={confirmGuessFromCard}
          />
        </section>

        <aside className="flex flex-col gap-4 lg:sticky lg:top-6">
          <ActionPanel
            isMyTurn={isMyTurn && gameStatus === 'in_progress' && !hasPendingQuestion}
            activeTab={activeTab}
            onTabChange={(tab) => {
              setActiveTab(tab)
              if (tab !== 'guess') {
                setSelectedGuessCharacterId(null)
              }
            }}
            pendingQuestion={
              isDefenderWaiting
                ? {
                  key: pendingQuestion.key,
                  secondsLeft: pendingSeconds,
                }
                : null
            }
            selectedGuessCharacterName={selectedGuessCharacterName}
            onAskQuestion={sendAskQuestion}
            onAnswerQuestion={sendAnswerQuestion}
          />
          <GameLog entries={logEntries} />
        </aside>
      </div>
    </main>
  )
}
