# Rutas verificadas · UMBRAL v5

## Victoria observando la abertura

1. Desde x=80, sube hasta quedar un poco por encima de la mitad de la cámara (y≈132). Frena con toques cortos de ↓ hasta que deje de subir. Pulsa una sola flecha a la vez.
2. Con la cápsula quieta, espera el **borde superior** de la abertura en ascenso. En entregas 1 y 2, lanza poco después de que empiece a subir desde el punto más bajo de su recorrido (y≈135). En las entregas 3 a 5, lanza cerca del tercio superior de ese recorrido (y≈88). Usa el movimiento del hueco como referencia.
3. Impulsa con →. Una vez que la cápsula avanza hacia la derecha, suelta la tecla y deja que el resto del cruce ocurra por inercia. En una medición anterior realizada en el VM Emulator con `Sys.wait(24)`, las 117 actualizaciones del cruce tomaron aproximadamente 3,07 s. La versión final utiliza `Sys.wait(18)`, cuyo tiempo real de recorrido no se cronometró de forma equivalente.
4. En un fallo, suelta → y deja que el rebote te aleje de la pared. Puedes reforzarlo con ←. Al cruzar la marca temporal, el aviso cambia a **LISTO PARA REINTENTAR**. Si el primer lanzamiento no entra, ajusta el momento del siguiente.

La prueba por fase usó ↑ durante 31 muestras, ↓ durante 1, soltó 3 y pulsó ↓ durante 1. Se detuvo en y=132 y salió hacia la derecha cuando el borde superior, en ascenso, llegó a y=135 para las primeras dos entregas y y=88 para las demás. Impulsó durante 41 muestras y soltó durante 76 para cada cruce. Llegó a 50 en 848 muestras. También ganaron dos rutas con y=125 y y=139, y otra que se recuperó de tres fallos.

## Secuencia exacta desde 00

Cada fila empieza con ↑ durante 33 muestras y ↓ durante 2. Después espera el número indicado y pulsa → durante 117. Son actualizaciones de juego y no pulsaciones individuales.

| Entrega | Espera tras frenar | Altura de salida del hueco / sentido | Ciclo acumulado | Puntaje |
|---:|---:|---|---:|---:|
| 1 | 53 | 136 / sube | 205 | 10 |
| 2 | 10 | 134 / sube | 367 | 20 |
| 3 | 9 | 90 / sube | 528 | 30 |
| 4 | 9 | 88 / sube | 689 | 40 |
| 5 | 9 | 86 / sube | 850 | 50 |

Si avanzaste siete actualizaciones sin mover la cápsula antes de empezar, resta siete de la primera espera. Estimar la fase es más práctico que contar muestras en una demostración manual.

## Derrota y reinicio

Desde 00 no cambies la altura. Mantén → hasta el primer impacto. El contador baja de **4 a 3**. Suelta → durante **12 muestras**: al llegar a x=288 aparece LISTO. Mantén → **15 muestras** para regresar al muro. Repite el retiro y la aproximación hasta completar los otros tres choques. Se ve **4 → 3 → 2 → 1 → 0**, con el puntaje preservado. Tras ganar o perder, suelta las teclas y pulsa **Espacio**. Se restablecen origen, fase 64, puntaje 00 y cuatro intentos.
