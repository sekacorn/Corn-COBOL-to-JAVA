/* ============================================
   CORN Demo UI — Application Logic
   Calls the real CORN backend when available,
   falls back to mock data for offline demos.
   ============================================ */

// --- Configuration ---
const API_BASE = ''; // Same origin when served by CornServer; change for dev

// --- Backend connectivity ---
let backendAvailable = null; // null = unknown, true/false after check

async function checkBackend() {
  try {
    const resp = await fetch(`${API_BASE}/api/health`, { signal: AbortSignal.timeout(2000) });
    if (resp.ok) { backendAvailable = true; return true; }
  } catch { /* offline */ }
  backendAvailable = false;
  return false;
}
checkBackend().then(ok => {
  const badge = document.getElementById('status-badge');
  if (badge) {
    badge.textContent = ok ? 'Live' : 'Demo Mode';
    badge.className = ok ? 'badge badge-green' : 'badge badge-amber';
  }
});

// --- Tab Navigation ---
const tabs = document.querySelectorAll('.tab');
const panels = document.querySelectorAll('.panel');

tabs.forEach(tab => {
  tab.addEventListener('click', () => {
    const target = tab.dataset.tab;
    tabs.forEach(t => {
      t.classList.remove('active');
      t.setAttribute('aria-selected', 'false');
    });
    panels.forEach(p => {
      p.classList.remove('active');
      p.hidden = true;
    });
    tab.classList.add('active');
    tab.setAttribute('aria-selected', 'true');
    const panel = document.getElementById(`panel-${target}`);
    panel.classList.add('active');
    panel.hidden = false;
  });

  // Keyboard navigation for tabs
  tab.addEventListener('keydown', (e) => {
    const tabList = Array.from(tabs);
    const idx = tabList.indexOf(tab);
    let newIdx = idx;
    if (e.key === 'ArrowRight') newIdx = (idx + 1) % tabList.length;
    else if (e.key === 'ArrowLeft') newIdx = (idx - 1 + tabList.length) % tabList.length;
    else if (e.key === 'Home') newIdx = 0;
    else if (e.key === 'End') newIdx = tabList.length - 1;
    else return;
    e.preventDefault();
    tabList[newIdx].focus();
    tabList[newIdx].click();
  });
});

// --- Sample COBOL Program ---
const SAMPLE_COBOL = `       IDENTIFICATION DIVISION.
       PROGRAM-ID. BANK-INTEREST.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-PRINCIPAL   PIC 9(7)V99 VALUE 10000.00.
       01  WS-RATE        PIC 9V9(4)  VALUE 0.0525.
       01  WS-YEARS       PIC 99      VALUE 5.
       01  WS-AMOUNT      PIC 9(7)V99 VALUE 0.
       01  WS-INTEREST    PIC 9(7)V99 VALUE 0.
       01  WS-COUNTER     PIC 99      VALUE 0.
       01  WS-DISPLAY     PIC Z(6)9.99.
       PROCEDURE DIVISION.
       MAIN-PARA.
           DISPLAY "=== COMPOUND INTEREST CALCULATOR ==="
           MOVE WS-PRINCIPAL TO WS-AMOUNT
           PERFORM CALC-INTEREST
               VARYING WS-COUNTER FROM 1 BY 1
               UNTIL WS-COUNTER > WS-YEARS
           MOVE WS-AMOUNT TO WS-DISPLAY
           DISPLAY "FINAL AMOUNT: $" WS-DISPLAY
           COMPUTE WS-INTEREST =
               WS-AMOUNT - WS-PRINCIPAL
           MOVE WS-INTEREST TO WS-DISPLAY
           DISPLAY "TOTAL INTEREST: $" WS-DISPLAY
           STOP RUN.
       CALC-INTEREST.
           COMPUTE WS-AMOUNT =
               WS-AMOUNT * (1 + WS-RATE)
           MOVE WS-AMOUNT TO WS-DISPLAY
           DISPLAY "YEAR " WS-COUNTER ": $" WS-DISPLAY.`;

