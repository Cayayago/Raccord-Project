# RESTRICCIONES - RACCORD
## RESTRICCIONES TÉCNICAS
### REST-001: Lenguaje de Programación Backend
#### Tipo: Técnica
**Descripción**: El backend del sistema debe ser desarrollado utilizando Python 3.11+ como lenguaje principal, sin posibilidad de usar otros lenguajes para el core del sistema.

**Justificación**:
- Definido explícitamente en el Alcance del Proyecto
- Ecosistema robusto para desarrollo web (FastAPI, Django, Flask)
- Excelente soporte para machine learning e IA (TensorFlow, PyTorch, YOLOv8)
- Librerías maduras para procesamiento de imágenes (Pillow, OpenCV)
- Equipo de desarrollo tiene experiencia en Python

**Impacto**:
- Determina frameworks disponibles (FastAPI, Django)
- Determina librerías de IA/ML disponibles
- Afecta performance (Python más lento que Go/Rust, pero suficiente para este caso)

**Alcance**:
- Todo el código backend
- APIs REST
- Procesamiento de IA
- Scripts de automatización


### REST-002: Sistema de Gestión de Base de Datos
#### Tipo: Técnica
**Descripción**: El sistema debe utilizar PostgreSQL 15+ como motor de base de datos relacional, sin posibilidad de usar MySQL, MongoDB u otros motores.

**Justificación**:
- Definido explícitamente en el Alcance del Proyecto
- Soporte nativo para JSON (metadatos flexibles de fotografías)
- Full-text search potente (búsqueda en comentarios)
- Tipos de datos ricos (arrays, JSONB)
- ACID completo (transacciones robustas)
- Superior a MySQL para queries complejas con metadata

**Impacto**:
- Determina estrategia de almacenamiento de metadatos
- Afecta queries de búsqueda (sintaxis PostgreSQL)
- Requiere conocimiento específico de PostgreSQL en equipo

**Alcance**:
- Almacenamiento de datos estructurados
- Metadata de fotografías
- Usuarios, roles, permisos
- Calendario, guiones, mensajes

### REST-003: Infraestructura Cloud Obligatoria
#### Tipo: Técnica / Económica
**Descripción**: El sistema debe desplegarse en infraestructura cloud (AWS, Azure o DigitalOcean), no en servidores on-premise del cliente.

**Justificación**:
- Escalabilidad automática
- Backups automáticos y redundancia geográfica
- Disponibilidad 99.9% garantizada por proveedor
- Costos operativos menores (no requiere hardware propio)
- Presupuesto limitado de microempresa no permite servidores propios

**Impacto**:
- Cliente debe pagar suscripción mensual a proveedor cloud
- Datos almacenados en servidores de terceros (requiere confianza en proveedor)
- Dependencia de conectividad a internet

**Alcance**:
- Servidores de aplicación
- Base de datos
- Storage de fotografías
- Servicios de notificaciones


### REST-004: Prioridad de Plataforma Móvil (Tablets iPad)
#### Tipo: Técnica / Organizacional
**Descripción**: El desarrollo debe priorizar tablets iPad como plataforma principal, seguido de Android tablets, smartphones y aplicación web. Si hay conflictos de recursos, iPad tiene máxima prioridad.

**Justificación**:
- Script, DoP y Oscar mencionan iPads como dispositivo más usado en set
- Tablets proveen pantalla suficiente para visualizar fotografías en detalle
- Más cómodo que smartphone para trabajo prolongado en set
- 
**Impacto**:
- Testing prioritario en iPad
- Optimización de interfaz para pantallas 10-13 pulgadas
- Si hay bugs específicos de plataforma, iPad se resuelve primero

**Alcance**:
- Diseño de interfaz
- Priorización de testing
- Asignación de recursos de desarrollo

### REST-005: Sin Soporte para Internet Explorer
#### Tipo: Técnica
**Descripción**: El sistema web NO soportará Internet Explorer en ninguna versión. Solo navegadores modernos (Chrome 90+, Firefox 88+, Safari 14+, Edge 90+).

