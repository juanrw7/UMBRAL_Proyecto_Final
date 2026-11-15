# UMBRAL: Cámara de Inercia

**Juan Manuel Ramírez · Organización de Computadores · Evaluación 4**

Videojuego 2D en Jack para la arquitectura Hack. Guía una cápsula de 8 × 8 hacia la abertura móvil de la pared derecha. Hay **un solo escenario y cinco entregas de 10 puntos**. Cada acierto devuelve la cápsula a (80,197) en reposo. La victoria ocurre en 50. Tienes **cuatro intentos globales**. Al perder el cuarto, el juego conserva el puntaje alcanzado. En ambos resultados, pulsa **Espacio** para empezar otra partida.

## Abrir en Nand2Tetris Online

1. Extrae este ZIP y abre el [Jack Compiler oficial](https://nand2tetris.github.io/web-ide/compiler).
2. Pulsa 📂 y selecciona la carpeta **01_Codigo_Fuente**, donde están los cuatro archivos .jack.
3. Pulsa **Compile → Run**. Si el VM Emulator está pausado, inicia su ejecución. Da foco a su control de teclado.
4. Usa una flecha a la vez. Al ganar o perder, suelta el teclado y pulsa **Espacio**.

La v5 se compiló y ejecutó con el motor del IDE por terminal. También se comprobó en la interfaz gráfica oficial de escritorio. La prueba humana de la versión anterior se hizo con la v4, según informó el estudiante.

## Abrir en el VM Emulator de escritorio

Descarga el [paquete oficial](https://www.nand2tetris.org/software) y verifica que `java -version` funcione. Abre `tools\VMEmulator.bat` en Windows o ejecuta `bash /ruta/nand2tetris/tools/VMEmulator.sh` en Linux/macOS.

En **File → Load Program** selecciona la carpeta **02_Ejecutable_VM**. Acepta usar el OS integrado. Selecciona **No Animation** y **Fast**, pulsa **Run** y haz clic en el icono de teclado del emulador. Si la puerta se mueve pero la cápsula no recibe flechas, recupera ese foco. En Linux/macOS, si no se encuentra el OS integrado, abre con `CLASSPATH=/ruta/nand2tetris/tools bash /ruta/nand2tetris/tools/VMEmulator.sh`. Como respaldo, copia los ocho .vm de `tools/OS` a una copia de la carpeta ejecutable y ajusta la velocidad del emulador. El OS en archivos VM implementa la espera de otra forma.

## Controles y recorrido

| Entrada | Código Hack | Efecto |
|---|---:|---|
| ← · ↑ · → · ↓ | 130 · 131 · 132 · 133 | Cada muestra cambia una componente de velocidad en ±1. Cada eje se limita a −2…2. |
| Soltar | 0 | La velocidad se conserva. Una flecha contraria permite frenar o invertir. |
| Espacio | 32 | Reinicia desde victoria o derrota tras una nueva pulsación. |

Desde el origen, **sube hasta quedar un poco por encima de la mitad de la cámara** y frena con ↓. Con la cápsula quieta, observa el borde superior de la abertura mientras asciende. En las dos primeras entregas, inicia el cruce poco después de que ese borde empiece a subir desde el extremo inferior. En las últimas tres, inicia cuando esté cerca del tercio superior de su recorrido. Las referencias y la secuencia exacta quedan en la guía.

Si chocas contra la pared derecha fuera de la abertura, la cápsula rebota y baja un intento. **Deja que se aleje hasta la marca temporal**, o impúlsala con ←, antes de volver hacia la derecha. Se rearma cuando la esquina izquierda de la cápsula llega a x≤288, a 24 px del contacto. Junto a la pared, los contactos del mismo acercamiento no descuentan ni anotan: retroceder forma parte del reintento. Las demás paredes no consumen intentos. Si aciertas, vuelves al origen con **los intentos que te queden**. Solo una partida nueva los devuelve a cuatro.

El pie muestra **RETROCEDE A LA MARCA PARA REINTENTAR** mientras falta el rearme. Después muestra **LISTO PARA REINTENTAR** durante **70 actualizaciones** (1,68 s nominales), y el pie vuelve a quedar vacío. Otro fallo reemplaza el aviso. La victoria o la derrota muestran siempre el resultado y la instrucción de Espacio.

Consulta la [secuencia verificada de victoria y derrota](04_Pruebas/Secuencia_Demo.md)

## Panel derecho

**PUNTAJE: 00/50**, **ENTREGA: 1/5** e **INTENTOS: 4**. El panel comienza en x=336, junto al recinto que termina en x=327. Solo se borran y reescriben los cuatro caracteres variables. La pequeña marca de x=288 se dibuja mientras falta rearmar y desaparece cuando se habilita otro intento o termina la partida.

## Progresión

| Entrega | Altura de la abertura | Desplazamiento por actualización |
|---:|---:|---:|
| 1 | 48 px | 1 px |
| 2 | 44 px | 1 px |
| 3 | 40 px | 2 px |
| 4 | 36 px | 2 px |
| 5 | 32 px | 2 px |

La fila superior del hueco recorre 64…144. Al alcanzar un extremo, se ajusta la coordenada y se invierte el signo. Se mueve antes de decidir la colisión. Con borde superior g y altura H, las ocho filas de cápsula caben si **g ≤ y ≤ g + H − 8**. El mayor y válido es 184. Desde y=197, una flecha horizontal nunca puede anotar. Solo subir o bajar mantiene x=80 y tampoco llega a la pared.

## Arquitectura y memoria

| Clase | Responsabilidad |
|---|---|
| Main | Construye el juego una vez. |
| Game | Consulta el teclado sin bloquear, administra estados, rearme, intentos, puntaje y dibujo. |
| Capsule | Guarda posición y velocidad, aplica impulsos y resuelve rebotes por eje. |
| Gate | Conserva fase, desplazamiento y altura, y comprueba el paso completo. |

El bucle principal sigue leyendo `Keyboard.keyPressed()` en los tres estados del juego. La configuración final utiliza `Sys.wait(18)`, seleccionada después de realizar pruebas manuales en Nand2Tetris Online para conseguir un movimiento más ágil sin modificar las ecuaciones físicas ni los límites de velocidad. Como referencia, en pruebas gráficas anteriores, un recorrido de 117 actualizaciones tomó 3,660 s con 28 ms, 3,071 s con 24 ms y 2,921 s con 22 ms. Estas mediciones corresponden a configuraciones previas y no representan una medición directa de 18 ms. Con la configuración definitiva, la cápsula y la abertura se actualizan con mayor frecuencia. La pantalla del resultado permanece fija hasta que el usuario presiona espacio. El reinicio reutiliza los tres objetos existentes y restablece los datos de la partida. Durante un ciclo ordinario se redibujan únicamente las regiones que cambian. Las cadenas de avisos se liberan después de mostrarse y el juego no vuelve a invocar constructores al iniciar una nueva partida.

## Recompilar

Desde la raíz del proyecto, ajusta la ruta a tu instalación.

```powershell
& "C:\nand2tetris\tools\JackCompiler.bat" (Resolve-Path .\01_Codigo_Fuente).Path
Copy-Item .\01_Codigo_Fuente\*.vm .\02_Ejecutable_VM\ -Force
```

```bash
bash /ruta/nand2tetris/tools/JackCompiler.sh "$PWD/01_Codigo_Fuente"
cp 01_Codigo_Fuente/*.vm 02_Ejecutable_VM/
```

## Verificación y publicación

- [Matriz de pruebas](04_Pruebas/Matriz_Pruebas.md) y [resultados reproducibles](04_Pruebas/Resultados_Verificacion.md).
- [Auditoría contra Teams y la rúbrica](04_Pruebas/Auditoria_Rubrica.md).
- [manual](03_Documentacion/Manual_Ejecucion.pdf).

## ENLACE DEL VIDEO DE YOUTUBE


