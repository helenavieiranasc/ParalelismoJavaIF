package jcrpgpd;

import java.util.Random;

public class GeradorDados {

    private static final String[] CATEGORIAS = {
        "Alimentação", "Transporte", "Eletrônicos", "Saúde", "Educação"
    };

    private static final String[] CIDADES = {
        "São Paulo", "Campinas", "Jacareí", "São José dos Campos", "Sorocaba"
    };

    private static final String[] ESTADOS = {
        "SP", "RJ", "MG", "PR", "SC"
    };

    private static final String[] TIPOS = {
        "PIX", "Crédito", "Débito", "Boleto", "Transferência"
    };

    public static Transacao[] gerarTransacoes(int quantidade) {
        Transacao[] transacoes = new Transacao[quantidade];
        Random random = new Random(42); 

        for (int i = 0; i < quantidade; i++) {
            long id = 10001L + i;
            String cliente = "C" + String.format("%04d", (i % 5000) + 1);
            String data = "2026-10-" + String.format("%02d", (i % 28) + 1);

            int distTipo = i % 4;
            double valor;
            String categoria;

            switch (distTipo) {
                case 0:

                    valor = 10.0 + random.nextDouble() * (500.0 - 10.0);
                    categoria = CATEGORIAS[random.nextInt(CATEGORIAS.length)];
                    break;

                case 1:

                    valor = Math.max(5.0, 150.0 + random.nextGaussian() * 40.0);
                    categoria = (random.nextBoolean()) ? "Alimentação" : "Transporte";
                    break;

                case 2:

                    double u = Math.max(0.0001, random.nextDouble());
                    valor = Math.max(2.0, -80.0 * Math.log(1.0 - u));
                    categoria = (random.nextBoolean()) ? "Saúde" : "Educação";
                    break;

                case 3:
                default:

                    double up = Math.max(0.001, random.nextDouble());
                    double alpha = 1.6; 
                    double xm = 50.0;
                    valor = xm / Math.pow(1.0 - up, 1.0 / alpha);

                    if (valor > 100_000.0) valor = 100_000.0;
                    categoria = "Eletrônicos";
                    break;
            }

            valor = Math.round(valor * 100.0) / 100.0;

            String cidade = CIDADES[random.nextInt(CIDADES.length)];
            String estado = ESTADOS[random.nextInt(ESTADOS.length)];
            String tipo = TIPOS[random.nextInt(TIPOS.length)];

            transacoes[i] = new Transacao(id, cliente, data, valor, categoria, cidade, estado, tipo);
        }

        return transacoes;
    }
}
