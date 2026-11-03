# Ruta Agua - Contexto del Proyecto y Reglas de Negocio

## 1. Visión General
**Ruta Agua** (`com.rutaagua.camion`) es una aplicación móvil profesional 100% offline para la administración de inventario en camión, ruta de clientes y contabilidad diaria de un camión repartidor de agua purificada.

- **Usuario objetivo**: Chofer y ayudante del camión.
- **Enfoque de UI/UX**: Ultra intuitiva, moderna, con botones grandes, retroalimentación táctil clara y soporte para trabajo en movimiento bajo luz solar.
- **Operatividad**: Venta y reparto a domicilio de Lunes a Sábado, abasteciéndose en la planta purificadora (1 o 2 vueltas diarias).

---

## 2. Catálogo de Productos y Precios
1. **Recarga de Bidones (20 Litros)**:
   - Clasificación de envases según material y estado:
     1. `TRANSPARENTE`: Bidón PET (preferido por clientes exigentes).
     2. `AZUL`: Otro material / color estándar.
     3. `MARCADO`: Bidones en mal estado o con sello de otro color.
   - **Precios de recarga**: Personalizado por cliente, oscila habitualmente entre **$0.75** y **$1.25** (típicos: $0.75, $1.00, $1.25).
2. **Galones (4 Litros)**: Precio fijo de **$0.75** c/u.
3. **Pacas (Pack de 24 botellas de 500ml)**: Precio fijo de **$3.75** c/u.
4. **Minis (Pack de 12 botellas de 500ml)**: Precio fijo de **$1.75** c/u.
5. **Bidones Vendidos (Envase nuevo + líquido)**: Precio de **$5.50** o **$6.00**.

---

## 3. Dinámica de Movimiento de Envases (Llenos, Vacíos y Prestados)
- **Recarga Normal**:
  - Camión: Baja 1 bidón lleno (tipo: `TRANSPARENTE`, `AZUL` o `MARCADO`).
  - Camión: Sube 1 bidón vacío.
- **Bidón Prestado**:
  - El cliente recibe el bidón lleno pero **NO** entrega envase vacío (queda con deuda de envase).
  - Camión: Baja 1 lleno.
  - Camión: **NO** sube vacío.
  - Cliente: Aumenta su contador de `bidonesPrestados`.
- **Bidón Devuelto**:
  - El cliente devuelve un envase vacío que adeudaba de días anteriores.
  - Camión: Sube 1 bidón vacío.
  - Cliente: Se descuenta de sus `bidonesPrestados`.
- **Bidón Vendido ($5.50 o $6.00)**:
  - El cliente compra el envase con agua.
  - Camión: Baja 1 lleno.
  - Camión: **NO** sube vacío (no se adeuda envase).

---

## 4. Capacidad y Recargas del Camión en Planta
- **Capacidades máximas de referencia**:
  - Bidones de 20L: **218** unidades (habitual, pero puede variar por día: ej. 189, 164, 163).
  - Galones: **90** unidades.
  - Pacas (24x500ml): **6** unidades.
  - Minis (12x500ml): **2** unidades.
- **Ciclo de Planta**:
  - El camión recarga 1 o 2 veces al día.
  - La aplicación debe mostrar en tiempo real:
    - Bidones llenos restantes en camión (desglosado por `TRANSPARENTE`, `AZUL`, `MARCADO`).
    - Bidones vacíos recolectados.
    - Galones, pacas y minis restantes.
    - **Cálculo automático de recarga**: Cantidad necesaria a cargar en planta para alcanzar el tope fijado (ej. si quedan 20 llenos y la meta es 218, sugiere cargar 198).

---

## 5. Tipos de Clientes
- **Clientes de Ruta (`RUTA`)**:
  - Días fijos de visita (ej. Lunes y Jueves). Integrados con el calendario de ruta semanal.
