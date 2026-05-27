'use client'

import { use, useEffect, useRef, useState } from 'react'
import { useRouter } from 'next/navigation'
import { Spinner } from '@/components/ui/Spinner'
import { Button } from '@/components/ui/Button'
import { getPlayerId } from '@/lib/player'
import { createWsClient, WsClient } from '@/lib/ws-client'
import {
  DifficultyWire,
  GameStartedEvent,
  parseServerMessage,
  QueueWaitingEvent,
  ServerMessage,
} from '@/lib/protocol'

const MATCH_TIMEOUT = 60
const AI_BUTTON_ENABLE_AFTER_SECONDS = 3

const DIFFICULTY_META: Record<string, { label: string; grid: string }> = {
  small:  { label: 'Pequeño',  grid: '3 × 4'  },
  medium: { label: 'Mediano',  grid: '4 × 5'  },
  large:  { label: 'Grande',   grid: '6 × 6'  },
}

interface PageProps {
  params: Promise<{ difficulty: string }>
}

export default function QueuePage({ params }: PageProps) {
  const { difficulty } = use(params)
  const router         = useRouter()
  const meta           = DIFFICULTY_META[difficulty] ?? { label: difficulty, grid: '?' }
  const wireDifficulty = (difficulty.toLowerCase() as DifficultyWire)

  const [secondsLeft, setSecondsLeft] = useState(MATCH_TIMEOUT)
  const [phase, setPhase]             = useState<'searching' | 'dummy'>('searching')
  const [status, setStatus]           = useState<'connecting' | 'connected' | 'error'>('connecting')
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [requestingAi, setRequestingAi] = useState(false)
  const wsClientRef = useRef<WsClient | null>(null)
  const playerIdRef = useRef<string>('')

  useEffect(() => {
    if (secondsLeft <= 0) {
      setPhase('dummy')
      return
    }
    const t = setTimeout(() => setSecondsLeft((s) => s - 1), 1000)
    return () => clearTimeout(t)
  }, [secondsLeft])

  useEffect(() => {
    const playerId = getPlayerId()
    playerIdRef.current = playerId
    let client: WsClient | null = null

    function handleEvent(message: ServerMessage) {
      if (message.type === 'queue_waiting') {
        const event = message as QueueWaitingEvent
        setSecondsLeft(event.payload.timeoutSeconds ?? MATCH_TIMEOUT)
        setPhase('searching')
        setRequestingAi(false)
        return
      }

      if (message.type === 'game_started') {
        const event = message as GameStartedEvent
        setRequestingAi(false)
        sessionStorage.setItem(
          `adivinaquien.game.${event.payload.gameId}`,
          JSON.stringify(event.payload),
        )
        router.push(`/game/${event.payload.gameId}`)
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

    client = createWsClient({
      onOpen: () => {
        setStatus('connected')
        setErrorMessage(null)
        wsClientRef.current = client
        client?.send({
          type: 'join_queue',
          payload: {
            playerId,
            difficulty: wireDifficulty,
          },
        })
      },
      onMessage: (raw) => {
        const parsed = parseServerMessage(raw)
        if (!parsed) return
        handleEvent(parsed)
      },
      onError: () => {
        setStatus('error')
        setErrorMessage('No se pudo establecer la conexión WebSocket.')
      },
      onClose: () => {
        setStatus((prev) => (prev === 'error' ? prev : 'connecting'))
      },
    })

    return () => {
      client?.send({ type: 'leave_queue', payload: { playerId } })
      client?.close()
      wsClientRef.current = null
    }
  }, [router, wireDifficulty])

  function handleCancel() {
    router.push('/')
  }

  function handleStartAiMatch() {
    if (phase !== 'searching' || status !== 'connected' || requestingAi) {
      return
    }
    if (MATCH_TIMEOUT - secondsLeft < AI_BUTTON_ENABLE_AFTER_SECONDS) {
      return
    }

    const playerId = playerIdRef.current
    if (!playerId) {
      return
    }

    setRequestingAi(true)
    setErrorMessage(null)
    wsClientRef.current?.send({
      type: 'start_dummy_match',
      payload: { playerId },
    })
  }

  const progress   = secondsLeft / MATCH_TIMEOUT
  const circumference = 2 * Math.PI * 42
  const elapsedSeconds = MATCH_TIMEOUT - secondsLeft
  const aiReady = phase === 'searching' && elapsedSeconds >= AI_BUTTON_ENABLE_AFTER_SECONDS && status === 'connected'

  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-8 py-12 text-center">
      {/* Progress ring */}
      <div className="relative">
        <svg
          className="-rotate-90 h-32 w-32 sm:h-36 sm:w-36"
          viewBox="0 0 100 100"
          aria-hidden="true"
        >
          {/* Track */}
          <circle cx="50" cy="50" r="42" fill="none" stroke="#1e293b" strokeWidth="8" />
          {/* Progress */}
          <circle
            cx="50"
            cy="50"
            r="42"
            fill="none"
            stroke={phase === 'dummy' ? '#f59e0b' : '#0ea5e9'}
            strokeWidth="8"
            strokeLinecap="round"
            strokeDasharray={circumference}
            strokeDashoffset={circumference * (1 - progress)}
            className="transition-all duration-1000 ease-linear"
          />
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span className="text-2xl font-bold tabular-nums text-slate-100 sm:text-3xl">
            {secondsLeft}s
          </span>
        </div>
      </div>

      {/* Status text */}
      <div className="flex flex-col items-center gap-2">
        <h1 className="text-2xl font-bold text-slate-100 sm:text-3xl">
          {phase === 'searching' ? 'Buscando oponente…' : 'Sin oponente encontrado'}
        </h1>
        <p className="text-xs text-slate-500">
          Estado de conexión: {status === 'connected' ? 'Conectado' : status === 'error' ? 'Error' : 'Conectando'}
        </p>
        <p className="text-slate-400">
          Dificultad:{' '}
          <span className="font-semibold text-slate-200">{meta.label}</span>
          {' · '}
          <span className="text-slate-400">{meta.grid} tablero</span>
        </p>
        {errorMessage && (
          <p className="mt-1 rounded-xl border border-red-700 bg-red-900/20 px-4 py-2 text-sm text-red-300">
            {errorMessage}
          </p>
        )}
        {(phase === 'dummy' || requestingAi) && (
          <p className="mt-1 rounded-xl border border-amber-700 bg-amber-900/20 px-4 py-2 text-sm text-amber-300">
            Iniciando partida contra la IA…
          </p>
        )}
      </div>

      {phase === 'searching' && <Spinner size="lg" />}

      {phase === 'searching' && (
        <Button
          onClick={handleStartAiMatch}
          disabled={!aiReady || requestingAi}
          variant={aiReady ? 'primary' : 'secondary'}
          className={aiReady ? 'animate-pulse' : ''}
        >
          {requestingAi ? 'Solicitando partida con IA…' : 'Jugar contra IA'}
        </Button>
      )}

      <Button variant="ghost" onClick={handleCancel}>
        Cancelar y volver al menú
      </Button>
    </main>
  )
}
