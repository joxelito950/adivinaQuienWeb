/**
 * Combina clases CSS filtrando valores falsy.
 * Solución ligera para el proyecto (sin dependencias externas).
 */
export function cn(...classes: (string | undefined | null | false)[]): string {
  return classes.filter(Boolean).join(' ')
}
