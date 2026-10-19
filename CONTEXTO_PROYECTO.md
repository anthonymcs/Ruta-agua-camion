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
