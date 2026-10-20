package jcrpgpd;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class HelenaVieiraAgregacao {

    public static class ResultadoAgregacao {
        public double somaTotal;
        public long quantidade;
        public double maiorValor;
        public double menorValor;

        public ResultadoAgregacao() {
            this.somaTotal = 0.0;
            this.quantidade = 0;
            this.maiorValor = Double.NEGATIVE_INFINITY;
            this.menorValor = Double.POSITIVE_INFINITY;
        }

        public ResultadoAgregacao(double somaTotal, long quantidade, double maiorValor, double menorValor) {
            this.somaTotal = somaTotal;
            this.quantidade = quantidade;
            this.maiorValor = maiorValor;
            this.menorValor = menorValor;
        }

        public double getMedia() {
            return (quantidade > 0) ? (somaTotal / quantidade) : 0.0;
        }

        public void combinar(ResultadoAgregacao outro) {
            this.somaTotal += outro.somaTotal;
            this.quantidade += outro.quantidade;
            if (outro.maiorValor > this.maiorValor) this.maiorValor = outro.maiorValor;
            if (outro.menorValor < this.menorValor) this.menorValor = outro.menorValor;
        }

        @Override
        public String toString() {
            return String.format("Soma=R$ %.2f, Quantidade=%d, Média=R$ %.2f, Maior=R$ %.2f, Menor=R$ %.2f",
                    somaTotal, quantidade, getMedia(), maiorValor, menorValor);
        }
    }

    public static ResultadoAgregacao processarSequencial(Transacao[] transacoes) {
        return processarFaixa(transacoes, 0, transacoes.length);
    }

    public static ResultadoAgregacao processarFaixa(Transacao[] transacoes, int inicio, int fim) {
        double soma = 0.0;
        double maior = Double.NEGATIVE_INFINITY;
        double menor = Double.POSITIVE_INFINITY;
        long qtd = fim - inicio;

        for (int i = inicio; i < fim; i++) {
            double v = transacoes[i].getValor();
            soma += v;
            if (v > maior) maior = v;
            if (v < menor) menor = v;
        }

        return new ResultadoAgregacao(soma, qtd, maior, menor);
    }

    public static ResultadoAgregacao processarParalelo(Transacao[] transacoes, int numThreads) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        try {
            return executarPool(transacoes, executor, numThreads, false);
        } finally {
            executor.shutdown();
        }
    }

    public static ResultadoAgregacao processarComAfinidade(Transacao[] transacoes, int numThreads) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        try {
            return executarPool(transacoes, executor, numThreads, true);
        } finally {
            executor.shutdown();
        }
    }

    private static ResultadoAgregacao executarPool(Transacao[] transacoes, ExecutorService executor, 
                                                   int numThreads, boolean usarAfinidade) throws Exception {
        int total = transacoes.length;
        int tamanhoBloco = total / numThreads;
        List<Future<ResultadoAgregacao>> futuros = new ArrayList<>();

        for (int t = 0; t < numThreads; t++) {
            final int cpuId = t % Runtime.getRuntime().availableProcessors();
            final int inicio = t * tamanhoBloco;
            final int fim = (t == numThreads - 1) ? total : inicio + tamanhoBloco;

            Callable<ResultadoAgregacao> tarefa = () -> {
                if (usarAfinidade) {
                    MariaClaraPastorBenchmarkEAfinidade.aplicarAfinidade(cpuId);
                }
                return processarFaixa(transacoes, inicio, fim);
            };

            futuros.add(executor.submit(tarefa));
        }

        ResultadoAgregacao resultadoFinal = new ResultadoAgregacao();
        for (Future<ResultadoAgregacao> f : futuros) {
            resultadoFinal.combinar(f.get());
        }

        return resultadoFinal;
    }
}
