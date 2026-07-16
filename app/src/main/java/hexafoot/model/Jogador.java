package hexafoot.model;

import java.io.Serializable;

/**
 * Atleta com atributos de desempenho, condição física e disponibilidade.
 */
public class Jogador implements Serializable {
    private String nome;
    private String posicao;
    private int ataque;
    private int defesa;
    private int fisico;
    private int resistenciaFisica; //atributo fixo vindo do csv, define o quanto o jogador cansa mais rapido ou mais devagar
    private float desgasteAcumulado; //guarda a fracao de desgaste que ainda nao completou 1 ponto inteiro
    private int estresse;
    private int cartoesAmarelos;
    private int rodadasAfastamento;
    private String status;

    /**
     * Cria um jogador descansado e ativo. O valor recebido como {@code fisica} é a
     * resistência permanente vinda do CSV, não o físico atual, que começa em 100.
     *
     * @param fisica resistência usada para acelerar ou reduzir o desgaste
     */
    public Jogador(String nome, String posicao, int ataque, int defesa, int fisica, int estresse) {
        this.nome = nome;
        this.posicao = posicao;
        this.ataque = ataque;
        this.defesa = defesa;
        this.fisico = 100; //fisico é o desgaste da partida, todo jogador comeca descansado
        this.resistenciaFisica = fisica; //esse sim é o atributo do csv, usado no calculo do desgaste
        this.desgasteAcumulado = 0;
        this.estresse = estresse;
        this.cartoesAmarelos = 0;
        this.rodadasAfastamento = 0;
        this.status = "Ativo";
    }

    //-----------------Métodos de disciplina e lesão-----------------
    /**
     * Acumula um amarelo e, ao atingir dois, suspende o jogador por uma rodada.
     */
    public void aplicarCartaoAmarelo() {
        this.cartoesAmarelos ++;
        if (this.cartoesAmarelos == 2) { //na copa 2 amarelos já está suspenso
            this.rodadasAfastamento = 1;
            this.status = "Suspenso";
        }
    }

    /**
     * Suspende o jogador por uma rodada sem alterar os amarelos acumulados.
     */
    public void aplicarCartaoVermelho() {
        this.rodadasAfastamento = 1;
        this.status = "Suspenso";
    }

    /**
     * Marca o jogador como lesionado e define o contador de afastamento.
     *
     * @param diasAfastamento quantidade de atualizações pós-jogo até o retorno
     */
    public void sofrerLesao(int diasAfastamento) {
        this.rodadasAfastamento = diasAfastamento;
        this.status = "Lesionado";
    }

    /**
     * Avança um período do afastamento e reativa o jogador quando o contador zera.
     * O mesmo contador também é usado para suspensões.
     */
    public void atualizarLesao() {
        if (this.rodadasAfastamento > 0) {
            this.rodadasAfastamento --;
            if (this.rodadasAfastamento == 0) {
                this.status = "Ativo";
            }
        }
    }

    //-----------------Método de consumo de energia-----------------
    /**
     * Reduz o físico segundo posição, tempo jogado, tática e resistência. Frações de
     * desgaste são acumuladas entre chamadas e o físico nunca fica abaixo de zero.
     *
     * @param minutosJogados duração considerada, sendo 90 uma partida completa
     * @param multiplicadorTatico fator de desgaste da tática; {@code 1} é neutro
     */
    public void consumirEnergia(int minutosJogados, float multiplicadorTatico) {
        float desgaste = 0;

        if (this.posicao.equals("Atacante")) {
            desgaste = 10;
        } else if (this.posicao.equals("Meio-campista")) {
            desgaste = 8;
        } else if (this.posicao.equals("Defensor")) {
            desgaste = 6;
        } else if (this.posicao.equals("Goleiro")) {
            desgaste = 2;
        }
        //o desgaste é proporcional ao tempo jogado e a um fator que vem da tática escolhida pelo técnico (ofensiva, equilibrada ou defensiva)
        float perdaBruta = (desgaste * (minutosJogados / 90f)) * multiplicadorTatico;

        //jogadores com resistencia fisica acima da media (85) cansam mais devagar, abaixo da media cansam mais rapido
        float fatorResistencia = 1f - ((this.resistenciaFisica - 85) / 100f);
        perdaBruta = perdaBruta * fatorResistencia;

        //como o metodo é chamado minuto a minuto, o desgaste de cada chamada é pequeno demais pra virar um int sozinho,
        //entao vamos acumulando a fracao e so tiramos do fisico quando ela completa 1 ponto inteiro
        this.desgasteAcumulado = this.desgasteAcumulado + perdaBruta;
        int perdaEnergia = (int) this.desgasteAcumulado;
        this.desgasteAcumulado = this.desgasteAcumulado - perdaEnergia;

        this.fisico = this.fisico - perdaEnergia;
        if (this.fisico < 0) {
            this.fisico = 0;
        }
    }

    /**
     * Recupera 2 pontos para titular ou 10 para reserva, limitado ao máximo de 100.
     *
     * @param foiTitular indica qual taxa de recuperação deve ser aplicada
     */
    public void recuperarEnergiaPosJogo(boolean foiTitular) {
        if (foiTitular) {
            this.fisico += 2;
        } else {
            this.fisico += 10; //se ficou no banco recupera mais energia
        }

        if (this.fisico > 100) {
            this.fisico = 100;
        }
    }

    //-----------------getters e setters-----------------
    public String getNome() {
        return nome;
    }

    public String getPosicao() {
        return posicao;
    }

    public int getAtaque() {
        return ataque;
    }

    public int getDefesa() {
        return defesa;
    }

    public int getFisico() {
        return fisico;
    }

    public int getEstresse() {
        return estresse;
    }

    public int getCartoesAmarelos() {
        return cartoesAmarelos;
    }

    public int getRodadasAfastamento() {
        return rodadasAfastamento;
    }

    public String getStatus() {
        return status;
    }

    public void setFisico(int fisico) {
        if (fisico < 0) { //fisico minimo é 0
            this.fisico = 0;
        } else if (fisico > 100) { //fisico maximo é 100
            this.fisico = 100;
        } else {
            this.fisico = fisico;
        }
    }

    public void setEstresse(int estresse) {
        this.estresse = estresse;
    }

    public void setCartoesAmarelos(int cartoesAmarelos) {
        this.cartoesAmarelos = cartoesAmarelos;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Considera iguais jogadores com o mesmo nome, respeitando maiúsculas e
     * minúsculas.
     */
    @Override
    public boolean equals(Object outro) { // resolve o bug do jogador duplicado na convocação
        if (this == outro) {
            return true;
        }
        if (outro instanceof Jogador == false) {
            return false;
        }
        Jogador jogadorOutro = (Jogador) outro;
        return this.nome.equals(jogadorOutro.nome);
    }
}
