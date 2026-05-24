# apps/server-java

Backend en Spring Boot con WebSocket puro para reglas de negocio del juego.

Responsabilidades:
- Matchmaking por dificultad.
- Gestion de partidas y turnos.
- Jugador dummy automatizado.
- Concurrencia con Virtual Threads.

## Estado actual (Mayo 2026)

- Endpoint WebSocket operativo en `/ws`.
- Matchmaking por dificultad con timeout a dummy.
- Flujo de eventos principal implementado (`queue_*`, `game_started`, `turn_changed`, `question_answered`, `guess_result`, `game_finished`).
- Reconexion basica de jugador por `reconnect_game`.

## Lo que falta para despliegue (backend)

### Requerido para release

- [ ] Restringir origenes WebSocket (reemplazar `setAllowedOrigins("*")` por dominios reales).
- [ ] Configurar entorno productivo (port, timeouts, logs, profile).
- [ ] Health endpoint y readiness/liveness para plataforma destino.
- [ ] Estrategia de escalado: sesiones WebSocket requieren sticky sessions o estado distribuido.
- [ ] Observabilidad minima: metricas de conexiones activas, partidas activas y errores por tipo de evento.

### Recomendado

- [ ] Versionar contrato de mensajes WebSocket.
- [ ] Endurecer validaciones de identidad (evitar confiar solo en `playerId` del cliente).
- [ ] Agregar pruebas de integracion de reconexion y abandono por timeout.

## Soporte de imagenes en contrato

Si se decide que backend provea las rutas de imagen, extender payload de `game_started`:

- `board.characters[]` actualmente envia:
	- `characterId`
	- `displayName`

Agregar campo opcional:

- `imageUrl` (ruta absoluta o relativa publica)

Consideraciones:

- Mantener campo opcional para compatibilidad con clientes existentes.
- Si se usa CDN, firmar/versionar URLs para cache busting.
- Definir politica de fallback si una imagen no existe.
