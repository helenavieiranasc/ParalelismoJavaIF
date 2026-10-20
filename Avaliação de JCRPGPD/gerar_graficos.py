import os
import csv
import matplotlib.pyplot as plt
import numpy as np

threads = [1, 2, 4, 8]

tempo_seq = 62.0
tempos_executor = [62.0, 78.0, 36.0, 29.0]
tempos_affinity = [62.0, 65.0, 41.0, 33.0]

if os.path.exists("resultados.csv"):
    try:
        with open("resultados.csv", "r", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            t_exec_map = {}
            t_aff_map = {}
            t_seq_val = None
            for row in reader:
                cfg = row.get("Configuracao", "").strip()
                th = int(row.get("Threads", 1))
                t_ms = float(row.get("TempoMs", 1.0))
                if cfg == "Sequencial":
                    t_seq_val = t_ms
                elif cfg == "Executor":
                    t_exec_map[th] = t_ms
                elif "Affinity" in cfg:
                    t_aff_map[th] = t_ms
            if t_seq_val is not None and len(t_exec_map) >= 3 and len(t_aff_map) >= 3:
                tempo_seq = t_seq_val
                tempos_executor = [tempo_seq] + [t_exec_map.get(t, tempo_seq) for t in [2, 4, 8]]
                tempos_affinity = [tempo_seq] + [t_aff_map.get(t, tempo_seq) for t in [2, 4, 8]]
                print(f"Carregado com sucesso de resultados.csv (T_seq = {tempo_seq:.1f} ms)!")
    except Exception as e:
        print(f"Aviso ao ler resultados.csv ({e}), mantendo valores de referência.")

speedup_ideal = threads
speedup_executor = [tempo_seq / t for t in tempos_executor]
speedup_affinity = [tempo_seq / t for t in tempos_affinity]

eficiencia_ideal = [100.0 for _ in threads]
eficiencia_executor = [(s / p) * 100.0 for s, p in zip(speedup_executor, threads)]
eficiencia_affinity = [(s / p) * 100.0 for s, p in zip(speedup_affinity, threads)]

fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(13, 5))
fig.suptitle('Avaliação de Desempenho Paralelo (10 Milhões de Transações)', fontsize=14, fontweight='bold')

ax1.plot(threads, speedup_ideal, 'k--', label='Speedup Linear (Ideal)', alpha=0.7)
ax1.plot(threads, speedup_executor, 'bo-', linewidth=2, markersize=7, label='ExecutorService (Sem Afinidade)')
ax1.plot(threads, speedup_affinity, 'rs-', linewidth=2, markersize=7, label='ExecutorService + Affinity')
ax1.set_title('Speedup por Quantidade de Threads', fontsize=12, fontweight='bold')
ax1.set_xlabel('Quantidade de Threads', fontsize=11)
ax1.set_ylabel('Speedup (Sp = T_seq / T_par)', fontsize=11)
ax1.set_xticks(threads)
ax1.grid(True, linestyle=':', alpha=0.6)
ax1.legend(fontsize=9)

ax2.plot(threads, eficiencia_ideal, 'k--', label='Eficiência Ideal (100%)', alpha=0.7)
ax2.plot(threads, eficiencia_executor, 'bo-', linewidth=2, markersize=7, label='ExecutorService (Sem Afinidade)')
ax2.plot(threads, eficiencia_affinity, 'rs-', linewidth=2, markersize=7, label='ExecutorService + Affinity')
ax2.set_title('Eficiência Paralela (%)', fontsize=12, fontweight='bold')
ax2.set_xlabel('Quantidade de Threads', fontsize=11)
ax2.set_ylabel('Eficiência (%)', fontsize=11)
ax2.set_xticks(threads)
ax2.set_ylim(0, 110)
ax2.grid(True, linestyle=':', alpha=0.6)
ax2.legend(fontsize=9)

plt.tight_layout()
plt.savefig('grafico_desempenho.png', dpi=300)
print('Gráfico salvo com sucesso como grafico_desempenho.png!')
