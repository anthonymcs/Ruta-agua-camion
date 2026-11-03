# Atenea - Sistema de Distribución y Gestión de Rutas de Agua Purificada

**Atenea** (anteriormente RutaAgua) es una aplicación Android nativa diseñada para la operación en campo y administración integral de empresas de distribución de agua purificada en camiones y camionetas de reparto, con enlace directo a planta de embotellado y control contable para gerencia.

---

## 📱 Contexto y Propósito del Proyecto

El sistema resuelve la operación diaria de dos vehículos de ruta (**Camión Principal** de 250 bidones y **Camioneta de Reparto** de 90 bidones) y el control administrativo del dueño (**Rolando**):
1. **Choferes en Ruta:** Registro ágil de ventas cliente por cliente, control de botellones (azules, PET transparente, marcadas), fiados, préstamos de envases, cortesías y recargas en planta.
2. **Liquidación y Cierre de Caja:** Cierre de jornada con arqueo ciego estilo RP3 (el chofer declara su dinero sin ver el cálculo del sistema para evitar faltantes encubiertos) y reporte de botellones entregados.
3. **Gerencia y Contabilidad (Rolando):** Panel financiero confidencial con ventas brutas, deducción de costos de agua en planta, control de gastos, cálculo de utilidad neta, división de semanas del mes y auditoría cruzada.
4. **Sincronización en Tiempo Real:** Enlace bidireccional mediante Firebase Cloud Firestore para que las ventas y recargas de los choferes se reflejen instantáneamente en el teléfono de Rolando y viceversa.

---

## 🛠️ Stack Tecnológico y Arquitectura

- **Lenguaje:** Kotlin 2.0+ (100% moderno, corrutinas y Flow reactivo).
- **Interfaz de Usuario:** Jetpack Compose con Material 3 y sistema de diseño personalizado **Claymorphism** y **Banking UI** (inspirado en la app del Banco Pichincha).
- **Arquitectura:** MVVM (Model-View-ViewModel) + Repository Pattern + Clean Architecture.
- **Base de Datos Local:** Room Database (SQLite) versión 9, con índices optimizados y soporte de exportación/importación de backups en JSON.
- **Sincronización en la Nube:** Firebase Cloud Firestore (modo offline/online con listeners en tiempo real).
- **Notificaciones y Mensajería:** Integración nativa con WhatsApp para envío de comprobantes de venta, alertas de crédito y reportes de cierre.
- **Geolocalización:** GPS Location Helper nativo para registrar coordenadas exactas de clientes y optimizar rutas.

---

## 🚀 Módulos y Pantallas de la Aplicación

### 1. Ventas y Caja (`RegistroDiaScreen` / `NuevaVentaSheet`)
- **Venta a Clientes Registrados o de Paso:** Permite vender a clientes habituales o clientes ocasionales no registrados ("Doña Martita") guardando su nombre en la transacción sin obligar a crearlos en la base de datos.
- **Precios Flexibles de Bidones 20L:** Botones rápidos de precios habituales ($0.75, $0.80, $0.85, $0.90, $1.00) y botón "Personalizar $" para ingresar cualquier valor libre por bidón.
- **Precios Editables de Productos Secundarios:** Precios unitarios editables al vuelo en cada venta para:
  - Galón 4L ($0.75 sugerido)
  - Paca de 24 botellas de 500ml ($3.75 sugerido)
  - Mini de 12 botellas ($1.90 sugerido)
- **Desglose Multi-tipo de Botellones:**
  - 🔵 Bidones Azules
  - ⚪ Bidones PET Transparentes
  - 🏷️ Bidones con Marca Específica
  - 📦 Envases Nuevos Vendidos ($5.50 - $6.00)
  - ⚠️ Bidones Prestados (el cliente debe el botellón)
  - 🔄 Bidones Devueltos (el cliente devuelve botellones prestados anteriormente)
  - 🎁 Botellones de Cortesía (ej. promoción 35+1, descuenta del camión a $0.00)
- **Formas de Pago Combinadas:** Efectivo, Transferencia bancaria, Crédito (fiado hoy), Abono a deuda previa o Cancelación total de deudas.
- **Compartir Cuentas Bancarias:** Envío con 1 clic hacia WhatsApp de los datos de cuenta bancaria (Pichincha, Produbanco, Guayaquil, etc.) para que el cliente realice la transferencia.
- **Alerta de Créditos Pendientes:** Detección visual y alerta si un cliente tiene deuda con más de 2 días sin cobrar.

