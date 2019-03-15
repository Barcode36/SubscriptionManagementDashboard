package it.arsinfo.smd.data;

public enum Mese {
	GENNAIO("Gen",1, "01"),
	FEBBRAIO("Feb",2, "02"),
	MARZO("Mar",3, "03"),
	APRILE("Apr",4, "04"),
	MAGGIO("Mag",5, "05"),
	GIUGNO("Giu",6, "06"),
	LUGLIO("Lug",7, "07"),
	AGOSTO("Ago",8, "08"),
	SETTEMBRE("Set",9, "09"),
	OTTOBRE("Ott",10, "10"),
	NOVEMBRE("Nov",11, "11"),
	DICEMBRE("Dic",12, "12");
	
	private String nomeBreve;
	private int posizione;
	private String code;
	
	
	private Mese(String nome, int posizione, String cod) {
		this.nomeBreve=nome;
		this.posizione=posizione;
		this.code=cod;
	}


	public String getNomeBreve() {
		return nomeBreve;
	}


	public void setNomeBreve(String nomeBreve) {
		this.nomeBreve = nomeBreve;
	}


	public int getPosizione() {
		return posizione;
	}


	public void setPosizione(int posizione) {
		this.posizione = posizione;
	}


	public String getCode() {
		return code;
	}


	public void setCode(String code) {
		this.code = code;
	}
}