# Adivina Quien Web

Aplicacion web multijugador del juego "Adivina Quien" con:

- Frontend en Next.js (UI y experiencia de usuario).
- Backend en Java (Spring Boot + WebSocket puro) para matchmaking, partidas, turnos y reglas de negocio.
- Concurrencia con hilos (Virtual Threads en Java 21).

Este README documenta la arquitectura y las reglas completas del sistema para guiar la implementacion.

## 1. Objetivo del sistema

Construir un juego en tiempo real donde dos jugadores se emparejan, reciben el mismo tablero, juegan por turnos y el servidor define toda la verdad del juego.

Si no aparece un segundo jugador en la cola en 60 segundos, el sistema inicia una partida contra un jugador dummy automatizado en backend.

## 2. Requisitos funcionales cerrados

### 2.1 Emparejamiento

- El jugador selecciona nivel de dificultad antes de entrar a cola.
- Existen colas separadas por dificultad:
	- small
	- medium
	- large
- Solo se emparejan jugadores del mismo nivel.
- Si un jugador espera 60 segundos sin rival humano, se crea partida contra dummy.
- Una partida iniciada contra dummy no se reemplaza luego por un humano.

### 2.2 Tablero y dificultad

Niveles de dificultad por tamano de tablero:

- small: 3x4 (12 personajes)
- medium: 4x5 (20 personajes)
- large: 6x6 (36 personajes)

Reglas de tablero:

- Ambos jugadores reciben el mismo tablero con personajes en las mismas posiciones.
- El tablero se genera aleatoriamente por partida.
- El personaje secreto de cada jugador debe ser distinto.

### 2.3 Mecanica de juego

- Turnos alternados estrictos.
- En su turno, un jugador puede:
	- Hacer una pregunta de caracteristicas (respuesta SI o NO).
	- Intentar adivinar el personaje rival.
- Si adivina correctamente: gana la partida.
- Si adivina incorrectamente: pierde turno (la partida continua).

### 2.4 Estado y consistencia

- Todo estado de la partida vive en el servidor.
- El cliente no decide reglas, solo representa estado.
- El servidor valida todas las acciones recibidas.

### 2.5 Desconexion

- Si un jugador se desconecta, tiene ventana de reconexion de 45 segundos.
- Si no reconecta dentro de ese tiempo, pierde por abandono.

## 3. Arquitectura

### 3.1 Componentes

- Frontend (Next.js):
	- Pantalla de seleccion de dificultad.
	- Pantalla de espera en cola.
	- Vista del tablero y panel de turnos.
	- Formulario de pregunta / intento.
- Backend (Spring Boot):
	- Endpoint WebSocket.
	- Matchmaking por dificultad.
	- Motor de reglas de juego.
	- Gestion de sesiones de partida.
	- Control de concurrencia por gameId.
	- Jugador dummy automatizado.

### 3.2 Despliegue

- Frontend: Vercel.
- Backend realtime: servicio separado con proceso persistente (Render, Railway, Fly u otro equivalente).

Motivo: el backend con WebSocket persistente debe ejecutarse en un servidor dedicado para sesiones en tiempo real.

### 3.3 Estado de implementacion (Mayo 2026)

- Frontend web conectado por WebSocket al backend para cola y partida.
- UI responsive habilitada en mobile/tablet/desktop.
- Tests unitarios base en frontend (protocolo, player id, ws client).

Pendientes de cierre para produccion:

- Seguridad de WebSocket (origenes y autenticacion de jugador).
- Pipeline CI/CD para build + test + deploy.
- Estrategia de observabilidad y alertas.

## 3.4 Checklist de despliegue

### Frontend (Next.js)

- [ ] Configurar `NEXT_PUBLIC_WS_URL` con `wss://` al backend productivo.
- [ ] Build de produccion sin warnings criticos.
- [ ] Test unitarios y smoke test de flujo principal en CI.
- [ ] Configurar dominio, HTTPS y politicas de cache para assets.

### Backend (Spring Boot + WebSocket)

- [ ] Restringir `allowedOrigins` a dominios oficiales.
- [ ] Parametrizar y validar `SERVER_PORT`, `MATCH_TIMEOUT_SECONDS`, `RECONNECT_TIMEOUT_SECONDS`.
- [ ] Exponer health checks y logs estructurados.
- [ ] Definir politica de escalado para sesiones persistentes.

### Operacion

- [ ] Configurar secretos y variables por ambiente (dev/staging/prod).
- [ ] Definir rollback rapido de frontend y backend.
- [ ] Definir tablero de monitoreo (latencia WS, errores, partidas activas).

## 3.5 Plan para integrar imagenes de personajes

### Objetivo

Mostrar imagen real por personaje en el tablero manteniendo compatibilidad con clientes actuales.

### Estrategia recomendada por fases

1. Fase 1 (rapida): catalogo local en frontend por `characterId`.
2. Fase 2 (escalable): backend agrega `imageUrl` opcional en `game_started.board.characters[]`.

### Requisitos tecnicos

- Definir identificador estable (`characterId`) y naming de assets.
- Formato recomendado: `webp`; fallback `png`.
- Peso objetivo por imagen: < 100KB.
- Fallback visual (avatar inicial) cuando imagen no disponible.
- Texto alternativo accesible por personaje.

### Tareas pendientes para cerrar imagenes

- [ ] Recopilar o disenar set final de personajes.
- [ ] Normalizar tamanos y recortes de imagen.
- [ ] Publicar assets (frontend public o CDN).
- [ ] Actualizar UI (`CharacterCard`) para priorizar imagen y fallback.
- [ ] Validar rendimiento en mobile (LCP/CLS) tras incorporar imagenes.

## 4. Modelo de dominio

Entidades sugeridas:

