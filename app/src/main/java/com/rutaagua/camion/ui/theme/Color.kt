package com.rutaagua.camion.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// PALETA MARINA PROFESIONAL (TONOS MARINOS SUAVES, EQUILIBRADOS Y LEGIBLES)
// Sin saturación excesiva ni bloques oscuros pesados; contraste limpio tipo Banca
// ============================================================================

// Tonos Marinos Principales (Camión - Azul Marino Oceánico Equilibrado)
val MarineOcean = Color(0xFF0A4D68)
val MarineOceanDark = Color(0xFF07364B)
val MarineOceanLight = Color(0xFF088395)
val MarineAquaAccent = Color(0xFF7DD3FC) // Aqua Marino claro (contraste > 8:1 con MarineOceanDark)
val MarineMistCanvas = Color(0xFFF0F6FA)
val MarineSoftSurface = Color(0xFFE1EFF7)

// Tonos Marinos Costeros (Camioneta - Turquesa Marino Sereno)
val MarineLagoon = Color(0xFF0E7490)
val MarineLagoonDark = Color(0xFF0B4F6C)
val MarineLagoonLight = Color(0xFF38BDF8)
val MarineLagoonCanvas = Color(0xFFF1F8FA)
val MarineLagoonSoft = Color(0xFFDDF2F7)

// Aliases de sistema para compatibilidad con componentes
val PichinchaNavy = MarineOcean
val PichinchaNavyDark = MarineOceanDark
val PichinchaYellow = MarineAquaAccent
val PichinchaYellowDark = Color(0xFF38BDF8)
val PichinchaCanvas = MarineMistCanvas

val NavyDeep = Color(0xFF072A3E)
val AguaPrimary = MarineOcean
val AguaPrimaryDark = MarineOceanDark
val AguaPrimaryLight = MarineOceanLight

val AguaSecondary = MarineLagoon
val AguaSecondaryDark = MarineLagoonDark
val AguaSecondaryLight = MarineLagoonLight

// Paleta por Vehículo (Camión vs Camioneta)
val CamionClayCanvas = MarineMistCanvas
val CamionClayPrimary = MarineOcean
val CamionClayPrimaryDark = MarineOceanDark
val CamionClaySoft = MarineSoftSurface

val CamionetaClayCanvas = MarineLagoonCanvas
val CamionetaClayPrimary = MarineLagoon
val CamionetaClayPrimaryDark = MarineLagoonDark
val CamionetaClaySoft = MarineLagoonSoft

// Colores específicos para tipos de bidones (Tonos marinos / coral legibles sin saturar)
val BidonTransparenteColor = Color(0xFF0284C7) // PET Cristal Marino
val BidonTransparenteReflejo = Color(0xFFBAE6FD)
val BidonTransparenteFondo = Color(0xFFF0F9FF)

val BidonAzulColor = Color(0xFF1D4ED8) // Azul Oceánico
val BidonAzulReflejo = Color(0xFF93C5FD)
val BidonAzulFondo = Color(0xFFEEF5FF)

val BidonMarcadoColor = Color(0xFFD9483B) // Coral Marino Destacado (Marcadas)
val BidonMarcadoTapa = Color(0xFFB91C1C)
val BidonMarcadoFondo = Color(0xFFFFF5F5)

// Estados Financieros y Operativos (Claros y distinguibles)
val VerdeEfectivo = Color(0xFF0D9488) // Esmeralda Marino
val VerdeEfectivoFondo = Color(0xFFF0FDFA)
val RojoAlerta = Color(0xFFD9383A)
val RojoAlertaFondo = Color(0xFFFEF2F2)
val AmbarAdvertencia = Color(0xFFD97706)
val AmbarFondo = Color(0xFFFFFBEB)

// Superficies y Textos (Siempre legibles en cualquier tema del teléfono)
val SuperficieClara = MarineMistCanvas
val SuperficieTarjeta = Color(0xFFFFFFFF)
val ClayInsetBackground = Color(0xFFE8F1F8)
val ClayRimLight = Color(0xFFFFFFFF)
val ClayRimShadow = Color(0xFFCEE0EE)
val FondoNeutralSuave = MarineMistCanvas
val TextoOscuro = Color(0xFF072A3E) // Azul Marino Profundo para textos nítidos
val TextoSecundario = Color(0xFF476278) // Gris Azulado Marino legible
val BordeSuave = Color(0xFFD5E4F0)

// Aliases adicionales
val AzulCorporativo = MarineOcean
val BordeSutil = BordeSuave
val FondoGrisClaro = FondoNeutralSuave
val GrisCard = ClayInsetBackground
val RojoDeuda = RojoAlerta
val SuperficieBlanca = SuperficieTarjeta
