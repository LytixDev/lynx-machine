const fibExample = `// initialize fib(0) and fib(1)
#set 0x00 0x00
#set 0x01 0x01

li 2
mv r1 r0

// fib procedure start
// r1 -> the number in the sequence to calculate
// r2 -> the result
mv r3 r1
dec r3
load r2 r3
dec r3
load r0 r3
add r0 r2

// trampoline
jmp 2
jmp -7

store r0 r1
inc r1

jmp -3
`;