### 2. Carga e Inventario del Vehículo (`InventarioCamionScreen`)
- **Stock Actual Real en Unidad:** Muestra exactamente los botellones y productos que tiene el vehículo en el momento actual (ej. 68 azules), eliminando acumulaciones artificiales de ventas pasadas.
- **Carga Lista para Iniciar el Día:** La carga dejada al final del día anterior se toma automáticamente como la carga inicial de hoy ("Carga del día anterior / Dejada lista"), evitando tener que reingresar datos en la mañana.
- **Recargas en Planta Dinámicas:** Cargas secuenciales (`Carga #1`, `Carga #2`, `Carga #3`...). Al llegar a planta, el sistema calcula cuántos botellones faltan para completar el tope por producto (ej. 88/90 galones -> faltan 2).
- **Auditoría de Cargas:** Posibilidad de editar o eliminar cargas registradas previamente (con control de acceso).

### 3. Clientes y Cartera (`ClientesScreen` / `HistorialClienteSheet`)
- **Apodo / Alias vs Nombre Formal:** Permite asignar un apodo o alias al cliente (ej. "Don Pepe", "La Vecina"). En las pantallas de la app se muestra el apodo para rapidez del chofer, mientras que en los comprobantes y WhatsApp se usa el nombre formal registrado.
- **Historial Completo:** Registro cronológico de compras, deudas, abonos y saldo de botellones prestados.

### 4. Ruta y Calendario (`CalendarioRutaScreen`)
- **Planificación Semanal:** Asignación de clientes por días (Lunes a Sábado) y frecuencia de visita.
- **Despacho Cruzado Inteligente:** Si un vehículo realiza la entrega programada del otro (ej. la camioneta atiende a un cliente del camión por cercanía), se registra la notificación operativa y queda marcado en la ruta del día como "Realizado por Camioneta".
- **Clientes de Llamada:** Inclusión ágil de pedidos imprevistos dentro del calendario del día.

### 5. Reporte de Bidones para Choferes (`ReportesCamionetaScreen`)
- **Enfoque 100% Operacional:** Libre de datos contables confidenciales. El chofer solo ve lo referente a botellones.
- **Selector Día / Semanal / Mensual:** Con selector de meses históricos.
- **Resumen de Botellones:** Total 20L, PET Transparentes, Azules, Marcadas, Envases Nuevos, Prestados, Devueltos, Cortesías, Galones, Pacas y Minis.
- **Sección "¿A quién se le dejó los bidones?":** Lista detallada con cliente, hora y desglose exacto de lo entregado.
- **Botón Compartir por WhatsApp:** Envía el resumen de botellones entregados a los choferes o despacho.

### 6. Panel Contable y Control Financiero (`PanelRolandoScreen`)
- **Exclusivo para Rolando (PIN 1234):** Pantalla directiva y confidencial.
- **Períodos Contables:** Día, Semanal y Mensual (con navegación histórica mes a mes).
- **Selector de Unidad:** Consolidado (Camión + Camioneta), Camión Principal o Camioneta.
- **Estado de Resultados Contables:**
  - Ventas Brutas
  - Deducción de Costo de Agua en Planta (configurado por producto)
  - Deducción de Gastos de Ruta
  - Utilidad Bruta
  - **UTILIDAD NETA** (con indicador de rentabilidad: 🟢 Rentable / 🔴 Déficit)
- **Flujo de Fondos y Arqueo:** Efectivo cobrado, Transferencias bancarias, Créditos otorgados, Abonos recaudados y Efectivo Neto en Caja.
- **División por Semanas del Mes:** En modo mensual, divide automáticamente el mes en Semana 1, Semana 2, Semana 3, Semana 4 y Semana 5, con ventas, créditos, abonos, gastos y neto por semana, más el **Gran Total Mensual Acumulado** al pie.
- **Clientes por Precio de Agua:** Desglose exacto de cuántos clientes compraron a cada tarifa registrada ($0.80, $0.75, $0.85, $0.90, $1.00) y el total facturado por tarifa.
- **Control Detallado de Gastos:** Lista de egresos de combustible, alimentación, repuestos, etc.
- **Gráfico de Evolución Financiera:** Gráfico visual de barras comparando ingresos vs gastos.
- **Auditoría Cierre RP3:** Comparación en vivo de lo declarado a ciegas por el chofer vs lo calculado por el sistema.

---

## 🗄️ Esquema de Entidades (Room Database)

