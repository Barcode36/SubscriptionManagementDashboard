package it.arsinfo.smd.vaadin.ui;

import com.vaadin.data.Binder;
import com.vaadin.data.converter.LocalDateToDateConverter;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;

import it.arsinfo.smd.entity.Anagrafica;
import it.arsinfo.smd.entity.Nota;
import it.arsinfo.smd.repository.AnagraficaDao;
import it.arsinfo.smd.repository.NoteDao;
import it.arsinfo.smd.vaadin.model.SmdEditor;

public class NoteEditor extends SmdEditor<Nota> {

    /**
     * 
     */
    private static final long serialVersionUID = 4673834235533544936L;

    private final DateField data = new DateField("data");
    private final TextArea description = new TextArea("Descrizione");
    private final ComboBox<Anagrafica> anagrafica = new ComboBox<Anagrafica>("Selezionare il cliente");

    public NoteEditor(NoteDao repo, AnagraficaDao anadao) {

        super(repo, new Binder<>(Nota.class));
        HorizontalLayout pri = new HorizontalLayout();
        pri.addComponent(anagrafica);
        pri.addComponent(data);
        pri.addComponentsAndExpand(description);
        addComponents(pri, getActions());
        setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);

        description.setWordWrap(false);
        description.setSizeFull();
        anagrafica.setItems(anadao.findAll());
        anagrafica.setItemCaptionGenerator(Anagrafica::getCaption);

        getBinder().forField(anagrafica).asRequired().withValidator(an -> an != null,
                                                               "Scegliere un Cliente").bind(Nota::getAnagrafica,
                                                                                            Nota::setAnagrafica);
        getBinder().forField(description).bind(Nota::getDescription,
                                          Nota::setDescription);
        getBinder().forField(data).withConverter(new LocalDateToDateConverter()).bind("data");
        // Configure and style components
        setSpacing(true);

        setVisible(false);

    }

    @Override
    public void focus(boolean persisted, Nota obj) {
        getSave().setEnabled(!persisted);
        getCancel().setEnabled(false);
        data.setReadOnly(persisted);
        data.setVisible(!persisted);
        anagrafica.setReadOnly(persisted);
        description.setReadOnly(persisted);
        anagrafica.focus();

    }

}
