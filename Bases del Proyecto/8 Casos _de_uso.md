# CASOS DE USO - RACCORD

## MÓDULO 1: GESTIÓN DE USUARIOS Y SEGURIDAD

### CU001 – Validar Usuario

#### Actores

- **Primario:** Cualquier usuario registrado
- **Secundario:** Sistema de autenticación, Servicio de email/SMS, Sistema de LOG

#### Descripción

Verifica el acceso al sistema mediante correo electrónico, contraseña y código de autenticación de dos factores (2FA). Solo se permite el ingreso a usuarios activos vinculados a un proyecto específico. El sistema registra todos los intentos de acceso (exitosos y fallidos) en una tabla LOG mediante disparadores para auditoría de seguridad, incluyendo: timestamp, usuario, IP de origen, dispositivo y resultado del intento.

#### Precondiciones

- Usuario registrado en el sistema con estado "Activo"
- Usuario vinculado a al menos un proyecto activo
- Cuenta activada (si fue registrado por administrador)
- Servicio de 2FA operativo

#### Postcondiciones

**Si exitoso:**
- Token JWT generado (30min)
- Sesión activa creada
- Usuario redirigido según rol
- Registro en LOG

**Si fallido:**
- Contador de intentos +1
- Bloqueo temporal tras 3 intentos (15 min)
- Registro en LOG
- Mensaje de error genérico

#### Flujo Principal

1. Usuario ingresa email y contraseña
2. Sistema valida credenciales en BD
3. Sistema genera código 2FA de 6 dígitos
4. Sistema envía código al canal elegido (email/SMS)
5. Usuario ingresa código 2FA
6. Sistema valida código (válido 2 minutos)
7. Sistema genera token JWT (30 Min) y refresh token (7 días)
8. Sistema registra acceso en LOG
9. Sistema redirige a página principal según rol

#### Flujos Alternativos


**FA-001: Recuperación de Contraseña**: Usuario selecciona "¿Olvidaste tu contraseña?" → Sistema envía email con enlace temporal (1 hora) → Usuario crea nueva contraseña → Sistema actualiza y registra LOG

**FA-002: Reenvío Código 2FA** : Usuario no recibe código → Selecciona "Reenviar" → Sistema invalida anterior y envía nuevo por canal alternativo

**FA-003: Activación de Cuenta Pendiente** : Usuario con cuenta "Inactiva" intenta login → Sistema muestra mensaje → Opción "Reenviar email activación" → Usuario activa cuenta mediante enlace → Redirigido a login

#### Flujos Excepcionales

**FE-001: Cuenta Bloqueada (3 intentos fallidos)**: Sistema bloquea cuenta 15 minutos → Envía email notificación → Muestra mensaje bloqueo → Desbloqueo automático tras 15 min

**FE-002: Cuenta Revocada** : Usuario "Revocado" intenta login → Sistema detecta estado → Muestra mensaje "Contacta administrador" → No permite continuar

**FE-003: Servicio 2FA No Disponible** : Sistema detecta fallo 2FA → Activa modo degradado sin 2FA (JWT 30 min) → Alerta Admins → Obliga cambio contraseña en próximo login

**FE-004: Código 2FA Expirado** :Usuario ingresa código después de 2 minutos → Sistema rechaza → Muestra "Código expirado, solicita uno nuevo"

#### Frecuencia

Alta (5-10 logins/usuario/día)


### CU002 – Gestionar Usuario

#### Actores

- **Primario:** Sub-admin
- **Secundario:** Sistema de notificaciones, Sistema de LOG, Usuario creado

#### Descripción

Permite al Productor de Línea o Jefe de Producción realizar el registro, consulta, modificación, asignación de rol y revocación de acceso de usuarios. Los usuarios pueden actualizar su perfil personal (nombre, teléfono, foto de perfil, configuración de notificaciones) pero no pueden modificar su rol ni permisos. El sistema permite tres estados de usuario: Activo, Inactivo (suspensión temporal) y Revocado (finalización de contrato, sin posibilidad de reactivación por el usuario). Solo el administrador del proyecto puede reactivar usuarios inactivos.

#### Precondiciones

- Ejecutor con rol "Administrador Total" autenticado
- Al menos un proyecto activo en el sistema
- Para nuevo usuario: email único, cupo disponible
- No se puede revocar último Administrador Total

#### Postcondiciones

**Registro:**
- Usuario creado estado "Inactivo"
- Email de activación enviado
- Vinculado a proyecto
- Registro en LOG

**Modificación:**
- Campos actualizados
- Permisos recalculados si cambió rol
- Usuario notificado
- Registro en LOG

**Revocación:**
- Estado "Revocado"
- Sesiones cerradas (<60s)
- Tokens invalidados
- Eliminado de canales
- Registro en LOG

**Actualización perfil:**
- Campos editables actualizados
- Foto comprimida (500x500px)
- Sincronizado en todos dispositivos

#### Flujo Principal (Registro)

1. Administrador Total abre formulario "Nuevo Usuario"
2. Administrador completa datos: nombre, apellido, email, teléfono, departamento, rol, proyecto(s)
3. Sistema valida email único
4. Sistema genera contraseña temporal aleatoria
5. Sistema crea usuario con estado "Inactivo"
6. Sistema envía email con credenciales y enlace de activación
7. Sistema registra acción en LOG
8. Sistema muestra mensaje "Usuario creado exitosamente"

#### Flujo Principal (Modificación)

1. Administrador selecciona usuario de lista
2. Administrador modifica campos autorizados
3. Sistema valida cambios
4. Sistema actualiza registro en BD
5. Sistema recalcula permisos si cambió rol
6. Sistema notifica cambios al usuario
7. Sistema registra modificación en LOG

#### Flujo Principal (Revocación)

1. Administrador selecciona usuario y hace clic "Revocar Acceso"
2. Sistema muestra confirmación
3. Administrador confirma
4. Sistema cambia estado a "Revocado"
5. Sistema cierra todas las sesiones activas del usuario
6. Sistema invalida tokens JWT
7. Sistema registra revocación en LOG
8. Sistema envía email de notificación al usuario

#### Flujos Alternativos

**FA-001: Modificación Masiva de Roles** Admin selecciona múltiples usuarios → Cambia rol en grupo → Sistema recalcula permisos RBAC → Notifica usuarios afectados

**FA-002: Reactivación de Usuario Inactivo** : Admin selecciona usuario "Inactivo" → Clic "Reactivar" → Sistema cambia estado a "Activo" → Notifica usuario → Registra LOG

#### Flujos Excepcionales

**FE-001: Intento Revocar Último Admin** : Sistema detecta es último Admin Total → Rechaza operación → Muestra error "Debe haber al menos un Admin Total activo"

**FE-002: Email Duplicado en Registro** : Sistema detecta email ya existe → Rechaza registro → Sugiere "¿Olvidaste tu contraseña?" o usar email diferente

**FE-003: Enlace Activación Expirado** : Usuario intenta activar después de 48h → Sistema detecta expiración → Muestra mensaje → Admin debe reenviar enlace

#### Frecuencia

Baja

### CU003 – Gestionar Roles y Permisos

#### Actores

- **Primario:** Administrador total, sub-admi
- **Secundario:** Sistema RBAC, Sistema de LOG, Usuario afectado

#### Descripción

Permite al Productor de Línea asignar y modificar roles de usuario entre los 7 niveles disponibles: Administrador Total, Continuidad Total, Jefe de Departamento, Asistente de Departamento, Dirección, Talento, y Lectura General. Cada rol tiene permisos granulares predefinidos sobre los módulos de Continuidad Visual, Calendario, Comunicación y Seguridad. El sistema valida que al menos un usuario tenga rol de Administrador Total en todo momento.

