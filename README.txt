--- The 8-bit Lynx Machine ---

A simple "true" 8-bit machine for visualizing and experimenting with computation. By “true” 8-bit we informally mean that all components from the registers, data bus, address bus, ALU and to the instruction set are exactly 8 bits wide. It therefore follows that the Lynx Abstract Machine is only able to perform 8-bit arithmetic while being able to address just 256 bytes of memory. Lynx is a primitive machine able to achieve simple computations. The motivation behind the project is to explore what a machine with the previously mentioned constraints would look like and experiment with the possible computational problems it is able to express.

Goals!:
- Specification for an 8-bit abstract machine
- Implementation 0: Software - Interactive and visual web-based VM and assembler 
- Implementation 1: HDL - Chisel

Side quests:
- Simple compiler for a B-like language
    - Mista zosin B integration? (https://github.com/tsoding/b)
- Simple compiler for a forth-like language
- Synthesize HDL implementation on an FPGA?
