'use client'

import { use, useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { Spinner } from '@/components/ui/Spinner'
import { Button } from '@/components/ui/Button'

const MATCH_TIMEOUT = 60

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

  const [secondsLeft, setSecondsLeft] = useState(MATCH_TIMEOUT)
  const [phase, setPhase]             = useState<'searching' | 'dummy'>('searching')

  useEffect(() => {
    if (secondsLeft <= 0) {
      setPhase('dummy')
      return
    }
    const t = setTimeout(() => setSecondsLeft((s) => s - 1), 1000)
    return () => clearTimeout(t)
  }, [secondsLeft])

  const progress   = secondsLeft / MATCH_TIMEOUT
  const circumference = 2 * Math.PI * 42

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
        <p className="text-slate-400">
          Dificultad:{' '}
          <span className="font-semibold text-slate-200">{meta.label}</span>
          {' · '}
          <span className="text-slate-400">{meta.grid} tablero</span>
        </p>
        {phase === 'dummy' && (
          <p className="mt-1 rounded-xl border border-amber-700 bg-amber-900/20 px-4 py-2 text-sm text-amber-300">
            Iniciando partida contra la IA…
          </p>
        )}
      </div>

      {phase === 'searching' && <Spinner size="lg" />}

      <Button variant="ghost" onClick={() => router.push('/')}>
        Cancelar y volver al menú
      </Button>
    </main>
  )
}
