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

import it.arsinfo.smd.Smd;
import it.arsinfo.smd.data.Anno;
import it.arsinfo.smd.data.Mese;
import it.arsinfo.smd.data.TipoPubblicazione;

@Entity
public class Pubblicazione implements SmdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nome;

    private String descrizione;

    private String autore;

    private String editore;

    private boolean active = true;
    
    private BigDecimal speseSpedizione=BigDecimal.ZERO;
    private BigDecimal costoUnitario=BigDecimal.ZERO;
    private BigDecimal abbonamentoItalia=BigDecimal.ZERO;
    private BigDecimal abbonamentoWeb=BigDecimal.ZERO;
    private BigDecimal abbonamentoEuropa=BigDecimal.ZERO;
    private BigDecimal abbonamentoAmericaAsiaAfrica=BigDecimal.ZERO;
    private BigDecimal abbonamentoSostenitore=BigDecimal.ZERO;
    private BigDecimal abbonamentoConSconto=BigDecimal.ZERO;

    private boolean gen = false;
    private boolean feb = false;
    private boolean mar = false;
    private boolean apr = false;
    private boolean mag = false;
    private boolean giu = false;
    private boolean lug = false;
    private boolean ago = false;
    private boolean set = false;
    private boolean ott = false;
    private boolean nov = false;
    private boolean dic = false;

    private int anticipoSpedizione=3;
    
    private int maxSpedizioniere=0;

    @Enumerated(EnumType.STRING)
    private Anno anno=Smd.getAnnoCorrente();
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
        return String.format("Pubblicazione[id=%d, Nome='%s', Tipo='%s', Pubblicazione='%s', CostoUnitario='%.2f', CostoScontato='%.2f']",
                             id, nome, tipo, getMesiPubblicazione(),costoUnitario,abbonamentoItalia);
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

    @Transient
    public String getPubblicato() {
        final StringBuffer sb = new StringBuffer();
        switch (tipo) {
        case UNICO:
            sb.append(anno.getAnnoAsString());
            break;
        case ANNUALE:
            sb.append(getMesiPubblicazione().iterator().next().getNomeBreve());
            break;
        case SEMESTRALE:
            for (Mese m : getMesiPubblicazione()) {
                sb.append(m.getNomeBreve());
            }
            break;
        case MENSILE:
            sb.append("Gen.-Dic.");
            break;
        default:
            break;
        }

        return sb.toString();
    }
    
    @Transient
    public EnumSet<Mese> getMesiPubblicazione() {
        EnumSet<Mese> mesi = EnumSet.noneOf(Mese.class);
        if (isGen()) 
            mesi.add(Mese.GENNAIO);
        if (isFeb()) 
            mesi.add(Mese.FEBBRAIO);
        if (isMar()) 
            mesi.add(Mese.MARZO);
        if (isApr()) 
            mesi.add(Mese.APRILE);
        if (isMag()) 
            mesi.add(Mese.MAGGIO);
        if (isGiu()) 
            mesi.add(Mese.GIUGNO);
        if (isLug()) 
            mesi.add(Mese.LUGLIO);
        if (isAgo()) 
            mesi.add(Mese.AGOSTO);
        if (isSet()) 
            mesi.add(Mese.SETTEMBRE);
        if (isOtt()) 
            mesi.add(Mese.OTTOBRE);
        if (isNov()) 
            mesi.add(Mese.NOVEMBRE);
        if (isDic()) 
            mesi.add(Mese.DICEMBRE);
        return mesi;
    }

    @Transient
    public String getDecodeAttivo() {
        return Smd.decodeForGrid(active);
    }
    
    @Transient
    public String getCaption() {
        return String.format("%s, %s. EUR:%f.", nome, tipo, costoUnitario);
    }

    @Transient
    public String getHeader() {
        return String.format("Pubblicazione:Edit:'%s'", nome);
    }

    public Anno getAnno() {
        return anno;
    }

    public void setAnno(Anno anno) {
        this.anno = anno;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public BigDecimal getAbbonamentoItalia() {
        return abbonamentoItalia;
    }

    public void setAbbonamentoItalia(BigDecimal abbonamentoItalia) {
        this.abbonamentoItalia = abbonamentoItalia;
    }

    public BigDecimal getAbbonamentoEuropa() {
        return abbonamentoEuropa;
    }

    public void setAbbonamentoEuropa(BigDecimal abbonamentoEuropa) {
        this.abbonamentoEuropa = abbonamentoEuropa;
    }

    public BigDecimal getAbbonamentoAmericaAsiaAfrica() {
        return abbonamentoAmericaAsiaAfrica;
    }

    public void setAbbonamentoAmericaAsiaAfrica(
            BigDecimal abbonamentoAmericaAsiaAfrica) {
        this.abbonamentoAmericaAsiaAfrica = abbonamentoAmericaAsiaAfrica;
    }

    public BigDecimal getAbbonamentoSostenitore() {
        return abbonamentoSostenitore;
    }

    public void setAbbonamentoSostenitore(BigDecimal abbonamentoSostenitore) {
        this.abbonamentoSostenitore = abbonamentoSostenitore;
    }

    public BigDecimal getAbbonamentoConSconto() {
        return abbonamentoConSconto;
    }

    public void setAbbonamentoConSconto(BigDecimal abbonamentoConSconto) {
        this.abbonamentoConSconto = abbonamentoConSconto;
    }

    public boolean isGen() {
        return gen;
    }

    public void setGen(boolean gen) {
        this.gen = gen;
    }

    public boolean isFeb() {
        return feb;
    }

    public void setFeb(boolean feb) {
        this.feb = feb;
    }

    public boolean isMar() {
        return mar;
    }

    public void setMar(boolean mar) {
        this.mar = mar;
    }

    public boolean isApr() {
        return apr;
    }

    public void setApr(boolean apr) {
        this.apr = apr;
    }

    public boolean isMag() {
        return mag;
    }

    public void setMag(boolean mag) {
        this.mag = mag;
    }

    public boolean isGiu() {
        return giu;
    }

    public void setGiu(boolean giu) {
        this.giu = giu;
    }

    public boolean isLug() {
        return lug;
    }

    public void setLug(boolean lug) {
        this.lug = lug;
    }

    public boolean isAgo() {
        return ago;
    }

    public void setAgo(boolean ago) {
        this.ago = ago;
    }

    public boolean isSet() {
        return set;
    }

    public void setSet(boolean set) {
        this.set = set;
    }

    public boolean isOtt() {
        return ott;
    }

    public void setOtt(boolean ott) {
        this.ott = ott;
    }

    public boolean isNov() {
        return nov;
    }

    public void setNov(boolean nov) {
        this.nov = nov;
    }

    public boolean isDic() {
        return dic;
    }

    public void setDic(boolean dic) {
        this.dic = dic;
    }

    public BigDecimal getAbbonamentoWeb() {
        return abbonamentoWeb;
    }

    public void setAbbonamentoWeb(BigDecimal abbonamentoWeb) {
        this.abbonamentoWeb = abbonamentoWeb;
    }

    public int getAnticipoSpedizione() {
        return anticipoSpedizione;
    }

    public void setAnticipoSpedizione(int anticipoSpedizione) {
        this.anticipoSpedizione = anticipoSpedizione;
    }

    public BigDecimal getSpeseSpedizione() {
        return speseSpedizione;
    }

    public void setSpeseSpedizione(BigDecimal speseSpedizione) {
        this.speseSpedizione = speseSpedizione;
    }

    public int getMaxSpedizioniere() {
        return maxSpedizioniere;
    }

    public void setMaxSpedizioniere(int maxSpedizioniere) {
        this.maxSpedizioniere = maxSpedizioniere;
    }
}
