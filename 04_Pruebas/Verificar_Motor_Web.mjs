// Ejecutar desde el repositorio oficial web-ide tras compilar sus dependencias.
import { readFileSync, readdirSync } from 'node:fs';
import assert from 'node:assert/strict';
import { unwrap } from '@davidsouther/jiffies/result.ts';
import { VM } from './simulator/build/languages/vm.js';
import { Vm } from './simulator/build/vm/vm.js';

const folder=process.argv[2];
const files=readdirSync(folder).filter(f=>f.endsWith('.vm')).map(name=>({
  name,instructions:unwrap(VM.parse(readFileSync(folder+'/'+name,'utf8'))).instructions,
}));
const vm=unwrap(Vm.buildFromFiles(files));
let count=0;
const sample=()=>vm.operation?.op==='call'&&vm.operation.name==='Keyboard.keyPressed';
async function nextSample(){
  for(let i=0;i<100000;i++){
    if(vm.operation?.op==='call'&&vm.operation.name==='Sys.wait'){
      vm.step();
      await new Promise(resolve=>setTimeout(resolve,26));
    } else {
      assert.equal(vm.step(),undefined,'El emulador detuvo la partida');
    }
    if(sample())return;
  }
  throw Error('El juego dejó de consultar el teclado');
}
await nextSample();
const game=vm.memory.THIS;
const field=(index)=>vm.RAM.get(game+index);
const gate=field(1);
const phase=()=>vm.RAM.get(gate);
async function ticks(n,key){
  for(let i=0;i<n;i++){
    vm.Keyboard.set(0,key);
    await nextSample();
    count++;
  }
}
await ticks(117,132);
assert.equal(field(3),3);
for(let i=0;i<3;i++){await ticks(12,0);await ticks(15,132);}
assert.deepEqual([field(2),field(3),field(4)],[0,0,2]);
console.log('Derrota estable, 0 intentos, ciclo',count);
await ticks(1,32);
assert.deepEqual([field(2),field(3),field(4),phase()],[0,4,0,64]);
await ticks(6,32);assert.equal(phase(),70);
await ticks(1,0);
console.log('Espacio reinicia una vez tras perder, ciclo',count);
const waits=[46,10,9,9,9];
for(let n=0;n<5;n++){
  await ticks(33,131);await ticks(2,133);await ticks(waits[n],0);await ticks(117,132);
  assert.deepEqual([field(2),field(3)],[10*(n+1),4]);
  console.log('Puntaje',field(2),'en ciclo',count);
}
assert.equal(field(4),1);
await ticks(1,0);await ticks(1,32);
assert.deepEqual([field(2),field(3),field(4),phase()],[0,4,0,64]);
console.log('Victoria a 50 y partida nueva, sin recargar. Total de muestras:',count);