#### Precondiciones

- Ejecutor con rol "Administrador Total" autenticado
- Usuario objetivo existe con estado "Activo" o "Inactivo"
- Existe al menos 1 Administrador Total adicional (si se va a cambiar último Admin)

#### Postcondiciones

**Si exitoso:**
- Rol actualizado
- Permisos recalculados según matriz RBAC
- Cache invalidado (<2s)
- Usuario notificado
- Registro en LOG

**Si intento cambiar último Admin:**
- Operación rechazada
- Mensaje de error
- Registro en LOG
- Sin cambios en BD

#### Flujo Principal

1. Administrador Total selecciona usuario de lista
2. Administrador hace clic "Cambiar Rol"
3. Administrador selecciona nuevo rol
4. Sistema valida que no es el último Administrador Total
5. Sistema actualiza rol en BD
6. Sistema recalcula permisos según matriz RBAC
7. Sistema invalida cache de permisos (<2s)
8. Sistema notifica cambio al usuario (email + push)
10. Sistema registra cambio en LOG

#### Flujos Alternativos

**FA-001: Asignación Temporal de Permisos Elevados** : Admin otorga rol superior temporal (ej: Asistente → Jefe Depto por 48h) → Sistema programa reversión automática → Notifica usuario → Al cumplir plazo revierte rol y notifica

#### Flujos Excepcionales

**FE-001: Cambio Rol Último Admin Total** : Sistema detecta es último Admin → Bloquea operación → Error "Asigna otro Admin Total primero"

#### Frecuencia

Baja

## MÓDULO 2: CONTINUIDAD VISUAL

### CU004 – Gestionar Fotografías de Continuidad

#### Actores

- **Primario:** onset y usuario
- **Secundario:** Sistema de storage, Sistema de marca de agua, Sistema de IA, Sistema de notificaciones, Sistema de LOG

#### Descripción

Permite al Script, Jefes de Departamento y Asistentes Onset subir fotografías de continuidad desde dispositivos móviles o tablets. Al subir, el sistema solicita completar campos obligatorios: Proyecto, Episodio, Escena, Toma, Personaje(s), Detalle (vestuario/maquillaje/utilería/set), Estado de Continuidad (OK/Pendiente/Error), y Comentarios adicionales. El sistema genera automáticamente la nomenclatura estandarizada: `PROYECTO_EPISODIO_ESCENA_TOMA_PERSONAJE_DETALLE_VERSION` y aplica marca de agua no removible con usuario y timestamp. Las fotografías se pueden modificar (actualizar metadatos, añadir comentarios) o marcar como "Versión anterior" cuando se sube una nueva versión del mismo detalle. Solo el Script puede eliminar fotografías (quedan en papelera por 30 días).

#### Precondiciones

- Usuario autenticado con rol autorizado (Script/Jefe Depto/Asistente Onset)
- Proyecto activo seleccionado
- Personajes registrados en tabla PERSONAJES
- Espacio storage ≥50 MB, almacenamiento local ≥100 MB (offline)
- No excedió límite 100 fotos/día

#### Postcondiciones

**Subida online:**
- Foto en storage con nomenclatura
- Versión auto-incrementada (V1→V2)
- Metadatos en BD
- Marca de agua aplicada
- Archivo original cifrado (AES-256)
- Hash SHA-256 calculado
- Visible en búsquedas
- Tarea IA encolada
- Registro en LOG
- Si Estado="Error": Notificación urgente a Jefe Depto según tipo detalle, marcada "Bloqueante" en reportes

**Subida offline:**
- Foto en almacenamiento local (SQLite)
- Marcada "Pendiente sincronización"
- Sincroniza automáticamente al reconectar

**Modificación:**
- Campos actualizados
- Timestamp modificado
- Registro en LOG

**Eliminación:**
- Estado "Eliminada"
- Movida a papelera (30 días)
- Excluida de búsquedas
- Registro en LOG

#### Flujo Principal (Subir)

1. Usuario abre módulo "Continuidad Visual"
2. Usuario hace clic "Subir Fotografía"
3. Sistema muestra opciones: "Tomar Foto" o "Seleccionar de Galería"
4. Usuario selecciona opción y captura/selecciona foto
5. Sistema muestra preview y formulario de metadatos
6. Usuario completa campos obligatorios: Proyecto, Episodio, Escena, Toma, Personaje(s), Tipo Detalle, Estado, Comentarios
7. Usuario hace clic "Guardar"
8. Sistema valida campos completos y formato/tamaño archivo
9. Sistema genera nomenclatura: `PROYECTO_EPISODIO_ESCENA_TOMA_PERSONAJE_DETALLE_VERSION`
10. Sistema verifica duplicados y asigna/incrementa versión (V1, V2...)
11. Sistema aplica marca de agua (usuario + timestamp + proyecto)
12. Sistema sube foto a storage cifrado
13. Sistema guarda metadatos en BD
14. Sistema registra acción en LOG
15. Si Estado="Error": sistema notifica a Jefe de Departamento
16. Sistema muestra mensaje "Fotografía subida exitosamente"

#### Flujos Alternativos

**FA-001: Subida Masiva de Fotografías** : Usuario selecciona múltiples fotos (hasta 50) → Sistema procesa en lote → Aplica nomenclatura secuencial → Muestra progreso → Notifica completado

**FA-002: Modo Offline - Sincronización Posterior** : Usuario sin conexión → Fotos almacenadas SQLite local → Al reconectar, sistema detecta pendientes → Sincroniza automáticamente → Notifica éxito/errores

**FA-003: Actualizar Metadatos sin Subir Nueva Versión** : Usuario selecciona foto → "Editar Metadatos" → Modifica campos → Sistema actualiza BD sin crear nueva versión → Registra LOG

#### Flujos Excepcionales

**FE-001: Fotografía Marcada "Bloqueante"** : Jefe Depto marca estado "Error" → Sistema genera notificación urgente a Script y AD → Foto destacada en rojo en galería → Requiere resolución antes de continuar escena

**FE-002: Límite 100 Fotos/Día Excedido** : Sistema detecta límite → Bloquea subida → Muestra "Límite diario alcanzado. Contacta Admin para ampliar cuota"

**FE-003: Storage Sin Espacio** : Sistema detecta storage <50MB → Bloquea subida → Alerta Admins → Muestra "Espacio insuficiente, contacta soporte técnico"

**FE-004: Fallo Aplicación Marca de Agua** : Sistema no puede aplicar marca → Rechaza subida → Registra error crítico → Alerta técnicos → No guarda foto sin marca

#### Frecuencia

Alta

### CU005 – Buscar Fotografías de Continuidad

#### Actores

- **Primario:** Todos los usuarios autorizados
- **Secundario:** Motor búsqueda PostgreSQL, Sistema de caché (Redis), Sistema RBAC

#### Descripción

Permite a todos los usuarios autorizados buscar fotografías mediante filtros múltiples: Proyecto, Episodio, Escena, Toma, Personaje, Fecha de rodaje, Departamento (Vestuario/Maquillaje/Utilería/Set), Estado de Continuidad, o búsqueda libre en comentarios. El sistema devuelve resultados en vista de galería con miniaturas, ordenados por defecto por Escena > Toma > Personaje. El tiempo de respuesta debe ser <10 segundos incluso con 10,000+ fotografías en base de datos. Los usuarios con rol de Talento solo pueden buscar dentro de sus escenas asignadas.

#### Precondiciones

- Usuario autenticado con acceso a Continuidad Visual
- Talento: escenas asignadas en tabla TALENTO_ESCENAS
- Al menos 1 fotografía en proyecto activo
- Índices BD creados, sistema caché operativo