// --- Mock Fallback Data ---
const MOCK_JAVA = `import com.sekacorn.corn.runtime.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BankInterest {
    // Working Storage
    private BigDecimal wsPrincipal = new BigDecimal("10000.00");
    private BigDecimal wsRate = new BigDecimal("0.0525");
    private int wsYears = 5;
    private BigDecimal wsAmount = BigDecimal.ZERO;
    private BigDecimal wsInterest = BigDecimal.ZERO;
    private int wsCounter = 0;

    public void procedureDivision() {
        mainPara();
    }

    private void mainPara() {
        CobolRuntime.display("=== COMPOUND INTEREST CALCULATOR ===");
        wsAmount = wsPrincipal;
        for (wsCounter = 1; wsCounter <= wsYears; wsCounter++) {
            calcInterest();
        }
        CobolRuntime.display("FINAL AMOUNT: $" + formatDisplay(wsAmount));
        wsInterest = wsAmount.subtract(wsPrincipal);
        CobolRuntime.display("TOTAL INTEREST: $" + formatDisplay(wsInterest));
    }

    private void calcInterest() {
        wsAmount = wsAmount.multiply(
            BigDecimal.ONE.add(wsRate)
        ).setScale(2, RoundingMode.HALF_UP);
        CobolRuntime.display(
            "YEAR " + wsCounter + ": $" + formatDisplay(wsAmount)
        );
    }

    private String formatDisplay(BigDecimal value) {
        return String.format("%,10.2f", value).trim();
    }

    public static void main(String[] args) {
        new BankInterest().procedureDivision();
    }
}`;

const MOCK_TRACE = [
  { stmt: 'DISPLAY "=== COMPOUND..."', vars: '', out: '=== COMPOUND INTEREST CALCULATOR ===' },
  { stmt: 'MOVE WS-PRINCIPAL TO WS-AMOUNT', vars: 'WS-AMOUNT = 10000.00', out: '' },
  { stmt: 'PERFORM (WS-COUNTER=1)', vars: 'WS-COUNTER = 1', out: '' },
  { stmt: 'COMPUTE WS-AMOUNT', vars: 'WS-AMOUNT = 10525.00', out: '' },
  { stmt: 'DISPLAY "YEAR 1..."', vars: '', out: 'YEAR 1: $10,525.00' },
  { stmt: 'PERFORM (WS-COUNTER=2)', vars: 'WS-COUNTER = 2', out: '' },
  { stmt: 'COMPUTE WS-AMOUNT', vars: 'WS-AMOUNT = 11077.56', out: '' },
  { stmt: 'DISPLAY "YEAR 2..."', vars: '', out: 'YEAR 2: $11,077.56' },
  { stmt: 'PERFORM (WS-COUNTER=3)', vars: 'WS-COUNTER = 3', out: '' },
  { stmt: 'COMPUTE WS-AMOUNT', vars: 'WS-AMOUNT = 11659.13', out: '' },
  { stmt: 'DISPLAY "YEAR 3..."', vars: '', out: 'YEAR 3: $11,659.13' },
  { stmt: 'PERFORM (WS-COUNTER=4)', vars: 'WS-COUNTER = 4', out: '' },
  { stmt: 'COMPUTE WS-AMOUNT', vars: 'WS-AMOUNT = 12271.23', out: '' },
  { stmt: 'DISPLAY "YEAR 4..."', vars: '', out: 'YEAR 4: $12,271.23' },
  { stmt: 'PERFORM (WS-COUNTER=5)', vars: 'WS-COUNTER = 5', out: '' },
  { stmt: 'COMPUTE WS-AMOUNT', vars: 'WS-AMOUNT = 12916.47', out: '' },
  { stmt: 'DISPLAY "YEAR 5..."', vars: '', out: 'YEAR 5: $12,916.47' },
  { stmt: 'DISPLAY "FINAL AMOUNT..."', vars: '', out: 'FINAL AMOUNT: $12,916.47' },
  { stmt: 'COMPUTE WS-INTEREST', vars: 'WS-INTEREST = 2916.47', out: '' },
  { stmt: 'DISPLAY "TOTAL INTEREST..."', vars: '', out: 'TOTAL INTEREST: $2,916.47' },
  { stmt: 'STOP RUN', vars: '', out: '--- PROGRAM TERMINATED ---' },
];

// ---------------------------------------------------------------
// Translate
// ---------------------------------------------------------------
const btnTranslate = document.getElementById('btn-translate');
const btnLoadSample = document.getElementById('btn-load-sample');
const btnCopyJava = document.getElementById('btn-copy-java');
const btnDownloadJava = document.getElementById('btn-download-java');
const cobolInput = document.getElementById('cobol-input');
const javaOutput = document.getElementById('java-output');
const translateStatus = document.getElementById('translate-status');
const fileUpload = document.getElementById('file-upload');

// Track last translation result for download filename
let lastClassName = 'Output';

