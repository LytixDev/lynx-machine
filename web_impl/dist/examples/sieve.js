"use strict";
const sieveExample = `// set r1 to loop max
li 0b1000
shift 4
mv r1 r0

// set r2 to be 2, the loop counter
li 2
mv r2 r0

// do-while loop counter less than loop max
li 1
store r0 r2
inc r2
sub r1 r2
mv r3 r0
// skip loop
li 2
jiz r0 r3
// continue loop
jmp -7


// main sieve algorithm

// multiplier (r1 in inner loop), max_loop (r2), inner loop counter (r3 in inner loop)), p (spilled to data at max_loop)

// init p to 2 and spill to data[max_loop]

li 2
store r0 r2

// @ 15 / 0x10
// outer loop: do-while p is less than max_loop

// load p
load r3 r2

// list of found primes
// i spilled to max_loop + 1
// list of primes spilled to max_loop + 16

// load i @ max_loop + 1:
mv r0 r2
inc r0
load r1 r0
// max_loop + 16 + i
li 4
shift 2
add r0 r1
add r0 r2
load r3 r2
store r3 r0
// store i + 1 @ max_loop + 1
mv r0 r2
inc r0
inc r1
store r1 r0

// if r3 (loop counter) > r2 (max loop) then we're done
ge r3 r2
dec r0
mv r3 r0
li 0b1000
shift 2
jiz r0 r3

// **if prime:
// set multiple: r1 to be p + p (which must be loaded)
load r0 r2
add r0 r0
mv r1 r0

// * INNER LOOP : while multiple < max_loop: data[multiple] = false, multiple += p
ge r2 r1
// NOOP, remove later
dec r3
mv r3 r0
// to ***inner loop done 10
li 0b1011
jiz r0 r3

// ***inner loop cont.d
//    data[multiple] = 0 (not prime)
//    multiple += p
jmp 2
jmp -6
li 0
store r0 r1
load r0 r2
add r0 r1
mv r1 r0
// jump back to inner loop start
jmp -6

// ***inner loop done



// trampoline back to outer loop start
jmp 2
jmp -8

// **if not prime: load p, find next p, jump back
load r0 r2
inc r0
store r0 r2
// if data[p] is 1 then this is the next p
// TODO: if p > r2 (max_loop) then stop
load r3 r0
dec r3
li 2
jiz r0 r3
jmp -7

// jump back to outer loop start
li 15
mv r3 r0
li 0
jaiz r3 r0
`;