#### Postcondiciones

**Con resultados:**
- Query SQL ejecutada con filtros (AND)
- Filtro automático Talento aplicado
- Resultados en <10s (objetivo <5s)
- Galería con thumbnails (300x300px)
- Metadatos en hover
- Ordenados Escena>Toma>Personaje
- Paginación 50/página
- Búsquedas frecuentes cacheadas (TTL 5min)

**Sin resultados:**
- Mensaje sugerencia
- Opción limpiar filtros
- Registro en LOG

**Si excede 10s:**
- Indicador progreso
- Sugerencia refinar filtros
- Alerta a admins
- Registro en LOG

#### Flujo Principal

1. Usuario abre módulo "Continuidad Visual"
2. Usuario aplica filtros deseados: Proyecto, Episodio, Escena, Toma, Personaje, Fecha, Departamento, Estado
3. Usuario hace clic "Buscar"
4. Sistema construye query SQL con filtros (AND entre filtros)
5. Si usuario es Talento: sistema añade filtro automático "solo mis escenas"
6. Sistema ejecuta búsqueda en PostgreSQL
7. Sistema devuelve resultados en <10 segundos
8. Sistema muestra galería con thumbnails (300x300px)
9. Sistema ordena por: Escena (ASC) → Toma (ASC) → Personaje (ASC)
10. Sistema muestra contador "Mostrando X-Y de Z fotografías"
11. Sistema implementa paginación (50 resultados/página)

#### Flujos Alternativos

**FA-001: Búsqueda Guardada (Favoritos)** : Usuario guarda combinación de filtros → Asigna nombre → Sistema guarda en tabla BUSQUEDAS_FAVORITAS → Usuario accede desde "Mis Búsquedas" → Carga filtros automáticamente

**FA-002: Búsqueda por Similitud Visual (IA)** : Usuario selecciona foto como referencia → Sistema ejecuta búsqueda por similitud visual (pHash) → Muestra fotos similares ordenadas por % similitud

#### Flujos Excepcionales

**FE-001: Timeout de Búsqueda (>10s)** : Sistema detecta exceso tiempo → Cancela query → Muestra "Búsqueda muy amplia, refina filtros" → Registra alerta Admins

**FE-002: Caché Redis No Disponible** : Sistema detecta Redis caído → Ejecuta búsqueda directa en PostgreSQL → Registra alerta técnicos → Funciona degradado (más lento)

#### Frecuencia

Alta

### CU006 – Visualizar y Comparar Fotografías

#### Actores

- **Primario:** Todos los usuarios autorizados según rol
- **Secundario:** Sistema storage, Sistema marca de agua, Sistema de LOG

#### Descripción

Permite a todos los usuarios autorizados visualizar fotografías en alta resolución con zoom, rotación y navegación secuencial (anterior/siguiente). El sistema muestra metadatos completos al lado de la imagen: nomenclatura, personajes, detalle, fecha, estado, comentarios, historial de versiones. Adicionalmente, permite comparación lado a lado de hasta 4 fotografías simultáneamente para detectar inconsistencias visuales. La visualización siempre incluye marca de agua dinámica superpuesta (no descargable sin marca). Los Jefes de Departamento pueden añadir comentarios específicos a cualquier fotografía.

#### Precondiciones

- Usuario autenticado con permiso visualización según rol
- Fotografía(s) existe(n) con estado "Activa"
- Talento: foto de escena asignada
- Storage accesible
- Para comparación: 2-4 fotos seleccionadas

#### Postcondiciones

**Visualización individual:**
- Imagen cargada (<5s con 10Mbps)
- Marca de agua dinámica (usuario+timestamp+proyecto, posición aleatoria, 30-40% opacidad)
- Metadatos en panel lateral
- Controles disponibles (zoom, rotación, navegación, fullscreen)
- Capturas bloqueadas (móvil) o click derecho deshabilitado (web)
- Registro en LOG
- Si Jefe Depto añade comentario: Guardado en BD, notificación a Script, visible para todos autorizados

**Comparación 2-4 fotos:**
- Vista dividida, mismo tamaño
- Zoom/scroll sincronizado
- Metadatos bajo cada imagen
- Posiciones intercambiables (drag&drop)
- Exportable a PDF (Script/Jefe Depto) con marca de agua reforzada
- Registro en LOG

#### Flujo Principal (Visualizar)

1. Usuario hace clic en fotografía desde galería
2. Sistema carga imagen alta resolución desde storage
3. Sistema aplica marca de agua dinámica en tiempo real (posición aleatoria)
4. Sistema muestra imagen en visor con metadatos en panel lateral
5. Sistema habilita controles: zoom, rotación, navegación, fullscreen
6. Sistema bloquea capturas de pantalla (móvil) o click derecho (web)
7. Sistema registra visualización en LOG (quién, qué, cuándo)

#### Flujo Principal (Comparar)

1. Usuario selecciona 2-4 fotografías desde galería
2. Usuario hace clic "Comparar"
3. Sistema carga fotografías seleccionadas
4. Sistema muestra vista dividida (2, 3 o 4 columnas)
5. Sistema aplica marca de agua a todas las imágenes
6. Sistema sincroniza zoom y scroll entre todas las imágenes
7. Sistema muestra metadatos básicos bajo cada imagen
8. Sistema permite intercambiar posiciones (drag&drop)
9. Sistema registra comparación en LOG

#### Flujos Alternativos

**FA-001: Comparación con Anotaciones Temporales** : Durante comparación → Usuario dibuja círculos/flechas sobre fotos → Marcas NO se guardan → Solo para referencia visual temporal → Se pierden al cerrar

**FA-002: Navegación Secuencial en Visor** : Usuario visualiza foto → Usa teclas ←/→ o botones → Sistema carga anterior/siguiente de la galería actual → Mantiene contexto de búsqueda

#### Flujos Excepcionales

**FE-001: Imagen No Disponible en Storage** : Sistema intenta cargar foto → Storage no responde → Muestra "Imagen temporalmente no disponible" → Registra error → Alerta técnicos

**FE-002: Intento Captura de Pantalla** : Usuario intenta captura (móvil) → Sistema detecta → Pantalla negra momentánea → Muestra alerta "Capturas no permitidas" → Registra intento en LOG

#### Frecuencia

Alta


### CU007 – Analizar Fotografías con IA

#### Actores

- **Primario:** Sub - admi y usuarios
- **Secundario:** Sistema IA (YOLOv8), Worker (Celery/Redis), Sistema notificaciones, Sistema LOG

#### Descripción

El sistema ejecuta automáticamente análisis de imágenes con inteligencia artificial al subir fotografías nuevas o a solicitud manual del Script. El análisis detecta y etiqueta automáticamente: tipo de prenda de vestuario, accesorios visibles (joyas, gafas, relojes), elementos de utilería, personas en escena. Cuando se suben múltiples tomas de la misma escena, el sistema compara automáticamente y genera alertas si detecta inconsistencias significativas (>20% de diferencia en elementos clave). El Script puede validar o descartar alertas. La precisión objetivo es ≥80% con mejora continua mediante reentrenamiento.

#### Precondiciones

- Fotografía almacenada en sistema
- Modelo YOLOv8 desplegado y accesible
- Worker Celery operativo, cola Redis con espacio
- Para comparación: ≥2 fotos de misma escena
- Precisión modelo actual >70%

#### Postcondiciones

**Análisis automático:**
- Tarea encolada (no bloquea subida)
- Imagen pre-procesada (640x640px)
- YOLOv8 ejecutado
- Objetos detectados con confianza (%)
- Etiquetas <50% descartadas
- Sugerencias en BD estado "Sugerida"
- Script notificado, puede aceptar/modificar/descartar

