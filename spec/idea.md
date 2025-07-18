... some ideas ...


The natural word size of the Lynx 8-bit machine will be ... 8 bits! The immediate issue with an 8-bit computer is addressing memory. With 8 bits we can address 2**8 or 256 unique addresses ... Ouch ! 8-bit machines from the ancient times solves this by having a 16-bit address bus with an accompanying a 16-bit program counter. That's cheating! We are 8-bit purist and such crap won't fly here.

For the Lynx 8-bit machine, everything which can be 8-bit will be 8-bit - even the address bus. That means we're stuck with with only being able to address 256 bytes of memory. To make this slighlty more bearable the Lynx 8-bit machine will not be a Von Neumann machine, but rather a Harvard machine. That means there will be two seperate addressable 256 byte long memory segments. One will hold the instructions. Another will hold data. Its not a lot, but hopefully enough for a couple of toy programs. 

If we are able to pack each instructions into a single byte then the maximum length of a single program will be 256 instructions. In some sense this proposes an interesting constraint that will be fun to work around.

So how many registers should the Lynx 8-bit machine support? If we are to pack each instruction into a byte then we don't have a lot to work with. If we allocate 4 of our 8 bits to registers, we can only represent 2**4 or 16 unique instructions. That may be just about enough for a minimal set of instructions.

That leaves us with 4 bits to specify the registers. If we want to be able to specify two operands then thats 2 bits for each register. That leaves us with a maximum of 4 registers. For a simple machine like this, that may be enough. However, while we now have specified the two operands, we have not specifed the output register. Well, we don't really have space for that. Instead, one of the 4 registers will function as an implicit accumulator register where the output is always stored.

Let's call our 4 registers r0, r1, r2, r3. r0 will be the special accumulator register. We must also have a program counter register, which we call pc, which tells us where in the instruction stream to fetch from.

As for the instructions, we must be careful as we are pressed for space. Our set of 16 instructions need to cover the basics. Data movement. Arithmetic. Control flow.

TODO: Incredibly clean semantics.

labels immediates

idea: variable-length opcodes. Means we can store immediates better.

Data movemnt:
1. load  r1, r2  # load value at r2 into r1
2. store r1, r2 # store value in r1 to r2

Arithmetic
1. add r1, r2
2. sub r1, r2
3. and r1, r2
4. or  r1, r2
5. not r1, r2

Control flow:
1. jmp r1 # uncondtional
2. jiz r1, r2 # jump if r2 is zero

3. halt


Okay, we need some instructions that take immediates.
Ideas:
- Pack immediate in the available 4 bits
- Instructions that take immediate take up 2 bytes. Problem: need seperate add, addi, sub, subi, etc. We could extend this idea to go full variable-length encoding and use more bits for the opcode. Idk.
- if both register operands are 0b00 (r0) and 0b00 (r0), then should load the next byte as an immediate? That would mean we don't need a seperate add and addi.
