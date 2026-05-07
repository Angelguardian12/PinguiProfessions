# PinguiProfessions - Wiki Oficial

**PinguiProfessions** es un robusto sistema de economía, roles y profesiones diseñado para servidores RPG de Minecraft. Obliga a los jugadores a especializarse e interactuar entre ellos para prosperar.

## 🎓 Sistema de Cursos (Escuela)
En lugar de simplemente hacer click en un menú, los jugadores deben "cursar" su profesión.
*   **Integración WorldGuard**: Los cursos funcionan mediante zonas geográficas. Si el jugador entra a una zona cuyo nombre contiene la profesión (ej. `escuela_herrero`), el sistema invocará **hologramas dinámicos** sobre todos los bloques registrados para ese curso.
*   **Progreso Dinámico**: Los jugadores deben interactuar con **todos** los bloques asignados al curso. Los hologramas muestran `[Pendiente]` o `[Completado]`.
*   **Graduación**: Al completar el curso, el jugador puede requerir entregar ciertos ítems para graduarse (Ej: El Herrero debe entregar 1 Yunque y 1 Espada de Hierro).

## ⚒️ Profesiones y Mecánicas

### 1. Herrero (Blacksmith)
*   **Restricción de Crafteo**: Solo los Herreros pueden craftear equipamiento de Diamante y Netherite. El resto del servidor tiene bloqueado este crafteo.
*   **Forja y Fallos**: Al graduarse, un Herrero tiene un **40% de probabilidad** de que el crafteo de este equipamiento falle.
*   **Pérdida Parcial**: Si el crafteo falla, no se pierde todo el material. El herrero pierde solo el **25% de los materiales** y el resto se conserva en la mesa.
*   **Subida de Nivel**: Cada forja exitosa le otorga 5 XP de profesión. Por cada 10 XP, la probabilidad de fallo se reduce un 1%, hasta llegar a **0%** de fallos cuando sea un maestro.

### 2. Extractor de Experiencia (Comercio de XP)
Dado que los yunques reales necesitan XP para reparar ítems, y los Herreros suelen quedarse en la forja, se ha implementado un sistema monetario basado en XP Vanilla.
*   **Comando**: `/extraerxp <niveles>`
*   Retira experiencia del jugador y entrega una **Botella de Experiencia Concentrada**.
*   Estas botellas están encriptadas mediante `PersistentDataContainer` (PDC), haciéndolas imposibles de falsificar.
*   Al darle click derecho, el jugador o herrero consume la botella y recibe exactamente los niveles almacenados.

### 3. Doctor
*   Puede utilizar un `Kit Médico` para reanimar jugadores o curarlos.
*   Posee un menú (`/profesiones doctor`) con la lista de jugadores conectados para teletransportarse a emergencias (sujeto a un cooldown).

### 4. Ladrón y Comisario (Thief & Investigator)
Un juego del gato y el ratón dentro del servidor.
*   **Ladrón**: Puede robar jugadores o usar Bombas de Humo (crafteables) para escapar. Al robar, tiene una probabilidad de dejar caer una "Pista" al suelo.
*   **Comisario**: Único capaz de desencriptar las pistas dejadas por los ladrones mediante un menú GUI, revelando la identidad del criminal.

### 5. Alquimista y Caballero (Integración Mágica)
Si la opción `use-mana` está activada en la config:
*   **Alquimistas**: Son los únicos que pueden craftear Botellas de Maná (Piedra Luminosa + Lapislázuli + Botella).
*   **Caballeros**: Obtienen bonificaciones de combate físico y un factor de resistencia pasiva al daño mágico procedente de otros plugins de magia (ej. ElementalOrigins).

### 6. Integración con SimpleClans (Límites por Clan)
Si utilizas el plugin `SimpleClans` y habilitas `use-simpleclans: true` en la configuración, podrás limitar el número de profesionales dentro de cada clan para fomentar la cooperación entre clanes.
*   **Límites Configurables**: Herreros (2), Doctores (2), Alquimistas (2), Ladrones (3), Comisarios (3), Caballeros (5). Taberneros y Comerciantes no tienen límite (`-1`).
*   **Aplicación**: Si un jugador intenta empezar el curso de Herrero pero su clan ya tiene 2 Herreros, el sistema lo rechazará.
*   **Sin Clan**: Los jugadores independientes (sin clan) pueden elegir cualquier profesión sin restricciones de límite.
*   **Anti-Bypass**: Si un clan invita a un Herrero y el clan ya tiene 2 Herreros, al momento de que el jugador acepte la invitación, el plugin interceptará su entrada y lo expulsará inmediatamente, notificándole del límite.

## 🛠️ Crafteos Especiales
El plugin registra recetas personalizadas que benefician a ciertas profesiones.

### 1. Kit Médico (Doctor)
Solo los doctores tienen el conocimiento para ensamblar este ítem en una mesa de trabajo. Sirve para reanimar jugadores inconscientes.
*   **Receta (Forma en Mesa de Crafteo)**:
    *   Fila 1: `[Semillas de Trigo]` `[Papel]` `[Semillas de Trigo]`
    *   Fila 2: `[Papel]` `[Papel]` `[Papel]`
    *   Fila 3: `[Semillas de Trigo]` `[Papel]` `[Semillas de Trigo]`

### 2. Poción de Maná (Alquimista)
Restaura 50 puntos de magia. Solo los alquimistas pueden prepararla si la integración mágica está activa.
*   **Receta (Mesa de Crafteo)**:
    *   Fila 1: `[Vacío]` `[Lapislázuli]` `[Vacío]`
    *   Fila 2: `[Vacío]` `[Polvo Piedra Luminosa]` `[Vacío]`
    *   Fila 3: `[Vacío]` `[Botella de Cristal]` `[Vacío]`

## ⚙️ Comandos
*   `/profesiones` (o `/prof`): Menú principal interactivo.
*   `/extraerxp <cantidad>`: Envasa XP vanilla para comercio.
*   `/reportar <jugador> <motivo>`: Envía un reporte que los comisarios y administradores pueden revisar en la comisaría.

## 📦 Gestión de Bases de Datos
*   El plugin utiliza **SQLite** de forma nativa (`database.db`) para guardar perfiles, rangos, profesiones y bloques completados de manera segura y eficiente.
