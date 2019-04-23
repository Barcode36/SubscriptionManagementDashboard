package it.arsinfo.smd.ui.vaadin;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Notification;

import it.arsinfo.smd.entity.Anagrafica;
import it.arsinfo.smd.entity.Nota;
import it.arsinfo.smd.entity.Storico;
import it.arsinfo.smd.repository.AbbonamentoDao;
import it.arsinfo.smd.repository.AnagraficaDao;
import it.arsinfo.smd.repository.CampagnaDao;
import it.arsinfo.smd.repository.NotaDao;
import it.arsinfo.smd.repository.PubblicazioneDao;
import it.arsinfo.smd.repository.StoricoDao;

@SpringUI(path = SmdUI.URL_ANAGRAFICA)
@Title("Anagrafica Clienti ADP")
public class AnagraficaUI extends SmdUI {

    /**
     * 
     */
    private static final long serialVersionUID = 7884064928998716106L;

    @Autowired
    AnagraficaDao anagraficaDao;
    @Autowired
    PubblicazioneDao pubblicazioneDao;
    @Autowired
    StoricoDao storicoDao;
    @Autowired
    AbbonamentoDao abbonamentoDao;
    @Autowired
    NotaDao notaDao;
    @Autowired
    CampagnaDao campagnaDao;

    @Override
    protected void init(VaadinRequest request) {
        super.init(request, "Anagrafica");
        AnagraficaAdd add = new AnagraficaAdd("Aggiungi ad Anagrafica");
        AnagraficaSearch search = new AnagraficaSearch(anagraficaDao,storicoDao);
        AnagraficaGrid grid = new AnagraficaGrid("Anagrafiche");
        AnagraficaEditor editor = new AnagraficaEditor(anagraficaDao);
        
        StoricoAdd storicoAdd = new StoricoAdd("Aggiungi Storico");
        StoricoEditor storicoEditor = 
                new StoricoEditor(
                      storicoDao,
                      pubblicazioneDao.findAll(),
                      anagraficaDao.findAll()
        ) {
            @Override
            public void save() {
                if (getPubblicazione().isEmpty()) {
                    Notification.show("Pubblicazione deve essere valorizzata");
                    return;
                }
                super.save();
                if (!getNota().isEmpty()) {
                    Nota nota = new Nota(get());
                    nota.setDescription(getNota().getValue());
                    notaDao.save(nota);
                    getNota().clear();
                }
            }
        };
        StoricoGrid storicoGrid = new StoricoGrid("Storico");
        NotaGrid notaGrid = new NotaGrid("Note");
        notaGrid.getGrid().setColumns("data","description");
        notaGrid.getGrid().setHeight("200px");
        
        addSmdComponents(
                         storicoAdd,
                         editor, 
                         storicoEditor,
                         storicoGrid, 
                         notaGrid,
                         add,
                         search, 
                         grid);

        storicoAdd.setVisible(false);
        storicoEditor.setVisible(false);
        storicoGrid.setVisible(false);
        notaGrid.setVisible(false);
        editor.setVisible(false);

        notaGrid.setChangeHandler(() -> {});
        
        add.setChangeHandler(() -> {
            setHeader("Anagrafica:Nuova");
            hideMenu();
            add.setVisible(false);
            search.setVisible(false);
            grid.setVisible(false);
            editor.edit(add.generate());
        });

        search.setChangeHandler(() -> {
            grid.populate(search.find());
        });
        
        grid.setChangeHandler(() -> {
            if (grid.getSelected() == null) {
                return;
            }
            setHeader(grid.getSelected().getHeader());
            hideMenu();
            add.setVisible(false);
            search.setVisible(false);
            editor.edit(grid.getSelected());
            storicoAdd.setIntestatario(grid.getSelected());
            storicoAdd.setVisible(true);
            storicoGrid.populate(findByCustomer(grid.getSelected()));
        });

        editor.setChangeHandler(() -> {
            grid.populate(search.find());
            editor.setVisible(false);
            setHeader("Anagrafica");
            showMenu();
            storicoGrid.setVisible(false);
            storicoAdd.setVisible(false);
            add.setVisible(true);
            search.setVisible(true);
        });

        storicoGrid.setChangeHandler(() -> {
            if (storicoGrid.getSelected() == null) {
                return;
            }
            setHeader(storicoGrid.getSelected().getHeader());
            //FIXME to be better defined (the status should be calculated based on the user operation
            //storicoGrid.getSelected().
            //    setStatoStorico(Smd.getStatoStorico(storicoGrid.getSelected(),
            //                   abbonamentoDao.findByIntestatario(storicoGrid.getSelected().getIntestatario())));
            //storicoDao.save(storicoGrid.getSelected());
            storicoEditor.edit(storicoGrid.getSelected());
            notaGrid.populate(notaDao.findByStorico(storicoGrid.getSelected()));
            add.setVisible(false);
            search.setVisible(false);
            editor.setVisible(false);
            storicoAdd.setVisible(false);
        });

        storicoEditor.setChangeHandler(() -> {
            storicoGrid.populate(findByCustomer(grid.getSelected()));
            setHeader(grid.getSelected().getHeader());
            editor.setVisible(true);
            storicoEditor.setVisible(false);
            notaGrid.setVisible(false);
            storicoAdd.setVisible(true);
        });

        storicoAdd.setChangeHandler(() -> {
            storicoEditor.edit(storicoAdd.generate());
            setHeader(String.format("%s:Storico:Nuovo",editor.get().getHeader()));
            storicoAdd.setVisible(false);
            editor.setVisible(false);
        });

        grid.populate(search.findAll());

    }
    
    public List<Storico> findByCustomer(Anagrafica customer) {
        List<Storico> list = storicoDao.findByIntestatario(customer);
        list.addAll(storicoDao.findByDestinatario(customer)
                    .stream()
                    .filter(ap -> customer.getId() != ap.getIntestatario().getId())
                    .collect(Collectors.toList()));
        return list;
    }

}
