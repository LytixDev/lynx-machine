type numberSystem = "hex" | "decimal";
let numberSystem: numberSystem = "decimal";
let assemblyInput: string = "";
let vmSpeed = 1;

function renderRegisters(): void {
  const regDiv = document.getElementById("registers-visualization");
  if (!regDiv) return;
  regDiv.innerHTML = "";

  const regs = [
    { name: "pc", value: lynxMachine.pc },
    { name: "r0", value: lynxMachine.r0 },
    { name: "r1", value: lynxMachine.r1 },
    { name: "r2", value: lynxMachine.r2 },
    { name: "r3", value: lynxMachine.r3 },
  ];

  for (const reg of regs) {
    const regRow = document.createElement("div");
    regRow.className = "register-display";

    const regName = document.createElement("span");
    regName.className = "register-name";
    regName.textContent = reg.name.toUpperCase();

    const regValue = document.createElement("span");
    regValue.className = "register-value";
    regValue.textContent =
      numberSystem === "hex"
        ? reg.value.toString(16).padStart(2, "0").toUpperCase()
        : reg.value.toString();

    // Make register value clickable for editing
    regValue.style.cursor = "pointer";
    regValue.addEventListener("click", () => {
      editRegister(regValue, reg.name);
    });

    regRow.appendChild(regName);
    regRow.appendChild(regValue);
    regDiv.appendChild(regRow);
  }

  // Display the dissasembled representation of the current instruction
  const pc = lynxMachine.pc;
  const inst = lynxMachine.instructions[pc];
  let dissasembled = dissasembleInstruction(inst);
  const disDiv = document.getElementById("current-disassembly");
  if (disDiv) {
    disDiv.innerHTML = `<span style='color:#888;'>Cycles executed: ${lynxMachine.cyclesExecuted}</span><br><span style='font-weight:bold;'>Current instruction:</span> ${dissasembled}`;
  }
}

function editRegister(regValueSpan: HTMLElement, regName: string): void {
  const currentValue = lynxMachine[regName as keyof LynxMachine] as number;

  const input = document.createElement("input");
  input.type = "number";
  input.value = currentValue.toString();
  input.className = "register-input";

  // Replace register value with input field
  regValueSpan.innerHTML = "";
  regValueSpan.appendChild(input);
  input.focus();
  input.select();

  const finishEdit = (value: number) => {
    // Clamp input so it fits within a single byte
    if (value >= 0 && value <= 255) {
      (lynxMachine as any)[regName] = value;
      if (numberSystem === "hex") {
        regValueSpan.textContent = value
          .toString(16)
          .padStart(2, "0")
          .toUpperCase();
      } else {
        regValueSpan.textContent = value.toString();
      }
    }
  };

  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      const value = parseInt(input.value);
      finishEdit(value);
      render();
    } else if (e.key === "Escape") {
      // Cancel edit -> restore original value
      if (numberSystem === "hex") {
        regValueSpan.textContent = currentValue
          .toString(16)
          .padStart(2, "0")
          .toUpperCase();
      } else {
        regValueSpan.textContent = currentValue.toString();
      }
    }
  });
}

function highlightCurrentInstruction(): void {
  const instructionsGrid = document.getElementById("instructions-grid");
  if (!instructionsGrid) return;

  // Remove previous highlight
  const cells = instructionsGrid.querySelectorAll(".byte-cell");
  cells.forEach((cell) => cell.classList.remove("current-instruction"));

  const currentCell = instructionsGrid.querySelector(
    `[data-index="${lynxMachine.pc}"]`
  ) as HTMLElement;
  if (currentCell) {
    currentCell.classList.add("current-instruction");
  }
}

// NOTE: This is very much hacked together!!
function renderByteArray(array: Uint8Array, containerId: string): void {
  const container = document.getElementById(containerId);
  if (!container) return;

  // Clear existing content but preserve the title
  // TODO: Would it be easier if we just updated the values?
  const title = container.querySelector(".grid-title");
  container.innerHTML = "";
  if (title) {
    container.appendChild(title);
  }

  // Create header row that shows the addresses
  const headerRow = document.createElement("div");
  headerRow.className = "grid-row header-row";

  // Add empty first cell to align with row headers
  const emptyHeader = document.createElement("div");
  emptyHeader.className = "header-cell";
  headerRow.appendChild(emptyHeader);

  for (let col = 0; col < 16; col++) {
    const headerCell = document.createElement("div");
    headerCell.className = "header-cell column-header";
    if (numberSystem === "hex") {
      headerCell.textContent = `0x${col
        .toString(16)
        .padStart(2, "0")
        .toUpperCase()}`;
    } else {
      headerCell.textContent = col.toString();
    }
    headerRow.appendChild(headerCell);
  }
  container.appendChild(headerRow);

  // Create the 16x16 grid
  for (let row = 0; row < 16; row++) {
    const gridRow = document.createElement("div");
    gridRow.className = "grid-row";
    // Row header with row offset
    const rowHeader = document.createElement("div");
    rowHeader.className = "header-cell row-header";
    if (numberSystem === "hex") {
      rowHeader.textContent = `0x${(row * 16)
        .toString(16)
        .padStart(2, "0")
        .toUpperCase()}`;
    } else {
      rowHeader.textContent = (row * 16).toString();
    }
    gridRow.appendChild(rowHeader);

    for (let col = 0; col < 16; col++) {
      const index = row * 16 + col;
      const byteValue = array[index];

      const cell = document.createElement("div");
      cell.className = "byte-cell";
      cell.setAttribute("data-index", index.toString());

      if (byteValue === 0) {
        cell.classList.add("zero");
      }

      if (numberSystem === "hex") {
        cell.textContent = byteValue
          .toString(16)
          .padStart(2, "0")
          .toUpperCase();
      } else {
        cell.textContent = byteValue.toString();
      }

      // Add click handler for editing
      cell.addEventListener("click", () => {
        editCell(cell, index, array);
      });

      gridRow.appendChild(cell);
    }
    container.appendChild(gridRow);
  }

  if (containerId === "instructions-grid") {
    highlightCurrentInstruction();
  }
}

