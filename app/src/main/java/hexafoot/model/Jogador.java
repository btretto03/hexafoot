package hexafoot.model;

import java.io.Serializable;

/**
 * Entidade Jogador - Representa cada atleta do time, com atributos como nome, posição, ataque, defesa, físico, estresse, cartões amarelos, rodadas de afastamento e status (ativo, suspenso ou lesionado).
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
    public void aplicarCartaoAmarelo() {
        this.cartoesAmarelos ++;
        if (this.cartoesAmarelos == 2) { //na copa 2 amarelos já está suspenso
            this.rodadasAfastamento = 1;
            this.status = "Suspenso";
        }
    }

    public void aplicarCartaoVermelho() {
        this.rodadasAfastamento = 1;
        this.status = "Suspenso";
    }

    public void sofrerLesao(int diasAfastamento) {
        this.rodadasAfastamento = diasAfastamento;
        this.status = "Lesionado";
    }

    public void atualizarLesao() {
        if (this.rodadasAfastamento > 0) {
            this.rodadasAfastamento --;
            if (this.rodadasAfastamento == 0) {
                this.status = "Ativo";
            }
        }
    }

    //-----------------Método de consumo de energia-----------------
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