btnLoadSample.addEventListener('click', () => {
  cobolInput.value = SAMPLE_COBOL;
  cobolInput.focus();
});

fileUpload.addEventListener('change', (e) => {
  const file = e.target.files[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = (ev) => {
    cobolInput.value = ev.target.result;
    cobolInput.focus();
  };
  reader.readAsText(file);
});

btnTranslate.addEventListener('click', async () => {
  const cobol = cobolInput.value.trim();
  if (!cobol) {
    translateStatus.textContent = 'Please enter COBOL source code.';
    translateStatus.className = 'status-bar error';
    return;
  }

  btnTranslate.disabled = true;
  javaOutput.innerHTML = '<span class="loading"><span class="loading-dot"></span><span class="loading-dot"></span><span class="loading-dot"></span></span> Translating...';
  translateStatus.textContent = 'Connecting to CORN engine...';
  translateStatus.className = 'status-bar';

  const startTime = performance.now();

  // Try real backend first
  if (backendAvailable !== false) {
    try {
      const resp = await fetch(`${API_BASE}/api/translate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ source: cobol }),
        signal: AbortSignal.timeout(15000),
      });
      const data = await resp.json();
      const elapsed = ((performance.now() - startTime) / 1000).toFixed(2);

      if (data.success && data.java) {
        javaOutput.textContent = data.java;
        lastClassName = data.className || 'Output';
        const lineCount = data.java.split('\n').length;
        const diagCount = (data.diagnostics || []).length;
        translateStatus.textContent = `Translation complete — ${lineCount} lines of Java generated in ${elapsed}s (${diagCount} diagnostics)`;
        translateStatus.className = 'status-bar success';
      } else {
        javaOutput.textContent = data.error || 'Translation failed';
        translateStatus.textContent = `Translation failed — ${data.error || 'unknown error'}`;
        translateStatus.className = 'status-bar error';
      }

      btnTranslate.disabled = false;
      btnCopyJava.disabled = false;
      btnDownloadJava.disabled = false;

      // Update analysis with real data
      updateAnalysisFromBackend(cobol);
      backendAvailable = true;
      return;
    } catch (err) {
      // Backend unreachable — fall through to mock
      backendAvailable = false;
      const badge = document.getElementById('status-badge');
      if (badge) { badge.textContent = 'Demo Mode'; badge.className = 'badge badge-amber'; }
    }
  }

  // Fallback: mock translation
  translateStatus.textContent = 'Parsing COBOL source (demo mode)...';
  await delay(600);
  translateStatus.textContent = 'Building IR...';
  await delay(400);
  translateStatus.textContent = 'Generating Java...';
  await delay(300);

  javaOutput.textContent = MOCK_JAVA;
  lastClassName = 'BankInterest';
  const elapsed = ((performance.now() - startTime) / 1000).toFixed(2);
  translateStatus.textContent = `Translation complete — ${MOCK_JAVA.split('\n').length} lines of Java generated in ${elapsed}s (demo mode)`;
  translateStatus.className = 'status-bar success';
  btnTranslate.disabled = false;
  btnCopyJava.disabled = false;
  btnDownloadJava.disabled = false;

  updateAnalysisMock(cobol);
});

btnCopyJava.addEventListener('click', async () => {
  try {
    await navigator.clipboard.writeText(javaOutput.textContent);
    btnCopyJava.textContent = 'Copied!';
    setTimeout(() => { btnCopyJava.textContent = 'Copy'; }, 1500);
  } catch {
    btnCopyJava.textContent = 'Failed';
    setTimeout(() => { btnCopyJava.textContent = 'Copy'; }, 1500);
  }
});

btnDownloadJava.addEventListener('click', () => {
  const blob = new Blob([javaOutput.textContent], { type: 'text/java' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `${lastClassName}.java`;
  a.click();
  URL.revokeObjectURL(url);
});

// ---------------------------------------------------------------
// Analysis — Real backend
// ---------------------------------------------------------------
async function updateAnalysisFromBackend(cobol) {
  try {
    const resp = await fetch(`${API_BASE}/api/analyze`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ source: cobol }),
      signal: AbortSignal.timeout(10000),
    });
    const data = await resp.json();
    if (!data.success) { updateAnalysisMock(cobol); return; }

    // Metrics
    document.getElementById('metric-lines').textContent = data.totalLines || '--';
    document.getElementById('metric-statements').textContent = data.statements ?? '--';
    document.getElementById('metric-complexity').textContent = data.complexity || '--';
    document.getElementById('metric-paragraphs').textContent = data.paragraphs ?? '--';
    document.getElementById('metric-data-items').textContent = data.dataItems ?? '--';
    document.getElementById('metric-copybooks').textContent = data.copybooks ?? 0;

    // Diagnostics
    const diagList = document.getElementById('diagnostics-list');
    diagList.innerHTML = '';
    const diagnostics = data.diagnostics || [];
    if (diagnostics.length === 0) {
      diagList.innerHTML = '<div class="diag-item diag-info" role="listitem"><span class="diag-icon" aria-label="info">I</span><span class="diag-text">No diagnostics — clean parse</span></div>';
    }
    diagnostics.forEach(d => {
      const type = d.severity === 'error' ? 'error' : d.severity === 'warning' ? 'warning' : 'info';
      const icon = type === 'error' ? 'E' : type === 'warning' ? 'W' : 'I';
      const div = document.createElement('div');
      div.className = `diag-item diag-${type}`;
      div.setAttribute('role', 'listitem');
      const loc = d.line ? `Line ${d.line}${d.column ? ':' + d.column : ''}` : '';
      div.innerHTML = `
        <span class="diag-icon" aria-label="${type}">${icon}</span>
        <span class="diag-text">${escapeHtml(d.message)}</span>
        ${loc ? `<span class="diag-loc">${escapeHtml(loc)}</span>` : ''}
      `;
      diagList.appendChild(div);
    });

    // Division breakdown
    const bd = data.divisionBreakdown;
    if (bd) {
      const breakdownEl = document.getElementById('division-breakdown');
      const divisions = [
        { name: 'Identification', pct: bd.identification || 0, color: 'var(--accent)' },
        { name: 'Environment', pct: bd.environment || 0, color: 'var(--purple)' },
        { name: 'Data', pct: bd.data || 0, color: 'var(--green)' },
        { name: 'Procedure', pct: bd.procedure || 0, color: 'var(--amber)' },
      ];
      breakdownEl.innerHTML = '';
      divisions.forEach(d => {
        breakdownEl.innerHTML += `
          <div class="breakdown-item">
            <div class="breakdown-label"><span>${d.name} Division</span><span>${d.pct}%</span></div>
            <div class="breakdown-bar"><div class="breakdown-fill" style="width: ${d.pct}%; background: ${d.color};"></div></div>
          </div>
        `;
      });
    }

    // Feature usage
    const ALL_FEATURES = [
      'COMPUTE', 'PERFORM', 'DISPLAY', 'MOVE', 'Arithmetic', 'IF/ELSE',
      'EVALUATE', 'STRING/UNSTRING', 'INSPECT', 'File I/O', 'CALL',
      'COPY', 'ACCEPT', 'SEARCH', 'GO TO', 'STOP RUN', 'EXEC SQL', 'EXEC CICS'
    ];
    const detected = new Set(data.features || []);
    const featureEl = document.getElementById('feature-usage');
    featureEl.innerHTML = '';
    ALL_FEATURES.forEach(f => {
      const span = document.createElement('span');
      span.className = `feature-tag${detected.has(f) ? ' used' : ''}`;
      span.textContent = f;
      featureEl.appendChild(span);
    });

  } catch {
    updateAnalysisMock(cobol);
  }
}

// ---------------------------------------------------------------
// Analysis — Mock fallback
// ---------------------------------------------------------------
function updateAnalysisMock(cobol) {
  const lines = cobol.split('\n');
  const stmtCount = lines.filter(l => l.trim() && !l.trim().startsWith('*')).length;

  document.getElementById('metric-lines').textContent = lines.length;
  document.getElementById('metric-statements').textContent = stmtCount;
  document.getElementById('metric-complexity').textContent = 'Medium';
  document.getElementById('metric-paragraphs').textContent =
    lines.filter(l => /^\s{7}\S.*\.\s*$/.test(l) && !l.match(/DIVISION|SECTION/i)).length || 2;
  document.getElementById('metric-data-items').textContent =
    lines.filter(l => /^\s+\d{2}\s+/.test(l)).length || 6;
  document.getElementById('metric-copybooks').textContent = 0;

  // Mock diagnostics
  const MOCK_DIAGNOSTICS = [
    { type: 'info', text: 'ANSI-85 COBOL dialect detected', loc: 'Line 1' },
    { type: 'info', text: 'Translation completed (demo mode)', loc: '' },
  ];
  const diagList = document.getElementById('diagnostics-list');
  diagList.innerHTML = '';
  MOCK_DIAGNOSTICS.forEach(d => {
    const icon = d.type === 'error' ? 'E' : d.type === 'warning' ? 'W' : 'I';
    const div = document.createElement('div');
    div.className = `diag-item diag-${d.type}`;
    div.setAttribute('role', 'listitem');
    div.innerHTML = `
      <span class="diag-icon" aria-label="${d.type}">${icon}</span>
      <span class="diag-text">${escapeHtml(d.text)}</span>
      ${d.loc ? `<span class="diag-loc">${escapeHtml(d.loc)}</span>` : ''}
    `;
    diagList.appendChild(div);
  });

  // Division breakdown (heuristic from source)
  const breakdownEl = document.getElementById('division-breakdown');
  const upper = cobol.toUpperCase();
  let idIdx = upper.indexOf('IDENTIFICATION DIVISION');
  let envIdx = upper.indexOf('ENVIRONMENT DIVISION');
  let dataIdx = upper.indexOf('DATA DIVISION');
  let procIdx = upper.indexOf('PROCEDURE DIVISION');
  const markers = [
    { name: 'Identification', start: idIdx, color: 'var(--accent)' },
    { name: 'Environment', start: envIdx, color: 'var(--purple)' },
    { name: 'Data', start: dataIdx, color: 'var(--green)' },
    { name: 'Procedure', start: procIdx, color: 'var(--amber)' },
  ].filter(m => m.start >= 0).sort((a, b) => a.start - b.start);
  breakdownEl.innerHTML = '';
  markers.forEach((m, i) => {
    const end = i < markers.length - 1 ? markers[i + 1].start : cobol.length;
    const pct = Math.round((end - m.start) / cobol.length * 100);
    breakdownEl.innerHTML += `
      <div class="breakdown-item">
        <div class="breakdown-label"><span>${m.name} Division</span><span>${pct}%</span></div>
        <div class="breakdown-bar"><div class="breakdown-fill" style="width: ${pct}%; background: ${m.color};"></div></div>
      </div>
    `;
  });

  // Feature detection
  const ALL_FEATURES = [
    'COMPUTE', 'PERFORM', 'DISPLAY', 'MOVE', 'Arithmetic', 'IF/ELSE',
    'EVALUATE', 'STRING/UNSTRING', 'INSPECT', 'File I/O', 'CALL',
    'COPY', 'ACCEPT', 'SEARCH', 'GO TO', 'STOP RUN', 'EXEC SQL', 'EXEC CICS'
  ];
  const featureEl = document.getElementById('feature-usage');
  featureEl.innerHTML = '';
  ALL_FEATURES.forEach(f => {
    const keywords = f === 'IF/ELSE' ? ['IF '] :
                     f === 'Arithmetic' ? ['ADD ', 'SUBTRACT ', 'MULTIPLY ', 'DIVIDE '] :
                     f === 'STRING/UNSTRING' ? ['STRING ', 'UNSTRING '] :
                     f === 'File I/O' ? ['OPEN ', 'READ ', 'WRITE '] :
                     [f];
    const used = keywords.some(kw => upper.includes(kw));
    const span = document.createElement('span');
    span.className = `feature-tag${used ? ' used' : ''}`;
    span.textContent = f;
    featureEl.appendChild(span);
  });
}

// ---------------------------------------------------------------
// Cost Estimator (client-side only — no backend needed)
// ---------------------------------------------------------------
document.getElementById('btn-estimate').addEventListener('click', () => {
  const rate = parseFloat(document.getElementById('cost-rate').value) || 175;
  const loc = parseInt(document.getElementById('cost-loc').value) || 156000;
  const programs = parseInt(document.getElementById('cost-programs').value) || 24;
  const complexity = document.getElementById('cost-complexity').value;

  const mult = complexity === 'low' ? 0.7 : complexity === 'high' ? 1.4 : 1.0;
  const baseHoursPer1K = 20;
  const totalBaseHours = (loc / 1000) * baseHoursPer1K * mult;

  const phases = [
    { name: 'Automated Translation', pct: 0.15 },
    { name: 'Code Review & Validation', pct: 0.23 },
    { name: 'Manual Remediation', pct: 0.21 },
    { name: 'Unit Testing', pct: 0.18 },
    { name: 'Integration Testing', pct: 0.13 },
    { name: 'Deployment & Cutover', pct: 0.10 },
  ];

  let totalHours = 0;
  let totalCost = 0;
  const tbody = document.getElementById('cost-table-body');
  tbody.innerHTML = '';

  phases.forEach(phase => {
    const hours = Math.round(totalBaseHours * phase.pct);
    const cost = hours * rate;
    totalHours += hours;
    totalCost += cost;
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${phase.name}</td>
      <td>${hours.toLocaleString()}</td>
      <td>$${cost.toLocaleString()}</td>
      <td>${Math.round(phase.pct * 100)}%</td>
    `;
    tbody.appendChild(tr);
  });

  document.getElementById('cost-total-value').textContent = `$${totalCost.toLocaleString()}`;
  document.getElementById('cost-h-total').innerHTML = `<strong>${totalHours.toLocaleString()}</strong>`;
  document.getElementById('cost-c-total').innerHTML = `<strong>$${totalCost.toLocaleString()}</strong>`;

  const weeks = Math.ceil(totalHours / (6 * 40));
  document.getElementById('cost-duration-value').textContent = `${weeks} weeks`;
});

// ---------------------------------------------------------------
// Trace (mock simulation — no backend for execution trace yet)
// ---------------------------------------------------------------
let traceStep = 0;
const btnTraceRun = document.getElementById('btn-trace-run');
const btnTraceStep = document.getElementById('btn-trace-step');
const btnTraceReset = document.getElementById('btn-trace-reset');
const traceOutput = document.getElementById('trace-output');
const traceStepInfo = document.getElementById('trace-step-info');

const TRACE_HEADER = `
  <div class="trace-line trace-header">
    <span class="trace-col trace-col-step">#</span>
    <span class="trace-col trace-col-stmt">Statement</span>
    <span class="trace-col trace-col-var">Variables</span>
    <span class="trace-col trace-col-out">Output</span>
  </div>
`;

btnTraceRun.addEventListener('click', async () => {
  traceStep = 0;
  traceOutput.innerHTML = TRACE_HEADER;
  btnTraceRun.disabled = true;
  btnTraceStep.disabled = true;
  btnTraceReset.disabled = false;

  for (let i = 0; i < MOCK_TRACE.length; i++) {
    traceStep = i + 1;
    addTraceLine(i, MOCK_TRACE[i]);
    traceStepInfo.textContent = `Step ${traceStep} / ${MOCK_TRACE.length}`;
    await delay(180);
  }

  traceStepInfo.textContent = `Complete — ${MOCK_TRACE.length} steps`;
  btnTraceRun.disabled = false;
  btnTraceStep.disabled = false;
});

btnTraceStep.addEventListener('click', () => {
  if (traceStep >= MOCK_TRACE.length) {
    traceStepInfo.textContent = `Complete — ${MOCK_TRACE.length} steps`;
    return;
  }
  if (traceStep === 0) {
    traceOutput.innerHTML = TRACE_HEADER;
    btnTraceReset.disabled = false;
  }
  traceOutput.querySelectorAll('.trace-highlight').forEach(el => el.classList.remove('trace-highlight'));
  addTraceLine(traceStep, MOCK_TRACE[traceStep], true);
  traceStep++;
  traceStepInfo.textContent = `Step ${traceStep} / ${MOCK_TRACE.length}`;
});

btnTraceReset.addEventListener('click', () => {
  traceStep = 0;
  traceOutput.innerHTML = TRACE_HEADER + '<p class="placeholder-text" style="padding: 1rem;">Click "Run Trace" to simulate execution.</p>';
  traceStepInfo.textContent = '';
  btnTraceStep.disabled = false;
  btnTraceRun.disabled = false;
  btnTraceReset.disabled = true;
});

function addTraceLine(idx, data, highlight = false) {
  const div = document.createElement('div');
  div.className = `trace-line${highlight ? ' trace-highlight' : ''}`;
  div.innerHTML = `
    <span class="trace-col trace-col-step">${idx + 1}</span>
    <span class="trace-col trace-col-stmt">${escapeHtml(data.stmt)}</span>
    <span class="trace-col trace-col-var">${escapeHtml(data.vars)}</span>
    <span class="trace-col trace-col-out">${escapeHtml(data.out)}</span>
  `;
  traceOutput.appendChild(div);
  div.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
}

// ---------------------------------------------------------------
// Utilities
// ---------------------------------------------------------------
function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}

// --- Initialize Cost on Load ---
document.getElementById('btn-estimate').click();
