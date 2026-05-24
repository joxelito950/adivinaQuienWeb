import { cn } from '@/lib/utils'

interface TurnIndicatorProps {
  isMyTurn:      boolean
  opponentName?: string
  className?:    string
}

export function TurnIndicator({
  isMyTurn,
  opponentName = 'Oponente',
  className,
}: TurnIndicatorProps) {
  return (
    <div
      className={cn(
        'flex items-center gap-3 rounded-xl border px-4 py-3 transition-all',
        isMyTurn
          ? 'border-brand-500 bg-brand-900/30 shadow-lg shadow-brand-500/10'
          : 'border-slate-600 bg-slate-800/60',
        className,
      )}
    >
      {/* Indicador de pulso */}
      <span className="relative flex h-3 w-3 shrink-0">
        {isMyTurn && (
          <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-brand-400 opacity-75" />
        )}
        <span
          className={cn(
            'relative inline-flex h-3 w-3 rounded-full',
            isMyTurn ? 'bg-brand-400' : 'bg-slate-500',
          )}
        />
      </span>

      <div className="min-w-0">
        <p
          className={cn(
            'text-sm font-semibold sm:text-base',
            isMyTurn ? 'text-brand-300' : 'text-slate-300',
          )}
        >
          {isMyTurn ? '¡Es tu turno!' : `Turno de ${opponentName}`}
        </p>
        <p className="text-xs text-slate-500">
          {isMyTurn
            ? 'Haz una pregunta o adivina el personaje secreto.'
            : 'Esperando la acción del oponente…'}
        </p>
      </div>
    </div>
  )
}