- **Clientes de Llamada (`LLAMADA`)**:
  - Pedidos bajo demanda (WhatsApp / llamada telefónica) sin día recurrente fijo.
- **Clientes de Paso (Venta Rápida / Ocasional)**:
  - Ventas al paso sin registro permanente en el directorio de clientes.
- **Campos del Cliente**:
  - Nombre, teléfono, dirección / geolocalización o referencia.
  - Tipo de botella preferida (`TRANSPARENTE`, `AZUL`, `MARCADO`).
  - Precio habitual acordado por bidón.
  - Días de frecuencia (ej. `["LUNES", "JUEVES"]`).
  - Deuda monetaria acumulada ($).
  - Bidones prestados acumulados.

---

## 6. Contabilidad y Cierre del Día
- **Métodos de Pago**:
  - `EFECTIVO`
  - `TRANSFERENCIA`
  - `CREDITO` (queda debiendo el importe monetario).
  - `ABONO` (pago parcial de deuda previa).
  - `CANCELADO` (liquidación total de deuda pendiente).
- **Gastos Extras de Ruta**:
  - Registro de gastos operativos cotidianos (almuerzos, bebidas/colas, imprevistos) que se restan del total de efectivo en caja.
- **Arqueo y Cierre Diario**:
  - Total de bidones vendidos.
  - Desglose de ingresos (Efectivo vs Transferencia).
  - Saldo en créditos otorgados y cobro de deudas.
  - Total de gastos extras.
  - Efectivo neto a entregar.
  - Botón de exportación / resumen a **WhatsApp** formateado para el dueño o la administración.

---

## 7. Arquitectura Técnica
- **Lenguaje**: Kotlin con Jetpack Compose (Material 3).
- **Patrón**: MVVM + Clean Architecture / Repository Pattern.
- **Persistencia**: Room Database con soporte offline 100%, transacciones ACID (`@Transaction`), y flujos reactivos con Kotlin Coroutines `Flow`.
- **Inyección de dependencias**: Arquitectura modular desacoplada con Singleton Database y Repository.

---

## 8. Mejoras Recientes de Operación y Estética Corporativa
1. **Estética Corporativa de Logística y Fintech**:
   - Paleta de colores ejecutiva de alto contraste: Azul Marino Profundo (`#0F172A`), Azul Corporativo (`#0284C7`), Cian Técnico (`#06B6D4`), Verde Financiero (`#16A34A`), Ámbar Sobrio (`#D97706`), Rojo Alerta (`#DC2626`), fondos `#F8FAFC` y bordes sutiles de `1.dp`.
2. **Cálculo Preciso del Calendario Semanal**:
   - Cada día de la semana (`LUNES` a `SABADO`) calcula su fecha real (`YYYY-MM-DD`). Los días futuros marcan los clientes como `PROGRAMADO`, y solo se marcan como `ATENDIDO` si existen ventas reales en esa fecha específica.
3. **Edición y Eliminación de Entregas con Rollback Atómico**:
   - Permite corregir o eliminar ventas del día con reversión atómica (`@Transaction`) del impacto en el inventario del camión y en el saldo/envases del cliente.
4. **Gráfico Donut de Ventas y Reporte de WhatsApp Refinado**:
   - Gráfico de dona en `Canvas` en la pantalla de Inventario mostrando porcentajes de `Transparentes`, `Azules` y `Marcados` vendidos, junto a barras de `Galones`, `Pacas` y `Minis`.
   - Reporte de WhatsApp actualizado: excluye el stock restante en camión e incluye el desglose exacto de botellones vendidos.
5. **Foto de Fachada / Casa del Cliente**:
   - Captura vía cámara (`FileProvider`) y selección desde galería.
   - Guardado y compresión automática JPEG en almacenamiento interno (`filesDir/fotos_clientes/`).
   - Miniatura en tarjetas de clientes y vista ampliada con diálogo modal de zoom.
