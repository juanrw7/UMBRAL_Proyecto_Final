import Hack.Controller.HackController;
import Hack.VMEmulator.VMEmulator;
import Hack.VMEmulator.VMProgramGUI;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationTargetException;
import SimulatorsGUI.VMEmulatorComponent;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Pruebas del VM de producción con las herramientas oficiales 2.7.
 * La partida y las pruebas de una flecha escriben solo el registro de teclado
 * (modo batch) o envían eventos al teclado oficial (modo gráfico).
 */
public final class VerificarUmbral {
    private static final String[] STEP = {"vmstep"};
    private static final String SAMPLE = "Game.run.10"; // Última instrucción antes de call Keyboard.keyPressed.
    private static final int LEFT=130, UP=131, RIGHT=132, DOWN=133;
    private final Path out;
    private final boolean visual, builtin;
    private String finishMessage;
    private VMEmulator vm;
    private VMEmulatorComponent gui;
    private int game, capsule, gate, passed, activeKey, cycles;
    private long instructions, allocations, releases;

    private VerificarUmbral(File program, Path out, boolean visual, boolean builtin) throws Exception {
        this.out=out;
        this.visual=visual;
        this.builtin=builtin;
        Files.createDirectories(out);
        if (visual) {
            SwingUtilities.invokeAndWait(() -> {
                gui=new VMEmulatorComponent(){
                    @Override public VMProgramGUI getProgram(){
                        VMProgramGUI original=super.getProgram();
                        if(!builtin)return original;
                        // Responde al diálogo de selección del OS y registra su mensaje final.
                        return (VMProgramGUI)Proxy.newProxyInstance(VMProgramGUI.class.getClassLoader(),
                            new Class<?>[]{VMProgramGUI.class},(proxy,method,args)->{
                                if(method.getName().equals("confirmBuiltInAccess"))return true;
                                if(method.getName().equals("notify")){
                                    finishMessage=(String)args[0];
                                    return null;
                                }
                                try{return method.invoke(original,args);}
                                catch(InvocationTargetException e){throw e.getCause();}
                            });
                    }
                };
                vm=new VMEmulator(gui);
                var frame=new JFrame("UMBRAL | Nand2Tetris 2.7 VM Emulator");
                frame.setContentPane(gui);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1020,620);
                frame.setLocation(0,0);
                frame.setVisible(true);
            });
        } else vm=new VMEmulator();
        vm.setAnimationMode(HackController.NO_DISPLAY_CHANGES);
        vm.setWorkingDir(program);
        vm.doCommand(new String[]{"load"});
        untilSample(3_000_000);
        check(atSample(),"Arranque");
        game=read(3);
        capsule=read(game);
        gate=read(game+1);
    }

    private boolean atSample() throws Exception {
        return "Game.run".equals(vm.getValue("currentFunction")) && SAMPLE.equals(vm.getValue("line"));
    }

    private void untilSample(int maximum) throws Exception {
        for(int i=0;i<maximum;i++){
            vm.doCommand(STEP);
            instructions++;
            String f=vm.getValue("currentFunction");
            check(finishMessage==null,"El emulador notificó final o error: "+finishMessage);
            if(f!=null && (f.startsWith("Sys.error")||f.startsWith("Sys.halt")))
                throw new AssertionError("El OS entro en "+f);
            if("Memory.alloc".equals(f) && "Memory.alloc.0".equals(vm.getValue("line")))allocations++;
            if("Memory.deAlloc".equals(f) && "Memory.deAlloc.0".equals(vm.getValue("line")))releases++;
            if("Game.run".equals(f) && SAMPLE.equals(vm.getValue("line")))return;
        }
        throw new AssertionError("No se alcanzo la siguiente muestra de teclado");
    }

    private int read(int address) throws Exception {
        return Integer.parseInt(vm.getValue("RAM["+address+"]"));
    }
    private void write(int address,int value) throws Exception {
        vm.setValue("RAM["+address+"]",Integer.toString(value));
    }
    private int x() throws Exception {return read(capsule);}
    private int y() throws Exception {return read(capsule+1);}
    private int vx() throws Exception {return read(capsule+2);}
    private int vy() throws Exception {return read(capsule+3);}
    private int top() throws Exception {return read(gate);}
    private int direction() throws Exception {return read(gate+1);}
    private int height() throws Exception {return read(gate+2);}
    private int score() throws Exception {return read(game+2);}
    private int attempts() throws Exception {return read(game+3);}
    private int mode() throws Exception {return read(game+4);}
    private boolean armed() throws Exception {return read(game+5)!=0;}
    private int retryTicks() throws Exception {return read(game+7);}
    private boolean spaceReady() throws Exception {return read(game+6)!=0;}
    private boolean state(int x,int y,int vx,int vy) throws Exception {
        return x()==x && y()==y && vx()==vx && vy()==vy;
    }
    private static void check(boolean ok,String name){
        if(!ok) throw new AssertionError(name);
    }
    private void pass(String name,boolean ok){
        check(ok,name);
        System.out.printf("OK %02d | %s%n",++passed,name);
    }
    private static int awt(int key) {
        return switch(key) {
            case 32 -> KeyEvent.VK_SPACE;
            case LEFT -> KeyEvent.VK_LEFT;
            case UP -> KeyEvent.VK_UP;
            case RIGHT -> KeyEvent.VK_RIGHT;
            case DOWN -> KeyEvent.VK_DOWN;
            default -> 0;
        };
    }
    private void key(int key) throws Exception {
        if(!visual){write(24576,key);return;}
        if(key!=activeKey){
            SwingUtilities.invokeAndWait(()->{
                var button=gui.getKeyboard().getKeyEventHandler();
                for(var listener:button.getKeyListeners()){
                    if(activeKey!=0)listener.keyReleased(new KeyEvent(button,KeyEvent.KEY_RELEASED,
                        System.currentTimeMillis(),0,awt(activeKey),activeKey==32?' ':KeyEvent.CHAR_UNDEFINED));
                    if(key!=0)listener.keyPressed(new KeyEvent(button,KeyEvent.KEY_PRESSED,
                        System.currentTimeMillis(),0,awt(key),key==32?' ':KeyEvent.CHAR_UNDEFINED));
                }
            });
            activeKey=key;
        }
        check(read(24576)==key,"El teclado Swing no genero el codigo esperado");
    }
    private void tick(int key) throws Exception {
        key(key);
        untilSample(1_300_000);
        cycles++;
        if(visual){
            SwingUtilities.invokeAndWait(()->((javax.swing.JComponent)gui.getScreen()).paintImmediately(0,0,516,260));
            if(!builtin)Thread.sleep(24);
        }
    }
    private void ticks(int n,int key) throws Exception {
        for(int i=0;i<n;i++)tick(key);
    }

    private boolean gatePixels() throws Exception {
        int start=top(), end=start+height();
        for(int y=51;y<=230;y++){
            int word=read(16384+y*32+20);
            if(word!=(y>=start && y<end?0:255))return false;
        }
        return true;
    }
    private boolean bounds() throws Exception {
        int t=top(), h=height(), d=direction();
        return x()>=34 && x()<=312 && y()>=51 && y()<=223 &&
            Math.abs(vx())<=2 && Math.abs(vy())<=2 && t>=64 && t<=144 &&
            h>=32 && h<=48 && Math.abs(d)>=1 && Math.abs(d)<=2 &&
            (t!=64 || d>0) && (t!=144 || d<0);
    }
    private int pixel(int x,int y) throws Exception {
        return (read(16384+y*32+x/16) >>> (x%16)) & 1;
    }
    private BufferedImage ramImage() throws Exception {
        var image=new BufferedImage(512,256,BufferedImage.TYPE_INT_RGB);
        for(int y=0;y<256;y++){
            for(int w=0;w<32;w++){
                int bits=read(16384+y*32+w);
                for(int k=0;k<16;k++)image.setRGB(w*16+k,y,
                    (bits & (1<<k))==0?0xffffff:0);
            }
        }
        return image;
    }
    private void capture(String base) throws Exception {
        var screen=ramImage();
        ImageIO.write(screen,"png",out.resolve(base+".png").toFile());
        if(!visual)return;
        var window=new BufferedImage(1018,611,BufferedImage.TYPE_INT_RGB);
        SwingUtilities.invokeAndWait(()->{
            Graphics2D g=window.createGraphics();
            gui.printAll(g);
            g.dispose();
        });
        Component c=(Component)gui.getScreen();
        var loc=c.getLocation();
        for(int y=0;y<256;y++)for(int x=0;x<512;x++)
            check(((screen.getRGB(x,y)&255)<128)==((window.getRGB(loc.x+2+x,loc.y+2+y)&255)<128),
                "El fotograma Swing no coincide con la RAM de video en "+x+","+y);
        ImageIO.write(window,"png",out.resolve(base+"_Swing.png").toFile());
    }

    private void prepare(int x,int y,int vx,int vy,int top,int dir,int height) throws Exception {
        check(!visual,"Un caso aislado no debe usar el modo visual");
        write(capsule,x);write(capsule+1,y);write(capsule+2,vx);write(capsule+3,vy);
        write(gate,top);write(gate+1,dir);write(gate+2,height);
        write(game+3,4);write(game+5,-1);
    }
    private boolean initial() throws Exception {
        return state(80,197,0,0)&&score()==0&&top()==64&&height()==48&&direction()==1
            &&attempts()==4&&armed()&&mode()==0&&retryTicks()==0;
    }
    private void logic() throws Exception {
        pass("Origen (80,197), cuatro intentos globales y hueco inicial",initial()&&gatePixels());
        tick(0);
        pass("Hueco avanza sin teclado",top()==65&&state(80,197,0,0)&&gatePixels());
        tick(LEFT);
        pass("Izquierda",state(79,197,-1,0));
        tick(0);
        pass("Inercia",state(78,197,-1,0));
        tick(RIGHT);
        pass("Derecha frena",state(78,197,0,0));
        tick(RIGHT);
        pass("Derecha impulsa",state(79,197,1,0));
        tick(UP);
        pass("Arriba impulsa sin cambiar vx",state(80,196,1,-1));
        tick(DOWN);
        pass("Abajo frena",state(81,196,1,0));
        tick(DOWN);
        pass("Abajo impulsa",state(82,197,1,1));
        prepare(200,190,2,2,100,1,48);tick(RIGHT);
        pass("Saturacion positiva en X",state(202,192,2,2));
        prepare(200,190,-2,-2,100,1,48);tick(LEFT);
        pass("Saturacion negativa en X",state(198,188,-2,-2));
        prepare(200,190,0,2,100,1,48);tick(DOWN);
        pass("Saturacion positiva en Y",state(200,192,0,2));
        prepare(200,190,0,-2,100,1,48);tick(UP);
        pass("Saturacion negativa en Y",state(200,188,0,-2));
        prepare(35,150,-2,1,100,1,48);tick(0);
        pass("Rebote izquierdo sin perder intento",state(34,151,2,1)&&attempts()==4);
        prepare(100,52,1,-2,100,1,48);tick(0);
        pass("Rebote superior sin perder intento",state(101,51,1,2)&&attempts()==4);
        prepare(100,222,1,2,100,1,48);tick(0);
        pass("Rebote inferior sin perder intento",state(101,223,1,-2)&&attempts()==4);
        prepare(35,52,-2,-2,100,1,48);tick(0);
        pass("Rebote en esquina superior izquierda",state(34,51,2,2));
        for(int h:new int[]{48,44,40,36,32}){
            int speed=h>=44?1:2;
            int start=100+speed;
            prepare(311,start-1,2,0,100,speed,h);tick(0);
            pass("Hueco "+h+": sobresale un pixel por arriba, rebota sin anotar",state(312,start-1,-2,0)&&score()==0&&attempts()==3);
            prepare(311,start+h-7,2,0,100,speed,h);tick(0);
            pass("Hueco "+h+": sobresale un pixel por abajo, rebota sin anotar",state(312,start+h-7,-2,0)&&score()==0&&attempts()==3);
        }
        prepare(80,197,0,0,143,2,32);tick(0);
        pass("Paso 2 corrige e invierte en 144",top()==144&&direction()==-2);
        prepare(80,197,0,0,65,-2,32);tick(0);
        pass("Paso 2 corrige e invierte en 64",top()==64&&direction()==2);
        prepare(310,101,2,0,100,1,48);tick(0);
        pass("Primer borde inclusivo, +10 y reaparicion",score()==10&&state(80,197,0,0)&&height()==44);
        tick(0);
        pass("Sin anotacion duplicada al siguiente ciclo",score()==10);
        prepare(310,141,2,0,100,1,48);write(game+3,3);tick(0);
        pass("Ultima fila inclusiva valida, sin restaurar el intento perdido",score()==20&&state(80,197,0,0)&&attempts()==3);
        // Espacio ya sostenido cuando el ultimo choque ocurre por inercia.
        write(game+3,1);
        prepareOnlyBody(310,197,2,0);
        tick(32);
        pass("Espacio al entrar en derrota se consume; el resultado sigue visible",mode()==2&&attempts()==0&&!spaceReady());
        ticks(5,32);
        pass("Espacio sostenido no salta el resultado",mode()==2&&!spaceReady());
        tick(0);
        pass("Soltar espacio arma una nueva pulsacion",mode()==2&&spaceReady());
        tick(32);
        pass("Nueva pulsacion restaura origen, intentos, fase y puntaje",initial()&&!spaceReady()&&gatePixels()&&minimal());
        tick(32);
        pass("Espacio sostenido en partida nueva no vuelve a reiniciar",top()==65&&mode()==0);
        tick(0);
        tick(32);
        pass("Espacio durante juego activo no reinicia",top()==67&&mode()==0&&!spaceReady());
    }
    
    private void prepareOnlyBody(int x,int y,int vx,int vy) throws Exception {
        write(capsule,x);write(capsule+1,y);write(capsule+2,vx);write(capsule+3,vy);
    }

    private void negative(int key) throws Exception {
        int hits=0;
        for(int i=1;i<=3000;i++){
            int before=attempts();
            tick(key);
            if(attempts()<before)hits++;
            check(mode()==0 && score()==0 && bounds(), "Estado invalido en "+i);
            check(gatePixels(),"Pared incorrecta en ciclo "+i);
            if(key==RIGHT||key==LEFT)check(y()==197&&vy()==0,"Deriva vertical");
            else check(x()==80&&vx()==0,"Deriva horizontal");
        }
        pass("KBD="+key+" durante 3000 ciclos, cero puntos, pared y limites verificados",hits==(key==RIGHT?1:0));
        pass("Descuentos: "+hits+"; intentos restantes: "+attempts(),attempts()==4-hits);
    }

    private int ink(int x0,int y0,int x1,int y1) throws Exception {
        int count=0;
        for(int y=y0;y<=y1;y++)for(int x=x0;x<=x1;x++)count+=pixel(x,y);
        return count;
    }

    private void win(int up,int[] waits,boolean evidence) throws Exception {
        long timestamp=System.nanoTime();
        int aimedY=197-2*up;
        int start=cycles;
        for(int stage=1;stage<=5;stage++){
            ticks(up,UP);
            ticks(2,DOWN);
            check(state(80,aimedY,0,0)&&armed()&&attempts()==4,"Alineacion o restauracion incorrecta");
            ticks(waits[stage-1],0);
            int launch=top(), dir=direction();
            for(int n=1;n<=117;n++){
                tick(RIGHT);
                check(bounds()&&gatePixels(),"Dibujo o fisica incorrecta en "+cycles);
                if(n<117)check(attempts()==4&&score()==(stage-1)*10,"Se adelanto un acierto o hubo rebote");
            }
            int height=stage<5?48-4*stage:32;
            check(state(80,197,0,0)&&attempts()==4&&height()==height&&score()==stage*10,"Acierto incorrecto "+stage);
            pass("Entrega "+stage+": "+score()+" puntos, [80,197], hueco "+height,mode()==(stage==5?1:0));
            System.out.printf("TRAZA | etapa=%d | y=%d | arriba=%d | abajo=2 | espera=%d | derecha=117 | salida=%d/%d | puertaEnContacto=%d | ciclo=%d%n",
                stage,aimedY,up,waits[stage-1],launch,dir,top(),cycles-start);
            if(evidence&&(stage==2||stage==5))capture(stage==2?"Entrega_20":"Victoria_50");
        }
        pass("Victoria 50 con titulo y aviso de espacio", mode()==1&&score()==50&&ink(336,55,472,65)>80&&ink(32,242,300,252)>120);
        double seconds=(System.nanoTime()-timestamp)/1e9;
        if(visual)System.out.printf(java.util.Locale.ROOT,"Victoria Swing: %d ciclos, %.2f s, %.1f ciclos/s con comprobaciones%n",cycles-start,seconds,(cycles-start)/seconds);
    }

    private void lose(boolean evidence) throws Exception {
        int begin=score(), budget=attempts();
        ticks(117,RIGHT);
        pass("Choque 1: rebota en x=312 y consume exactamente un intento",
            state(312,197,-2,0)&&attempts()==budget-1&&!armed()&&score()==begin);
        if(evidence)capture("Rebote_3_intentos");
        for(int hit=2;hit<=budget;hit++){
            ticks(11,0);
            check(x()==290&&!armed(),"Se rearmo antes de alejarse 24 pixeles");
            tick(0);
            check(x()==288&&armed(),"No se rearmo al llegar a 24 pixeles");
            ticks(15,RIGHT);
            pass("Choque "+hit+": rearme y descuento unico; puntaje "+begin,
                state(312,197,-2,0)&&score()==begin&&attempts()==budget-hit&&!armed());
        }
        pass("Derrota estable con contador 0 y aviso de espacio",mode()==2&&retryTicks()==0&&rearmMark()==0&&ink(336,55,503,65)>50&&ink(32,242,300,252)>120);
        if(evidence)capture("Derrota");
    }

    private void restart(boolean evidence) throws Exception {
        int oldMode=mode(),oldScore=score(),oldTop=top();
        ticks(3,0);
        pass("Resultado congelado sin entradas",mode()==oldMode&&score()==oldScore&&top()==oldTop);
        tick(32);
        pass("Espacio reinicia sin recargar VM",initial()&&!spaceReady()&&gatePixels()&&minimal());
        if(evidence)capture(oldMode==1?"Reinicio_Tras_Victoria":"Reinicio_Tras_Derrota");
        ticks(6,32);
        pass("Espacio mantenido avanza seis ciclos normales; no vuelve a reiniciar",top()==70&&mode()==0&&score()==0&&!spaceReady());
        tick(0);
        pass("Teclado liberado habilita otra pulsacion",spaceReady()&&top()==71);
    }

    private void camping() throws Exception {
        ticks(33,UP);ticks(2,DOWN);
        ticks(117,RIGHT);
        pass("Aproximacion fuera de fase pierde un intento",state(312,131,-2,0)&&attempts()==3&&!armed());
        int openContacts=0;
        for(int i=0;i<320;i++){
            tick(RIGHT);
            if(x()==312&&vx()<0&&y()>=top()&&y()<=top()+height()-8)openContacts++;
            check(attempts()==3&&!armed()&&score()==0&&mode()==0,"Camping desconto o anoto indebidamente");
        }
        pass("Bloqueado junto a la pared, incluso en "+openContacts+" contactos geometricamente abiertos",openContacts>0);
    }

    private void recover() throws Exception {
        ticks(33,UP);ticks(2,DOWN);ticks(117,RIGHT);
        check(attempts()==3&&!armed(),"No se registro el primer choque");
        ticks(12,0);ticks(2,RIGHT);
        pass("Retiro fisico a x=287, rearme y frenado horizontal",state(287,131,0,0)&&armed()&&attempts()==3);
        ticks(27,0);ticks(13,RIGHT);
        pass("Reintento legitimo: 10 puntos, conserva 3 de los 4 intentos",score()==10&&state(80,197,0,0)&&armed()&&attempts()==3);
        lose(false);
        pass("Derrota posterior conserva los 10 puntos",mode()==2&&score()==10);
        restart(false);
    }

    private void session(int rounds) throws Exception {
        int sp=read(0);
        long balance=allocations-releases;
        if(visual)capture("Arranque_00");
        lose(visual);
        restart(visual);
        // El reinicio muestreado termina con la puerta en t=7 (espacio seguido de liberacion).
        win(33,new int[]{46,10,9,9,9},visual);
        restart(visual);
        for(int i=0;i<rounds;i++){
            lose(false);restart(false);
            win(33,new int[]{46,10,9,9,9},false);restart(false);
            check(read(0)==sp&&read(game)==capsule&&read(game+1)==gate,"Cambio de pila u objetos");
            if(!builtin)check(allocations-releases==balance,"Balance de heap distinto");
        }
        pass("Misma pila y mismos punteros de Game, Capsule y Gate",read(0)==sp&&read(3)==game&&read(game)==capsule&&read(game+1)==gate);
        if(!builtin)pass("Balance de bloques asignados constante: "+balance+
            " (reservas="+allocations+", liberaciones="+releases+")",allocations-releases==balance);
        System.out.printf("PARTIDAS EN UNA CARGA: %d victorias, %d derrotas y %d reinicios%n",
            rounds+1,rounds+1,2*(rounds+1));
    }
    
    // Variante de demostracion: dos toques separados, fase estimada en la pared
    // desnuda y recorrido por inercia tras soltar derecha.
    private void coarse() throws Exception {
        int[] launchPhase={135,135,88,88,88};
        for(int stage=0;stage<5;stage++){
            ticks(31,UP);
            tick(DOWN);
            ticks(3,0);
            tick(DOWN);
            check(state(80,132,0,0),"No se estabilizo la altura");
            int mark=launchPhase[stage], waited=0;
            while(!(direction()<0 && top()<=mark && top()>=mark-4) && waited<160){tick(0);waited++;}
            check(waited<160,"No aparecio la ventana de salida");
            int start=top();
            ticks(41,RIGHT);
            check(state(161,132,2,0),"No avanzo el tramo inicial");
            ticks(76,0);
            pass("Fase estimada, dos pulsos separados y avance por inercia: "+score()+" puntos (salida "+start+")",
                score()==10*(stage+1)&&attempts()==4&&state(80,197,0,0)&&gatePixels());
        }
        pass("Partida con ventanas y deriva deliberada llega a 50",mode()==1&&score()==50);
        if(visual)capture("Victoria_Ventana");
    }

    private long footer() throws Exception {
        long signature=0;
        for(int row=242;row<=252;row++)for(int col=0;col<32;col++)
            signature=31*signature+read(16384+row*32+col);
        return signature;
    }

    // Firma de la parte fija del panel: se excluyen los cuatro caracteres
    // que cambian y la fila reservada para el resultado.
    private long panel() throws Exception {
        long signature=0;
        for(int row=0;row<=231;row++){
            check((read(16384+row*32+20)&0xff00)==0,"Se invadio el margen entre camara y panel");
            if(row>=55&&row<=65)continue;
            for(int col=21;col<32;col++){
                int pixels=read(16384+row*32+col)&0xffff;
                if(col==25&&row>=11&&row<=32)pixels&=0xff;
                if(col==26&&((row>=11&&row<=21)||(row>=33&&row<=43)))pixels&=0xff00;
                signature=31*signature+pixels;
            }
        }
        return signature;
    }

    private int rearmMark() throws Exception { return ink(288,43,288,47); }
    private boolean minimal() throws Exception {
        return ink(0,0,327,48)==0 && ink(336,44,511,241)==0 &&
            ink(0,233,335,241)==0 && ink(0,242,511,255)==0;
    }

    private void feedback(boolean evidence) throws Exception {
        long controls=footer(), staticPanel=panel();
        pass("HUD visible y sin marcas permanentes, guias, titulo o pie",minimal());
        for (int row=11;row<=43;row+=11)
            check(ink(336,row,455,row+10)>30,"Renglon HUD ausente");
        if(evidence)capture("Panel_Inicial");
        ticks(117,RIGHT);
        long retreat=footer();
        pass("Choque cambia el pie y resta exactamente un intento",retreat!=controls&&retryTicks()==0&&!armed()&&attempts()==3&&rearmMark()==5);
        if(evidence)capture("Aviso_Retrocede");
        int start=top();
        for(int i=0;i<70;i++){
            tick(RIGHT);
            check(footer()==retreat && rearmMark()==5 && !armed() && retryTicks()==0 && attempts()==3 && score()==0,"Desaparecio el aviso de retroceso");
        }
        pass("Retroceso obligatorio visible tras 70 muestras y puerta en movimiento",top()!=start&&gatePixels());
        int elapsed=0;
        while(!armed()&&elapsed++<30)tick(LEFT);
        long retry=footer();
        pass("Rearme borra la marca e inicia un aviso de 93 actualizaciones",retry!=retreat&&retry!=controls&&retryTicks()==93&&armed()&&rearmMark()==0);
        if(evidence)capture("Aviso_Listo");
        long startTime=System.nanoTime();
        start=top();
        int before=x();
        for(int i=1;i<=92;i++){
            tick(i==5?RIGHT:0);
            if(i==5)check(vx()==-1,"El freno no respondio durante el aviso");
            check(footer()==retry&&retryTicks()==93-i,"Aviso cambio antes de tiempo");
        }
        pass("Fase de 93 muestras: la puerta, la capsula y el freno siguen activos",top()!=start&&x()!=before&&gatePixels());
        tick(0);
        pass("Al terminar las 93 muestras el pie queda vacio, sin residuos",footer()==controls&&retryTicks()==0);
        if(visual)System.out.printf(java.util.Locale.ROOT,
            "AVISO MEDIDO | 93 actualizaciones | %.3f s con comprobaciones%n",
            (System.nanoTime()-startTime)/1e9);
        pass("El panel fijo y el margen se conservan y no quedan guias ni avisos",panel()==staticPanel&&minimal());
        if(evidence)capture("Aviso_Vencido");
    }

    private void priority() throws Exception {
        ticks(117,RIGHT);ticks(12,0);ticks(15,RIGHT);
        pass("Un choque antes del vencimiento reemplaza LISTO y vuelve a desarmar",!armed()&&retryTicks()==0&&attempts()==2);
        // La ruta prolongada recorre otros tres choques, incluido el definitivo
        // antes de que venza LISTO. La captura de derrota revisa su mensaje.
        for(int i=0;i<2;i++){ticks(12,0);ticks(15,RIGHT);}
        pass("Derrota tiene prioridad sobre los avisos activos",mode()==2&&retryTicks()==0&&rearmMark()==0&&ink(336,55,503,65)>50);
        // Caso aislado: final exitoso al contactar con un aviso aún activo.
        write(game+4,0);
        prepare(310,120,2,0,100,2,32);
        write(game+2,40);write(game+3,1);write(game+7,40);
        tick(0);
        pass("Victoria tiene prioridad y conserva el intento global restante",mode()==1&&score()==50&&retryTicks()==0&&attempts()==1&&ink(336,55,503,65)>50);
    }

    private int gateAt(int lookahead) throws Exception {
        int p=top(),d=direction();
        for(int i=0;i<lookahead;i++){
            p+=d;
            if(p<=64){p=64;d=-d;}
            if(p>=144){p=144;d=-d;}
        }
        return p;
    }

    private void approach(int length,boolean fail) throws Exception {
        int waited=0;
        while(waited<165){
            int p=gateAt(length);
            int margin=Math.min(y()-p,p+height()-8-y());
            if(fail?margin < -4:margin >= 5)break;
            tick(0);waited++;
        }
        check(waited<165,"No hubo ventana");
        ticks(length,RIGHT);
    }

    private void tough(boolean evidence) throws Exception {
        long staticPanel=panel();
        for(int stage=1;stage<=5;stage++){
            // Dos pulsos de frenado separados. El objetivo varía por entrega.
            int up=(stage%2==0)?30:31;
            ticks(up,UP);tick(DOWN);ticks(3,0);tick(DOWN);
            int before=attempts();
            check(vy()==0,"No se estabilizo");
            if(stage%2==1){
                approach(117,true);
                check(attempts()==before-1&&!armed()&&score()==10*(stage-1),"No se registro el fallo planificado");
                ticks(12,0);ticks(2,RIGHT);
                check(x()==287&&vx()==0&&armed(),"No se rearmo correctamente");
                approach(13,false);
                before--;
            } else approach(117,false);
            pass("Entrega "+stage+": "+score()+" puntos con "+attempts()+" intentos globales",
                score()==10*stage&&attempts()==before&&state(80,197,0,0)&&gatePixels()&&panel()==staticPanel);
            if(evidence&&stage==1)capture("Entrega_10_Con_3_Intentos");
        }
        pass("Victoria incluso con tres fallos: queda un intento",mode()==1&&attempts()==1&&score()==50&&retryTicks()==0&&ink(336,55,503,65)>50);
        if(evidence)capture("Victoria_Con_1_Intento");
    }
    
    private void bench() throws Exception {
        long start=System.nanoTime();
        ticks(41,RIGHT);
        check(state(161,197,2,0),"El impulso no preparó el deslizamiento");
        ticks(75,0);
        check(state(311,197,2,0),"No se conservaron los 150 px de deslizamiento");
        tick(0);
        double elapsed=(System.nanoTime()-start)/1e9;
        pass("117 muestras, 150 px sin tecla y rebote esperado",state(312,197,-2,0)&&attempts()==3);
        ticks(2,RIGHT);
        pass("Dos muestras de la tecla contraria frenan sin alterar la fisica",vx()==0&&x()==311);
        System.out.printf(java.util.Locale.ROOT,"MEDICION_GUI | 117 actualizaciones | %.3f s | %.2f actualizaciones/s%n",elapsed,117/elapsed);
    }

    public static void main(String[] args) throws Exception {
        try {
            String cmd=args[2];
            boolean withGui=cmd.endsWith("Swing")||cmd.equals("builtin");
            var run=new VerificarUmbral(new File(args[0]),Path.of(args[1]),withGui,withGui);
            switch(cmd){
                case "logic" -> run.logic();
                case "feedback","feedbackSwing" -> run.feedback(true);
                case "priority" -> run.priority();
                case "tough","toughSwing" -> run.tough(true);
                case "benchSwing" -> run.bench();
                case "timing","timingSwing" -> run.coarse();
                case "camping" -> run.camping();
                case "recover" -> run.recover();
                case "session" -> run.session(7);
                case "builtin" -> run.session(0);
                case "play" -> {run.win(33,new int[]{53,10,9,9,9},true);run.restart(true);}
                case "alt139" -> {run.ticks(23,0);run.win(29,new int[]{42,14,13,13,13},false);}
                case "alt125" -> {run.ticks(69,0);run.win(36,new int[]{53,3,4,4,4},false);}
                default -> run.negative(Integer.parseInt(cmd));
            }
            System.out.printf("%d verificaciones, %d muestras de teclado, %d instrucciones VM%n",run.passed,run.cycles,run.instructions);
            System.exit(0);
        } catch(Throwable error){
            error.printStackTrace();
            System.exit(1);
        }
    }
}