- Player:
	- playerId
	- socketSessionId
	- type (human | dummy)
- GameSession:
	- gameId
	- difficulty (small | medium | large)
	- status (waiting | in_progress | finished | abandoned)
	- players[2]
	- board
	- secretByPlayer
	- currentTurnPlayerId
	- timestamps (createdAt, startedAt, updatedAt)
- MatchQueue:
	- difficulty
	- waitingPlayers FIFO

## 5. Flujo de vida de una partida

1. El cliente se conecta por WebSocket.
2. El cliente envia join_queue con dificultad.
3. Backend encola al jugador en la cola de esa dificultad.
4. Si aparece segundo humano antes de 60s:
	 - Se crea partida humano vs humano.
5. Si no aparece segundo humano en 60s:
	 - Se crea partida humano vs dummy.
6. Backend envia game_started con tablero y estado inicial.
7. Se procesan turnos alternados hasta victoria o abandono.
8. Backend envia game_finished y cierra sesion de partida.

## 6. Protocolo WebSocket (propuesto)

Formato general de mensaje:

```json
{
	"type": "event_or_command",
	"correlationId": "uuid-opcional",
	"payload": {}
}
```

### 6.1 Comandos cliente -> servidor

- join_queue
- leave_queue
- ask_question
- guess_character
- reconnect_game
- ping

Ejemplo join_queue:

```json
{
	"type": "join_queue",
	"payload": {
		"playerId": "p1",
		"difficulty": "medium"
	}
}
```

Ejemplo ask_question:

```json
{
	"type": "ask_question",
	"payload": {
		"gameId": "g-123",
		"questionKey": "uses_glasses"
	}
}
```

Ejemplo guess_character:

```json
{
	"type": "guess_character",
	"payload": {
		"gameId": "g-123",
		"characterId": "char-7"
	}
}
```

### 6.2 Eventos servidor -> cliente

- queue_joined
- queue_waiting
- game_started
- turn_changed
- question_answered
- guess_result
- invalid_action
- player_disconnected
- game_finished
- error

Ejemplo game_started:

```json
{
	"type": "game_started",
	"payload": {
		"gameId": "g-123",
		"difficulty": "medium",
		"opponentType": "dummy",
		"board": {
			"rows": 4,
			"cols": 5,
			"characters": []
		},
		"yourSecretCharacterId": "char-2",
		"firstTurnPlayerId": "p1"
	}
}
```

## 7. Reglas del motor de juego

### 7.1 Validaciones por accion

Para cada comando, el backend valida:

- La partida existe y esta activa.
- El jugador pertenece a esa partida.
- Es su turno para actuar.
- El comando tiene payload valido.

Si falla alguna validacion, responder con invalid_action sin mutar estado.

### 7.2 Preguntas SI/NO

- Las preguntas son sobre atributos definidos en el catalogo de personajes.
- El backend evalua la pregunta contra el personaje secreto del oponente.
- Respuesta posible: SI o NO.

### 7.3 Intento de adivinanza

- Si acierta: fin de partida y victoria.
- Si falla: cambio de turno, partida sigue.

## 8. Concurrencia y hilos

### 8.1 Estrategia

- Uso de Virtual Threads (Java 21) para procesamiento concurrente de acciones.
- Serializacion por partida:
	- lock por gameId
	- cola FIFO de acciones

Objetivo: evitar race conditions (por ejemplo, dobles jugadas en el mismo turno).

### 8.2 Aislamiento entre partidas

- Cada gameId se procesa de manera independiente.
- Acciones de una partida no deben bloquear ni contaminar otra.

## 9. Jugador dummy (backend)

### 9.1 Activacion

- Se activa exactamente a los 60 segundos de espera sin rival humano.
- Disponible en small, medium y large.

### 9.2 Comportamiento

- Toma acciones en su turno desde backend.
- Estrategia parametrizable por dificultad.
- Debe respetar las mismas reglas que un jugador humano.

## 10. Estructura propuesta del repositorio

```text
adivinaQuienWeb/
	README.md
	apps/
		web/                    # Next.js
		server-java/            # Spring Boot + WebSocket
			pom.xml
			src/main/java/...
			src/test/java/...
```

## 11. Variables de entorno (sugeridas)

Frontend:

- NEXT_PUBLIC_WS_URL=ws://localhost:8080/ws

Backend:

- SERVER_PORT=8080
- MATCH_TIMEOUT_SECONDS=60
- RECONNECT_TIMEOUT_SECONDS=45

## 12. Pruebas y criterios de aceptacion

Pruebas minimas:

1. Matchmaking por dificultad con colas separadas.
2. Activacion del dummy a los 60 segundos.
3. Tablero compartido correcto en small/medium/large.
4. Secretos distintos por jugador.
5. Acciones fuera de turno rechazadas.
6. Preguntas respondidas SI/NO correctamente.
7. Adivinar mal solo cambia turno.
8. Reconexion valida antes de 45s.
9. Derrota por abandono al superar 45s.
10. Multiples partidas simultaneas sin interferencia.

## 13. Estado actual del repositorio

Actualmente este repositorio contiene documentacion y definicion de arquitectura.

La implementacion de:

- Frontend Next.js
- Backend Java Spring Boot
- Protocolo WebSocket
- Motor de juego

se construira siguiendo este documento como contrato funcional.

## 14. Roadmap de implementacion

1. Inicializar apps/web y apps/server-java.
2. Implementar WebSocket backend y contrato base de mensajes.
3. Implementar matchmaking por nivel + timeout dummy.
4. Implementar motor de reglas, turnos y cierre de partida.
5. Integrar UI Next.js en tiempo real.
6. Agregar pruebas unitarias e integracion.
7. Preparar despliegue separado (Vercel + backend realtime).