**Justificación**:
- Internet Explorer está descontinuado desde junio 2022 (fin de soporte de Microsoft)
- Desarrollo para IE requiere código legacy y compatibilidad compleja
- Recursos de desarrollo limitados
- Usuario objetivo (profesionales audiovisuales) usa navegadores modernos

**Impacto**:
- Si usuario intenta acceder con IE: mostrar mensaje "Navegador no soportado, use Chrome, Firefox, Safari o Edge"
- Permite usar tecnologías modernas (ES2020+, CSS Grid, Flexbox sin prefijos)

**Alcance**:
- Aplicación web
- Testing de compatibilidad

### REST-006: Tamaño Máximo de Archivo Individual
#### Tipo: Técnica
**Descripción**: 
- El sistema debe limitar el tamaño máximo de archivos individuales:

      Fotografías: 10 MB
      Guiones PDF: 50 MB
      Documentos adjuntos: 20 MB

**Justificación**:
- Balance entre calidad de imagen y performance de carga
- Fotografías >10 MB son comprimidas automáticamente a 85% calidad sin pérdida visual significativa
- Guiones PDF raramente superan 50 MB
- Límites de API Gateway y tiempo de request (generalmente 30-60 segundos)

**Impacto**:
- Fotografías profesionales de cámaras de alta gama (RAW) deben convertirse a JPG antes de subir
- Si usuario intenta subir archivo mayor: sistema rechaza y muestra mensaje de error con límite

**Alcance**:
- RF-011 (Carga de fotografías)
- RF-032 (Subida de guiones)
- RF-029 (Adjuntos en mensajes)


## RESTRICCIONES TEMPORALES
### REST-007: Plazo de Desarrollo
#### Tipo: Temporal / Académica
**Descripción**: El sistema debe completarse dentro del programa de formación de 18 meses (contexto académico), con estimado de desarrollo real de 16 semanas (4 meses) para el MVP funcional.

**Justificación**:
- Proyecto es parte de programa de formación académica
- 18 meses = duración total del programa
- 16 semanas = tiempo estimado de desarrollo efectivo del MVP
- Resto del tiempo: análisis, diseño, documentación, testing, presentaciones académicas

**Impacto**:
- Priorización estricta de funcionalidades (MVP vs Nice-to-Have)
- Funcionalidades avanzadas (reconocimiento facial, integraciones complejas) quedan fuera del alcance inicial
- Presión de tiempo para equipo de desarrollo

**Alcance**:
- Planning del proyecto
- Priorización de requisitos
- Gestión de expectativas del cliente

## RESTRICCIONES ECONÓMICAS
### REST-008: Presupuesto Limitado de Microempresa (continuación)

**Impacto (continuación)**:

- Servicios cloud con free tier o pay-as-you-go:

    AWS S3, Azure Blob o DigitalOcean Spaces (pago por GB usado)
    SendGrid free tier (100 emails/día gratis, luego pago)
    Firebase Cloud Messaging (gratis hasta 1M mensajes/día)


- Evitar:
    Software con licencias anuales costosas
    Servicios enterprise con contratos mínimos
    Hardware dedicado (servidores físicos)
- Hosting en tier básico suficiente (no over-provisioning)

**Alcance**:
- Selección de tecnologías
- Selección de proveedores cloud
- Arquitectura del sistema


## RESTRICCIONES ORGANIZACIONALES
### REST-009: Equipo de Desarrollo Reducido
#### Tipo: Organizacional
**Descripción**: El desarrollo será realizado por equipo de 4 personas (estudiantes en formación), limitando alcance de funcionalidades implementables en el MVP.

**Justificación**:

- Proyecto académico con equipo de estudiantes
- Recursos humanos limitados
- Experiencia del equipo en formación (no senior developers)

**Impacto**:
- Priorización estricta: solo funcionalidades críticas en MVP
- Funcionalidades complejas (IA avanzada, integraciones múltiples) requieren más tiempo
- Posible necesidad de simplificar algunas funcionalidades
- Testing menos exhaustivo que proyecto enterprise
- Documentación limitada a lo esencial

