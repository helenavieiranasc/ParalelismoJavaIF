package jcrpgpd;

import java.util.Locale;

public class Main {

    public static void main(String[] args) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
        Locale.setDefault(Locale.US);

        int quantidadeTransacoes = 10_000_000; 
        if (args.length > 0) {
            try {
                quantidadeTransacoes = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Parâmetro inválido, usando padrão: " + quantidadeTransacoes);
            }
        }

        System.out.println("==============================================");
        System.out.println(" ANÁLISE PARALELA DE TRANSAÇÕES FINANCEIRAS");
        System.out.println("==============================================");
        System.out.printf("Quantidade de transações: %,d\n\n", quantidadeTransacoes);

        System.out.print("Gerando dados sintéticos com 4 distribuições...");
        long tGenIni = System.currentTimeMillis();
        Transacao[] dados = GeradorDados.gerarTransacoes(quantidadeTransacoes);
        long tGenFim = System.currentTimeMillis();
        System.out.printf(" Concluído em %d ms!\n\n", (tGenFim - tGenIni));

        System.out.println("----------- RESULTADOS DAS ANÁLISES (NEGÓCIO) -----------");

        HelenaVieiraAgregacao.ResultadoAgregacao resAgregacao = 
                HelenaVieiraAgregacao.processarSequencial(dados);
        System.out.println("[Helena Vieira - Agregação Financeira Geral]");
        System.out.printf("  * Total Transacionado : R$ %,.2f\n", resAgregacao.somaTotal);
        System.out.printf("  * Ticket Médio (Média): R$ %,.2f\n", resAgregacao.getMedia());
        System.out.printf("  * Maior Transação     : R$ %,.2f\n", resAgregacao.maiorValor);
        System.out.printf("  * Menor Transação     : R$ %,.2f\n\n", resAgregacao.menorValor);

        CamilaRibeiroSuspeitasECategorias.ResultadoAnalise2 resAnalise2 = 
                CamilaRibeiroSuspeitasECategorias.processarSequencial(dados);
        System.out.println("[Camila Ribeiro - Detecção de Suspeitas e Fraudes]");
        System.out.printf("  * Quantidade de Suspeitas: %,d transações\n", resAnalise2.totalSuspeitas);
        System.out.printf("  * Montante Suspeito      : R$ %,.2f\n\n", resAnalise2.somaValorSuspeitas);

        System.out.println("[Camila Ribeiro - Estatísticas por Categoria]");
        System.out.printf("%-15s %-15s %-20s %-15s %-15s\n", 
                "Categoria", "Quantidade", "Valor Total (R$)", "Média (R$)", "Maior (R$)");
        System.out.println("-------------------------------------------------------------------------------");
        for (CamilaRibeiroSuspeitasECategorias.EstatisticaCategoria est : resAnalise2.categorias.values()) {
            System.out.printf("%-15s %-15d %-20.2f %-15.2f %-15.2f\n",
                    est.nome, est.quantidade, est.valorTotal, est.getMedia(), est.maiorValor);
        }
        System.out.println();

        System.out.println("[Maria Clara Pastor - Benchmark e Métricas de Desempenho]");
        System.out.println("---------------- SEQUENCIAL -----------------");

        HelenaVieiraAgregacao.processarSequencial(dados);
        MariaClaraPastorBenchmarkEAfinidade.aplicarAfinidade(0);

        long tSeqIni = System.currentTimeMillis();
        HelenaVieiraAgregacao.processarSequencial(dados);
        long tempoSeq = Math.max(1, System.currentTimeMillis() - tSeqIni);
        System.out.printf("\nTempo: %,d ms\n\n", tempoSeq);

        java.util.List<MariaClaraPastorBenchmarkEAfinidade.MetricaExperimento> metricas = new java.util.ArrayList<>();
        MariaClaraPastorBenchmarkEAfinidade.MetricaExperimento mSeq = 
                new MariaClaraPastorBenchmarkEAfinidade.MetricaExperimento("Sequencial", 1, quantidadeTransacoes, tempoSeq);
        metricas.add(mSeq);

        System.out.println("---------------- EXECUTOR -------------------");
        int[] threads = {2, 4, 8};
        long[] temposExecutor = new long[threads.length];

        for (int i = 0; i < threads.length; i++) {
            int th = threads[i];
            long tIni = System.currentTimeMillis();
            try {
                HelenaVieiraAgregacao.processarParalelo(dados, th);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long tPar = Math.max(1, System.currentTimeMillis() - tIni);
            temposExecutor[i] = tPar;
            double speedup = MariaClaraPastorBenchmarkEAfinidade.calcularSpeedup(tempoSeq, tPar);
            double eficiencia = MariaClaraPastorBenchmarkEAfinidade.calcularEficiencia(speedup, th);

            MariaClaraPastorBenchmarkEAfinidade.MetricaExperimento m = 
                    new MariaClaraPastorBenchmarkEAfinidade.MetricaExperimento("Executor", th, quantidadeTransacoes, tPar);
            m.speedup = speedup;
            m.eficiencia = eficiencia;
            metricas.add(m);

            System.out.printf("\nThreads: %d\n", th);
            System.out.printf("Tempo: %,d ms\n", tPar);
            System.out.printf(Locale.forLanguageTag("pt-BR"), "Speedup: %.2f\n", speedup);
            System.out.printf(Locale.forLanguageTag("pt-BR"), "Eficiência: %.1f%%\n", eficiencia);
        }

        System.out.println("\n----------- EXECUTOR + AFFINITY -------------");
        String[] cpusDesc = {"0,1", "0,1,2,3", "0,1,2,3,4,5,6,7"};
        long[] temposAffinity = new long[threads.length];

        for (int i = 0; i < threads.length; i++) {
            int th = threads[i];
            long tIni = System.currentTimeMillis();
            try {
                HelenaVieiraAgregacao.processarComAfinidade(dados, th);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long tAff = Math.max(1, System.currentTimeMillis() - tIni);
            temposAffinity[i] = tAff;
            double speedup = MariaClaraPastorBenchmarkEAfinidade.calcularSpeedup(tempoSeq, tAff);
            double eficiencia = MariaClaraPastorBenchmarkEAfinidade.calcularEficiencia(speedup, th);

            MariaClaraPastorBenchmarkEAfinidade.MetricaExperimento m = 
                    new MariaClaraPastorBenchmarkEAfinidade.MetricaExperimento("Executor + Affinity", th, quantidadeTransacoes, tAff);
            m.speedup = speedup;
            m.eficiencia = eficiencia;
            metricas.add(m);

            System.out.printf("\nThreads: %d\n", th);
            System.out.printf("CPUs: %s\n", cpusDesc[i]);
            System.out.printf("Tempo: %,d ms\n", tAff);
            System.out.printf(Locale.forLanguageTag("pt-BR"), "Speedup: %.2f\n", speedup);
            System.out.printf(Locale.forLanguageTag("pt-BR"), "Eficiência: %.1f%%\n", eficiencia);
        }

        MariaClaraPastorBenchmarkEAfinidade.exibirTabela(metricas);
        MariaClaraPastorBenchmarkEAfinidade.salvarResultadosCsv(metricas, "resultados.csv");

        System.out.println("\n==============================================");
        System.out.println(" EXPERIMENTOS CONCLUÍDOS COM SUCESSO!");
        System.out.println("==============================================");
    }
}
