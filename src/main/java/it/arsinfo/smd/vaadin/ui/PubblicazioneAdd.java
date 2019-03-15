package it.arsinfo.smd.vaadin.ui;

import it.arsinfo.smd.entity.Pubblicazione;
import it.arsinfo.smd.vaadin.model.SmdAdd;

public class PubblicazioneAdd extends SmdAdd<Pubblicazione> {

    public PubblicazioneAdd(String caption) {
        super(caption);
    }
    
    @Override
    public Pubblicazione generate() {
        return new Pubblicazione();
    }

}