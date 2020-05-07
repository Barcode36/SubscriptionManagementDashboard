package it.arsinfo.smd.ui.storico;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Title;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;

import it.arsinfo.smd.dao.AnagraficaDao;
import it.arsinfo.smd.dao.CampagnaDao;
import it.arsinfo.smd.dao.NotaDao;
import it.arsinfo.smd.dao.PubblicazioneDao;
import it.arsinfo.smd.dao.StoricoDao;
import it.arsinfo.smd.data.StatoCampagna;
import it.arsinfo.smd.entity.Anagrafica;
import it.arsinfo.smd.entity.Campagna;
import it.arsinfo.smd.entity.Nota;
import it.arsinfo.smd.entity.Pubblicazione;
import it.arsinfo.smd.entity.Storico;
import it.arsinfo.smd.service.SmdService;
import it.arsinfo.smd.ui.SmdUI;
import it.arsinfo.smd.ui.nota.NotaGrid;
import it.arsinfo.smd.ui.vaadin.SmdButtonComboBox;

@SpringUI(path = SmdUI.URL_STORICO)
@Title("Storico Anagrafica Pubblicazioni ADP")
@Push
public class StoricoUI extends SmdUI {

    /**
     * 
     */
    private static final long serialVersionUID = 7884064928998716106L;

    @Autowired
    private CampagnaDao campagnaDao;
    
    @Autowired
    private PubblicazioneDao pubblicazioneDao;

    @Autowired
    private AnagraficaDao anagraficaDao;

    @Autowired
    private StoricoDao storicoDao;

    @Autowired
    private NotaDao notaDao;

    @Autowired
    private SmdService smdService;

    @Override
    protected void init(VaadinRequest request) {
        super.init(request, "Storico");
        List<Anagrafica> anagrafica = anagraficaDao.findAll();
        List<Pubblicazione> pubblicazioni = pubblicazioneDao.findAll();
        StoricoAdd add = new StoricoAdd("Aggiungi Storico");
        StoricoSearch search = new StoricoSearch(storicoDao,anagrafica,pubblicazioni);
        StoricoGrid grid = new StoricoGrid("Storico");
        
        StoricoEditor editor = 
                new StoricoEditor(
                                  storicoDao, 
                                  pubblicazioni, 
                                  anagrafica) {
            @Override
            public void save() {
                if (!isStoricoValid(this)) {
                    return;
                }
                try {
                    smdService.save(get(), getNote(this));
                    onChange();
                } catch (Exception e) {
                    Notification.show("Non è possibile salvare questo record: ",
                                      Notification.Type.ERROR_MESSAGE);
                }
            }
            
            @Override
            public void delete() {
                smdService.delete(get());
                onChange();
            }
        };
        
        NotaGrid notaGrid = new NotaGrid("Note");
        notaGrid.getGrid().setColumns("operatore","data","description");
        notaGrid.getGrid().setHeight("200px");

        SmdButtonComboBox<Campagna> update = 
            new SmdButtonComboBox<Campagna>("Seleziona", campagnaDao.findAll().stream().filter(c -> c.getStatoCampagna() != StatoCampagna.Chiusa).collect(Collectors.toList()),"Aggiorna Campagna", VaadinIcons.ARCHIVES);
        update.getButton().addStyleName(ValoTheme.BUTTON_PRIMARY);
        update.getComboBox().setItemCaptionGenerator(Campagna::getCaption);
        update.getComboBox().setEmptySelectionAllowed(false);
        addSmdComponents(add,update,editor,notaGrid,search, grid);
        editor.setVisible(false);
        notaGrid.setVisible(false);
        update.setVisible(false);
        
        search.setChangeHandler(()-> {
            grid.populate(search.find());
        });
        
        grid.setChangeHandler(() -> {
            if (grid.getSelected() == null) {
                return;
            }
            hideMenu();
            search.setVisible(false);
            editor.edit(grid.getSelected());
            update.setVisible(true);
            notaGrid.populate(notaDao.findByStorico(grid.getSelected()));
            setHeader(grid.getSelected().getHeader());
            add.setVisible(false);
        });

        editor.setChangeHandler(() -> {
            grid.populate(search.find());
            showMenu();
            search.setVisible(true);
            setHeader("Storico");
            editor.setVisible(false);
            notaGrid.setVisible(false);
            update.setVisible(false);
            add.setVisible(true);
        });

        add.setChangeHandler(() -> {
            editor.edit(add.generate());
            setHeader(String.format("Storico:Nuovo"));
            add.setVisible(false);
        });

        add.getButton().addClickListener(event ->
            search.setVisible(false));
        
        notaGrid.setChangeHandler(() -> {});

        update.setChangeHandler(() -> {
            if (update.getValue() == null) {
                Notification.show("La Campagna da aggiornare deve essere valorizzato", Type.WARNING_MESSAGE);                 
                return;
            }
            try {
                if (editor.get().getNumero() <= 0) {
                    Nota[] note = getUNote(editor, update.getValue(), "rimuovi");
                    smdService.rimuovi(update.getValue(),editor.get(),note);
                } else {
                    Nota[] note = getUNote(editor, update.getValue(), "aggiorna");
                    smdService.aggiorna(update.getValue(),editor.get(),note);
                }
                editor.onChange();
            } catch (Exception e) {
                Notification.show("Campagna ed Abbonamento non aggiornati:" + e.getMessage(), Type.ERROR_MESSAGE);
                return;                    
            }
        });

        grid.populate(search.find());

    }

    private boolean isStoricoValid(StoricoEditor editor) {
        if (editor.getIntestatario().isEmpty()) {
            Notification.show("Intestatario deve essere valorizzato", Type.WARNING_MESSAGE);
            return false;                    
        }
        if (editor.getDestinatario().isEmpty()) {
            Notification.show("Destinatario deve essere valorizzato", Type.WARNING_MESSAGE);
            return false;                    
        }
        if (editor.getPubblicazione().isEmpty()) {
            Notification.show("Pubblicazione deve essere valorizzata", Type.WARNING_MESSAGE);
            return false;
        }        
        return true;
    }
    
    private  Nota[] getNote(StoricoEditor editor) {
        Storico storico = editor.get();
        Nota nota = new Nota(storico);
        nota.setOperatore(getLoggedInUser().getUsername());
        if (storico.getId() == null) {
            nota.setDescription("Nuovo: " + storico.toString());
        } else {
            nota.setDescription("Aggiornato: " + storico.toString());                    
        }
        
        if (editor.getNota().isEmpty()) {
            Nota[] note = {nota}; 
            return note;
        }
        Nota unota = new Nota(storico);
        unota.setOperatore(getLoggedInUser().getUsername());
        unota.setDescription(editor.getNota().getValue());
        editor.getNota().clear();
        Nota[] note = {nota,unota}; 
        return note;
    }

    private  Nota[] getUNote(StoricoEditor editor,Campagna campagna, String action) {
        Nota[] dnote = getNote(editor);
        Nota unota = new Nota(editor.get());
        unota.setOperatore(getLoggedInUser().getUsername());
        unota.setDescription(action+": " + campagna.getCaption());
        Nota[] note = new Nota[dnote.length+1];
        for (int i = 0;i<dnote.length;i++)
            note[i] = dnote[i];
        note[dnote.length] = unota;
        return note;
    }

}