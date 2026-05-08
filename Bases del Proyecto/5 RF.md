
# **REQUISITOS FUNCIONALES - RACCORD**

## AUTENTICACIÓN Y GESTIÓN DE USUARIOS
### RF-001: Auto-registro del Primer Usuario (Administrador Total)
**Descripción**: El sistema debe permitir que el primer usuario se registre automáticamente como "Administrador Total" cuando la base de datos no contenga ningún usuario previo. Este escenario aplica cuando el cliente implementa Raccord por primera vez sin usuarios pre-cargados por los desarrolladores.

**Prioridad**: Crítica

**Entradas**:
- Nombre (texto, máx 50 caracteres)
- Apellido (texto, máx 50 caracteres)
- Email (formato RFC 5322)
- Teléfono (formato internacional)
- Contraseña (mínimo 12 caracteres: 1 mayúscula, 1 minúscula, 1 número, 1 carácter especial)
- Confirmación de contraseña (debe coincidir con contraseña)
- Nombre del proyecto inicial (texto, máx 100 caracteres)

**Proceso**: Sistema verifica si existe algún usuario en la base de datos  
Si base de datos está vacía (primer usuario):
- Habilitar formulario de auto-registro sin restricciones
- Validar formato de email y unicidad
- Validar política de contraseña
- Hashear contraseña con bcrypt (12 rounds)
- Crear usuario con rol "Administrador Total" automáticamente
- Crear proyecto inicial automáticamente
- Asignar usuario como Administrador Total del proyecto
- Enviar email de confirmación
- Activar cuenta automáticamente.
Si ya existen usuarios:

- Deshabilitar auto-registro
- Mostrar mensaje: "Contacte al administrador del sistema para obtener acceso"

**Salidas**:

- Usuario creado con rol "Administrador Total" y estado "Activo"
- Proyecto inicial creado
- Usuario vinculado al proyecto
- Email de confirmación enviado
- Registro en log de auditoría

**Precondiciones**:

- Base de datos de usuarios debe estar vacía para ese proyecto.
- Sistema debe estar desplegado y accesible

**Postcondiciones**:

- Primer usuario queda registrado como Administrador Total
- Proyecto inicial queda creado
- Auto-registro queda deshabilitado automáticamente
- Usuario puede iniciar sesión inmediatamente

**Actores**: Productor de Línea o Jefe de Producción (primer usuario del cliente)

**Dependencias**: Ninguna (es el primer requisito funcional del sistema)


### RF-002: Registro de Usuarios Adicionales por Administrador Total
**Descripción**: El sistema debe permitir a usuarios con rol "Administrador Total" registrar nuevos usuarios en la plataforma, asignándoles un rol específico dentro de la jerarquía de producción audiovisual y vinculándolos a uno o más proyectos activos. Este requisito aplica después de que el primer administrador ya existe.

**Prioridad**: Crítica

**Entradas**:
- Nombre (texto, máx 50 caracteres)
- Apellido (texto, máx 50 caracteres)
- Email (formato RFC 5322)
- Teléfono (formato internacional)
- Departamento (Vestuario/Maquillaje/Utilería/Fotografía/Arte/Dirección/Producción)
- Rol (Administrador Total/Continuidad Total/Jefe de Departamento/Asistente de Departamento/Dirección/Talento/Lectura General)
- Proyecto(s) asignado(s) (selección múltiple)

**Proceso**:
- Validar que usuario ejecutor tenga rol "Administrador Total"
- Validar formato de email y unicidad en base de datos
- Validar que campos obligatorios estén completos
- Generar contraseña temporal aleatoria (12 caracteres: 1 mayúscula, 1 minúscula, 1 número, 1 carácter especial)
- Crear registro de usuario con estado "Inactivo"
- Enviar email con credenciales y enlace de activación
- Registrar acción en log de auditoría (quién creó el usuario, cuándo, con qué rol)

**Salidas**:
- Usuario creado en base de datos con estado "Inactivo"
- Email de activación enviado al usuario nuevo
- Mensaje de confirmación al administrador
- Registro en log de auditoría

**Precondiciones**:
- Usuario ejecutor debe tener rol "Administrador Total"
- Usuario ejecutor debe estar autenticado
- Debe existir al menos un proyecto activo en el sistema
- Ya debe existir al menos 1 usuario Administrador Total en el sistema (RF-001 o RF-002 ejecutado)

**Postcondiciones**:

Usuario nuevo queda registrado en estado "Inactivo" hasta activación. Si el usuario no activa cuenta en 48 horas, registro se elimina automáticamente

**Actores**: Administrador Total (Productor de Línea, Jefe de Producción)

**Dependencias**: RF-001 (debe existir al menos un Administrador Total)


### RF-003: Inicio de Sesión con Autenticación de Dos Factores
**Descripción**: El sistema debe permitir a usuarios registrados iniciar sesión validando credenciales (email + contraseña) y requiriendo posteriormente autenticación de dos factores (2FA) mediante código TOTP de 6 dígitos enviado al canal elegido por el usuario.

**Prioridad**: Crítica

**Entradas**:
- Email del usuario
- Contraseña
- Código 2FA de 6 dígitos
- Canal preferido para 2FA (Email)

**Proceso**:
- Validar email y contraseña contra base de datos
- Si las credenciales son incorrectas: registrar intento fallido y mostrar error genérico ("Email o contraseña incorrectos")
- Si 3 intentos fallidos consecutivos: bloquear cuenta temporalmente (15 minutos)
- Enviar código al canal seleccionado por usuario
- Validar código ingresado (válido por 5 minutos)
- Si código es incorrecto 3 veces: bloquear cuenta temporalmente (15 minutos)
- Registrar acceso exitoso en log de auditoría (timestamp, IP origen, dispositivo, resultado)

**Salidas**:
- Redirección a página principal según rol del usuario
- Registro en log de auditoría: timestamp, IP origen, dispositivo, resultado

**Precondiciones**:
- Usuario debe estar registrado en el sistema (RF-001, o RF-002)
- Usuario debe estar en estado "Activo"
- Usuario debe haber activado su cuenta (si fue registrado por RF-002)

**Postcondiciones**:

- Usuario puede acceder a módulos según su rol
- Registro de acceso exitoso en tabla LOG de auditoría

**Actores**: Todos los usuarios registrados del sistema

Dependencias:

RF-001 o RF-002 (Usuario debe estar registrado)


### RF-004: Sistema de Notificaciones por Email
**Descripción**: El sistema debe proveer servicio de envío de notificaciones a usuarios mediante email para eventos críticos (activación de cuenta, código 2FA, recuperación de contraseña, cambios de calendario, comunicados oficiales).

**Prioridad**: Crítica

**Entradas**:
- Destinatario (email o teléfono del usuario)
- Tipo de notificación (Activación/2FA/Recuperación/Cambio Calendario/Comunicado)
- Contenido del mensaje (texto dinámico según tipo)
- Prioridad (Normal/Urgente/Crítica)

**Proceso**:
- Validar destinatario (formato de email)
- Seleccionar plantilla según tipo de notificación
- Generar contenido dinámico (nombre usuario, código, enlace, etc.)
- Enviar mediante correo la notificación.


- Si envío falla: reintentar hasta 3 veces con delay exponencial (1s, 2s, 4s)
- Si 3 intentos fallan: registrar error en log y notificar a administradores
- Registrar envío exitoso en log de notificaciones

**Salidas**:

- Notificación enviada al destinatario
- Timestamp de envío registrado
- Confirmación de entrega (cuando servicio lo provea)
- Registro en log de notificaciones

**Precondiciones**: Usuario destinatario debe tener email o teléfono válido registrado y servicio de terceros, debe estar configurado y con créditos

**Postcondiciones**:

- Notificación entregada en menos de 60 segundos (RNF aplicable)
- Registro de entrega guardado para auditoría

**Actores**: Sistema (envío automático de mails) 

**Dependencias**:

- Integración con proveedor de email.


### RF-005: Sistema de Roles y Permisos Granulares (RBAC)
**Descripción**: El sistema debe implementar control de acceso basado en roles (RBAC) con 4 roles predefinidos, cada uno con permisos granulares específicos sobre los 3 módulos principales (Continuidad Visual, Comunicación, Guiones).

**Prioridad**: Crítica

**Entradas**:
- Rol asignado al usuario (definido en RF-001, RF-002)
- Acción solicitada por usuario (ej: subir fotografía, editar plan de rodaje, ver guion)
- Módulo al que intenta acceder (Continuidad Visual/plan de rodaje/Comunicación/Guiones)

**Proceso**:

- Usuario solicita ejecutar acción en el sistema
- Sistema consulta rol del usuario desde base de datos
- Sistema verifica en matriz de permisos si ese rol tiene permiso para esa acción en ese módulo
- Si permiso existe: permitir acción y ejecutar
- Si permiso NO existe: denegar acción y mostrar mensaje "No tiene permisos para realizar esta acción"
- Registrar intento de acceso (permitido o denegado) en log de auditoría
  
**Salidas**:

- Acción permitida o denegada
- Mensaje de error si acción denegada
- Registro en log de auditoría

**Precondiciones**:

- Usuario debe estar autenticado (RF-003)
- Usuario debe tener rol asignado

**Postcondiciones** : Acción ejecutada si el permiso existe y registro de intento (exitoso o fallido) en log de auditoría

**Actores**: Todos los usuarios del sistema

**Dependencias**:

RF-003 (Usuario autenticado)

Nota Crítica: El sistema debe validar que siempre exista al menos 1 usuario con rol "Administrador Total" activo. No se puede eliminar o cambiar rol del último Administrador Total.

### RF-006: Política de Contraseñas Seguras
**Descripción**: El sistema debe validar y enforcar una política de contraseñas seguras para todos los usuarios en registro inicial, cambio de contraseña y recuperación de contraseña.

**Prioridad**: Alta

**Entradas**:
- Contraseña propuesta por usuario (texto)
- Confirmación de contraseña (texto)
- Contraseñas anteriores del usuario (últimas 3, hasheadas en BD)

**Proceso**:

