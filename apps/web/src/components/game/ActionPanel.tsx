'use client'

import { useState } from 'react'
import { QuestionKey } from '@/lib/protocol'
import { ACTIVE_QUESTION_KEYS, QUESTION_LABELS } from '@/lib/questions'
import { AttributeBadge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { cn } from '@/lib/utils'

export type ActionTab = 'question' | 'guess'

interface PendingQuestion {
  key: QuestionKey
  secondsLeft: number
}

interface ActionPanelProps {
  isMyTurn:                boolean
  activeTab:               ActionTab
  onTabChange:             (tab: ActionTab) => void
  pendingQuestion?:        PendingQuestion | null
  selectedGuessCharacterName?: string | null
  resolvedQuestionAnswers?: Partial<Record<QuestionKey, boolean>>
  onAskQuestion?:          (key: QuestionKey) => void
  onAnswerQuestion?:       (answer: boolean) => void
  className?:              string
}

export function ActionPanel({
  isMyTurn,
  activeTab,
  onTabChange,
  pendingQuestion = null,
  selectedGuessCharacterName = null,
  resolvedQuestionAnswers = {},
  onAskQuestion,
  onAnswerQuestion,
  className,
}: ActionPanelProps) {
  const [selectedQuestion, setQuestion]   = useState<QuestionKey | null>(null)
  const [selectedAnswer, setSelectedAnswer] = useState<boolean | null>(null)

  function handleConfirm() {
    if (pendingQuestion && selectedAnswer !== null) {
      onAnswerQuestion?.(selectedAnswer)
      setSelectedAnswer(null)
      return
    }

    if (activeTab === 'question' && selectedQuestion) {
      if (Object.prototype.hasOwnProperty.call(resolvedQuestionAnswers, selectedQuestion)) {
        setQuestion(null)
        return
      }
      onAskQuestion?.(selectedQuestion)
      setQuestion(null)
    }
  }

  const canConfirm = pendingQuestion
    ? selectedAnswer !== null
    : isMyTurn && (
      (activeTab === 'question' && !!selectedQuestion)
    )

  const waitingForOpponentAnswer = !pendingQuestion && !isMyTurn

  return (
    <div className={cn('rounded-2xl border border-slate-700 bg-slate-800/60 p-4 shadow-lg', className)}>
      <h2 className="mb-3 text-xs font-semibold uppercase tracking-wider text-slate-400">
        Tu acción
      </h2>

      {pendingQuestion ? (
        <div className="space-y-3">
          <div className="rounded-xl border border-brand-500/60 bg-brand-900/20 px-3 py-2 text-sm text-brand-200">
            <p className="font-semibold">Responde la pregunta del oponente</p>
            <p>{QUESTION_LABELS[pendingQuestion.key]}</p>
            <p className="mt-1 text-xs text-brand-300">Tiempo restante: {pendingQuestion.secondsLeft}s</p>
          </div>

          <div className="grid grid-cols-2 gap-2">
            <button
              onClick={() => setSelectedAnswer(true)}
              className={cn(
                'rounded-xl border px-3 py-2 text-sm font-medium transition-all',
                selectedAnswer === true
                  ? 'border-emerald-400 bg-emerald-900/30 text-emerald-200'
                  : 'border-slate-700 bg-slate-700/40 text-slate-300 hover:border-slate-500 hover:bg-slate-700',
              )}
            >
              Sí
            </button>
            <button
              onClick={() => setSelectedAnswer(false)}
              className={cn(
                'rounded-xl border px-3 py-2 text-sm font-medium transition-all',
                selectedAnswer === false
                  ? 'border-red-400 bg-red-900/30 text-red-200'
                  : 'border-slate-700 bg-slate-700/40 text-slate-300 hover:border-slate-500 hover:bg-slate-700',
              )}
            >
              No
            </button>
          </div>
        </div>
      ) : (
        <>
      {/* Tabs */}
      <div className="mb-4 flex rounded-xl bg-slate-900 p-1 gap-1">
        {(['question', 'guess'] as ActionTab[]).map((t) => (
          <button
            key={t}
            onClick={() => onTabChange(t)}
            disabled={!isMyTurn}
            className={cn(
              'flex-1 rounded-lg py-2 text-sm font-medium transition-all',
              'disabled:cursor-not-allowed disabled:opacity-50',
              activeTab === t
                ? 'bg-brand-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-slate-200',
            )}
          >
            {t === 'question' ? '❓ Preguntar' : '🎯 Adivinar'}
          </button>
        ))}
      </div>

      {/* Question options */}
      {activeTab === 'question' && (
        <div className="grid grid-cols-1 gap-2 xs:grid-cols-2">
          {ACTIVE_QUESTION_KEYS.map((key) => (
            (() => {
              const isResolved = Object.prototype.hasOwnProperty.call(resolvedQuestionAnswers, key)
              const resolvedAnswer = resolvedQuestionAnswers[key]
              const isDisabled = !isMyTurn || isResolved

              return (
            <button
              key={key}
              disabled={isDisabled}
              onClick={() => {
                if (isResolved) return
                setQuestion(key)
              }}
              className={cn(
                'flex items-center gap-2 rounded-xl border px-3 py-2 text-left text-sm transition-all',
                'disabled:cursor-not-allowed disabled:opacity-50',
                isResolved
                  ? 'border-emerald-700/70 bg-emerald-900/20 text-emerald-200'
                  : selectedQuestion === key
                  ? 'border-brand-400 bg-brand-900/30 text-brand-200'
                  : 'border-slate-700 bg-slate-700/40 text-slate-300 hover:border-slate-500 hover:bg-slate-700',
              )}
            >
              <AttributeBadge attribute={key} showLabel={false} className="shrink-0" />
              <span className="flex-1">{QUESTION_LABELS[key]}</span>
              {isResolved && (
                <span className={cn('rounded-md px-1.5 py-0.5 text-xs font-semibold', resolvedAnswer ? 'bg-emerald-800/70 text-emerald-100' : 'bg-rose-800/70 text-rose-100')}>
                  {resolvedAnswer ? 'Sí' : 'No'}
                </span>
              )}
            </button>
              )
            })()
          ))}
        </div>
      )}

      {/* Guess mode guidance */}
      {activeTab === 'guess' && (
        <div className="space-y-2 rounded-xl border border-slate-700 bg-slate-900/60 px-3 py-3 text-sm text-slate-300">
          <p className="font-medium text-slate-200">Selecciona una carta del tablero para adivinar.</p>
          <p>La confirmación aparece en la misma carta seleccionada.</p>
          <p className="text-xs text-slate-400">
            Candidato actual: {selectedGuessCharacterName ?? 'ninguno'}
          </p>
        </div>
      )}
        </>
      )}

      <Button
        className="mt-4 w-full"
        disabled={!canConfirm || (!pendingQuestion && activeTab === 'guess')}
        onClick={handleConfirm}
      >
        {pendingQuestion ? 'Confirmar respuesta' : 'Confirmar pregunta'}
      </Button>

      {waitingForOpponentAnswer && (
        <div className="mt-2 space-y-1 text-center text-xs text-slate-500">
          <p>Esperando el turno del oponente…</p>
          <p>Si recibes una pregunta, deberás responderla antes de 15 segundos.</p>
        </div>
      )}
    </div>
  )
}