**Comparación automática:**
- Trigger al subir 2ª+ foto misma escena
- pHash+SSIM+YOLOv8 ejecutados
- Diferencias >20% generan alerta (descripción+thumbnails+confianza)
- Notificación urgente a Script
- Script valida: OK/Error/Descartar
- Validaciones registradas para reentrenamiento

**Análisis manual:**
- Tarea prioridad Alta encolada
- Resultado en <60s

**Si IA no disponible:**
- Error en LOG
- Foto subida normal
- Reintento en 5min
- Tras 3 fallos alerta a admins

#### Flujo Principal (Automático)

1. Usuario sube fotografía (CU004)
2. Sistema encola tarea de análisis en Celery (background)
3. Worker toma tarea y descarga imagen desde storage
4. Worker pre-procesa imagen (640x640px, normalización)
5. Worker ejecuta modelo YOLOv8
6. Sistema detecta objetos con nivel de confianza (%)
7. Sistema descarta etiquetas con confianza <50%
8. Sistema guarda etiquetas sugeridas en BD (estado "Sugerida")
9. Sistema notifica a Script "Análisis IA completado"
10. Script visualiza sugerencias y puede: Aceptar, Modificar, Descartar
11. Si acepta: etiqueta se convierte en "Validada"

#### Flujo Principal (Comparación automática)

1. Usuario sube 2ª+ fotografía de misma escena
2. Sistema detecta escena duplicada (trigger)
3. Sistema carga todas las fotos de la escena
4. Sistema ejecuta pHash + SSIM + YOLOv8 en todas
5. Sistema compara listas de objetos detectados
6. Si diferencia >20%: sistema genera alerta
7. Sistema envía notificación urgente a Script con descripción + thumbnails
8. Script valida alerta: OK / Error a Corregir / Descartar (falso positivo)
9. Sistema registra validación para mejora del modelo

#### Flujos Alternativos

**FA-001: Análisis Manual Prioritario** : Script selecciona foto → "Analizar con IA Ahora" → Tarea prioridad Alta → Worker procesa inmediatamente → Resultado <60s

**FA-002: Reentrenamiento del Modelo** : Script valida 500+ alertas → Sistema recopila datos → Ejecuta reentrenamiento → Modelo actualizado → Precisión aumenta

#### Flujos Excepcionales

**FE-001: Modelo IA No Disponible** : Sistema no puede conectar YOLOv8 → Foto se sube normal → Registra error → Reintento cada 5min × 3 → Tras 3 fallos: alerta Admins

**FE-002: Cola Celery Saturada** : Cola >10,000 tareas → Sistema pausa nuevos análisis → Alerta Admins "Aumentar workers" → Procesa cola pendiente primero

**FE-003: Falso Positivo Recurrente** : Sistema detecta mismo error >10 veces → Marca patrón problemático → Notifica técnicos → Excluye de alertas temporalmente

#### Frecuencia

Alta

## MÓDULO 3: PLANEACIÓN Y COMUNICACIÓN

### CU008 – Gestionar Calendario de Rodaje

#### Actores

- **Primario:** Administrador y sub - admi
- **Secundario:** Jefes de Departamento, Sistema notificaciones (CU009), Sistema desgloses (CU010), Sistema LOG

#### Descripción

Permite a la Primera Ayudante de Dirección crear, modificar y eliminar eventos en el calendario centralizado. Cada evento representa una jornada de rodaje e incluye varios campos obligatorios: Fecha, Escenas a grabar (múltiples), Locación, Personajes presentes, Hora ficticia (día/tarde/noche), Páginas de guion, Equipo técnico requerido, Vestuario específico por personaje, Maquillaje especial, Utilería clave, Notas de dirección, y Estado (Confirmado/Tentativo/Cancelado). Los Jefes de Departamento pueden añadir notas específicas a su área. El calendario tiene vista diaria, semanal y mensual. Todos los usuarios pueden visualizar el calendario.

#### Precondiciones

- Ejecutor Primera AD o Administrador Total autenticado
- Proyecto activo seleccionado
- Escenas y personajes registrados en BD
- Para modificar: evento existe, no cancelado definitivamente

#### Postcondiciones

**Creación:**
- Evento en BD con campos ya definidos.
- UUID generado
- Desgloses vinculados automáticamente
- Usuarios afectados calculados (personajes+deptos+Script+AD)
- Visible en vistas (diaria/semanal/mensual)
- Notificaciones enviadas (CU009)
- Registro en LOG

**Modificación:**
- Campos actualizados
- Cambios críticos identificados (Fecha/Escenas/Locación/Personajes)
- Historial en BD
- Usuarios recalculados
- Notificaciones según prioridad
- Registro en LOG

**Nota Jefe Depto:**
- Guardada en BD vinculada a evento
- Visible en sección departamento
- Notificación a Primera AD
- Registro en LOG

**Cancelación:**
- Marcado "Cancelado"
- Notificación crítica a todos afectados
- Movido a "Eventos Cancelados"
- Registro en LOG

**Visualización:**
- Talento ve solo sus eventos (filtro auto)
- Eventos coloreados (Confirmado:verde, Tentativo:amarillo, Cancelado:rojo)

#### Flujo Principal (Crear)

1. Primera AD abre "Calendario"
2. Primera AD hace clic "Nuevo Evento"
3. Sistema muestra formulario con campos obligatorios
4. Primera AD completa: Fecha, Escenas, Locación, Personajes, Hora ficticia, Páginas guion, Equipo técnico, Vestuario, Maquillaje, Utilería, Notas dirección, Estado
5. Sistema valida campos completos
6. Sistema genera UUID para evento
7. Si escenas tienen desgloses: sistema vincula información automáticamente
8. Sistema calcula usuarios afectados (personajes + deptos + Script + AD)
9. Sistema crea evento en BD
10. Sistema envía notificaciones push a usuarios afectados (CU009)
11. Sistema muestra evento en vistas: diaria, semanal, mensual
12. Sistema registra creación en LOG

#### Flujo Principal (Modificar)

1. Primera AD selecciona evento de calendario
2. Primera AD hace clic "Editar"
3. Primera AD modifica campos deseados
4. Sistema detecta qué campos cambiaron
5. Sistema identifica cambios críticos (Fecha/Escenas/Locación/Personajes)
6. Sistema guarda cambios en BD + historial
7. Sistema recalcula usuarios afectados
8. Sistema envía notificaciones según prioridad del cambio
9. Sistema registra modificación en LOG

#### Flujos Alternativos

**FA-001: Duplicar Evento Existente** : Primera AD selecciona evento → "Duplicar" → Sistema copia campos → Primera AD modifica fecha y personajes → Sistema crea nuevo evento

**FA-002: Exportar Calendario a PDF/Excel** : Primera AD selecciona rango fechas → "Exportar" → Sistema genera documento con eventos → Descarga con marca de agua

#### Flujos Excepcionales

**FE-001: Conflicto de Locación** : Sistema detecta locación ya usada mismo día/hora → Alerta "Conflicto detectado" → Primera AD confirma o cambia locación

#### Frecuencia

Media

### CU009 – Notificar Cambios de Calendario

#### Actores

- **Primario:** Todos los usuarios.
- **Secundario:** Firebase Cloud Messaging, SendGrid, Usuarios afectados

#### Descripción

El sistema envía automáticamente notificaciones push instantáneas (<30 segundos) a todos los usuarios afectados cuando se modifica, crea o cancela un evento en el calendario. La notificación identifica claramente: qué cambió, quién realizó el cambio, y cuándo. Los usuarios reciben notificación en la app móvil con alerta sonora y badge, y opcionalmente por email (configurable por usuario). La Primera AD puede marcar cambios como "Urgente" o "Crítico" para destacarlos visualmente. El sistema registra confirmación de lectura (timestamp) por cada usuario.