// NOTE: This is also very hacky
function editCell(cell: HTMLElement, index: number, array: Uint8Array): void {
  const currentValue = array[index];

  const input = document.createElement("input");
  input.type = "number";
  input.value = currentValue.toString();
  input.className = "cell-input";

  input.style.width = "100%";
  input.style.height = "100%";
  input.style.border = "none";
  input.style.textAlign = "center";
  input.style.fontSize = "16px";
  input.style.fontWeight = "bold";
  input.style.fontFamily = "inherit";
  input.style.backgroundColor = "transparent";

  // Replace cell content with new input field
  cell.innerHTML = "";
  cell.appendChild(input);
  input.focus();
  input.select();

  const finishEdit = (value: number) => {
    // Clamp input so it fits within a single byte
    if (value >= 0 && value <= 255) {
      array[index] = value;
      if (numberSystem === "hex") {
        cell.textContent = value.toString(16).padStart(2, "0").toUpperCase();
      } else {
        cell.textContent = value.toString();
      }

      if (value === 0) {
        cell.classList.add("zero");
      } else {
        cell.classList.remove("zero");
      }
    }
  };

  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      const value = parseInt(input.value);
      finishEdit(value);
    } else if (e.key === "Escape") {
      // Cancel edit -> restore original value
      if (numberSystem === "hex") {
        cell.textContent = currentValue
          .toString(16)
          .padStart(2, "0")
          .toUpperCase();
      } else {
        cell.textContent = currentValue.toString();
      }
    }
  });
}

function toggleNumberSystem(): void {
  numberSystem = numberSystem === "hex" ? "decimal" : "hex";

  // Update button text
  const toggleButton = document.getElementById(
    "display-toggle"
  ) as HTMLButtonElement;
  if (toggleButton) {
    toggleButton.textContent = numberSystem === "decimal" ? "Decimal" : "Hex";
  }

  // re-render everything
  render();
}

function updateLineNumbers() {
  const textarea = document.getElementById(
    "assembly-input"
  ) as HTMLTextAreaElement;
  const lineNumbers = document.getElementById("line-numbers");
  if (!textarea || !lineNumbers) return;

  const lines = textarea.value.split("\n").length || 1;
  let numbers = "";
  for (let i = 1; i <= lines; i++) {
    numbers += `<div>${i}</div>`;
  }
  lineNumbers.innerHTML = numbers;

  // Sync height and scroll position
  lineNumbers.style.height = textarea.offsetHeight + "px";
  lineNumbers.scrollTop = textarea.scrollTop;
}

function exportInstructionsToClipboard() {
  const hexBytes = Array.from(lynxMachine.instructions).map((byte) =>
    byte.toString(16).padStart(2, "0").toUpperCase()
  );

  // Find the last non-zero index
  let lastNonZeroIndex = -1;
  for (let i = hexBytes.length - 1; i >= 0; i--) {
    if (hexBytes[i] !== "00") {
      lastNonZeroIndex = i;
      break;
    }
  }

  let hexInstructions: string;

  // If there are no non-zero bytes, export just "00"
  if (lastNonZeroIndex === -1) {
    hexInstructions = "00";
  } else {
    // Count how many trailing zeros we have after the last non-zero byte
    const totalTrailingZeros = hexBytes.length - 1 - lastNonZeroIndex;
    // Keep one for the final halt instruction
    const zerosToKeep = Math.min(totalTrailingZeros, 1);
    const finalIndex = lastNonZeroIndex + zerosToKeep;
    // Create final hex string without spaces
    hexInstructions = hexBytes.slice(0, finalIndex + 1).join("");
  }

  // Copy to clipboard
  navigator.clipboard
    .writeText(hexInstructions)
    .then(() => {
      // Visual feedback - temporarily change button text
      const exportBtn = document.getElementById(
        "export-instructions-btn"
      ) as HTMLButtonElement;
      if (exportBtn) {
        const originalText = exportBtn.textContent;
        exportBtn.textContent = "Copied!";
        exportBtn.style.backgroundColor = "var(--retro-accent)";
        exportBtn.style.color = "white";

        setTimeout(() => {
          exportBtn.textContent = originalText;
          exportBtn.style.backgroundColor = "";
          exportBtn.style.color = "";
        }, 1000);
      }
    })
    .catch((err) => {
      console.error("Failed to copy instructions to clipboard: ", err);
      alert(
        "Failed to copy to clipboard. Make sure your browser supports clipboard access."
      );
    });
}

