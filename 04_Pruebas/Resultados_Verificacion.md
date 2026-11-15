# Resultados de ejecución · UMBRAL v5

Se usaron JackCompiler 2.7 y el compilador Online por terminal. Los cuatro .vm distribuidos son del compilador de escritorio. Las huellas SHA-256 identifican los fuentes y binarios. El respaldo v4 no se alteró.

## Comparación en el VM Emulator gráfico

Se compiló el mismo v5 con tres valores de espera. Desde (80,197), hubo 41 muestras con →, 75 sin tecla (150 px) y una más hasta el rebote. Después se verificó el frenado. Los tres ensayos usaron la interfaz y el OS integrado oficiales con el mismo controlador.

| Espera solicitada | Tiempo medido en 117 actualizaciones | Muestras por segundo |
|---:|---:|---:|
| 28 ms | 3,660 s | 31,96 |
| **24 ms** | **3,071 s** | **38,10** |
| 22 ms | 2,921 s | 40,06 |

Se eligió 24 ms. El cruce fue 0,589 s (16,1 %) más corto que con 28 ms. El salto posterior a 22 ganó otros 0,150 s, a cambio de un menor intervalo para aplicar la opuesta. La comparación usa una corrida por variante. Mide el mismo deslizamiento a distinta frecuencia, sin cambios al modelo físico. La abertura también se mueve más rápido en tiempo real.

## Partidas y dibujo

- **Lógica.** 38 verificaciones de arranque, controles, freno, límites, rebotes, colisiones, abertura y pulsación de Espacio.
- **Partidas.** La ruta de fase con dos pulsos de frenado separados y liberación de → ganó en 848 muestras. Las variantes de altura y=125 y y=139 también ganaron. Una partida con fallos en las entregas 1, 3 y 5 llegó a 50 con un intento restante.
- **Derrota y motivo de rearme.** Se conservaron los cuatro intentos globales, la derrota en el cuarto fallo y el marcador si se pierde con puntos. Mantener → al lado de la pared sigue sin provocar descuentos extra ni dar puntos. Se reinició en ambos resultados en el VM gráfico y en el motor web.
- **Avisos.** Se comprobó la marca de cinco píxeles durante 70 muestras de retroceso pendiente. Al alcanzar x≤288 se borró la línea y aparecieron 70 muestras de LISTO, con 1,68 s nominales de espera (1,790 s medidos en el ensayo gráfico). Tras la última quedó el pie vacío. La prueba accionó el freno durante el aviso y comprobó que la puerta continuó avanzando.
- **HUD.** Se compararon las palabras fijas del panel y el margen durante avisos y entregas. Las regiones antes ocupadas por ayudas y marcas quedaron en blanco, incluido el reinicio desde ambos resultados. En las capturas Swing se cotejaron los bits visibles con la RAM de video.
- **Ejecución continua.** 8.504 muestras, 8 victorias, 8 derrotas y 16 reinicios sin cargar otra vez. La pila, las referencias y el balance de asignaciones quedaron estables en el ensayo con OS en VM. Se observaron 464 reservas y 262 liberaciones, diferencia de 202, igual a la inicial. Esta cifra incluye reservas del OS.

Los controladores son herramientas externas. El de recuperación escoge fases por cálculo y los demás reproducen teclas. Ninguno escribe datos de la partida para lograr una victoria. Los cambios de RAM de los casos aislados están indicados en el fuente de pruebas. No se usaron capturas viejas. El ensayo con teclado físico de v5 queda pendiente.

## Repetir las pruebas

Con el paquete oficial 2.7 y JDK 17+, desde la raíz del proyecto:

```bash
export N2T_TOOLS=/ruta/nand2tetris/tools
for prueba in logic 0 130 131 132 133 camping recover play alt139 alt125 timing feedback priority tough session; do
  bash 04_Pruebas/verificar.sh "$prueba"
done
# En una sesión gráfica:
for prueba in builtin benchSwing feedbackSwing toughSwing timingSwing; do
  bash 04_Pruebas/verificar.sh "$prueba"
done
```

Para comparar cadencias, crea copias separadas, cambia únicamente el argumento de Sys.wait, recompila y repite benchSwing. El controlador usa el OS en VM en modo sin ventana y el OS integrado cuando abre la GUI. Los archivos locales se escriben en 04_Pruebas/salida_local.

Para Online, clona [el repositorio oficial](https://github.com/nand2tetris/web-ide), ejecuta `npm ci` y `npm run preinstall-cli`. Copia Verificar_Motor_Web.mjs a la raíz de esa copia y compila con `node cli/dist/index.js compile /ruta/01_Codigo_Fuente /ruta/salida`. Ejecuta `node Verificar_Motor_Web.mjs /ruta/salida`.
