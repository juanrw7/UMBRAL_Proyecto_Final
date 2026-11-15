# Resultados de ejecución · UMBRAL v5

Se usaron JackCompiler 2.7 y el compilador Online por terminal. Los cuatro .vm distribuidos son del compilador de escritorio. Las huellas SHA-256 identifican los fuentes y binarios. El respaldo v4 no se alteró.

## Comparación en el VM Emulator gráfico

Se compiló el mismo v5 con tres valores de espera. Desde (80,197), hubo 41 muestras con →, 75 sin tecla (150 px) y una más hasta el rebote. Después se verificó el frenado. Los tres ensayos usaron la interfaz y el OS integrado oficiales con el mismo controlador.

| Espera solicitada | Tiempo medido en 117 actualizaciones | Muestras por segundo |
|---:|---:|---:|
| 28 ms | 3,660 s | 31,96 |
| **24 ms** | **3,071 s** | **38,10** |
| 22 ms | 2,921 s | 40,06 |

La configuración final utiliza 18 ms por actualización, elegida después de pruebas manuales en Nand2Tetris Online. Las mediciones anteriores corresponden a valores de 28, 24 y 22 ms. Entre 28 y 24 ms se observó una reducción de 0,589 s (16,1 %) en el tiempo de recorrido de 117 actualizaciones. La configuración de 18 ms no cuenta con una medición cronometrada equivalente. La física permanece igual y el cambio incrementa la frecuencia de actualización visual.

## Partidas y dibujo

- **Lógica.** 38 verificaciones de arranque, controles, freno, límites, rebotes, colisiones, abertura y pulsación de Espacio.
- **Partidas.** La ruta de fase con dos pulsos de frenado separados y liberación de → ganó en 848 muestras. Las variantes de altura y=125 y y=139 también ganaron. Una partida con fallos en las entregas 1, 3 y 5 llegó a 50 con un intento restante.
- **Derrota y motivo de rearme.** Se conservaron los cuatro intentos globales, la derrota en el cuarto fallo y el marcador si se pierde con puntos. Mantener → al lado de la pared sigue sin provocar descuentos extra ni dar puntos. Se reinició en ambos resultados en el VM gráfico y en el motor web.
- **Avisos.** La marca y el mensaje de retroceso obligatorio permanecen visibles hasta que la cápsula regresa a x≤288. Al rearmarse, la marca desaparece y el aviso LISTO PARA REINTENTAR permanece durante 93 actualizaciones (aproximadamente 1,67 s de espera nominal con la configuración final de 18 ms, más el procesamiento). Los registros gráficos anteriores corresponden a la configuración de 70 actualizaciones y se conservan como evidencia histórica. La física y el movimiento de la abertura continúan durante los avisos.
- **HUD.** Se compararon las palabras fijas del panel y el margen durante avisos y entregas. Las regiones antes ocupadas por ayudas y marcas quedaron en blanco, incluido el reinicio desde ambos resultados. En las capturas Swing se cotejaron los bits visibles con la RAM de video.
- **Ejecución continua.** 8.504 muestras, 8 victorias, 8 derrotas y 16 reinicios sin cargar otra vez. La pila, las referencias y el balance de asignaciones quedaron estables en el ensayo con OS en VM. Se observaron 464 reservas y 262 liberaciones, diferencia de 202, igual a la inicial. Esta cifra incluye reservas del OS.

Los controladores son herramientas externas. El de recuperación selecciona fases mediante cálculos y los demás reproducen entradas de teclado. Ninguno modifica los datos de la partida para conseguir una victoria. Las modificaciones de RAM utilizadas para preparar estados aislados están identificadas en el código de pruebas. Las capturas y los registros de las configuraciones anteriores se conservan como evidencia histórica. La versión definitiva con `Sys.wait(18)` y `retryTicks = 93` también fue probada manualmente en Nand2Tetris Online. La demostración presencial en el equipo de clase permanece pendiente.

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
