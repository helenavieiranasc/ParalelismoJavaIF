package jcrpgpd;

public class Transacao {
    private final long id;
    private final String cliente;
    private final String data;
    private final double valor;
    private final String categoria;
    private final String cidade;
    private final String estado;
    private final String tipo;

    public Transacao(long id, String cliente, String data, double valor, 
                     String categoria, String cidade, String estado, String tipo) {
        this.id = id;
        this.cliente = cliente;
        this.data = data;
        this.valor = valor;
        this.categoria = categoria;
        this.cidade = cidade;
        this.estado = estado;
        this.tipo = tipo;
    }

    public long getId() {
        return id;
    }

    public String getCliente() {
        return cliente;
    }

    public String getData() {
        return data;
    }

    public double getValor() {
        return valor;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getCidade() {
        return cidade;
    }

    public String getEstado() {
        return estado;
    }

    public String getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        return String.format("Transacao[id=%d, cliente=%s, valor=R$ %.2f, cat=%s, tipo=%s]",
                id, cliente, valor, categoria, tipo);
    }
}
