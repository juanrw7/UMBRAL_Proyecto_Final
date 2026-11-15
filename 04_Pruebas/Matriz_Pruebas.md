# Matriz de verificación · UMBRAL v5

| Nº | Verificación | Resultado observado | Evidencia en Evidencias/ |
|---:|---|---|---|
| 1 | Compilación | Las cuatro clases compilaron con JackCompiler 2.7 y el compilador oficial Online | Compilacion_Oficial, Compilacion_Web |
| 2 | HUD legible | Los tres renglones tienen contenido y están separados del recinto. El aspecto se revisó en capturas de inicio, puntaje intermedio, derrota y victoria | Avisos_VM, OS_Integrado |
| 3 | Sin guías ni números tutoriales | Se comprobaron blancas las regiones que contenían el título en pantalla, la ayuda derecha, las marcas fijas y el pie. Solo se ve la marca temporal de rearme cuando hace falta | Avisos_VM, Avisos_Swing |
| 4 | Puntaje, entrega e intentos | 00 a 50 en cinco entregas. Los fallos consumen un intento global y anotar no lo repone | Victoria_VM, Victoria_Tres_Fallos_Swing |
| 5 | Física | Cuatro flechas, saturación ±2, rebotes por eje y esquinas, contención y recorrido del hueco | Logica_VM |
| 6 | Inercia | En el cruce de referencia se registraron 150 px sin pulsaciones antes del choque. Las tres cadencias conservaron la trayectoria | Cadencia_28, Cadencia_24, Cadencia_22 |
| 7 | Control y frenado | Dos impulsos opuestos frenaron la cápsula. Hubo victoria con toques separados, pausas y recorrido sin tecla después del impulso | Cadencia_24, Ensayo_Fase_Swing |
| 8 | Victoria | Se llegó a 50 en una ruta base, dos alturas alternativas, una estimación de fase sin guías y una recuperación de tres fallos | Victoria_VM, Variante_125, Variante_139, Ensayo_Fase_Swing, Victoria_Tres_Fallos_Swing |
| 9 | Derrota | 4→3→2→1→0. Probada conservación de 10 puntos en otra corrida | Sesion_Swing, Recuperacion |
| 10 | Espacio | Victoria y derrota reinician origen, fase, puntaje, etapa e intentos. Mantener la tecla no repite el reinicio | Sesion_Swing, Sesion_Motor_Web |
| 11 | Reintento | Aviso y marca persisten mientras no se vuelve a x≤288. LISTO dura 70 muestras. Frenado, puerta y movimiento se mantienen. Terminales tienen prioridad | Avisos_VM, Avisos_Swing, Prioridad_Avisos |
| 12 | Sin solapamientos ni residuos | Margen x328–335 vacío, iguales los píxeles fijos al cambiar números, pie y marca borrados al vencer o rearmar. Tras Espacio desaparecen avisos terminados | Avisos_VM, Sesion_Prolongada |
| 13 | Regresiones de v4 | 3.000 ciclos por cada flecha y sin entrada, sin puntos. La sesión de 8.504 muestras alternó 8 victorias, 8 derrotas y 16 reinicios, con balance de asignaciones estable | Tecla_*, Sesion_Prolongada |

Las pruebas de frontera preparan un estado aislado. Las partidas completas solo alimentan el teclado. La percepción humana de la cadencia y el control de la v5 quedan para el ensayo con teclado físico.