6. **Conexión Directa a WhatsApp por Cliente y Envío de Recibos**:
   - Normalización de números para Ecuador (`09xxxxxxxx` o `9xxxxxxxx` a `5939xxxxxxxx`).
   - Botón directo **"WhatsApp Cliente"** en tarjetas de clientes para enviar su Estado de Cuenta (deuda y envases prestados).
   - Botón directo **"📲 Enviar recibo al cliente"** en cada entrega del día para emitir recibo digital con productos, totales, método de pago y saldo actualizado.
   - Enlace directo vía `wa.me` con fallback a selector de contactos de WhatsApp para clientes sin número guardado o ventas al paso.


---

## 9. Arquitectura Multi-Vehículo, Sincronización Real-Time y Respaldo OTA ($0.00 Costo)
1. **Soporte Multi-Vehículo (`CAMION` + `CAMIONETA`)**:
   - Configuración independiente de inventarios y capacidades:
     - `CAMION`: Capacidad estándar 218 bidones, 90 galones, 6 pacas, 2 minis.
     - `CAMIONETA`: Capacidad estándar 80 bidones, 30 galones, 2 pacas, 1 mini.
   - Selector de vehículo activo visible en el header superior con alternancia rápida en 1 toque.
   - Todas las transacciones, recargas de planta, gastos e inventarios quedan asociados al vehículo operativo (`vehiculo: String`).
   - Filtro rápido en `RegistroDiaScreen`: `📊 Consolidado Ambos`, `🚚 Solo Camión`, `🛻 Solo Camioneta`.
2. **Sincronización en Tiempo Real Offline-First (Cloud Firestore a $0.00)**:
   - Autenticación formal de operadores y gerencia mediante `FirebaseAuth` (Email/Password). Se eliminó la autenticación anónima; los observadores remotos de Firestore (`addSnapshotListener`) se inicializan únicamente cuando se detecta un usuario autenticado verificado vía `AuthStateListener`.
   - Caché persistente offline activada (`persistentCacheSettings`) para funcionamiento ininterrumpido sin señal móvil en carretera.
   - 5 Observadores reactivos (`addSnapshotListener`) sincronizan bidireccionalmente: `clientes`, `transacciones`, `inventarios`, `recargas_planta` y `gastos`.
   - Directorio de clientes 100% compartido: las fotos de referencia se comprimen a miniaturas JPEG en Base64 (<20KB), eliminando la necesidad de Firebase Storage (costo $0.00 garantizado).
3. **Visibilidad Cruzada de Entregas entre Vehículos**:
   - Alertas visuales y badges en tiempo real en `CalendarioRutaScreen` y `ClientesScreen` si el cliente ya fue entregado hoy por el otro vehículo (ej. `"✅ Atendido hoy por 🛻 CAMIONETA (10:30 • 2 un.)"`), evitando duplicar viajes.
4. **Seguridad Local por PIN (4 Dígitos)**:
   - PIN predeterminado `1234` configurable en Ajustes.
   - Se solicita exclusivamente antes de:
     1) Editar una venta registrada.
     2) Eliminar una venta registrada.
     3) Eliminar a un cliente.
     4) Modificar manualmente deuda monetaria o envases prestados de un cliente.
5. **Copias de Seguridad Locales (JSON) y Actualizaciones OTA**:
   - Menú de Ajustes (⚙️) accesible desde la barra superior.
   - Exportación completa de base de datos a archivo JSON local con opción de compartir vía WhatsApp u otras aplicaciones.
   - Importación y restauración segura de respaldos JSON mediante selector de archivos del sistema.
   - Comprobador de actualizaciones Over-The-Air (`OTAUpdateChecker`) con descarga e instalación automática del APK más reciente mediante `DownloadManager` y `FileProvider`.

---