#### Precondiciones

- Evento creado/modificado/cancelado (trigger desde CU008)
- Usuarios con FCM tokens válidos
- Firebase y SendGrid operativos
- Permisos notificaciones concedidos en dispositivos

#### Postcondiciones

**Envío exitoso:**
- Trigger automático
- Usuarios afectados identificados
- Contenido generado (título+descripción+detalles+autor+timestamp+deep link)
- Prioridad aplicada (Normal/Urgente/Crítico con iconos/sonidos/vibraciones diferenciados)
- Enviado vía FCM (<30s)
- Email adicional si preferencia activa
- Reintentos automáticos (3×) si falla
- Timestamp envío en BD

**Usuario abre:**
- Timestamp lectura registrado
- Badge -1
- App redirige a evento
- Estado "Leída"

**Comunicados obligatorios:**
- Checkbox "He leído" requerido
- Timestamp confirmación guardado
- Reporte disponible (leídos/pendientes/%)

#### Flujo Principal

1. Sistema detecta cambio en tabla CALENDARIO (trigger automático)
2. Sistema identifica usuarios afectados
3. Sistema genera contenido de notificación: título + descripción + detalles + autor + timestamp
4. Sistema aplica prioridad visual según criticidad (Normal/Urgente/Crítico)
5. Sistema envía notificación vía Firebase Cloud Messaging
6. Si usuario tiene preferencia email activa: sistema envía email adicional
7. Sistema registra timestamp de envío en BD
8. Usuario recibe notificación push en dispositivo (<30 segundos)
9. Usuario abre notificación
10. Sistema registra timestamp de lectura
11. App redirige a evento en calendario

#### Flujos Alternativos

**FA-001: Notificación con Retraso Programado** : Primera AD programa notificación para envío futuro (ej: 24h antes de rodaje) → Sistema guarda en cola programada → Envía en horario especificado

#### Flujos Excepcionales

**FE-001: Firebase No Disponible** : Sistema detecta FCM caído → Guarda notificaciones en cola → Envía emails alternativos → Reintenta FCM cada 5min → Alerta técnicos

**FE-002: Usuario Sin Token FCM** : Sistema no encuentra token válido → Envía solo email → Registra usuario sin token → Sugiere reactivar notificaciones push

**FE-003: Envío Masivo (>100 usuarios)** : Sistema detecta >100 destinatarios → Divide en lotes de 100 → Procesa en paralelo → Evita rate limit FCM

#### Frecuencia

Alta

### CU010 – Gestionar Desgloses de Escenas

#### Actores

- **Primario:** Administrador y Sub-admi
- **Secundario:** Sistema calendario (CU008), Sistema fotografías (CU004), Sistema LOG

#### Descripción

Permite a la Primera AD y al Script crear y modificar desgloses detallados por escena individual. Cada desglose incluye: Número de escena, Descripción narrativa breve, Interior/Exterior, Día/Noche, Personajes con diálogo, Personajes secundarios/extras, Vestuario requerido por personaje con estado (limpio/sucio/roto), Maquillaje especial requerido, Utilería hero (objetos clave en acción), Vehículos, Efectos especiales, Armas/elementos regulados, y Notas de producción. Los desgloses se vinculan automáticamente a las fotografías de continuidad de la misma escena. Todos los departamentos pueden visualizar desgloses completos.

#### Precondiciones

- Ejecutor Primera AD o Script autenticado
- Proyecto activo, personajes registrados
- Número escena único en proyecto

#### Postcondiciones

**Creación:**
- Desglose en BD
- UUID generado
- Vestuario por personaje en subtabla
- Evento calendario actualizado automáticamente (si existe)
- Vinculado a fotos (búsqueda auto por num_escena)
- Visible todos deptos
- Registro en LOG

**Modificación:**
- Campos actualizados
- Timestamp modificado
- Notificación a Primera AD si vinculado a evento
- Registro en LOG

**Visualización:**
- Todos deptos ven completo
- Organizado por secciones
- Botones "Ver Fotografías" y "Ver Evento" disponibles

**Vinculación bidireccional:**
- Desde desglose→galería filtrada
- Desde foto→desglose en modal

#### Flujo Principal

1. Primera AD o Script abre "Desgloses"
2. Usuario hace clic "Nueva Escena"
3. Sistema muestra formulario de desglose
4. Usuario completa: Num escena, Descripción, INT/EXT, Día/Noche, Personajes diálogo, Secundarios/extras, Vestuario por personaje + estado, Maquillaje, Utilería hero, Vehículos, Efectos, Armas, Notas producción
5. Sistema valida número escena único
6. Sistema genera UUID
7. Sistema crea desglose en BD
8. Sistema guarda vestuario por personaje en subtabla
9. Si escena ya está en evento calendario: sistema actualiza evento automáticamente
10. Sistema vincula desglose a fotografías existentes (búsqueda por num_escena)
11. Sistema registra creación en LOG
12. Desglose queda visible para todos los departamentos

#### Flujos Alternativos

**FA-001: Importar Desgloses desde Excel** : Primera AD/Script selecciona archivo Excel → Sistema valida formato → Importa múltiples desgloses → Valida nums escena únicos → Crea en lote → Notifica completado

**FA-002: Plantilla de Desglose Reutilizable** : Script crea desglose con elementos comunes → Guarda como "Plantilla" → Al crear nueva escena → Selecciona plantilla → Sistema pre-llena campos

#### Flujos Excepcionales

**FE-001: Número Escena Duplicado** : Sistema detecta num_escena ya existe → Rechaza creación → Muestra "Escena ya registrada" → Sugiere editar existente o usar num diferente

#### Frecuencia

Media

### CU011 – Comunicación Interna por Canales

#### Actores

- **Primario:** Todos los usuarios
- **Secundario:** Sistema canales, Sistema notificaciones, Sistema adjuntos (CU004), Sistema LOG

#### Descripción

Permite a todos los usuarios enviar y recibir mensajes mediante canales segmentados: Canal General (todo el equipo), Canales por Departamento, Canales por Escena, Canales por Personaje, y Mensajes Directos. Los mensajes pueden incluir texto, fotografías adjuntas desde la galería de continuidad, documentos PDF, y nivel de prioridad (Normal/Urgente). Los mensajes urgentes se destacan con color rojo y generan notificación push inmediata. El sistema permite búsqueda histórica de mensajes. Los mensajes tienen confirmación de lectura visible para el emisor.

#### Precondiciones

- Usuario autenticado con acceso al canal (validación RBAC)
- Canal existe (canales Escena/Personaje auto-creados)
- Adjunto foto: existe en sistema; PDF: ≤10 MB

#### Postcondiciones

**Envío:**
- Validación acceso ejecutada
  - General: todos
  - Depto: usuarios depto + Script + Dir+Admin
  - Escena: involucrados + Script + AD + Dir
  - Personaje: actores + Script + Jefes + Dir
  - Directo: emisor + receptor
- Mensaje en BD
- Adjuntos vinculados (foto:referencia no duplica, nueva:carga+marca agua, PDF:validado)
- Visible inmediato online
- Notificación push offline (Normal:badge solo, Urgente:rojo+sonido+vibra)
- Checkmarks (✓:enviado, ✓✓:leído todos)
- Registro en LOG

**Búsqueda histórica:**
- Filtros (canal/keyword/emisor/fecha)
- Full-text search
- Orden cronológico inverso
- Keyword resaltado
- Paginación 50/página
- Click→scroll auto

