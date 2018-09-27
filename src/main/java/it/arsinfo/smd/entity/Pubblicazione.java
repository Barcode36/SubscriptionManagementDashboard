package it.arsinfo.smd.entity;

import it.arsinfo.smd.entity.Abbonamento.Mese;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Pubblicazione {

	public enum Tipo {
		UNICO,
		MENSILE,
		SEMESTRALE,
		ANNUALE;
	}
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nome;

    private String autore;

    private String editore;

    private boolean active;

    private boolean abbonamento;
    
    @Enumerated(EnumType.STRING)
    private Mese primaPubblicazione;

	private Float costo;
    
    @Enumerated(EnumType.STRING)
    private Tipo tipo;
    
	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}

    @Override
	public String toString() {
		return String.format("Pubblicazione[id=%d, Nome='%s', Tipo='%s']", 
				id, nome, tipo);
	}

	public Pubblicazione(String nome, Tipo tipo) {
		super();
		this.nome = nome;
		this.tipo = tipo;
		this.costo = 1.00f;
	}

	public Pubblicazione(String nome) {
		super();
		this.nome = nome;
		this.tipo = Tipo.UNICO;
		this.costo = 1.00f;
	}
	
	public Pubblicazione() {
		super();
		this.nome = "";
		this.tipo = Tipo.UNICO;
		this.costo = 1.00f;
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

	public Float getCosto() {
		return costo;
	}

	public void setCosto(Float costo) {
		this.costo = costo;
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
	
    

}
