package it.arsinfo.smd.entity;

import java.math.BigDecimal;
import java.util.EnumSet;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import it.arsinfo.smd.data.Mese;
import it.arsinfo.smd.data.TipoPubblicazione;

@Entity
public class Pubblicazione implements SmdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nome;

    private String autore;

    private String editore;

    private boolean active = true;

    private boolean abbonamento = false;

    @Enumerated(EnumType.STRING)
    private Mese primaPubblicazione=Mese.GENNAIO;

    private BigDecimal costoUnitario=BigDecimal.ZERO;

    private BigDecimal costoScontato=BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private TipoPubblicazione tipo=TipoPubblicazione.UNICO;

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoPubblicazione getTipo() {
        return tipo;
    }

    public void setTipo(TipoPubblicazione tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return String.format("Pubblicazione[id=%d, Nome='%s', Tipo='%s', Prima Pubblicazione='%s', CostoUnitario='%f', CostoScontato='%f']",
                             id, nome, tipo, primaPubblicazione,costoUnitario,costoScontato);
    }

    public Pubblicazione(String nome, TipoPubblicazione tipo) {
        super();
        this.nome = nome;
        this.tipo = tipo;
    }

    public Pubblicazione(String nome) {
        super();
        this.nome = nome;
    }

    public Pubblicazione() {
        super();
        this.nome = "AAA";
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isAbbonamento() {
        return abbonamento;
    }

    public void setAbbonamento(boolean abbonamento) {
        this.abbonamento = abbonamento;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costo) {
        this.costoUnitario = costo;
    }

    public String getAutore() {
        return autore;
    }

    public void setAutore(String autore) {
        this.autore = autore;
    }

    public String getEditore() {
        return editore;
    }

    public void setEditore(String editore) {
        this.editore = editore;
    }

    public Mese getPrimaPubblicazione() {
        return primaPubblicazione;
    }

    public void setPrimaPubblicazione(Mese primapubblicazione) {
        this.primaPubblicazione = primapubblicazione;
    }

    public BigDecimal getCostoScontato() {
        if (costoScontato == BigDecimal.ZERO) {
            return costoUnitario;
        }
        return costoScontato;
    }

    public void setCostoScontato(BigDecimal costoScontato) {
        this.costoScontato = costoScontato;
    }

    @Transient
    public String getMesiUscita() {
        final StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (Mese m : getMesiPubblicazione()) {
            if (!first) {
                sb.append(",");
            }
            sb.append(m.getNomeBreve());
            first=false;
        }
        return sb.toString();
    }
    
    @Transient
    public EnumSet<Mese> getMesiPubblicazione() {
        EnumSet<Mese> mesi = null;
        switch (tipo) {
        case UNICO:
            mesi = EnumSet.of(primaPubblicazione);
            break;
        case ANNUALE:
            mesi = EnumSet.of(primaPubblicazione);
            break;
        case SEMESTRALE:
            mesi = EnumSet.of(primaPubblicazione, Mese.getByPosizione(primaPubblicazione.getPosizione()+6));
            break;
        case MENSILE:
            mesi = EnumSet.allOf(Mese.class);
        }
        return mesi;
    }

    @Transient
    public String getCaption() {
        return String.format("%s, %s. EUR:%f.", nome, tipo, costoUnitario);
    }

    @Transient
    public String getHeader() {
        return String.format("Pubblicazione:Edit:'%s'", nome);
    }
}