function render(): void {
  renderRegisters();
  renderByteArray(lynxMachine.instructions, "instructions-grid");
  renderByteArray(lynxMachine.data, "data-grid");
}

// Initialize everyting then the document loads
// TODO: Better way to do this?
document.addEventListener("DOMContentLoaded", () => {
  // Initial render. render() must be manually called whenever any relevant state changes
  render();

  // Add event listeners
  document
    .getElementById("display-toggle")
    ?.addEventListener("click", toggleNumberSystem);

  document.getElementById("assembly-submit")?.addEventListener("click", () => {
    const assemblyInputDoc = document.getElementById(
      "assembly-input"
    ) as HTMLTextAreaElement;
    if (assemblyInputDoc) {
      const result = assemble(assemblyInputDoc.value);
      // Clear old as set() won't do this
      lynxMachine.instructions.fill(0);
      lynxMachine.data.fill(0);

      lynxMachine.instructions.set(result.instructions);
      lynxMachine.data.set(result.data);
      lynxMachine.initialDataCopy.set(result.data);

      // Show assembler errors if any
      const errorDiv = document.getElementById("assembler-errors");
      if (errorDiv) {
        errorDiv.innerHTML = result.errorMessages
          .map((msg: string) => `<div>${msg}</div>`)
          .join("");
      }

      render();
    }
  });

  // Assembly line numbers ... I don't understand this
  const assemblyInput = document.getElementById(
    "assembly-input"
  ) as HTMLTextAreaElement;
  if (assemblyInput) {
    assemblyInput.addEventListener("input", updateLineNumbers);
    assemblyInput.addEventListener("scroll", () => {
      const lineNumbers = document.getElementById("line-numbers");
      if (lineNumbers) {
        lineNumbers.scrollTop = assemblyInput.scrollTop;
      }
      updateLineNumbers();
    });

    if (window.ResizeObserver) {
      const resizeObserver = new ResizeObserver(() => {
        updateLineNumbers();
      });
      resizeObserver.observe(assemblyInput);
    }

    updateLineNumbers();
  }

  // CPU clock rate
  const speedInput = document.getElementById("speed-input") as HTMLInputElement;
  const speedDecrease = document.getElementById("speed-decrease");
  const speedIncrease = document.getElementById("speed-increase");

  function updateSpeedDisplay() {
    if (speedInput) speedInput.value = vmSpeed.toString();
  }

  if (speedDecrease) {
    speedDecrease.addEventListener("click", () => {
      vmSpeed = Math.max(1, vmSpeed - 1);
      updateSpeedDisplay();
    });
  }
  if (speedIncrease) {
    speedIncrease.addEventListener("click", () => {
      vmSpeed = vmSpeed + 1;
      updateSpeedDisplay();
    });
  }
  if (speedInput) {
    speedInput.addEventListener("change", () => {
      const val = parseInt(speedInput.value);
      if (!isNaN(val) && val >= 1) {
        vmSpeed = val;
      } else {
        vmSpeed = 1;
      }
      updateSpeedDisplay();
    });
    updateSpeedDisplay();
  }

  // Remove stop button event listener, update start button to toggle
  const startBtn = document.getElementById("start-btn") as HTMLButtonElement;
  if (startBtn) {
    startBtn.addEventListener("click", () => {
      if (vmRunning) {
        stopVM();
      } else {
        startVM();
      }
    });
  }

  document.getElementById("step-btn")?.addEventListener("click", () => {
    if (!vmRunning) {
      // Only allow stepping when not running
      executeNextInstruction(lynxMachine);
      render();
    }
  });

  document.getElementById("reset-btn")?.addEventListener("click", () => {
    lynxMachine.pc = 0;
    lynxMachine.r0 = 0;
    lynxMachine.r1 = 0;
    lynxMachine.r2 = 0;
    lynxMachine.r3 = 0;
    lynxMachine.data.set(lynxMachine.initialDataCopy);
    lynxMachine.cyclesExecuted = 0;
    render();
  });

  document.getElementById("load-example-btn")?.addEventListener("click", () => {
    const selector = document.getElementById(
      "example-selector"
    ) as HTMLSelectElement;
    const assemblyInput = document.getElementById(
      "assembly-input"
    ) as HTMLTextAreaElement;

    if (selector.value === "fib") {
      assemblyInput.value = fibExample;
    } else if (selector.value === "sieve") {
      assemblyInput.value = sieveExample;
    }

    updateLineNumbers();
  });

  // Export instructions button event listener
  document
    .getElementById("export-instructions-btn")
    ?.addEventListener("click", exportInstructionsToClipboard);
});