**Confirmaciones lectura:**
- Modal con lista destinatarios (✓✓Leído+timestamp, ✓Entregado)
- % lectura mostrado

#### Flujo Principal

1. Usuario abre canal deseado (General/Departamento/Escena/Personaje/Directo)
2. Sistema valida acceso del usuario al canal (RBAC)
3. Usuario escribe mensaje (máx 2000 caracteres)
4. Usuario opcionalmente adjunta: foto desde galería, foto nueva, PDF
5. Usuario selecciona prioridad: Normal / Urgente
6. Usuario hace clic "Enviar"
7. Sistema valida adjuntos (foto existe o PDF ≤10MB)
8. Sistema crea mensaje en BD
9. Sistema vincula adjuntos (foto: referencia, nueva: sube+marca agua, PDF: valida)
10. Sistema muestra mensaje inmediatamente para usuarios online
11. Sistema envía notificación push a usuarios offline
12. Sistema implementa confirmación de lectura (✓ enviado, ✓✓ leído todos)
13. Sistema registra envío en LOG

#### Flujos Alternativos

**FA-001: Responder Mensaje Específico (Thread)** : Usuario selecciona mensaje → "Responder" → Mensaje vinculado como respuesta → Mostrado anidado con línea de conexión

**FA-002: Mencionar Usuario (@nombre)** : Usuario escribe @nombre → Autocompletado → Sistema notifica usuario mencionado → Mensaje destacado para mencionado

**FA-003: Fijar Mensaje Importante** : Jefe Depto fija mensaje en canal → Permanece arriba → Visible con icono 📌 → Todos pueden ver hasta des-fijar

#### Flujos Excepcionales

**FE-001: PDF Excede 10MB** : Sistema detecta tamaño → Rechaza adjunto → Muestra "PDF muy grande, máx 10MB" → Sugiere comprimir o usar enlace externo

**FE-002: Usuario Sin Acceso a Canal** : Usuario intenta acceder canal restringido → Sistema valida RBAC → Bloquea acceso → Muestra "No tienes permisos para este canal"

#### Frecuencia

Alta

### CU012 – Gestionar Comunicados Generales

#### Actores

- **Primario:** Administrador
- **Secundario:** Sistema Canal General, Sistema notificaciones, Sistema reportes, Sistema LOG

#### Descripción

Permite a la Primera AD, Director y Productor de Línea enviar comunicados oficiales a todo el equipo o grupos específicos. Los comunicados se destacan visualmente en la app con icono especial y permanecen fijados en la parte superior del Canal General hasta ser archivados. Los comunicados requieren confirmación de lectura obligatoria (checkbox) y el sistema genera reporte de quién ha leído el comunicado.

#### Precondiciones

- Ejecutor Primera AD/Director/Productor autenticado
- Proyecto activo, destinatarios estado "Activo"

#### Postcondiciones

**Creación:**
- Destinatarios seleccionados (todos/deptos/creativos/producción/talento)
- Formulario completado (asunto 150 chars, contenido 5000 chars HTML, PDF ≤20MB)
- Comunicado en BD
- Publicado Canal General (icono, fijado sticky, fondo destacado)
- Notificación crítica a destinatarios
- Si requiere confirmación: checkbox "He leído" mostrado, permanece fijado hasta confirmar
- Registro en LOG

**Usuario lee:**
- Checkbox marcado
- Timestamp guardado
- Deja de estar fijado para usuario
- Notificación a autor

**Reporte confirmaciones:**
- Modal con leídos (lista + timestamp)
- No leídos (lista+tiempo transcurrido)
- Estadísticas (total/confirmados/%/pendientes)
- Opción reenviar recordatorio
- Exportable PDF/Excel

**Archivo:**
- Solo autor puede
- Deja de estar fijado
- Movido a "Archivados"
- Estado "Archivado"
- Registro en LOG

#### Flujo Principal

1. Primera AD/Director/Productor hace clic "Nuevo Comunicado"
2. Sistema muestra formulario con editor de texto enriquecido
3. Usuario selecciona destinatarios: Todos/Deptos/Creativos/Producción/Talento
4. Usuario completa: Asunto (150 chars), Contenido (5000 chars HTML), Adjuntos PDF (≤20MB)
5. Usuario marca "Requiere confirmación de lectura" (checkbox por defecto activo)
6. Usuario hace clic "Publicar"
7. Sistema crea comunicado en BD
8. Sistema publica en Canal General con icono 📣 y fondo destacado
9. Sistema fija comunicado en parte superior (sticky)
10. Sistema envía notificación push CRÍTICA a todos los destinatarios
11. Usuario destinatario abre comunicado
12. Si requiere confirmación: sistema muestra checkbox "He leído este comunicado"
13. Usuario marca checkbox
14. Sistema registra timestamp de confirmación
15. Comunicado deja de estar fijado para ese usuario

#### Flujos Alternativos

**FA-001: Programar Comunicado Futuro** : Autor programa fecha/hora publicación → Sistema guarda en cola → Publica automáticamente en horario → Envía notificaciones

#### Flujos Excepcionales

**FE-001: Reenvío Recordatorio a No Leídos** : Autor ve reporte → Detecta usuarios sin leer tras 24h → "Reenviar Recordatorio" → Sistema envía notificación urgente solo a pendientes

#### Frecuencia

Baja 


### CU013 – Gestionar Notas Personales en Calendario

#### Actores

- **Primario:** Cualquier usuario
- **Secundario:** Sistema calendario, Sistema cifrado

#### Descripción

Permite a cualquier usuario crear, modificar y eliminar notas personales privadas asociadas a cualquier día del calendario. Estas notas son completamente privadas y solo visibles para el usuario que las creó, sin importar su rol en el sistema. Las notas se muestran en el calendario con un icono distintivo (candado) visible solo para su creador. Estas notas NO son incluidas en reportes de producción ni son accesibles en auditorías, garantizando privacidad absoluta del usuario.

#### Precondiciones

- Usuario autenticado
- Fecha parte del calendario proyecto activo

#### Postcondiciones

**Creación:**
- Modal con formulario (contenido 1000 chars, tipo opcional:Recordatorio/Tarea/Observación/Idea)
- Nota en BD cifrada (AES-256 con clave única usuario)
- Flag privada=true
- Icono candado en fecha (solo visible para creador)
- NO en LOG

**Visualización:**
- Click en candado
- Contenido descifrado
- Modal con contenido+tipo
- Opciones Editar/Eliminar

**Modificación:**
- Contenido re-cifrado
- Timestamp actualizado
- NO en LOG

**Eliminación:**
- Confirmación requerida
- Nota borrada BD
- Icono removido
- NO en LOG

**Búsqueda:**
- Filtros (fecha/tipo/keyword)
- Resultados cronológicos
- Click→navega a fecha+abre nota

**Privacidad absoluta:**
- NO en reportes
- NO en LOG
- Admins NO ven
- NO en exportaciones
- Eliminadas automáticamente si usuario revocado

#### Flujo Principal

1. Usuario selecciona fecha en calendario
2. Usuario hace clic icono "Añadir Nota Personal"
3. Sistema muestra modal con formulario
4. Usuario ingresa contenido (máx 1000 chars) y tipo opcional (Recordatorio/Tarea/Observación/Idea)
5. Usuario hace clic "Guardar"
6. Sistema cifra contenido con AES-256 (clave única del usuario)
7. Sistema crea nota en BD con flag privada=true
8. Sistema muestra icono candado en fecha (solo visible para creador)
9. Nota NO se registra en LOG (privacidad absoluta)

#### Flujos Alternativos

