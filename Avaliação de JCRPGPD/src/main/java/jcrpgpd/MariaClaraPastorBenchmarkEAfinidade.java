package jcrpgpd;

import java.util.ArrayList;
import java.util.List;

public class MariaClaraPastorBenchmarkEAfinidade {

    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
    }

    private static boolean afinidadeDisponivel = true;

    public static void aplicarAfinidade(int cpuId) {
        if (!afinidadeDisponivel) return;
        try {
            net.openhft.affinity.Affinity.setAffinity(cpuId);
        } catch (Throwable t) {
            afinidadeDisponivel = false;
        }
    }

    public static class MetricaExperimento {
        public String configuracao;
        public int threads;
        public int quantidadeDados;
        public long tempoMs;
        public double speedup;
        public double eficiencia;

        public MetricaExperimento(String configuracao, int threads, int quantidadeDados, long tempoMs) {
            this.configuracao = configuracao;
            this.threads = threads;
            this.quantidadeDados = quantidadeDados;
            this.tempoMs = tempoMs;
            this.speedup = 1.0;
            this.eficiencia = 100.0;
        }
    }

    public static double calcularSpeedup(long tempoSequencialMs, long tempoParaleloMs) {
        if (tempoParaleloMs <= 0) return 1.0;
        return (double) tempoSequencialMs / tempoParaleloMs;
    }

    public static double calcularEficiencia(double speedup, int threads) {
        if (threads <= 0) return 100.0;
        return (speedup / threads) * 100.0;
    }

    public static List<MetricaExperimento> executarExperimentos(Transacao[] dados) throws Exception {
        List<MetricaExperimento> resultados = new ArrayList<>();
        int total = dados.length;

        System.out.println("\n[AQUECIMENTO DA JVM - Warmup]");

        HelenaVieiraAgregacao.processarSequencial(dados);

        System.out.println("Executando Sequencial...");
        long tInicio = System.currentTimeMillis();
        HelenaVieiraAgregacao.processarSequencial(dados);
        long tSeq = Math.max(1, System.currentTimeMillis() - tInicio);

        MetricaExperimento mSeq = new MetricaExperimento("Sequencial", 1, total, tSeq);
        mSeq.speedup = 1.0;
        mSeq.eficiencia = 100.0;
        resultados.add(mSeq);

        int[] threadsTestes = {1, 2, 4, 8};

        System.out.println("Executando ExecutorService (sem afinidade)...");
        for (int th : threadsTestes) {
            long tIni = System.currentTimeMillis();
            HelenaVieiraAgregacao.processarParalelo(dados, th);
            long tPar = Math.max(1, System.currentTimeMillis() - tIni);

            MetricaExperimento m = new MetricaExperimento("Executor", th, total, tPar);
            m.speedup = calcularSpeedup(tSeq, tPar);
            m.eficiencia = calcularEficiencia(m.speedup, th);
            resultados.add(m);
        }

        System.out.println("Executando ExecutorService + Affinity (com afinidade)...");
        for (int th : threadsTestes) {
            long tIni = System.currentTimeMillis();
            HelenaVieiraAgregacao.processarComAfinidade(dados, th);
            long tAff = Math.max(1, System.currentTimeMillis() - tIni);

            MetricaExperimento m = new MetricaExperimento("Executor + Affinity", th, total, tAff);
            m.speedup = calcularSpeedup(tSeq, tAff);
            m.eficiencia = calcularEficiencia(m.speedup, th);
            resultados.add(m);
        }

        return resultados;
    }

    public static void exibirTabela(List<MetricaExperimento> metricas) {
        System.out.println("\n----------------------------------------------------------------------------------");
        System.out.printf("%-22s %-8s %-12s %-12s %-10s %-10s\n", 
                "Configuração", "Threads", "Dados", "Tempo (ms)", "Speedup", "Eficiência");
        System.out.println("----------------------------------------------------------------------------------");
        for (MetricaExperimento m : metricas) {
            System.out.printf("%-22s %-8d %-12d %-12d %-10.2f %-9.1f%%\n",
                    m.configuracao, m.threads, m.quantidadeDados, m.tempoMs, m.speedup, m.eficiencia);
        }
        System.out.println("----------------------------------------------------------------------------------");
    }

    public static void salvarResultadosCsv(List<MetricaExperimento> metricas, String arquivoCsv) {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(arquivoCsv))) {
            pw.println("Configuracao,Threads,QuantidadeDados,TempoMs,Speedup,Eficiencia");
            for (MetricaExperimento m : metricas) {
                pw.printf(java.util.Locale.US, "%s,%d,%d,%d,%.2f,%.1f\n",
                        m.configuracao, m.threads, m.quantidadeDados, m.tempoMs, m.speedup, m.eficiencia);
            }
        } catch (Exception e) {
            System.err.println("Aviso: Não foi possível salvar " + arquivoCsv + ": " + e.getMessage());
        }
    }
}