## 10. Sustitución de Selector Local por Autenticación Formal Firebase Auth (`LoginScreen.kt`)
1. **Ilustraciones Vectoriales en Compose Canvas (`VehiculoIllustrations.kt`)**:
   - **`CamionIllustration`**: Camión repartidor moderno (cabina blanca/azul corporativo, ventanas con visera de protección solar, chasis, parachoques y carrocería trasera con múltiples estantes de bidones celestes PET y azules con tapones blancos, llantas dobles con aros de acero y sombra de apoyo).
   - **`CamionetaIllustration`**: Camioneta pickup de reparto (cabina deportiva en azul acero y plata, balde/paila de carga con barandas metálicas protectoras, bidones de 20L apilados en el platón, luces frontales y posteriores, rines deportivos y sombra).
   - **`VehiculoThumbnail`**: Componente compacto de miniatura vectorial (22.dp a 28.dp) para usar en badges y estados de entrega.
2. **Sustitución Integral de `SeleccionVehiculoScreen.kt`**:
   - Se eliminó el selector manual de vehículo y la clave PIN local en favor de una pantalla formal de login en la nube (`ui/auth/LoginScreen.kt`) respaldada por **Firebase Authentication** (Email/Password).
   - El vehículo operativo y el rol de usuario ya no se seleccionan a discreción del usuario; se determinan dinámicamente mediante el correo electrónico autenticado en Firebase (`resolverPerfilPorEmail()`).
3. **Aislamiento Operativo de Sesión y Persistencia**:
   - La sesión autenticada se evalúa al arranque de la app mediante `FirebaseAuth.getInstance().currentUser`.
   - Si no hay sesión activa, navega a la ruta `"login"`.
   - Si hay sesión activa, resuelve el perfil operativo, fija el vehículo y rol en `AppPreferences` y entra directamente a la pantalla principal sin volver a pedir credenciales.
   - La barra lateral incluye la opción de **"Cerrar Sesión"** con diálogo de confirmación, ejecutando `auth.signOut()` y redirigiendo a `"login"` limpiando el historial de navegación.

---

## 11. Roles Operativos, Privacidad de Datos y Cierre de Caja Ciego RP3
1. **Roles y Privacidad Estricta (`RolUsuario`)**:
   - **`OPERADOR_RUTA` (Teléfono de Ruta - Camión)**:
     - Chofer y ayudante en ruta.
     - **Regla estricta de privacidad**: Se ocultan completamente los costos de planta, utilidad bruta, utilidad neta y el efectivo esperado en caja durante el arqueo para evitar manipulaciones o presiones indebidas.
   - **`ADMIN_ROLANDO` (Camioneta / Administrador)**:
     - Protegido por PIN de 4 dígitos (`1234` configurable).
     - Acceso al **Panel Financiero de Rolando** y la **Auditoría de Caja RP3**.
2. **Cierre de Caja Ciego Estilo "RP3 Gasolinera" (`CierreCajaEntity` + `CierreCajaDialog`)**:
   - Al terminar la jornada, el chofer/operador abre **"🔒 Realizar Cierre de Caja (Fin de Jornada)"**.
   - Solo declara lo que contó físicamente:
     - `efectivoDeclarado`: Billetes y monedas contados.
     - `transferenciasDeclaradas`: Suma de comprobantes bancarios verificados.
     - `observaciones`: Novedades del día.
   - Al confirmar, el sistema NO le revela si sobra o falta dinero; solo muestra `"Total Declarado a Entregar a Rolando: $XX.XX"` y permite enviar el resumen formal por WhatsApp a Rolando.
   - En el perfil de Rolando (Administrador), la tarjeta **Auditoría RP3** audita automáticamente en tiempo real:
     - Declarado por Ruta vs Sistema (Ventas reales - Gastos).
     - Diferencia: 🔴 Faltante, 🟢 Cuadrado ($0.00), 🔵 Sobrante.
     - Costos de planta, Utilidad Bruta y Utilidad Neta.

---

