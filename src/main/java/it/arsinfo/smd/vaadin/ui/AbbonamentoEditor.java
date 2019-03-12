package it.arsinfo.smd.vaadin.ui;

import java.math.BigDecimal;
import java.util.EnumSet;

import com.vaadin.data.Binder;
import com.vaadin.data.converter.LocalDateToDateConverter;
import com.vaadin.data.converter.StringToBigDecimalConverter;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

import it.arsinfo.smd.data.Anno;
import it.arsinfo.smd.data.Cassa;
import it.arsinfo.smd.data.ContoCorrentePostale;
import it.arsinfo.smd.data.Mese;
import it.arsinfo.smd.entity.Abbonamento;
import it.arsinfo.smd.entity.Anagrafica;
import it.arsinfo.smd.repository.AbbonamentoDao;
import it.arsinfo.smd.repository.AnagraficaDao;
import it.arsinfo.smd.repository.PubblicazioneDao;
import it.arsinfo.smd.vaadin.model.SmdEditor;

public class AbbonamentoEditor extends SmdEditor<Abbonamento> {

    private final ComboBox<Anagrafica> intestatario = new ComboBox<Anagrafica>("Intestatario");
    private final ComboBox<Anno> anno = new ComboBox<Anno>("Selezionare Anno",
            EnumSet.allOf(Anno.class));
    private final ComboBox<Mese> inizio = new ComboBox<Mese>("Selezionare Inizio",
            EnumSet.allOf(Mese.class));
    private final ComboBox<Mese> fine = new ComboBox<Mese>("Selezionare Fine",
          EnumSet.allOf(Mese.class));

    private final TextField costo = new TextField("Costo");
    private final ComboBox<Cassa> cassa = new ComboBox<Cassa>("Cassa",
            EnumSet.allOf(Cassa.class));
    private final TextField campo = new TextField("V Campo Poste Italiane");
    private final ComboBox<ContoCorrentePostale> contoCorrentePostale = new ComboBox<ContoCorrentePostale>("Selezionare ccp",
            EnumSet.allOf(ContoCorrentePostale.class));
    private final TextField spese = new TextField("Spese Spedizione");

    private final CheckBox pagato = new CheckBox("Pagato");
    private final DateField incasso = new DateField("Incassato");

    private HorizontalLayout pub = new HorizontalLayout();



    public AbbonamentoEditor(AbbonamentoDao repo, AnagraficaDao anagraficaDao,
            PubblicazioneDao pubblDao) {

        super(repo,new Binder<>(Abbonamento.class));

        HorizontalLayout pri = new HorizontalLayout(intestatario,
                                                    anno, inizio, fine);
        HorizontalLayout sec = new HorizontalLayout(costo, cassa, campo,
                                                    contoCorrentePostale,spese);
        
        HorizontalLayout pag = new HorizontalLayout(pagato,incasso);

        setComponents(pri, sec, pub, pag, getActions());

        anno.setItemCaptionGenerator(Anno::getAnnoAsString);

        inizio.setItemCaptionGenerator(Mese::getNomeBreve);
        fine.setItemCaptionGenerator(Mese::getNomeBreve);

        contoCorrentePostale.setItemCaptionGenerator(ContoCorrentePostale::getCcp);

        intestatario.setItems(anagraficaDao.findAll());
        intestatario.setItemCaptionGenerator(Anagrafica::getCaption);

        getBinder().forField(intestatario).asRequired().withValidator(an -> an != null,
                                                               "Scegliere un Cliente").bind(Abbonamento::getIntestatario,
                                                                                            Abbonamento::setIntestatario);
        getBinder().forField(anno).bind("anno");
        getBinder().forField(inizio).bind("inizio");
        getBinder().forField(fine).bind("fine");

        getBinder().forField(costo).asRequired().withConverter(new StringToBigDecimalConverter("Conversione in Eur")).bind(Abbonamento::getCosto,
                                                                                                                           Abbonamento::setCosto);
        getBinder().forField(cassa).bind("cassa");
        getBinder().forField(campo).asRequired().withValidator(ca -> ca != null,
                "Deve essere definito").bind(Abbonamento::getCampo,
                                             Abbonamento::setCampo);
        getBinder().forField(contoCorrentePostale).bind("contoCorrentePostale");
        

        getBinder().forField(spese).asRequired().withConverter(new StringToBigDecimalConverter("Conversione in Eur")).bind(Abbonamento::getSpese,
                                                                                                                      Abbonamento::setSpese);
        getBinder().forField(pagato).bind("pagato");
        getBinder().forField(incasso).withConverter(new LocalDateToDateConverter()).bind("incasso");

    }

    @Override
    public void focus(boolean persisted, Abbonamento abbonamento) {
        getCancel().setVisible(persisted);

        intestatario.setReadOnly(persisted);

        spese.setReadOnly(persisted);

        anno.setReadOnly(persisted);
        inizio.setReadOnly(persisted);
        fine.setReadOnly(persisted);
        costo.setVisible(persisted);
        costo.setReadOnly(persisted);
        campo.setVisible(persisted);
        campo.setReadOnly(persisted);

        if (persisted && abbonamento.getCosto() == BigDecimal.ZERO) {
            getSave().setEnabled(false);
            getCancel().setEnabled(false);
            pagato.setVisible(false);
            pagato.setReadOnly(true);
            incasso.setVisible(false);
            incasso.setReadOnly(true);
            return;

        }

        if (persisted && abbonamento.isPagato()) {
            getSave().setEnabled(false);
            getCancel().setEnabled(false);
            pagato.setVisible(true);
            pagato.setReadOnly(true);
            incasso.setVisible(true);
            incasso.setReadOnly(true);
            return;
        }

        if (persisted) {
            getSave().setEnabled(true);
            getCancel().setEnabled(true);
            pagato.setVisible(true);
            incasso.setVisible(true);
            return;
        }

        getSave().setEnabled(true);
        getCancel().setEnabled(false);
        pagato.setVisible(false);
        incasso.setVisible(false);

        intestatario.focus();

    }
}
