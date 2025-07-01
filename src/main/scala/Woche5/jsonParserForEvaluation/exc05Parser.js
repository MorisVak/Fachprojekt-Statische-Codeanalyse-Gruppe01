const fs = require("fs");
const path = require("path");

const inputFolder = "/Desktop/small json parser/Reports"; // 🔁 Change this to your folder path
const outputFile = "summary_output.json";

const summary = {};

fs.readdirSync(inputFolder).forEach((file) => {
  if (file.endsWith(".json")) {
    const filePath = path.join(inputFolder, file);
    try {
      const rawData = fs.readFileSync(filePath, "utf-8");
      const json = JSON.parse(rawData);
      const result = json.resultJson;

      if (!result) return;

      const domain = result.domainUsed || "Unknown";
      const methodCount = Array.isArray(result.methodsFound)
        ? result.methodsFound.length
        : 0;
      const runtimeMs = result.totalRuntimeMs || 0;
      const runtimeSec = runtimeMs / 1000;

      const totalDeads = result.methodsFound.reduce(
        (prev, curr) => prev + curr.numberOfDeadInstructions,
        0
      );
      if (!summary[domain]) {
        summary[domain] = {
          totalMethods: 0,
          totalRuntimeSeconds: 0,
          totalDeadInstructions: 0,
        };
      }

      summary[domain].totalMethods += methodCount;
      summary[domain].totalRuntimeSeconds += runtimeSec;
      summary[domain].totalDeadInstructions += totalDeads;
    } catch (err) {
      console.error(`Error reading/parsing file "${file}":`, err.message);
    }
  }
});

fs.writeFileSync(outputFile, JSON.stringify(summary, null, 2), "utf-8");
console.log(`Summary written to ${outputFile}`);
