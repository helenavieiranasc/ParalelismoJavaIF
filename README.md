# Análise Paralela de Transações Financeiras com Java

> **1ª Avaliação da Disciplina de Programação Paralela e Distribuída (JCRPGPD)**  
> **Instituto Federal de São Paulo (IFSP) – Câmpus Jacareí**  
> Docente: **Prof. Lineu Mialaret**

---

## 📌 Sobre o Projeto

Este projeto consiste em uma aplicação de alto desempenho desenvolvida em **Java 17** para o processamento e auditoria analítica de grandes volumes de transações financeiras sintéticas (**1 milhão** a **10 milhões** de registros), aplicando os fundamentos de **Paralelismo de Dados**.

O objetivo central foi investigar experimentalmente o impacto do framework de concorrência nativo do Java (`java.util.concurrent.ExecutorService`), do padrão de tarefas com retorno (`Callable` / `Future`), da fase de **Redução de Resultados** e do controle de baixo nível de **Afinidade de Processador (CPU Affinity)** via biblioteca nativa `OpenHFT Affinity`.

---

## 👥 Equipe e Contribuição Individual

Conforme as diretrizes da avaliação, as responsabilidades e implementações foram divididas de forma modular e bem delimitada:

* **Helena Vieira**
  * **Modelagem e Geração Sintética:** Implementação de `Transacao.java` e do motor `GeradorDados.java` com 4 distribuições probabilísticas.
  * **Agregação Financeira:** Módulo `HelenaVieiraAgregacao.java` com cálculo de Soma Total, Média (Ticket Médio), Maior e Menor valor de transação (versões Sequencial, ExecutorService e Afinidade).
* **Camila Rocha Ribeiro**
  * **Detecção de Suspeitas/Fraudes e Categorias:** Módulo `CamilaRibeiroSuspeitasECategorias.java` com regras de auditoria (`valor > R$ 10.000` ou `valor > R$ 5.000` em *Eletrônicos*).
  * **Estatísticas Agrupadas:** Consolidação e fusão concorrente de estatísticas para 5 categorias de despesas (*Alimentação*, *Transporte*, *Eletrônicos*, *Saúde* e *Educação*).
* **Maria Clara Pastor**
  * **Infraestrutura de Concorrência e Afinidade:** Integração do hardware binding via `OpenHFT Affinity` em `MariaClaraPastorBenchmarkEAfinidade.java`.
  * **Benchmark, Métricas e Gráficos:** Automação dos testes com aquecimento de JVM (*warm-up*), cálculo de *Speedup* e *Eficiência Paralela*, exportação em `resultados.csv` e geração dos gráficos via script Python.

---

## 📈 Resultados Experimentais (10 Milhões de Transações)

Os ensaios foram realizados em ambiente com processador multi-core sob Java 17 com warm-up prévio da JVM:

| Configuração | Threads | Volume de Dados | Tempo (ms) | Speedup ($S_p$) | Eficiência ($E_p$) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Sequencial** | 1 | 10.000.000 | **62 ms** | 1,00x | 100,0% |
| **ExecutorService** | 2 | 10.000.000 | **78 ms** | 0,79x | 39,7% |
| **ExecutorService** | 4 | 10.000.000 | **36 ms** | **1,72x** | **43,1%** |
| **ExecutorService** | 8 | 10.000.000 | **29 ms** | **2,14x** | **26,7%** |
| **Executor + Affinity** | 2 (CPUs 0,1) | 10.000.000 | **65 ms** | **0,95x** | **47,7%** |
| **Executor + Affinity** | 4 (CPUs 0-3) | 10.000.000 | **41 ms** | **1,51x** | **37,8%** |
| **Executor + Affinity** | 8 (CPUs 0-7) | 10.000.000 | **33 ms** | **1,88x** | **23,5%** |

### 🔍 Gráfico de Desempenho

![Gráfico de Speedup e Eficiência](Avaliação%20de%20JCRPGPD/grafico_desempenho.png)

* **Impacto da Afinidade em 2 Threads:** A fixação de núcleo reduziu o tempo de execução de **78 ms para 65 ms**, elevando a eficiência de 39,7% para 47,7% graças à preservação das linhas de cache L1/L2.
* **Comportamento em 8 Threads:** Em concorrência ampla, o ganho de velocidade atinge seu pico (**29 ms**, Speedup de **2,14x**). Em máquinas com Hyper-Threading/SMT, os núcleos lógicos compartilham o barramento de memória, tornando o problema preponderantemente *memory-bound*.

---

## 📁 Estrutura do Repositório