**FA-001: Sincronización Multi-Dispositivo** : Usuario crea nota en móvil → Sistema sincroniza cifrada → Usuario abre tablet → Nota visible descifrada con misma clave

#### Frecuencia

Media


## MÓDULO 4: GESTIÓN DE GUIONES

### CU014 – Gestionar Versiones de Guion

#### Actores

- **Primario:** Administrador y Sub admi
- **Secundario:** Sistema storage, Sistema versionado, Sistema notificaciones, Sistema LOG

#### Descripción

Permite a la Primera AD, Script y Director subir versiones del guion en formato PDF. Cada versión tiene: número de versión, fecha de emisión, descripción de cambios principales, y estado (Borrador/Revisión/Aprobado/En Rodaje). El sistema mantiene historial completo de versiones con posibilidad de descargar versiones anteriores (solo para roles autorizados). Solo la versión marcada como "En Rodaje" es visible para Talento. El guion siempre se visualiza dentro de la app con marca de agua dinámica. El sistema desactiva capturas de pantalla en apps móviles y click derecho en web.

#### Precondiciones

- Ejecutor Primera AD/Script/Director autenticado
- Proyecto activo seleccionado
- Archivo PDF válido ≤50 MB
- Número versión único en proyecto

#### Postcondiciones

**Subida nueva versión:**
- Formulario completado (PDF, versión ej:v1.0, fecha emisión, descripción 1000 chars, estado:Borrador/Revisión/Aprobado/En Rodaje)
- Validaciones ejecutadas (formato/tamaño/versión única)
- PDF en storage cifrado (AES-256)
- Registro BD con metadatos+UUID
- Si estado="En Rodaje": versión anterior→"No visible Talento", nueva→visible Talento
- Notificación push todo equipo
- Historial actualizado
- Registro en LOG

**Descarga versión anterior:**
- Solo Script/AD/Dir/Admin
- Permiso verificado
- PDF con marca de agua reforzada
- Descarga en LOG
- Si no autorizado: mensaje error

**Talento visualiza:**
- Solo versión "En Rodaje" visible
- Anteriores ocultas
- Marca de agua con nombre actor
- Capturas bloqueadas
- Visualización en LOG

#### Flujo Principal

1. Primera AD/Script/Director hace clic "Subir Guion"
2. Sistema muestra formulario
3. Usuario completa: Archivo PDF (≤50MB), Versión (ej: v1.0), Fecha emisión, Descripción cambios (1000 chars), Estado (Borrador/Revisión/Aprobado/En Rodaje)
4. Usuario hace clic "Subir"
5. Sistema valida formato PDF, tamaño ≤50MB, versión única
6. Sistema almacena PDF en storage cifrado (AES-256)
7. Sistema crea registro en BD con metadatos + UUID
8. Si estado="En Rodaje": sistema marca versión anterior como "No visible para Talento"
9. Sistema envía notificación push a todo el equipo
10. Sistema actualiza historial de versiones
11. Sistema registra subida en LOG

#### Flujos Alternativos

**FA-001: Reversión a Versión Anterior** : Script/AD selecciona versión antigua → "Restaurar como Actual" → Sistema crea nueva versión (copia) con num incremental → Marca "En Rodaje"

#### Flujos Excepcionales

**FE-001: PDF Corrupto o Ilegible** : Sistema intenta procesar PDF → Detecta archivo corrupto → Rechaza subida → Muestra "Archivo dañado, intenta nuevamente"

**FE-002: Versión Duplicada** : Sistema detecta num_versión ya existe → Rechaza → Muestra "Versión ya registrada" → Sugiere incrementar número

#### Frecuencia

Baja

### CU015 – Visualizar Guion con Anotaciones

#### Actores

- **Primario:** Todos usuarios autorizados según rol
- **Secundario:** Sistema visor PDF, Sistema marca de agua, Sistema anotaciones, Sistema sincronización, Sistema LOG

#### Descripción

Permite a todos los usuarios autorizados visualizar el guion en visor PDF integrado dentro de la app. Los usuarios pueden realizar anotaciones personales (notas, resaltados, marcadores) que solo ellos ven. Los Jefes de Departamento pueden crear anotaciones departamentales (visibles para todo su equipo). El Script puede crear anotaciones generales visibles para todos los roles. Las anotaciones se sincronizan entre dispositivos del mismo usuario. El Talento solo puede anotar en sus escenas asignadas.

#### Precondiciones

- Usuario autenticado con permiso visualización según rol
- Guion subido al sistema (CU014)
- Talento: guion estado "En Rodaje"
- Storage accesible

#### Postcondiciones

**Visualización:**
- PDF desde storage
- Visor integrado (NO descarga externa)
- Marca de agua dinámica cada página (usuario+timestamp+proyecto, diagonal 30-40% opacidad)
- Controles (navegación/zoom/búsqueda texto)
- Anotaciones cargadas
- Capturas bloqueadas móvil (pantalla negra)
- Click derecho/atajos deshabilitados web
- Visualización en LOG (quién/versión/timestamp/duración/IP/dispositivo)

**Anotación personal:**
- Menú contextual (Nota/Resaltado/Marcador)
- Si Nota: pop-up 500 chars→guardada BD (usuario/guion/página/coords/tipo/contenido/visibilidad:Personal)
- Si Resaltado: color seleccionable→coords guardadas
- Si Marcador: página marcada→panel lateral
- Sincronizada todos dispositivos usuario

**Anotación departamental:**
- Solo Jefes Depto
- Visibilidad:Departamental
- Visible equipo depto+Script+Admin
- Notificación equipo
- Icono🔵

**Anotación general:**
- Solo Script
- Visibilidad:General
- Visible TODOS roles
- Notificación push todo equipo
- Icono🔴

**Editar/Eliminar:**
- Solo propias
- Otras solo lectura
- Al eliminar: borrada BD, sincronizada dispositivos

#### Flujo Principal (Visualizar)

1. Usuario selecciona versión de guion desde lista
2. Sistema valida permiso de visualización según rol
3. Sistema carga PDF desde storage
4. Sistema muestra visor PDF integrado
5. Sistema aplica marca de agua dinámica en cada página (usuario+timestamp+proyecto)
6. Sistema carga anotaciones existentes (personal, departamental, general según rol)
7. Sistema habilita controles: navegación, zoom, búsqueda texto
8. Sistema bloquea capturas (móvil) o click derecho (web)
9. Sistema registra visualización en LOG

#### Flujo Principal (Anotar)

1. Usuario selecciona texto o hace clic en página
2. Sistema muestra menú contextual: Nota / Resaltado / Marcador
3. Usuario elige opción (ej: "Nota")
4. Sistema muestra pop-up para ingresar texto (500 chars)
5. Usuario ingresa contenido y hace clic "Guardar"
6. Sistema guarda anotación en BD con: usuario, guion, página, coords, tipo, contenido, visibilidad
7. Sistema renderiza anotación con icono según visibilidad
8. Sistema sincroniza anotación en todos los dispositivos del usuario

#### Flujos Alternativos

**FA-001: Filtrar Anotaciones por Tipo** : Usuario activa filtro → Muestra solo: Personales / Departamentales / Generales / Todas → Vista actualizada

**FA-002: Exportar Anotaciones a PDF** : Script exporta anotaciones generales → Sistema genera PDF (página+anotación+autor) → Descarga con marca de agua

#### Flujos Excepcionales

**FE-001: Talento Intenta Anotar Escena No Asignada** : Sistema detecta escena no pertenece a actor → Bloquea anotación → Muestra "Solo puedes anotar tus escenas asignadas"

**FE-002: Conflicto Anotación Simultánea** : Dos usuarios anotan mismo punto → Sistema detecta coords similares → Guarda ambas desplazadas → Notifica conflicto

