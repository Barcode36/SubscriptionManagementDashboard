package it.arsinfo.smd.ui.vaadin;

import java.util.EnumSet;
import java.util.List;

import com.vaadin.data.Binder;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

import it.arsinfo.smd.data.Anno;
import it.arsinfo.smd.data.Invio;
import it.arsinfo.smd.data.Omaggio;
import it.arsinfo.smd.entity.Anagrafica;
import it.arsinfo.smd.entity.Pubblicazione;
import it.arsinfo.smd.entity.Spedizione;
import it.arsinfo.smd.repository.SpedizioneDao;

public class SpedizioneEditor
        extends SmdEditor<Spedizione> {

    private final ComboBox<Anagrafica> intestatario = new ComboBox<Anagrafica>("Intestatario");
    private final ComboBox<Anagrafica> destinatario = new ComboBox<Anagrafica>("Destinatario");
    private final ComboBox<Pubblicazione> pubblicazione = new ComboBox<Pubblicazione>("Pubblicazione");
    private final ComboBox<Omaggio> omaggio = new ComboBox<Omaggio>("Omaggio",
                                                                    EnumSet.allOf(Omaggio.class));
    private final ComboBox<Invio> invio = new ComboBox<Invio>("Invio",
                                                              EnumSet.allOf(Invio.class));
    private final ComboBox<Anno> anno = new ComboBox<Anno>("Anno",
            EnumSet.allOf(Anno.class));
    private final TextField numero = new TextField("Numero");

    private final CheckBox sospesa = new CheckBox("Spedizione Sospesa");

    public SpedizioneEditor(
            SpedizioneDao anagraficaPubblicazioneDao,
            List<Pubblicazione> pubblicazioni, List<Anagrafica> anagrafica) {

        super(anagraficaPubblicazioneDao, new Binder<>(Spedizione.class) );
        pubblicazione.setEmptySelectionAllowed(false);
        pubblicazione.setPlaceholder("Pubblicazione");
        pubblicazione.setItems(pubblicazioni);
        pubblicazione.setItemCaptionGenerator(Pubblicazione::getNome);

        intestatario.setEmptySelectionAllowed(false);
        intestatario.setPlaceholder("Intestatario");
        intestatario.setItems(anagrafica);
        intestatario.setItemCaptionGenerator(Anagrafica::getCaption);

        destinatario.setEmptySelectionAllowed(false);
        destinatario.setPlaceholder("Destinatario");
        destinatario.setItems(anagrafica);
        destinatario.setItemCaptionGenerator(Anagrafica::getCaption);

        anno.setEmptySelectionAllowed(false);
        anno.setItemCaptionGenerator(Anno::getAnnoAsString);
        invio.setEmptySelectionAllowed(false);
        omaggio.setEmptySelectionAllowed(false);
        
        setComponents(getActions(), new HorizontalLayout(intestatario, destinatario,
                                           pubblicazione,numero),
                      new HorizontalLayout(anno, omaggio, invio),
                      sospesa);
 
        getBinder()
            .forField(numero)
            .withValidator(str -> str != null, "Inserire un numero")
            .withConverter(new StringToIntegerConverter(""))
            .withValidator(num -> num > 0,"deve essere maggiore di 0")
            .bind(Spedizione::getNumero, Spedizione::setNumero);
        getBinder()
            .forField(pubblicazione)
            .asRequired()
            .withValidator(p -> p != null, "Pubblicazione deve essere selezionata")
            .bind(Spedizione::getPubblicazione,Spedizione::setPubblicazione);
        getBinder().bindInstanceFields(this);

    }

    @Override
    public void focus(boolean persisted, Spedizione obj) {
        intestatario.setValue(obj.getAbbonamento().getIntestatario());
        intestatario.setReadOnly(true);
        anno.setValue(obj.getAbbonamento().getAnno());
        anno.setReadOnly(true);
        destinatario.setReadOnly(persisted);
        pubblicazione.setReadOnly(persisted);
        numero.setReadOnly(persisted);
        omaggio.setReadOnly(persisted);
        getDelete().setEnabled(!persisted);
        destinatario.focus();        
    }

}