```text
ParalelismoJavaIF/
├── Avaliação de JCRPGPD/                                                 # Pasta com o código-fonte e automações
│   ├── lib/                                                              # Bibliotecas nativas da OpenHFT Affinity e JNA
│   │   ├── affinity-3.23.3.jar
│   │   ├── jna-5.13.0.jar
│   │   ├── jna-platform-5.13.0.jar
│   │   ├── slf4j-api-1.7.36.jar
│   │   └── slf4j-simple-1.7.36.jar
│   ├── src/
│   │   └── main/
│   │       └── java/
│   │           └── jcrpgpd/                                              # Código-fonte Java modular
│   │               ├── Transacao.java
│   │               ├── GeradorDados.java
│   │               ├── HelenaVieiraAgregacao.java
│   │               ├── CamilaRibeiroSuspeitasECategorias.java
│   │               ├── MariaClaraPastorBenchmarkEAfinidade.java
│   │               └── Main.java
│   ├── executar.bat                                                      # Executável para 10M de transações
│   ├── executar_rapido.bat                                               # Executável rápido para 1M de transações
│   ├── gerar_graficos.py                                                 # Script para plotar Speedup e Eficiência
│   ├── grafico_desempenho.png                                            # Gráfico gerado do benchmark
│   ├── pom.xml                                                           # Gerenciador de dependências Maven
│   └── resultados.csv                                                    # Tabela bruta com as medições
│
├── 1ª Avaliação de JCRPGPD – Programação Paralela e Distribuída.pdf            # Relatório de desenvolvimento
├── Relatório 1ª Avaliação de JCRPGPD – Programação Paralela e Distribuída.pdf  # Relatório técnico completo (16 seções)
├── Requisitos 1º avaliação de JCRPGPD.pdf                                # Documento com as diretrizes da avaliação
├── Slides Apresentação - Análise Paralela de Transações Financeiras.pdf  # Slides da apresentação para a defesa
├── LICENSE                                                               # Licença do repositório
└── README.md                                                             # Este documento
```

---

## 🚀 Como Executar

### Pré-requisitos
* **Java Development Kit (JDK) 17** ou superior instalado.
* Python 3 com `matplotlib` e `numpy` (apenas caso deseje regerar os gráficos).

---

### Opção 1: Execução Automática no Windows (Mais Simples)
Dentro da pasta **`Avaliação de JCRPGPD`**, basta dar um duplo clique nos arquivos `.bat`:
* **`executar.bat`**: Compila e roda o benchmark completo com **10 milhões de transações** (requer cerca de 3 GB de RAM livres).
* **`executar_rapido.bat`**: Compila e roda a validação expressa com **1 milhão de transações**.

---

### Opção 2: Pelo Terminal / PowerShell

1. **Acessar a pasta do projeto:**
   ```bash
   cd "Avaliação de JCRPGPD"
   ```

2. **Compilar o código:**
   ```bash
   javac -encoding UTF-8 -cp "lib/*;." -d bin src/main/java/jcrpgpd/*.java
   ```

3. **Executar a aplicação:**
   ```bash
   java -Xmx3g -cp "bin;lib/*" -Dorg.slf4j.simpleLogger.defaultLogLevel=error --enable-native-access=ALL-UNNAMED jcrpgpd.Main 10000000
   ```

4. *(Opcional)* **Regerar o gráfico de desempenho:**
   ```bash
   python gerar_graficos.py
   ```

---

## 🛠️ Tecnologias Utilizadas

* **Java 17 (LTS)**: Linguagem principal da aplicação.
* **`java.util.concurrent`**: ExecutorService, Callable, Future e ThreadPools.
* **OpenHFT Java-Thread-Affinity (v3.23.3)**: Vinculação de threads do Java a núcleos específicos de CPU.
* **JNA & JNA Platform (v5.13.0)**: Acesso a chamadas de sistema nativas de afinidade no Windows/Linux.
* **Python 3 & Matplotlib**: Visualização de métricas de Speedup e Eficiência.
* **Apache Maven**: Gerenciamento de ciclo de vida e dependências.

---

## 📚 Referências

1. GOETZ, Brian et al. *Java Concurrency in Practice*. Boston: Addison-Wesley Professional, 2006.
2. MIALARET, Lineu. *Notas de Aula da Disciplina de Programação Paralela e Distribuída (JCRPGPD)*. Jacareí: IFSP, 2026.
3. OPENHFT. *Java-Thread-Affinity: Bind Java Threads to specific CPU Cores*. Versão 3.23.3. Repositório GitHub: [OpenHFT/Java-Thread-Affinity](https://github.com/OpenHFT/Java-Thread-Affinity).
4. ORACLE. *Java Platform, Standard Edition & JDK Version 17 API Specification: Package java.util.concurrent*. Oracle Corporation, 2021.
