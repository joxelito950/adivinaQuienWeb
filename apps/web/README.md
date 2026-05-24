# apps/web

Frontend en Next.js para la interfaz del juego Adivina Quien.

Responsabilidades:
- Seleccion de dificultad y cola.
- Render del tablero y estado de turno.
- Envio y recepcion de mensajes WebSocket.

## Estado actual (Mayo 2026)

- UI responsive implementada para mobile, tablet y desktop.
- Flujo de cola conectado por WebSocket al backend.
- Navegacion automatica a partida al recibir `game_started`.
- Pantalla de juego conectada para recibir eventos y enviar acciones (`ask_question`, `guess_character`).
- Tests unitarios base en librerias de protocolo, identidad de jugador y cliente WebSocket.

## Configuracion local

1. Instalar dependencias:

```bash
npm install
```

2. Configurar URL del backend WebSocket en `.env.local` (tomar como base `.env.local.example`):

```env
NEXT_PUBLIC_WS_URL=ws://localhost:8080/ws
```

3. Ejecutar en desarrollo:

```bash
npm run dev
```

4. Validar calidad minima:

```bash
npx tsc --noEmit
npm run test:run
```

## Lo que falta para despliegue (frontend)

### Requerido para release

- [ ] Definir entorno productivo (`NEXT_PUBLIC_WS_URL`) apuntando al backend publico con `wss://`.
- [ ] Configurar dominio permitido en backend (evitar `setAllowedOrigins("*")` en produccion).
- [ ] Agregar pagina de error/reintento para caida de backend y reconexion prolongada.
- [ ] Agregar smoke test E2E del flujo: home -> queue -> game.
- [ ] Validar telemetria/logs de cliente para errores WebSocket.

### Recomendado

- [ ] Extraer estado de juego a un store global (context/reducer) para reconexion y refresh robusto.
- [ ] Manejar heartbeat cliente (`ping`) cada 20-30s para detectar cortes silenciosos.
- [ ] Preparar feature flags para habilitar/deshabilitar dummy UI hints.

## Guia para colocar imagenes de personajes

Actualmente el backend envia `characterId` y `displayName`. Para soportar imagen real sin romper compatibilidad, hay dos caminos:

### Opcion A (recomendada): catalogo en frontend por `characterId`

1. Agregar assets en `apps/web/public/characters/`.
2. Crear un mapa en `src/lib/character-assets.ts`:
	- key: `characterId`
	- value: ruta publica (`/characters/char-1.webp`) y metadatos opcionales.
3. En `CharacterCard`, renderizar `<img>`/`next/image` cuando exista ruta; si no, mantener fallback de inicial.

Ventajas:
- No requiere cambiar contrato de backend.
- Permite iterar rapido en UI.

### Opcion B: backend envia `imageUrl`

1. Extender payload de `game_started.board.characters[]` con `imageUrl`.
2. Actualizar tipos en `src/lib/protocol.ts`.
3. Ajustar UI para consumir `imageUrl` con fallback si viene vacio.

Ventajas:
- Fuente unica de verdad en backend.
- Facil versionado del catalogo.

### Checklist de imagenes

- [ ] Definir naming estable por `characterId`.
- [ ] Usar formato optimizado (`webp` recomendado, `png` fallback).
- [ ] Comprimir assets (objetivo < 100KB por imagen).
- [ ] Incluir `alt` accesible por personaje.
- [ ] Mantener fallback visual cuando falte la imagen.