- Validar longitud mínima: 12 caracteres
- Validar presencia de al menos 1 letra mayúscula (A-Z)
- Validar presencia de al menos 1 letra minúscula (a-z)
- Validar presencia de al menos 1 número (0-9)
- Validar presencia de al menos 1 carácter especial (!@#$%^&*()_+-=[]{}|;:,.<>?)
- Validar que contraseña y confirmación coincidan
- Validar que contraseña NO sea igual a las últimas 3 contraseñas del usuario (comparar hashes)
- Si todas validaciones pasan: hashear con bcrypt (12 rounds) y almacenar
- Si alguna validación falla: mostrar mensaje específico del error

**Salidas**:

- Contraseña hasheada y almacenada en base de datos
- Mensaje de confirmación o error específico al usuario
- Registro en log de cambio de contraseña (no almacena contraseña, solo evento)

**Precondiciones**: Usuario debe estar en proceso de registro (RF-001, RF-002) o cambio de contraseña (RF-007)

**Postcondiciones**:

- Contraseña almacenada de forma segura (hasheada)
- Historial de contraseñas actualizado (últimas 3)

**Actores**: Todos los usuarios del sistema

**Dependencias**:

Ninguna (política aplicable en múltiples RF)


### RF-007: Recuperación de Contraseña
**Descripción**: El sistema debe permitir a usuarios que olvidaron su contraseña solicitar un restablecimiento mediante enlace de recuperación enviado a su email registrado.

**Prioridad**: Alta

**Entradas**:
- Email del usuario registrado
- Nueva contraseña (en formulario tras clic en enlace)
- Confirmación de nueva contraseña

**Proceso**:
- Validar que email exista en base de datos
- Enviar email con enlace de recuperación.
- Usuario hace clic en enlace
- Usuario ingresa nueva contraseña (2 veces para confirmación)
- Aplicar validaciones de RF-006 (Política de contraseñas)
- Validar que nueva contraseña NO sea igual a las últimas 3
- Hashear nueva contraseña con bcrypt (12 rounds)
- Actualizar contraseña en base de datos
- Cerrar todas las sesiones activas del usuario
- Enviar email de confirmación de cambio exitoso

**Salidas**:

- Email con enlace de recuperación enviado
- Contraseña actualizada en base de datos
- Todas las sesiones activas del usuario cerradas
- Registro en log de auditoría

**Precondiciones**:

- Usuario debe estar registrado en el sistema
- Email del usuario debe ser válido y accesible

**Postcondiciones**:

- Contraseña antigua queda invalidada
- Usuario debe iniciar sesión nuevamente con nueva contraseña
- Enlace de recuperación queda invalidado (no reutilizable)

**Actores**: Cualquier usuario registrado que olvidó su contraseña

**Dependencias**:

- RF-004 (Sistema de notificaciones por email)
- RF-006 (Política de contraseñas seguras)


### RF-008: Gestión de Perfil de Usuario
**Descripción**: El sistema debe permitir a usuarios actualizar su información personal (datos de contacto, preferencias de notificación, foto de perfil) excepto datos críticos que solo puede modificar un Administrador Total.

**Prioridad**: Media

**Entradas**:

- Teléfono (opcional, formato internacional)
- Foto de perfil (imagen JPG/PNG, máx 2 MB)
- Preferencias de notificación: Email (sí/no), Push (sí/no)
- Configuración de interfaz: Modo oscuro (sí/no)

**Proceso**:

- Validar que usuario esté autenticado.
- Mostrar formulario con datos actuales pre-cargados
- Usuario modifica campos editables
- Validar formato de teléfono si se proporciona
- Validar formato (JPG/PNG) y tamaño (<2 MB) de foto si se sube
- Comprimir foto a resolución óptima (500x500px) manteniendo calidad
- Actualizar solo campos modificados en base de datos

**Salidas**:

- Información de perfil actualizada en base de datos
- Foto de perfil almacenada en storage (S3 o equivalente)
- Mensaje de confirmación al usuario

**Precondiciones**: Usuario debe estar autenticado (RF-003) y el usuario debe tener sesión activa

**Postcondiciones**:

- Cambios reflejados inmediatamente en interfaz
- Cambios sincronizados automáticamente en todos los dispositivos del usuario

**Actores**: Todos los usuarios registrados

**Dependencias**: RF-003 (Usuario autenticado)

Campos NO Editables por Usuario:

- Email (solo Administrador Total puede cambiar)
- Nombre y Apellido (solo Administrador Total puede cambiar)
- Departamento (solo Administrador Total puede cambiar)
- Rol (solo Administrador Total puede cambiar)
- Proyectos asignados (solo Administrador Total puede cambiar)


### RF-009: Log de Auditoría de Accesos y Acciones
**Descripción**: El sistema debe registrar automáticamente en una tabla LOG todos los eventos de seguridad y acciones críticas realizadas por usuarios para garantizar trazabilidad completa.

**Prioridad**: Alta

**Entradas (capturadas automáticamente por el sistema)**:

- Usuario que ejecuta acción (ID y email)
- Tipo de acción (Login exitoso/fallido, Visualización de fotografía, Visualización de guion, Modificación de calendario, Exportación de reporte, Cambio de permisos, Revocación de acceso)
- Timestamp (fecha y hora exacta con zona horaria)
- IP de origen
- Dispositivo (tipo, modelo, sistema operativo, navegador)
- Resultado (Éxito/Fallo)
- Detalles adicionales (ej: qué fotografía visualizó, qué evento del calendario modificó)

**Proceso**:

- Cada acción crítica del sistema ejecuta trigger que registra evento en tabla LOG
- Sistema captura automáticamente contexto de la acción (usuario, IP, dispositivo)
- Registro se almacena en tabla LOG de base de datos
- Sistema NO permite edición ni eliminación de registros de log (solo inserción)
- Logs se retienen 90 días en sistema activo (acceso inmediato)
- Logs mayores a 90 días se archivan automáticamente (acceso mediante solicitud)
- Logs mayores a 15 meses se purgan automáticamente

**Salidas**:
- Registro insertado en tabla LOG con todos los campos
- Registro inmutable (no puede editarse ni eliminarse)

**Precondiciones**: Usuario debe ejecutar alguna acción en el sistema

**Postcondiciones**:

- Evento queda registrado permanentemente en log

**Actores**:

- Sistema (registro automático)
- Todos los usuarios del sistema (generan eventos)

**Dependencias**: Base de datos PostgreSQL con tabla LOG configurada

Eventos Registrados:

- Intentos de login (exitosos y fallidos)
- Visualización de fotografías de continuidad
- Visualización de guiones
- Subida de fotografías
- Exportación de reportes
- Cambios de roles/permisos
- Revocación de accesos
- Cambios de contraseña


## GESTIÓN DE FOTOGRAFÍAS DE CONTINUIDAD
### RF-010: Carga de Fotografías
**Descripción**: El sistema debe permitir a usuarios autorizados (Script, Jefes de Departamento, Asistentes Onset) cargar fotografías de continuidad desde dispositivos móviles o tablets, completando campos de metadatos obligatorios en el momento de la subida.

**Prioridad**: Crítica

**Entradas**:
- Archivo de fotografía (JPG/PNG máx 10 MB)
- Proyecto (selección de proyectos activos del usuario)
- Episodio (texto, máx 20 caracteres, opcional si no aplica)
- Número de Escena (número entero)
- Número de Toma (número entero)
- Personaje(s) (selección múltiple de personajes del proyecto)
- Tipo de Detalle (selección única): Vestuario / Maquillaje / Utilería / Set / Otro
- Estado de Continuidad (selección única): OK / Pendiente / Error a Corregir
- Comentarios adicionales (texto libre, máx 500 caracteres)

**Proceso**:
- Validar que usuario tenga permiso para subir fotografías (según RF-006)
- Validar formato de archivo (JPG/PNG)
- Validar tamaño de archivo (<10 MB)
- Si archivo >10 MB: comprimir automáticamente a 85% calidad manteniendo resolución
- Generar nomenclatura estandarizada automáticamente: "PROYECTO_EPISODIO_ESCENA_TOMA_PERSONAJE_DETALLE_VERSION"
- Si ya existe fotografía con misma nomenclatura: incrementar VERSION (V1 → V2)
- Aplicar marca de agua dinámica con: nombre usuario, timestamp, nombre
- Almacenar imagen original (sin marca de agua) en storage seguro (S3 o equivalente)
- Registrar acción en log de auditoría (RF-010)
- Si modo offline: almacenar localmente y sincronizar al reconectar

**Salidas**:

- Fotografía almacenada en storage con nomenclatura estandarizada
- Metadatos almacenados en base de datos
- Marca de agua aplicada para visualización
- Mensaje de confirmación al usuario
- Registro en log de auditoría

**Precondiciones**:

- Usuario debe estar autenticado (RF-004)
- Usuario debe tener rol autorizado: Script, Jefe de Departamento, Asistente Onset
- Proyecto debe estar activo
- Personajes deben estar previamente registrados en el proyecto

**Postcondiciones**:

- Fotografía disponible inmediatamente para búsqueda (RF-012)
- Fotografía visible para usuarios autorizados según su rol

**Actores**:

- Script (Continuista)
- Jefe de Departamento (Vestuario, Maquillaje, Utilería, Fotografía, Arte)
- Asistente Onset

**Dependencias**:

- RF-004 (Autenticación)
- RF-006 (Sistema de roles y permisos)
- RF-010 (Log de auditoría)

### RF-011: Generación Automática de Nomenclatura
**Descripción**: El sistema debe generar automáticamente una nomenclatura única y estandarizada para cada fotografía de continuidad siguiendo el formato definido, garantizando consistencia y facilitando búsqueda posterior.

**Prioridad**: Media

**Entradas**:

- Metadatos ingresados en RF-011 (Proyecto, Episodio, Escena, Toma, Personaje, Detalle)
- Fotografías existentes en base de datos para detectar duplicados

**Proceso**: Extraer valores de metadatos:
- *PROYECTO*: nombre del proyecto sin espacios, máx 20 caracteres
- *EPISODIO*: número de episodio con formato EP## (ej: EP01, EP12), "NA" si no aplica
- *ESCENA*: número de escena con formato ESC### (ej: ESC001, ESC045)
- *TOMA*: número de toma con formato T## (ej: T01, T15)
- *PERSONAJE*: nombre del personaje sin espacios, máx 15 caracteres
- *DETALLE*: abreviatura del tipo (VEST, MAQ, UTIL, SET, OTRO)
- *VERSION*: V1, V2, V3... (autoincremental) 
- Generar string: PROYECTO_EPISODIO_ESCENA_TOMA_PERSONAJE_DETALLE_VERSION
- Buscar en base de datos si ya existe nomenclatura idéntica (excluyendo VERSION)
- Si existe: incrementar VERSION automáticamente (V1 → V2 → V3...)
- Si no existe: asignar V1
- Asignar nomenclatura generada como nombre único de archivo

Ejemplo de nomenclatura generada:

    RACCORD_EP01_ESC005_T02_MARIA_VEST_V1.jpg 
    RACCORD_EP01_ESC005_T03_MARIA_VEST_V1.jpg
    RACCORD_EP01_ESC005_T02_MARIA_MAQ_V1.jpg
    RACCORD_EP01_ESC005_T02_MARIA_VEST_V2.jpg (versión actualizada)

**Salidas**:

-Nomenclatura única generada
- Archivo renombrado automáticamente con nomenclatura
- Nomenclatura almacenada en campo "filename" de base de datos

**Precondiciones**: Metadatos obligatorios deben estar completos (validado en RF-011)

**Postcondiciones**:

- Fotografía tiene nombre único y estandarizado
- Nomenclatura permite búsqueda eficiente (RF-013)
- Versiones de una misma fotografía son fácilmente identificables

**Actores**: Sistema (proceso automático)

**Dependencias**: RF-011 (Carga de fotografías)


### RF-012: Búsqueda Avanzada por Filtros
**Descripción**: El sistema debe permitir a usuarios autorizados buscar fotografías de continuidad mediante filtros múltiples (individuales o combinados) con resultados devueltos en menos de 10 segundos (Tiempo que se busca).

**Prioridad**: Crítica

**Entradas**:

- Filtros de búsqueda (opcionales, combinables)**:
- Proyecto (selección única)
- Episodio (selección única o múltiple)
- Número de Escena (número entero o rango)
- Número de Toma (número entero o rango)
- Personaje(s) (selección múltiple)
- Fecha de Rodaje (fecha única o rango)
- Departamento (Vestuario/Maquillaje/Utilería/Set)
- Estado de Continuidad (OK/Pendiente/Error)
- Búsqueda libre en comentarios (texto)


**Proceso**:

- Validar que usuario tenga permiso para buscar fotografías según su rol (RF-006)
- Usuarios con rol Talento: aplicar filtro automático "solo escenas donde están involucrados"
- Construir query SQL con filtros seleccionados (AND entre filtros)
- Ejecutar búsqueda en base de datos PostgreSQL con índices optimizados
- Ordenar resultados por defecto: Escena (ASC) → Toma (ASC) → Personaje (ASC)
- Devolver resultados en vista de galería con miniaturas (thumbnails 300x300px)
- Mostrar metadatos básicos en hover: Nomenclatura, Personaje, Estado
- Permitir cambio de vista: Galería / Lista / Comparación
- Implementar paginación (50 resultados por página)
- Si búsqueda tarda >10 segundos: mostrar indicador de progreso y sugerencia de refinar filtros

**Salidas**:

- Lista de fotografías que coinciden con filtros
- Miniaturas (thumbnails) de fotografías
- Metadatos básicos visibles por fotografía
- Contador de resultados encontrados
- Tiempo de búsqueda (para monitoreo de performance)

**Precondiciones**:

- Usuario debe estar autenticado (RF-004)
- Usuario debe tener permiso de búsqueda según su rol
- Debe existir al menos 1 fotografía en la base de datos

**Postcondiciones**:
- Resultados mostrados en menos de 10 segundos (Objetivo Específico 1)
- Usuario puede seleccionar fotografías para visualización.

**Actores**: Todos los usuarios autorizados (excepto Lectura General que no tiene acceso a fotografías)

**Dependencias**:

- RF-004 (Autenticación)
- RF-006 (Sistema de roles - Talento ve solo sus escenas)
- RF-011 (Fotografías deben estar cargadas)


### RF-013: Comparación Visual Lado a Lado
**Descripción**: El sistema debe permitir comparar hasta 2 fotografías simultáneamente en vista lado a lado con zoom sincronizado para detectar inconsistencias visuales entre tomas de una misma escena.

**Prioridad**: Alta

**Entradas**:
- 2, 3 o 4 fotografías seleccionadas desde RF-013 (Búsqueda)
- Acciones del usuario: Zoom sincronizado, Intercambio de posiciones

**Proceso**:

- Validar que usuario tenga permiso para comparar fotografías
- Cargar fotografías seleccionadas en vista lado a lado
- Mostrar todas las imágenes con mismo tamaño de visualización
- Aplicar marca de agua dinámica a todas las imágenes
- Implementar zoom sincronizado: si usuario hace zoom en una, se replica automáticamente en todas
- Permitir intercambiar posiciones de imágenes arrastrando
- Mostrar metadatos debajo de cada imagen: Nomenclatura, Escena, Toma, Personaje, Estado
- Permitir exportar comparación como PDF (solo roles autorizados: Script, Jefes de Departamento)
- Si se exporta: aplicar marca de agua reforzada en múltiples puntos

**Salidas**:

- Vista comparativa de 2-4 fotografías lado a lado
- Zoom sincronizado entre todas las imágenes
- PDF de comparación (si usuario exporta y tiene permiso)
- Registro de comparación en log de auditoría

**Precondiciones**:

- Usuario debe estar autenticado (RF-004)
- Usuario debe tener permiso según su rol
- Deben existir al menos 2 fotografías seleccionadas

**Postcondiciones**:

- Usuario puede identificar visualmente inconsistencias entre tomas
- Exportación de comparación queda registrada en auditoría

**Actores**: Script, Jefes de Departamento, Director de Fotografía, Director

**Dependencias**:

- RF-004 (Autenticación)
- RF-006 (Sistema de roles)
- RF-013 (Búsqueda de fotografías)

### RF-014: Gestión de Estados de Continuidad
**Descripción**: El sistema debe permitir al Script marcar cada fotografía con uno de tres estados de continuidad (OK, Pendiente, Corregir) y enviar notificaciones automáticas al departamento correspondiente cuando se marca un error.

**Prioridad**: Alta

**Entradas**:

- Fotografía seleccionada
- Estado a asignar: OK / Pendiente / Error a Corregir
- Descripción del error (texto, máx 300 caracteres, obligatorio si Estado = Error)

**Proceso**:

- Validar que usuario sea Script (solo Script puede cambiar estados)
- Mostrar dropdown con 3 estados disponibles
- Si usuario selecciona "Error a Corregir":
- Mostrar campo obligatorio "Descripción del error"

        Identificar departamento correspondiente según Tipo de Detalle de la fotografía:

            Si Detalle = Vestuario → notificar a Jefe de Vestuario
            Si Detalle = Maquillaje → notificar a Jefe de Maquillaje
            Si Detalle = Utilería → notificar a Jefe de Utilería
            Si Detalle = Set → notificar a Jefe de Arte

        Enviar notificación push inmediata al Jefe de Departamento con:

            Thumbnail de la fotografía
            Descripción del error
            Enlace directo a la fotografía
            Prioridad: Urgente

- Actualizar estado de fotografía en base de datos
- Registrar cambio de estado en log de auditoría
- Si estado cambia de "Error" a "OK": enviar notificación de confirmación al departamento

**Salidas**:

- Estado de fotografía actualizado en base de datos
- Notificación push enviada a Jefe de Departamento (si aplica)
- Registro en log de auditoría

**Precondiciones**:

- Usuario debe ser Script
- Fotografía debe existir en sistema

**Postcondiciones**: Estado visible en búsqueda y visualización de fotografía


**Actores**: Script (Continuista) - único autorizado para cambiar estados

**Dependencias**:

- RF-010 (Fotografías deben existir)
- RF-009 (Log de auditoría)


### RF-015: Marca de Agua Dinámica No Removible
**Descripción**: El sistema debe aplicar marca de agua dinámica en tiempo real (superpuesta, NO incrustada en archivo original) a todas las fotografías y guiones descargados para proteger contenido confidencial y garantizar trazabilidad.

**Prioridad**: Crítica

**Entradas**:
- Fotografía o guion a visualizar
- Usuario que visualiza (capturado automáticamente)
- Timestamp de visualización (capturado automáticamente)

**Proceso**:

- Al cargar imagen o documento en visor:

        Aplicar capa superpuesta (overlay) con marca de agua en tiempo real
        NO modificar archivo original almacenado


- Marca de agua debe incluir:

        Nombre completo del usuario que visualiza
        Nombre del proyecto


- Características de la marca:

        Semi-transparente (opacidad 30-40%)
        Color: blanco con borde negro (legible sobre cualquier fondo)
        Fuente: Sans-serif, tamaño 14-16px
        Posición: aleatoria cada vez (no siempre en mismo lugar para dificultar eliminación)
        Se repite múltiples veces en diagonal sobre la imagen


- En aplicaciones móviles (iOS/Android): deshabilitar capturas de pantalla.
- En aplicación web: deshabilitar click derecho y atajos de teclado (Ctrl+S, Ctrl+P, PrtScr)
- Si usuario intenta exportar (solo roles autorizados): aplicar marca de agua.

**Salidas**:

- Imagen/documento con marca de agua visible
- Archivo original sin marca permanece intacto en storage
- Registro de visualización en log de auditoría (RF-009)

**Precondiciones**:

- Usuario debe estar autenticado
- Contenido debe estar almacenado en sistema

**Postcondiciones**:

- Contenido visualizado tiene trazabilidad completa (quién lo vio, cuándo)
- Capturas de pantalla bloqueadas en móviles
- Archivo original protegido de modificación

**Actores**: Todos los usuarios que visualizan contenido

**Dependencias**:

- RF-013 (Visualización de fotografías)
- Librería de procesamiento de imágenes (Pillow para Python)


## RF-016: Análisis Automático de Imágenes con IA
**Descripción**: El sistema debe ejecutar análisis automático de fotografías mediante inteligencia artificial al momento de subir (procesamiento en background) detectando elementos clave de continuidad con precisión mínima del 80%.

**Prioridad**: Media
**Entradas**:

- Fotografía recién subida (desde RF-011)
- Modelo de IA pre-entrenado.

**Proceso**:

- Al subir fotografía exitosamente: encolar tarea de análisis en Celery/Redis
- Procesamiento en background (no bloquea subida):

        Cargar imagen desde storage
        Pre-procesamiento: normalización, redimensionamiento a 640x640px
        Ejecutar modelo YOLOv8 para detección de objetos

        Detectar y etiquetar automáticamente:
            Tipo de prenda: camisa, pantalón, vestido, chaqueta, falda, etc.
            Accesorios: joyas, gafas, relojes, sombreros, bufandas, etc.
            Elementos de utilería: vasos, platos, armas, libros, teléfonos, etc.
            Número de personas en escena

        Generar lista de etiquetas con nivel de confianza (%)
        Filtrar resultados con confianza <50% (demasiado incierto)


- Almacenar etiquetas sugeridas en base de datos como "Sugerencias IA"
- Notificar a Script que análisis está completo
- Mostrar etiquetas sugeridas en visualización de fotografía
- Script puede: Aceptar sugerencia (se convierte en etiqueta oficial), Modificar, Descartar
- Registrar precisión del modelo para mejora continua (almacenar aciertos/fallos)

**Salidas**:

- Lista de etiquetas sugeridas por IA con nivel de confianza
- Notificación a Script cuando análisis completo
- Registro de precisión del modelo para reentrenamiento futuro

**Precondiciones**:

- Fotografía debe estar almacenada en sistema (RF-010)
- Modelo de AI debe estar desplegado y accesible


**Postcondiciones**:

- Etiquetas sugeridas disponibles para Script
- Análisis NO retrasa subida de fotografía (background)
- Precisión objetivo: ≥80% 

**Actores**:

- Sistema (análisis automático)
- Script (valida sugerencias)

**Dependencias**:

- RF-010 (Carga de fotografías)


### RF-017: Detección Automática de Inconsistencias entre Tomas
**Descripción**: El sistema debe comparar automáticamente fotografías de múltiples tomas de una misma escena y generar alertas si detecta diferencias significativas (>20% de cambio) en elementos clave.

**Prioridad**: Media

**Entradas**:
- Fotografías de una misma escena (mismo Proyecto_Episodio_Escena, diferente Toma)
Umbral de diferencia: 20% (configurable por administrador)

**Proceso**:

- Cuando se suben 2 o más fotografías de la misma escena:

        Encolar tarea de comparación en background
        Cargar fotografías desde storage

        Aplicar técnicas de comparación visual

        Comparar listas de objetos detectados

- Si diferencia detectada >20%:

        Generar alerta con:

            Descripción del problema: "Posible inconsistencia detectada: Accesorio (gafas) presente en Toma 1 no visible en Toma 3"
            Thumbnails de ambas fotografías involucradas
            Nivel de confianza (porcentaje)
            Tipo de inconsistencia: Objeto ausente/presente, Cambio de color, Cambio de posición

        Enviar notificación a Script


- Script puede: Validar (marcar como OK, es intencional), Descartar (falso positivo), Marcar como "Error a Corregir"
- Si se marca "Error a Corregir": ejecutar RF-014 (notificar a departamento)

**Salidas**:

- Alertas de inconsistencias detectadas
- Notificación a Script
- Registro de alertas validadas/descartadas para mejora del modelo

**Precondiciones**:

- Deben existir al menos 2 fotografías de la misma escena
- Modelo de IA debe estar funcional

**Postcondiciones**:

- Script recibe alertas de posibles errores antes de edición
- Precisión objetivo: ≥75% (menos exigente que etiquetado porque son alertas)

**Actores**: Script (valida alertas)

**Dependencias**:

- RF-010 (Fotografías cargadas)
- RF-016 (Análisis de IA)
- Librerías: OpenCV, imagehash, scikit-image


### RF-018: Eliminación Lógica de Fotografías (Papelera)
**Descripción**: El sistema debe permitir al Script eliminar fotografías, pero en lugar de borrarlas permanentemente, moverlas a una papelera de reciclaje durante 30 días antes de eliminación definitiva.

**Prioridad**: Media
**Entradas**:

- Fotografía seleccionada
- Acción: Eliminar

**Proceso**:

- Validar que usuario sea Script (único autorizado para eliminar)
- Mostrar confirmación: "¿Está seguro de eliminar esta fotografía? Se moverá a papelera por 30 días"
        Si confirma:

            NO eliminar archivo de storage
            Marcar registro en base de datos con estado "Eliminado" y timestamp de eliminación
            Mover fotografía a sección "Papelera" visible solo para Script y Administrador Total
            Configurar eliminación automática definitiva en 30 días
            Registrar eliminación en log de auditoría

- Script y Administrador Total pueden:

        Ver papelera con fotografías eliminadas
        Restaurar fotografía

- Tras 30 días: proceso automático elimina permanentemente fotografía de storage y base de datos

**Salidas**:

- Fotografía movida a papelera (estado "Eliminado")
- Fotografía excluida de búsquedas normales
- Registro de eliminación en log de auditoría
- Eliminación definitiva tras 30 días

**Precondiciones**:

- Usuario debe ser Script o Administrador Total
- Fotografía debe existir y estar en estado "Activo"

**Postcondiciones**:

- Fotografía recuperable durante 30 días
-Tras 30 días: eliminación permanente e irreversible

**Actores**:

* Script (elimina)
* Administrador Total (puede restaurar o eliminar definitivamente)

**Dependencias**:

- RF-010 (Fotografías deben existir)
- RF-009 (Log de auditoría)
- Cron job para eliminación automática tras 30 días

## GESTIÓN DE CALENDARIO Y PLANEACIÓN
### RF-019: Creación de Proyectos
**Descripción**: El sistema debe permitir a Administradores Totales crear proyectos nuevos con información básica que servirá como contenedor para todas las fotografías, guiones y desglose y plan de rodaje de esa producción específica.

**Prioridad**: Crítica

**Entradas**:

- Nombre del proyecto (texto, máx 100 caracteres)
- Tipo de producción (Película/Serie/Cortometraje/Comercial/Documental/Video Institucional)
- Fecha de inicio estimada (fecha)
- Fecha de finalización estimada (fecha)
- Descripción breve (texto, máx 500 caracteres, opcional)

**Proceso**:

- Validar que usuario sea Administrador Total
- Validar que nombre de proyecto no esté duplicado
- Crear registro de proyecto en base de datos con estado "Activo"
- Generar ID único de proyecto
- Crear automáticamente estructuras base:

- Asignar creador como Administrador Total del proyecto
- Registrar creación en log de auditoría

**Salidas**:

- Proyecto creado en base de datos
- Estructura de directorios en storage
- Calendario inicializado
- Registro en log de auditoría

**Precondiciones**:

- Usuario debe ser Administrador Total
- Usuario debe estar autenticado

**Postcondiciones**:

- Proyecto disponible para asignación a usuarios (RF-003)
- Proyecto visible en selector de proyectos de todos los módulos

**Actores**: Administrador Total (Productor de Línea)

**Dependencias**: RF-001 o RF-002 (Administrador Total debe existir)


### RF-024: Creación de Eventos en Calendario de Rodaje
**Descripción**: El sistema debe permitir a la Primera Ayudante de Dirección crear eventos en calendario centralizado con 12 campos obligatorios que definen cada jornada de rodaje.

**Prioridad**: Crítica

**Entradas**:

- Fecha de rodaje (fecha única)
- Escenas a grabar (selección múltiple de escenas del proyecto)
- Locación (texto, máx 200 caracteres)
- Personajes presentes (selección múltiple de personajes del proyecto)
- Hora ficticia (selección única): Día / Tarde / Noche / Amanecer / Atardecer
- Páginas de guion (texto, ej: "15-23")
- Equipo técnico requerido (texto libre, máx 300 caracteres)
- Vestuario específico por personaje (texto libre, máx 500 caracteres)
- Maquillaje especial requerido (texto libre, máx 300 caracteres)
- Utilería clave (objetos hero) (texto libre, máx 300 caracteres)
- Notas de dirección (texto libre, máx 500 caracteres)
- Estado (selección única): Confirmado / Tentativo / Cancelado

**Proceso**:

- Validar que usuario sea Primera Ayudante de Dirección o Administrador Total
- Validar que todos los 12 campos obligatorios estén completos
- Validar que fecha no esté en el pasado (advertencia, no bloqueo)
- Crear evento en base de datos vinculado al proyecto
- Si escenas seleccionadas tienen desgloses (RF-026): vincular automáticamente información del desglose
- Calcular automáticamente usuarios afectados:

        Todos los usuarios cuyos personajes están involucrados
        Jefes de departamentos mencionados (Vestuario, Maquillaje, Utilería)
        Script siempre incluido

- Evento visible inmediatamente en calendario (vistas diaria/semanal/mensual)
- Enviar notificaciones push a usuarios afectados
- Registrar creación en log de auditoría

**Salidas**:
- Evento creado en calendario
- Notificaciones push enviadas a usuarios afectados
- Registro en log de auditoría

**Precondiciones**:
- Usuario debe ser Primera AD o Administrador Total
- Proyecto debe estar activo
- Escenas y personajes deben estar previamente registrados

**Postcondiciones**:
- Evento visible en calendario para usuarios autorizados
- Usuarios afectados reciben notificación instantánea

**Actores**:
- Primera Ayudante de Dirección
- Administrador Total (también puede crear eventos)

**Dependencias**:
- RF-023 (Proyecto debe existir)
- RF-010 (Log de auditoría)


### RF-025: Modificación de Eventos de Calendario
**Descripción**: El sistema debe permitir a Primera AD y Administrador Total modificar eventos existentes en el calendario, registrando qué cambió y notificando automáticamente a usuarios afectados.

**Prioridad**: Crítica

**Entradas**:

- Evento seleccionado
- Campos a modificar (cualquiera de los 12 campos de RF-024)
- Prioridad del cambio (Normal / Urgente / Crítico)

Proceso:

- Validar que usuario sea Primera AD o Administrador Total
- Mostrar formulario con datos actuales pre-cargados
- Usuario modifica campos necesarios
- Sistema detecta automáticamente qué campos cambiaron (comparación pre/post)
- Si cambio es en: Fecha, Escenas, Locación, Personajes → considerar cambio crítico automáticamente
- Usuario puede marcar cambio como "Urgente" o "Crítico" manualmente
- Actualizar evento en base de datos
- Registrar cambio en tabla de historial de calendario:

        Qué campos cambiaron
        Valor anterior vs valor nuevo
        Quién modificó
        Timestamp


- Recalcular usuarios afectados (pueden cambiar si se añaden/quitan personajes)
- Enviar notificaciones push (RF-028) con nivel de prioridad:

        Normal: notificación estándar
        Urgente: notificación con color naranja y sonido distintivo
        Crítico: notificación con color rojo, sonido de alerta y vibración

**Salidas**:

- Evento actualizado en calendario
- Historial de cambios registrado
- Notificaciones push enviadas según prioridad
- Registro en log de auditoría

**Precondiciones**:
- Usuario debe ser Primera AD o Administrador Total
- Evento debe existir en calendario

**Postcondiciones**:
- Cambios visibles inmediatamente en calendario
- Usuarios afectados notificados en <30 segundos (Objetivo Específico 3)

**Actores**:
- Primera Ayudante de Dirección
- Administrador Total

-**Dependencias**:
- RF-024 (Evento debe existir)
- RF-010 (Log de auditoría)


### RF-026: Gestión de Desgloses de Escenas
**Descripción**: El sistema debe permitir a Primera AD y Script crear y modificar desgloses detallados por escena individual con información técnica y de producción que se vincula automáticamente a eventos del calendario.

**Prioridad**: Alta
**Entradas**:

- Número de escena (número entero, único por proyecto)
- Descripción narrativa breve (texto, máx 500 caracteres)
- Interior / Exterior (selección única)
- Día / Noche (selección única)
- Personajes con diálogo (selección múltiple)
- Personajes secundarios / Extras (texto libre, máx 300 caracteres)
- Vestuario requerido por personaje con estado (tabla):

        Personaje
        Descripción de vestuario
        Estado requerido: Limpio / Sucio / Roto / Mojado / Otro
- Maquillaje especial requerido (texto libre, máx 300 caracteres)
- Utilería hero (objetos clave en acción) (texto libre, máx 300 caracteres)
- Vehículos (texto libre, máx 200 caracteres, opcional)
- Efectos especiales (texto libre, máx 300 caracteres, opcional)
- Armas / Elementos regulados (texto libre, máx 200 caracteres, opcional)
-Notas de producción (texto libre, máx 500 caracteres)

**Proceso**:
- Validar que usuario sea Primera AD o Script
- Validar que número de escena no esté duplicado en el proyecto
- Crear registro de desglose en base de datos vinculado al proyecto
- Si escena ya está asignada a evento de calendario (RF-024): actualizar evento automáticamente con información del desglose
- Permitir edición posterior del desglose (solo Primera AD y Script)
- Vincular automáticamente con fotografías de continuidad (RF-011) de la misma escena mediante número de escena
- Mostrar desglose completo al crear evento de calendario que incluya esa escena
- Registrar creación/modificación en log de auditoría

**Salidas**:
- Desglose de escena creado en base de datos
- Información vinculada a eventos de calendario que incluyan la escena
- Vinculación bidireccional con fotografías de continuidad
- Registro en log de auditoría

**Precondiciones**:
- Usuario debe ser Primera AD o Script
- Proyecto debe estar activo

**Postcondiciones**:
- Desglose disponible para consulta por todos los departamentos
- Información se refleja automáticamente en eventos de calendario

**Actores**:
- Primera Ayudante de Dirección
- Script (Continuista)

**Dependencias**:
- RF-023 (Proyecto debe existir)
- RF-024 (Puede vincularse con eventos de calendario)
- RF-011 (Puede vincularse con fotografías de continuidad)


### RF-027: Notas Personales Privadas en Calendario
**Descripción**: El sistema debe permitir a cualquier usuario crear notas personales privadas asociadas a cualquier día del calendario que sean completamente privadas y no visibles para otros usuarios ni administradores.

**Prioridad**: Baja

**Entradas**:

- Fecha seleccionada en calendario
- Contenido de nota personal (texto libre, máx 1000 caracteres)
- Tipo de nota (opcional): Recordatorio / Tarea pendiente / Observación / Idea

**Proceso**:
- Validar que usuario esté autenticado
- Crear nota en base de datos con:

        ID de usuario (propietario)
        Fecha asociada
        Contenido
        Tipo (opcional)
        Timestamp de creación
        Flag "privada" = true

- Nota se almacena con cifrado adicional (solo usuario puede descifrarla)
- Mostrar icono de candado en calendario solo visible para su creador en la fecha correspondiente
- Permitir editar y eliminar nota (solo el propietario)
- Permitir búsqueda de notas personales por fecha o contenido (solo el propietario)
- Notas NO se incluyen en:

        Reportes de producción (RF-046 a RF-052)
        Auditorías de acceso (RF-010)
        Exportaciones de calendario

**Salidas**:

- Nota personal almacenada cifrada en base de datos
- Icono de candado visible solo para creador en calendario
- Nota excluida de reportes y auditorías

**Precondiciones**:
- Usuario debe estar autenticado
- Fecha debe ser parte del calendario del proyecto activo

**Postcondiciones**:
- Nota accesible solo por su creador
- Privacidad absoluta garantizada (ni administradores pueden ver contenido)

**Actores**: Cualquier usuario del sistema

**Dependencias**:

- RF-004 (Usuario autenticado)
- RF-024 (Calendario debe existir)


### RF-028: Notificaciones Push Instantáneas de Cambios de Calendario
**Descripción**: El sistema debe enviar notificaciones push automáticas en menos de 30 segundos a todos los usuarios afectados cuando se crea, modifica o cancela un evento en el calendario.

**Prioridad**: Crítica

**Entradas (capturadas automáticamente)**:

- Evento de calendario modificado/creado/cancelado (desde RF-024 o RF-025)
- Tipo de cambio: Creación / Modificación / Cancelación
- Campos que cambiaron (en caso de modificación)
- Prioridad asignada: Normal / Urgente / Crítico
- Lista de usuarios afectados (calculada automáticamente)

**Proceso**:
- Cuando se crea/modifica/cancela evento: trigger automático ejecuta envío de notificaciones
- Sistema identifica usuarios afectados:

        Todos los usuarios cuyos personajes están involucrados en las escenas del evento
        Jefes de departamentos mencionados en el evento
        Script siempre incluido
        Primera AD siempre incluida
- Generar contenido de notificación:

        Título: "Cambio en calendario: [Fecha]"
        Descripción: Resumen de qué cambió (ej: "Locación cambió de 'Estudio A' a 'Locación Externa - Parque Nacional'")
        Detalles completos: Todos los campos del evento
        Quién realizó el cambio
        Timestamp del cambio
        Enlace directo al evento en calendario

- Enviar notificación mediante Firebase Cloud Messaging (FCM):

        Push a app móvil (iOS/Android) con alerta sonora y badge
        Si app está abierta: mostrar banner in-app
        Si app está cerrada: notificación en centro de notificaciones del dispositivo


- Aplicar prioridad visual:

        Normal: icono estándar, sonido suave
        Urgente: icono naranja, sonido distintivo, vibración corta
        Crítico: icono rojo, sonido de alerta, vibración larga

- Registrar timestamp de envío de notificación
- Registrar timestamp de lectura cuando usuario abre notificación
- Si usuario tiene preferencia de email activada: enviar también por email (no reemplaza push)
- Si notificación no se entrega en 30 segundos: reintentar hasta 3 veces
- Si 3 reintentos fallan: registrar error y notificar a administradores

**Salidas**:
- Notificación push entregada en <30 segundos
- Registro de envío y lectura en base de datos
- Email enviado (si usuario tiene preferencia activada)
- Registro de errores de entrega (si aplica)

**Precondiciones**:
- Evento debe existir en calendario (RF-024)
- Usuarios afectados deben tener dispositivos registrados para push notifications
- Firebase Cloud Messaging debe estar configurado

**Postcondiciones**:
- Usuarios notificados en menos de 30 segundos (Objetivo Específico 3)
- Confirmaciones de lectura registradas para seguimiento

**Actores**:
- Sistema (envío automático)
- Servicio de terceros (Firebase Cloud Messaging)

**Dependencias**:

- RF-024 (Creación de eventos)
- RF-025 (Modificación de eventos)
- Integración con Firebase Cloud Messaging


### RF-029: Comunicación Interna por Canales Segmentados
**Descripción**: El sistema debe permitir enviar y recibir mensajes mediante canales segmentados (General, por Departamento, por Escena, por Personaje, Mensajes Directos) con adjuntos y priorización.

**Prioridad**: Media

**Entradas**:

- Canal de comunicación (selección única):

        Canal General (todo el equipo)
        Canal por Departamento (Vestuario/Maquillaje/Utilería/Fotografía/Dirección/Producción)
        Canal por Escena (específico de una escena en rodaje)
        Canal por Personaje (todo relacionado a un personaje)
        Mensaje Directo (usuario a usuario)
- Contenido del mensaje (texto, máx 2000 caracteres)
        Adjuntos (opcionales):
            Fotografías desde galería de continuidad (selección desde RF-013)
            Fotografías nuevas (carga directa, máx 5 MB)
            Documentos PDF (máx 10 MB)

- Nivel de prioridad (selección única): Normal / Urgente

**Proceso**:

- Validar que usuario esté autenticado
- Validar que usuario tenga acceso al canal seleccionado según su rol:

Canal General: todos los usuarios
Canal por Departamento: solo usuarios de ese departamento + Script + Dirección + Administrador Total
Canal por Escena: usuarios involucrados en esa escena + Script + Primera AD + Dirección
Canal por Personaje: actores del personaje + Script + Jefes de Departamento + Dirección
Mensaje Directo: emisor y receptor únicamente


- Validar formato y tamaño de adjuntos
- Si adjunto es fotografía de continuidad: vincular referencia (no duplicar archivo)
- Si adjunto es fotografía nueva: almacenar en storage y aplicar marca de agua
- Crear mensaje en base de datos vinculado al canal
- Enviar notificación push a usuarios del canal:

        Normal: notificación estándar sin sonido (solo badge)
        Urgente: notificación con color rojo, sonido y badge
- Mostrar mensaje inmediatamente en chat del canal para usuarios online
- Permitir búsqueda histórica de mensajes por:

        Canal
        Palabra clave en contenido
        Usuario emisor
        Rango de fechas
- Mostrar confirmación de lectura (checkmarks):

        1 check: mensaje enviado
        2 checks: mensaje leído por todos los destinatarios

**Salidas**:

- Mensaje creado en base de datos
- Mensaje visible en canal correspondiente
- Notificaciones push enviadas a usuarios del canal
- Adjuntos almacenados o vinculados
- Confirmaciones de lectura visibles para emisor

**Precondiciones**:

- Usuario debe estar autenticado
- Usuario debe tener acceso al canal según su rol
- Canal debe existir (canales por Escena/Personaje se crean automáticamente)

**Postcondiciones**:

- Mensaje disponible para búsqueda histórica
- Usuarios del canal reciben notificación si están offline

**Actores**: Todos los usuarios del sistema (según permisos de canal)

**Dependencias**:

- RF-004 (Usuario autenticado)
- RF-006 (Sistema de roles)
- RF-028 (Notificaciones push)
- RF-013 (Adjuntar fotografías desde galería)

### RF-030: Comunicados Oficiales con Confirmación de Lectura Obligatoria
**Descripción**:El sistema debe permitir a Primera AD, Director y Productor de Línea enviar comunicados oficiales a todo el equipo o grupos específicos, requiriendo confirmación de lectura obligatoria y generando reporte de quiénes han leído.

**Prioridad**: Media

**Entradas**:

- Destinatarios (selección única):

        Todo el equipo
        Todos los departamentos (excluye Talento)
        Solo departamentos creativos (Vestuario, Maquillaje, Utilería, Fotografía, Arte)
        Solo producción (Producción, Coordinadores)
- Asunto del comunicado (texto, máx 150 caracteres)
- Contenido del comunicado (texto enriquecido, máx 5000 caracteres)
- Documentos adjuntos (opcionales, PDF, máx 20 MB total)
- Requiere confirmación de lectura: Sí (por defecto) / No

**Proceso**:
- Validar que usuario sea Primera AD, Director o Productor de Línea
- Mostrar formulario de comunicado con editor de texto enriquecido (negrita, cursiva, listas, enlaces)
- Validar asunto y contenido completos
- Validar formato y tamaño de adjuntos
- Crear comunicado en base de datos con:

        Estado: Activo
        Timestamp de publicación
        Autor
        Destinatarios
- Publicar comunicado en Canal General con:

        Icono distintivo (megáfono o estrella)
        Fijado en parte superior del Canal General (sticky)
        Destacado visualmente (fondo color diferente)
- Enviar notificación push CRÍTICA a todos los destinatarios (sonido de alerta, vibración)
- Si requiere confirmación de lectura:

        Mostrar checkbox "He leído este comunicado" al final del mensaje
        Usuario debe marcar checkbox para confirmar lectura
        Sistema registra timestamp de confirmación
        Comunicado permanece fijado hasta que usuario confirme lectura
- Generar reporte automático para autor del comunicado:

        Lista de usuarios que confirmaron lectura (con timestamp)
        Lista de usuarios que NO han confirmado lectura
        Porcentaje de confirmaciones (ej: 45/50 usuarios - 90%)
- Permitir archivar comunicado (deja de estar fijado, pasa a historial)

**Salidas**:
- Comunicado publicado en Canal General
- Comunicado fijado en parte superior hasta confirmación de lectura
- Notificaciones push críticas enviadas
- Reporte de confirmaciones de lectura disponible para autor
- Registro en log de auditoría

**Precondiciones**: Usuario debe ser Primera AD, Director o Productor de Línea y el proyecto debe estar activo

**Postcondiciones**:
- Comunicado visible para todos los destinatarios
- Autor puede monitorear quiénes han leído
- Comunicado archivado queda en historial consultable

**Actores**:
- Primera Ayudante de Dirección
- Director
- Productor de Línea (Administrador Total)

**Dependencias**:

- RF-029 (Canal General debe existir)
- RF-028 (Notificaciones push)
- RF-010 (Log de auditoría)


### RF-031: Directorio de Equipo (Crew List)
**Descripción**:El sistema debe mantener un directorio completo del equipo de producción con información de contacto, consultable por todos los usuarios y editable solo por Coordinadores de Departamento y Productor de Línea.

**Prioridad**: Baja 

**Entradas**:
- Nombre completo (texto, máx 100 caracteres)
- Cargo (texto, máx 100 caracteres, ej: "Gaffer", "Key Grip", "Script Supervisor")
- Departamento (selección única): Vestuario/Maquillaje/Utilería/Fotografía/Arte/Dirección/Producción/Sonido/Edición
- Rol en el sistema (si está registrado): vinculación con RF-003
- Teléfono de contacto (formato internacional)
- Email (formato RFC 5322)
- Notas adicionales (texto, máx 300 caracteres, opcional)

**Proceso**:

- Validar que usuario sea Coordinador de Departamento, Primera AD o Administrador Total
- Permitir añadir miembros del equipo al directorio
- Si miembro ya está registrado en sistema (RF-003): vincular automáticamente y pre-completar datos
- Si miembro NO está registrado: permitir añadir solo datos de contacto (no tiene acceso al sistema)
- Organizar directorio por departamento
- Permitir búsqueda por: nombre, cargo, departamento, teléfono
- Todos los usuarios pueden consultar directorio completo
- Solo Coordinadores y Administrador Total pueden: añadir, editar, eliminar miembros
- Exportar directorio en formato Excel o PDF (todos los usuarios)

**Salidas**:
- Directorio completo del equipo consultable
- Búsqueda rápida de contactos
- Exportación en Excel/PDF

**Precondiciones**: Usuario debe estar autenticado y proyecto debe estar activo

**Postcondiciones**:

- Equipo completo visible para coordinación
- Contactos accesibles rápidamente en rodaje

**Actores**:

- Todos los usuarios (consulta)
- Coordinadores de Departamento, Primera AD, Administrador Total (edición)

**Dependencias**:

- RF-003 (Puede vincularse con usuarios registrados)
- RF-004 (Usuario autenticado)


## GESTIÓN DE GUIONES
### RF-032: Subida y Control de Versiones de Guion
**Descripción**: El sistema debe permitir a Primera AD y Script subir versiones del guion en formato PDF con control de versiones, historial completo y restricción de visibilidad según rol de usuario.
**Prioridad**: Alta

**Entradas**:
- Archivo de guion (formato PDF, máx 50 MB)
- Número de versión (texto, ej: v1.0, v1.1, v2.0)
- Fecha de emisión (fecha)
- Descripción de cambios principales (texto, máx 1000 caracteres)
- Estado (selección única): Borrador / Revisión / Aprobado / En Rodaje

**Proceso**:

- Validar que usuario sea Primera AD, Script O Director
- Validar formato de archivo (solo PDF)
- Validar tamaño de archivo (<50 MB)
- Si ya existe guion con mismo número de versión: rechazar y solicitar número diferente
- Almacenar PDF en storage seguro (S3 o equivalente) con cifrado
- Crear registro en base de datos con metadatos del guion
- Si estado = "En Rodaje":

        Marcar automáticamente versión anterior como "No visible para Talento"
        Esta versión se convierte en la visible para Talento
        Enviar notificación a todo el equipo de nueva versión disponible
- Mantener historial completo de versiones en orden cronológico
- Permitir descargar versiones anteriores (solo roles autorizados: Script, Primera AD, Dirección, Administrador Total)
- Usuarios con rol Talento solo pueden ver versión marcada como "En Rodaje"
- Registrar subida en log de auditoría

**Salidas**:

- Guion almacenado en storage con cifrado
- Metadatos del guion en base de datos
- Notificación enviada a equipo (si estado = En Rodaje)
- Historial de versiones actualizado
- Registro en log de auditoría

**Precondiciones**: Usuario debe ser Primera AD o Script y proyecto debe estar activo

**Postcondiciones**:

- Guion disponible para visualización según permisos de rol
- Versión "En Rodaje" es la única visible para Talento
- Historial completo accesible para roles autorizados

**Actores**:
- Primera Ayudante de Dirección (sube guiones)
- Script (sube guiones)
- Director (sube guiones)

**Dependencias**:

- RF-023 (Proyecto debe existir)
- RF-006 (Sistema de roles controla visibilidad)
- RF-010 (Log de auditoría)


### RF-033: Visualización de Guion con Marca de Agua y Protección Avanzada
**Descripción**:El sistema debe permitir visualizar guiones en formato PDF dentro de la aplicación con marca de agua dinámica, bloqueo de capturas de pantalla y protección contra descargas no autorizadas.

**Prioridad**: Crítica

**Entradas**:

- Guion seleccionado desde lista de versiones
- Usuario que visualiza (capturado automáticamente)

**Proceso**:

- Validar que usuario tenga permiso para ver guion según su rol:

        Talento: solo versión "En Rodaje"
        Otros roles: todas las versiones


- Cargar PDF desde storage
- Renderizar PDF en visor integrado dentro de la app (NO descarga directa)
- Aplicar marca de agua dinámica en tiempo real superpuesta en cada página:

        Nombre completo del usuario
        Timestamp de visualización
        Nombre del proyecto
        Posición: diagonal, semi-transparente, múltiples repeticiones por página


- En aplicaciones móviles (iOS/Android):

        Deshabilitar capturas de pantalla a nivel de sistema operativo
        Si usuario intenta captura: pantalla se pone negra automáticamente
        Detectar intentos de grabación de pantalla externa: cerrar visor automáticamente


- En aplicación web:

        Deshabilitar click derecho (menú contextual)
        Deshabilitar atajos de teclado: Ctrl+S, Ctrl+P, PrtScr, Ctrl+C
        Usar JavaScript para detectar extensiones de captura y bloquearlas


- Permitir navegación por páginas del guion (anterior/siguiente, ir a página específica)
- Permitir zoom para lectura
- Registrar visualización en log de auditoría (RF-010): quién vio qué guion, cuándo, por cuánto tiempo

**Salidas**:

- Guion visualizado con marca de agua en cada página
- Capturas de pantalla bloqueadas
- Registro de visualización en log de auditoría

**Precondiciones**:
- Usuario debe estar autenticado
- Usuario debe tener permiso para ver guion según su rol
- Guion debe estar subido al sistema (RF-032)

**Postcondiciones**:

- Contenido protegido contra capturas y descargas
- Visualización registrada en auditoría para trazabilidad
- Marca de agua identifica al usuario en caso de filtración

**Actores**: Todos los usuarios autorizados según su rol

**Dependencias**:
- RF-004 (Usuario autenticado)
- RF-006 (Sistema de roles)
- RF-018 (Marca de agua dinámica)
- RF-010 (Log de auditoría)


### RF-034: Anotaciones Colaborativas en Guion
**Descripción**: El sistema debe permitir tres tipos de anotaciones en guiones PDF: personales (solo usuario), departamentales (equipo del departamento) y generales (todos los roles), con sincronización entre dispositivos.

**Prioridad**: Alta

**Entradas**:
- Guion abierto en visor (RF-033)
- Página y posición donde se crea anotación
- Tipo de anotación (selección única):

        Nota (comentario de texto)
        Resaltado (color de fondo sobre texto)
        Marcador (bookmark de página)

- Contenido de anotación (texto, máx 500 caracteres, si es nota)
- Nivel de visibilidad (selección única):

        Personal (solo yo)
        Departamental (mi departamento)
        General (todos)

**Proceso**:

- Validar que usuario esté visualizando guion (RF-033)
- Usuario selecciona texto o hace clic en página para crear anotación
- Mostrar menú contextual con opciones: Nota / Resaltado / Marcador
- Si elige Nota o Resaltado: solicitar contenido/color
- Solicitar nivel de visibilidad:

        Personal: siempre disponible para cualquier usuario
        Departamental: solo Jefes de Departamento pueden crear anotaciones departamentales
        General: solo Script puede crear anotaciones generales
- Crear anotación en base de datos con:

        ID de guion y versión
        Página y coordenadas (posición exacta)
        Tipo de anotación
        Contenido
        Nivel de visibilidad
        Autor
        Timestamp de creación

- Renderizar anotación superpuesta en el visor PDF:

        Personal: icono verde (solo visible para creador)
        Departamental: icono azul (visible para equipo del departamento)
        General: icono rojo (visible para todos)
- Sincronizar anotaciones entre todos los dispositivos del usuario
- Si usuario con rol Talento: solo puede crear anotaciones personales y solo en sus escenas asignadas
- Permitir editar y eliminar anotaciones propias
- Anotaciones de otros usuarios son solo lectura

**Salidas**:
- Anotación creada en base de datos
- Anotación visible en visor según nivel de visibilidad
- Sincronización automática entre dispositivos del usuario
- Notificación a equipo de departamento (si anotación departamental)

**Precondiciones**:
- Usuario debe estar visualizando guion (RF-033)
- Usuario debe tener permiso para crear anotaciones según su rol

**Postcondiciones**:
- Anotación disponible en todas las visualizaciones futuras del guion
- Anotaciones sincronizadas entre dispositivos

**Actores**: Todos los usuarios autorizados (con restricciones según rol)

**Dependencias**:

- RF-033 (Visualización de guion)
- RF-006 (Sistema de roles controla qué anotaciones puede crear cada rol)


### RF-035: Comparación de Versiones de Guion (Diff)
**Descripción**:El sistema debe permitir al Script y Primera AD visualizar diferencias entre dos versiones del guion mediante comparación visual lado a lado con resaltado de colores para texto eliminado, nuevo y modificado.

**Prioridad**: Media

**Entradas**:
- Versión 1 del guion (selección de versión antigua)
- Versión 2 del guion (selección de versión nueva)

**Proceso**:

- Validar que usuario sea Script o Primera AD
- Cargar ambas versiones del guion desde storage
- Extraer texto de ambos PDFs mediante OCR o extracción de texto nativo
- Ejecutar algoritmo diff (diferencias línea por línea):

        Texto eliminado (presente en V1, ausente en V2): resaltar en rojo
        Texto nuevo (ausente en V1, presente en V2): resaltar en verde
        Texto modificado (cambio parcial): resaltar en amarillo
        Texto sin cambios: sin resaltado
- Mostrar comparación en vista lado a lado:

        Columna izquierda: Versión anterior
        Columna derecha: Versión nueva
        Scroll sincronizado entre ambas columnas
- Mostrar índice de cambios:

        Lista de páginas con cambios
        Cantidad de cambios por página
        Navegación rápida a páginas con cambios
- Permitir generar "Reporte de Cambios" en PDF:

        Lista textual de todos los cambios
        Formato: "Página X, Línea Y: [Texto eliminado] → [Texto nuevo]"
        Exportar con marca de agua
- Registrar comparación en log de auditoría

**Salidas**:

- Vista comparativa lado a lado con resaltado de diferencias
- Índice de cambios por página
- Reporte de Cambios en PDF (si usuario lo genera)
- Registro en log de auditoría

**Precondiciones**:

- Usuario debe ser Script o Primera AD
- Deben existir al menos 2 versiones del guion

**Postcondiciones**: Script puede comunicar cambios a departamentos mediante RF-030 (Comunicados) adjuntando Reporte de Cambios

**Actores**:

- Script (Continuista)
- Primera Ayudante de Dirección

**Dependencias**:
- RF-032 (Múltiples versiones de guion deben existir)
- Librería de OCR/extracción de texto: PyPDF2, pdfplumber
- Algoritmo diff: difflib (Python estándar)


### RF-036: Vinculación Automática Guion-Continuidad
**Descripción**: El sistema debe vincular automáticamente cada escena del guion con las fotografías de continuidad correspondientes mediante match por número de escena, con navegación bidireccional.

**Prioridad**: Media

**Entradas**:

- Guion abierto en visor (RF-033)
- Número de escena detectado automáticamente en el PDF o ingresado manualmente

**Proceso**:

- Al visualizar guion: sistema identifica automáticamente números de escena mediante:

        Patrón regex: "INT.", "EXT.", "ESC.", "ESCENA", seguido de número
        Ejemplo: "INT. OFICINA - DÍA" → detecta número de escena
- Si detección automática falla: Script puede etiquetar manualmente escenas en el guion
- Para cada escena detectada, mostrar botón "Ver Continuidad" al lado del número de escena
- Al hacer clic en "Ver Continuidad":

- Abrir vista lateral (split screen)
- Ejecutar búsqueda automática en base de fotografías (RF-013) filtrando por número de escena
- Mostrar galería de todas las fotografías de esa escena organizadas por:

        Personaje
        Tipo de Detalle (Vestuario, Maquillaje, Utilería, Set)
        Toma

- Permitir navegación bidireccional:

        Desde guion → fotografías de continuidad (descrito arriba)
        Desde fotografía → escena del guion: botón "Ver Escena en Guion" en RF-014
- Mantener sincronización: si usuario está en Escena 5 del guion y navega a fotografía, al volver al guion debe regresar a Escena 5

**Salidas**:
- Vista lateral con fotografías de continuidad de la escena
- Navegación bidireccional funcional
- Sincronización de posición entre guion y fotografías

**Precondiciones**:
- Guion debe estar subido (RF-032)
- Fotografías de continuidad deben existir (RF-011)
- Números de escena deben coincidir entre guion y fotografías

**Postcondiciones**: Departamentos pueden revisar simultáneamente qué dice el guion y cómo se ejecutó visualmente

**Actores**: Todos los usuarios que visualizan guiones

**Dependencias**:

- RF-033 (Visualización de guion)
- RF-013 (Búsqueda de fotografías)
- RF-014 (Visualización de fotografías)


### RF-037: Desglose Editable de Guion
**Descripción**: El sistema debe proveer herramienta para que Script y Primera AD realicen desgloses de guion directamente en la plataforma, identificando automáticamente elementos por categoría y permitiendo edición manual.

**Prioridad**: Media

**Entradas**:

- Guion cargado (RF-032)
- Escena específica seleccionada
- Identificación manual de elementos (si automática falla)

**Proceso**:

- Script o Primera AD abre herramienta "Desglosar Escena"
- Sistema intenta identificar automáticamente elementos del guion mediante procesamiento de lenguaje natural (NLP):

        Personajes: detectar nombres propios y diálogos
        Locaciones: detectar encabezados "INT.", "EXT." con nombre de locación
        Objetos/Utilería: detectar sustantivos comunes mencionados en acción
        Vestuario: detectar referencias a ropa (ej: "vestido rojo", "traje azul")
        Vehículos: detectar menciones de vehículos
        Efectos especiales: detectar palabras clave (explosión, lluvia, humo)
- Mostrar elementos detectados organizados por categoría:

        Personajes
        Locaciones
        Utilería/Props
        Vestuario
        Vehículos
        Efectos Especiales
        Armas/Elementos Regulados
- Permitir a Script/Primera AD:

        Confirmar elementos detectados automáticamente
        Añadir elementos no detectados manualmente
        Eliminar falsos positivos
        Editar descripciones
- Una vez confirmado, crear desglose estructurado (vincula con RF-026)
- Si versión de guion cambia (RF-032): notificar que desglose puede requerir actualización
- Permitir exportar desglose en formato estándar (PDF, Excel)

**Salidas**:

- Desglose estructurado de escena por categorías
- Elementos identificados automática o manualmente
- Exportación en PDF/Excel
- Vinculación con RF-026 (Gestión de Desgloses de Escenas)

** Precondiciones**:
- Usuario debe ser Script o Primera AD
- Guion debe estar cargado en sistema

**Postcondiciones**:

- Desglose disponible para coordinación de departamentos
- Información se refleja en calendario (RF-024) si escena se programa

**Actores**:

- Script (Continuista)
- Primera Ayudante de Dirección

**Dependencias**:
- RF-032 (Guion debe existir)
- RF-026 (Vinculación con desgloses de escenas)
- Librería NLP: spaCy, NLTK (Python)

## REPORTES Y ANALÍTICA
### RF-038: Reporte de Preparación para Rodaje
**Descripción**: El sistema debe generar checklist visual que muestre estado de preparación de una escena o episodio específico antes del rodaje, identificando por departamento qué está completo, incompleto o bloqueante.

**Prioridad**: Alta

**Entradas**:
- Escena(s) o episodio a consultar (selección única o múltiple)
- Fecha estimada de rodaje (opcional)

**Proceso**:
- Validar que usuario sea Script, Primera AD, Jefe de Departamento o Administrador Total
- Para cada escena seleccionada, consultar:

        Fotografías de continuidad subidas (RF-011)
        Estados de continuidad (RF-017): OK / Pendiente / Error
        Desglose de escena (RF-026): qué se requiere por departamento
- Calcular estado por departamento:

        Completo: Todas las fotografías requeridas están subidas y en estado OK
        Incompleto: Faltan fotografías de referencia
        Bloqueante: Existen errores críticos (estado "Error a Corregir") no resueltos
- Generar reporte visual:

        Encabezado: Escena(s), Episodio, Fecha de rodaje
        Tabla por departamento:
        DepartamentoEstadoFotografías OKFotografías PendientesErrores CríticosResponsable

- Sección "Fotografías Faltantes":

        Personaje: [Nombre]
        Detalle: [Vestuario/Maquillaje/Utilería]
        Descripción: [Qué falta específicamente]

- Sección "Errores Críticos Pendientes":

        Departamento
        Descripción del error
        Fotografía involucrada (thumbnail)
        Responsable de resolver
- Permitir exportar reporte en PDF con marca de agua
- Mostrar indicador visual general:

        Verde: Todo listo para rodar
        Amarillo: Listo con observaciones menores
        Rojo: NO listo, existen bloqueantes

**Salidas**:
- Reporte visual de preparación por departamento
- Lista de fotografías faltantes
- Lista de errores críticos pendientes
- Exportación en PDF con marca de agua

**Precondiciones**:
- Escena(s) debe(n) existir en el proyecto
- Debe existir desglose de escena (RF-026) para saber qué se requiere

**Postcondiciones**:
- Equipo puede identificar qué falta antes de rodar
- Prevención de retrasos en set por falta de preparación

**Actores**: Script, Primera AD, Jefes de Departamento, Administrador Total

**Dependencias**:
- RF-011 (Fotografías de continuidad)
- RF-017 (Estados de continuidad)
- RF-026 (Desgloses de escenas)


### RF-039: Informe de Continuidad por Escena
**Descripción**: El sistema debe generar documento PDF con todas las fotografías de continuidad de una escena específica, organizadas por toma y personaje, con metadatos completos y comentarios.

**Prioridad**: Media

**Entradas**:
- Número de escena (selección única)
- Filtros opcionales: Personaje específico, Tipo de Detalle

**Proceso**:
- Validar que usuario sea Script, Jefe de Departamento, Primera AD o Administrador Total
- Consultar todas las fotografías de la escena desde base de datos (RF-013)
- Aplicar filtros si usuario los especificó
- Organizar fotografías por:

        Toma (orden ascendente)
        Personaje (alfabético)
        Tipo de Detalle (Vestuario, Maquillaje, Utilería, Set)
- Generar PDF con estructura:

**Portada**:
- Proyecto
- Número de escena y descripción (desde RF-026 si existe)
- Fecha de generación del reporte
- Usuario que generó el reporte
- Contenido por toma:

        Número de Toma
        Fotografías de la toma (3-4 por página, tamaño adecuado)
- Debajo de cada fotografía:

        Nomenclatura completa
        Personaje(s)
        Tipo de Detalle
        Estado de Continuidad
        Fecha de rodaje
        Comentarios

- Historial de versiones:

        Si existen múltiples versiones de una misma fotografía, incluir comparación V1 vs V2 vs V3

- Aplicar marca de agua reforzada en cada página:

        Usuario que generó reporte
        Timestamp de generación
        Proyecto
        Marca múltiple en diagonal


- Comprimir PDF si supera 50 MB (mantener calidad de imágenes)
- Registrar generación de reporte en log de auditoría

**Salidas**:
- PDF con todas las fotografías de la escena organizadas
- Metadatos completos por fotografía
- Historial de versiones (si aplica)
- Marca de agua reforzada en cada página
- Registro en log de auditoría

**Precondiciones**:
- Usuario debe tener permiso según su rol
- Escena debe tener al menos 1 fotografía cargada

**Postcondiciones**:
- Reporte disponible para revisión antes de rodar escenas relacionadas o retomas
- Exportación queda registrada en auditoría

**Actores**: Script, Jefes de Departamento, Primera AD, Administrador Total

**Dependencias**:
- RF-011 (Fotografías de continuidad)
- RF-013 (Búsqueda de fotografías)
- RF-018 (Marca de agua)
- RF-010 (Log de auditoría)


### RF-040: Parte de Rodaje Diario Semi-Automático
**Descripción**: El sistema debe permitir al Script generar el parte de rodaje diario de forma semi-automática, pre-completando información desde el calendario y fotografías del día, y permitiendo añadir datos manualmente.

**Prioridad**: Media

**Entradas (pre-completadas automáticamente)**:

- Escenas rodadas (desde RF-024: eventos del calendario del día)
- Fotografías de continuidad tomadas (cuenta desde RF-011: fotografías con fecha = hoy)
- Estado de continuidad (resumen desde RF-017: X fotos OK, Y Pendientes, Z Errores)
- Cambios de calendario (desde RF-025: modificaciones realizadas durante el día)
- Mensajes urgentes (desde RF-029: mensajes con prioridad Urgente del día)
- Entradas (manuales, completadas por Script):

        Minutaje rodado (texto, ej: "12 minutos")
        Incidencias técnicas (texto libre, máx 500 caracteres)
        Notas generales de producción (texto libre, máx 1000 caracteres)
        Observaciones para día siguiente (texto libre, máx 500 caracteres)

**Proceso**:

- Validar que usuario sea Script
- Al final de jornada de rodaje, Script abre "Generar Parte de Rodaje"
- Sistema pre-completa automáticamente:

        Fecha del parte
        Proyecto
        Escenas programadas para hoy (desde calendario)
        Escenas efectivamente rodadas (Script marca cuáles se completaron)
        Total de fotografías de continuidad subidas hoy
        Resumen de estados: X OK, Y Pendientes, Z Errores
        Cambios de calendario realizados hoy (lista cronológica)
        Mensajes urgentes enviados/recibidos


- Script completa campos manuales:

        Minutaje rodado
        Incidencias técnicas (ej: "Falla de cámara B en toma 15, reemplazo en 30 min")
        Notas generales
        Observaciones para mañana


- Generar PDF con formato estándar de parte de rodaje:

        Encabezado con proyecto, fecha, Script responsable
        Sección 1: Escenas rodadas
        Sección 2: Fotografías y continuidad
        Sección 3: Cambios y novedades
        Sección 4: Incidencias técnicas
        Sección 5: Notas de producción
        Sección 6: Observaciones para día siguiente
        Firma digital del Script


- Aplicar marca de agua
- Enviar automáticamente por email a: Productor de Línea, Director, Primera AD
- Almacenar PDF en storage vinculado al proyecto

**Salidas**:

- Parte de rodaje diario en PDF
- Email automático enviado a destinatarios
- PDF almacenado en historial de partes de rodaje
- Registro en log de auditoría

**Precondiciones**:

- Usuario debe ser Script
- Debe haber actividad de rodaje en el día (eventos en calendario, fotografías subidas)

**Postcondiciones**:

- Parte de rodaje disponible para consulta histórica
- Productor y Director reciben reporte automático diario

**Actores**: Script (Continuista) - genera el parte

**Dependencias**:
- RF-011 (Fotografías del día)
- RF-024 (Eventos del calendario)
- RF-025 (Cambios de calendario)
- RF-017 (Estados de continuidad)
- RF-029 (Mensajes urgentes)
- Sistema de notificaciones por email (RF-005)


### RF-041: Listado de Vestuario por Personaje
**Descripción**: El sistema debe permitir al Jefe de Vestuario generar informe visual de inventario de vestuario por personaje con fotografías, descripciones, escenas donde se usa y estados de las prendas.

**Prioridad**: Media
**Entradas**:
- Personaje seleccionado (selección única)
- Filtros opcionales: Tipo de prenda, Estado (limpio/sucio/roto)

**Proceso**:

- Validar que usuario sea Jefe de Vestuario, Script o Administrador Total
- Consultar todas las fotografías de continuidad con:

        Personaje = personaje seleccionado
        Tipo de Detalle = Vestuario


- Aplicar filtros si usuario los especificó
        Organizar fotografías por:

            Orden cronológico de escenas (Escena 1, Escena 2, etc.)
            Estado de la prenda (si existen múltiples copias)


- Extraer metadatos de cada fotografía:

        Descripción de la prenda (desde comentarios o análisis IA)
        Escena donde se usa
        Estado de la prenda (limpio/sucio/roto/mojado)
        Fecha de rodaje
        Notas de vestuario (desde RF-026: Desgloses)


- Generar reporte visual:

        Portada:

            Proyecto
            Personaje
            Total de prendas inventariadas
            Fecha de generación


        Contenido:

            Tabla con fotografía + descripción por prenda:
            FotografíaDescripciónEscenasEstado(s)Notas

- Sección de tracking:

        Cronología de uso: cuándo se usó cada prenda en orden de escenas
        Estados por escena: Escena 1 (limpio), Escena 5 (sucio), Escena 8 (roto)

- Notas de mantenimiento:

        Prendas que requieren lavandería
        Prendas que requieren reparación
        Prendas que requieren duplicados adicionales


- Aplicar marca de agua
- Permitir exportar en PDF o Excel

**Salidas**:
- Informe visual de inventario de vestuario por personaje
- Cronología de uso por escena
- Notas de mantenimiento
- Exportación en PDF o Excel con marca de agua

**Precondiciones**:
- Debe existir al menos 1 fotografía de vestuario del personaje
- Usuario debe tener permiso según rol

**Postcondiciones**:
- Jefe de Vestuario tiene control visual completo del vestuario del personaje
- Planning de vestuario facilitado para fechas de rodaje

**Actores**:
- Jefe de Vestuario (principal usuario)
- Script, Administrador Total (también pueden generar)

**Dependencias**:
- RF-011 (Fotografías de continuidad con Detalle = Vestuario)
- RF-026 (Desgloses pueden incluir notas de vestuario)


### RF-042: Checklist de Escenas Grabadas vs Pendientes
**Descripción**:El sistema debe mostrar lista visual de todas las escenas del proyecto con indicadores de estado (completadas, en progreso, pendientes), permitiendo filtrado por episodio, locación, personaje y fecha.

**Prioridad**: Media

**Entradas**:

- Proyecto activo
- Filtros opcionales: Episodio, Locación, Personaje, Rango de fechas

**Proceso**:

- Validar que usuario esté autenticado
- Consultar todas las escenas del proyecto desde:

        Desgloses de escenas (RF-026)
        Calendario de rodaje (RF-024): escenas programadas
        Parte de rodaje (RF-040): escenas efectivamente rodadas


- Calcular estado de cada escena:

        Completada: Escena marcada como rodada en parte de rodaje Y tiene fotografías de continuidad con estado OK
        En Progreso: Escena tiene fotografías pero no marcada como completada
        Pendiente: Escena sin fotografías y no rodada
        Programada: Escena tiene fecha en calendario pero no rodada aún


- Mostrar lista visual:

        Vista de tabla o cards
        Indicador visual de estado (color + icono)
- Información por escena:

        Número de escena
        Descripción breve
        Estado
        Fecha programada (si existe)
        Fecha rodada (si completada)
        Personajes involucrados
        Locación
        Progreso de continuidad (% de fotografías OK)


- Permitir filtros múltiples:

        Por episodio (si es serie)
        Por locación (agrupar escenas de misma locación)
        Por personaje (escenas donde aparece personaje X)
        Por rango de fechas (programadas o rodadas)
        Por estado (solo completadas, solo pendientes, etc.)


- Mostrar estadísticas generales:

        Total de escenas: X
        Completadas: Y (Z%)
        En progreso: W
        Pendientes: V
        Días de rodaje transcurridos vs estimados


- Permitir exportar checklist en PDF o Excel

**Salidas**:

- Lista visual de escenas con estados
- Estadísticas de progreso del proyecto
- Exportación en PDF/Excel

**Precondiciones**:

- Proyecto debe estar activo
- Debe existir al menos 1 escena (RF-026)

**Postcondiciones**:

- Equipo puede visualizar evolución del proyecto
- Identificar escenas pendientes para coordinación

**Actores**: Todos los usuarios autorizados (visibilidad según rol)

**Dependencias**:

- RF-026 (Desgloses de escenas)
- RF-024 (Calendario)
- RF-040 (Parte de rodaje marca escenas como completadas)
- RF-011 (Fotografías de continuidad)0


### RF-043: Reporte de Auditoría de Accesos
**Descripción**: El sistema debe permitir al Productor de Línea generar reportes de auditoría de accesos y acciones con filtros personalizables para investigar actividad de usuarios y posibles filtraciones.

**Prioridad**: Alta

**Entradas**:
- Filtros de auditoría (opcionales, combinables):

        Usuario específico (selección única)
        Rango de fechas (desde-hasta)
        Tipo de acción (Login/Visualización fotografía/Visualización guion/Modificación calendario/Exportación reporte/Cambio de permisos/Revocación de acceso)
        Solo contenido sensible (checkbox: limita a visualizaciones de guiones y fotografías)
        Resultado (Éxito/Fallo)

**Proceso**:

- Validar que usuario sea Productor de Línea (Administrador Total) o Director
- Consultar tabla LOG de auditoría (RF-010) aplicando filtros seleccionados
- Ordenar resultados por timestamp descendente (más recientes primero)
- Para cada registro, mostrar:

        Timestamp (fecha y hora exacta con zona horaria)
        Usuario (nombre completo + email)
        Acción realizada (descripción clara)
        Detalles adicionales (ej: "Visualizó fotografía RACCORD_EP01_ESC005_T02_MARIA_VEST_V1.jpg")
        IP de origen
        Dispositivo (tipo, modelo, sistema operativo, navegador)
        Resultado (Éxito/Fallo)
- Implementar paginación (100 registros por página)
- Permitir exportar reporte completo en Excel con:

        Todas las columnas mencionadas
        Filtros aplicados en encabezado
        Fecha de generación del reporte
        Usuario que generó el reporte
- Registrar generación de reporte de auditoría en el mismo LOG (meta-auditoría)

**Salidas**:

- Lista de eventos de auditoría según filtros
- Exportación en Excel con todos los detalles
- Registro de generación de reporte en log

**Precondiciones**:
- Usuario debe ser Administrador Total o Director
- Debe existir al menos 1 registro en tabla LOG

**Postcondiciones**:

- Administrador puede identificar patrones de acceso sospechosos
- Investigación de posibles filtraciones con trazabilidad completa

**Actores**: Productor de Línea (Administrador Total) y Director

**Dependencias**:

- RF-010 (Log de auditoría debe estar registrando eventos)

## INTEGRACIONES Y FUNCIONALIDADES ADICIONALES
### RF-044: Integración con Google Workspace
**Descripción**: El sistema debe integrarse con Google Workspace permitiendo vincular cuenta de Google del usuario, importar eventos de Calendar, adjuntar documentos desde Drive y exportar reportes directamente a Drive.

**Prioridad**: Media

**Entradas**:
- Credenciales de Google del usuario (OAuth 2.0)
- Acción a realizar: Importar calendario / Adjuntar documento / Exportar reporte

**Proceso**:

- Usuario accede a configuración de integración en su perfil (RF-009)
- Hacer clic en "Conectar con Google"
- Sistema redirige a página de autorización de Google OAuth 2.0
- Usuario autoriza permisos solicitados:

        Google Calendar: lectura de eventos
        Google Drive: lectura de documentos, escritura de reportes


- Sistema recibe token de acceso y lo almacena cifrado en base de datos vinculado al usuario
- Importar eventos de Google Calendar:

        Usuario selecciona "Importar desde Google Calendar"
        Sistema consulta API de Google Calendar
        Mostrar eventos próximos (próximos 30 días)
        Usuario selecciona eventos a importar
        Sistema crea eventos en calendario de Raccord (RF-024) marcados como "Importados de Google"
        Sincronización unidireccional (Google → Raccord, NO al revés)


- Adjuntar documento desde Google Drive:

        Usuario en mensajería interna (RF-029) o comunicados (RF-030) hace clic en "Adjuntar desde Drive"
        Sistema muestra navegador de Google Drive del usuario
        Usuario selecciona documento
        Sistema obtiene enlace compartido del documento
        Mensaje incluye enlace al documento (no descarga, evita duplicación)


- Exportar reporte a Google Drive:

        Usuario genera reporte (RF-038 a RF-043)
        En lugar de descargar, selecciona "Guardar en Google Drive"
        Sistema sube PDF a carpeta "Raccord Reports" en Drive del usuario
        Confirmar ubicación del archivo guardado

- Registrar todas las acciones de integración en log de auditoría

**Salidas**:

- Cuenta de Google vinculada al usuario
- Eventos importados desde Google Calendar
- Documentos adjuntados desde Drive en mensajes
- Reportes exportados a Google Drive
- Registro de integraciones en log de auditoría

**Precondiciones**:

- Usuario debe tener cuenta de Google activa
- Usuario debe autorizar permisos OAuth 2.0

**Postcondiciones**:

- Integración activa hasta que usuario revoque permisos
- Token de acceso renovado automáticamente antes de expiración

**Actores**: Cualquier usuario que desee integrar su cuenta de Google

**Dependencias**:

- Google OAuth 2.0 API
- Google Calendar API
- Google Drive API
- RF-024 (Importar eventos a calendario)
- RF-029 (Adjuntar documentos en mensajes)


### RF-045: Inventario de Recursos/Materiales
**Descripción**: El sistema debe permitir a Coordinadores y Jefes de Departamento llevar inventario de materiales, recursos necesarios y listas de compras con estado de adquisición, SIN incluir gestión de presupuesto ni costos.

**Prioridad**: Baja

**Entradas**:

- Nombre del recurso/material (texto, máx 200 caracteres)
- Departamento responsable (selección única)
- Cantidad requerida (número entero)
- Descripción detallada (texto, máx 500 caracteres)
- Proveedor/Origen (texto, máx 200 caracteres, opcional)
- Fecha requerida (fecha)
- Estado de adquisición (selección única): Pendiente / En Proceso / Adquirido
- Escenas donde se requiere (selección múltiple, opcional)

**Proceso**:

-Validar que usuario sea Coordinador de Departamento, Jefe de Departamento o Administrador Total
- Mostrar formulario de registro de recurso
- Crear ítem en inventario vinculado a:

        Proyecto
        Departamento responsable
        Usuario que registró
- Permitir editar estado de adquisición:

        Pendiente → En Proceso (se está gestionando)
        En Proceso → Adquirido (ya está disponible)
- Mostrar vista de inventario por departamento:

        Tabla con: Recurso, Cantidad, Estado, Fecha requerida, Responsable
        Filtros: Por departamento, Por estado, Por fecha
        Indicadores visuales:

            Pendiente y fecha vencida (fecha requerida < hoy)
            Pendiente próximo a vencer (fecha requerida < 7 días)
            Adquirido




- Enviar notificaciones automáticas:

        7 días antes de fecha requerida: recordatorio a responsable
        Fecha vencida y estado Pendiente: alerta a coordinador

- Vincular recursos con escenas (RF-026) si se especificó
- Permitir exportar lista de compras pendientes en PDF o Excel (sin precios, solo ítems)

**Salidas**:

- Inventario de recursos por departamento
- Lista de compras pendientes
- Notificaciones de recordatorios
- Exportación en PDF/Excel

**Precondiciones**:

- Usuario debe ser Coordinador, Jefe de Departamento o Administrador Total
- Proyecto debe estar activo

**Postcondiciones**:

- Control de recursos necesarios para rodaje
- Identificación temprana de faltantes

**Actores**:

- Coordinadores de Departamento
- Jefes de Departamento
- Administrador Total

**Dependencias**:

- RF-026 (Vinculación con escenas)
- RF-028 (Notificaciones de recordatorios)