#### Frecuencia

Alta

### CU016 – Comparar Versiones de Guion

#### Actores

- **Primario:** Administrador, sub-admi y onset
- **Secundario:** Sistema extracción PDF, Algoritmo diff, Sistema reportes, Sistema LOG

#### Descripción

Permite al Script y Primera AD visualizar diferencias entre dos versiones del guion mediante comparación visual lado a lado (diff). El sistema resalta en colores: texto eliminado (rojo), texto nuevo (verde), texto modificado (amarillo). El Script puede generar un "Reporte de Cambios" en PDF que lista todas las diferencias entre versiones, útil para comunicar actualizaciones a los departamentos.

#### Precondiciones

- Ejecutor Script o Primera AD autenticado
- ≥2 versiones guion en sistema
- PDFs con texto extraíble (no escaneos imagen)

#### Postcondiciones

**Comparación:**
- Dos versiones seleccionadas (ej v1.0→v2.0)
- PDFs cargados
- Texto extraído (PyPDF2/pdfplumber, OCR si necesario)
- Diff ejecutado (difflib línea×línea)
- Diferencias categorizadas (eliminado:rojo, nuevo:verde, modificado:amarillo, sin cambio:normal)
- Vista lado a lado (izq:v1.0, der:v2.0, scroll sincronizado)
- Índice cambios (páginas+cantidad+navegación rápida)
- Registro en LOG

**Reporte Cambios:**
- PDF generado con:
  - Portada (proyecto/comparación/fecha/usuario)
  - Resumen (total cambios/páginas/escenas nuevas/eliminadas)
  - Detalle tabla (Página|Línea|Cambio|Anterior|Nuevo)
- Marca de agua reforzada (usuario+timestamp+proyecto múltiple diagonal)
- Descargable
- Registro en LOG
- Compartible vía CU012/CU011

#### Flujo Principal (Comparar)

1. Script/Primera AD hace clic "Comparar Versiones"
2. Sistema muestra selector de versiones
3. Usuario selecciona versión antigua (ej: v1.0) y versión nueva (ej: v2.0)
4. Usuario hace clic "Comparar"
5. Sistema carga ambas versiones desde storage
6. Sistema extrae texto de PDFs (PyPDF2/pdfplumber)
7. Sistema ejecuta algoritmo diff (difflib línea por línea)
8. Sistema categoriza diferencias: eliminado (rojo), nuevo (verde), modificado (amarillo)
9. Sistema muestra vista lado a lado: izquierda (v1.0), derecha (v2.0)
10. Sistema sincroniza scroll entre columnas
11. Sistema genera índice de cambios (páginas + cantidad)
12. Sistema registra comparación en LOG

#### Flujo Principal (Generar Reporte)

1. Usuario hace clic "Generar Reporte de Cambios"
2. Sistema genera PDF con: Portada + Resumen + Detalle tabla (Página|Línea|Cambio|Anterior|Nuevo)
3. Sistema aplica marca de agua reforzada
4. Sistema permite descarga del PDF
5. Sistema registra generación en LOG

#### Flujos Alternativos

**FA-001: Comparar Versiones No Consecutivas** : Usuario selecciona v1.0 y v3.0 (saltando v2.0) → Sistema compara directamente → Muestra cambios acumulados

#### Flujos Excepcionales

**FE-001: PDF Sin Texto Extraíble (Escaneo)** : Sistema intenta extracción → Detecta imagen escaneada → Ejecuta OCR (Tesseract) → Si falla: muestra "No se puede comparar PDF escaneado"

**FE-002: Diferencias Excesivas (>80% cambios)** : Sistema calcula % cambios → Detecta >80% → Alerta "Guiones muy diferentes, considera versión nueva en lugar de comparación"

#### Frecuencia

Baja

### CU017 – Vincular Guion con Continuidad

#### Actores

- **Primario:** Todos usuarios visualizando guiones
- **Secundario:** Sistema NLP detección escenas, Sistema búsqueda fotos (CU005), Sistema vinculación bidireccional

#### Descripción

El sistema vincula automáticamente cada escena del guion con las fotografías de continuidad correspondientes (match por número de escena). Al visualizar una escena en el guion, el usuario puede hacer clic en "Ver Continuidad" y el sistema abre una vista lateral con todas las fotografías de esa escena organizadas por personaje y detalle. El vínculo es bidireccional: desde las fotografías también se puede acceder a la escena correspondiente del guion.

#### Precondiciones

- Guion subido (CU014)
- Usuario visualizando guion (CU015)
- Fotografías existen para ≥1 escena
- Números escena coinciden guion-fotos

#### Postcondiciones

**Detección auto escenas:**
- Al subir/abrir guion: NLP ejecutado
- Regex aplicado ("INT.","EXT.","ESC.","ESCENA"+número)
- Números extraídos→tabla ESCENAS_GUION
- Si falla: Script etiqueta manual (modal ingresa número)

**"Ver Continuidad" desde guion:**
- Botón junto a num_escena
- Click→búsqueda auto CU005 (WHERE escena=actual)
- Vista dividida (izq:guion posición actual, der:galería fotos)
- Fotos organizadas (Personaje>Detalle>Toma)
- Navegación sin cerrar guion
- Sincronización posición

**"Ver Escena en Guion" desde foto:**
- Metadatos muestran "Escena[X]"+botón
- Click→abre guion CU015+navega auto a escena
- Escena resaltada (fondo amarillo)
- Vista dividida opcional (foto|guion)

**Navegación bidireccional:**
- Vínculo mantenido sesión
- Posición preservada
- Breadcrumbs mostrados

**Si escena sin fotos:**
- Botón deshabilitado (gris)
- Tooltip "No hay fotos"
- Sugerencia subir (si permiso)

#### Flujo Principal (Detección automática)

1. Usuario sube guion o abre por primera vez (CU014)
2. Sistema ejecuta análisis NLP automático
3. Sistema aplica regex: "INT.", "EXT.", "ESC.", "ESCENA" + número
4. Sistema extrae números de escena
5. Sistema guarda números en tabla ESCENAS_GUION
6. Sistema vincula automáticamente con fotografías (match por num_escena)

#### Flujo Principal (Ver Continuidad desde guion)

1. Usuario está visualizando guion (CU015)
2. Usuario hace clic botón "Ver Continuidad" junto a número de escena
3. Sistema ejecuta búsqueda automática (CU005) con filtro WHERE escena=actual
4. Sistema muestra vista dividida: izquierda (guion), derecha (galería fotos)
5. Sistema organiza fotos por: Personaje > Detalle > Toma
6. Usuario navega fotos sin cerrar guion
7. Posición del guion se mantiene sincronizada

#### Flujo Principal (Ver Guion desde foto)

1. Usuario visualiza fotografía (CU006)
2. Usuario hace clic botón "Ver Escena en Guion" en metadatos
3. Sistema abre guion (CU015)
4. Sistema navega automáticamente a la escena correspondiente
5. Sistema resalta escena con fondo amarillo
6. Usuario puede ver foto y guion simultáneamente

#### Flujos Alternativos

**FA-001: Corrección Manual Vinculación** : Script detecta num_escena incorrecto → Edita foto → Cambia num_escena → Sistema actualiza vínculo automáticamente

#### Flujos Excepcionales

**FE-001: NLP No Detecta Escenas** : Sistema ejecuta regex → No encuentra patrones → Notifica Script → Modal permite etiquetado manual página por página

**FE-002: Múltiples Escenas en Misma Página** : NLP detecta 2+ nums escena misma página → Sistema vincula a primera → Alerta Script para revisar

#### Frecuencia

Alta