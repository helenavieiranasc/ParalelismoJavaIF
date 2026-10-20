package jcrpgpd;

import java.util.*;
import java.util.concurrent.*;

public class CamilaRibeiroSuspeitasECategorias {

    public static class EstatisticaCategoria {
        public String nome;
        public long quantidade;
        public double valorTotal;
        public double maiorValor;

        public EstatisticaCategoria(String nome) {
            this.nome = nome;
            this.quantidade = 0;
            this.valorTotal = 0.0;
            this.maiorValor = 0.0;
        }

        public double getMedia() {
            return (quantidade > 0) ? (valorTotal / quantidade) : 0.0;
        }

        public void combinar(EstatisticaCategoria outro) {
            this.quantidade += outro.quantidade;
            this.valorTotal += outro.valorTotal;
            if (outro.maiorValor > this.maiorValor) {
                this.maiorValor = outro.maiorValor;
            }
        }
    }

    public static class ResultadoAnalise2 {
        public long totalSuspeitas;
        public double somaValorSuspeitas;
        public Map<String, EstatisticaCategoria> categorias;

        public ResultadoAnalise2() {
            this.totalSuspeitas = 0;
            this.somaValorSuspeitas = 0.0;
            this.categorias = new LinkedHashMap<>();
            for (String cat : new String[]{"Alimentação", "Transporte", "Eletrônicos", "Saúde", "Educação"}) {
                this.categorias.put(cat, new EstatisticaCategoria(cat));
            }
        }

        public void combinar(ResultadoAnalise2 outro) {
            this.totalSuspeitas += outro.totalSuspeitas;
            this.somaValorSuspeitas += outro.somaValorSuspeitas;
            for (Map.Entry<String, EstatisticaCategoria> entry : outro.categorias.entrySet()) {
                String cat = entry.getKey();
                EstatisticaCategoria est = this.categorias.computeIfAbsent(cat, EstatisticaCategoria::new);
                est.combinar(entry.getValue());
            }
        }
    }

    public static ResultadoAnalise2 processarSequencial(Transacao[] transacoes) {
        return processarFaixa(transacoes, 0, transacoes.length);
    }

    public static ResultadoAnalise2 processarFaixa(Transacao[] transacoes, int inicio, int fim) {
        ResultadoAnalise2 res = new ResultadoAnalise2();

        for (int i = inicio; i < fim; i++) {
            Transacao t = transacoes[i];
            double v = t.getValor();
            String cat = t.getCategoria();

            if (v > 10_000.0 || (v > 5_000.0 && "Eletrônicos".equals(cat))) {
                res.totalSuspeitas++;
                res.somaValorSuspeitas += v;
            }

            EstatisticaCategoria est = res.categorias.get(cat);
            if (est != null) {
                est.quantidade++;
                est.valorTotal += v;
                if (v > est.maiorValor) {
                    est.maiorValor = v;
                }
            }
        }

        return res;
    }

    public static ResultadoAnalise2 processarParalelo(Transacao[] transacoes, int numThreads) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        try {
            return executarPool(transacoes, executor, numThreads, false);
        } finally {
            executor.shutdown();
        }
    }

    public static ResultadoAnalise2 processarComAfinidade(Transacao[] transacoes, int numThreads) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        try {
            return executarPool(transacoes, executor, numThreads, true);
        } finally {
            executor.shutdown();
        }
    }

    private static ResultadoAnalise2 executarPool(Transacao[] transacoes, ExecutorService executor,
                                                  int numThreads, boolean usarAfinidade) throws Exception {
        int total = transacoes.length;
        int tamanhoBloco = total / numThreads;
        List<Future<ResultadoAnalise2>> futuros = new ArrayList<>();

        for (int t = 0; t < numThreads; t++) {
            final int cpuId = t % Runtime.getRuntime().availableProcessors();
            final int inicio = t * tamanhoBloco;
            final int fim = (t == numThreads - 1) ? total : inicio + tamanhoBloco;

            Callable<ResultadoAnalise2> tarefa = () -> {
                if (usarAfinidade) {
                    MariaClaraPastorBenchmarkEAfinidade.aplicarAfinidade(cpuId);
                }
                return processarFaixa(transacoes, inicio, fim);
            };

            futuros.add(executor.submit(tarefa));
        }

        ResultadoAnalise2 resultadoFinal = new ResultadoAnalise2();
        for (Future<ResultadoAnalise2> f : futuros) {
            resultadoFinal.combinar(f.get());
        }

        return resultadoFinal;
    }
}
