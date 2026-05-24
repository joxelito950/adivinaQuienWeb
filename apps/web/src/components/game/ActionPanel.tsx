'use client'

import { useState } from 'react'
import { QuestionKey } from '@/lib/protocol'
import { AttributeBadge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { cn } from '@/lib/utils'

type Tab = 'question' | 'guess'

const QUESTION_LABELS: Record<QuestionKey, string> = {
  [QuestionKey.USES_GLASSES]:    '¿Usa lentes?',
  [QuestionKey.HAS_BEARD]:       '¿Tiene barba?',
  [QuestionKey.HAS_HAT]:         '¿Usa sombrero?',
  [QuestionKey.HAS_BLONDE_HAIR]: '¿Tiene pelo rubio?',
  [QuestionKey.HAS_BLUE_EYES]:   '¿Tiene ojos azules?',
  [QuestionKey.HAS_EARRINGS]:    '¿Tiene aretes?',
}

interface CharacterOption {
  characterId:  string
  displayName:  string
}

interface ActionPanelProps {
  isMyTurn:                boolean
  opponentCharacterOptions?: CharacterOption[]
  onAskQuestion?:          (key: QuestionKey) => void
  onGuessCharacter?:       (characterId: string) => void
  className?:              string
}

export function ActionPanel({
  isMyTurn,
  opponentCharacterOptions = [],
  onAskQuestion,
  onGuessCharacter,
  className,
}: ActionPanelProps) {
  const [tab, setTab]                     = useState<Tab>('question')
  const [selectedQuestion, setQuestion]   = useState<QuestionKey | null>(null)
  const [selectedCharacter, setCharacter] = useState<string | null>(null)

  function handleConfirm() {
    if (tab === 'question' && selectedQuestion) {
      onAskQuestion?.(selectedQuestion)
      setQuestion(null)
    } else if (tab === 'guess' && selectedCharacter) {
      onGuessCharacter?.(selectedCharacter)
      setCharacter(null)
    }
  }

  const canConfirm = isMyTurn && (
    (tab === 'question' && !!selectedQuestion) ||
    (tab === 'guess'    && !!selectedCharacter)
  )

  return (
    <div className={cn('rounded-2xl border border-slate-700 bg-slate-800/60 p-4 shadow-lg', className)}>
      <h2 className="mb-3 text-xs font-semibold uppercase tracking-wider text-slate-400">
        Tu acción
      </h2>

      {/* Tabs */}
      <div className="mb-4 flex rounded-xl bg-slate-900 p-1 gap-1">
        {(['question', 'guess'] as Tab[]).map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            disabled={!isMyTurn}
            className={cn(
              'flex-1 rounded-lg py-2 text-sm font-medium transition-all',
              'disabled:cursor-not-allowed disabled:opacity-50',
              tab === t
                ? 'bg-brand-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-slate-200',
            )}
          >
            {t === 'question' ? '❓ Preguntar' : '🎯 Adivinar'}
          </button>
        ))}
      </div>

      {/* Question options */}
      {tab === 'question' && (
        <div className="grid grid-cols-1 gap-2 xs:grid-cols-2">
          {(Object.values(QuestionKey) as QuestionKey[]).map((key) => (
            <button
              key={key}
              disabled={!isMyTurn}
              onClick={() => setQuestion(key)}
              className={cn(
                'flex items-center gap-2 rounded-xl border px-3 py-2 text-left text-sm transition-all',
                'disabled:cursor-not-allowed disabled:opacity-50',
                selectedQuestion === key
                  ? 'border-brand-400 bg-brand-900/30 text-brand-200'
                  : 'border-slate-700 bg-slate-700/40 text-slate-300 hover:border-slate-500 hover:bg-slate-700',
              )}
            >
              <AttributeBadge attribute={key} showLabel={false} className="shrink-0" />
              <span>{QUESTION_LABELS[key]}</span>
            </button>
          ))}
        </div>
      )}

      {/* Guess options */}
      {tab === 'guess' && (
        <div className="max-h-48 overflow-y-auto scrollbar-thin">
          {opponentCharacterOptions.length === 0 ? (
            <p className="py-4 text-center text-sm text-slate-500">
              No hay personajes disponibles.
            </p>
          ) : (
            <div className="grid grid-cols-1 gap-1.5 xs:grid-cols-2">
              {opponentCharacterOptions.map(({ characterId, displayName }) => (
                <button
                  key={characterId}
                  disabled={!isMyTurn}
                  onClick={() => setCharacter(characterId)}
                  className={cn(
                    'rounded-xl border px-3 py-2 text-left text-sm transition-all',
                    'disabled:cursor-not-allowed disabled:opacity-50',
                    selectedCharacter === characterId
                      ? 'border-brand-400 bg-brand-900/30 text-brand-200'
                      : 'border-slate-700 bg-slate-700/40 text-slate-300 hover:border-slate-500 hover:bg-slate-700',
                  )}
                >
                  {displayName}
                </button>
              ))}
            </div>
          )}
        </div>
      )}

      <Button
        className="mt-4 w-full"
        disabled={!canConfirm}
        onClick={handleConfirm}
      >
        Confirmar acción
      </Button>

      {!isMyTurn && (
        <p className="mt-2 text-center text-xs text-slate-500">
          Esperando el turno del oponente…
        </p>
      )}
    </div>
  )
}