## 12. Función "Cliente No Estaba en Casa" y Rediseño de Calendario de Ruta
1. **Registro de Visita Fallida con Evidencia Fotográfica (`VisitaFallidaEntity`)**:
   - Botón directo **"🚪 No estaba"** en `CalendarioRutaScreen` y `ClientesScreen`.
   - Abre la cámara del dispositivo mediante `FileProvider` para capturar la fachada/puerta como evidencia física.
   - Registra el cliente hoy como **"⚠️ Visitado - No estaba en casa (Hora)"** y lo sincroniza en tiempo real en Firestore.
   - Lanza WhatsApp automáticamente al cliente con la foto adjunta (`Intent.ACTION_SEND` con `EXTRA_STREAM`) y el mensaje prellenado de cortesía para coordinar una segunda visita o entrega más tarde.
2. **Rediseño Visual del Calendario de Ruta (`CalendarioRutaScreen.kt`)**:
   - Selector estilizado en píldoras compactas para Lunes a Sábado con contador de clientes y distintivo "HOY".
   - Barra de progreso visual segmentada: Atendidos (Verde), No estaban (Ámbar), Pendientes (Gris).
   - Filtros rápidos en chips: `Todos`, `Pendientes`, `Atendidos`, `No estaban`.
   - Tarjetas de cliente con estado de entrega visual claro y 2 botones de acción inferiores: **"💧 Entregar Agua"** y **"🚪 No estaba (Foto)"**, más acceso a WhatsApp.

---

## 13. Menú Lateral (ModalNavigationDrawer), Dieta de Texto y UI Minimalista
1. **Menú Lateral Ejecutivo (`ModalNavigationDrawer`)**:
   - Accesible desde el ícono de menú `☰` en el `TopAppBar`.
   - **Header del Drawer**: Ilustración vectorial destacada del vehículo activo (`CamionIllustration` / `CamionetaIllustration`), nombre del rol activo, indicador `🟢 En línea (Firebase)` y botón directo **"🔄 Cambiar Vehículo"**.
   - **Ítems del Menú Lateral**:
     1) `📅 Ruta del Día (Calendario)`
     2) `👥 Clientes`
     3) `🚚 Inventario y Recargas`
     4) `💵 Caja y Cierre Diario`
     5) `📊 Panel de Rolando (Auditoría y Utilidades)` *(Visible de manera exclusiva cuando el perfil activo es Camioneta / Rolando)*.
     6) `💾 Respaldo y Ajustes`
2. **Dieta de Texto y Tarjetas Colapsables Minimalistas**:
   - En `CalendarioRutaScreen` y `ClientesScreen`, las tarjetas muestran a primera vista exclusivamente:
     - Nombre del cliente y Barrio/Sector corto.
     - Pastillas compactas de color: Deuda (`💲 $X.XX`), Envases adeudados (`🍾 X`), Estado de atención o visita fallida hoy.
     - Fila de 4 botones de acción directos: `[💧 Vender / Re-entregar]`, `[🚪 No estaba]`, `[🧭 Mapa]` y `[💬 WhatsApp]`.
   - Al tocar la tarjeta, se despliegan suavemente los detalles secundarios: foto de fachada con ampliador, tipo de bidón preferido, precio unitario negociado, teléfono, días de ruta y botón `[📍 Guardar / Editar GPS]`.

---

## 14. Geolocalización GPS Offline-First ($0.00), Navegación Google Maps Directa y Notificaciones Push Locales
1. **Ubicación GPS a Costo $0.00 (`GpsLocationHelper.kt` + `GuardarUbicacionDialog.kt`)**:
   - Integración con el sensor GPS nativo del dispositivo (`LocationManager`) con permisos `ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION`.
   - Permite al chofer fijar las coordenadas exactas de la casa en 1 toque al estar estacionado frente a ella, o pegar un enlace de Google Maps / WhatsApp (con extractor automático de coordenadas `lat,lng`).
   - Si el cliente ya tiene ubicación registrada, al presionar `[🧭 Mapa]`, abre directamente Google Maps en modo navegación guiada punto a punto: `google.navigation:q=${lat},${lng}&mode=d`.
   - Si no tiene ubicación, abre instantáneamente el diálogo para capturarla.