**Alcance**:
- Planning del proyecto
- Asignación de tareas
- Expectativas de calidad y cobertura


### REST-010: Contexto Académico del Proyecto
#### Tipo: Organizacional
**Descripción**: El proyecto es desarrollado en contexto académico, requiriendo entregables específicos para evaluación (documentación, presentaciones, informes) además del software funcional.

**Justificación**:
- Proyecto es requisito de graduación
- Institución educativa requiere documentación formal
- Presentaciones y defensas del proyecto son obligatorias

**Impacto**:
- Tiempo dedicado a documentación académica (no solo técnica)
- Presentaciones intermedias y finales
- Posible necesidad de ajustar cronograma por fechas académicas fijas
- Entregables adicionales:

    Documentos de análisis
    Propuesta de proyecto
    Informes de avance
    Presentación final
    Defensa ante jurados

**Alcance**:
- Gestión del tiempo
- Priorización de actividades
- Entregables del proyecto
## RESTRICCIONES LEGALES Y DE CUMPLIMIENTO
### REST-011: Cumplimiento de Políticas de Confidencialidad
#### Tipo: Legal / Seguridad
***Descripción**: El sistema debe cumplir con políticas de confidencialidad de productoras audiovisuales y plataformas de streaming (Netflix, Amazon Prime, HBO), incluyendo contratos OPS estándar de la industria.

**Justificación**:
- Oscar describe requisitos de confidencialidad extremos en producciones
- Multas millonarias por incumplimiento (hasta 50 millones COP por filtración)
- Productoras trabajan con contenido de plataformas que exigen seguridad estricta
- Contratos de prestación de servicios (OPS) incluyen cláusulas de confidencialidad

**Impacto**:
- Seguridad debe ser prioridad crítica (no negociable)
- Marca de agua obligatoria en todo contenido visualizado
- Bloqueo de capturas de pantalla obligatorio
- Auditoría completa de accesos obligatoria
- Cifrado end-to-end obligatorio
- NO se pueden hacer excepciones de seguridad aunque usuario lo solicite

**Alcance**:
- Todos los requisitos de seguridad (RNF-014 a RNF-020)
- RF-018 (Marca de agua)
- RF-033 (Visualización de guiones)
- RF-010 (Log de auditoría)

### REST-012: Regulaciones de Privacidad de Datos (GDPR-equivalente Colombia)
#### Tipo: Legal
**Descripción**: El sistema debe cumplir con regulaciones de privacidad de datos personales aplicables en Colombia (Ley 1581 de 2012 - Protección de Datos Personales) y consideraciones de GDPR si trabajan con producciones europeas.

**Justificación**:
- Ley colombiana de protección de datos personales es obligatoria
- Producciones con equipos internacionales (Europa) pueden requerir cumplimiento GDPR
- Datos personales de usuarios (email, teléfono, fotografías con rostros) están regulados

**Impacto**:
- Requerir consentimiento explícito para recolección de datos personales
- Permitir a usuarios solicitar:

    Acceso a sus datos (derecho de acceso)
    Corrección de datos (derecho de rectificación)
    Eliminación de datos (derecho al olvido)
- Retención limitada de datos:

    Logs de auditoría: 90 días activo, 1 año archivo, purga a 15 meses
    Datos de usuarios inactivos: eliminación tras 2 años sin uso
- Política de privacidad visible y aceptada por usuario
- Notificación de brechas de seguridad en 72 horas

**Alcance**:
- RF-001, RF-002, RF-003 (Registro de usuarios)
- RF-009 (Gestión de perfil)
- RF-010 (Log de auditoría)
- Política de privacidad del sistema


## RESTRICCIONES FUNCIONALES EXCLUIDAS
### REST-013: Sin Gestión de Presupuesto ni Contabilidad
#### Tipo: Funcional Excluida
**Descripción**: El sistema NO debe incluir funcionalidades de gestión de presupuesto, contabilidad de costos de producción, facturación ni pago a proveedores.

**Justificación**:

- Alcance se limita a continuidad visual y coordinación operativa
- Gestión financiera es responsabilidad de departamento de producción administrativo
- Requeriría integraciones contables complejas fuera del alcance MVP
- Requeriría cumplimiento de regulaciones fiscales adicionales

**Impacto**:
- RF-045 (Inventario de recursos) NO incluye precios ni costos
- Sistema NO calcula presupuestos ni gastos
- NO genera facturas ni reportes financieros
- Si cliente necesita gestión financiera: debe usar software contable separado

**Alcance**: RF-045 (Inventario es solo tracking de materiales físicos, no costos)


### REST-014: Sin Edición de Imágenes
#### Tipo: Funcional Excluida
**Descripción**: El sistema NO debe incluir funcionalidades de edición de imágenes (recorte, ajuste de color, filtros, anotaciones sobre imagen, eliminación de objetos).

**Justificación**:
- Fuera del alcance MVP
- Fotografías se suben tal cual fueron tomadas en set (sin post-procesamiento)
- Edición requeriría herramientas complejas (tipo Photoshop) que aumentan desarrollo significativamente
- No es necesario para continuidad (fotografías son referencias, no material final)

**Impacto**:
- Si usuario necesita editar fotografía: debe usar software externo (Photoshop, Lightroom) antes de subir
- Sistema solo permite: visualizar, comparar, buscar, etiquetar
- NO permite: recortar, rotar (más allá de visualización), ajustar brillo/contraste, añadir anotaciones gráficas

**Alcance**:

- RF-014 (Visualización)
- RF-015 (Comparación)

### REST-015: Sin Integraciones con Software de Edición Profesional
### Tipo: Funcional Excluida
**Descripción**: La versión 1.0 NO incluirá integraciones directas con software de edición profesional: Adobe Premiere, DaVinci Resolve, Final Draft, Movie Magic Scheduling.

**Justificación**:
- Alcance limitado de MVP
- Integraciones complejas requieren APIs de terceros y desarrollo extenso
- Presupuesto y tiempo limitados
- Pueden considerarse en versiones futuras (roadmap post-MVP)

**Impacto**:
- Datos de Raccord no se sincronizan automáticamente con herramientas de edición
- Si usuario necesita exportar datos a estas herramientas: debe hacerlo manualmente (exportar reportes PDF/Excel y copiar información)
- NO hay plugin de Raccord para Premiere/DaVinci

**Alcance**: Funcionalidades de integración quedan fuera de versión 1.0


### REST-016: Sin Reconocimiento Facial Automático
### Tipo: Funcional Excluida
**Descripción**: El sistema NO incluirá reconocimiento facial automático para identificación de actores en fotografías.

**Justificación**:
- Complejidad técnica alta (requiere entrenamiento de modelo custom)
- Preocupaciones de privacidad y regulaciones (reconocimiento facial está regulado en muchos países)
- Requiere dataset de rostros de actores (recolección de datos sensibles)
- Fuera del alcance MVP
- RF-020 (Análisis de IA) detecta "número de personas" pero NO identifica quiénes son

**Impacto**:
- Usuario debe etiquetar manualmente qué actores están en cada fotografía
- Sistema NO sugiere automáticamente: "Esta persona es Actor X"
- Análisis de IA solo detecta: "Hay 3 personas en la foto" (sin identificar)

**Alcance**:
- RF-020 (Análisis de IA)
- RF-011 (Etiquetado manual de personajes)


### REST-017: Sin Generación Automática de Storyboards
#### Tipo: Funcional Excluida
**Descripción**: El sistema NO generará automáticamente storyboards a partir de fotografías de continuidad ni guiones.

**Justificación**:
- Funcionalidad avanzada fuera del alcance MVP
- Requeriría IA generativa compleja
- Storyboards son herramienta de pre-producción (Raccord enfocado en producción y continuidad)

**Impacto**:
- Si usuario necesita storyboards: debe crearlos manualmente en herramientas especializadas (Storyboard Pro, Photoshop)
- Raccord solo muestra fotografías de referencia existentes, no genera nuevas visualizaciones
