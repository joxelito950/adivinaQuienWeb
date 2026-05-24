'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { Difficulty } from '@/lib/protocol'
import { Button } from '@/components/ui/Button'
import { cn } from '@/lib/utils'

const DIFFICULTY_CONFIG: Record<
  Difficulty,
  { label: string; grid: string; chars: number; accent: string }
> = {
  [Difficulty.SMALL]:  { label: 'Pequeño',  grid: '3 × 4',  chars: 12, accent: 'text-green-400  border-green-700  bg-green-900/20'  },
  [Difficulty.MEDIUM]: { label: 'Mediano',  grid: '4 × 5',  chars: 20, accent: 'text-yellow-400 border-yellow-700 bg-yellow-900/20' },
  [Difficulty.LARGE]:  { label: 'Grande',   grid: '6 × 6',  chars: 36, accent: 'text-red-400    border-red-700    bg-red-900/20'    },
}

export default function HomePage() {
  const [selected, setSelected] = useState<Difficulty | null>(null)
  const router = useRouter()

  function handleJoin() {
    if (!selected) return
    router.push(`/queue/${selected.toLowerCase()}`)
  }

  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-10 py-12">
      {/* Title */}
      <div className="text-center">
        <h1 className="text-5xl font-extrabold tracking-tight text-brand-400 sm:text-6xl">
          Adivina Quién
        </h1>
        <p className="mt-3 text-slate-400 max-w-sm mx-auto text-sm sm:text-base">
          Elige la dificultad, únete a la cola y adivina el personaje secreto de tu oponente.
        </p>
      </div>

      {/* Difficulty cards */}
      <div className="grid w-full max-w-xl gap-4 sm:grid-cols-3">
        {(Object.values(Difficulty) as Difficulty[]).map((diff) => {
          const { label, grid, chars, accent } = DIFFICULTY_CONFIG[diff]
          const isSelected = selected === diff
          return (
            <button
              key={diff}
              onClick={() => setSelected(diff)}
              className={cn(
                'flex flex-col gap-1 rounded-2xl border-2 p-5 text-left transition-all active:scale-95',
                isSelected
                  ? `${accent} shadow-lg ring-1 ring-current`
                  : 'border-slate-700 bg-slate-800 hover:border-slate-500 hover:bg-slate-700/80',
              )}
              aria-pressed={isSelected}
            >
              <span
                className={cn(
                  'text-2xl font-bold transition-colors',
                  isSelected ? accent.split(' ')[0] : 'text-slate-100',
                )}
              >
                {label}
              </span>
              <span className="text-sm text-slate-400">{grid} tablero</span>
              <span className="text-xs text-slate-500">{chars} personajes</span>
            </button>
          )
        })}
      </div>

      {/* CTA */}
      <Button
        size="lg"
        disabled={!selected}
        onClick={handleJoin}
        className="w-full max-w-xl"
      >
        Unirse a la cola
      </Button>

      <p className="text-xs text-slate-600">
        Si no hay oponente disponible en 60&nbsp;s, se empareja con la IA.
      </p>
    </main>
  )
}
