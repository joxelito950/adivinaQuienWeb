'use client'

import { useState } from 'react'
import { QuestionKey } from '@/lib/protocol'
import { cn } from '@/lib/utils'

const QUESTION_LABELS: Record<QuestionKey, string> = {
  [QuestionKey.USES_GLASSES]:    '¿Usa lentes?',
  [QuestionKey.HAS_BEARD]:       '¿Tiene barba?',
  [QuestionKey.HAS_HAT]:         '¿Usa sombrero?',
  [QuestionKey.HAS_BLONDE_HAIR]: '¿Tiene pelo rubio?',
  [QuestionKey.HAS_BLUE_EYES]:   '¿Tiene ojos azules?',
  [QuestionKey.HAS_EARRINGS]:    '¿Tiene aretes?',
  [QuestionKey.IS_MALE]:         '¿Es hombre?',
  [QuestionKey.IS_FEMALE]:       '¿Es mujer?',
  [QuestionKey.IS_BALD]:         '¿Es calvo?',
  [QuestionKey.HAS_FAIR_SKIN]:   '¿Es de tez clara?',
  [QuestionKey.HAS_DARK_SKIN]:   '¿Es de tez oscura?',
}

export type LogEntry =
  | { type: 'question'; by: string; key: QuestionKey; answer: boolean }
  | { type: 'guess';    by: string; characterName: string; correct: boolean }
  | { type: 'event';    message: string }

interface GameLogProps {
  entries:    LogEntry[]
  className?: string
}

export function GameLog({ entries, className }: GameLogProps) {
  const [collapsed, setCollapsed] = useState(false)

  return (
    <div className={cn('rounded-2xl border border-slate-700 bg-slate-800/60 shadow-lg', className)}>
      {/* Header — permite colapsar en mobile */}
      <button
        onClick={() => setCollapsed((c) => !c)}
        className="flex w-full items-center justify-between rounded-t-2xl px-4 py-3 text-left transition-colors hover:bg-slate-700/30"
        aria-expanded={!collapsed}
      >
        <h2 className="text-xs font-semibold uppercase tracking-wider text-slate-400">
          Historial ({entries.length})
        </h2>
        <span className="text-slate-500 text-sm lg:hidden" aria-hidden="true">
          {collapsed ? '▾' : '▴'}
        </span>
      </button>

      {!collapsed && (
        <div className="max-h-52 overflow-y-auto scrollbar-thin px-4 pb-4 lg:max-h-80">
          {entries.length === 0 ? (
            <p className="py-4 text-center text-sm text-slate-500">
              La partida acaba de comenzar.
            </p>
          ) : (
            <ol className="flex flex-col gap-2">
              {entries.map((entry, i) => (
                <li
                  key={i}
                  className="rounded-lg border border-slate-700/60 bg-slate-900/60 px-3 py-2 text-sm"
                >
                  {entry.type === 'question' && (
                    <p>
                      <span className="font-semibold text-slate-300">{entry.by}</span>
                      {' preguntó '}
                      <span className="italic text-slate-400">{QUESTION_LABELS[entry.key]}</span>
                      {' → '}
                      <span className={cn('font-bold', entry.answer ? 'text-green-400' : 'text-red-400')}>
                        {entry.answer ? 'Sí ✓' : 'No ✗'}
                      </span>
                    </p>
                  )}
                  {entry.type === 'guess' && (
                    <p>
                      <span className="font-semibold text-slate-300">{entry.by}</span>
                      {' adivinó '}
                      <span className="italic text-slate-400">{entry.characterName}</span>
                      {' → '}
                      <span className={cn('font-bold', entry.correct ? 'text-green-400' : 'text-red-400')}>
                        {entry.correct ? '¡Correcto! 🎉' : 'Incorrecto ✗'}
                      </span>
                    </p>
                  )}
                  {entry.type === 'event' && (
                    <p className="italic text-slate-400">{entry.message}</p>
                  )}
                </li>
              ))}
            </ol>
          )}
        </div>
      )}
    </div>
  )
}