2. **Notificaciones Locales en Tiempo Real entre Vehículos (`RutaNotificationHelper.kt`)**:
   - Canal de alta prioridad (`IMPORTANCE_HIGH`) con sonido y vibración en Android 8.0+.
   - En `FirestoreSyncManager`, cuando ingresa una nueva transacción de venta realizada por el OTRO vehículo después de iniciar la aplicación (`fecha >= appStartTimestamp && vehiculo != miVehiculo`), dispara una notificación en la barra de estado del celular:
     - *"🚨 Ruta Actualizada: [🛻 CAMIONETA / 🚚 CAMIÓN] acaba de atender a [Nombre Cliente] ([Hora])."*

---

## 15. Rediseño Visual Estilo Duolingo, Navegación Exclusiva por Drawer y Firma "Aqua Apolo"
1. **Navegación Exclusiva por Menú Lateral (`ModalNavigationDrawer`)**:
   - Se removió por completo la barra inferior (`bottomBar` en `Scaffold`) para maximizar el área de visualización en pantallas móviles y evitar redundancia.
   - Navegación centralizada vía Drawer (`☰`) con opciones claras, íconos grandes y etiquetas oficiales:
     - `📅 Ruta del Día`
     - `👥 Clientes`
     - `🚚 Inventario`
     - `💵 Caja y Cierre`
     - `📊 Panel de Rolando (Administrador)` *(Exclusivo para rol Administrador)*
     - `⚙️ Ajustes`
2. **Rediseño Táctil Estilo Duolingo (Ultra-Visual y Ergonómico en Movimiento)**:
   - Tarjetas de cliente en `CalendarioRutaScreen.kt` y `ClientesScreen.kt` con pastillas de datos rápidos (`💲 $X.XX`, `🍾 X`, `✅ Atendido`, `🚪 No estaba`).
   - Botones grandes redondeados (`shape = RoundedCornerShape(14.dp)`, altura 44–46.dp) con paleta de alto contraste y respuesta táctil:
     - 💧 **Vender** (`#22C55E` Verde vibrante) con ícono de gota de agua.
     - 🚪 **No estaba** (`#F59E0B` Ámbar vibrante / `#D97706`) con ícono de cámara fotográfica para captura de evidencia.
     - 🧭 **Mapa GPS** (`#0284C7` Azul técnico) para navegación punto a punto en Google Maps.
     - 💬 **WhatsApp** (`#25D366` Verde WhatsApp oficial) para contacto y envío de recibo o estado de cuenta.
3. **Firma Oficial y Marca "Aqua Apolo" en WhatsApp**:
   - En `WhatsAppHelper.kt` y `RutaAguaRepository.kt`, todos los comprobantes, estados de cuenta, avisos de visita fallida y reportes diarios concluyen con la firma oficial:
     `"Gracias por confiar en nosotros. Atentamente, Aqua Apolo 💧🚚"`.
4. **Privacidad Operativa y Arqueo Ciego RP3 por Perfil**:
   - Perfil `CAMION`: Interfaz simplificada sin acceso a costos unitarios de planta, utilidades ni descuadres de caja. El chofer realiza su cierre ciego declarando valores físicos y envía el balance a Rolando.
   - Perfil `CAMIONETA` (Rolando): Acceso total a auditorías, balances detallados, utilidad bruta/neta y control del negocio.

---