1. `TransaccionVentaEntity`
   - `id`, `vehiculo`, `clienteId`, `nombreClientePaso`, `fecha`, `fechaTexto`
   - `tipoBidon`, `cantidadBidonesRecarga`, `precioUnitarioBidon`
   - `cantidadBidonesPrestados`, `cantidadBidonesDevueltos`, `cantidadBidonesVendidosEnvase`, `precioUnitarioBidonVendido`
   - `recargaTransparentes`, `recargaAzules`, `recargaMarcados`
   - `prestadosTransparentes`, `prestadosAzules`, `prestadosMarcados`
   - `envasesVendidosTransparentes`, `envasesVendidosAzules`, `envasesVendidosMarcados`
   - `cantidadBidonesGratis`
   - `cantidadGalones`, `precioUnitarioGalon`
   - `cantidadPacas`, `precioUnitarioPaca`
   - `cantidadMinis`, `precioUnitarioMini`
   - `totalImporte`, `formaPagoPrincipal`, `montoEfectivo`, `montoTransferencia`, `montoCredito`, `montoAbono`, `montoCanceladoDeuda`, `nota`

2. `ClienteEntity`
   - `id`, `nombre`, `alias`, `telefono`, `direccion`, `referenciaUbicacion`, `latitud`, `longitud`
   - `tipoPrecioAgua`, `precioEspecial`, `tipoBidonPreferido`
   - `saldoPendienteCredito`, `bidonesPrestados`
   - `diasEntrega` (Lunes a Sábado), `activo`

3. `InventarioCamionEntity`
   - `vehiculo` (CAMION / CAMIONETA), `capacidadMaxima`
   - `stockBidonesAzules`, `stockBidonesTransparentes`, `stockBidonesMarcados`
   - `stockGalones`, `stockPacas`, `stockMinis`, `stockEnvasesNuevos`
   - `fechaUltimaActualizacion`

4. `RecargaPlantaEntity`
   - `id`, `vehiculo`, `numeroVuelta`, `fecha`, `fechaTexto`, `horaTexto`
   - `bidonesAzulesCargados`, `bidonesTransparentesCargados`, `bidonesMarcadosCargados`
   - `galonesCargados`, `pacasCargadas`, `minisCargadas`, `envasesNuevosCargados`

5. `GastoExtraEntity`
   - `id`, `vehiculo`, `fecha`, `fechaTexto`, `concepto`, `categoria`, `monto`, `comprobanteNumero`

6. `CierreCajaEntity`
   - `id`, `vehiculo`, `fechaTexto`, `fechaHoraMillis`
   - `efectivoDeclarado`, `transferenciaDeclarada`, `gastosDeclarados`
   - `totalSistemaEfectivo`, `totalSistemaTransferencia`, `totalSistemaGastos`
   - `diferenciaEfectivo`, `diferenciaTransferencia`, `cierreCuadrado`, `observaciones`

---

## 🔐 Cuentas de Firebase Authentication y Asignación de Roles

El sistema utiliza **Firebase Authentication** (Email/Password) con asignación dinámica de roles y vehículo:
- **Rolando (Administrador General):**
  - Correo: `rolando@rutaatenea.app`
  - Rol asignado: `ADMIN_ROLANDO`
  - Acceso total a `PanelRolandoScreen` (utilidad neta, ventas brutas, costos unitarios de planta, auditoría RP3 y vista Consolidada).
- **Camión Principal:**
  - Correo: `camion@rutaatenea.app`
  - Rol asignado: `OPERADOR_RUTA` con vehículo predeterminado `CAMION`.
  - Capacidad: 250 unidades.
  - Privacidad estricta (oculta costos y utilidades de planta). Cierre ciego RP3.
- **Camioneta de Reparto:**
  - Correo: `camioneta@rutaatenea.app`
  - Rol asignado: `OPERADOR_RUTA` con vehículo predeterminado `CAMIONETA`.
  - Capacidad: 90 unidades.
  - Mismas restricciones de confidencialidad que el camión.

---

## 🎯 Control de Distribución y Versiones (GitHub Releases & OTA Updates)

- **Actualizaciones Automáticas OTA (Over-The-Air):**
  - Conectado a la API pública de **GitHub Releases** (`/releases/latest`).
  - Detección automática al arrancar la app o desde el menú de Ajustes.
  - Al detectar una versión superior en GitHub (ej. `v1.1.0`), muestra diálogo interactivo con notas del parche y botón **"Actualizar Ahora"**.
  - Descarga en segundo plano con `DownloadManager` e instalación desatendida mediante `FileProvider`.

- **Firebase App Distribution (Opcional):**
  - Soporte para subida de builds de prueba a evaluadores (`./gradlew appDistributionUploadDebug`).

---

## 🔨 Comandos de Compilación y Git

```powershell
# Compilar y verificar tests unitarios
.\gradlew.bat test --console=plain

# Generar APK de depuración
.\gradlew.bat assembleDebug --console=plain
# Ubicación del APK resultante: app/build/outputs/apk/debug/app-debug.apk

# Subida obligatoria al repositorio Git con soporte SSL Windows
git add .
git commit -m "feat: implementacion completa de requerimientos operativos y financieros"
git -c http.sslBackend=schannel push github main
```
