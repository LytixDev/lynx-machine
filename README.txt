--- The 8-bit Lynx Machine ---

A simple "true" 8-bit machine for visualizing and experimenting with computation. By “true” 8-bit we informally mean that all components from the registers, data bus, address bus, ALU and to the instruction set are exactly 8 bits wide. It therefore follows that the Lynx Abstract Machine is only able to perform 8-bit arithmetic while being able to address just 256 bytes of memory. Lynx is a primitive machine able to achieve simple computations. The motivation behind the project is to explore what a machine with the previously mentioned constraints would look like and experiment with the possible computational problems it is able to express.

- See https://lynx.lytix.dev for an interactive web-based VM.
- See spec/spec.md or https://lynx.lytix.dev/spec.html for a somewhat formal specification of the Lynx Abstract Machine
- See chisel/ for both a software VM implementation in Scala and a HCL (hardware construction language) implemention in Chisel.

Side quests:
- Simple compiler for a B-like language
    - Mista zosin B integration? (https://github.com/tsoding/b)
- Simple compiler for a forth-like language
- Synthesize Verilog emitted by Chisel on an FPGA?