## 16. Autenticación con Firebase Authentication, Asignación Dinámica de Roles y Pantalla de Login Duolingo
1. **Cuentas y Reglas de Asignación por Correo Electrónico (`resolverPerfilPorEmail`)**:
   - **`rolando@rutaatenea.app`**:
     - Rol: `ADMIN_ROLANDO`.
     - Habilita acceso total al `PanelRolandoScreen` (utilidad neta, ventas brutas, costos unitarios de agua en planta y auditoría del Cierre RP3).
     - Permite alternar entre visualización Consolidada, Camión o Camioneta.
   - **`camion@rutaatenea.app`**:
     - Rol: `OPERADOR_RUTA` con vehículo predeterminado `"CAMION"`.
     - Oculta completamente costos de planta, márgenes de ganancia y balances confidenciales.
     - Habilita exclusivamente el Cierre de Caja Ciego RP3 y el reporte operacional de botellones.
   - **`camioneta@rutaatenea.app`**:
     - Rol: `OPERADOR_RUTA` con vehículo predeterminado `"CAMIONETA"`.
     - Mismas restricciones de confidencialidad que el camión, asociado a la camioneta de reparto.
2. **Pantalla de Inicio de Sesión (`ui/auth/LoginScreen.kt`)**:
   - Estética Duolingo / Banking UI con bordes redondeados táctiles (`RoundedCornerShape(14.dp)`).
   - Encabezado: Título "Aqua Apolo • Atenea", subtítulo "Distribución de Agua Purificada" e ícono vectorial en degradado azul marino.
   - Chips rápidos de autocompletado en 1 toque: `[🚚 Camión]`, `[🛻 Camioneta]`, `[👑 Rolando]` para evitar escribir correos largos en carretera.
   - Campos de texto de alto contraste con soporte para alternar visibilidad de contraseña y teclado optimizado.
   - Botón principal de acción "Iniciar Sesión" (54.dp, ergonómico) con spinner circular reactivo durante la llamada a Firebase y mensajes claros en español ante errores de credenciales o falta de red.
3. **Gestión de Sesión Persistente y Logout en Navigation Drawer**:
   - Al arrancar la aplicación, se evalúa `FirebaseAuth.getInstance().currentUser`. Si existe sesión, se configuran las preferencias locales y navega directo a la pantalla principal sin volver a solicitar credenciales.
   - En `ModalNavigationDrawer`, el botón **"Cerrar Sesión"** despliega un diálogo de confirmación antes de ejecutar `auth.signOut()`, limpiando la pila de navegación y regresando a la pantalla de login.
   - En `FirestoreSyncManager.kt`, se eliminó `signInAnonymously()`; los listeners en tiempo real se inicializan dinámicamente mediante `AuthStateListener` únicamente cuando se detecta un usuario autenticado verificado.

---

## 17. Sistema de Actualizaciones Automáticas OTA (Over-The-Air) con GitHub Releases
1. **Endpoint Oficial**:
   - Conectado a la API pública de GitHub Releases: `https://api.github.com/repos/anthonymcs/Ruta-agua-camion/releases/latest`.
   - Incluye cabecera HTTP `User-Agent: AteneaApp-Android` y soporte dual para respuesta estándar GitHub API y formato json directo.
2. **Comparación Inteligente de Versiones**:
   - Extrae el tag de versión (ej. `v1.1.0`), parsea semánticamente `versionName` y evalúa `versionCode` contra `BuildConfig.VERSION_CODE` y `BuildConfig.VERSION_NAME`.
   - Si detecta una versión superior, activa el diálogo interactivo con las notas del parche.
3. **Flujo de Descarga e Instalación Desatendida**:
   - Descarga en segundo plano utilizando `DownloadManager`.
   - Receptor broadcast dinámico (`ACTION_DOWNLOAD_COMPLETE`) que captura la finalización y lanza el instalador oficial de Android mediante `FileProvider` (`application/vnd.android.package-archive`).
   - Requiere permiso `REQUEST_INSTALL_PACKAGES` y rutas declaradas en `file_paths.xml`.
