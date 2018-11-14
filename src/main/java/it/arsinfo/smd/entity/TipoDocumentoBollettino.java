package it.arsinfo.smd.entity;

public enum TipoDocumentoBollettino {
     TIPO247(247,"premarcati Mav"),
     TIPO896(896,"premarcati fatturatori"),
     TIPO674(674,"premarcati non fatturatori"),
     TIPO451(451,"bianchi personalizzati"),
     TIPO123(123,"bianchi");
    
    String bollettino;
    int tipo;
    
    public static TipoDocumentoBollettino getTipoBollettino(int tipo) {
        for (TipoDocumentoBollettino l : TipoDocumentoBollettino.values()) {
            if (l.tipo == tipo) return l;
        }
        throw new IllegalArgumentException("Tipo Documento not found.");
    }
    
    private TipoDocumentoBollettino(int tipo,String descr) {
        this.bollettino=descr;
        this.tipo=tipo;        
    }

    public String getBollettino() {
        return bollettino;
    }

    public void setBollettino(String descr) {
        this.bollettino = descr;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }
    
    
}
